
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;
import anywheresoftware.b4a.objects.streams.File.OutputStreamWrapper;
@Version(1.0f)
@ShortName("Bluetooth")
@Events(values={"DiscoveryFinished",
		"DeviceFound (Name As String, MacAddress As String)", "Connected (Success As Boolean, Connection As BluetoothConnection)"})
@DependsOn(values={"bluecove-2.1.1-SNAPSHOT"})
public class Bluetooth {
	private BA ba;
	private String eventName;
	@Hide
	public LocalDevice device;
	@Hide
	public final ConcurrentHashMap<String, RemoteDevice> devices = new ConcurrentHashMap<String, RemoteDevice>();


	private DiscoveryListener searchForDevices;
	private volatile StreamConnectionNotifier serverListener;
	/**
	 * Initializes the object and sets the subs that will handle the events.
	 */
	public void Initialize(final BA ba, String EventName) throws BluetoothStateException {
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		if (LocalDevice.isPowerOn())
			device = LocalDevice.getLocalDevice();
	}

	public boolean IsInitialized() {
		return device != null;
	}
	/**
	 * Returns true if the Bluetooth adapter is powered on.
	 */
	public boolean getIsEnabled() {
		return LocalDevice.isPowerOn();
	}
	/**
	 * Starts searching for devices. The DeviceFound will be raised for each device.
	 *Note that paired devices will also be listed (even if they are not nearby).
	 *The DiscoveryFinished event will be raised at the end.
	 */
	public boolean StartDiscovery() throws BluetoothStateException {
		searchForDevices = new DiscoveryListener() {

			@Override
			public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
			}

			@Override
			public void serviceSearchCompleted(int transID, int respCode) {
			}

			@Override
			public void inquiryCompleted(int discType) {
				ba.raiseEventFromDifferentThread(Bluetooth.this, null, 0, eventName + "_discoveryfinished", false, null);
			}

			@Override
			public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
				String name = "NA";
				try {
					name = btDevice.getFriendlyName(false);
				}catch (IOException e) {
					e.printStackTrace();
				}
				devices.put(btDevice.getBluetoothAddress(), btDevice);
				ba.raiseEventFromDifferentThread(Bluetooth.this, null, 0, eventName + "_devicefound", false, 
						new Object[] {name, btDevice.getBluetoothAddress()});
			}
		};
		return device.getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC,searchForDevices); 

	}
	/**
	 * Cancels the current discovery process.
	 */
	public boolean CancelDiscovery() {
		if (searchForDevices != null)
			return device.getDiscoveryAgent().cancelInquiry(searchForDevices);
		return false;
	}
	/**
	 * Connects to the device with the given address.
	 *Note that the device must be first found.
	 *The service UUID is set to: 00001101-0000-1000-8000-00805F9B34FB (default SPP).
	 *The Connected event will be raised when the device is connected or if there was a problem to connect.
	 */
	public void Connect(String MacAddress) throws BluetoothStateException {
		Connect2(MacAddress, "00001101-0000-1000-8000-00805F9B34FB");
	}
	/**
	 * Similar to Connect. Allows you to use a different UUID.
	 */
	public void Connect2(String MacAddress, String UUID) throws BluetoothStateException {
		RemoteDevice rd = devices.get(MacAddress);
		if (rd == null)
			throw new RuntimeException("Unknown device. Devices must first be discovered.");
		device.getDiscoveryAgent().searchServices(null, new javax.bluetooth.UUID[] {new javax.bluetooth.UUID(UUID.replace("-", ""), false)}, rd,
				new DiscoveryListener() {
			boolean discovered = false;
			@Override
			public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
				String url = null;
				for (ServiceRecord sr : servRecord) {
					url = sr.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
					if (url != null)
						break;
				}
				if (url != null) {
					try {
						BluetoothConnection bc = new BluetoothConnection();
						bc.connection = (StreamConnection) Connector.open(url);
						ba.raiseEventFromDifferentThread(Bluetooth.this, null, 0, eventName + "_connected", false, new Object[] {true, bc});
						discovered = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void serviceSearchCompleted(int transID, int respCode) {
				if (!discovered) {
					System.out.println("Service search completed. Exit code: " + respCode);
					ba.raiseEventFromDifferentThread(Bluetooth.this, null, 0, eventName + "_connected", false, new Object[] {false, null});
				}
			}

			@Override
			public void inquiryCompleted(int discType) {
			}

			@Override
			public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
			}
		});
	}


	/**
	 * Starts listening for connections.
	 *The service UUID is set to: 00001101-0000-1000-8000-00805F9B34FB
	 *The Connected event will be raised when a client is connected.
	 */
	public void Listen() {
		Listen2("00001101-0000-1000-8000-00805F9B34FB");
	}
	/**
	 * Similar to Listen. Allows you to use a different UUID for the service.
	 */
	public void Listen2(final String UUID) {
		BA.submitRunnable(new Runnable() {

			@Override
			public void run() {
				try {
					StopListening();
					if (device.getDiscoverable() == DiscoveryAgent.NOT_DISCOVERABLE) {
						try {
							if (device.setDiscoverable(DiscoveryAgent.GIAC) == false)
								System.out.println("Failed to set discovery mode.");
						} catch (Exception ee) {
							System.out.println("Failed to set discovery mode.");
							ee.printStackTrace();
						}
					}
					serverListener = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + new javax.bluetooth.UUID(UUID.replace("-", ""), false) + 
							";name=b4j;authenticate=false;encrypt=false;");
					BluetoothConnection bc = new BluetoothConnection();
					bc.connection = serverListener.acceptAndOpen();
					ba.raiseEventFromDifferentThread(Bluetooth.this, null, 0, eventName + "_connected", false, new Object[]{true, bc});
				} catch (Exception e) {
					if (serverListener != null && getServerClosedState() == true)
						return;
					ba.setLastException(e);
					e.printStackTrace();
					ba.raiseEventFromDifferentThread(Bluetooth.this, null, 0, eventName + "_connected", false, new Object[]{false, null});
				}
			}
		}, null, 0);
	}
	/**
	 * Stops listening for incoming connections.
	 */
	public void StopListening() {
		try {
			if (serverListener != null) {
				serverListener.close();
			}
			serverListener = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private boolean getServerClosedState() {
		try {
			Field f = Class.forName("com.intel.bluetooth.BluetoothConnectionNotifierBase").getDeclaredField("closed");
			f.setAccessible(true);
			return (Boolean)f.get(serverListener);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	@ShortName("BluetoothConnection")
	public static class BluetoothConnection {
		@Hide
		public volatile StreamConnection connection;
		private RemoteDevice remoteDevice;
		/**
		 * Closes the connection.
		 */
		public void Disconnect() throws IOException {
			if (connection != null)
				connection.close();
			connection = null;
		}
		public InputStreamWrapper getInputStream() throws IOException {
			return (InputStreamWrapper)AbsObjectWrapper.ConvertToWrapper(new InputStreamWrapper(), connection.openInputStream());
		}
		public OutputStreamWrapper getOutputStream() throws IOException {
			return (OutputStreamWrapper) AbsObjectWrapper.ConvertToWrapper(new OutputStreamWrapper(),connection.openOutputStream());
		}
		public String getName() throws IOException {
			if (remoteDevice == null)
				remoteDevice = RemoteDevice.getRemoteDevice(connection);
			return remoteDevice.getFriendlyName(false);
		}
		public String getMacAddress() throws IOException {
			if (remoteDevice == null)
				remoteDevice = RemoteDevice.getRemoteDevice(connection);
			return remoteDevice.getBluetoothAddress();
		}
	}
}
