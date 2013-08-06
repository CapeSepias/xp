package com.enonic.wem.runner;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.naming.resources.VirtualDirContext;

public final class Runner
{
    private final Tomcat tomcat;

    public Runner()
    {
        this.tomcat = new Tomcat();
        init();
    }

    private void init()
    {
        try
        {
            doInit();
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void start()
    {
        try
        {
            doStart();
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void stop()
    {
        try
        {
            dostop();
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    private void doInit()
        throws Exception
    {
        final String docBase = new File( "./modules/wem-webapp/src/main/webapp" ).getAbsolutePath();
        final String generatedDir = new File( "./modules/wem-webapp/target/generated-sources/webapp" ).getAbsolutePath();

        this.tomcat.setPort( 8080 );
        this.tomcat.setHostname( "localhost" );
        this.tomcat.setBaseDir( new File( "./modules/wem-runner/target/tomcat" ).getAbsolutePath() );

        final VirtualDirContext resources = new VirtualDirContext();
        resources.setDocBase( docBase );
        resources.setExtraResourcePaths( "/=" + generatedDir );

        final Context context = this.tomcat.addWebapp( "/", docBase );
        context.setResources( resources );
    }

    private void doStart()
        throws Exception
    {
        this.tomcat.start();
        this.tomcat.getServer().await();
    }

    private void dostop()
        throws Exception
    {
        this.tomcat.stop();
    }
}
