
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.B4AClass;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4j.object.JServlet.ServletRequestWrapper;
import anywheresoftware.b4j.object.WebSocketModule.Adapter;
import anywheresoftware.b4j.objects.collections.JSONParser.JSONGenerator;

@ShortName("WebSocket")
public class WebSocket{
	/**
	 * Holds a reference to a value. The value may not be currently available.
	 *In that case the thread will wait for it to be available when you call Value.
	 */
	@ShortName("Future")
	public static class SimpleFuture {
		final WebSocket ws;
		final CountDownLatch latch;
		Object value;
		long startTime;
		public SimpleFuture() {
			ws = null;
			latch = null;
		}
		@Hide
		public SimpleFuture(WebSocket ws) {
			latch = new CountDownLatch(1);
			this.ws = ws;
			if (ServerWrapper.debugNetworkLatency > 0)
				startTime = System.currentTimeMillis();
		}
		/**
		 * If the value is already available then it is immediately returned. Otherwise the thread will wait for it to be available.
		 */
		public Object getValue() throws InterruptedException, TimeoutException, ExecutionException, IOException {
			ws.Flush();
			long n = 0;
			if (ServerWrapper.LogWaitingMessages && latch.getCount() > 0) {
				n = System.currentTimeMillis();
			}
			if (!latch.await(WebSocketModule.Servlet.DATA_TIMEOUT, TimeUnit.MILLISECONDS)) {
				throw new TimeoutException();
			}
			if (ServerWrapper.debugNetworkLatency > 0 ) {
				long timeToWait = ServerWrapper.debugNetworkLatency - (System.currentTimeMillis() - n);
				if (timeToWait > 0)
					Thread.sleep(timeToWait);
			}
			if (ServerWrapper.LogWaitingMessages && n > 0)
				System.out.println("Waiting for value (" + (System.currentTimeMillis() - n) + " ms)");
			
			return value;
		}
	}
	@Hide
	public Session session;
	Adapter adapter;
	boolean singleThread;
	final LinkedList<SimpleFuture> futures = new LinkedList<SimpleFuture>();
	private boolean shouldFlushOutput;
	
	/**
	 * Returns true if the request was made with a secure channel (SSL).
	 */
	public boolean getSecure() {
		return session.isSecure();
	}
	/**
	 * Returns true if the connection is open.
	 */
	public boolean getOpen() {
		return session.isOpen();
	}
	/**
	 * Flushes the output stream. Flush is called automatically when client events complete.
	 *You need to explicitly call it at the end of server events.
	 */
	public void Flush() throws IOException {
		if (shouldFlushOutput) {
			session.getRemote().flush();
			shouldFlushOutput = false;
		}
	}
	
	private void sendText(String s) throws IOException {
		session.getRemote().sendString(s, null);
		shouldFlushOutput = true;
	}
	private SimpleFuture get(String etype, Map m) {
		JSONGenerator jg = new JSONGenerator();
		m.Put("etype", etype);
		try {
			jg.Initialize(m);
			SimpleFuture sf = new SimpleFuture(this);
			futures.add(sf);
			sendText(jg.ToString());
			return sf;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void set(String etype, String id, Object value, String property)  {
		Map m = new Map(); m.Initialize();
		if (property != null)
			m.Put("prop", property);
		if (id != null)
			m.Put("id", id.toLowerCase(BA.cul));
		if (value != null)
			m.Put("value", value);
		set(etype, m);
	}
	private void set(String etype, Map m) {
		JSONGenerator jg = new JSONGenerator();
		m.Put("etype", etype);
		try {
			jg.Initialize(m);
			if (session.isOpen())
				sendText(jg.ToString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Creates a JQueryElement object mapped to the given id.
	 *This is equivalent to GetElementBySelector("#" &amp; id.ToLowerCase).
	 */
	public JQueryElement GetElementById(String Id) {
		return GetElementBySelector("#" + Id.toLowerCase(BA.cul));
	}
	/**
	 *Creates a JQueryElement object mapped to the given selector.
	 */
	public JQueryElement GetElementBySelector(String Selector) {
		JQueryElement j = new JQueryElement();
		j.ws = this;
		j.id = Selector;
		return j;
	}
	@ShortName("JQueryElement")
	@Events(values={"Change (Params As Map)", 
			"Click (Params As Map)", 
			"DblClick (Params As Map)",
			"Focus (Params As Map)",
			"FocusIn (Params As Map)",
			"FocusOut (Params As Map)",
			"KeyUp (Params As Map)",
			"MouseDown (Params As Map)",
			"MouseEnter (Params As Map)",
			"MouseLeave (Params As Map)",
			"MouseMove (Params As Map)",
			"MouseUp (Params As Map)"
	})
	public static class JQueryElement{
		static final HashSet<String> knownEvents = new HashSet<String>(
				Arrays.asList("change", "click", "dblclick", "focus", "focusin", "focusout", "keyup",
						"mousedown", "mouseenter", "mouseleave", "mousemove", "mouseup"));
		private WebSocket ws;
		private String id;
		public String getId() {
			return id;
		}
		/**
		 * Runs the given jQuery method.
		 *Params - List or array of values to pass to the method. Set to Null if not needed.
		 */
		public void RunMethod(String Method, List Params) {
			Map m = new Map();
			m.Initialize();
			m.Put("id", id);
			m.Put("method", Method);
			if (Params != null && Params.IsInitialized())
				m.Put("params", Params);
			ws.set("runmethod", m);
		}
		/**
		 * Similar to RunMethod. The result of the method is returned as a Future object.
		 */
		public SimpleFuture RunMethodWithResult(String Method, List params) {
			Map m = new Map();
			m.Initialize();
			m.Put("id", id);
			m.Put("method", Method);
			if (params != null && params.IsInitialized())
				m.Put("params", params);
			return ws.get("runmethodWithResult", m);
		}
		private List createList(Object... args) {
			return Common.ArrayToList(args);
		}
		/**
		 * Returns the result of jQuery prop method.
		 */
		public SimpleFuture GetProp(String Property) {
			return RunMethodWithResult("prop", createList(Property));
		}
		/**
		 * Calls jQuery prop method with the given value.
		 */
		public void SetProp(String Property, Object Value) {
			RunMethod("prop", createList(Property, Value));
		}
		/**
		 * Returns the result of jQuery val method.
		 */
		public SimpleFuture GetVal() {
			return RunMethodWithResult("val", null);
		}
		/**
		 * Calls jQuery val method with the given value.
		 */
		public void SetVal(Object Value)  {
			RunMethod("val", createList(Value));
		}
		/**
		 * Returns the result of jQuery text method.
		 */
		public SimpleFuture GetText() {
			return RunMethodWithResult("text", null);
		}
		/**
		 * Calls jQuery text method with the given value.
		 */
		public void SetText(String Value)  {
			RunMethod("text", createList(Value));
		}
		/**
		 * Returns the result of jQuery html method.
		 */
		public SimpleFuture GetHtml() {
			return RunMethodWithResult("html", null);
		}
		public void SetHtml(String Value)  {
			RunMethod("html", createList(Value));
		}
		/**
		 * Returns the result of jQuery width method.
		 */
		public SimpleFuture GetWidth() {
			return RunMethodWithResult("width", null);
		}
		/**
		 * Calls jQuery width method with the given value.
		 */
		public void SetWidth(String Value)  {
			RunMethod("width", createList(Value));
		}
		/**
		 * Returns the result of jQuery height method.
		 */
		public SimpleFuture GetHeight() {
			return RunMethodWithResult("height", null);
		}
		/**
		 * Calls jQuery height method with the given value.
		 */
		public void SetHeight(String Value)  {
			RunMethod("height", createList(Value));
		}
		/**
		 * Returns the result of jQuery CSS method.
		 */
		public SimpleFuture GetCSS(String Property) {
			return RunMethodWithResult("css", createList(Property));
		}
		/**
		 * Calls jQuery css method with the given property and value.
		 */
		public void SetCSS(String Property, String Value) {
			RunMethod("css", createList(Property, Value));
		}
	}
	/**
	 * Runs a JavaScript function. Pass null to Args if it is not needed.
	 */
	public void RunFunction(String Function, List Args) {
		set("runFunction", null, (Args != null && Args.IsInitialized() == false) ? null : Args, Function);
	}
	/**
	 * Similar to RunFunction. Returns the result of the function.
	 */
	public SimpleFuture RunFunctionWithResult(String Function, List Args) {
		Map m = new Map(); m.Initialize();
		m.Put("prop", Function);
		if (Args != null && Args.IsInitialized())
			m.Put("value", Args);
		return get("runFunctionWithResult", m);
	}
	/**
	 * Creates a new JavaScript function with the Script parameter as the body and runs it. Args is an optional list of arguments to pass to this function.
	 */
	public void Eval(String Script, List Args) {
		set("eval", null, (Args != null && Args.IsInitialized() == false) ? null : Args, Script);
	}
	/**
	 * Calls JavaScript Alert method (shows a dialog).
	 */
	public void Alert(String Message) {
		set("alert", null, null, Message);
	}
	/**
	 * Similar to Eval. Returns the result of this function. Make sure to return a value from the script.
	 */
	public SimpleFuture EvalWithResult(String Script, List Args) {
		Map m = new Map(); m.Initialize();
		m.Put("prop", Script);
		if (Args != null && Args.IsInitialized())
			m.Put("value", Args);
		return get("evalWithResult", m);
	}
	/**
	 * Gets the upgrade request object. This is the request that started the WebSocket connection.
	 */
	public ServletRequestWrapper getUpgradeRequest() {
		return (ServletRequestWrapper) AbsObjectWrapper.ConvertToWrapper(new ServletRequestWrapper(),
				((ServletUpgradeRequest)((WebSocketSession)session).getUpgradeRequest()).getHttpServletRequest());
	}
	/**
	 * Returns the http session object which is tied to the current user.
	 */
	public HttpSessionWrapper getSession() {
		return (HttpSessionWrapper) AbsObjectWrapper.ConvertToWrapper(new HttpSessionWrapper(),
				((ServletUpgradeRequest)((WebSocketSession)session).getUpgradeRequest()).getHttpServletRequest().getSession());
	}
	/**
	 * Closes the WebSocket connection.
	 */
	public void Close() {
		if (session.isOpen())
			session.close();
	}
	
	void setEvents(BA ba) {
		Map m = new Map(); m.Initialize();
		m.Put("etype", "setAutomaticEvents");
		List events = new List();
		events.Initialize();
		m.Put("data", events);
		for (String sub : ba.htSubs.keySet()) {
			int i = sub.lastIndexOf("_");
			if (i > -1) {
				String event = sub.substring(i + 1);
					if (JQueryElement.knownEvents.contains(event)) {
					Map e = new Map();e.Initialize();
					e.Put("id", sub.substring(0, i));
					e.Put("event", event);
					events.Add(e);
				}
			}
		}
		JSONGenerator jg = new JSONGenerator();
		try {
			jg.Initialize(m);
			sendText(jg.ToString());
			Flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	void setJQElements(B4AClass handler) {
		for (Field f : handler.getClass().getFields()) {
			if (f.getType().equals(JQueryElement.class)) {
				try {
					JQueryElement j = this.GetElementById(f.getName().substring(1));
					f.set(handler, j);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}


}
