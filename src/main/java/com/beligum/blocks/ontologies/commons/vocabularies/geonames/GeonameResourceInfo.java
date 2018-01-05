/*
 * Copyright 2017 Republic of Reinvention bvba. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beligum.blocks.ontologies.commons.vocabularies.geonames;

import com.beligum.blocks.config.RdfFactory;
import com.beligum.blocks.endpoints.ifaces.ResourceInfo;
import com.beligum.blocks.utils.RdfTools;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;
import java.util.Locale;

/**
 * Created by bram on 3/12/16.
 */
public class GeonameResourceInfo extends AbstractGeoname implements ResourceInfo
{
    //-----CONSTANTS-----
    //special value for 'lang' that maps to external documentation
    private static final String LINK_LANGUAGE = "link";

    //-----VARIABLES-----
    private URI resourceType;
    private String name;
    private String toponymName;
    private String geonameId;
    private List<GeonameLangValue> alternateName;
    private Locale language;

    //temp values...
    private transient boolean triedLink;
    private transient URI cachedLink;

    //-----CONSTRUCTORS-----
    public GeonameResourceInfo()
    {
    }

    //-----PUBLIC METHODS-----
    @Override
    public URI getResourceUri()
    {
        return RdfTools.createRelativeResourceId(RdfFactory.getClassForResourceType(this.getResourceType()), this.geonameId);
    }
    @Override
    public URI getResourceType()
    {
        return resourceType;
    }
    @Override
    public String getLabel()
    {
        return name;
    }
    @Override
    public URI getLink()
    {
        //note that the endpoint behind this will take care of the redirection to a good external landing page
        return getResourceUri();
    }
    @Override
    public boolean isExternalLink()
    {
        return true;
    }
    @Override
    public URI getImage()
    {
        return null;
    }
    //this getter is a little bit of a mindfuck because it has the same name as it's setter but is used differently;
    // the setter is used to set the name property, coming in (deserialized) from geonames,
    // this getter is called when the same object is serialized to our own JS client code, but we can return a different property if we want to
    @Override
    public String getName()
    {
        return name;
    }
    @Override
    public Locale getLanguage()
    {
        return language;
    }
    /**
     * We need to make this one public because the geonames webservice doesn't return the value; it's set manually after fetching it from the service
     */
    public void setLanguage(Locale language)
    {
        this.language = language;
    }

    //-----PROTECTED METHODS-----
    //see http://stackoverflow.com/questions/11872914/write-only-properties-with-jackson
    @JacksonInject(RESOURCE_TYPE_INJECTABLE)
    private void setResourceType(URI resourceType)
    {
        this.resourceType = resourceType;
    }
    @JsonProperty
    private void setName(String name)
    {
        this.name = name;
    }
    @JsonProperty
    private void setToponymName(String toponymName)
    {
        this.toponymName = toponymName;
    }
    @JsonProperty
    private void setGeonameId(String geonameId)
    {
        this.geonameId = geonameId;
    }
    @JsonProperty
    private void setAlternateName(List<GeonameLangValue> alternateName)
    {
        this.alternateName = alternateName;
    }
    @JsonIgnore
    private String getToponymName()
    {
        return toponymName;
    }
    @JsonIgnore
    private String getGeonameId()
    {
        return geonameId;
    }
    @JsonIgnore
    private List<GeonameLangValue> getAlternateName()
    {
        return alternateName;
    }
    @JsonIgnore
    private boolean isTriedLink()
    {
        return triedLink;
    }
    @JsonIgnore
    private URI getCachedLink()
    {
        return cachedLink;
    }

    //-----PRIVATE METHODS-----
    private URI findExternalLink()
    {
        if (!this.triedLink) {
            if (this.alternateName != null) {
                for (GeonameLangValue val : this.alternateName) {
                    if (val != null && val.getLang() != null && val.getLang().equals(LINK_LANGUAGE)) {
                        this.cachedLink = URI.create(val.getValue());
                        //we stop at first sight of a link
                        break;
                    }
                }
            }

            this.triedLink = true;
        }

        return this.cachedLink;
    }
}
