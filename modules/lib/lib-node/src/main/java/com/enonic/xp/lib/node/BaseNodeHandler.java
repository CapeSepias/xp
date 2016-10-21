package com.enonic.xp.lib.node;

import com.enonic.xp.node.NodeService;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

public abstract class BaseNodeHandler
    implements ScriptBean
{
    protected NodeService nodeService;

    protected RepositoryService repositoryService;

    public final Object execute()
    {
        return doExecute();
    }

    protected abstract Object doExecute();

    protected <T> T valueOrDefault( final T value, final T defValue )
    {
        return value == null ? defValue : value;
    }

    @Override
    public void initialize( final BeanContext context )
    {
        this.nodeService = context.getService( NodeService.class ).get();
        this.repositoryService = context.getService( RepositoryService.class ).get();
    }
}
