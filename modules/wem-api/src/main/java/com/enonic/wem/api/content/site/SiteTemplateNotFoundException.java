package com.enonic.wem.api.content.site;

import com.enonic.wem.api.NotFoundException;
import com.enonic.wem.api.exception.BaseException;

public final class SiteTemplateNotFoundException
    extends NotFoundException
{
    public SiteTemplateNotFoundException( final SiteTemplateKey templateKey )
    {
        super( "SiteTemplate [{0}] was not found", templateKey );
    }
}
