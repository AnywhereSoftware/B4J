
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
 
 package anywheresoftware.b4j.objects;
import io.moquette.BrokerConstants;
import io.moquette.broker.AutoFlushHandler;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthenticator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
//Changes in NewNettyAcceptor
@Version(2.02f)
@DependsOn(values={"moquette2"})
@ShortName("MqttBroker")
public class MqttBroker {
	@Hide
	public Server server;
	@Hide
	public IConfig config;
	private boolean logEnabled;
	/**
	 * Initializes the broker and sets the broker port (currently there are no events).
	 */
	public void Initialize(String EventName, int Port) {
		server = new Server();
		config = new MemoryConfig(new Properties());
		config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(Port)); 
		config.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
		AppenderSkeleton appender = new AppenderSkeleton() {
			
			@Override
			public boolean requiresLayout() {
				return false;
			}
			
			@Override
			public void close() {
				
			}
			
			@Override
			protected void append(LoggingEvent arg0) {
				if (logEnabled) {
					BA.Log(String.valueOf(getLayout().format(arg0)));
					if (arg0.getThrowableInformation() != null && arg0.getThrowableInformation().getThrowable() != null) {
						arg0.getThrowableInformation().getThrowable().printStackTrace();
					}
				}
			}
		};
		appender.setLayout(new PatternLayout("%m%n"));
		appender.activateOptions();
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(appender);
//		Logger.getLogger(AutoFlushHandler.class).setLevel(Level.ALL);
		
	}
	/**
	 * The server will only allow connections with the provided user names and password.
	 */
	public void SetUserAndPassword(String Username, String Password) {
		config.setProperty("b4x_user", Username);
		config.setProperty("b4x_password", Password);
		config.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, String.valueOf(false));
		config.setProperty(BrokerConstants.AUTHENTICATOR_CLASS_NAME, B4XAuthenticator.class.getName());
		
	}
	@Hide
	public static class B4XAuthenticator implements IAuthenticator
	{
		final String b4xUsername;
		final String b4xPassword;
		public B4XAuthenticator(IConfig config) {
			b4xUsername = config.getProperty("b4x_user", "");
			b4xPassword = config.getProperty("b4x_password", "");
		}
		@Override
		public boolean checkValid(String clientId, String username,
				byte[] password) {
			try {
				return b4xUsername.equals(username) && b4xPassword.equals(Common.BytesToString(password, 0, password.length, "UTF8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	public void setDebugLog(boolean b) {
		logEnabled = b;
	}
	/**
	 * Starts the server.
	 */
	public void Start() throws IOException {
		server.startServer(config);
	}
	/**
	 * Stops the server.
	 */
	public void Stop() {
		server.stopServer();
	}
	
}
