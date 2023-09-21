
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.RequestLogWriter;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CustomClass;
import anywheresoftware.b4a.BA.CustomClasses;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.IterableList;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.collections.Map.MyMap;
import anywheresoftware.b4a.objects.streams.File;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;

@CustomClasses(values = {
		@CustomClass(name = "Server Handler", fileNameWithoutExtension = "server_handler"),
		@CustomClass(name = "Server Filter", fileNameWithoutExtension = "server_filter"),
		@CustomClass(name = "Server WebSocket", fileNameWithoutExtension = "server_websocket")
})
@Version(4.01f)
@ShortName("Server")
@DependsOn(values={"c3p0-0.9.5.2", "c3p0-oracle-thin-extras-0.9.5.2", "mchange-commons-java-0.2.11"
		, "json", "jserver/http2-common-11.0.9.jar", 
		"jserver/http2-server-11.0.9.jar", 
		"jserver/jetty-alpn-java-server-11.0.9.jar", 
		"jserver/jetty-alpn-server-11.0.9.jar", 
		"jserver/jetty-io-11.0.9.jar", 
		"jserver/jetty-jakarta-servlet-api-5.0.2.jar", 
		"jserver/jetty-jakarta-websocket-api-2.0.0.jar", 
		"jserver/jetty-server-11.0.9.jar", 
		"jserver/jetty-servlet-11.0.9.jar", 
		"jserver/jetty-servlets-11.0.9.jar", 
		"jserver/jetty-slf4j-impl-11.0.9.jar", 
		"jserver/jetty-util-11.0.9.jar", 
		"jserver/slf4j-api-2.0.0-alpha6.jar", 
		"jserver/websocket-core-common-11.0.9.jar", 
		"jserver/websocket-core-server-11.0.9.jar", 
		"jserver/websocket-jakarta-client-11.0.9.jar", 
		"jserver/websocket-jakarta-common-11.0.9.jar", 
		"jserver/websocket-jakarta-server-11.0.9.jar", 
		"jserver/websocket-jetty-api-11.0.9.jar", 
		"jserver/websocket-jetty-common-11.0.9.jar", 
		"jserver/websocket-jetty-server-11.0.9.jar", 
		"jserver/websocket-servlet-11.0.9.jar", 
		"jserver/jetty-http-11.0.9.jar", 
		"jserver/jetty-security-11.0.9.jar", 
		"jserver/http2-hpack-11.0.9.jar", 
		"jserver/jetty-webapp-11.0.9.jar"})
public class ServerWrapper {
	@Hide
	public Server server;
	private BA ba;
	@SuppressWarnings("unused")
	private String eventName;
	private String staticFiles = File.Combine(File.getDirApp(), "www");
	private int retainDays = 30;
	private int port = 8080;
	private String logsFileFolder = File.Combine(File.getDirApp(), "logs");
	private ArrayList<HandlerData> webSockets = new ArrayList<HandlerData>();
	@Hide
	public ArrayList<HandlerData> filters = new ArrayList<HandlerData>();
	private ArrayList<HandlerData> backgroundWorkers = new ArrayList<HandlerData>();
	private boolean http2Enabled;
	@Hide
	public ServletContextHandler context;
	@Hide
	public String host = null;
	@Hide
	public GzipHandler gzipHandler;
	private int threadsIndexCounter;
	private java.util.Map<String, String> staticFilesOptions;
	private java.util.Map<String, String> errorMap;
	private SslContextFactory.Server sslFactory;
	private int SslPort;
	@Hide
	public boolean SniHostCheck = false;
	@Hide
	public boolean SniRequired = false;
	private String customLogFormat = CustomRequestLog.EXTENDED_NCSA_FORMAT;

	private final ArrayList<HandlerData> handlers = new ArrayList<ServerWrapper.HandlerData>();
	private final ThreadLocal<Integer> threadsIndex = new ThreadLocal<Integer>() {
		protected synchronized Integer initialValue() {
			return threadsIndexCounter++;
		}
	};
	/**
	 * Sets whether "waiting for value" messages are logged.
	 *These messages can help you to optimize the code and reduce the network latency effects.
	 */
	public static boolean LogWaitingMessages = true;
	static int debugNetworkLatency = 100;
	/**
	 * Initializes the server.
	 *EventName is currently not used.
	 */
	public void Initialize(BA ba, String EventName) {
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		threadsIndex.get().intValue(); //main thread should always be 0.
		server = new Server();
		context = new ServletContextHandler();
		context.setContextPath("/");
	}

	/**
	 * Starts the server. The handlers and the configuration settings should be set before the server is started.
	 */
	@SuppressWarnings({ "resource", "unchecked" })
	public void Start() throws Exception {
		BA.exitOnUnhandledExceptions = false;
		ServerConnector http = new ServerConnector(server);
		http.setPort(port);
		if (host != null)
			http.setHost(host);
		Connector[] connectors;
		if (http2Enabled) {
			if (sslFactory == null)
				throw new RuntimeException("SSL must be configured for HTTP2 to be enabled.");
		}
		if (sslFactory != null) {
			HttpConfiguration https_config = new HttpConfiguration();
			SecureRequestCustomizer src = new SecureRequestCustomizer();
			src.setSniHostCheck(SniHostCheck);
			src.setSniRequired(SniRequired);
			https_config.addCustomizer(src);
			HttpConnectionFactory http1 = new HttpConnectionFactory(https_config);
			SslConnectionFactory ssl = new SslConnectionFactory(sslFactory, http2Enabled ? "alpn": "HTTP/1.1");
			ServerConnector https;
			
			if (http2Enabled) {
				HTTP2ServerConnectionFactory http2 = new HTTP2ServerConnectionFactory(https_config);
				ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
				alpn.setDefaultProtocol(http1.getProtocol());
//				if (draftHttp3PortWillBeChanged > 0) {
//					HTTP3ServerConnector h3 = new HTTP3ServerConnector(server, sslFactory, new HTTP3ServerConnectionFactory(https_config));
//					h3.setPort(draftHttp3PortWillBeChanged);
//					server.addConnector(h3);
//					System.out.println("adding h3 connector");
//				}
				https = new ServerConnector(server, ssl, alpn, http2, http1);
			}
			else {
				https = new ServerConnector(server, ssl, http1);
			}
			sslFactory.setUseCipherSuitesOrder(true);
			https.setPort(SslPort);
			if (host != null)
				https.setHost(host);
			connectors = new Connector[] {http, https};
		}
		else {
			connectors = new Connector[] {http};
		}
		for (Connector connector : connectors)
			server.addConnector(connector);
		context.setResourceBase(staticFiles);
		final boolean debug = BA.isShellModeRuntimeCheck(ba);
		for (HandlerData hd : handlers) {
			JServlet js = new JServlet(Class.forName(fixClassName(hd)), hd.singleThread | debug);
			ServletHolder sh = new ServletHolder(js);
			context.addServlet(sh, hd.path);
		}
		for (HandlerData hd : filters) {
			FilterHolder fh;
			if (hd.internal) {
				fh = new FilterHolder((Class<? extends Filter>) Class.forName(hd.clazz));
				Map settings = (Map) hd.extra;
				if (settings != null && settings.IsInitialized()) {
					HashMap<String,String> m = new HashMap<String, String>();
					copyMap(settings, m, true); //integerNumbersOnly!
					fh.setInitParameters(m);
				}
			}
			else {
				JServlet js = new JServlet(Class.forName(fixClassName(hd)), hd.singleThread | debug);
				fh = new FilterHolder(js);
			}
			context.addFilter(fh, hd.path, EnumSet.of(DispatcherType.REQUEST));
		}
		
		for (HandlerData hd : webSockets) {
			WebSocketModule.Servlet s = new WebSocketModule.Servlet(Class.forName(fixClassName(hd)), hd.singleThread | debug,
					hd.maxIdleTime);
			ServletHolder sh = new ServletHolder(s);
			context.addServlet(sh, hd.path);
		}
		JettyWebSocketServletContainerInitializer.configure(context, null);
		ServletHolder staticHolder = context.addServlet(DefaultServlet.class, "/");
		if (staticFilesOptions != null)
			staticHolder.setInitParameters(staticFilesOptions);
		context.setSessionHandler(new SessionHandler());
		SessionHandler manager = context.getSessionHandler();
		manager.setHttpOnly(true);
		if (errorMap != null) {
			ErrorPageErrorHandler err = new ErrorPageErrorHandler();
			err.setErrorPages(errorMap);
			context.setErrorHandler(err);
		}
		HandlerCollection handlers = new HandlerCollection();
		RequestLogHandler log = new RequestLogHandler();
		File.MakeDir(null, logsFileFolder);
		CustomRequestLog rl = new CustomRequestLog(File.Combine(logsFileFolder, "b4j-yyyy_mm_dd.request.log"), customLogFormat);
		RequestLogWriter logWriter = (RequestLogWriter)rl.getWriter();
		logWriter.setRetainDays(retainDays);
		logWriter.setAppend(true);
		log.setRequestLog(rl);
		handlers.setHandlers(new Handler[]{context,new DefaultHandler(), log});
		Handler h = handlers;
		if (gzipHandler != null) {
			gzipHandler.setHandler(handlers);
			h = gzipHandler;
		}
		server.setHandler(h);
		server.start();
		if (!debug)
			debugNetworkLatency = 0;
		if (debugNetworkLatency > 0)
			System.out.println("Emulated network latency: " + debugNetworkLatency + "ms");
		if (backgroundWorkers.size() > 0) {
			ba.postRunnable(new Runnable() {

				@Override
				public void run() {
					try {
						BackgroundWorkersManager backWorkers = new BackgroundWorkersManager(ba, debug);
						for (HandlerData hd : backgroundWorkers)
							backWorkers.startWorker(Class.forName(fixClassName(hd)));
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});

		}
	}
	
	private String fixClassName(HandlerData hd) {
		String className = hd.clazz.toLowerCase(BA.cul);
		if (className.contains(".") == false)
			className = BA.packageName + "." + className;
		return className;
	}
	/**
	 * Sets the emulated network latency in debug mode, measured in milliseconds.
	 *Default value is 100.
	 *You should only call this method before the server is started.
	 *The emulated latency only affects server -> client -> server round-trips.
	 */
	public void setDebugNetworkLatency (int i) {
		debugNetworkLatency = i;
	}
	/**
	 * Gets or sets the static files folder.
	 */
	public void setStaticFilesFolder (String s) {
		staticFiles = s;
	}
	public String getStaticFilesFolder() {
		return staticFiles;
	}
	/**
	 * Gets or sets the logs files folder.
	 */
	public void setLogsFileFolder (String s) {
		logsFileFolder = s;
	}
	public String getLogsFileFolder() {
		return logsFileFolder;
	}
	/**
	 * Gets or sets whether Http/2.0 is enabled. Note that it requires some configuration (see the tutorial for more information).
	 */
	public boolean getHttp2Enabled() {
		return http2Enabled;
	}
	public void setHttp2Enabled(boolean b) {
		http2Enabled = b;
	}
	/**
	 * Calling this method will cause the server to compress the responses if the client supports GZIP responses and the resource mime type is not of a known compressed type.
	 *In most cases it should be enabled.
	 */
	public void setGzipEnabled(boolean b) {
		if (b) {
			gzipHandler = new GzipHandler();
			gzipHandler.setIncludedMethods("POST", "GET");
		} else {
			gzipHandler = null;
		}
	}

	/**
	 * Gets or sets the server port.
	 */
	public void setPort(int p) {
		port = p;
	}
	public int getPort() {
		return port;
	}
	/**
	 * Gets or sets the number of days the rotated logs will be kept.
	 */
	public void setLogsRetainDays(int p) {
		retainDays = p;
	}
	public int getLogsRetainDays() {
		return retainDays;
	}
	/**
	 * Each thread is mapped to a specific index. This property returns the current thread index. 
	 *Note that the main thread index is always 0.
	 */
	public int getCurrentThreadIndex() {
		return threadsIndex.get().intValue();
	}
	/**
	 * Maps a handler class.
	 *Path - The handler will be mapped to the following path. You can use wildcards in the path.
	 *Class - The class name (string).
	 *SingleThreadHandler - Whether this handler should always run in the main thread.
	 */
	public void AddHandler(String Path, String Class, boolean SingleThreadHandler) {
		handlers.add(new HandlerData(Class, Path, SingleThreadHandler));
	}
	/**
	 * Maps a WebSocket class.
	 *Path - The WebSocket will be mapped to this path.
	 *Class - WebSocket class (string).
	 */
	public void AddWebSocket(String Path, String Class) {
		HandlerData hd = new HandlerData(Class, Path, false);
		hd.maxIdleTime = 180;
		webSockets.add(hd);
	}
	/**
	 * Maps a filter class. 
	 *Path - The filter will be mapped to the following path. You can use wildcards in the path.
	 *Class - The class name (string).
	 *SingleThreadHandler - Whether this handler should always run in the main thread.
	 */
	public void AddFilter(String Path, String Class, boolean SingleThreadHandler) {
		filters.add(new HandlerData(Class, Path, SingleThreadHandler));
	}
	/**
	 * Adds a filter that protects against intentional or unintentional denial of service attacks.
	 *It restricts the number of requests sent from the same connection.
	 *Path - The filter will be mapped to the given path. You can use wildcards in the path.
	 *Settings - An optional map with various settings. See this link: <link>DoS Filter Documentation|http://www.eclipse.org/jetty/documentation/9.4.x/dos-filter.html</link>
	 *
	 *Example:<code>srvr.AddDoSFilter("/*", Null)</code>
	 */
	public void AddDoSFilter(String Path, Map Settings) {
		HandlerData hd = new HandlerData("org.eclipse.jetty.servlets.DoSFilter", Path, false);
		hd.extra = Settings;
		hd.internal = true;
		filters.add(hd);
	}
	/**
	 * Adds a background worker. An instance of the specified class will be created and initialized from a background thread.
	 *Class - The class name. The class should be a standard class (not server handler).
	 *
	 *You can call StartMessageLoop in the Initialize sub to keep the class instance running for as long as needed.
	 *Note that in debug mode the code will be executed by the main thread.
	 */
	public void AddBackgroundWorker (String Class) {
		HandlerData hd = new HandlerData(Class, null, false);
		backgroundWorkers.add(hd);
	}
	/**
	 * This method allows you to set custom error pages.
	 *The PagesMap maps between status codes (such as 404 and 500) and the handlers or pages that will be called for each error.
	 *This method should be called before the server is started.
	 *Example:<code>
	 *Dim err As Map
	 *err.Initialize
	 *err.Put(404, "/404.html") 'map to a custom page
	 *srvr.SetCustomErrorPages(err)</code>
	 */
	public void SetCustomErrorPages(Map PagesMap) {
		errorMap = new java.util.HashMap<String, String>();
		copyMap(PagesMap, errorMap, false);
	}
	/**
	 * A Map with configuration parameters that affect the way the server serves static files.
	 *See this link for more information: <link>http://download.eclipse.org/jetty/stable-9/apidocs/org/eclipse/jetty/servlet/DefaultServlet.html|http://download.eclipse.org/jetty/stable-9/apidocs/org/eclipse/jetty/servlet/DefaultServlet.html</link>.
	 */
	public void SetStaticFilesOptions(Map Options) {
		staticFilesOptions = new HashMap<String, String>();
		copyMap(Options, staticFilesOptions, true);
	}
	private void copyMap(Map m, java.util.Map<String, String> o, boolean integerNumbersOnly) {
		for (Entry<Object, Object> e : ((MyMap)m.getObject()).entrySet()) {
			String value;
			if (integerNumbersOnly && e.getValue() instanceof Number) {
				value = String.valueOf(((Number)e.getValue()).longValue());
			}
			else
				value = String.valueOf(e.getValue());
			o.put(String.valueOf(e.getKey()), value);
		}
	}
	/**
	 * Gets or sets the log format.
	 *The format is documented <link>here|https://www.eclipse.org/jetty/javadoc/jetty-11/org/eclipse/jetty/server/CustomRequestLog.html</link>.
	 */
	public String getLogFormat() {
		return customLogFormat;
	}
	public void setLogFormat(String s) {
		customLogFormat = s;
	}
	/**
	 * Configures the SSL connector and sets the port used for https connections.
	 */
	public void SetSslConfiguration(SslContextFactoryWrapper Config, int Port) {
		this.sslFactory = Config.getObject();
		this.SslPort = Port;
	}
	/**
	 * Returns the port used for SSL (https) connections.
	 */
	public int getSslPort() {
		return this.SslPort;
	}
	/**
	 * Returns an initialized Map that can safely be accessed by multiple threads (based on Java ConcurrentHashMap).
	 *Unlike the standard Map the order of items is not preserved.
	 *Note that this Map does not support the following methods: GetKeyAt and GetValueAt. 
	 */
	public Map CreateThreadSafeMap() {
		MyMapWrapper m = new MyMapWrapper();
		m.setObject(new ConcurrentMyMap());
		return m;
	}
	@Hide
	public static class MyMapWrapper extends Map {
		@Override
		public IterableList Values() {
			List l1 = new List();
			l1.Initialize();
			for (Object o : getObject().values()) {
				l1.Add(o);
			}
			return l1;
		}
		@Override
		public IterableList Keys() {
			List l1 = new List();
			l1.Initialize();
			for (Object o : getObject().keySet()) {
				l1.Add(o);
			}
			return l1;
		}
	}
	static class ConcurrentMyMap extends MyMap {
		public Object getKey(int index) {
			throw new RuntimeException("Concurrent Map does not support this method.");
		}
		public Object getValue(int index) {
			throw new RuntimeException("Concurrent Map does not support this method.");
		}
		protected java.util.Map<Object, Object> createInnerMap() {
			return new ConcurrentHashMap<Object, Object>();
		}
	}
	
	@Hide
	public static class HandlerData {
		public final String clazz;
		public final String path;
		public final boolean singleThread;
		public int maxIdleTime;
		public Object extra;
		public boolean internal;
		public HandlerData(String clazz, String path, boolean singleThread) {
			this.clazz = clazz;
			this.path = path;
			this.singleThread = singleThread;
		}

	}
	/**
	 * Holds the key store configuration. 
	 */
	@ShortName("SslConfiguration")
	public static class SslContextFactoryWrapper extends AbsObjectWrapper<SslContextFactory.Server> {
		public void Initialize() {
			setObject(new SslContextFactory.Server());
		}
		/**
		 * Sets the path to the keystore file.
		 */
		public void SetKeyStorePath(String Dir, String FileName) {
			getObject().setKeyStorePath(File.Combine(Dir, FileName));
		}
		/**
		 * Sets the keystore password.
		 */
		public void setKeyStorePassword(String p) {
			getObject().setKeyStorePassword(p);
		}
		/**
		 * Sets the key password.
		 */
		public void setKeyManagerPassword(String p) {
			getObject().setKeyManagerPassword(p);
		}
	}
}
