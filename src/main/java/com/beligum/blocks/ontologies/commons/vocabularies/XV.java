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

package com.beligum.blocks.ontologies.commons.vocabularies;

import com.beligum.blocks.rdf.ifaces.RdfVocabulary;
import com.beligum.blocks.rdf.ontology.vocabularies.AbstractRdfVocabulary;

import java.net.URI;

/**
 * Extended VCard Vocabulary
 *
 * The Extended VCard Vocabulary defines a set of classes and properties to describe
 * with enough granularity concepts related to the vcard:Address class like Street, neighborhood, district, etc.
 *
 * Created by bram on 2/28/16.
 */
public final class XV extends AbstractRdfVocabulary
{
    //-----SINGLETON-----
    public static final RdfVocabulary INSTANCE = new XV();
    private XV()
    {
        //Note: don't know about the prefix; didn't find many public uses...
        super(URI.create("http://www.bcn.cat/data/v8y/xvcard/#"), "xv");
    }

    //-----ENTRIES-----

}
