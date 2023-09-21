
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

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.collections.Map.MyMap;

import com.mongodb.client.MongoDatabase;

/**
 * Provides access to a database.
 */
@ShortName("MongoDatabase")
public class MongoDatabaseWrapper extends AbsObjectWrapper<MongoDatabase>{
	/**
	 * Deletes the database.
	 */
	public void Drop() {
		getObject().drop();
	}
	/**
	 * Returns a MongoCollection. Creates the collection if it does not already exist.
	 */
	public MongoCollectionWrapper GetCollection(String CollectionName) {
		return (MongoCollectionWrapper)AbsObjectWrapper.ConvertToWrapper(new MongoCollectionWrapper(), getObject().getCollection(CollectionName, MyMap.class));
	}
	/**
	 * Gets the database name.
	 */
	public String getName() {
		return getObject().getName();
	}
	/**
	 * Lists the collections names.
	 */
	public List getCollectionNames() {
		return MongoUtils.ListFromIterable(getObject().listCollectionNames());
	}
	/**
	 * Runs a database command.
	 */
	public Map RunCommand(Map Command) {
		return (Map)AbsObjectWrapper.ConvertToWrapper(new Map(), getObject().runCommand(MongoUtils.MapToBson((MyMap) Command.getObject()), MyMap.class));
	}
	
}
