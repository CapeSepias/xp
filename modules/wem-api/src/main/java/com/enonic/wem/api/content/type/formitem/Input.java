package com.enonic.wem.api.content.type.formitem;


import com.google.common.base.Preconditions;

import com.enonic.wem.api.content.data.Data;
import com.enonic.wem.api.content.datatype.InvalidValueTypeException;
import com.enonic.wem.api.content.type.formitem.inputtype.BaseInputType;
import com.enonic.wem.api.content.type.formitem.inputtype.InputType;
import com.enonic.wem.api.content.type.formitem.inputtype.InputTypeConfig;

public class Input
    extends HierarchicalComponent
{
    private BaseInputType type;

    private String label;

    private boolean immutable;

    private final Occurrences occurrences = new Occurrences( 0, 1 );

    private boolean indexed;

    private String customText;

    private ValidationRegex validationRegexp;

    private String helpText;

    private InputTypeConfig inputTypeConfig;

    protected Input()
    {
    }

    public InputType getInputType()
    {
        return type;
    }

    public String getLabel()
    {
        return label;
    }

    public boolean isRequired()
    {
        return occurrences.impliesRequired();
    }

    public boolean isImmutable()
    {
        return immutable;
    }

    public boolean isMultiple()
    {
        return occurrences.isMultiple();
    }

    public Occurrences getOccurrences()
    {
        return occurrences;
    }

    public boolean isIndexed()
    {
        return indexed;
    }

    public String getCustomText()
    {
        return customText;
    }

    public ValidationRegex getValidationRegexp()
    {
        return validationRegexp;
    }

    public String getHelpText()
    {
        return helpText;
    }

    public InputTypeConfig getInputTypeConfig()
    {
        return inputTypeConfig;
    }

    public void checkBreaksRequiredContract( final Data data )
        throws BreaksRequiredContractException
    {
        Preconditions.checkNotNull( data, "Given data is null" );

        if ( isRequired() )
        {
            type.checkBreaksRequiredContract( data );
        }
    }

    public void checkValidityAccordingToInputTypeConfig( final Data data )
        throws InvalidValueException
    {
        if ( inputTypeConfig != null )
        {
            inputTypeConfig.checkValidity( data );
        }
    }

    public void checkValidationRegexp( final Data data )
        throws InvalidDataException
    {
        try
        {
            validationRegexp.checkValidity( data );
        }
        catch ( BreaksRegexValidationException e )
        {
            throw new InvalidDataException( data, e );
        }
    }

    public void checkValidity( final Data data )
        throws InvalidDataException
    {
        try
        {
            if ( data == null )
            {
                return;
            }

            checkValidityAccordingToInputTypeConfig( data );
            type.checkValidity( data );
        }
        catch ( InvalidValueException e )
        {
            throw new InvalidDataException( data, e );
        }
        catch ( InvalidValueTypeException e )
        {
            throw new InvalidDataException( data, e );
        }
    }


    @Override
    public Input copy()
    {
        final Input copy = (Input) super.copy();
        copy.type = type;
        copy.label = label;
        copy.immutable = immutable;
        copy.occurrences.setMinOccurences( occurrences.getMinimum() );
        copy.occurrences.setMaxOccurences( occurrences.getMaximum() );
        copy.indexed = indexed;
        copy.customText = customText;
        copy.validationRegexp = validationRegexp;
        copy.helpText = helpText;
        copy.inputTypeConfig = inputTypeConfig;
        return copy;
    }

    public static Builder newInput()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String name;

        private BaseInputType inputType;

        private String label;

        private boolean immutable;

        private Occurrences occurrences = new Occurrences( 0, 1 );

        private boolean indexed;

        private String customText;

        private ValidationRegex validationRegexp;

        private String helpText;

        private InputTypeConfig inputTypeConfig;

        private Builder()
        {
            // protection
        }

        public Builder name( String value )
        {
            name = value;
            return this;
        }

        public Builder type( BaseInputType value )
        {
            inputType = value;
            return this;
        }

        public Builder label( String value )
        {
            label = value;
            return this;
        }

        public Builder immutable( boolean value )
        {
            immutable = value;
            return this;
        }

        public Builder occurrences( Occurrences value )
        {
            occurrences.setMinOccurences( value.getMinimum() );
            occurrences.setMaxOccurences( value.getMaximum() );
            return this;
        }

        public Builder occurrences( int minOccurrences, int maxOccurrences )
        {
            occurrences.setMinOccurences( minOccurrences );
            occurrences.setMaxOccurences( maxOccurrences );
            return this;
        }

        public Builder required( boolean value )
        {
            if ( value && !occurrences.impliesRequired() )
            {
                occurrences.setMinOccurences( 1 );
            }
            else if ( !value && occurrences.impliesRequired() )
            {
                occurrences.setMinOccurences( 0 );
            }
            return this;
        }

        public Builder multiple( boolean value )
        {
            if ( value )
            {
                occurrences.setMaxOccurences( 0 );
            }
            else
            {
                occurrences.setMaxOccurences( 1 );
            }
            return this;
        }

        public Builder indexed( boolean value )
        {
            indexed = value;
            return this;
        }

        public Builder customText( String value )
        {
            customText = value;
            return this;
        }

        public Builder validationRegexp( String value )
        {
            validationRegexp = new ValidationRegex( value );
            return this;
        }

        public Builder helpText( String value )
        {
            helpText = value;
            return this;
        }

        public Builder inputTypeConfig( InputTypeConfig value )
        {
            inputTypeConfig = value;
            return this;
        }

        public Input build()
        {
            Preconditions.checkNotNull( name, "name cannot be null" );
            Preconditions.checkNotNull( inputType, "inputType cannot be null" );

            if ( inputType.requiresConfig() )
            {
                Preconditions.checkArgument( inputTypeConfig != null, "Input [name='%s', type=%s] is missing required InputTypeConfig: %s",
                                             name, inputType.getName(), inputType.requiredConfigClass().getName() );

                //noinspection ConstantConditions
                Preconditions.checkArgument( inputType.requiredConfigClass().isInstance( inputTypeConfig ),
                                             "Input [name='%s', type=%s] expects InputTypeConfig of type [%s] but was: %s", name,
                                             inputType.getName(), inputType.requiredConfigClass().getName(),
                                             inputTypeConfig.getClass().getName() );
            }

            Input input = new Input();
            input.setName( name );
            input.type = inputType;
            input.label = label;
            input.immutable = immutable;
            input.occurrences.setMinOccurences( occurrences.getMinimum() );
            input.occurrences.setMaxOccurences( occurrences.getMaximum() );
            input.indexed = indexed;
            input.customText = customText;
            input.validationRegexp = validationRegexp;
            input.helpText = helpText;
            input.inputTypeConfig = inputTypeConfig;
            input.setPath( new ComponentPath( input.getName() ) );
            return input;
        }
    }
}
