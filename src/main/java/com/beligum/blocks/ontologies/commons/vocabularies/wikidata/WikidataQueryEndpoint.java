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

import com.beligum.base.cache.Cache;
import com.beligum.base.cache.EhCacheAdaptor;
import com.beligum.base.server.R;
import com.beligum.base.utils.Logger;
import com.beligum.base.utils.json.Json;
import com.beligum.blocks.config.RdfFactory;
import com.beligum.blocks.endpoints.ifaces.AutocompleteSuggestion;
import com.beligum.blocks.endpoints.ifaces.ResourceInfo;
import com.beligum.blocks.ontologies.commons.vocabularies.SKOS;
import com.beligum.blocks.ontologies.commons.vocabularies.WB;
import com.beligum.blocks.rdf.ifaces.Format;
import com.beligum.blocks.rdf.ifaces.RdfClass;
import com.beligum.blocks.rdf.ifaces.RdfProperty;
import com.beligum.blocks.rdf.importers.SesameImporter;
import com.beligum.blocks.utils.RdfTools;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;

import static com.beligum.blocks.ontologies.commons.config.CacheKeys.WIKIDATA_CACHED_RESULTS;

/**
 * Created by Bram on 6/01/17.
 * <p>
 * Handles queries to Wikidata / Wikipedia.
 */
public class WikidataQueryEndpoint implements com.beligum.blocks.endpoints.ifaces.RdfQueryEndpoint
{

    private final String WIKIPEDIPrefix = "https://";
    private final String WIKIPEDIA_API_URI = ".wikipedia.org/w/api.php";
    private final String WIKIDATA_PUBLIC_URI = "http://www.wikidata.org/entity/";
    private final String action = "action";
    private final String search = "search";
    private AbstractWikidata.Type wikiType;
    private String[] wikidataInstancesOff;
    private RdfProperty[] cachedLabelProps;

    /**
     * Constructer.
     *
     * @param wikiType
     * @param wikidataInstancesOff : class of which the results should be an instance of (e.g. human = Q5). Use overloaded constructor if it should be null.
     *                             Using wikidataInstancesOff will cause an extra sparql query so will be slower.
     */
    public WikidataQueryEndpoint(AbstractWikidata.Type wikiType, String... wikidataInstancesOff)
    {
        this.wikiType = wikiType;
        this.wikidataInstancesOff = wikidataInstancesOff;
    }

    /**
     * @param wikiType
     */
    public WikidataQueryEndpoint(AbstractWikidata.Type wikiType)
    {
        this.wikiType = wikiType;
    }

    @Override
    public boolean isExternal()
    {
        return true;
    }
    @Override
    public Collection<AutocompleteSuggestion> search(RdfClass resourceType, final String query, QueryType queryType, Locale language, int maxResults, SearchOption... options) throws IOException
    {
        try {
            Collection<AutocompleteSuggestion> retVal = this.doSearchQueryForLanguage(query, resourceType, language);
            return retVal;
        }
        catch (Exception ex) {
            throw new IOException(ex);
        }

    }

    /**
     * Looks up the actual resource.
     *
     * @param resourceType
     * @param resourceId   is the id of the 'local' resource, e.g. /resource/WikidataCountry/Q12345. The 'Q12345' is the Wikidata resource id.
     * @param language
     * @return
     * @throws IOException
     */
    @Override
    public ResourceInfo getResource(RdfClass resourceType, URI resourceId, Locale language) throws IOException
    {
        String wikibase_item = null;
        if (resourceId.getAuthority() != null && resourceId.getAuthority().contains("wikipedia")) {
            //from wikipedia, so it's a new one
            String title = resourceId.toString().substring(resourceId.toString().lastIndexOf('/') + 1);
            wikibase_item = this.getWikibase_item(title, language);
        }
        else {
            //should be an existing one
            wikibase_item = resourceId.toString().substring(resourceId.toString().lastIndexOf('/') + 1);
        }

        WikidataResourceInfo retVal = null;
        if (this.wikiType.equals(AbstractWikidata.Type.THING)) {
            //dutch, english and french implemented now
            String query = "PREFIX schema: <http://schema.org/> SELECT ?dataLabel ?sitelink ?pic ?lang WHERE {BIND(wd:" +
                           wikibase_item +
                           " AS ?data) { ?sitelink schema:about ?data. ?sitelink schema:isPartOf <https://en.wikipedia.org/>.?sitelink schema:inLanguage ?lang. SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\". }}UNION{ ?sitelink schema:about ?data. ?sitelink schema:isPartOf <https://fr.wikipedia.org/>.?sitelink schema:inLanguage ?lang. SERVICE wikibase:label { bd:serviceParam wikibase:language \"?lang, fr\". } }UNION{ ?sitelink schema:about ?data. ?sitelink schema:isPartOf <https://nl.wikipedia.org/>.?sitelink schema:inLanguage ?lang. SERVICE wikibase:label { bd:serviceParam wikibase:language \"?lang, nl\". } } OPTIONAL{?data wdt:P18 ?pic}}";
            //                           " AS ?data) { ?sitelink schema:about ?data. ?sitelink schema:isPartOf <https://en.wikipedia.org/>.?sitelink schema:inLanguage ?lang. SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\". }}UNION{ ?sitelink schema:about ?data. ?sitelink schema:isPartOf <https://fr.wikipedia.org/>.?sitelink schema:inLanguage ?lang. SERVICE wikibase:label { bd:serviceParam wikibase:language \"?lang, en\". } }UNION{ ?sitelink schema:about ?data. ?sitelink schema:isPartOf <https://nl.wikipedia.org/>.?sitelink schema:inLanguage ?lang. SERVICE wikibase:label { bd:serviceParam wikibase:language \"?lang, en\". } } OPTIONAL{?data wdt:P18 ?pic}}";
            RepositoryConnection sparqlConnection = null;
            try {
                SPARQLRepository sparqlRepository = new SPARQLRepository("https://query.wikidata.org/sparql");
                sparqlRepository.initialize();
                sparqlConnection = sparqlRepository.getConnection();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            TupleQuery tupleQuery = null;
            try {
                tupleQuery = sparqlConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            }
            catch (MalformedQueryException e) {
                e.printStackTrace();
            }
            catch (RepositoryException e) {
                e.printStackTrace();
            }
            Map<String, TupleResult> tupleResults = new HashMap<>();
            try {
                TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
                while (tupleQueryResult.hasNext()) {
                    BindingSet bindingSet = tupleQueryResult.next();
                    String lang = bindingSet.getValue("lang").stringValue();
                    String labelForLanguage = bindingSet.getValue("dataLabel") == null ? null : bindingSet.getValue("dataLabel").stringValue();
                    String label = labelForLanguage;
                    String pic = bindingSet.getValue("pic") == null ? null : bindingSet.getValue("pic").stringValue();
                    String sitelink = bindingSet.getValue("sitelink") == null ? null : bindingSet.getValue("sitelink").stringValue();
                    tupleResults.put(lang, new TupleResult(
                                    lang,
                                    label,
                                    pic,
                                    sitelink
                    ));
                }
            }
            catch (QueryEvaluationException e) {
                e.printStackTrace();
            }

            //see if we got it in the proper language
            TupleResult choosenTupleResult = null;

            if(tupleResults.size() > 0){
                if (tupleResults.keySet().contains(language.toLanguageTag())) {
                    choosenTupleResult = tupleResults.get(language.toLanguageTag());
                }
                else if(tupleResults.keySet().contains(Locale.ENGLISH.toLanguageTag())){
                    //fall back to english
                    choosenTupleResult = tupleResults.get(language.toLanguageTag());
                }else{
                    //pic the first
                    String key = null;
                    Iterator<String> it = tupleResults.keySet().iterator();
                    if(it.hasNext()){
                        choosenTupleResult = tupleResults.get(it.next());
                    }
                }
            }

            retVal = new WikidataResourceInfo();
            try {
                //try linking to wikipedia. If not exists, link to wikidata
                if (choosenTupleResult != null && choosenTupleResult.getSitelink() != null) {
                    retVal.setLink(new URI(choosenTupleResult.getSitelink()));
                }
            }
            catch (URISyntaxException e) {
                e.printStackTrace();
            }

            retVal.setId(wikibase_item);
            if (choosenTupleResult != null && choosenTupleResult.getLabel() != null) {
                retVal.setName(choosenTupleResult.getLabel());
            }
            else {
                retVal.setName(wikibase_item);
            }
            if (choosenTupleResult != null && choosenTupleResult.getImage() != null) {
                try{
                    retVal.setImage(choosenTupleResult.getImage());
                }
                catch (URISyntaxException e) {
                    Logger.error("URISyntaxException for"+choosenTupleResult.getImage());
                }
            }
            retVal.setLanguage(language);
            retVal.setResourceType(resourceType.getCurieName());
        }
        if (wikibase_item == null) {
            return null;
        }
        else {
            return retVal;
        }
    }
    @Override
    public RdfProperty[] getLabelCandidates(RdfClass rdfClass)
    {
        if (this.cachedLabelProps == null) {
            this.cachedLabelProps = new RdfProperty[] { SKOS.prefLabel, SKOS.altLabel };
        }

        return this.cachedLabelProps;
    }
    @Override
    public URI getExternalResourceId(URI resourceId, Locale locale)
    {
        return AbstractWikidata.toWikidataUri(new RdfTools.RdfResourceUri(resourceId).getResourceId());
    }
    @Override
    public Model getExternalRdfModel(RdfClass rdfClass, URI resourceId, Locale language) throws IOException
    {
        Model retVal = null;
        if (resourceId != null && !resourceId.toString().isEmpty()) {

            //use a cached result if it's there
            WikidataQueryEndpoint.CachedExternalModel cacheKey = new WikidataQueryEndpoint.CachedExternalModel(rdfClass, resourceId, language);
            Model cachedResult = this.getCachedEntry(cacheKey);
            if (cachedResult != null) {
                retVal = cachedResult;
            }
            else {
                Client httpClient = null;

                try {
                    httpClient = ClientBuilder.newClient(new ClientConfig());
                    URI rdfUri = this.getExternalResourceId(resourceId, language);
                    //the wikidata endpoint is actually https.
                    String[] segments = rdfUri.getPath().split("/");
                    String idStr = segments[segments.length - 1];
                    //we'll query the https directly so we don't get redirected
                    rdfUri = new URI("https://www.wikidata.org/entity/" + idStr);
                    //this seems to be the only RDF format that's accepted
                    Response response = httpClient.target(rdfUri).request("application/rdf+xml").get();
                    if (response.getStatus() >= 300 && response.getStatus() < 400) {
                        //catch redirect
                        URI redirectUri = new URI(response.getHeaders().get("Location").get(0).toString());
                        response = httpClient.target(redirectUri).request("application/rdf+xml").get();
                    }
                    if (response.getStatus() == Response.Status.OK.getStatusCode()) {

                        Model remoteModel = new SesameImporter(Format.RDF_XML).importDocument(rdfUri, new ByteArrayInputStream(response.readEntity(String.class).getBytes()));
                        //if we get here, it means we have a model, empty or not
                        retVal = new LinkedHashModel();

                        for (Statement statement : remoteModel) {
                            //only adds the properties that hava to do with the actual entity.
                            //Note that the predicate uses http, while the actual URL uses https.
                            if (statement.getSubject().stringValue().equals("http://www.wikidata.org/entity/" + new RdfTools.RdfResourceUri(resourceId).getResourceId())) {
                                retVal.add(statement);
                            }
                        }
                    }
                    else {
                        throw new IOException("Wikidata RDF endpoint returned unexpected http code (" + response.getStatus() + ") when fetching dependency model for " + resourceId + "; " +
                                              response.getStatusInfo().getReasonPhrase());
                    }

                    this.putCachedEntry(cacheKey, retVal);
                }
                catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                finally {
                    if (httpClient != null) {
                        httpClient.close();
                    }
                }
            }
        }

        return retVal;
    }
    @Override
    public RdfClass getExternalClasses(RdfClass rdfClass)
    {
        return WB.Item;
    }

    private String getBaseString(Locale locale)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.WIKIPEDIPrefix);
        sb.append(locale.getLanguage());
        sb.append(this.WIKIPEDIA_API_URI);
        return sb.toString();
    }

    private String getWikibase_item(String title, Locale language)
    {
        String wikidataApiString = "https://www.wikidata.org/w/api.php?";
        String action = "action";
        String wbgetentities = "wbgetentities";
        String sites = "sites";
        String wikiString = language.toString() + "wiki";
        String titles = "titles";

        Client httpClient = null;
        Response response = null;
        ClientConfig config = new ClientConfig();
        httpClient = ClientBuilder.newClient(config);
        UriBuilder builder = UriBuilder.fromUri(wikidataApiString)
                                       .queryParam(action, wbgetentities)
                                       .queryParam(sites, wikiString)
                                       .queryParam("format", "json")
                                       .queryParam(titles, title);

        URI target = builder.build();
        response = httpClient.target(target).request(MediaType.APPLICATION_JSON).get();
        String wikibase_item = null;
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            JsonNode jsonNode = null;
            try {
                jsonNode = Json.getObjectMapper().readTree(response.readEntity(String.class));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            wikibase_item = jsonNode.get("entities").fields().next().getKey();
        }
        Logger.error("no wikibase item found for: " + title);
        return wikibase_item;
    }

    private synchronized Collection<AutocompleteSuggestion> doSearchQueryForLanguage(String query, RdfClass resourceType, Locale language) throws IOException
    {
        Set<AutocompleteSuggestion> retVal = new HashSet<>();
        Set<AutocompleteSuggestion> tempVal = new HashSet<>();
        Client httpClient = null;

        Response response = null;
        try {
            //basestring in english
            String englishBaseString = this.getBaseString(Locale.ENGLISH);

            //basestring for current language
            String baseString = this.getBaseString(language);

            ClientConfig config = new ClientConfig();
            httpClient = ClientBuilder.newClient(config);

            UriBuilder builder = UriBuilder.fromUri("https://www.wikidata.org/w/api.php?")
                                           .queryParam(action, "wbsearchentities")
                                           .queryParam("format", "json")
                                           .queryParam("language", language)
                                           .queryParam("type", "item")
                                           .queryParam("continue", "0")
                                           .queryParam("limit", "15")
                                           .queryParam(search, query);
            URI target = builder.build();

            response = httpClient.target(target).request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                //the request will return a jsonNode.
                // Resource Titles and links are in seperate trees with the same order.
                //response example in json : https://en.wikipedia.org/w/api.php?action=opensearch&search=tank&format=json
                JsonNode jsonNode = Json.getObjectMapper().readTree(response.readEntity(String.class));

                Map<String, AutocompleteSuggestion> suggestionMap = new HashMap<>();
                for (JsonNode iteratingNode : jsonNode.get("search")) {
                    String label = iteratingNode.get("label") == null ? "" : iteratingNode.get("label").textValue();
                    String description = iteratingNode.get("description") == null ? "" : iteratingNode.get("description").textValue();
                    String id = iteratingNode.get("id") == null ? "" : iteratingNode.get("id").textValue();
                    WikidataSuggestion autocompleteSuggestion = new WikidataSuggestion();
                    autocompleteSuggestion.setLabel(label);
                    autocompleteSuggestion.setSubtitle(description);
                    autocompleteSuggestion.setUri(RdfTools.createRelativeResourceId(RdfFactory.getClassForResourceType(resourceType.getCurieName())
                                    , id).toString());
                    autocompleteSuggestion.setLanguage(language.getLanguage());
                    autocompleteSuggestion.setWikidatatId(id);
                    tempVal.add(autocompleteSuggestion);
                    suggestionMap.put(id, autocompleteSuggestion);
                }
                if (this.wikidataInstancesOff != null) {

                    //Start iterating over the entityIds. Add them to the  query.
                    StringBuilder valuesBuilder = new StringBuilder("VALUES ?values {");
                    for (String wikiClass : this.wikidataInstancesOff) {
                        valuesBuilder.append(" wd:");
                        valuesBuilder.append(wikiClass);
                    }
                    valuesBuilder.append("} ");
                    String values = valuesBuilder.toString();
                    Iterator<String> entityIdIterator = suggestionMap.keySet().iterator();
                    StringBuilder totalSelectQuery = null;
                    while (entityIdIterator.hasNext()) {
                        //this is the current wikidataId.
                        String currentIteratedId = entityIdIterator.next();
                        if (totalSelectQuery == null) {
                            totalSelectQuery = new StringBuilder();
                            totalSelectQuery.append("SELECT ?item WHERE {");
                            totalSelectQuery.append(values);
                            totalSelectQuery.append(" {BIND (wd:");
                            totalSelectQuery.append(currentIteratedId);
                            totalSelectQuery.append(" as ?item) ?item wdt:P31 ?values}");
                        }
                        else {
                            totalSelectQuery.append("UNION{ BIND (wd:" + currentIteratedId + " as ?item) ?item wdt:P31 ?values}");
                        }

                    }
                    //add a closing bracket
                    totalSelectQuery.append("}");
                    RepositoryConnection sparqlConnection = null;
                    try {
                        SPARQLRepository sparqlRepository = new SPARQLRepository("https://query.wikidata.org/sparql");
                        sparqlRepository.initialize();
                        sparqlConnection = sparqlRepository.getConnection();

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    TupleQuery tupleQuery = null;
                    try {
                        tupleQuery = sparqlConnection.prepareTupleQuery(QueryLanguage.SPARQL, totalSelectQuery.toString());
                    }
                    catch (MalformedQueryException e) {
                        e.printStackTrace();
                    }
                    catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                    String label = null;
                    retVal = new HashSet<>();

                    try {
                        TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
                        while (tupleQueryResult.hasNext()) {
                            //?item is the wikidataId, it should be the only thing we find
                            BindingSet bindingSet = tupleQueryResult.next();
                            String wikidataItemId = bindingSet.getValue("item").stringValue();
                            String qId = wikidataItemId.substring(wikidataItemId.lastIndexOf('/') + 1);
                            retVal.add(suggestionMap.get(qId));
                        }
                    }
                    catch (QueryEvaluationException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    retVal = tempVal;
                }
            }

        }
        catch (UnknownHostException uhe) {
            Logger.error("UnknownHostException thrown for " + query + ". No internet connection?");
        }
        catch (ProcessingException exception) {
            Logger.error("ProcessingException thrown for " + query + ". No internet connection?");
        }

        finally {
            if (response != null) {
                response.close();
            }
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return retVal;
    }

    private synchronized Collection<AutocompleteSuggestion> getCachedEntry(WikidataQueryEndpoint.CachedSearch query)
    {
        return (Collection<AutocompleteSuggestion>) this.getWikidataCache().get(query);
    }
    private synchronized void putCachedEntry(WikidataQueryEndpoint.CachedSearch key, Collection<AutocompleteSuggestion> results)
    {
        this.getWikidataCache().put(key, results);
    }
    private synchronized Model getCachedEntry(WikidataQueryEndpoint.CachedExternalModel externalModel)
    {
        return (Model) this.getWikidataCache().get(externalModel);
    }
    private synchronized void putCachedEntry(WikidataQueryEndpoint.CachedExternalModel key, Model model)
    {
        this.getWikidataCache().put(key, model);
    }
    private synchronized Cache getWikidataCache()
    {
        if (!R.cacheManager().cacheExists(WIKIDATA_CACHED_RESULTS.name())) {
            //we instance a cache where it's entries live for one day (both from creation time as from last accessed time),
            //Overflows to disk and keep at most 10000 results
            R.cacheManager().registerCache(new EhCacheAdaptor(WIKIDATA_CACHED_RESULTS.name(), 100, false, false, 24 * 60 * 60, 24 * 60 * 60));
        }
        return R.cacheManager().getCache(WIKIDATA_CACHED_RESULTS.name());
    }

    /**
     * This class makes sure the hashmap takes all query parameters into account while caching the resutls
     */
    private static class CachedSearch
    {
        private AbstractWikidata.Type wikidataType;
        private RdfClass resourceType;
        private String query;
        private QueryType queryType;
        private Locale language;
        private SearchOption[] options;

        public CachedSearch(AbstractWikidata.Type wikidataType, RdfClass resourceType, String query, QueryType queryType, Locale language, SearchOption[] options)
        {
            this.wikidataType = wikidataType;
            this.resourceType = resourceType;
            this.query = query;
            this.queryType = queryType;
            this.language = language;
            this.options = options;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WikidataQueryEndpoint.CachedSearch)) {
                return false;
            }

            WikidataQueryEndpoint.CachedSearch that = (WikidataQueryEndpoint.CachedSearch) o;

            if (wikidataType != that.wikidataType) {
                return false;
            }
            if (resourceType != null ? !resourceType.equals(that.resourceType) : that.resourceType != null) {
                return false;
            }
            if (query != null ? !query.equals(that.query) : that.query != null) {
                return false;
            }
            if (queryType != that.queryType) {
                return false;
            }
            if (language != null ? !language.equals(that.language) : that.language != null) {
                return false;
            }
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(options, that.options);

        }
        @Override
        public int hashCode()
        {
            int result = wikidataType != null ? wikidataType.hashCode() : 0;
            result = 31 * result + (resourceType != null ? resourceType.hashCode() : 0);
            result = 31 * result + (query != null ? query.hashCode() : 0);
            result = 31 * result + (queryType != null ? queryType.hashCode() : 0);
            result = 31 * result + (language != null ? language.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(options);
            return result;
        }
    }

    private static class CachedResource
    {
        private RdfClass resourceType;
        private URI resourceId;
        private Locale language;

        public CachedResource(RdfClass resourceType, URI resourceId, Locale language)
        {
            this.resourceType = resourceType;
            this.resourceId = resourceId;
            this.language = language;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WikidataQueryEndpoint.CachedResource)) {
                return false;
            }

            WikidataQueryEndpoint.CachedResource that = (WikidataQueryEndpoint.CachedResource) o;

            if (resourceType != null ? !resourceType.equals(that.resourceType) : that.resourceType != null) {
                return false;
            }
            if (resourceId != null ? !resourceId.equals(that.resourceId) : that.resourceId != null) {
                return false;
            }
            return language != null ? language.equals(that.language) : that.language == null;

        }
        @Override
        public int hashCode()
        {
            int result = resourceType != null ? resourceType.hashCode() : 0;
            result = 31 * result + (resourceId != null ? resourceId.hashCode() : 0);
            result = 31 * result + (language != null ? language.hashCode() : 0);
            return result;
        }
    }

    private static class CachedExternalModel
    {
        private RdfClass resourceType;
        private URI resourceId;
        private Locale language;

        public CachedExternalModel(RdfClass resourceType, URI resourceId, Locale language)
        {
            this.resourceType = resourceType;
            this.resourceId = resourceId;
            this.language = language;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WikidataQueryEndpoint.CachedExternalModel)) {
                return false;
            }

            WikidataQueryEndpoint.CachedExternalModel that = (WikidataQueryEndpoint.CachedExternalModel) o;

            if (resourceType != null ? !resourceType.equals(that.resourceType) : that.resourceType != null) {
                return false;
            }
            if (resourceId != null ? !resourceId.equals(that.resourceId) : that.resourceId != null) {
                return false;
            }
            return language != null ? language.equals(that.language) : that.language == null;

        }
        @Override
        public int hashCode()
        {
            int result = resourceType != null ? resourceType.hashCode() : 0;
            result = 31 * result + (resourceId != null ? resourceId.hashCode() : 0);
            result = 31 * result + (language != null ? language.hashCode() : 0);
            return result;
        }
    }

    private class TupleResult
    {
        private String label = null;
        private String sitelink = null;
        private String image = null;
        private String languageTag = null;

        public TupleResult(String languageTag, String label, String image, String siteLink)
        {
            this.label = label;
            this.sitelink = siteLink;
            this.image = image;
            this.languageTag = languageTag;
        }

        public String getLabel()
        {
            return label;
        }
        public String getSitelink()
        {
            return sitelink;
        }
        public String getImage()
        {
            return image;
        }
        public String getLanguageTag()
        {
            return languageTag;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            TupleResult result = (TupleResult) o;

            return languageTag.equals(result.languageTag);
        }
        @Override
        public int hashCode()
        {
            return languageTag.hashCode();
        }
    }

}
