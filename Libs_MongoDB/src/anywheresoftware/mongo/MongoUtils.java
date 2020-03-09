
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

import org.bson.conversions.Bson;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map.MyMap;

@Hide
public class MongoUtils {
	public static List ListFromIterable(Iterable<?> it) {
		List l1 = new List();
		l1.Initialize();
		for (Object s : it) {
			l1.Add(s);
		}
		return l1;
	}
	
	public static Bson MapToBson(MyMap map) {
		if (map == null)
			return null;
		return new MapCodec.MyMapBson(map);
	}
	
}
