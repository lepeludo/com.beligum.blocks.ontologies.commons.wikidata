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

import com.beligum.base.utils.toolkit.StringFunctions;
import com.beligum.blocks.endpoints.ifaces.AutocompleteSuggestion;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Bram on 6/01/17.
 */
public abstract class AbstractWikidataSuggestion extends AbstractWikidata implements AutocompleteSuggestion
{

    protected URI resourceType;
    protected String uri;
    protected String subtitle;
    protected String language;
    protected String name;
    protected String description;
    protected String wikidatatId;


    //    protected String toponymName;

    //-----CONSTRUCTORS-----

    public String getWikidatatId()
    {
        return wikidatatId;
    }
    public void setWikidatatId(String wikidatatId)
    {
        this.wikidatatId = wikidatatId;
    }
    //-----PUBLIC METHODS-----
    @Override
    public String getValue()
    {

//        String title = this.uri.toString().substring(this.uri.toString().lastIndexOf('/') + 1);
//        //action=wbgetentities&sites=enwiki&titles=Camion
//        String wikidataApiString = "https://www.wikidata.org/w/api.php?";
//        String action = "action";
//        String wbgetentities = "wbgetentities";
//        String sites = "sites";
//        String wikiString = this.language.toString() + "wiki";
//        String titles = "titles";
//
//        Client httpClient = null;
//        Response response = null;
//        ClientConfig config = new ClientConfig();
//        httpClient = ClientBuilder.newClient(config);
//        //            //for details, see http://www.geonames.org/export/geonames-search.html
//        UriBuilder builder = UriBuilder.fromUri(wikidataApiString)
//                                       //no need to fetch the entire node; we'll do that during selection
//                                       .queryParam(action, wbgetentities)
//                                       .queryParam(sites, wikiString)
//                                       .queryParam("format", "json")
//                                       .queryParam(titles, title);
//
//        URI target = builder.build();
//        response = httpClient.target(target).request(MediaType.APPLICATION_JSON).get();
//        String wikibase_item = null;
//        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
//            JsonNode jsonNode = null;
//            try {
//                jsonNode = Json.getObjectMapper().readTree(response.readEntity(String.class));
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//            wikibase_item = jsonNode.get("entities").fields().next().getKey();
//        }

//        String id = uri.substring(uri.lastIndexOf('/') + 1);
//        return RdfTools.createRelativeResourceId(RdfFactory.getClassForResourceType(this.getResourceType()), name).toString();
//        String id = uri.substring(uri.lastIndexOf('/') + 1);
//        Logger.info(wikibase_item);
        return uri;
    }

    @Override
    public URI getResourceType()
    {
        return resourceType;
    }
    @Override
    public URI getPublicPage()
    {
        URI retVal = null;
        //Note: it makes sense to return the resource address as the public page; the application endpoint will decide what to do with it
        try {
            retVal = new URI(uri);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    @Override
    public String getTitle()
    {
        return label;
    }
    @Override
    public String getSubTitle()
    {
        return subtitle;
    }

    //-----PROTECTED METHODS-----

    //-----PRIVATE METHODS-----
    @JacksonInject(RESOURCE_TYPE_INJECTABLE)
    public void setResourceType(URI resourceType)
    {
        this.resourceType = resourceType;
    }
    @JsonIgnore
    public String getUri()
    {
        return uri;
    }
    @JsonProperty
    public void setUri(String uri)
    {
        //normalize the uri to correspond with the framework
        this.uri = StringFunctions.decodeHtmlUrl(uri);
    }
    @JsonIgnore
    private String getName()
    {
        return name;
    }
//    @JsonProperty
    public void setName(String name)
    {
        this.name = name;
    }
    @JsonIgnore
    private String getDescription()
    {
        return description;
    }
    @JsonProperty
    public void setDescription(String description)
    {
        this.description = description;
    }
    @JsonIgnore
    public String getLabel()
    {
        return label;
    }
    @JsonProperty
    public void setLabel(String label)
    {
        this.label = label;
    }
    protected String label;
    public String getLanguage()
    {
        return language;
    }
    public void setLanguage(String language)
    {
        this.language = language;
    }
    public void setSubtitle(String subtitle)
    {
        this.subtitle = subtitle;
    }
}
