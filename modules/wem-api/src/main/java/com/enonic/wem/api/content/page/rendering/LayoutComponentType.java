package com.enonic.wem.api.content.page.rendering;


import com.enonic.wem.api.content.page.Layout;
import com.enonic.wem.api.rendering.ComponentType;
import com.enonic.wem.api.rendering.Context;
import com.enonic.wem.api.rendering.RenderingResult;

public class LayoutComponentType
    implements ComponentType<Layout>
{
    @Override
    public RenderingResult execute( final Layout layout, final Context context)
    {
        return null;
    }
}
