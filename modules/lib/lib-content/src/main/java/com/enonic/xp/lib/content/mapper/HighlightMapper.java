package com.enonic.xp.lib.content.mapper;

import com.google.common.collect.ImmutableMap;

import com.enonic.xp.content.ContentId;
import com.enonic.xp.highlight.HighlightedProperties;
import com.enonic.xp.script.serializer.MapGenerator;
import com.enonic.xp.script.serializer.MapSerializable;

public final class HighlightMapper
    implements MapSerializable
{
    private final ImmutableMap<ContentId, HighlightedProperties> value;

    public HighlightMapper( final ImmutableMap<ContentId, HighlightedProperties> value )
    {
        this.value = value;
    }

    @Override
    public void serialize( final MapGenerator gen )
    {
        serializeHighlight( gen, this.value );
    }

    private static void serializeHighlight( final MapGenerator gen, final ImmutableMap<ContentId, HighlightedProperties> value )
    {
        for ( ContentId id : value.keySet() )
        {
            gen.map( id.toString() );

            value.get( id ).forEach( highlightedField -> {

                gen.array( highlightedField.getName() );
                highlightedField.getFragments().forEach( gen::value );
                gen.end();

            } );
            gen.end();
        }
    }
}
