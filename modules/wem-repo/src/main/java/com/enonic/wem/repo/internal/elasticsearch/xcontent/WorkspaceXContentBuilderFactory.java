package com.enonic.wem.repo.internal.elasticsearch.xcontent;

import org.elasticsearch.common.xcontent.XContentBuilder;

import com.enonic.wem.api.workspace.Workspace;
import com.enonic.wem.repo.internal.elasticsearch.workspace.WorkspaceIndexPath;
import com.enonic.wem.repo.internal.index.IndexException;
import com.enonic.wem.repo.internal.workspace.StoreWorkspaceDocument;

public class WorkspaceXContentBuilderFactory
    extends AbstractXContentBuilderFactor
{
    public static XContentBuilder create( final StoreWorkspaceDocument storeWorkspaceDocument, final Workspace workspace )
    {
        try
        {
            final XContentBuilder builder = startBuilder();

            addField( builder, WorkspaceIndexPath.VERSION_ID.getPath(), storeWorkspaceDocument.getNodeVersionId().toString() );
            addField( builder, WorkspaceIndexPath.WORKSPACE_ID.getPath(), workspace.getName() );
            addField( builder, WorkspaceIndexPath.NODE_ID.getPath(), storeWorkspaceDocument.getNodeId().toString() );
            endBuilder( builder );
            return builder;
        }
        catch ( Exception e )
        {
            throw new IndexException( "Failed to build xContent for WorkspaceDocument", e );
        }

    }

}
