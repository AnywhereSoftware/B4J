
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


import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;

/**
 * A MongoDB client. Implemented as a connection pool. A single instance can be used by multiple handlers.
 *<link>MongoDB manual|https://docs.mongodb.com/manual/</link> 
 */
@Version(0.9f)
@DependsOn(values={"mongodb-driver-3.3.0", "mongodb-driver-core-3.3.0", "bson-3.3.0"})
@ShortName("MongoClient")
public class MongoClientWrapper extends AbsObjectWrapper<MongoClient>{
	/**
	 * Initializes the client and sets the connection Uri.
	 *EventName - Currently not used.
	 *ConnectionString - Connection Uri.
	 *Example: <code>
	 *mongo.Initialize("", "mongodb://127.0.0.1:27017")</code>
	 */
	public void Initialize(String EventName, String ConnectionString) {
		Logger mongoLogger = Logger.getLogger( "org.mongodb" );
		mongoLogger.setLevel(Level.SEVERE);
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs(new MapCodec())
				);

		MongoClientOptions.Builder options = MongoClientOptions.builder().codecRegistry(codecRegistry);
		setObject(new MongoClient(new MongoClientURI(ConnectionString, options)));
	}
	/**
	 * Closes the client.
	 */
	public void Close() {
		getObject().close();
	}
	/**
	 * Lists the database names.
	 */
	public List getDatabaseNames() {
		return MongoUtils.ListFromIterable(getObject().listDatabaseNames());
	}
	/**
	 * Returns a MongoDatabase. Creates the database if it does not already exist.
	 */
	public MongoDatabaseWrapper GetDatabase(String DatabaseName) {
		return (MongoDatabaseWrapper)AbsObjectWrapper.ConvertToWrapper(new MongoDatabaseWrapper(), getObject().getDatabase(DatabaseName));
	}
	/**
	 * Utility method that converts a native Date object to ticks.
	 */
	public long DateToTicks(Object Date) {
		Date d = (Date) Date;
		return d.getTime();
	}
	/**
	 * Utility method that converts ticks to a native Date object.
	 */
	public Object TicksToDate(long Ticks) {
		Date d = new Date(Ticks);
		return d;
	}
	/**
	 * Utility method that converts a 24 byte hex string to an object id.
	 *Can be used to find documents based on the id.
	 */
	public Object StringToObjectId(String Id) {
		return new ObjectId(Id);
	}
	

}
