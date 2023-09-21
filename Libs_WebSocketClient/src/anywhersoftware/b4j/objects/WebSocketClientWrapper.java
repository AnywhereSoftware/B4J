
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
 
 package anywhersoftware.b4j.objects;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
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
@DependsOn(values={"jetty_b4j"})
@Version(1.13f)
public class WebSocketClientWrapper {
	private BA ba;
	private String eventName;
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
	public void Connect(final String Url) {
		Connect2(Url, new ClientUpgradeRequest());
	}
	/**
	 * Similar to Connect. Allows to configure the upgrade request.
	 */
	public void Connect2(final String Url, final ClientUpgradeRequest UpgradeRequest) {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					wsc.start();
					URI echoUri = new URI(Url);
					session = wsc.connect(new WSHandler(), echoUri, UpgradeRequest);
				} catch (Exception e) {
					
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
		if (session != null && session.isDone())
			session.get().close();
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
	 * <b>Do not use this method.</b> Use SendTextAsync instead.
	 */
	public void SendText(String Text) throws IOException, InterruptedException, ExecutionException {
		session.get().getRemote().sendString(Text);
	}
	/**
	 * Asynchronously sends a text message.
	 */
	public void SendTextAsync(String Text) throws InterruptedException, ExecutionException {
		session.get().getRemote().sendString(Text, null);
	}
	/**
	 * Asynchronously sends a binary message;
	 */
	public void SendBinaryAsync(byte[] Data) throws InterruptedException, ExecutionException {
		session.get().getRemote().sendBytes(ByteBuffer.wrap(Data), null);
	}
	/**
	 * <b>Do not use this method.</b> Use SendBinaryAsync instead.
	 */
	public void SendBinary(byte[] Data) throws IOException, InterruptedException, ExecutionException {
		session.get().getRemote().sendBytes(ByteBuffer.wrap(Data));
	}
	@Hide
	public class WSHandler extends WebSocketAdapter {
		@Override
		public void onWebSocketConnect(Session sess) {
			super.onWebSocketConnect(sess);
			ba.raiseEventFromDifferentThread(WebSocketClientWrapper.this, null, 0, eventName + "_connected", false, null);
		}
		@Override
		public void onWebSocketClose(int statusCode, String reason)
		{
			super.onWebSocketClose(statusCode, reason);
			ba.raiseEventFromDifferentThread(WebSocketClientWrapper.this, null, 0, eventName + "_closed", false, 
				new Object[] {BA.ReturnString(reason)});
		}
		@Override
		public void onWebSocketError(Throwable cause) {
			super.onWebSocketError(cause);
			if (BA.debugMode)
				cause.printStackTrace();
			try {
				Close();
				onWebSocketClose(0, cause.getMessage());
			} catch (Exception e) {
				if (BA.debugMode)
					e.printStackTrace();
			}
		
		}
		@Override
		public void onWebSocketText(String message) {
			ba.raiseEventFromDifferentThread(WebSocketClientWrapper.this, null, 0, eventName + "_textmessage",
					false, new Object[] {message});
		}
		@Override
	    public void onWebSocketBinary(byte[] payload, int offset, int len)
	    {
			ba.raiseEventFromDifferentThread(WebSocketClientWrapper.this, null, 0, eventName + "_binarymessage",
					false, new Object[] {payload});
	    }
		
	}
}
