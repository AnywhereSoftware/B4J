
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
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.ee11.servlet.ServletApiRequest;
import org.eclipse.jetty.ee11.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.ee11.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.Session.Listener.AbstractAutoDemanding;
import org.eclipse.jetty.websocket.api.exceptions.WebSocketTimeoutException;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.B4AClass;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4j.objects.collections.JSONParser;

public class WebSocketModule {
	@Hide
	public static boolean suppressClosedChannelException = true;
	@Hide
	public static class Servlet extends JettyWebSocketServlet  {
		private static final long serialVersionUID = 1L;
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
		protected void configure(JettyWebSocketServletFactory factory) {
			factory.setIdleTimeout(Duration.ofMinutes(maxIdleTimeMinutes));
			factory.setCreator(new JettyWebSocketCreator() {

				@Override
				public Object createWebSocket(JettyServerUpgradeRequest req, JettyServerUpgradeResponse resp) {
					try {
						Adapter adapter = new Adapter(Servlet.this, req);
						if (adapter.start() == false) {
							resp.sendForbidden("");
							return null;
						}
						return adapter;
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			});

		}
	}
	@Hide
	public static class Adapter extends AbstractAutoDemanding {
		private ThreadHandler handler;
		public B4AClass classInstance;
		private final Servlet parentServlet;
		public StubServletRequest upgradeRequest;
		public JettyServerUpgradeRequest originalUpgradeRequest;
		private BA ba;
		private WebSocket ws;
		private boolean disconnectRun;
		private final CountDownLatch startLatch = new CountDownLatch(1);
		public boolean startResult;
		public Adapter(Servlet servlet, JettyServerUpgradeRequest upgradeRequest) throws InterruptedException {
			parentServlet = servlet;
			this.originalUpgradeRequest = upgradeRequest;
			handler = new ThreadHandler();

		}
		public boolean start() throws InterruptedException {
			if (parentServlet.singleThread) {
				BA.firstInstance.postRunnable(handler);
			}
			else {
				Servlet.pool.submit(handler);
			}
			if (startLatch.await(120, TimeUnit.SECONDS) == false) {
				throw new RuntimeException("timeout waiting for handler to start");
			}
			upgradeRequest = new StubServletRequest((ServletApiRequest) originalUpgradeRequest.getHttpServletRequest());
			return startResult;

		}

		@Override
		public void onWebSocketOpen(Session session) {
			super.onWebSocketOpen(session);
			
			ba.postRunnable(() -> {
				ws = new WebSocket(getSession(), Adapter.this, parentServlet.singleThread);
				ws.setEvents(ba);
				ws.setJQElements(classInstance);
				ba.raiseEvent(null, "websocket_connected", ws);
			});

		}
		@Override
		public void onWebSocketClose(int statusCode, String reason, Callback callback)
		{
			super.onWebSocketClose(statusCode, reason, callback);
			runDisconnect(true);

		}
		@Override
		public void onWebSocketError(Throwable cause) {
			super.onWebSocketError(cause);
			if (cause instanceof SocketTimeoutException || cause instanceof EOFException ||cause instanceof ClosedChannelException || cause instanceof WebSocketTimeoutException)
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
					classInstance = JServlet.createInstance(parentServlet.handlerClass, parentServlet.initializeMethod);
					ba = classInstance.getBA();
					if (parentServlet.singleThread == false)
						ba.cleanMessageLoop();
					Boolean startResult = (Boolean)ba.raiseEvent(null, "websocket_upgrade", AbsObjectWrapper.ConvertToWrapper(new JServlet.ServletRequestWrapper(), originalUpgradeRequest.getHttpServletRequest()));
					Adapter.this.startResult = Boolean.FALSE.equals(startResult) ? false : true;
				}
				catch (Exception e) {
					System.err.println("failed to create websocket handler.");
					logException(e);
					return;
				} finally {
					Adapter.this.startLatch.countDown();
				}

				try {
					if (parentServlet.singleThread == false) {
						ba.startMessageLoop();
					}
				} catch (Exception e) {
					logException(e);
					try {
						runDisconnect(false);
						if (getSession() != null && getSession().isOpen()) {
							getSession().close();
						}
					} catch (Exception ee) {
						logException(ee);
					}
				}
			}

		}
		private void logException(Exception e) {
			if (suppressClosedChannelException && e instanceof ClosedChannelException)
				BA.LogError(String.valueOf(e));
			else
				e.printStackTrace();
		}

	}
}
