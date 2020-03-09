
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
 
 package org.eclipse.paho.client.mqttv3;

/**
 * Implementors of this interface will be notified when an asynchronous action completes.
 * 
 * <p>A listener is registered on an MqttToken and a token is associated
 * with an action like connect or publish. When used with tokens on the MqttAsyncClient 
 * the listener will be called back on the MQTT client's thread. The listener will be informed 
 * if the action succeeds or fails. It is important that the listener returns control quickly 
 * otherwise the operation of the MQTT client will be stalled.
 * </p>  
 */
public interface IMqttActionListener {
	/**
	 * This method is invoked when an action has completed successfully.  
	 * @param asyncActionToken associated with the action that has completed
	 */
	public void onSuccess(IMqttToken asyncActionToken );
	/**
	 * This method is invoked when an action fails.  
	 * If a client is disconnected while an action is in progress 
	 * onFailure will be called. For connections
	 * that use cleanSession set to false, any QoS 1 and 2 messages that 
	 * are in the process of being delivered will be delivered to the requested
	 * quality of service next time the client connects.  
	 * @param asyncActionToken associated with the action that has failed
	 */
	public void onFailure(IMqttToken asyncActionToken, Throwable exception);
}
