
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

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

/**
 * Implementation of a MQTT client.
 */
@ShortName("MqttClient")
@Events(values={"Connected (Success As Boolean)", "Disconnected", "MessageArrived (Topic As String, Payload() As Byte)"})
@Permissions(values={"android.permission.INTERNET"})
@Version(1.01f)
public class MqttAsyncClientWrapper{
	public static final int QOS_0_MOST_ONCE = 0;
	public static final int QOS_1_LEAST_ONCE = 1;
	public static final int QOS_2_EXACTLY_ONCE = 2;
	@Hide
	public MqttAsyncClient client;
	private BA ba;
	private String eventName;
	
	public boolean IsInitialized() {
		return client != null;
	}
	/**
	 * Initializes the client.
	 *EventName - Sets the subs that will handle the events.
	 *ServerURI - The server URI. For example: tcp://localhost:51044 or ssl://localhost:51044
	 *ClientId - Client ID (each client needs a unique id).
	 */
	public void Initialize(final BA ba, String EventName, String ServerURI, String ClientId) throws MqttException {
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		client = new MqttAsyncClient(ServerURI, ClientId, new MemoryPersistence());
		client.setCallback(new MqttCallback() {
			
			@Override
			public void messageArrived(String topic, MqttMessage message)
					throws Exception {
				ba.raiseEventFromDifferentThread(client, null, 0, eventName + "_messagearrived", false, 
						new Object[] {topic, message.getPayload()});
			}
			
			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
			}
			
			@Override
			public void connectionLost(Throwable cause) {
				setThrowable(cause);
				ba.raiseEventFromDifferentThread(client, null, 0, eventName + "_disconnected", false, null);
				
			}
		});
	}
	/**
	 * Tries to connect to the broker. The Connected event will be raised.
	 */
	public void Connect() throws MqttSecurityException, MqttException {
		Connect2(new MqttConnectOptions());
	}
	/**
	 * Similar to Connect. Allows you to configure the connection options.
	 */
	public void Connect2(MqttConnectOptions Options) throws MqttSecurityException, MqttException {
		client.connect(Options, null, new IMqttActionListener() {
			
			@Override
			public void onSuccess(IMqttToken asyncActionToken) {
				ba.raiseEventFromDifferentThread(client, null, 0, eventName + "_connected", false, new Object[] {true});
			}
			
			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				setThrowable(exception);
				ba.raiseEventFromDifferentThread(client, null, 0, eventName + "_connected", false, new Object[] {false});
				
			}
		});
	}
	/**
	 * Returns true if the client is connected.
	 */
	public boolean getConnected() {
		return client != null && client.isConnected();
	}
	/**
	 * Gets the client id.
	 */
	public String getClientId() {
		return client.getClientId();
	}
	/**
	 * Subscribes to the given topic.
	 *Topic - Topic to subscribe.
	 *QOS - QOS setting.
	 */
	public void Subscribe(String Topic, int QOS) throws MqttException {
		client.subscribe(Topic, QOS);
	}
	/**
	 * Unsubscribes from the given topic.
	 */
	public void Unsubscribe(String Topic) throws MqttException {
		client.unsubscribe(Topic);
	}
	/**
	 * Publishes a message to the given topic. The QOS will be set to 1.
	 *Topic - The message will be delivered to this topic.
	 *Payload - Message payload.
	 *For example:<code>
	 *Client.Publish("Topic1", "Message".GetBytes("UTF8"))</code>
	 */
	public void Publish(String Topic, byte[] Payload) throws MqttPersistenceException, MqttException {
		Publish2(Topic, Payload, 1, false);
	}
	/**
	 * Publishes a message to the given topic.
	 *Topic - The message will be delivered to this topic.
	 *Payload - Message payload.
	 *QOS - The QOS level.
	 *Retained - Whether the server should retain the message (only the last message per topic is retained).
	 */
	public void Publish2(String Topic, byte[] Payload, int QOS, boolean Retained) throws MqttPersistenceException, MqttException {
		client.publish(Topic, Payload, QOS, Retained);
	}
	/**
	 * Asynchronously closes the connection.
	 */
	public void Close() throws MqttException {
		if (client == null)
			return;
		BA.submitRunnable(new Runnable() {
			
			@Override
			public void run() {
				try {
					client.disconnectForcibly();
					client.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				client = null;
			}
		}, null, 0);
	
	}
	private void setThrowable(Throwable t) {
		if (t instanceof Exception)
			ba.setLastException((Exception)t);
	}
	@ShortName("MqttConnectOptions")
	public static class MqttConnectOptionsWrapper extends AbsObjectWrapper<MqttConnectOptions> {
		/**
		 * Initializes the object and sets the username and password.
		 *Pass empty strings if username or password are not required.
		 */
		public void Initialize(String UserName, String Password) {
			setObject(new MqttConnectOptions());
			if (UserName.length() > 0)
				setUserName(UserName);
			if (Password.length() > 0)
				setPassword(Password);
		}
		/**
		 * Gets or sets the connection password.
		 */
		public String getPassword() {
			return new String(getObject().getPassword());
		}
		public void setPassword(String s) {
			getObject().setPassword(s.toCharArray());
		}
		/**
		 * Gets or sets the connection user name.
		 */
		public String getUserName() {
			return getObject().getUserName();
		}
		public void setUserName(String s) {
			getObject().setUserName(s);
		}
		/**
		 * If set to true (default value) then the state will not be preserved in the case of client restarts.
		 */
		public boolean getCleanSession() {
			return getObject().isCleanSession();
		}
		public void setCleanSession(boolean b) {
			getObject().setCleanSession(b);
		}
		/**
		 * Sets the Last Will message that will be sent if the client was disconnected abruptly.
		 */
		public void SetLastWill(String Topic, byte[] Payload, int QOS, boolean Retained) {
			getObject().setWill(Topic, Payload, QOS, Retained);
		}
	}
}
