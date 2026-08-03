
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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.Session.Listener.AutoDemanding;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

/**
 * Implementation of a WebSocket client.
 */
@ShortName("WebSocketClient")
@Events(values={"Connected", "Closed (Reason As String)", "TextMessage (Message As String)", "BinaryMessage (Data() As Byte)"})
@DependsOn(values={
		"jserver/jetty-util-12.1.10.jar", 
		"jserver/jetty-websocket-core-common-12.1.10.jar", 
		"jserver/jetty-websocket-jetty-api-12.1.10.jar", 
		"jserver/jetty-websocket-jetty-common-12.1.10.jar", 
		"jserver/jetty-compression-common-12.1.10.jar", 
		"jserver/jetty-websocket-jetty-client-12.1.10.jar", 
		"jserver/jetty-security-12.1.10.jar", 
		"jserver/jetty-io-12.1.10.jar", 
		"jserver/jetty-client-12.1.10.jar", 
		"jserver/jetty-http-12.1.10.jar", 
		"jserver/slf4j-api-2.0.17.jar",		
		"jserver/jetty-websocket-core-client-12.1.10.jar",		
		"jserver/jetty-slf4j-impl-12.1.10.jar"		
})
@Version(2.50f)
public class WebSocketClientWrapper {
	@Hide
	public BA ba;
	@Hide
	public String eventName;
	@Hide
	public WebSocketClient wsc;
	@Hide
	public Future<Session> session;
	/**
	 * Initializes the object and sets the subs that will handle the events.
	 */
	public void Initialize(BA ba, String EventName) {
		wsc = new WebSocketClient();
		eventName = EventName.toLowerCase(BA.cul);
		this.ba = ba;
	}
	/**
	 * Tries to connect to the given Url. The Url should start with ws:// or wss:// (for SSL)
	 */
	public void Connect(final String Url) throws URISyntaxException {
		Connect2("", new ClientUpgradeRequest(new URI(Url)));
	}
	/**
	 * Similar to Connect. Allows to configure the upgrade request. Pass empty string to Url if configured in the UpgradeRequest.
	 */
	public void Connect2(final String Url, final ClientUpgradeRequest UpgradeRequest) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					wsc.start();
					if (Url.length() > 0) {
						URI echoUri = new URI(Url);
						session = wsc.connect(new WSHandler(WebSocketClientWrapper.this), echoUri, UpgradeRequest);
					} else {
						session = wsc.connect(new WSHandler(WebSocketClientWrapper.this), UpgradeRequest);
					}
				} catch (Exception e) {
					e.printStackTrace();
					//handled on onWebSocketError
				}
			}
		};
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.start();
	}
	/**
	 * Checks whether the connection is open.
	 */
	public boolean getConnected() throws InterruptedException {
		try {
			return session != null && session.isDone() && session.get().isOpen();
		} catch (ExecutionException e) {
			return false;
		}
	}
	/**
	 * Closes the connection.
	 */
	public void Close() throws Exception {
		try {
			if (session != null && session.isDone())
				session.get().close();
		} catch (ExecutionException ee) {
			if (BA.debugMode)
				ee.printStackTrace();
		}
		if (wsc.isRunning()) {
			BA.submitRunnable(new Runnable() {

				@Override
				public void run() {
					try {
						wsc.stop();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}, null, 0);

		}
	}
	
	/**
	 * Same as SendTextAsync. Kept for backward compatibility.
	 */
	public void SendText(String Text) throws InterruptedException, ExecutionException {
		SendTextAsync(Text);
	}
	/**
	 * Asynchronously sends a text message.
	 */
	public void SendTextAsync(String Text) throws InterruptedException, ExecutionException {
		session.get().sendText(Text, Callback.NOOP);
	}
	/**
	 * Same as SendBinaryAsync. Kept for backward compatibility.
	 */
	public void SendBinary(byte[] Data) throws InterruptedException, ExecutionException {
		SendBinaryAsync(Data);
	}
	/**
	 * Asynchronously sends a binary message;
	 */
	public void SendBinaryAsync(byte[] Data) throws InterruptedException, ExecutionException {
		session.get().sendBinary(ByteBuffer.wrap(Data), Callback.NOOP);
	}
}
