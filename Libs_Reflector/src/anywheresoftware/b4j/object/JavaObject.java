
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
 
 package anywheresoftware.b4j.object;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

@ShortName("JavaObject")
@Events(values={"Event (MethodName As String, Args() As Object) As Object"})
@Version(2.05f)
public class JavaObject extends AbsObjectWrapper<Object>{
	private static final FieldCache fieldCache = new FieldCache();
	private static final MethodCache methodCache = new MethodCache();
	private static final HashMap<String, Class<?>> primitives = new HashMap<String, Class<?>>();
	private static final HashMap<Class<?>, Class<?>> primitiveToBoxed = new HashMap<Class<?>, Class<?>>();
	static {

		primitiveToBoxed.put(byte.class, Byte.class);
		primitiveToBoxed.put(char.class, Character.class);
		primitiveToBoxed.put(short.class, Short.class);
		primitiveToBoxed.put(int.class, Integer.class);
		primitiveToBoxed.put(long.class, Long.class);
		primitiveToBoxed.put(float.class, Float.class);
		primitiveToBoxed.put(double.class, Double.class);
		primitiveToBoxed.put(boolean.class, Boolean.class);
		primitives.put("byte", byte.class);
		primitives.put("char", char.class);
		primitives.put("short", short.class);
		primitives.put("int", int.class);
		primitives.put("long", long.class);
		primitives.put("float", float.class);
		primitives.put("double", double.class);
		primitives.put("boolean", boolean.class);
	}
	private static Field context;
	/**
	 * <b>B4A only method.</b>
	 *Initializes the object with the current context (current Activity or Service).
	 */
	public JavaObject InitializeContext(BA ba) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		Object shared = BA.class.getDeclaredField("sharedProcessBA").get(ba);
		@SuppressWarnings("unchecked")
		WeakReference<BA> activityBA = (WeakReference<BA>) shared.getClass().getDeclaredField("activityBA").get(shared);
		if (activityBA != null) {
			BA aba = activityBA.get();
			if (aba != null)
				ba = aba;
		}
		if (context == null)
			context = BA.class.getDeclaredField("context");
		setObject(context.get(ba));
		return this;
	}
	/**
	 * Initializes the object. The object will wrap the given class (for static access).
	 *ClassName - The full class name.
	 */
	public JavaObject InitializeStatic(String ClassName) throws ClassNotFoundException {
		setObject(getCorrectClassName(ClassName));
		return this;
	}
	/**
	 * Creates a new instance of the given class.
	 *ClassName - The full class name.
	 *Params - An array of objects to pass to the constructor (or Null).
	 */
	public JavaObject InitializeNewInstance(String ClassName, Object[] Params) throws Exception {
		Class<?> cls = getCorrectClassName(ClassName);
		if (Params == null || Params.length == 0) {
			setObject(cls.newInstance());
		} else {
			for (Constructor<?> c : cls.getConstructors()) {
				Class<?>[] mTypes = c.getParameterTypes();
				if (arrangeAndCheckMatch(mTypes, Params)) {
					setObject(c.newInstance(Params));
					return this;
				}
			}
			throw new RuntimeException("Constructor not found.");
		}
		return this;
	}
	/**
	 * Creates an array with the given class and values.
	 */
	public JavaObject InitializeArray(String ClassName, Object[] Values) throws ClassNotFoundException {
		Class<?> c = primitives.get(ClassName);
		boolean primitive = c != null;
		if (!primitive)
			c = getCorrectClassName(ClassName);
		Object arr = Array.newInstance(c, Values.length);
		for (int i = 0;i < Values.length;i++)
			Array.set(arr, i, Values[i]);
		setObject(arr);
		return this;
	}
	/**
	 * Runs the given method and returns the method return value.
	 *MethodName - The case-sensitive method name.
	 *Params - Method paramters (or Null).
	 */
	public Object RunMethod(String MethodName, Object[] Params) throws Exception{
		Class<?> cls = getCurrentClass();
		List<Method> mm = methodCache.getMethod(cls.getName(), MethodName, Params);
		Method method = null;
		for (Method m : mm) {
			Class<?>[] mTypes = m.getParameterTypes();
			if (arrangeAndCheckMatch(mTypes, Params)) {
				method = m;
				break;
			}
		}
		if (method == null)
			throw new RuntimeException("Method: " + MethodName + " not matched.");
		//method.setAccessible(true);
		return method.invoke(getObject(), Params);

	}
	/**
	 * Similar to RunMethod. Returns a JavaObject instead of Object.
	 */
	public JavaObject RunMethodJO(String MethodName, Object[] Params) throws Exception {
		return (JavaObject)AbsObjectWrapper.ConvertToWrapper(new JavaObject(), RunMethod(MethodName, Params));
	}

	@SuppressWarnings("unchecked")
	private boolean arrangeAndCheckMatch(Class<?>[] mTypes, Object[] Params) {
		int i;
		if (Params == null)
			return mTypes.length == 0;
		if (Params.length != mTypes.length)
			return false;
		for (i = 0;i < Params.length;i++) {
			if (Params[i] == null)
				continue;
			Class<?> p = Params[i].getClass();
			if (mTypes[i].isPrimitive())
				mTypes[i] = primitiveToBoxed.get(mTypes[i]);
			if (mTypes[i].isEnum() && p == String.class) {
				//small bug here. The parameter is changed even if there is no match...
				Params[i] = Enum.valueOf((Class<Enum>)mTypes[i], (String)Params[i]);
				continue;
			}
			if (mTypes[i].isAssignableFrom(p) == false) {
				break;
			}
		}
		if (i == Params.length)
			return true;
		return false;
	}

	/**
	 * Sets the value of the given field.
	 */
	public void SetField(String FieldName, Object Value) throws Exception {
		Class<?> cls = getCurrentClass();
		Field field = fieldCache.getField(cls.getName(), FieldName);
		field.set(getObject(), Value);
	}
	/**
	 * Gets the value of the given field.
	 */
	public Object GetField(String Field) throws Exception {
		Class<?> cls = getCurrentClass();
		Field field = fieldCache.getField(cls.getName(), Field);
		return field.get(getObject());
	}
	/**
	 * Similar to GetField. Returns a JavaObject instead of Object.
	 */
	public JavaObject GetFieldJO(String Field) throws Exception {
		return (JavaObject)AbsObjectWrapper.ConvertToWrapper(new JavaObject(), GetField(Field));
	}
	/**
	 * Creates an instance of the interface and binds it to the object.
	 *Interface - The full interface name.
	 *EventName - The prefix of the event sub.
	 *DefaultReturnValue - This value will be returned if no value was returned from the event sub. This can happen if the Activity is paused for example.
	 *
	 *For example:<code>
	 *Sub Activity_Create(FirstTime As Boolean)
	 *   Dim b As Button
	 *   b.Initialize("")
	 *   Activity.AddView(b, 0, 0, 200dip, 200dip)
	 *   Dim jo As JavaObject = b
	 *   Dim e As Object = jo.CreateEvent("android.view.View.OnTouchListener", "btouch", False)
	 *   jo.RunMethod("setOnTouchListener", Array As Object(e))
	 *End Sub
	 *
	 *Sub btouch_Event (MethodName As String, Args() As Object) As Object
	 *   Dim motion As JavaObject = Args(1) 'args(0) is View
	 *   Dim x As Float = motion.RunMethod("getX", Null)
	 *   Dim y As Float = motion.RunMethod("getY", Null)
	 *   Log(x & ", " & y)
	 *   Return True
	 *End Sub</code>
	 */
	public Object CreateEvent(final BA ba, String Interface, final String EventName, Object DefaultReturnValue) throws Exception {
		return createEvent(ba, Interface, EventName, false, DefaultReturnValue);
	}
	/**
	 * Similar to CreateEvent. The event will be sent to the message queue and then be processed (similar to CallSubDelayed).
	 */
	public Object CreateEventFromUI(final BA ba, String Interface, final String EventName,
			Object ReturnValue) throws Exception {
		return createEvent(ba, Interface, EventName, true, ReturnValue);
	}
	private Object createEvent(final BA ba, String Interface, final String EventName, 
			final boolean fromUi, final Object returnValue) 
					throws Exception {
		final Object obj = getObject();
		InvocationHandler handler = new InvocationHandler() {
			String eventName = EventName.toLowerCase(BA.cul) + "_event";
			Thread t = Thread.currentThread();
			@Override
			public Object invoke(Object arg0, Method arg1, Object[] arg2)
					throws Throwable {
				Object[] params = new Object[] {arg1.getName(), arg2};
				if (Thread.currentThread() == t) {
					if (!fromUi) {
						Object ret = ba.raiseEvent(obj, eventName, params);
						return ret == null ? returnValue : ret;
					}
					else {
						ba.raiseEventFromUI(obj, eventName, params);
						return returnValue;
					}
				}
				else {
					ba.raiseEventFromDifferentThread(obj, null, 0, eventName, false, params);
					return returnValue;
				}
			}

		};
		Class<?> inter = getCorrectClassName(Interface);
		return Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class[] {inter  },
				handler);
	}
	private Class<?> getCurrentClass() {
		if (getObject() instanceof Class<?>) {
			return (Class<?>) getObject();
		}
		else {
			return getObject().getClass();
		}
	}
	private static Class<?> getCorrectClassName(String className) throws ClassNotFoundException {
		if (className.equals("Object"))
			return Object.class;
		else if (className.equals("String"))
			return String.class;
		Class<?> cls = null;
		for (int i = 0;i < 3;i++) {
			try {
				cls = Class.forName(className);
				return cls;
			} catch (ClassNotFoundException c) {
				int dot = className.lastIndexOf(".");
				if (dot == -1) {
					if (i == 0) {
						className = "java.lang." + className; //Short, Integer,...
						continue;
					}
					else
						throw c;
				}
				className = className.substring(0, dot) + "$" + className.substring(dot + 1);
			}
		}
		throw new ClassNotFoundException(className);

	}

	static class FieldCache {
		private ConcurrentHashMap<String, HashMap<String, Field>> cache = new ConcurrentHashMap<String, HashMap<String, Field>>();

		public Field getField(String className, String fieldName) throws Exception{
			HashMap<String, Field> classFields = cache.get(className);
			if (classFields == null) {
				classFields = new HashMap<String, Field>();
				for (Field m : Class.forName(className).getFields()) {
					classFields.put(m.getName(), m);
				}
				cache.put(className, classFields);
			}
			Field m = classFields.get(fieldName);
			if (m == null)
				throw new RuntimeException("Field: " + fieldName + " not found in: " + className);
			return m;
		}
	}
	static class MethodCache {
		private ConcurrentHashMap<String, HashMap<String, ArrayList<Method>>> cache = new ConcurrentHashMap<String, HashMap<String, ArrayList<Method>>>();
		private static final HashMap<String, ArrayList<Method>> cantGetAllMethods = new HashMap<String, ArrayList<Method>>();
		/**
		 * Should not be called directly (except of Shell.getMethod).
		 */
		public List<Method> getMethod(String className, String methodName, Object[] params) throws Exception{
			HashMap<String, ArrayList<Method>> classMethods = cache.get(className);
			if (classMethods == null) {
				classMethods = new HashMap<String, ArrayList<Method>>();
				Class<?> cls = Class.forName(className);
				if (BA.isB4J && (cls.getModifiers() & Modifier.PUBLIC) == 0) {
					fillNonPublicB4JMethods(className, classMethods, cls);
				} else {
					Method[] methods = null;
					try {
						methods = cls.getMethods();
					}
					catch (Throwable e) {
						//this will happen when there is a method from a newer API.
						BA.LogError("Cannot get methods of class: " + className + ", disabling cache.");
						classMethods = cantGetAllMethods;
					}
					fillMethods(methods, classMethods);
				}
				cache.put(className, classMethods);
			}
			if (classMethods == cantGetAllMethods) {
				Class<?> cls = Class.forName(className);
				Class<?>[] paramTypes = new Class<?>[params.length];
				for (int i = 0;i < params.length;i++) {
					paramTypes[i] = params[i] == null ? Object.class : params[i].getClass();
				}
				for (int i = 0;i < params.length;i++) {
					try {
						return Arrays.asList(cls.getMethod(methodName, paramTypes));
					} catch (NoSuchMethodException nsme) {
						Class<?> orig = paramTypes[i];
						Class<?> parent = paramTypes[i].getSuperclass();
						if (parent != null) {
							paramTypes[i] = parent;
							try {
								return Arrays.asList(cls.getMethod(methodName, paramTypes));
							} catch (NoSuchMethodException nsme2) {
								paramTypes[i] = orig;
							}
						}
					}
				}
			}
			ArrayList<Method> m = classMethods.get(methodName);
			if (m == null)
				throw new RuntimeException("Method: " + methodName + " not found in: " + className);
			return m;
		}
		private void fillMethods(Method[] methods, HashMap<String, ArrayList<Method>> classMethods) {
			if (methods == null)
				return;
			for (Method m : methods) {
				ArrayList<Method> overloaded = classMethods.get(m.getName());
				if (overloaded == null) {
					overloaded = new ArrayList<Method>();
					classMethods.put(m.getName(), overloaded);
				}
				overloaded.add(m);
			}
		}
		private void fillNonPublicB4JMethods(String className, HashMap<String, ArrayList<Method>> classMethods, Class<?> cls) {
			for (Class<?> interf : cls.getInterfaces())
				fillMethods(interf.getMethods(), classMethods);
			cls = cls.getSuperclass();
			while (cls != null) {
				if ((cls.getModifiers() & Modifier.PUBLIC) != 0) {
					fillMethods(cls.getMethods(), classMethods);
					return;
				}
				cls = cls.getSuperclass();
			}
		}
	}
}
