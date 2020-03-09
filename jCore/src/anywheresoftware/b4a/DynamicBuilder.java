
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
 
 package anywheresoftware.b4a;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DynamicBuilder {
	@SuppressWarnings("unchecked")
	public static <T> T build(Object prev, Map<String, Object> props, boolean designer, Object tag) {
		String cls = (String) props.get("type");
		try {
			if (cls.startsWith("."))
				cls = "anywheresoftware.b4j.objects" + cls;
			Class<?> c = Class.forName(cls);
			Method m = c.getMethod("build", Object.class, HashMap.class, boolean.class, Object.class);
			return (T)m.invoke(null, prev, props, designer, tag);
//		} catch (InvocationTargetException ie) {
//			String tt = "B4A";
//			if (ie.getCause() instanceof FileNotFoundException)
//				tt = "";
//			ie.printStackTrace();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
