package com.enonic.wem.core.schema.content.dao;

import com.enonic.wem.api.schema.content.ContentType;
import com.enonic.wem.api.schema.content.ContentTypeName;
import com.enonic.wem.api.schema.content.ContentTypes;

public interface ContentTypeDao
{
    ContentTypes getAllContentTypes();

    ContentType getContentType( ContentTypeName contentTypeName );
}
