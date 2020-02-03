package com.enonic.xp.core.impl.content.page;

import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.page.PageTemplate;
import com.enonic.xp.page.PageTemplateFilter;
import com.enonic.xp.page.PageTemplates;
import com.enonic.xp.schema.content.ContentTypeName;

final class GetDefaultPageTemplateCommand
{
    private ContentTypeName contentType;

    private ContentId site;

    private ContentPath sitePath;

    private ContentService contentService;

    public PageTemplate execute()
    {

        final PageTemplates pageTemplates = new GetPageTemplateBySiteCommand().
            site( site ).
            sitePath( sitePath ).
            contentService( contentService ).
            execute();
        final PageTemplates supportedTemplates = pageTemplates.filter( PageTemplateFilter.canRender( contentType ) );
        return supportedTemplates.first();
    }

    public GetDefaultPageTemplateCommand contentType( final ContentTypeName contentType )
    {
        this.contentType = contentType;
        return this;
    }

    public GetDefaultPageTemplateCommand site( final ContentId site )
    {
        this.site = site;
        return this;
    }

    public GetDefaultPageTemplateCommand sitePath( final ContentPath sitePath )
    {
        this.sitePath = sitePath;
        return this;
    }

    public GetDefaultPageTemplateCommand contentService( final ContentService contentService )
    {
        this.contentService = contentService;
        return this;
    }
}
