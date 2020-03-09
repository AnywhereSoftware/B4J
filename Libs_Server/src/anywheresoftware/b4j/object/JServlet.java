
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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.MultiPartInputStreamParser;
import org.eclipse.jetty.util.MultiPartInputStreamParser.MultiPart;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.B4AClass;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;
import anywheresoftware.b4a.objects.streams.File.OutputStreamWrapper;

@Hide
public class JServlet extends HttpServlet implements Filter{
	private final Class<?> handlerClass;
	private final Method initializeMethod;
	private final boolean singleThread;
	public JServlet(Class<?> handlerClass, boolean singleThread) throws SecurityException, NoSuchMethodException {
		this.handlerClass = handlerClass;
		initializeMethod = getInitializeMethod(handlerClass);
		this.singleThread = singleThread;
	}
	public static Method getInitializeMethod(Class<?> c) throws NoSuchMethodException, SecurityException {
		Method m = null;
		try {
			m = c.getDeclaredMethod("_initialize", BA.class);
		} catch (NoSuchMethodException e) {
			m = c.getDeclaredMethod("innerInitializeHelper", BA.class);
		}
		return m;
	}
	public static B4AClass createInstance(Class<?> handlerClass, Method initializeMethod) throws Exception {
		B4AClass handler = (B4AClass)handlerClass.newInstance();
		initializeMethod.invoke(handler, (Object)null);
		BA ba = handler.getBA();
		if (BA.isShellModeRuntimeCheck(ba)) {
			ba.raiseEvent(null, "initialize", (Object)null);
		}
		return handler;
	}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		Handle((HttpServletRequest)request, (HttpServletResponse)response, chain);
	}
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Handle(request, response, null);
	}
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Handle(request, response, null);
	}
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Handle(request, response, null);
	}
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Handle(request, response, null);
	}
		
	private void Handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		Handle h = new Handle(request, response, chain);
		if (singleThread && 
				(BA.firstInstance.getOwnerThread() != Thread.currentThread())) {
			h.cdl = new CountDownLatch(1);
			synchronized (BA.firstInstance) {
				BA.firstInstance.postRunnable(h);
				try {
					h.cdl.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			h.run();
		}
	}

	class Handle implements Runnable {
		private final HttpServletRequest request;
		private final HttpServletResponse response;
		private final FilterChain chain;
		public CountDownLatch cdl;
		public Handle (HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
			this.request = request;
			this.response = response;
			this.chain = chain;
		}
		@Override
		public void run() {
			try {
				try {
					B4AClass b4aclass = createInstance(handlerClass, initializeMethod);
					BA ba = b4aclass.getBA();
					Object o = ba.raiseEvent(null, chain == null ? "handle" : "filter" ,
							AbsObjectWrapper.ConvertToWrapper(new ServletRequestWrapper(),request), 
							AbsObjectWrapper.ConvertToWrapper(new ServletResponseWrapper(), response));
					if (chain != null) {
						if (Boolean.TRUE.equals(o))
							chain.doFilter(request, response);
					}
				} finally {
					if (cdl != null)
						cdl.countDown();
				}
			} catch (Exception e) {
				try {
					response.sendError(500, e.toString());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}

		}

	}

	@ShortName("ServletRequest")
	public static class ServletRequestWrapper extends AbsObjectWrapper<HttpServletRequest> {
		/**
		 * Returns true if the request was made with a secure channel (SSL).
		 */
		public boolean getSecure() {
			return getObject().isSecure();
		}
		/**
		 * Gets the user session. A new session will be created if this is the first request from this user.
		 */
		public HttpSessionWrapper GetSession() {
			return (HttpSessionWrapper)AbsObjectWrapper.ConvertToWrapper(new HttpSessionWrapper(), getObject().getSession());
		}
		/**
		 * Returns the request content type header or an empty string if the content type is not available.
		 */
		public String getContentType() {
			return BA.ReturnString(getObject().getContentType());
		}
		/**
		 * Returns the request character encoding or an empty string if the encoding is not available.
		 */
		public String getCharacterEncoding() {
			return BA.ReturnString(getObject().getCharacterEncoding());
		}
		/**
		 * Returns the request content length.
		 */
		public int getContentLength() {
			return getObject().getContentLength();
		}
		/**
		 * Returns a list with all the headers values mapped to the specified header.
		 */
		public List GetHeaders(String Name) {
			List l1 = new List();
			l1.Initialize();
			for (Enumeration<String> e = getObject().getHeaders(Name);e.hasMoreElements();) {
				l1.Add(e.nextElement());
			}
			return l1;
		}
		/**
		 * Returns the header value or an empty string if the header does not exist.
		 */
		public String GetHeader(String Name) {
			String s = getObject().getHeader(Name);
			return s == null ? "" : s;
		}
		/**
		 * Returns the HTTP method (GET or POST).
		 */
		public String getMethod() {
			return getObject().getMethod();
		}
		/**
		 * Returns the request URL without the host and any parameters.
		 */
		public String getRequestURI() {
			return getObject().getRequestURI();
		}
		/**
		 * Returns the full request URL including the scheme, host and parameters.
		 */
		public String getFullRequestURI() {
			return getObject().getRequestURL().toString() + (getObject().getQueryString() != null ? "?" + getObject().getQueryString() : "");
		}
		/**
		 * Returns the client IP address.
		 */
		public String getRemoteAddress() {
			return getObject().getRemoteAddr();
		}
		/**
		 * Returns the parameter value or an empty string if the parameter does not exist.
		 */
		public String GetParameter(String Name) {
			String h = getObject().getParameter(Name);
			return h == null ? "" : h;
		}
		public String[] GetParameterValues(String Name) {
			String[] s = getObject().getParameterValues(Name);
			if (s == null)
				s = new String[0];
			return s;
		}
		/**
		 * Returns a Map with all the parameter. The keys are the parameters names (strings) and the values are the parameters values (arrays of strings).
		 * 
		 */
		public Map getParameterMap() {
			Map m = new Map();
			m.Initialize();
			for (Entry<String, String[]> e : getObject().getParameterMap().entrySet())
				m.Put(e.getKey(), e.getValue());
			return m;
		}
		/**
		 * Returns an InputStream that allows you to read directly from the request body.
		 */
		public InputStreamWrapper getInputStream() throws IOException {
			return (InputStreamWrapper)AbsObjectWrapper.ConvertToWrapper(new InputStreamWrapper(), getObject().getInputStream());
		}
		/**
		 * Parses a multipart request and returns a Map with the parsed Parts as values and the parts names as keys.
		 *Folder - The files will be saved in this folder.
		 *MaxSize - The request maximum size.
		 */
		public Map GetMultipartData(String Folder, long MaxSize) throws IOException, ServletException {
			MultipartConfigElement config = new MultipartConfigElement(Folder, MaxSize, MaxSize, 81920);
			MultiPartInputStreamParser in = new MultiPartInputStreamParser(getObject().getInputStream(), getObject().getContentType(),
					config, new File(Folder));
			Collection<Part> parts = in.getParts();
			Map m = new Map();
			m.Initialize();
			for (Part p : parts) {
				MultiPartInputStreamParser.MultiPart mp = (MultiPartInputStreamParser.MultiPart)p;
				m.Put(mp.getName(),  mp);
			}
			return m;
		}
		/**
		 * Returns an array with the request cookies.
		 */
		public CookieWrapper[] GetCookies() {
			Cookie[] c = getObject().getCookies();
			if (c == null)
				return new CookieWrapper[0];
			CookieWrapper[] cw = new CookieWrapper[c.length];
			for (int i = 0;i < c.length;i++)
				cw[i] = (CookieWrapper)AbsObjectWrapper.ConvertToWrapper(new CookieWrapper(), c[i]);
			return cw;
		}
		

	}
	@ShortName("Part")
	public static class PartWrapper extends AbsObjectWrapper<MultiPart> {
		/**
		 * <b>This method should not be used. Call TempFile instead to get the file.</b>
		 */
		public boolean getIsFile() {
			return getObject().getFile() != null;
		}
		/**
		 * Returns the submitted file name.
		 */
		public String getSubmittedFilename() {
			return BA.ReturnString(getObject().getSubmittedFileName());
		}
		/**
		 * Returns the path to the temporary file.
		 */
		public String getTempFile() throws IOException {
			if (getObject().getFile() == null) {
				try {
					Method m = MultiPart.class.getDeclaredMethod("createFile");
					m.setAccessible(true);
					m.invoke(getObject());
					m = MultiPart.class.getDeclaredMethod("close");
					m.setAccessible(true);
					m.invoke(getObject());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return getObject().getFile().getCanonicalPath();
		}
		/**
		 * Returns the string value of this part. This should only be used with non-files parts.
		 */
		public String GetValue(String CharacterEncoding) throws UnsupportedEncodingException {
			return new String(getObject().getBytes(), (CharacterEncoding == null || CharacterEncoding == "") ? "UTF8" : CharacterEncoding);
		}
	}
	
	@ShortName("ServletResponse")
	public static class ServletResponseWrapper extends AbsObjectWrapper<HttpServletResponse>  {
		/**
		 * Returns the error string that is tied to this response. This is useful with handlers that act as custom error pages.
		 */
		public String getErrorReason() {
			return BA.ReturnString(getObject() instanceof Response ? ((Response)getObject()).getReason() : "");
		}
		/**
		 * Writes the text to the response stream.
		 */
		public ServletResponseWrapper Write(String Text) throws IOException {
			getObject().getWriter().write(Text);
			return this;
		}
		/**
		 * Returns an OutputStream that can be used to write bytes directly to the response stream.
		 */
		public OutputStreamWrapper getOutputStream() throws IOException {
			return (OutputStreamWrapper) AbsObjectWrapper.ConvertToWrapper(new OutputStreamWrapper(), getObject().getOutputStream());
		}
		/**
		 * Gets or sets the response status code.
		 *Note that you should use SendError to send error statuses. Otherwise the custom error pages will be skipped.
		 */
		public void setStatus(int s) {
			getObject().setStatus(s);
		}
		public int getStatus() {
			return getObject().getStatus();
		}
		/**
		 * Gets or sets the response character encoding.
		 */
		public String getCharacterEncoding() {
			return BA.ReturnString(getObject().getCharacterEncoding());
		}
		public void setCharacterEncoding(String s) {
			getObject().setCharacterEncoding(s);
		}
		/**
		 * Gets or sets the response content type.
		 */
		public void setContentType(String s) {
			getObject().setContentType(s);
		}
		public String getContentType() {
			return BA.ReturnString(getObject().getContentType());
		}
		/**
		 * Sets the response header.
		 */
		public void SetHeader(String Name, String Value) {
			getObject().setHeader(Name, Value);
		}
		/**
		 * Sends a redirect response with the status code 302.
		 *Location - The new address.
		 */
		public void SendRedirect(String Location) throws IOException {
			getObject().sendRedirect(Location);
		}
		/**
		 * Sets the response content length header.
		 */
		public void setContentLength(int i) {
			getObject().setContentLength(i);
		}
		/**
		 * Sends an error message.
		 *StatusCode - Usually 500.
		 *Message - The error message.
		 */
		@RaisesSynchronousEvents
		public void SendError(int StatusCode, String Message) throws IOException {
			try {
				getObject().sendError(StatusCode, Message);
			} catch (Exception e) {
				BA.LogError("Failed to send error.");
				e.printStackTrace();
			}
		}
		/**
		 * Adds a cookie to the response.
		 */
		public void AddCookie(CookieWrapper Cookie) {
			getObject().addCookie(Cookie.getObject());
		}
	}

	
}
