package com.enonic.xp.core.project;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

import com.google.common.io.ByteSource;
import com.google.common.net.HttpHeaders;

import com.enonic.xp.attachment.CreateAttachment;
import com.enonic.xp.attachment.CreateAttachments;
import com.enonic.xp.audit.AuditLogService;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentInheritType;
import com.enonic.xp.content.ContentName;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.CreateContentParams;
import com.enonic.xp.content.DeleteContentParams;
import com.enonic.xp.content.MoveContentParams;
import com.enonic.xp.content.RenameContentParams;
import com.enonic.xp.content.ReorderChildContentsParams;
import com.enonic.xp.content.ReorderChildParams;
import com.enonic.xp.content.SetContentChildOrderParams;
import com.enonic.xp.content.UpdateContentParams;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.core.impl.content.ContentAuditLogExecutorImpl;
import com.enonic.xp.core.impl.content.ContentAuditLogSupportImpl;
import com.enonic.xp.core.impl.content.ContentConfig;
import com.enonic.xp.core.impl.content.ContentServiceImpl;
import com.enonic.xp.core.impl.content.ParentProjectSynchronizer;
import com.enonic.xp.core.impl.media.MediaInfoServiceImpl;
import com.enonic.xp.core.impl.project.ProjectPermissionsContextManagerImpl;
import com.enonic.xp.core.impl.project.ProjectServiceImpl;
import com.enonic.xp.core.impl.schema.content.ContentTypeServiceImpl;
import com.enonic.xp.core.impl.security.SecurityServiceImpl;
import com.enonic.xp.core.impl.site.SiteServiceImpl;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.extractor.BinaryExtractor;
import com.enonic.xp.extractor.ExtractedData;
import com.enonic.xp.form.Form;
import com.enonic.xp.index.ChildOrder;
import com.enonic.xp.page.PageDescriptorService;
import com.enonic.xp.project.CreateProjectParams;
import com.enonic.xp.project.Project;
import com.enonic.xp.project.ProjectName;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.repo.impl.node.AbstractNodeTest;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.schema.content.ContentTypeName;
import com.enonic.xp.schema.mixin.MixinService;
import com.enonic.xp.schema.xdata.XDataService;
import com.enonic.xp.security.IdProviderKey;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.SystemConstants;
import com.enonic.xp.security.User;
import com.enonic.xp.security.auth.AuthenticationInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParentProjectSynchronizerTest
    extends AbstractNodeTest
{

    private static final User REPO_TEST_DEFAULT_USER =
        User.create().key( PrincipalKey.ofUser( IdProviderKey.system(), "repo-test-user" ) ).login( "repo-test-user" ).build();

    private static final AuthenticationInfo REPO_TEST_ADMIN_USER_AUTHINFO = AuthenticationInfo.create().
        principals( RoleKeys.AUTHENTICATED ).
        principals( RoleKeys.ADMIN ).
        user( REPO_TEST_DEFAULT_USER ).
        build();

    private static final Context ADMIN_CONTEXT = ContextBuilder.create().
        branch( "master" ).
        repositoryId( SystemConstants.SYSTEM_REPO_ID ).
        authInfo( REPO_TEST_ADMIN_USER_AUTHINFO ).
        build();

    public Context sourceContext;

    private ProjectServiceImpl projectService;

    private ContentServiceImpl contentService;

    private MediaInfoServiceImpl mediaInfoService;

    private Context targetContext;

    private Project sourceProject;

    private Project targetProject;

    private ParentProjectSynchronizer synchronizer;

    @BeforeEach
    protected void setUpNode()
        throws Exception
    {
        super.setUpNode();

        setUpProjectService();
        setUpContentService();

        synchronizer = new ParentProjectSynchronizer();
        synchronizer.setContentService( contentService );
        synchronizer.setMediaInfoService( mediaInfoService );
    }

    private void setUpProjectService()
    {
        ADMIN_CONTEXT.runWith( () -> {
            SecurityServiceImpl securityService = new SecurityServiceImpl( this.nodeService, indexService );

            securityService.initialize();

            projectService = new ProjectServiceImpl( repositoryService, indexService, nodeService, securityService,
                                                     new ProjectPermissionsContextManagerImpl(), eventPublisher );

            sourceProject = projectService.create( CreateProjectParams.create().
                name( ProjectName.from( "source_project" ) ).
                displayName( "Source Project" ).
                build() );

            targetProject = projectService.create( CreateProjectParams.create().
                name( ProjectName.from( "target_project" ) ).
                displayName( "Target Project" ).
                parent( sourceProject.getName() ).
                build() );

            this.targetContext = ContextBuilder.from( ContextAccessor.current() ).
                repositoryId( targetProject.getName().getRepoId() ).
                branch( ContentConstants.BRANCH_DRAFT ).
                authInfo( REPO_TEST_ADMIN_USER_AUTHINFO ).
                build();

            this.sourceContext = ContextBuilder.from( ContextAccessor.current() ).
                repositoryId( sourceProject.getName().getRepoId() ).
                branch( ContentConstants.BRANCH_DRAFT ).
                authInfo( REPO_TEST_ADMIN_USER_AUTHINFO ).
                build();
        } );
    }

    private void setUpContentService()
    {

        Map<String, List<String>> metadata = new HashMap<>();
        metadata.put( HttpHeaders.CONTENT_TYPE, List.of( "image/jpg" ) );

        final ExtractedData extractedData = ExtractedData.create().
            metadata( metadata ).
            build();

        final BinaryExtractor extractor = Mockito.mock( BinaryExtractor.class );
        Mockito.when( extractor.extract( Mockito.isA( ByteSource.class ) ) ).
            thenReturn( extractedData );

        mediaInfoService = new MediaInfoServiceImpl();
        mediaInfoService.setBinaryExtractor( extractor );

        XDataService xDataService = Mockito.mock( XDataService.class );

        MixinService mixinService = Mockito.mock( MixinService.class );
        Mockito.when( mixinService.inlineFormItems( Mockito.isA( Form.class ) ) ).then( AdditionalAnswers.returnsFirstArg() );

        PageDescriptorService pageDescriptorService = Mockito.mock( PageDescriptorService.class );
        PartDescriptorService partDescriptorService = Mockito.mock( PartDescriptorService.class );
        LayoutDescriptorService layoutDescriptorService = Mockito.mock( LayoutDescriptorService.class );

        ContentTypeServiceImpl contentTypeService = new ContentTypeServiceImpl();
        contentTypeService.setMixinService( mixinService );

        final ResourceService resourceService = Mockito.mock( ResourceService.class );
        final SiteServiceImpl siteService = new SiteServiceImpl();
        siteService.setResourceService( resourceService );
        siteService.setMixinService( mixinService );

        AuditLogService auditLogService = Mockito.mock( AuditLogService.class );
        final ContentConfig contentConfig = Mockito.mock( ContentConfig.class );

        final ContentAuditLogSupportImpl contentAuditLogSupport =
            new ContentAuditLogSupportImpl( contentConfig, new ContentAuditLogExecutorImpl(), auditLogService );

        contentService = new ContentServiceImpl();
        contentService.setNodeService( nodeService );
        contentService.setEventPublisher( eventPublisher );
        contentService.setMediaInfoService( mediaInfoService );
        contentService.setSiteService( siteService );
        contentService.setContentTypeService( contentTypeService );
        contentService.setxDataService( xDataService );
        contentService.setPageDescriptorService( pageDescriptorService );
        contentService.setPartDescriptorService( partDescriptorService );
        contentService.setLayoutDescriptorService( layoutDescriptorService );
        contentService.setFormDefaultValuesProcessor( ( form, data ) -> {
        } );
        contentService.setContentAuditLogSupport( contentAuditLogSupport );
        contentService.initialize();
    }

    @Test
    public void testCreatedChild()
        throws Exception
    {
        final Content sourceParent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content sourceChild = sourceContext.callWith( () -> createContent( sourceParent.getPath() ) );

        final Content targetParent = synchronizer.syncCreated( sourceParent.getId(), sourceProject, targetProject );
        final Content targetChild = synchronizer.syncCreated( sourceChild.getId(), sourceProject, targetProject );

        assertEquals( targetChild.getParentPath(), targetParent.getPath() );

        compareSynched( sourceChild, targetChild );
    }

    @Test
    public void testCreated()
        throws Exception
    {
        final CreateContentParams createContentParams = CreateContentParams.create().
            contentData( new PropertyTree() ).
            displayName( "This is my content" ).
            createAttachments( CreateAttachments.create().
                add( CreateAttachment.create().
                    byteSource( ByteSource.wrap( "bytes".getBytes() ) ).
                    label( "attachment" ).
                    name( "attachmentName" ).
                    mimeType( "image/png" ).
                    build() ).
                build() ).
            parent( ContentPath.ROOT ).
            type( ContentTypeName.folder() ).
            build();

        final Content sourceContent = sourceContext.callWith( () -> this.contentService.create( createContentParams ) );
        final Content targetContent = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        compareSynched( sourceContent, targetContent );
    }

    @Test
    public void testCreateExisted()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );

        final Content targetContent1 = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );
        final Content targetContent2 = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        assertEquals( targetContent1, targetContent2 );

        compareSynched( sourceContent, targetContent1 );
    }

    @Test
    public void testCreatedWithoutParent()
        throws Exception
    {
        final Content sourceParent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content sourceChild = sourceContext.callWith( () -> createContent( sourceParent.getPath() ) );

        assertNull( synchronizer.syncCreated( sourceChild.getId(), sourceProject, targetProject ) );

    }

    @Test
    public void updateNotCreated()
        throws Exception
    {
        assertNull( synchronizer.syncUpdated( ContentId.from( "source" ), sourceProject, targetProject ) );
    }

    @Test
    public void updateNotSynched()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        assertNull( synchronizer.syncUpdated( sourceContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void updateNotChanged()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content targetContent = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        assertEquals( targetContent, synchronizer.syncUpdated( sourceContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void updateDataChanged()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content targetContent = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        sourceContext.runWith( () -> {
            contentService.update( new UpdateContentParams().
                contentId( sourceContent.getId() ).
                editor( ( edit -> edit.data = new PropertyTree() ) ) );
        } );

        final Content targetContentUpdated = synchronizer.syncUpdated( sourceContent.getId(), sourceProject, targetProject );
        assertNotEquals( targetContent.getData(), targetContentUpdated.getData() );
        assertNotEquals( targetContent.getModifiedTime(), targetContentUpdated.getModifiedTime() );
    }

    @Test
    public void renameNotSynched()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        assertNull( synchronizer.syncRenamed( sourceContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void renameNotExisted()
        throws Exception
    {
        assertNull( synchronizer.syncRenamed( ContentId.from( "source" ), sourceProject, targetProject ) );
    }

    @Test
    public void renameNotChanged()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content targetContent = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        assertEquals( targetContent, synchronizer.syncRenamed( sourceContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void renameChanged()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content targetContent = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        sourceContext.runWith( () -> {
            contentService.rename( RenameContentParams.create().
                contentId( sourceContent.getId() ).
                newName( ContentName.from( "newName" ) ).
                build() );
        } );

        final Content targetContentRenamed = synchronizer.syncRenamed( sourceContent.getId(), sourceProject, targetProject );
        assertNotEquals( targetContent.getName(), targetContentRenamed.getName() );
        assertEquals( "newName", targetContentRenamed.getName().toString() );
    }

    @Test
    public void sortNotExisted()
        throws Exception
    {
        assertNull( synchronizer.syncSorted( ContentId.from( "source" ), sourceProject, targetProject ) );
    }

    @Test
    public void sortNotSynched()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        assertNull( synchronizer.syncSorted( sourceContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void sortNotChanged()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content targetContent = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        assertEquals( targetContent, synchronizer.syncSorted( sourceContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void sortChanged()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content targetContent = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        sourceContext.runWith( () -> {
            contentService.setChildOrder( SetContentChildOrderParams.create().
                contentId( sourceContent.getId() ).
                childOrder( ChildOrder.from( "modifiedTime ASC" ) ).
                build() );
        } );

        final Content targetContentSorted = synchronizer.syncSorted( sourceContent.getId(), sourceProject, targetProject );
        assertNotEquals( targetContent.getChildOrder(), targetContentSorted.getChildOrder() );
    }

    @Test
    public void moveNotExisted()
        throws Exception
    {
        assertNull( synchronizer.syncMoved( ContentId.from( "source" ), sourceProject, targetProject ) );
    }

    @Test
    public void moveNotSynched()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        assertNull( synchronizer.syncMoved( sourceContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void moveNotChanged()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content targetContent = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        assertEquals( targetContent, synchronizer.syncMoved( sourceContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void moveChanged()
        throws Exception
    {
        final Content sourceContent1 = sourceContext.callWith( () -> createContent( ContentPath.ROOT, "name1" ) );
        final Content sourceContent2 = sourceContext.callWith( () -> createContent( ContentPath.ROOT, "name2" ) );
        final Content targetContent1 = synchronizer.syncCreated( sourceContent1.getId(), sourceProject, targetProject );
        final Content targetContent2 = synchronizer.syncCreated( sourceContent2.getId(), sourceProject, targetProject );

        assertEquals( "/name1", targetContent1.getPath().toString() );
        assertEquals( "/name2", targetContent2.getPath().toString() );

        sourceContext.runWith( () -> contentService.move( MoveContentParams.create().
            contentId( sourceContent1.getId() ).
            parentContentPath( sourceContent2.getPath() ).
            build() ) );

        final Content targetContentSorted = synchronizer.syncMoved( sourceContent1.getId(), sourceProject, targetProject );
        assertEquals( "/name2/name1", targetContentSorted.getPath().toString() );
    }

    @Test
    public void deleteNotExisted()
        throws Exception
    {
        assertFalse( synchronizer.syncDeleted( ContentId.from( "source" ), sourceProject, targetProject ) );
    }

    @Test
    public void deleteNotSynched()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        assertFalse( synchronizer.syncDeleted( sourceContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void deleteNotDeletedInParent()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content targetContent = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        assertFalse( synchronizer.syncDeleted( targetContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void deleteDeletedInParent()
        throws Exception
    {
        final Content sourceContent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content targetContent = synchronizer.syncCreated( sourceContent.getId(), sourceProject, targetProject );

        sourceContext.runWith( () -> contentService.deleteWithoutFetch( DeleteContentParams.create().
            contentPath( sourceContent.getPath() ).
            build() ) );

        assertTrue( synchronizer.syncDeleted( targetContent.getId(), sourceProject, targetProject ) );
    }

    @Test
    public void updateSorted()
        throws Exception
    {
        final Content sourceParent = sourceContext.callWith( () -> createContent( ContentPath.ROOT ) );
        final Content sourceChild1 = sourceContext.callWith( () -> createContent( sourceParent.getPath(), "child1" ) );
        final Content sourceChild2 = sourceContext.callWith( () -> createContent( sourceParent.getPath(), "child2" ) );

        final Content targetParent = synchronizer.syncCreated( sourceParent.getId(), sourceProject, targetProject );
        final Content targetChild1 = synchronizer.syncCreated( sourceChild1.getId(), sourceProject, targetProject );
        final Content targetChild2 = synchronizer.syncCreated( sourceChild2.getId(), sourceProject, targetProject );

        sourceContext.runWith( () -> {
            contentService.setChildOrder( SetContentChildOrderParams.create().
                contentId( sourceParent.getId() ).
                childOrder( ChildOrder.manualOrder() ).
                build() );

            assertTrue( synchronizer.syncSorted( sourceParent.getId(), sourceProject, targetProject ).getChildOrder().isManualOrder() );

            contentService.reorderChildren( ReorderChildContentsParams.create().
                contentId( sourceParent.getId() ).
                add( ReorderChildParams.create().
                    contentToMove( sourceChild1.getId() ).
                    contentToMoveBefore( sourceChild2.getId() ).
                    build() ).
                build() );

            final Long newManualOrderValue1 =
                synchronizer.syncManualOrderUpdated( sourceChild1.getId(), sourceProject, targetProject ).getManualOrderValue();
            final Long newManualOrderValue2 =
                synchronizer.syncManualOrderUpdated( sourceChild2.getId(), sourceProject, targetProject ).getManualOrderValue();

            assertTrue( newManualOrderValue2 < newManualOrderValue1 );
        } );

    }

    private Content createContent( final ContentPath parent )
    {

        return createContent( parent, "name" );
    }

    private Content createContent( final ContentPath parent, final String name )
    {
        final PropertyTree data = new PropertyTree();
        data.addStrings( "stringField", "stringValue" );

        final CreateContentParams createParent = CreateContentParams.create().
            contentData( data ).
            displayName( name ).
            parent( parent ).
            type( ContentTypeName.folder() ).
            build();

        return this.contentService.create( createParent );
    }

    private void compareSynched( final Content sourceContent, final Content targetContent )
    {
        assertEquals( sourceContent.getId(), targetContent.getId() );
        assertEquals( sourceContent.getName(), targetContent.getName() );
        assertEquals( sourceContent.getDisplayName(), targetContent.getDisplayName() );
        assertEquals( sourceContent.getData(), targetContent.getData() );
        assertEquals( sourceContent.getPath(), targetContent.getPath() );
        assertEquals( sourceContent.getAllExtraData(), targetContent.getAllExtraData() );
        assertEquals( sourceContent.getAttachments(), targetContent.getAttachments() );
        assertEquals( sourceContent.getOwner(), targetContent.getOwner() );
        assertEquals( sourceContent.getLanguage(), targetContent.getLanguage() );
        assertEquals( sourceContent.getWorkflowInfo(), targetContent.getWorkflowInfo() );
        assertEquals( sourceContent.getPage(), targetContent.getPage() );
        assertEquals( sourceContent.isValid(), targetContent.isValid() );
        assertEquals( sourceContent.inheritsPermissions(), targetContent.inheritsPermissions() );
        assertEquals( sourceContent.getCreatedTime(), targetContent.getCreatedTime() );
        assertEquals( sourceContent.getModifiedTime(), targetContent.getModifiedTime() );

        assertNotEquals( sourceContent.getPermissions(), targetContent.getPermissions() );

        assertTrue( targetContent.getInherit().containsAll( EnumSet.allOf( ContentInheritType.class ) ) );
    }
}
