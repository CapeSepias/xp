package com.enonic.wem.api.content.schema.content.form.inputtype;


import com.enonic.wem.api.content.data.Data;
import com.enonic.wem.api.content.data.Value;
import com.enonic.wem.api.content.schema.content.form.BreaksRequiredContractException;

/**
 * Common interface for all kinds of input types.
 */
public interface InputType
{
    String getName();

    boolean isBuiltIn();

    boolean requiresConfig();

    Class requiredConfigClass();

    AbstractInputTypeConfigJsonSerializer getInputTypeConfigJsonGenerator();

    AbstractInputTypeConfigXmlSerializer getInputTypeConfigXmlGenerator();

    void checkBreaksRequiredContract( Data data )
        throws BreaksRequiredContractException;

    Value newValue( String value );
}
