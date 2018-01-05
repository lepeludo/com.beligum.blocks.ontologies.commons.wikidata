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

import com.beligum.blocks.rdf.ifaces.RdfClass;
import com.beligum.blocks.rdf.ifaces.RdfVocabulary;
import com.beligum.blocks.rdf.ontology.RdfClassImpl;
import com.beligum.blocks.rdf.ontology.vocabularies.AbstractRdfVocabulary;

import java.net.URI;

import static gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.WB_label_Item;
import static gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.WB_title_Item;

/**
 * Created by bram on 2/28/16.
 */
public final class WB extends AbstractRdfVocabulary
{
    //-----VARIABLES-----

    //-----SINGLETON-----
    public static final RdfVocabulary INSTANCE = new WB();
    private WB()
    {
        super(URI.create("http://wikiba.se/ontology-beta#"), "WB");
    }

    //-----PUBLIC FUNCTIONS-----

    //-----ENTRIES-----
    /**
     * The class of OWL individuals
     * Note: this is actually an OwlClass (that subclasses rdfs:Class)
     */
    public static final RdfClass Item = new RdfClassImpl("Item", INSTANCE, WB_title_Item, WB_label_Item, null);


}