package com.enonic.wem.portal;

import javax.inject.Singleton;

import com.sun.jersey.api.core.ResourceConfig;

import com.enonic.wem.portal.content.ComponentResource;
import com.enonic.wem.portal.content.ContentResource;
import com.enonic.wem.portal.exception.mapper.DefaultExceptionMapper;
import com.enonic.wem.portal.exception.mapper.EvaluationExceptionMapper;
import com.enonic.wem.portal.exception.mapper.PortalWebExceptionMapper;
import com.enonic.wem.portal.exception.mapper.WebApplicationExceptionMapper;
import com.enonic.wem.portal.underscore.ImageResource;
import com.enonic.wem.portal.underscore.PublicResource;
import com.enonic.wem.portal.underscore.ServicesResource;
import com.enonic.wem.web.jaxrs.JaxRsServlet;
import com.enonic.wem.web.mvc.FreeMarkerViewWriter;

@Singleton
public final class PortalServlet
    extends JaxRsServlet
{
    @Override
    protected void configure()
    {
        setFeature( ResourceConfig.FEATURE_DISABLE_WADL, true );
        addClass( ContentResource.class );
        addClass( ComponentResource.class );
        addClass( PublicResource.class );
        addClass( ImageResource.class );
        addClass( ServicesResource.class );
        addClass( FreeMarkerViewWriter.class );
        addClass( EvaluationExceptionMapper.class );
        addClass( PortalWebExceptionMapper.class );
        addClass( DefaultExceptionMapper.class );
        addClass( WebApplicationExceptionMapper.class );
    }
}
