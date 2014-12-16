package com.enonic.wem.admin.rest.resource.content;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteSource;

import com.enonic.wem.admin.rest.resource.BaseImageHelper;
import com.enonic.wem.api.util.Exceptions;
import com.enonic.wem.api.util.ImageHelper;
import com.enonic.wem.core.image.filter.effect.ScaleMaxFilter;
import com.enonic.wem.core.image.filter.effect.ScaleSquareFilter;

final class ContentImageHelper
    extends BaseImageHelper
{
    public enum ImageFilter
    {
        ScaleSquareFilter,
        ScaleMax
    }

    BufferedImage readImage( final ByteSource blob, final int size, final ImageFilter imageFilter )
    {
        try (final InputStream inputStream = blob.openStream())
        {
            return readImage( inputStream, size, imageFilter );
        }
        catch ( IOException e )
        {
            throw Exceptions.unchecked( e );
        }
    }

    private BufferedImage readImage( final InputStream inputStream, final int size, final ImageFilter imageFilter )
    {
        final BufferedImage image = ImageHelper.toBufferedImage( inputStream );
        switch ( imageFilter )
        {
            case ScaleSquareFilter:
                return new ScaleSquareFilter( size ).filter( image );

            case ScaleMax:
                return new ScaleMaxFilter( size ).filter( image );

            default:
                throw new IllegalArgumentException( "Invalid image filter: " + imageFilter );
        }
    }
}
