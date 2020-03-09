
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

import javax.servlet.http.Cookie;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;

/**
 * Represents a HTTP cookie. You can add cookies to the response with ServletResponse.AddCookie.
 *These cookies will then be sent in the following browser requests.
 *You can fetch the cookies with ServletRequest.GetCookies.
 */
@ShortName("Cookie")
public class CookieWrapper extends AbsObjectWrapper<Cookie> {
	/**
	 * Creates a new cookie with the given name and value.
	 */
	public void Initialize(String Name, String Value) {
		setObject(new Cookie(Name, Value));
	}
	/**
	 * Gets or sets the cookie maximum age in seconds.
	 *A negative value means that the cookie will be deleted when the Web browser exists.
	 *A zero value causes the cookie to be deleted.
	 */
	public void setMaxAge(int i) {
		getObject().setMaxAge(i);
	}
	public int getMaxAge() {
		return getObject().getMaxAge();
	}
	/**
	 * Gets or sets whether the cookie is a http only cookie (a cookie that cannot be read by client-side scripts).
	 */
	public void setHttpOnly(boolean b) {
		getObject().setHttpOnly(b);
	}
	public boolean getHttpOnly() {
		return getObject().isHttpOnly();
	}
	/**
	 * Gets the cookie name.
	 */
	public String getName() {
		return getObject().getName();
	}
	/**
	 * Gets or sets the cookie value.
	 */
	public void setValue(String s) {
		getObject().setValue(s);
	}
	public String getValue() {
		return getObject().getValue();
	}
}
