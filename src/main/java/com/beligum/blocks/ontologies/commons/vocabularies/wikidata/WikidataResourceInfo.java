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
package com.beligum.blocks.ontologies.commons.vocabularies.wikidata;

import com.beligum.base.utils.Logger;
import com.beligum.base.utils.toolkit.StringFunctions;
import com.beligum.blocks.config.RdfFactory;
import com.beligum.blocks.endpoints.ifaces.ResourceInfo;
import com.beligum.blocks.utils.RdfTools;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class WikidataResourceInfo extends AbstractWikidata implements ResourceInfo
{

    private static final String LINK_LANGUAGE = "link";
    //-----VARIABLES-----
    private URI resourceType;
    private String id;
    private URI link;

    private String name;
    //same as name. Can altered
    private String label;
    private URI image;

    private Locale language;

    //temp values...
    private transient boolean triedLink;
    private transient URI cachedLink;

    //-----CONSTRUCTORS-----
    public WikidataResourceInfo()
    {
    }

    //-----PUBLIC METHODS-----
    @Override
    public URI getResourceUri(){
        //URI of the local resource
        String normalizedId = StringFunctions.decodeHtmlUrl(this.id);
        return RdfTools.createRelativeResourceId(RdfFactory.getClassForResourceType(this.getResourceType()), normalizedId);
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
        //return the wikipedia page in proper language. If none was set (probably because it doesn't exist), return the language independent wikidata page.
        URI retVal = this.link;
        if(retVal == null){
            String normalizedId = StringFunctions.decodeHtmlUrl(this.id);
            try {
                retVal = new URI(super.WIKIDATAHUMANPREFIX + normalizedId);
            }
            catch (URISyntaxException e) {
                Logger.error("the URI syntax is wrong for the wikidata stub with ID "+normalizedId+".Returning null. This shouldn't happen");
                e.printStackTrace();
            }
        }

        return  retVal;
    }
    @Override
    public boolean isExternalLink()
    {
        return true;
    }
    @Override
    public URI getImage()
    {
        //return a Wikimedia Commons image link for the wikidata item (if one exists). Return null if you don't want an image.
        return this.image;
    }
    public void setImage(String image) throws URISyntaxException
    {
        this.image = new URI(image);
    }
    @Override
    public String getName()
    {
        return name;
    }
    @Override
    public Locale getLanguage()
    {
        return this.language;
    }

    public void setLanguage(Locale language)
    {
        this.language = language;
    }

    //-----PROTECTED METHODS-----
    public void setResourceType(URI resourceType)
    {
        this.resourceType = resourceType;
    }

    private boolean isTriedLink()
    {
        return triedLink;
    }
    private URI getCachedLink()
    {
        return cachedLink;
    }
    public static String getLinkLanguage()
    {
        return LINK_LANGUAGE;
    }
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = StringFunctions.decodeHtmlUrl(id);
    }
    public void setLink(URI link)
    {
        this.link = link;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public void setLabel(String label)
    {
        this.label = label;
    }
    public void setTriedLink(boolean triedLink)
    {
        this.triedLink = triedLink;
    }
    public void setCachedLink(URI cachedLink)
    {
        this.cachedLink = cachedLink;
    }


}
