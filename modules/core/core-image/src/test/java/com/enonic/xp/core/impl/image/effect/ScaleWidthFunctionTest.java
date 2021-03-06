/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.xp.core.impl.image.effect;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import com.enonic.xp.core.impl.image.ImageScaleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScaleWidthFunctionTest
    extends BaseImageFilterTest
{
    @Test
    public void testDownscale()
    {
        BufferedImage scaled = scale( 100 );
        assertEquals( 100, scaled.getWidth() );
        assertEquals( 75, scaled.getHeight() );
    }

    @Test
    public void testUpscale()
    {
        BufferedImage scaled = scale( 600 );
        assertEquals( 600, scaled.getWidth() );
        assertEquals( 450, scaled.getHeight() );
    }

    private BufferedImage scale( int size )
    {
        ImageScaleFunction scaleFunction = new ScaleWidthFunction( size );
        return scaleFunction.scale( getOpaque() );
    }
}
