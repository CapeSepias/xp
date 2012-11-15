package com.enonic.wem.web.rest.rpc.content;

import org.springframework.stereotype.Component;

import com.enonic.wem.api.command.Commands;
import com.enonic.wem.api.command.content.GetContents;
import com.enonic.wem.api.content.Content;
import com.enonic.wem.api.content.ContentPath;
import com.enonic.wem.api.content.ContentPaths;
import com.enonic.wem.api.content.Contents;
import com.enonic.wem.web.json.JsonErrorResult;
import com.enonic.wem.web.json.rpc.JsonRpcContext;
import com.enonic.wem.web.rest.rpc.AbstractDataRpcHandler;

@Component
public class GetContentRpcHandler
    extends AbstractDataRpcHandler
{

    public GetContentRpcHandler()
    {
        super( "content_get" );
    }

    @Override
    public void handle( final JsonRpcContext context )
        throws Exception
    {
        final String path = context.param( "path" ).required().asString();
        final Content content = findContent( ContentPath.from( path ) );

        if ( content != null )
        {
            context.setResult( new GetContentJsonResult( content ) );
        }
        else
        {
            context.setResult( new JsonErrorResult( "Content [{0}] was not found", path ) );
        }
    }

    private Content findContent( final ContentPath contentPath )
    {
        final GetContents getContent = Commands.content().get();
        getContent.paths( ContentPaths.from( contentPath ) );

        final Contents contents = client.execute( getContent );
        return contents.isNotEmpty() ? contents.getFirst() : null;
    }

}
