package com.enonic.wem.core.content.type.form;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.enonic.wem.api.content.type.form.FormItem;
import com.enonic.wem.api.content.type.form.FormItemSet;
import com.enonic.wem.api.content.type.form.FormItems;
import com.enonic.wem.core.content.AbstractJsonSerializer;
import com.enonic.wem.core.content.JsonParserUtil;

import static com.enonic.wem.api.content.type.form.FormItemSet.newFormItemSet;

class FormItemSetJsonSerializer
    extends AbstractJsonSerializer<FormItemSet>
{
    private static final String NAME = "name";

    public static final String LABEL = "label";

    public static final String IMMUTABLE = "immutable";

    public static final String CUSTOM_TEXT = "customText";

    public static final String HELP_TEXT = "helpText";

    public static final String OCCURRENCES = "occurrences";

    public static final String ITEMS = "items";

    private final OccurrencesJsonSerializer occurrencesJsonSerializer = new OccurrencesJsonSerializer();

    private final FormItemsJsonSerializer formItemsJsonSerializer;

    public FormItemSetJsonSerializer( final FormItemsJsonSerializer formItemsJsonSerializer )
    {
        this.formItemsJsonSerializer = formItemsJsonSerializer;
    }

    @Override
    protected JsonNode serialize( final FormItemSet set, final ObjectMapper objectMapper )
    {
        final ObjectNode jsonObject = objectMapper.createObjectNode();
        jsonObject.put( NAME, set.getName() );
        jsonObject.put( LABEL, set.getLabel() );
        jsonObject.put( IMMUTABLE, set.isImmutable() );
        jsonObject.put( OCCURRENCES, occurrencesJsonSerializer.serialize( set.getOccurrences(), objectMapper ) );
        jsonObject.put( CUSTOM_TEXT, set.getCustomText() );
        jsonObject.put( HELP_TEXT, set.getHelpText() );
        jsonObject.put( ITEMS, formItemsJsonSerializer.serialize( set.getFormItems(), objectMapper ) );
        return jsonObject;
    }


    public FormItemSet parse( final JsonNode formItemSetObj )
    {
        final FormItemSet.Builder builder = newFormItemSet();
        builder.name( JsonParserUtil.getStringValue( NAME, formItemSetObj ) );
        builder.label( JsonParserUtil.getStringValue( LABEL, formItemSetObj, null ) );
        builder.immutable( JsonParserUtil.getBooleanValue( IMMUTABLE, formItemSetObj ) );
        builder.helpText( JsonParserUtil.getStringValue( HELP_TEXT, formItemSetObj ) );
        builder.customText( JsonParserUtil.getStringValue( CUSTOM_TEXT, formItemSetObj ) );

        parseOccurrences( builder, formItemSetObj.get( OCCURRENCES ) );

        final FormItems formItems = formItemsJsonSerializer.parse( formItemSetObj.get( ITEMS ) );
        for ( FormItem formItem : formItems.iterable() )
        {
            builder.add( formItem );
        }

        return builder.build();
    }

    private void parseOccurrences( final FormItemSet.Builder builder, final JsonNode occurrencesNode )
    {
        if ( occurrencesNode != null )
        {
            builder.occurrences( occurrencesJsonSerializer.parse( occurrencesNode ) );
        }
        else
        {
            builder.multiple( false );
        }
    }
}
