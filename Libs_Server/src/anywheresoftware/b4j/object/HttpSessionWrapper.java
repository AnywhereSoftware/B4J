
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

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;

/**
 * HttpSessions allows you to identify a user across multiple requests and to store data specific to this user.
 *The session id is stored as a cookie in the user browser, or as a URL parameter if cookies are disabled.
 *The data is stored in the server memory.
 *Getting and creating sessions is done with ServletRequest.GetSession.
 */
@ShortName("HttpSession")
public class HttpSessionWrapper extends AbsObjectWrapper<HttpSession> {
	/**
	 * Returns the session unique id.
	 */
	public String getId() {
		return getObject().getId();
	}
	/**
	 * Invalidates the session. This causes the session to be deleted.
	 */
	public void Invalidate() {
		getObject().invalidate();
	}
	/**
	 * Returns the last time the user sent a request associated with this session.
	 */
	public long getLastAccessedTime() {
		return getObject().getLastAccessedTime();
	}
	/**
	 * Returns the session creation time.
	 */
	public long getCreationTime() {
		return getObject().getCreationTime();
	}
	/**
	 * Returns the value tied to the attribute with the specified name.
	 */
	public Object GetAttribute(String Name) {
		return getObject().getAttribute(Name);
	}
	/**
	 * Returns the value tied to the attribute with the specified name.
	 *Returns the DefaultValue if no value exists.
	 */
	public Object GetAttribute2(String Name, Object DefaultValue) {
		Object o = getObject().getAttribute(Name);
		return o == null ? DefaultValue : o;
	}
	/**
	 * Adds an attribute to the session. If there is an existing value with the same name then it will be replaced.
	 */
	public void SetAttribute(String Name, Object Value) {
		getObject().setAttribute(Name, Value);
	}
	/**
	 * Removes the attribute with the given name.
	 */
	public void RemoveAttribute(String Name) {
		getObject().removeAttribute(Name);
	}
	/**
	 * Returns a List with all the session names.
	 */
	public List GetAttributesNames() {
		List l = new List();
		l.Initialize();
		Enumeration<String> e = getObject().getAttributeNames();
		while (e.hasMoreElements())
			l.Add(e.nextElement());
		return l;
	}
	/**
	 * Gets or sets the maximum inactive interval, in seconds. The session will be invalidated if there was not any activity for a period larger than this value.
	 *The default value is -1 which means that there is no maximum interval.
	 */
	public void setMaxInactiveInterval(int i) {
		getObject().setMaxInactiveInterval(i);
	}
	public int getMaxInactiveInterval() {
		return getObject().getMaxInactiveInterval();
	}
	/**
	 * Returns true if the session was created in this request.
	 */
	public boolean getIsNew() {
		return getObject().isNew();
	}
	/**
	 * Tests whether there is a value tied to the given name.
	 */
	public boolean HasAttribute(String Name) {
		return getObject().getAttribute(Name) != null;
	}
	
}
