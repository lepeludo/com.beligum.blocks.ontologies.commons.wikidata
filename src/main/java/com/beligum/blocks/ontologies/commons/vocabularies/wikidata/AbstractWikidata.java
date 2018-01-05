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

import com.beligum.blocks.endpoints.ifaces.AutocompleteSuggestion;

import java.net.URI;

/**
 * Created by Bram on 6/01/17.
 */
public abstract class AbstractWikidata
{
    //-----CONSTANTS-----
    protected static final String RESOURCE_TYPE_INJECTABLE = "resourceType";
    protected static final String WIKIDATARDFPREFIX = "http://www.wikidata.org/entity/";
    protected static final String WIKIDATAHUMANPREFIX ="https://www.wikidata.org/wiki/";


    public enum Type
    {
        THING(WikidataSuggestion.class);

        public Class<? extends AutocompleteSuggestion> suggestionClass;
        Type(Class<? extends AutocompleteSuggestion> suggestionClass)
        {
            this.suggestionClass = suggestionClass;
        }
    }



    //-----VARIABLES-----

    //-----CONSTRUCTORS-----

    //-----STATIC METHODS-----
    public static URI toWikidataUri(String wikidataId)
    {
        return URI.create(WIKIDATARDFPREFIX + wikidataId);
    }


    //-----PUBLIC METHODS-----

    //-----PROTECTED METHODS-----

    //-----PRIVATE METHODS-----
}
