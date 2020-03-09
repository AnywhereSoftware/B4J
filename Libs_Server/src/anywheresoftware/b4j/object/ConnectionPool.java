
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

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4j.objects.SQL;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Maintains a pool of database connections.
 */
@ShortName("ConnectionPool")
@Events(values={"ConnectionReady (Success As Boolean, SQL As SQL)"})
public class ConnectionPool extends AbsObjectWrapper<ComboPooledDataSource> {
	/**
	 * Initializes the pool.
	 *DriverClass - The JDBC driver class.
	 *JdbcUrl - JDBC connection url.
	 *User / Password - Connection credentials.
	 */
	public void Initialize(String DriverClass, String JdbcUrl, String User, String Password) throws PropertyVetoException {
		final ComboPooledDataSource pool = new ComboPooledDataSource();
		setObject(pool);
		pool.setDriverClass(DriverClass);
		pool.setJdbcUrl(JdbcUrl);
		pool.setUser(User);
		pool.setPassword(Password);
		pool.setMaxStatements(150);
		pool.setMaxIdleTime(1800);
		pool.setIdleConnectionTestPeriod(600);
		pool.setCheckoutTimeout(20000);
		pool.setTestConnectionOnCheckout(true);
	}
	/**
	 * Retrieves a connection from the pool. Make sure to close the connection when you are done with it.
	 */
	public SQL GetConnection() throws SQLException {
		SQL s = new SQL();
		s.connection = getObject().getConnection();
		return s;
	}
	/**
	 * Asynchronously gets a SQL connection. This method is useful in UI applications as it prevents the main thread from freezing until the connection is available.
	 *The ConnectionReady event will be raised when the connection is ready.
	 */
	public void GetConnectionAsync(final BA ba, final String EventName) {
		BA.runAsync(ba, this, EventName.toLowerCase(BA.cul) + "_connectionready", new Object[] {false, null}, new Callable<Object[]>() {

			@Override
			public Object[] call() throws Exception {
				return new Object[] {true, GetConnection()};
			}
			
		});
	}
	public void ClosePool() {
		if (IsInitialized())
			getObject().close();
	}
}
