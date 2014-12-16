package com.enonic.wem.admin.rest.resource.content.json;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.enonic.wem.admin.json.content.MetadataJson;
import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.content.ContentName;
import com.enonic.wem.api.content.Metadata;
import com.enonic.wem.api.content.Metadatas;
import com.enonic.wem.api.content.RenameContentParams;
import com.enonic.wem.api.content.UpdateContentParams;
import com.enonic.wem.api.data.PropertyArrayJson;
import com.enonic.wem.api.data.PropertyTree;
import com.enonic.wem.api.data.PropertyTreeJson;
import com.enonic.wem.api.form.FormJson;
import com.enonic.wem.api.security.PrincipalKey;

public final class UpdateContentJson
{
    final ContentName contentName;

    final UpdateContentParams updateContentParams;

    final RenameContentParams renameContentParams;

    @JsonCreator
    UpdateContentJson( @JsonProperty("contentId") final String contentId, @JsonProperty("contentName") final String contentName,
                       @JsonProperty("data") final List<PropertyArrayJson> propertyArrayJsonList,
                       @JsonProperty("metadata") final List<MetadataJson> metadataJsonList, @JsonProperty("form") final FormJson form,
                       @JsonProperty("displayName") final String displayName, @JsonProperty("draft") final String draft )
    {
        this.contentName = ContentName.from( contentName );

        final PropertyTree contentData = PropertyTreeJson.fromJson( propertyArrayJsonList );
        final Metadatas metadatas = parseMetadata( metadataJsonList );

        this.updateContentParams = new UpdateContentParams().
            contentId( ContentId.from( contentId ) ).
            modifier( PrincipalKey.ofAnonymous() ).
            editor( edit -> {
                edit.form = form.getForm();
                edit.data = contentData;
                edit.metadata = metadatas;
                edit.draft = Boolean.valueOf( draft );
                edit.displayName = displayName;
            } );

        this.renameContentParams = new RenameContentParams().
            contentId( ContentId.from( contentId ) ).
            newName( this.contentName );
    }

    @JsonIgnore
    public UpdateContentParams getUpdateContentParams()
    {
        return updateContentParams;
    }

    @JsonIgnore
    public RenameContentParams getRenameContentParams()
    {
        return renameContentParams;
    }

    @JsonIgnore
    public ContentName getContentName()
    {
        return contentName;
    }

    private Metadatas parseMetadata( final List<MetadataJson> metadataJsonList )
    {
        final List<Metadata> metadataList = new ArrayList<>();
        for ( MetadataJson metadataJson : metadataJsonList )
        {
            metadataList.add( metadataJson.getMetadata() );
        }
        return Metadatas.from( metadataList );
    }
}
