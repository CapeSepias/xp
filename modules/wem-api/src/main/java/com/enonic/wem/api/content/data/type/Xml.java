package com.enonic.wem.api.content.data.type;


import com.enonic.wem.api.content.data.Value;

public class Xml
    extends BaseDataType
{
    Xml( int key )
    {
        super( key, JavaType.STRING );
    }

    @Override
    public String getIndexableString( final Object value )
    {
        return value.toString();
    }

    public Value toXml( final Value value )
    {
        if ( hasCorrectType( value ) )
        {
            return value;
        }
        else
        {
            throw new InconvertibleValueException( value, this );
        }
    }
}
