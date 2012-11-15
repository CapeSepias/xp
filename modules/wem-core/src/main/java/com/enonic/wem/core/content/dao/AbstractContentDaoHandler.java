package com.enonic.wem.core.content.dao;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

import com.enonic.wem.api.content.Content;
import com.enonic.wem.api.content.ContentPath;
import com.enonic.wem.core.jcr.JcrHelper;

abstract class AbstractContentDaoHandler
{
    protected final Session session;

    protected final ContentJcrMapper contentJcrMapper = new ContentJcrMapper();

    AbstractContentDaoHandler( final Session session )
    {
        this.session = session;
    }

    protected final List<ContentAndNode> doContentNodesToContentAndNodes( final NodeIterator nodeIterator )
        throws RepositoryException
    {
        List<ContentAndNode> contentList = new ArrayList<ContentAndNode>();
        while ( nodeIterator.hasNext() )
        {
            Node contentNode = nodeIterator.nextNode();
            final Content content = Content.create( ContentPath.from( contentNode.getPath() ) );
            contentJcrMapper.toContent( contentNode, content );
            contentList.add( new ContentAndNode( content, contentNode ) );
        }
        return contentList;
    }

    protected final NodeIterator doGetTopContentNodes( final Session session )
        throws RepositoryException
    {
        final Node rootNode = session.getRootNode();
        final Node contentsNode = JcrHelper.getNodeOrNull( rootNode, ContentDaoConstants.CONTENTS_PATH );
        return contentsNode.getNodes();
    }

    protected final NodeIterator doGetChildContentNodes( final Node contentParentNode )
        throws RepositoryException
    {
        return contentParentNode.getNodes();
    }

    protected final Node doGetContentNode( final Session session, final ContentPath contentPath )
        throws RepositoryException
    {
        if ( contentPath.isRoot() )
        {
            return null;
        }
        final String path = getNodePath( contentPath );
        final Node rootNode = session.getRootNode();
        return JcrHelper.getNodeOrNull( rootNode, path );
    }

    protected final Content doFindContent( final ContentPath contentPath, final Session session )
        throws RepositoryException
    {
        final Node contentNode = doGetContentNode( session, contentPath );
        if ( contentNode == null )
        {
            return null;
        }

        final Content content = Content.create( contentPath );
        contentJcrMapper.toContent( contentNode, content );
        return content;

    }

    private String getNodePath( final ContentPath contentPath )
    {
        final String relativePathToContent = StringUtils.removeStart( contentPath.toString(), "/" );
        return ContentDaoConstants.CONTENTS_PATH + relativePathToContent;
    }

    class ContentAndNode
    {
        Content content;

        Node contentNode;

        ContentAndNode( final Content content, final Node contentNode )
        {
            this.content = content;
            this.contentNode = contentNode;
        }
    }
}
