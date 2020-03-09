
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
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
 
 package anywheresoftware.b4j.objects;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClient.FailureListener;
import org.json.JSONException;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.collections.Map.MyMap;
import anywheresoftware.b4j.objects.collections.JSONParser;
import anywheresoftware.b4j.objects.collections.JSONParser.JSONGenerator;

/**
 * REST client for Elasticsearch.
 *Note that all methods are synchronous. The client is expected to be used in server solutions.
 *Each document is identified by the <Index, Type, Id> tuple.
 */
@Version(1.10f)
@ShortName("ESClient")
@DependsOn(values = {"commons-codec", "httpasyncclient-4.1.2", "httpclient-4.5.2", "httpcore-4.4.5",
		"httpcore-nio-4.4.5", "rest-6.0.0", "commons-logging-1.1.3", "json"})
public class RestClientWrapper {
	@Hide
	public static final Charset utf8 = Charset.forName("utf8");
	@Hide
	public RestClient client;
	/**
	 * Initializes the client and sets the list of hosts.
	 *EventName - Currently there are no events.
	 *Hosts - A list or array with one or more hosts.
	 *Example:<code>
	 *esclient.Initialize("", Array("127.0.0.1:9200"))</code>
	 */
	public void Initialize(BA ba, String EventName, List Hosts) {
		HttpHost[] hh = new HttpHost[Hosts.getSize()];
		for (int i = 0;i < Hosts.getSize();i++) {
			String r = (String) Hosts.Get(i);
			int c = r.indexOf(':');
			hh[i] = new HttpHost(r.substring(0, c), Integer.parseInt(r.substring(c + 1)));
		}
		client = RestClient.builder(hh).setFailureListener(new FailureListener() {
			public void onFailure(HttpHost host) {
				System.err.println("OnFailure: " + host);
	        }
		}).build();
	}
	/**
	 * Inserts or replaces a document. Set the Id to an empty string to create the id automatically.
	 *Returns the server response.
	 */
	public ResponseWrapper Insert(String Index, String Type, String Id, Map Document) throws Exception {
		return PerformRawRequest(Id.isEmpty() ? "POST": "Put",endpoint(Index, Type, Id), null, mapToString(Document));
	}
	/**
	 * Bulk inserts multiple documents.
	 *IdsAndDocuments is a list (or array) with pairs of ids and documents.
	 *Pass empty strings as the ids to let Elasticsearch create the ids.
	 *Example: <code>
	 *client.BulkInsert("index1", "type1", Array("id1", CreateMap("text": "doc1"), "id2", CreateMap("text": "doc2"))</code>
	 */
	public ResponseWrapper BulkInsert (String Index, String Type, List IdsAndDocuments) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0;i < IdsAndDocuments.getSize(); i+= 2) {
			String id = (String) IdsAndDocuments.Get(i);
			MyMap m = (MyMap) IdsAndDocuments.Get(i + 1);
			Map mid = new Map();
			mid.Initialize();
			MyMap mid2 = new MyMap();
			mid.Put("index", mid2);
			if (id.isEmpty() == false) {
				
				mid2.put("_id", id);
			}
			sb.append(mapToString(mid)).append("\n");
			mid.setObject(m);
			sb.append(mapToString(mid)).append("\n");
		}
		return PerformRawRequest("POST", endpoint(Index, Type, "_bulk"), null, sb.toString());
	}
	/**
	 * Makes a search request.
	 */
	public ResponseWrapper Search(String Index, String Type, Map Query) throws Exception {
		return PerformRawRequest("GET", endpoint(Index, Type, "_search"), null, mapToString(Query));
	}
	/**
	 * Checks whether a document with the given Index, Type and Id exists.
	 */
	public boolean Exists(String Index, String Type, String Id) throws Exception {
		ResponseWrapper rw = PerformRawRequest("HEAD", endpoint(Index, Type, Id), null, "");
		return rw.getStatusCode() == 200;
	}
	/**
	 * Returns the document. An exception will be thrown if there is no such document.
	 */
	public Map Get(String Index, String Type, String Id) throws Exception {
		ResponseWrapper rw = PerformRawRequest("GET", endpoint(Index, Type, Id), null, "");
		Map m = new Map();
		m.setObject((MyMap)rw.ResponseAsMap().Get("_source"));
		return m;
	}
	/**
	 * Deletes the document. An exception will be thrown if there is no such document.
	 */
	public ResponseWrapper Delete(String Index, String Type, String Id) throws Exception {
		return PerformRawRequest("DELETE", endpoint(Index, Type, Id), null, "");
	}
	private String endpoint(String Index, String Type, String Id) {
		if (Index.isEmpty() && Type.isEmpty() == false)
			throw new RuntimeException("Type parameter must be empty if the Index parameter is empty.");
		return "/" + (Index.isEmpty() ? "" : Index + "/") + (Type.isEmpty() ? "" : Type + "/") + Id;
	}
	private String mapToString(Map m) throws Exception {
		JSONGenerator jg = new JSONGenerator();
		jg.Initialize(m);
		return jg.ToString();
	}
	/**
	 * Performs a raw request.
	 *Method - Request method (GET, POST, ...)
	 *EndPoint - Request end point.
	 *QueryParameters - Map of query parameters. Pass Null if not required.
	 *Payload - Body payload. Pass an empty string if not required. 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResponseWrapper PerformRawRequest (String Method, String Endpoint, Map QueryParameters, String Payload) throws Exception {
		Response res = client.performRequest(Method, Endpoint, 
				(QueryParameters == null || QueryParameters.IsInitialized() == false) ? Collections.<String, String>emptyMap() : (java.util.Map)QueryParameters.getObject(), 
				Payload.length() == 0 ? null : new NStringEntity(Payload, ContentType.APPLICATION_JSON));
		return new ResponseWrapper(res);
		
	}
	/**
	 * Closes the client.
	 */
	public void Close() throws IOException {
		if (client != null)
			client.close();
		client = null;
	}
	/**
	 * Holds the server response.
	 */
	@ShortName("ESResponse")
	public static class ResponseWrapper  {
		
		@Hide
		public final Response response;
		private Map parsedMap;
		public ResponseWrapper() {
			this(null);
		}
		public ResponseWrapper(Response response) {
			this.response = response;
		}
		/**
		 * Returns the request status code (200 in most cases).
		 */
		public int getStatusCode() {
			return response.getStatusLine().getStatusCode();
		}
		/**
		 * Returns the response as string.
		 */
		public String ResponseAsString() throws ParseException, IOException {
			return EntityUtils.toString(response.getEntity(), utf8);
		}
		/**
		 * Parses the JSON response and returns a map with the result.
		 *Note that the map is cached after it is parsed.
		 */
		public Map ResponseAsMap() throws JSONException, ParseException, IOException {
			if (parsedMap == null) {
				JSONParser par = new JSONParser();
				par.Initialize(ResponseAsString());
				parsedMap = par.NextObject();
			}
			return parsedMap;
		}
		/**
		 * Returns the hits. Relevant for search requests.
		 */
		public List getHits() throws ParseException, JSONException, IOException {
			List res = new List();
			Map m = ResponseAsMap();
			MyMap mm = (MyMap) m.Get("hits");
			@SuppressWarnings("unchecked")
			ArrayList<Object> hits = (ArrayList<Object>) mm.get("hits");
			res.setObject(hits);
			return res;
		}
	}
}
