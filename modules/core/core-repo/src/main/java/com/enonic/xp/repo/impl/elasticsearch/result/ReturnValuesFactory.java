package com.enonic.xp.repo.impl.elasticsearch.result;

import java.util.Map;

import org.elasticsearch.search.SearchHitField;

import com.enonic.xp.repo.impl.ReturnValues;

class ReturnValuesFactory
{
    public static ReturnValues create( final org.elasticsearch.search.SearchHit hit )
    {
        final Map<String, SearchHitField> fields = hit.getFields();

        final ReturnValues.Builder builder = ReturnValues.create();

        fields.forEach( ( fieldName, hitField ) -> builder.add( fieldName, hitField.values() ) );

        return builder.build();
    }
}
