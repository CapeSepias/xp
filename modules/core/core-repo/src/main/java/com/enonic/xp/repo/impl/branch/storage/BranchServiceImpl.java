package com.enonic.xp.repo.impl.branch.storage;

import java.util.Set;

import org.elasticsearch.common.Strings;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import com.enonic.xp.node.NodeBranchEntries;
import com.enonic.xp.node.NodeBranchEntry;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodeIds;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodePaths;
import com.enonic.xp.repo.impl.InternalContext;
import com.enonic.xp.repo.impl.ReturnFields;
import com.enonic.xp.repo.impl.StorageSettings;
import com.enonic.xp.repo.impl.branch.BranchService;
import com.enonic.xp.repo.impl.cache.BranchPath;
import com.enonic.xp.repo.impl.cache.PathCache;
import com.enonic.xp.repo.impl.cache.PathCacheImpl;
import com.enonic.xp.repo.impl.search.result.SearchHit;
import com.enonic.xp.repo.impl.search.result.SearchResult;
import com.enonic.xp.repo.impl.storage.GetByIdRequest;
import com.enonic.xp.repo.impl.storage.GetByIdsRequest;
import com.enonic.xp.repo.impl.storage.GetByValuesRequest;
import com.enonic.xp.repo.impl.storage.GetResult;
import com.enonic.xp.repo.impl.storage.GetResults;
import com.enonic.xp.repo.impl.storage.StaticStorageType;
import com.enonic.xp.repo.impl.storage.StorageDao;
import com.enonic.xp.repo.impl.storage.StoreRequest;
import com.enonic.xp.repo.impl.storage.StoreStorageName;

@Component
public class BranchServiceImpl
    implements BranchService
{
    private static final ReturnFields BRANCH_RETURN_FIELDS =
        ReturnFields.from( BranchIndexPath.NODE_ID, BranchIndexPath.VERSION_ID, BranchIndexPath.STATE, BranchIndexPath.PATH,
                           BranchIndexPath.TIMESTAMP, BranchIndexPath.REFERENCES );

    private StorageDao storageDao;

    private final PathCache pathCache = new PathCacheImpl();

    private static final Logger LOG = LoggerFactory.getLogger( BranchServiceImpl.class );

    @Override
    public String store( final NodeBranchEntry nodeBranchEntry, final InternalContext context )
    {
        return doStore( nodeBranchEntry, context );
    }

    private String doStore( final NodeBranchEntry nodeBranchEntry, final InternalContext context )
    {
        final StoreRequest storeRequest = BranchStorageRequestFactory.create( nodeBranchEntry, context );
        final String id = this.storageDao.store( storeRequest );

        pathCache.cache( createPath( nodeBranchEntry.getNodePath(), context ), BranchDocumentId.from( id ) );

        return id;
    }

    @Override
    public String move( final MoveBranchParams moveBranchParams, final InternalContext context )
    {
        final NodeBranchEntry nodeBranchEntry = moveBranchParams.getNodeBranchEntry();

        this.pathCache.evict( createPath( moveBranchParams.getPreviousPath(), context ) );

        return doStore( nodeBranchEntry, context );
    }

    @Override
    public void delete( final NodeId nodeId, final InternalContext context )
    {
        final NodeBranchEntry nodeBranchEntry = doGetById( nodeId, context );

        if ( nodeBranchEntry == null )
        {
            return;
        }

        storageDao.delete( BranchDeleteRequestFactory.create( nodeId, context ) );

        pathCache.evict( createPath( nodeBranchEntry.getNodePath(), context ) );
    }

    @Override
    public NodeBranchEntry get( final NodeId nodeId, final InternalContext context )
    {
        return doGetById( nodeId, context );
    }

    private NodeBranchEntry doGetById( final NodeId nodeId, final InternalContext context )
    {
        final GetByIdRequest getByIdRequest = createGetByIdRequest( nodeId, context );
        final GetResult getResult = this.storageDao.getById( getByIdRequest );

        if ( getResult.isEmpty() )
        {
            return null;
        }

        final NodeBranchEntry nodeBranchEntry = NodeBranchVersionFactory.create( getResult.getReturnValues() );

        pathCache.cache( new BranchPath( context.getBranch(), nodeBranchEntry.getNodePath() ),
                         BranchDocumentId.from( getResult.getId() ) );

        return nodeBranchEntry;
    }

    @Override
    public NodeBranchEntries get( final NodeIds nodeIds, final InternalContext context )
    {
        final GetByIdsRequest getByIdsRequest = new GetByIdsRequest();

        for ( final NodeId nodeId : nodeIds )
        {
            getByIdsRequest.add( GetByIdRequest.create().
                id( new BranchDocumentId( nodeId, context.getBranch() ).toString() ).
                storageSettings( createStorageSettings( context ) ).
                returnFields( BRANCH_RETURN_FIELDS ).
                routing( nodeId.toString() ).
                build() );
        }

        final GetResults getResults = this.storageDao.getByIds( getByIdsRequest );

        final NodeBranchEntries.Builder builder = NodeBranchEntries.create();

        for ( final GetResult getResult : getResults )
        {
            if ( !getResult.isEmpty() )
            {
                final NodeBranchEntry nodeBranchEntry = NodeBranchVersionFactory.create( getResult.getReturnValues() );

                pathCache.cache( new BranchPath( context.getBranch(), nodeBranchEntry.getNodePath() ),
                                 BranchDocumentId.from( getResult.getId() ) );

                builder.add( nodeBranchEntry );
            }
        }

        return builder.build();
    }


    @Override
    public NodeBranchEntry get( final NodePath nodePath, final InternalContext context )
    {
        return doGetByPath( nodePath, context );
    }

    @Override
    public NodeBranchEntries get( final NodePaths nodePaths, final InternalContext context )
    {
        Set<NodeBranchEntry> nodeBranchEntries = Sets.newHashSet();

        for ( final NodePath nodePath : nodePaths )
        {
            final NodeBranchEntry branchVersion = doGetByPath( nodePath, context );

            if ( branchVersion != null )
            {
                nodeBranchEntries.add( branchVersion );
            }
        }

        return NodeBranchEntries.from( nodeBranchEntries );
    }

    @Override
    public void cachePath( final NodeId nodeId, final NodePath nodePath, final InternalContext context )
    {
        pathCache.cache( new BranchPath( context.getBranch(), nodePath ), new BranchDocumentId( nodeId, context.getBranch() ) );
    }

    @Override
    public void evictPath( final NodePath nodePath, final InternalContext context )
    {
        pathCache.evict( new BranchPath( context.getBranch(), nodePath ) );
    }

    private BranchPath createPath( final NodePath nodePath, final InternalContext context )
    {
        return new BranchPath( context.getBranch(), nodePath );
    }

    private NodeBranchEntry doGetByPath( final NodePath nodePath, final InternalContext context )
    {
        final String id = this.pathCache.get( new BranchPath( context.getBranch(), nodePath ) );

        if ( id != null )
        {
            final NodeId nodeId = createNodeId( id );
            return doGetById( nodeId, context );
        }

        final SearchResult result = this.storageDao.getByValues( GetByValuesRequest.create().
            storageSettings( createStorageSettings( context ) ).
            addValue( BranchIndexPath.BRANCH_NAME.getPath(), context.getBranch().getName() ).
            addValue( BranchIndexPath.PATH.getPath(), nodePath.toString() ).
            returnFields( BRANCH_RETURN_FIELDS ).
            expectSingleValue( true ).
            build() );

        if ( !result.isEmpty() )
        {
            final SearchHit firstHit = result.getResults().getFirstHit();

            final GetResult getResult = createGetResult( firstHit );

            doCacheResult( context, getResult );

            return NodeBranchVersionFactory.create( getResult.getReturnValues() );
        }

        return null;
    }

    private NodeId createNodeId( final String id )
    {
        final int branchSeparator = id.lastIndexOf( "_" );

        if ( branchSeparator < 0 )
        {
            throw new StorageException( "Invalid BranchNodeId: " + id );
        }

        return NodeId.from( Strings.substring( id, 0, branchSeparator ) );
    }

    private void doCacheResult( final InternalContext context, final GetResult getResult )
    {
        final NodeBranchEntry nodeBranchEntry = NodeBranchVersionFactory.create( getResult.getReturnValues() );

        pathCache.cache( new BranchPath( context.getBranch(), nodeBranchEntry.getNodePath() ),
                         BranchDocumentId.from( getResult.getId() ) );
    }

    private GetResult createGetResult( final SearchHit searchHit )
    {
        return GetResult.create().
            id( searchHit.getId() ).
            resultFieldValues( searchHit.getReturnValues() ).
            build();
    }

    private GetByIdRequest createGetByIdRequest( final NodeId nodeId, final InternalContext context )
    {
        return GetByIdRequest.create().
            id( new BranchDocumentId( nodeId, context.getBranch() ).toString() ).
            storageSettings( createStorageSettings( context ) ).
            returnFields( BRANCH_RETURN_FIELDS ).
            routing( nodeId.toString() ).
            build();
    }

    private StorageSettings createStorageSettings( final InternalContext context )
    {
        return StorageSettings.create().
            storageName( StoreStorageName.from( context.getRepositoryId() ) ).
            storageType( StaticStorageType.BRANCH ).
            build();
    }

    @Reference
    public void setStorageDao( final StorageDao storageDao )
    {
        this.storageDao = storageDao;
    }
}

