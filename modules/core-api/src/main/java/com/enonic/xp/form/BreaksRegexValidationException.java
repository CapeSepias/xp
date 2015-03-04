package com.enonic.xp.form;


import com.enonic.xp.data.Property;

public class BreaksRegexValidationException
    extends Exception
{
    private final Property property;

    private final String regex;

    public BreaksRegexValidationException( final Property property, final String regex )
    {
        super( buildMessage( property, regex ) );
        this.property = property;
        this.regex = regex;
    }

    public Property getProperty()
    {
        return property;
    }

    public String getRegex()
    {
        return regex;
    }

    private static String buildMessage( final Property property, final String regex )
    {
        return "Data [" + property + "] breaks regexp [" + regex + "]";
    }
}
