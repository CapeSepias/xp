package com.enonic.xp.web.impl.dispatch.pipeline;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import com.enonic.xp.web.dispatch.ServletMapping;
import com.enonic.xp.web.impl.dispatch.mapping.ServletDefinition;

import static org.junit.Assert.*;

public class ServletPipelineImplTest
    extends ResourcePipelineImplTest<ServletDefinition, ServletPipelineImpl>
{
    @WebServlet
    private final class MyServlet
        extends HttpServlet
    {
    }

    @Override
    ServletPipelineImpl newPipeline()
    {
        return new ServletPipelineImpl();
    }

    @Override
    ServletDefinition newDefinition()
    {
        final ServletDefinition def = Mockito.mock( ServletDefinition.class );
        Mockito.when( def.getResource() ).thenReturn( new MyServlet() );
        return def;
    }

    @Test
    public void addRemove_servlet()
    {
        final MyServlet servlet = new MyServlet();

        assertEquals( 0, Lists.newArrayList( this.pipeline ).size() );
        this.pipeline.addServlet( servlet );

        assertEquals( 1, Lists.newArrayList( this.pipeline ).size() );
        this.pipeline.removeServlet( servlet );
    }

    @Test
    public void addRemove_mapping()
    {
        final ServletMapping mapping = Mockito.mock( ServletMapping.class );

        assertEquals( 0, Lists.newArrayList( this.pipeline ).size() );
        this.pipeline.addMapping( mapping );

        assertEquals( 1, Lists.newArrayList( this.pipeline ).size() );
        this.pipeline.removeMapping( mapping );
    }

    @Test
    public void testService()
        throws Exception
    {
        final ServletDefinition def1 = newDefinition();
        final ServletDefinition def2 = newDefinition();

        this.pipeline.add( def1 );
        this.pipeline.add( def2 );

        this.pipeline.service( this.request, this.response );

        Mockito.verify( def1, Mockito.times( 1 ) ).service( this.request, this.response );
        Mockito.verify( def2, Mockito.times( 1 ) ).service( this.request, this.response );

        Mockito.when( def1.service( this.request, this.response ) ).thenReturn( true );
        this.pipeline.service( this.request, this.response );

        Mockito.verify( def1, Mockito.times( 2 ) ).service( this.request, this.response );
        Mockito.verify( def2, Mockito.times( 1 ) ).service( this.request, this.response );
    }
}
