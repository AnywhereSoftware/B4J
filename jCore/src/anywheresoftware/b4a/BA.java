
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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.B4AException;
import anywheresoftware.b4a.objects.streams.File;

@Hide
public abstract class BA {
	public static boolean isB4J = true, isB4A = false;
	//Static fields
	public static boolean debugMode = false;
	/**
	 * Don't use without thinking about compiled libraries.
	 */
	public static boolean shellMode = false;
	public static String packageName;
	private volatile static B4AThreadPool threadPool;
	public static String debugLine;
	public static int debugLineNum;
	public final static Locale cul = Locale.US;
	public static WarningEngine warningEngine;
	public static BA firstInstance;
	public static boolean exitOnUnhandledExceptions = true;
	//instance fields
	public final Object eventsTarget;
	public HashMap<String, Method> htSubs;
	public HashMap<String, LinkedList<WaitForEvent>> waitForEvents;
	public final String className;
	public static final ThreadLocal<Object> senderHolder = new ThreadLocal<Object>();
	public Exception lastException = null;
	private static int nestLevel;
	private static boolean IDERun;
	
	static {
		Thread.setDefaultUncaughtExceptionHandler(new B4AExceptionHandler());
		IDERun = System.getProperty("b4j.ide", "false").equals("true");
		if (System.getProperty("b4j.simplelogs", "false").equals("true"))
			IDERun = false;
	}


	public BA (String packageName, String className, Object eventsTarget) {
		if (firstInstance == null) {
			firstInstance = this;
			BA.packageName = packageName;
			try {
				File.getResourceClass = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		this.eventsTarget = eventsTarget;
		this.className = className;
	}


	public boolean subExists(String sub) {
		return htSubs.containsKey(sub);
	}

	public Object raiseEvent(Object sender, String event, Object... params) {
		return raiseEvent2(sender, false, event, false, params);
	}
	public Object raiseEvent2(Object sender, boolean unused, String event, boolean throwErrorIfMissingSub, Object... params) {
		try {
			nestLevel++;
			senderHolder.set(sender);
			if (waitForEvents != null) {
				if (checkAndRunWaitForEvent(sender, event, params))
					return null;
			}
			Method m = htSubs.get(event);
			if (m != null) {
				try {
					return m.invoke(eventsTarget, params);
				} catch (IllegalArgumentException e) {
					throw new Exception("Sub " + event + " signature does not match expected signature.");
				}
			}
			else { 
				if (throwErrorIfMissingSub) {
					throw new Exception("Sub " + event + " was not found.");
				}
			}
		} catch (B4AUncaughtException e) {
			throw e;
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException)
				e = e.getCause();
			if (BA.shellMode == false) {
				try {
					if (handleUncaughtException(e))
						return null;
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
				if (nestLevel == 1) {
					printException(e, true);
					if (exitOnUnhandledExceptions) {
						Common.ExitApplication2(1);
					}
				}
			}
			throw new RuntimeException(e);
		}
		finally {
			senderHolder.set(null);
			nestLevel--;
		}
		return null;
	}
	public boolean checkAndRunWaitForEvent(Object sender, String event, Object[] params) throws Exception {
		LinkedList<WaitForEvent> events = waitForEvents.get(event);
		if (events != null) {
			Iterator<WaitForEvent> it = events.iterator();
			while (it.hasNext()) {
				WaitForEvent wfe = it.next();
				if (wfe.senderFilter == null || (sender != null && sender == wfe.senderFilter.get())) {
					it.remove();
					wfe.rs.resume(this, params);
					senderHolder.set(null);
					return true;
				}
			}
		}
		return false;
	}
	private static boolean insideHandler;
	private boolean handleUncaughtException (Throwable t) throws IllegalArgumentException, SecurityException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		if (insideHandler) {
			return false;
		}
		try {
			insideHandler = true;
			if (Common.SubExists(this, "main", "application_error") == false)
				return false;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(out);
			t.printStackTrace(pw);
			pw.close();
			byte[] b = out.toByteArray();
			B4AException exc = new B4AException();
			if (t instanceof Exception)
				exc.setObject((Exception)t);
			else
				exc.setObject(new Exception(t));

			Boolean res = (Boolean) Common.CallSubNew3(this, "main", "application_error", exc, new String(b, Charset.forName("UTF8")));
			if (Boolean.TRUE.equals(res)) {
				return false;
			}
			return true;
		} finally {
			insideHandler = false;
		}
	}

	public static String printException(Throwable e, boolean print) {
		String sub = "";
		if (BA.debugMode == false && packageName != null) {
			StackTraceElement[] stes = e.getStackTrace();
			for (StackTraceElement ste : stes) {
				if (ste.getClassName().startsWith(packageName)) {
					sub = ste.getClassName().substring(packageName.length() + 1) + "." 
							+ ste.getMethodName();
					if (debugLine != null)
						sub += " (B4A line: " + debugLineNum + ")\n" + debugLine;
					else
						sub += " (java line: " + ste.getLineNumber() + ")";
					break;
				}
			}
		}
		if (print) {
			if (sub.length() > 0)
				LogError(sub);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(out);
			e.printStackTrace(pw);
			pw.close();
			byte[] b = out.toByteArray();
			try {
				LogError(new String(b, "UTF8"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		return sub;
	}
	public abstract void cleanMessageLoop() throws InterruptedException;
	public abstract void startMessageLoop() throws InterruptedException;
	public abstract void stopMessageLoop();
	public abstract void postRunnable(Runnable runnable);
	public abstract Thread getOwnerThread();
	public void raiseEventFromUI(final Object sender, final String event, final Object... params) {

		Runnable runnable = new B4ARunnable() {
			@Override
			public void run() {
				raiseEvent2(sender, false, event, false, params);
			}
		};
		postRunnable(runnable);
	}
	@Deprecated
	public Object raiseEventFromDifferentThread(Object unused, Object unused2, final Object sender,
			final String event,
			final boolean throwErrorIfMissingSub, final Object[] params) {
		postRunnable(new Runnable() {

			@Override
			public void run() {

				raiseEvent2(sender, false, event, throwErrorIfMissingSub, params);

			}
		});
		return null;
	}

	public Object raiseEventFromDifferentThread(final Object sender,
			final Object container, final int TaskId, 
			final String event,
			final boolean throwErrorIfMissingSub, final Object[] params) {
		postRunnable(new Runnable() {

			@Override
			public void run() {
				if (container != null)
					markTaskAsFinish(container, TaskId);
				raiseEvent2(sender, false, event, throwErrorIfMissingSub, params);

			}
		});
		return null;
	}


	private static void markTaskAsFinish(Object container, int TaskId) {
		if (threadPool == null)
			return;
		threadPool.markTaskAsFinished(container, TaskId);
	}

	public String getClassNameWithoutPackage() {
		return className.substring(className.lastIndexOf(".") + 1);
	}
	public static void runAsync(final BA ba, final Object Sender, String FullEventName, 
			final Object[] errorResult, final Callable<Object[]> callable) {
		final String eventName = FullEventName.toLowerCase(BA.cul);
		BA.submitRunnable(new Runnable() {

			@Override
			public void run() {
				try {
					Object[] ret = callable.call();
					Object send = Sender;
					if (Sender instanceof ObjectWrapper)
						send = ((ObjectWrapper<?>)Sender).getObjectOrNull();
					ba.raiseEventFromDifferentThread(send, null, 0, eventName,
							false, ret);
				} catch (Exception e) {
					e.printStackTrace();
					ba.setLastException(e);
					Object send = Sender;
					if (Sender instanceof ObjectWrapper)
						send = ((ObjectWrapper<?>)Sender).getObjectOrNull();
					ba.raiseEventFromDifferentThread(send, null, 0, eventName,
							false, errorResult);
				}
			}
		}, null, 0);
	}
	public static Future<?> submitRunnable(Runnable runnable, Object container, int TaskId) {
		if (threadPool == null) {
			synchronized (BA.class) {
				if (threadPool == null) {
					threadPool = new B4AThreadPool();
				}
			}
		}
		if (container instanceof ObjectWrapper)
			container = ((ObjectWrapper<?>)container).getObject();
		threadPool.submit(runnable, container, TaskId);
		return null;
	}

	public static boolean isTaskRunning(Object container, int TaskId) {
		if (threadPool == null)
			return false;

		return threadPool.isRunning(container, TaskId);
	}
	public void loadHtSubs(Class<?> cls) {
		if (htSubs == null)
			htSubs = new HashMap<String, Method>();
		for (Method m : cls.getDeclaredMethods()) {
			if (m.getName().startsWith("_")) {
				htSubs.put(m.getName().substring(1).toLowerCase(cul), m);
			}
		}
	}
	public boolean isActivityPaused() {
		return false;
	}


	public static void addLogPrefix(String prefix, String message) {
		prefix = "~" + prefix + ":";
		if (message == null) {
			message = "(null string)";
		}
		if (message.length() < 3900 && IDERun) {
			StringBuilder sb = new StringBuilder();
			for (String line : message.split("\\n")) {
				if (line.length() > 0) {
					sb.append(prefix).append(line);
				} 
				sb.append("\n");
			}
			message = sb.toString();
		}
		Log(message);
	}

	
	public static void Log(String Message) {
		System.out.println(Message == null ? "null" : Message);
	}


	public static void LogError(String Message) {
		System.err.println(Message);
	}
	public static void LogInfo(String Message) {
		Log(Message);
	}
	public static boolean parseBoolean(String b) {
		if (b.equals("true"))
			return true;
		else if (b.equals("false"))
			return false;
		else
			throw new RuntimeException("Cannot parse: " + b + " as boolean");
	}
	public static char CharFromString(String s) {
		if (s == null || s.length() == 0)
			return '\0';
		else
			return s.charAt(0);
	}
	public Object getSender() { //take the sender object from the processBA
		return senderHolder.get();
	}
	public Exception getLastException() {
		return lastException;
	}
	/**
	 * Should be called with processBA.
	 */
	public void setLastException(Exception e) {
		while (e != null && e.getCause() != null && e instanceof Exception)
			e = (Exception) e.getCause();
		lastException = e;
	}
	//used by generated calls when method parameter is enum.
	public static <T extends Enum<T>> T getEnumFromString(Class<T> enumType, String name) {
		return Enum.valueOf(enumType, name);
	}
	public static interface B4ARunnable extends Runnable {
		//this way we can treat internal events different than other messages.
	}
	public static String NumberToString(double value) {
		String s = Double.toString(value);
		if (s.length() > 2 && s.charAt(s.length() - 2) == '.' && s.charAt(s.length() - 1) == '0')
			return s.substring(0, s.length() - 2);
		return s;
	}
	public static String NumberToString(float value) {
		return NumberToString((double)value);
	}
	public static String NumberToString(int value) {
		return String.valueOf(value);
	}
	public static String NumberToString(long value) {
		return String.valueOf(value);
	}
	public static String NumberToString(Number value) {
		if (value instanceof Double)
			return NumberToString(value.doubleValue());
		return String.valueOf(value);
	}
	public static double ObjectToNumber(Object o) {
		if (o instanceof Number) {
			return ((Number)o).doubleValue();
		}
		else {
			return Double.parseDouble(String.valueOf(o));
		}
	}
	public static long ObjectToLongNumber(Object o) {
		if (o instanceof Number) {
			return ((Number)o).longValue();
		}
		else {
			return Long.parseLong(String.valueOf(o));
		}
	}
	public static boolean ObjectToBoolean(Object o) {
		if (o instanceof Boolean)
			return ((Boolean)o).booleanValue();
		else
			return parseBoolean(String.valueOf(o));
	}
	public static char ObjectToChar(Object o) {
		if (o instanceof Character)
			return ((Character)o).charValue();
		else
			return CharFromString(o.toString());
	}
	private static int checkStackTraceEvery50;
	public static String TypeToString(Object o, boolean clazz)  {
		try {
			if (++checkStackTraceEvery50 % 50 == 0 || checkStackTraceEvery50 < 0) {
				if (Thread.currentThread().getStackTrace().length >= (checkStackTraceEvery50 < 0 ? 20 : 150)) {
					checkStackTraceEvery50 = -100; //continue checking...
					return "";					
				}
				else {
					checkStackTraceEvery50 = 0;
				}
			}
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			int i = 0;
			for (Field f : o.getClass().getDeclaredFields()) {
				String fname = f.getName();
				if (clazz) {
					if (fname.startsWith("_") == false)
						continue;
					fname = fname.substring(1);
					if (fname.startsWith("_")) //_c
						continue;
				}
				f.setAccessible(true);
				sb.append(fname).append("=")
				.append(String.valueOf(f.get(o)));
				if (++i % 3 == 0)
					sb.append("\n");
				sb.append(", ");
			}
			if (sb.length() >= 2)
				sb.setLength(sb.length() - 2);
			sb.append("]");
			return sb.toString();
		} catch (Exception e) {
			if (o != null)
				return o.getClass() + ": " + System.identityHashCode(o);
			return "N/A";
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> T gm(Map map, Object key, T defValue) {
		T o = (T)map.get(key);
		if (o == null)
			return defValue;
		return o;
	}
	public static String ObjectToString(Object o) {
		return String.valueOf(o);
	}
	public static int switchObjectToInt(Object test, Object... values) {
		int res = -1;
		if (test instanceof Number){
			double t = ((Number)test).doubleValue();
			for (int i = 0;i < values.length;i++) {
				if (t == ((Number)values[i]).doubleValue()) {
					res = i;
					break;
				}
			}
		}
		else {
			for (int i = 0;i < values.length;i++) {
				if (test.equals(values[i])) {
					res = i;
					break;
				}
			}
		}
		return res;
	}
	public static boolean fastSubCompare(String s1, String s2) {
		if (s1 == s2)
			return true;
		if (s1.length() != s2.length())
			return false;
		for (int i = 0;i < s1.length();i++) {
			if ((((int)s1.charAt(i)) & 0xDF) != (((int)s2.charAt(i)) & 0xDF))
				return false;
		}
		return true;
	}
	
	public static boolean isShellModeRuntimeCheck(BA ba) {
		return ba.getClass().getName().endsWith("ShellBA");
	}
	public interface IBridgeLog {
		void offer(String msg);
	}

	@Hide
	public static abstract class WarningEngine {
		public static final int ZERO_SIZE_PANEL = 1001;
		public static final int SAME_OBJECT_ADDED_TO_LIST = 1002;
		public static final int OBJECT_ALREADY_INITIALIZED = 1003;
		public static final int FULLSCREEN_MISMATCH = 1004;
		public static void warn(int warning) {
			if (warningEngine != null)
				warningEngine.warnImpl(warning);
		}
		public abstract void checkFullScreenInLayout(boolean fullscreen, boolean includeTitle);

		protected abstract void warnImpl(int warning);
	}
	public interface IterableList {
		int getSize();
		Object Get(int index);
	}
	public interface B4aDebuggable {
		Object[] debug(int limit, boolean[] outShouldAddReflectionFields);
	}
	public interface CheckForReinitialize {
		boolean IsInitialized();
	}
	public interface SubDelegator {
		public static final Object SubNotFound = new Object();
		Object callSub(String sub, Object Sender, Object[] args) throws Exception;

	}
	public static String ReturnString(String s) {
		return s == null ? "" : s;
	}
	public static class B4AExceptionHandler implements UncaughtExceptionHandler {
		public final Thread.UncaughtExceptionHandler original;
		public B4AExceptionHandler() {
			original = Thread.getDefaultUncaughtExceptionHandler();
		}
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			try {
				printException(e, true);
			} catch (Exception ee) {
				ee.printStackTrace();
			}

			if (original != null)
				original.uncaughtException(t, e);
		}
		
	}
	@Hide
	public static abstract class ResumableSub {
		public int state;
		public int catchState;
		public BA waitForBA;
		public boolean completed;
		public abstract void resume(BA ba, Object[] result) throws Exception;
//		protected void finalize() throws Throwable {
//			System.out.println("finalized: " + this.getClass());
//		}
	}
	@Hide
	public static class WaitForEvent {
		public ResumableSub rs;
		public WeakReference<Object> senderFilter;
		public WaitForEvent(ResumableSub rs, Object senderFilter) {
			this.rs = rs;
			if (senderFilter == null)
				this.senderFilter = null;
			else
				this.senderFilter = new WeakReference<Object>(senderFilter);
		}
		public boolean noFilter() {
			return senderFilter == null;
		}
		public boolean cleared() {
			return senderFilter != null && senderFilter.get() == null;
		}
//		public String toString() {
//			return "WaitForEvent: " + (senderFilter == null ? "no filter" : senderFilter.get());
//		}
	}
	
	@Hide
	public static @interface Hide {}
	@Hide
	public static @interface Pixel {}
	@Retention(RetentionPolicy.RUNTIME)
	@Hide
	public static @interface ShortName {
		String value();
	}
	/**
	 * Used by String2. Should not be used normally.
	 */
	@Hide
	public static @interface DesignerName {
		String value();
	}
	/**
	 * Should only be applied to classes or to Object types.
	 */
	@Hide
	public static @interface ActivityObject {}
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	@Hide
	public static @interface Events {
		String[] values();
	}
	@Hide
	@Retention(RetentionPolicy.SOURCE)
	public static @interface Property {
		String key();
		String displayName();
		String description() default "";
		String defaultValue();
		String fieldType();
		String minRange() default "";
		String maxRange() default "";
		String list() default "";
	}
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	@Hide
	public static @interface DesignerProperties {
		Property[] values();
	}
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	@Hide
	public static @interface DependsOn {
		String[] values();
	}
	@Target(ElementType.METHOD)
	@Hide
	public static @interface RaisesSynchronousEvents {}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	@Hide
	public static @interface DontInheritEvents {
	}
	@Retention(RetentionPolicy.SOURCE)
	@Hide
	public static @interface Permissions {
		String[] values();
	}
	@Retention(RetentionPolicy.RUNTIME)
	@Hide
	public static @interface Version {
		float value();
	}
	@Retention(RetentionPolicy.RUNTIME)
	@Hide
	public static @interface Author {
		String value();
	}
	@Hide
	@Retention(RetentionPolicy.SOURCE)
	public static @interface CustomClass {
		String name();
		String fileNameWithoutExtension();
		int priority() default 0;
	}
	@Hide
	@Retention(RetentionPolicy.SOURCE)
	public static @interface CustomClasses {
		CustomClass[] values();
	}

}
