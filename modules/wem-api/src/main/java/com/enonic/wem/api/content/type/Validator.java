package com.enonic.wem.api.content.type;


import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import com.enonic.wem.api.content.data.ContentData;
import com.enonic.wem.api.content.data.Data;
import com.enonic.wem.api.content.data.DataSet;
import com.enonic.wem.api.content.type.formitem.Component;
import com.enonic.wem.api.content.type.formitem.ComponentSet;
import com.enonic.wem.api.content.type.formitem.Input;
import com.enonic.wem.api.content.type.formitem.InvalidDataException;

/**
 * Validates that given data is valid, meaning it is of valid:
 * type, format, value.
 */
public class Validator
{
    private ContentType contentType;

    private boolean recordExceptions = false;

    private boolean checkValidationRegexp;

    private final List<InvalidDataException> invalidDataExceptions = new ArrayList<InvalidDataException>();

    private Validator()
    {
        // Protection
    }

    public void validate( final ContentData contentData )
        throws InvalidDataException
    {
        doValidateEntries( contentData );
    }

    public void validate( final DataSet dataSet )
        throws InvalidDataException
    {
        doValidateEntries( dataSet );
    }

    public List<InvalidDataException> getInvalidDataExceptions()
    {
        return invalidDataExceptions;
    }

    private void doValidateEntries( final Iterable<Data> entries )
    {
        for ( Data data : entries )
        {
            doValidateData( data );
        }
    }

    private void doValidateData( final Data data )
        throws InvalidDataException
    {
        if ( data.hasDataSetAsValue() )
        {
            doValidateDataWithDataSet( data );
        }
        else
        {
            checkDataTypeValidity( data );

            final Component component = contentType.getComponent( data.getPath().resolveComponentPath().toString() );
            if ( component != null )
            {
                if ( component instanceof Input )
                {
                    checkInputValidity( data, (Input) component );
                }
            }
        }
    }

    private void doValidateDataWithDataSet( final Data dataWithDataSet )
    {
        final DataSet dataSet = dataWithDataSet.getDataSet();
        final Component component = contentType.getComponent( dataSet.getPath().resolveComponentPath().toString() );
        if ( component != null )
        {
            if ( component instanceof ComponentSet )
            {
                for ( Data subData : dataSet )
                {
                    final Component subComponent = contentType.getComponent( subData.getPath().resolveComponentPath().toString() );
                    if ( subComponent instanceof Input )
                    {
                        checkInputValidity( subData, (Input) subComponent );
                    }
                }
            }
            else if ( component instanceof Input )
            {
                checkInputValidity( dataWithDataSet, (Input) component );
            }
        }
        else
        {
            doValidateEntries( dataSet );
        }
    }

    private void checkDataTypeValidity( final Data data )
    {
        try
        {
            data.checkDataTypeValidity();
        }
        catch ( InvalidDataException e )
        {
            registerInvalidDataException( e );
        }
    }

    private void checkInputValidity( final Data data, final Input input )
    {
        try
        {
            input.checkValidity( data );
        }
        catch ( InvalidDataException e )
        {
            registerInvalidDataException( e );
        }

        if ( checkValidationRegexp )
        {
            try
            {
                input.checkValidationRegexp( data );
            }
            catch ( InvalidDataException e )
            {
                registerInvalidDataException( e );
            }
        }
    }

    private void registerInvalidDataException( final InvalidDataException invalidDataException )
    {
        if ( !recordExceptions )
        {
            throw invalidDataException;
        }

        invalidDataExceptions.add( invalidDataException );
    }

    public static Builder newValidator()
    {
        return new Builder();
    }

    public static class Builder
    {
        private Validator validator = new Validator();

        public Builder contentType( ContentType contentType )
        {
            validator.contentType = contentType;
            return this;
        }

        public Builder recordExceptions( boolean value )
        {
            validator.recordExceptions = value;
            return this;
        }

        public Builder checkValidationRegexp( boolean value )
        {
            validator.checkValidationRegexp = value;
            return this;
        }

        public Validator build()
        {
            Preconditions.checkNotNull( validator.contentType, "contenType is required" );
            return validator;
        }

    }

}
