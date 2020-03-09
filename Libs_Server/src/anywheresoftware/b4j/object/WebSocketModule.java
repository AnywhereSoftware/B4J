
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

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.common.WebSocketRemoteEndpoint;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.B4AClass;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4j.objects.collections.JSONParser;

public class WebSocketModule {
	@Hide
	public static class Servlet extends WebSocketServlet {
		public static int DATA_TIMEOUT = 10000;
		private final Class<?> handlerClass;
		private final Method initializeMethod;
		private final boolean singleThread;
		private final int maxIdleTimeMinutes;
		static ThreadPoolExecutor pool;
		public Servlet(Class<?> handlerClass, boolean singleThread, int maxIdleTimeMinutes) throws SecurityException, NoSuchMethodException {
			this.handlerClass = handlerClass;
			initializeMethod = JServlet.getInitializeMethod(handlerClass);
			this.singleThread = singleThread;
			this.maxIdleTimeMinutes = maxIdleTimeMinutes;
			if (pool == null) {
				pool = new ThreadPoolExecutor(10, Integer.MAX_VALUE,
                        30L, TimeUnit.MINUTES,
                        new SynchronousQueue<Runnable>());
			}
		}
		@Override
		public void configure(WebSocketServletFactory factory) {
			factory.getPolicy().setIdleTimeout(maxIdleTimeMinutes * 1000 * 60);
			factory.setCreator(new WebSocketCreator() {

				@Override
				public Object createWebSocket(ServletUpgradeRequest req,
						ServletUpgradeResponse resp) {
					HttpSession hs = req.getHttpServletRequest().getSession(true);
					if (hs.isNew())
						hs.setMaxInactiveInterval(maxIdleTimeMinutes * 60);
					return new Adapter(Servlet.this);
				}
			});
		}
	}
	@Hide
	public static class Adapter extends WebSocketAdapter {
		private ThreadHandler handler;
		private final Servlet parentServlet;
		private BA ba;
		private WebSocket ws;
		private boolean disconnectRun;
		public Adapter(Servlet servlet) {
			parentServlet = servlet;
		}

		@Override
		public void onWebSocketConnect(Session sess) {
			super.onWebSocketConnect(sess);
			((WebSocketRemoteEndpoint)sess.getRemote()).setBatchMode(BatchMode.ON);
			handler = new ThreadHandler();
			if (parentServlet.singleThread) {
				BA.firstInstance.postRunnable(handler);
			}
			else {
				Servlet.pool.submit(handler);
			}
		}
		@Override
		public void onWebSocketClose(int statusCode, String reason)
		{
			super.onWebSocketClose(statusCode, reason);
			runDisconnect(true);

		}
		@Override
		public void onWebSocketError(Throwable cause) {
			super.onWebSocketError(cause);
			if (cause instanceof SocketTimeoutException || cause instanceof EOFException)
				System.err.println("onWebSocketError: " + cause.getMessage());
			else
				cause.printStackTrace();
		}
		@Override
		public void onWebSocketText(String message) {
			try {
				JSONParser jp = new JSONParser();
				jp.Initialize(message);
				Map map = jp.NextObject();
				String type = (String)map.Get("type");
				if (type.equals("event")) {
					final String eventName = (String) map.Get("event");
					if (eventName.contains("_") == false)
						throw new RuntimeException("Invalid event name: " + eventName);
					final Map params;
					if (map.ContainsKey("params"))
						params = (Map) AbsObjectWrapper.ConvertToWrapper(new Map(), map.Get("params"));
					else
						params = new Map();
					ba.postRunnable(new Runnable() {

						@Override
						public void run() {
							try {
								ba.raiseEvent(null, eventName.toLowerCase(BA.cul), params);
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								ws.Flush();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} else if (type.equals("data")) {
					WebSocket.SimpleFuture sf = ws.futures.removeFirst();
					sf.value = map.Get("data");
					sf.latch.countDown();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		private void runDisconnect(boolean messageLoopIsRunning) {
			if (ba != null) {
				if (messageLoopIsRunning) {
					ba.postRunnable(new Runnable() {
	
						@Override
						public void run() {
							if (ba != null) {
								if (disconnectRun == false) {
									ba.raiseEvent(null, "websocket_disconnected");
									if (parentServlet.singleThread == false)
										Common.StopMessageLoop(ba);
									disconnectRun = true;
								}
								ba = null;
							}
						}
					});
				} else {
					if (disconnectRun == false) {
						ba.raiseEvent(null, "websocket_disconnected");
						disconnectRun = true;
						ba = null;
					}
				}
			}
		}
		public class ThreadHandler implements Runnable {
			@Override
			public void run() {
				try {
					B4AClass handler = JServlet.createInstance(parentServlet.handlerClass, parentServlet.initializeMethod);
					ba = handler.getBA();
					ws = new WebSocket();
					if (parentServlet.singleThread == false)
						ba.cleanMessageLoop();
					ws.adapter = Adapter.this;
					ws.session = getSession();
					ws.singleThread = parentServlet.singleThread;
					ws.setEvents(ba);
					ws.setJQElements(handler);
					ba.raiseEvent(null, "websocket_connected", ws);
					ws.Flush();
					if (parentServlet.singleThread == false) {
						ba.startMessageLoop();
					}
				} catch (Exception e) {
					e.printStackTrace();
					try {
						runDisconnect(false);
						if (getSession() != null && getSession().isOpen()) {
							getSession().close();
						}
					} catch (Exception ee) {
						ee.printStackTrace();
					}
				}
			}

		}

	}
}
