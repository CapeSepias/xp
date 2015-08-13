package com.enonic.xp.form.inputtype;

import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class InputTypes2
    implements Iterable<InputType>, InputTypeResolver
{
    public final static InputTypes2 BUILTIN = InputTypes2.create().
        add( ComboBoxType.INSTANCE ).
        add( DateType.INSTANCE ).
        add( TimeType.INSTANCE ).
        add( DateTimeType.INSTANCE ).
        add( CheckboxType.INSTANCE ).
        add( DoubleType.INSTANCE ).
        add( GeoPointType.INSTANCE ).
        add( new HtmlAreaType() ).
        add( ImageUploaderType.INSTANCE ).
        add( FileUploaderType.INSTANCE ).
        add( ImageSelectorType.INSTANCE ).
        add( ContentSelectorType.INSTANCE ).
        add( RadioButtonsType.INSTANCE ).
        add( TagType.INSTANCE ).
        add( TextAreaType.INSTANCE ).
        add( TextLineType.INSTANCE ).
        add( LongType.INSTANCE ).
        add( PageControllerType.INSTANCE ).
        add( ContentTypeFilterType.INSTANCE ).
        add( SiteConfiguratorType.INSTANCE ).
        add( new TinyMCEType() ).
        build();

    private final ImmutableMap<String, InputType> map;

    private InputTypes2( final Builder builder )
    {
        this.map = ImmutableMap.copyOf( builder.map );
    }

    @Override
    public InputType resolve( final InputTypeName name )
    {
        final InputType type = this.map.get( name.toString() );
        if ( type != null )
        {
            return type;
        }

        throw new InputTypeNotFoundException( name );
    }

    @Override
    public Iterator<InputType> iterator()
    {
        return this.map.values().iterator();
    }

    public static Builder create()
    {
        return new Builder();
    }

    public final static class Builder
    {
        private final Map<String, InputType> map;

        private Builder()
        {
            this.map = Maps.newHashMap();
        }

        private void register( final InputType type )
        {
            final Object previous = this.map.put( type.getName(), type );
            Preconditions.checkState( previous == null, "InputType already registered: " + type.getName() );
        }

        public Builder add( final InputType... types )
        {
            return add( Lists.newArrayList( types ) );
        }

        public Builder add( final Iterable<InputType> types )
        {
            for ( final InputType type : types )
            {
                register( type );
            }

            return this;
        }

        public InputTypes2 build()
        {
            return new InputTypes2( this );
        }
    }
}
