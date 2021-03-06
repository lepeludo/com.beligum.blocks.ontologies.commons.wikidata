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

import com.beligum.blocks.rdf.ifaces.RdfProperty;
import com.beligum.blocks.rdf.ifaces.RdfVocabulary;
import com.beligum.blocks.rdf.ontology.RdfPropertyImpl;
import com.beligum.blocks.rdf.ontology.vocabularies.AbstractRdfVocabulary;
import com.beligum.blocks.rdf.ontology.vocabularies.XSD;

import java.net.URI;

import static gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.*;

/**
 * Using skos, but we might as well use http://schema.org/name or
 * http://www.w3.org/2000/01/rdf-schema#label
 */
public final class SKOS extends AbstractRdfVocabulary
{
    //-----VARIABLES-----

    //-----SINGLETON-----
    public static final RdfVocabulary INSTANCE = new SKOS();
    private SKOS()
    {
        super(URI.create("http://www.w3.org/2004/02/skos/core#"), "SKOS");
    }

    //-----PUBLIC FUNCTIONS-----

    //-----ENTRIES-----
    public static final RdfProperty prefLabel = new RdfPropertyImpl("prefLabel", INSTANCE, SKOS_title_prefLabel, SKOS_label_prefLabel, XSD.STRING);
    public static final RdfProperty altLabel = new RdfPropertyImpl("altLabel", INSTANCE, SKOS_title_altLabel, SKOS_label_altLabel, XSD.STRING);


}
