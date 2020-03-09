
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
 
 package anywheresoftware.mongo;

import java.util.ArrayList;
import java.util.HashMap;

import org.bson.Document;
import org.bson.conversions.Bson;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.collections.Map.MyMap;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

/**
 * Represents a single collection.
 */
@ShortName("MongoCollection")
public class MongoCollectionWrapper extends AbsObjectWrapper<MongoCollection<MyMap>>{
	/**
	 * Gets the number of documents in the collection.
	 */
	public long getCount() {
		return getObject().count();
	}
	/**
	 * Deletes the collection.
	 */
	public void Drop() {
		getObject().drop();
	}
	/**
	 * Creates an index. Returns the index name.
	 *Example: <code>
	 *Collection.CreateIndex(CreateMap("rank": 1))</code>
	 */
	public String CreateIndex (Map Keys) {
		return getObject().createIndex(MongoUtils.MapToBson(Keys.getObject()));
	}
	/**
	 * Finds matching documents in the collection.
	 *Filter - A Map with the filter.
	 *Projection - List with the field names that should be returned. Pass Null to return all fields.
	 *Sort - The sort map. Pass Null if not needed.
	 *
	 *Example: <code>
	 *Dim res As List = Collection.Find(CreateMap("status": "good"), Null, CreateMap("score": 1))</code>
	 */
	public List Find(Map Filter, List Projection, Map Sort) {
		return Find2(Filter, Projection, Sort, 0, 0);
	}
	/**
	 * Similar to Find
	 *Skip - Number of documents to skip.
	 *Limit - Maximum number of documents to return.
	 */
	public List Find2(Map Filter, List Projection, Map Sort, int Skip, int Limit) {
		FindIterable<MyMap> ff = Filter.IsInitialized() ? getObject().find(MongoUtils.MapToBson(Filter.getObject())) : getObject().find();
		if (Projection.IsInitialized()) {
			boolean idFound = false;
			HashMap<String, Object> fields = new HashMap<String, Object>();
			for (Object field : Projection.getObject()) {
				fields.put((String)field, true);
				if (String.valueOf(field).equals("_id"))
					idFound = true;
			}
			if (idFound == false)
				fields.put("_id", false);
			ff = ff.projection(new Document(fields));
		}
		if (Sort.IsInitialized())
			ff = ff.sort(MongoUtils.MapToBson(Sort.getObject()));
		if (Skip > 0)
			ff = ff.skip(Skip);
		if (Limit > 0)
			ff = ff.limit(Limit);
		return MongoUtils.ListFromIterable(ff);
	}
	/**
	 * Executes an aggregation pipeline. Each element in the list is a Map that defines a single step.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List Aggregate(List Pipeline) {
		ArrayList<Bson> steps = new ArrayList<Bson>();
		for (Object o : Pipeline.getObject())
			steps.add(MongoUtils.MapToBson((MyMap)o));
		return MongoUtils.ListFromIterable(getObject().aggregate(steps));
	}
	/**
	 * Inserts one or more documents.
	 * Example: <code>Collection.Insert(Array(CreateMap("key1": 100, "key2": 200)))</code>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void Insert(List Documents) {
		getObject().insertMany((java.util.List)Documents.getObject());
	}
	/**
	 * Updates matching documents.
	 *Filter - The filter map.
	 *Update - Update operations.
	 *
	 *Example: <code>
	 *Collection.Update(CreateMap("_id": "some_id"), CreateMap("$set": CreateMap("score": 3)))</code>
	 */
	public UpdateResultWrapper Update(Map Filter, Map Update) {
		return (UpdateResultWrapper)AbsObjectWrapper.ConvertToWrapper(new UpdateResultWrapper(), getObject().updateMany(MongoUtils.MapToBson(Filter.getObject()),
				MongoUtils.MapToBson(Update.getObject())));
	}
	/**
	 * Replaces the first matching document.
	 *Filter - The filter map.
	 *Document - New document.
	 *Upsert - If true then the document will be inserted if there is no match.
	 */
	public UpdateResultWrapper Replace(Map Filter, Map Document, boolean Upsert) {
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(Upsert);
		return (UpdateResultWrapper)AbsObjectWrapper.ConvertToWrapper(new UpdateResultWrapper(), getObject().replaceOne(MongoUtils.MapToBson(Filter.getObject()),
				Document.getObject(), opt));
	}
	/**
	 * Deletes the documents matching the filter. Returns the number of documents deleted.
	 */
	public long Delete(Map Filter) {
		return getObject().deleteMany(MongoUtils.MapToBson(Filter.getObject())).getDeletedCount();
	}


	@ShortName("UpdateResult")
	public static class UpdateResultWrapper extends AbsObjectWrapper<UpdateResult> {
		public long getMatchedCount() {
			return getObject().getMatchedCount();
		}
		public long getModifiedCount() {
			return getObject().getModifiedCount();
		}

	}
}
