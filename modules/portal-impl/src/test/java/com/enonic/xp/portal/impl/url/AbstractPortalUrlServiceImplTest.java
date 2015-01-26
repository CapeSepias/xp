package com.enonic.xp.portal.impl.url;

import org.junit.Before;

import com.enonic.wem.api.content.ContentPath;
import com.enonic.wem.api.module.ModuleKey;
import com.enonic.wem.api.workspace.Workspace;
import com.enonic.xp.portal.PortalContext;

public abstract class AbstractPortalUrlServiceImplTest
{
    protected PortalContext context;

    protected PortalUrlServiceImpl service;

    @Before
    public void setup()
    {
        this.context = new PortalContext();
        this.context.setWorkspace( Workspace.from( "stage" ) );
        this.context.setModule( ModuleKey.from( "mymodule" ) );
        this.context.setBaseUri( "/portal" );
        this.context.setContentPath( ContentPath.from( "context/path" ) );

        this.service = new PortalUrlServiceImpl();
    }
}
