
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
 
 package anywheresoftware.b4a.keywords;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.B4AClass;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CustomClasses;
import anywheresoftware.b4a.BA.CustomClass;
import anywheresoftware.b4a.BA.DesignerName;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ResumableSub;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.SubDelegator;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.BA.WaitForEvent;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.StandardBA;
import anywheresoftware.b4a.objects.B4AException;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.streams.File;

@CustomClasses(values = {
		@CustomClass(name = "Standard Class", fileNameWithoutExtension = "standard", priority = 900),
})
/**
 * These are the internal keywords.
 */
@Version(9.50f)
public class Common {

	@Hide
	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		if (System.getProperty("javafx.autoproxy.disable") == null)
			System.setProperty("javafx.autoproxy.disable", "true");
		Class.forName("com.javafx.main.Main").getMethod("main", String[].class).invoke(null,(Object) args);
	}
	public static final boolean True = true;
	public static final boolean False = false;
	public static final Object Null = null;
	private static java.text.NumberFormat numberFormat, numberFormat2;
	/**
	 * New line character. The value of Chr(10).
	 */
	public static final String CRLF = "\n";
	/**
	 * Tab character.
	 */
	public static final String TAB = "\t";
	/**
	 * Quote character. The value of Chr(34).
	 */
	public static final String QUOTE = "\"";
	/**
	 * PI constant.
	 */
	public static final double cPI = Math.PI;
	/**
	 * e (natural logarithm base) constant.
	 */
	public static final double cE = Math.E;
	/**
	 * Returns the monitor scale, which is DPI / 96.
	 *(DPI stands for dots per inch).
	 */
	public static float Density = 1f;
	/**
	 * Files related methods.
	 */
	public static final File File = null;
	/**
	 * Bitwise related methods.
	 */
	public static final Bit Bit = null;
	/**
	 * Date and time related methods.
	 */
	public static final DateTime DateTime = null;
	/**
	 * Regular expressions related methods.
	 */
	public static final Regex Regex = null;
	private static Random random;
	/**
	 * Converts the specified number to a string. 
	 *The string will include at least Minimum Integers and at most Maximum Fractions digits.
	 *Example:<code>
	 *Log(NumberFormat(12345.6789, 0, 2)) '"12,345.68"
	 *Log(NumberFormat(1, 3 ,0)) '"001"</code>
	 */
	public static synchronized String NumberFormat(double Number, int MinimumIntegers, int MaximumFractions) {
		if (numberFormat == null)
			numberFormat = java.text.NumberFormat.getInstance(Locale.US);
		numberFormat.setMaximumFractionDigits(MaximumFractions);
		numberFormat.setMinimumIntegerDigits(MinimumIntegers);
		return numberFormat.format(Number);
	}
	/**
	 * Converts the specified number to a string. 
	 *The string will include at least Minimum Integers, at most Maximum Fractions digits and at least Minimum Fractions digits.
	 *GroupingUsed - Determines whether to group every three integers.
	 *Example:<code>
	 *Log(NumberFormat2(12345.67, 0, 3, 3, false)) '"12345.670"</code>
	 */
	public static synchronized String NumberFormat2(double Number, int MinimumIntegers, int MaximumFractions, int MinimumFractions,
			boolean GroupingUsed) {
		if (numberFormat2 == null)
			numberFormat2 = java.text.NumberFormat.getInstance(Locale.US);
		numberFormat2.setMaximumFractionDigits(MaximumFractions);
		numberFormat2.setMinimumIntegerDigits(MinimumIntegers);
		numberFormat2.setMinimumFractionDigits(MinimumFractions);
		numberFormat2.setGroupingUsed(GroupingUsed);
		return numberFormat2.format(Number);
	}
	/**
	 * Logs a message to StdOut stream.
	 */
	public static void Log(String Message) {
		BA.Log(Message);
	}
	/**
	 * Logs a message to StdErr stream.
	 */
	public static void LogError (String Message) {
		BA.LogError(Message);
	}
	/**
	 * Logs a message. The message will only be logged in Debug mode.
	 */
	public static void LogDebug(String Message) {
		if (BA.debugMode)
			BA.Log(Message);
	}
	/**
	 * Logs a message. The message will be displayed in the IDE with the specified color.
	 */
	public static void LogColor(String Message, int Color) {
	}
	@Hide
	public static void LogImpl(String line, String Message, int Color) {
		String prefix = Color == 0 ? "l0" + line : "L0" + line + "~" + Color;
		BA.addLogPrefix(prefix, Message);
	}
	/**
	 * Calling this method causes the thread to start managing the message queue.
	 *This method should only be called in non-UI applications.
	 */

	public static void StartMessageLoop(BA ba) throws InterruptedException {
		ba.startMessageLoop();
		if (BA.isShellModeRuntimeCheck(ba))
			ba.raiseEvent(null, "STOP_MESSAGE_LOOP");

	}
	/**
	 * Calling this method will cause the thread to stop managing the message queue.
	 *This method should only be called in non-UI applications.
	 */
	public static void StopMessageLoop(final BA ba) {
		ba.stopMessageLoop();
	}
	/**
	 *Returns the object that raised the event.
	 *Only valid while inside the event sub.
	 *Example:<code>
	 *Sub Button_Click
	 * Dim b As Button
	 * b = Sender
	 * b.Text = "I've been clicked"
	 *End Sub</code>
	 */
	public static Object Sender(BA ba) {
		return ba.getSender();
	}
	/**
	 * Inverts the value of the given boolean.
	 */
	public static boolean Not(boolean Value) {
		return !Value;
	}
	/**
	 * Sets the random seed value. 
	 *This method can be used for debugging as it allows you to get the same results each time.
	 */
	public synchronized static void RndSeed(long Seed) {
		if (random == null)
			random = new Random(Seed);
		else
			random.setSeed(Seed);
	}
	/**
	 * Returns a random integer between Min (inclusive) and Max (exclusive).
	 */
	public synchronized static int Rnd(int Min, int Max)
	{
		if (random == null)
			random = new Random();
		return Min + random.nextInt(Max - Min);
	}
	/**
	 * Returns the absolute value.
	 */
	public static double Abs(double Number) {
		return Math.abs(Number);
	}
	@Hide
	public static int Abs(int Number) {
		return Math.abs(Number);
	}
	/**
	 * Returns the larger number between the two numbers.
	 */
	public static double Max(double Number1, double Number2) {
		return Math.max(Number1, Number2);
	}
	@Hide
	public static double Max(int Number1, int Number2) {
		return Math.max(Number1, Number2);
	}
	/**
	 * Returns the smaller number between the two numbers.
	 */
	public static double Min(double Number1, double Number2) {
		return Math.min(Number1, Number2);
	}
	@Hide
	public static double Min(int Number1, int Number2) {
		return Math.min(Number1, Number2);
	}
	/**
	 * Calculates the trigonometric sine function. Angle measured in radians.
	 */
	public static double Sin(double Radians) {
		return Math.sin(Radians);
	}
	/**
	 * Calculates the trigonometric sine function. Angle measured in degrees.
	 */
	public static double SinD(double Degrees) {
		return Math.sin(Degrees / 180 * Math.PI);
	}
	/**
	 * Calculates the trigonometric cosine function. Angle measured in radians.
	 */
	public static double Cos(double Radians) {
		return Math.cos(Radians);
	}
	/**
	 * Calculates the trigonometric cosine function. Angle measured in degrees.
	 */
	public static double CosD(double Degrees) {
		return Math.cos(Degrees / 180 * Math.PI);
	}
	/**
	 * Calculates the trigonometric tangent function. Angle measured in radians.
	 */
	public static double Tan(double Radians) {
		return Math.tan(Radians);
	}
	/**
	 * Calculates the trigonometric tangent function. Angle measured in degrees.
	 */
	public static double TanD(double Degrees) {
		return Math.tan(Degrees / 180 * Math.PI);
	}
	/**
	 * Returns the Base value raised to the Exponent power.
	 */
	public static double Power(double Base, double Exponent) {
		return Math.pow(Base, Exponent);
	}
	/**
	 * Returns the positive square root.
	 */
	public static double Sqrt(double Value) {
		return Math.sqrt(Value);
	}
	/**
	 * Returns the angle measured with radians.
	 */
	public static double ASin(double Value) {
		return Math.asin(Value);
	}
	/**
	 * Returns the angle measured with degrees.
	 */
	public static double ASinD(double Value) {
		return Math.asin(Value) / Math.PI * 180;
	}
	/**
	 * Returns the angle measured with radians.
	 */
	public static double ACos(double Value) {
		return Math.acos(Value);
	}
	/**
	 * Returns the angle measured with degrees.
	 */
	public static double ACosD(double Value) {
		return Math.acos(Value) / Math.PI * 180;
	}
	/**
	 * Returns the angle measured with radians.
	 */
	public static double ATan(double Value) {
		return Math.atan(Value);
	}
	/**
	 * Returns the angle measured with degrees.
	 */
	public static double ATanD(double Value) {
		return Math.atan(Value) / Math.PI * 180;
	}
	/**
	 * Returns the angle measured with radians.
	 */
	public static double ATan2(double Y, double X) {
		return Math.atan2(Y, X);
	}
	/**
	 * Returns the angle measured with degrees.
	 */
	public static double ATan2D(double Y, double X) {
		return Math.atan2(Y, X) / Math.PI * 180;
	}

	public static double Logarithm(double Number, double Base) {
		return Math.log(Number) / Math.log(Base);
	}
	/**
	 * Returns the long number closest to the given number. 
	 */
	public static long Round(double Number) {
		return Math.round(Number);
	}
	/**
	 * Rounds the given number and leaves up to the specified number of fractional digits.
	 */
	public static double Round2(double Number, int DecimalPlaces) {
		double shift = Math.pow(10, DecimalPlaces);
		return Math.round(Number * shift) / shift;
	}
	/**
	 * Returns the largest double that is smaller or equal to the specified number and is equal to an integer.
	 */
	public static double Floor(double Number) {
		return Math.floor(Number);
	}
	/**
	 * Returns the smallest double that is greater or equal to the specified number and is equal to an integer.
	 */
	public static double Ceil(double Number) {
		return Math.ceil(Number);
	}
	/**
	 * Returns the unicode code point of the given character or first character in string.
	 */
	public static int Asc(char Char) {
		return (int) Char;
	}
	/**
	 * Returns the character that is represented by the given unicode value.
	 */
	public static char Chr(int UnicodeValue) {
		return (char)UnicodeValue;
	}
	/**
	 * Returns a string representing the object's java type.
	 */
	public static String GetType(Object object) {
		return object.getClass().getName();
	}
	/**
	 * Returns true if ToolName equals B4J.
	 */
	public static boolean IsDevTool(String ToolName) {
		return ToolName.toLowerCase(BA.cul).equals("b4j");
	}

	/**
	 * Scales the value, which represents a specific length on a default density device (Density = 1.0),
	 *to the current device.
	 *For example, the following code will set the width value of this button to be the same physical size
	 *on all devices.
	 *Button1.Width = DipToCurrent(100)
	 *
	 *Note that a shorthand syntax for this method is available. Any number followed by the string 'dip'
	 *will be converted in the same manner (no spaces are allowed between the number and 'dip').
	 *So the previous code is equivalent to:
	 *Button1.Width = 100dip 'dip -> density independent pixel
	 */
	public static int DipToCurrent(int Length)
	{
		return (int)(Density * Length);
	}
	@Hide
	public static void setDensity(double dpi) {
		float standard = 96;
		if (System.getProperty("os.name", "").toLowerCase(BA.cul).contains("mac"))
			standard = 72;
		//Density = (float) (dpi / standard);
		Density = 1.0f;
	}

	/**
	 * Tests whether the specified string can be safely parsed as a number.
	 */
	public static boolean IsNumber(String Text){
		try {
			Double.parseDouble(Text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	/**
	 * Returns the last exception that was caught (if such exists).
	 */
	public static B4AException LastException(BA ba) {
		B4AException e = new B4AException();
		e.setObject(ba.getLastException());
		return e;
	}
	/**
	 * Dynamically calls a sub based on the sub name.
	 *Note that unlike Basic4android CallSub is not required unless you need to call a sub based on the sub name.
	 */
	@DesignerName("CallSub")
	@RaisesSynchronousEvents
	public static Object CallSubNew(BA mine, Object Component, String Sub) {
		return CallSub4(mine, Component, Sub, null);
	}
	/**
	 * Similar to CallSub. Calls a sub with a single argument.
	 */
	@DesignerName("CallSub2")
	@RaisesSynchronousEvents
	public static Object CallSubNew2(BA mine, Object Component, String Sub, Object Argument) {
		return CallSub4(mine, Component, Sub, new Object[] {Argument});
	}
	/**
	 * Similar to CallSub. Calls a sub with two arguments.
	 */
	@DesignerName("CallSub3")
	@RaisesSynchronousEvents
	public static Object CallSubNew3(BA mine, Object Component, String Sub, Object Argument1, Object Argument2) {
		return CallSub4(mine, Component, Sub, new Object[] {Argument1, Argument2});
	}
	@Hide
	public static Object CallSubDebug(BA mine, Object Component, String Sub) throws Exception {
		return Class.forName("anywheresoftware.b4a.debug.Debug").getDeclaredMethod("CallSubNew", BA.class, Object.class, String.class)
				.invoke(null, mine, Component, Sub);
	}
	@Hide
	public static Object CallSubDebug2(BA mine, Object Component, String Sub, Object Argument) throws Exception {
		return Class.forName("anywheresoftware.b4a.debug.Debug").getDeclaredMethod("CallSubNew2", BA.class, Object.class, String.class, Object.class)
				.invoke(null, mine, Component, Sub, Argument);
	}
	@Hide
	public static Object CallSubDebug3(BA mine, Object Component, String Sub, Object Argument1, Object Argument2) throws Exception {
		return Class.forName("anywheresoftware.b4a.debug.Debug").getDeclaredMethod("CallSubNew3", BA.class, Object.class, String.class, Object.class, Object.class)
				.invoke(null, mine, Component, Sub, Argument1, Argument2);
	}
	private static Object CallSub4(BA mine, Object Component, String Sub, Object[] Arguments) {
		try {
			Object o = null;
			if (Component instanceof SubDelegator) {
				o = ((SubDelegator)Component).callSub(Sub, mine.eventsTarget, Arguments);
				if (o != SubDelegator.SubNotFound) {
					//we got result
					if (o != null && o instanceof ObjectWrapper) {
						return ((ObjectWrapper<?>)o).getObject();
					}
					return o;
				}
				else {
					o = null;
				}
			}
			BA ba = getComponentBA(mine, Component);
			if (ba != null) {
				boolean isTargetClass = Component instanceof B4AClass;
				//for classes we allow even when the context is paused (it is less critical).
				o = ba.raiseEvent2(mine.eventsTarget, isTargetClass /*allow during pause*/ ,
						Sub.toLowerCase(BA.cul), isTargetClass /*raiseEvent*/, Arguments);
			}

			if (o != null && o instanceof ObjectWrapper) {
				return ((ObjectWrapper<?>)o).getObject();
			}
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Dynamically calls a sub. The sub will be called after the current code execution completes.
	 */
	public static void CallSubDelayed(BA mine, Object Component, String Sub) {
		CallSubDelayed4(mine, Component, Sub, null);
	}
	/**
	 * Similar to CallSubDelayed. Calls a sub with a single argument.
	 */
	public static void CallSubDelayed2(BA mine, Object Component, String Sub, Object Argument) {
		CallSubDelayed4(mine, Component, Sub, new Object[] {Argument});
	}
	/**
	 * Similar to CallSubDelayed. Calls a sub with two arguments.
	 */
	public static void CallSubDelayed3(BA mine, Object Component, String Sub, Object Argument1, Object Argument2) {
		CallSubDelayed4(mine, Component, Sub, new Object[] {Argument1, Argument2});
	}
	private static void CallSubDelayed4(final BA mine, final Object Component, final String Sub,
			final Object[] Arguments) {
		BA ba;
		try {
			ba = getComponentBA(mine, Component);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ba.postRunnable(new CallSubDelayedHelper(mine, Component, Sub, Arguments));
	}
	private static class CallSubDelayedHelper implements Runnable {
		private BA mine;
		private Object Component;
		private String Sub;
		private Object[] Arguments;
		public CallSubDelayedHelper(BA mine, Object Component, final String Sub,
				final Object[] Arguments) {
			this.mine = mine;
			this.Component = Component;
			this.Sub = Sub;
			this.Arguments = Arguments;
		}
		@Override
		public void run() {
			CallSub4(mine, Component, Sub, Arguments);
		}
	}
	@Hide
	public static BA getComponentBA(BA mine, Object Component) throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		Class<?> c;
		if (Component instanceof Class<?>)
			c = (Class<?>) Component;
		else if (Component instanceof B4AClass) {
			return ((B4AClass)Component).getBA();
		}
		else if (Component == null || Component.toString().length() == 0)
			return mine;
		else {
			c = Class.forName(BA.packageName + "." + ((String)Component).toLowerCase(BA.cul));
		}
		return (BA) c.getField("ba").get(null);
	}

	/**
	 * Tests whether the object includes the specified method.
	 *Returns false if the object was not initialized or not an instance of a user class.
	 */
	public static boolean SubExists(BA mine, Object Object, String Sub) throws IllegalArgumentException, SecurityException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		if (Object == null)
			return false;
		BA ba = getComponentBA(mine, Object);
		if (ba == null)
			return false;
		return ba.subExists(Sub.toLowerCase(BA.cul));
	}


	/**
	 * Creates a new String by copying the characters from the array.
	 *Copying starts from StartOffset and the number of characters copied equals to Length.
	 */
	public static String CharsToString(char[] Chars, int StartOffset, int Length) {
		return new String(Chars, StartOffset, Length);
	}
	/**
	 * Decodes the given bytes array as a string.
	 *Data - The bytes array.
	 *StartOffset - The first byte to read.
	 *Length - Number of bytes to read.
	 *CharSet - The name of the character set.
	 *Example:<code>
	 *Dim s As String
	 *s = BytesToString(Buffer, 0, Buffer.Length, "UTF-8")</code>
	 */
	public static String BytesToString(byte[] Data, int StartOffset, int Length, String CharSet) throws UnsupportedEncodingException {
		return new String(Data, StartOffset, Length, CharSet);
	}
	/**
	 * Gets the value of the system property mapped to the given key.
	 *Returns the DefaultValue parameter if there is no such property.
	 */
	public static String GetSystemProperty(String Key, String DefaultValue) {
		return System.getProperty(Key, DefaultValue);
	}
	/**
	 * Sets the system property indicated by the given key.
	 */
	public static void SetSystemProperty(String Key, String Value) {
		System.setProperty(Key, Value);
	}
	/**
	 * Returns the value of the environment variable mapped to the given key.
	 *Returns the DefaultValue parameter if there is no such variable.
	 */
	public static String GetEnvironmentVariable(String Key, String DefaultValue) {
		String r = System.getenv(Key);
		return r == null ? DefaultValue : r;
	}
	@Hide
	public static Map createMap(Object[] data) {
		Map m = new Map();
		m.Initialize();
		for (int i = 0;i < data.length;i+=2) {
			m.Put(data[i], data[i + 1]);
		}
		return m;
	}
	@Hide
	public static List ArrayToList(Object[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		list.setObject(Arrays.asList(Array));
		return list;
	}
	@Hide
	public static List ArrayToList(int[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Integer.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(long[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Long.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(float[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Float.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(double[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Double.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(boolean[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Boolean.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(short[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Short.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}
	@Hide
	public static List ArrayToList(byte[] Array) {
		anywheresoftware.b4a.objects.collections.List list = new List();
		Object[] o = new Object[Array.length];
		for (int i = 0;i < Array.length;i++) {
			o[i] = Byte.valueOf(Array[i]);
		}
		list.setObject(Arrays.asList(o));
		return list;
	}

	public static String SmartStringFormatter(String Format, Object Value) {
		//format lower cased
		if (Format.length() == 0)
			return BA.ObjectToString(Value);
		if (Format.equals("date"))
			return DateTime.Date(BA.ObjectToLongNumber(Value));
		else if (Format.equals("datetime")) {
			long l = BA.ObjectToLongNumber(Value);
			return DateTime.Date(l) + " " + DateTime.Time(l);
		}
		else if (Format.equals("time"))
			return DateTime.Time(BA.ObjectToLongNumber(Value));
		else if (Format.equals("xml")) {
			StringBuilder sb = new StringBuilder();
			String s = String.valueOf(Value);
			for (int i = 0;i < s.length();i++) {
				char c = s.charAt(i);
				switch (c) {
				case '\"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&#39;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				default:
					sb.append(c);
					break;
				}
			}
			return sb.toString();
		}
		else {
			int i = Format.indexOf(".");
			int minInts, maxFracs;
			if (i > -1) {
				minInts = Integer.parseInt(Format.substring(0, i));
				maxFracs = Integer.parseInt(Format.substring(i + 1));
			}
			else {
				minInts = Integer.parseInt(Format);
				maxFracs = Integer.MAX_VALUE;
			}
			try {
				return NumberFormat(BA.ObjectToNumber(Value), minInts, maxFracs);
			} catch (Exception e) {
				return "NaN";
			}
		}
	}


	/**
	 * Creates a single dimension array of the specified type, or Object if the type is not specified.
	 *The syntax is: Array As type (list of values).
	 *Example:<code>
	 *Dim Days() As String = Array As String("Sunday", "Monday", ...)</code>
	 */
	public static void Array() {

	}
	/**
	 * Creates a Map with the given key / value pairs.
	 *The syntax is: CreateMap (key1: value1, key2: value2, ...)
	 *Example: <code>
	 *Dim m As Map = CreateMap("January": 1, "February": 2)</code>
	 */
	public static void CreateMap() {

	}
	/**
	 * Single line:
	 *If condition Then true-statement [Else false-statement]
	 *Multiline:
	 *If condition Then
	 * statement
	 *Else If condition Then
	 * statement
	 *...
	 *Else
	 * statement
	 *End If
	 */
	public static void If() {

	}
	/**
	 * Any exception thrown inside a try block will be caught in the catch block.
	 *Call LastException to get the caught exception.
	 *Syntax:
	 *Try
	 * ...
	 *Catch
	 * ...
	 *End Try
	 */
	public static void Try() {

	}
	/**
	 * Any exception thrown inside a try block will be caught in the catch block.
	 *Call LastException to get the caught exception.
	 *Syntax:
	 *Try
	 * ...
	 *Catch
	 * ...
	 *End Try
	 */
	public static void Catch() {

	}
	/**
	 * Declares a variable.
	 *Syntax:
	 *Declare a single variable:
	 *Dim variable name [As type] [= expression]
	 *The default type is String.
	 *
	 *Declare multiple variables. All variables will be of the specified type.
	 *Dim variable1 [= expression], variable2 [= expression], ..., [As type]
	 *Note that the shorthand syntax only applies to Dim keyword.
	 *Example:<code>Dim a = 1, b = 2, c = 3 As Int</code>
	 *
	 *Declare an array:
	 *Dim variable(Rank1, Rank2, ...) [As type]
	 *Example:<code>Dim Days(7) As String</code>
	 *The actual rank can be omitted for zero length arrays.
	 */
	public static void Dim() {

	}
	/**
	 * Loops while the condition is true.
	 * Syntax:
	 * Do While condition
	 *  ...
	 * Loop
	 */
	public static void While() {

	}
	/**
	 * Loops until the condition is true.
	 * Syntax:
	 * Do Until condition
	 *  ...
	 * Loop
	 */
	public static void Until() {

	}
	/**
	 * Syntax:
	 *For variable = value1 To value2 [Step interval]
	 * ...
	 *Next
	 *If the iterator variable was not declared before it will be of type Int.
	 *
	 *Or:
	 *For Each variable As type In collection
	 * ...
	 *Next
	 *Examples:<code>
	 *For i = 1 To 10
	 * Log(i) 'Will print 1 to 10 (inclusive).
	 *Next
	 *For Each n As Int In Numbers 'an array
	 * Sum = Sum + n
	 *Next
	 *</code>
	 *Note that the loop limits will only be calculated once before the first iteration.
	 */
	public static void For() {

	}

	/**
	 * Declares a structure.
	 *Can only be used inside sub Globals or sub Process_Globals.
	 *Syntax:
	 *Type type-name (field1, field2, ...)
	 *Fields include name and type.
	 *Example:<code>
	 *Type MyType (Name As String, Items(10) As Int)
	 *Dim a, b As MyType
	 *a.Initialize
	 *a.Items(2) = 123</code>
	 */
	public static void Type() {

	}
	/**
	 * Returns from the current sub and optionally returns the given value.
	 *Syntax: Return [value]
	 */
	public static void Return() {

	}
	/**
	 * Declares a sub with the parameters and return type.
	 *Syntax: Sub name [(list of parameters)] [As return-type]
	 *Parameters include name and type.
	 *The lengths of arrays dimensions should not be included.
	 *Example:<code>
	 *Sub MySub (FirstName As String, LastName As String, Age As Int, OtherValues() As Double) As Boolean
	 * ...
	 *End Sub</code>
	 *In this example OtherValues is a single dimension array.
	 *The return type declaration is different than other declarations as the array parenthesis follow the type and not
	 *the name (which does not exist in this case).
	 */
	public static void Sub() {

	}
	/**
	 * Exits the most inner loop.
	 *Note that Exit inside a Select block will exit the Select block.
	 */
	public static void Exit() {

	}
	/**
	 * Stops executing the current iteration and continues with the next one.
	 */
	public static void Continue() {

	}
	/**
	 * Compares a single value to multiple values.
	 *Example:<code>
	 *Dim value As Int = 7
	 *Select value
	 *	Case 1
	 *		Log("One")
	 *	Case 2, 4, 6, 8
	 *		Log("Even")
	 *	Case 3, 5, 7, 9
	 *		Log("Odd larger than one")
	 *	Case Else
	 *		Log("Larger than 9")
	 *End Select</code>
	 */
	public static void Select() {

	}
	/**
	 * Tests whether the object is of the given type.
	 *Example:<code>
	 *For i = 0 To Activity.NumberOfViews - 1
	 *  If Activity.GetView(i) Is Button Then
	 *   Dim b As Button
	 *   b = Activity.GetView(i)
	 *   b.Color = Colors.Blue
	 *  End If
	 *Next</code>
	 */
	public static void Is() {

	}
	/**
	 * Immediately ends the application and stops the process.
	 */
	public static void ExitApplication() {
		System.exit(0);
	}
	/**
	 * Immediately ends the application and stops the process.
	 *The specified ExitCode will be returned as the process exit code (0 means no errors).
	 */
	public static void ExitApplication2(int ExitCode) {
		System.exit(ExitCode);
	}

	/**
	 * For classes: returns a reference to the current instance.
	 *For activities and services: returns a reference to an object that can be used with CallSub, CallSubDelayed and SubExists keywords.
	 *Cannot be used in code modules.
	 */
	public static Object Me(BA ba) {
		return null;
	}
	private static ScheduledThreadPoolExecutor sleepPool;
	/**
	 * Pauses the current sub execution and resumes it after the specified time.
	 */
	public static void Sleep(int Milliseconds) {
		
	}
	/**
	 * Inline If - returns TrueValue if Condition is True and False otherwise. Only the relevant expression is evaluated. 
	 */
	public static Object IIf (boolean Condition, Object TrueValue, Object FalseValue) {
		return null;
	}
	@Hide
	public static void Sleep(final BA ba, final ResumableSub rs, int Milliseconds) {
		if (sleepPool == null) {
			sleepPool = new ScheduledThreadPoolExecutor(1,  new ThreadFactory() {
	            public Thread newThread(Runnable r) {
	                Thread t = Executors.defaultThreadFactory().newThread(r);
	                t.setDaemon(true);
	                return t;
	            }
	        });
		}
		sleepPool.schedule(new Runnable() {

			@Override
			public void run() {
				ba.postRunnable(new Runnable() {

					@Override
					public void run() {
						try {
							rs.resume(ba, null);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
			}
		}, Milliseconds, TimeUnit.MILLISECONDS);
	}
	
	
	@Hide
	public static void WaitFor(String SubName, BA ba, ResumableSub rs, Object SenderFilter) {
		if (ba.waitForEvents == null)
			ba.waitForEvents = new HashMap<String, LinkedList<WaitForEvent>>();
		Object o;
		if (SenderFilter instanceof ObjectWrapper)
			o = ((ObjectWrapper<?>)SenderFilter).getObject();
		else
			o = SenderFilter;
		if (o instanceof ResumableSub) {
			ResumableSub rsSenderFilter = (ResumableSub)o;
			if (rsSenderFilter.completed)
				throw new RuntimeException("Resumable sub already completed");
			rsSenderFilter.waitForBA = ba;
			
		}
		LinkedList<WaitForEvent> ll = ba.waitForEvents.get(SubName);
		if (ll == null) {
			ll = new LinkedList<BA.WaitForEvent>();
			ba.waitForEvents.put(SubName, ll);
		}
		boolean added = false;
		Iterator<WaitForEvent> it = ll.iterator();
		while (it.hasNext()) {
			WaitForEvent wfe = it.next();
			if (added == false && ((o == null && wfe.noFilter()) || (o != null && o == wfe.senderFilter.get()))) {
				added = true;
				wfe.rs = rs;
			} else if (wfe.cleared()) {
				it.remove();
			}
		}
		if (added == false) {
			WaitForEvent wfe = new WaitForEvent(rs, o);
			if (wfe.noFilter())
				ll.addLast(wfe);
			else
				ll.addFirst(wfe);
		}
	}
	@Hide
	public static void ReturnFromResumableSub(final ResumableSub rs, final Object returnValue) {
		BA.firstInstance.postRunnable(new Runnable() {
			
			@Override
			public void run() {
				rs.completed = true;
				if (rs.waitForBA != null) {
					if (rs.waitForBA instanceof StandardBA && BA.firstInstance instanceof StandardBA) {
						StandardBA sba = (StandardBA)rs.waitForBA;
						StandardBA fba = (StandardBA)BA.firstInstance;
						if (sba.getOwnerThread() != fba.getOwnerThread()) {
							rs.waitForBA.raiseEventFromDifferentThread(rs, null, 0, "complete", false, new Object[] {returnValue});
							return;
						}
						
					} 
					rs.waitForBA.raiseEvent(rs, "complete", returnValue);
				}
				
			}
		});
		
	}
	/**
	 * This object is returned from a call to a non-void resumable sub.
	 *You can use it as the sender filter parameter and wait for the Complete event. 
	 */
	@ShortName("ResumableSub")
	public static class ResumableSubWrapper extends AbsObjectWrapper<ResumableSub> {
		/**
		 * Tests whether the resumable sub has already completed.
		 */
		public boolean getCompleted() {
			return getObject().completed;
		}
	}
			


}

