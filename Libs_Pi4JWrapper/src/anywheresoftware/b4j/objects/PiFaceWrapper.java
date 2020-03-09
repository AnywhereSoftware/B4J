
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

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;

import com.pi4j.component.light.LED;
import com.pi4j.component.relay.Relay;
import com.pi4j.component.switches.Switch;
import com.pi4j.component.switches.SwitchListener;
import com.pi4j.component.switches.SwitchState;
import com.pi4j.component.switches.SwitchStateChangeEvent;
import com.pi4j.device.piface.PiFace;
import com.pi4j.device.piface.impl.PiFaceDevice;

@ShortName("PiFace")
public class PiFaceWrapper extends AbsObjectWrapper<PiFace>{
	/**
	 * Initializes the PiFace object.
	 *Example:<code>
	 *pf.Initialize(0x40, 0)</code>
	 */
	public void Initialize(byte SpiAddress, int SpiChannel) throws IOException {
		setObject(new PiFaceDevice(SpiAddress, SpiChannel));
	}
	/**
	 * Gets a reference to a Switch. Index should be between 0 to 3.
	 */
	public SwitchWrapper GetSwitch(int Index) {
		return (SwitchWrapper)AbsObjectWrapper.ConvertToWrapper(new SwitchWrapper(), getObject().getSwitch(Index));
	}
	/**
	 * Gets a reference to a LED. Index should be between 0 to 7.
	 */
	public LEDWrapper GetLED(int Index) {
		return (LEDWrapper)AbsObjectWrapper.ConvertToWrapper(new LEDWrapper(), getObject().getLed(Index));
	}
	/**
	 * Gets a reference to a Relay. Index should be between 0 to 1.
	 */
	public RelayWrapper GetRelay(int Index) {
		return (RelayWrapper)AbsObjectWrapper.ConvertToWrapper(new RelayWrapper(), getObject().getRelay(Index));
	}
	
	
	@ShortName("Switch")
	@Events(values={"StateChange(State As Boolean)"})
	public static class SwitchWrapper extends AbsObjectWrapper<Switch> {
		/**
		 * Gets the switch state. True = On, False = Off.
		 */
		public boolean getState() {
			return getObject().isOn();
		}
		/**
		 * Adds an event listener.
		 */
		public void AddListener(final BA ba, String EventName) {
			final String eventName = EventName.toLowerCase(BA.cul);
			final Object sender = getObject();
			getObject().addListener(new SwitchListener() {

				@Override
				public void onStateChange(SwitchStateChangeEvent event) {
					ba.raiseEventFromDifferentThread(sender, null, 0, eventName + "_statechange", false, new Object[] {
							event.getNewState() == SwitchState.ON});
				}
				
			});
		}
	}
	@ShortName("LED")
	public static class LEDWrapper extends AbsObjectWrapper<LED> {
		/**
		 * Gets or sets the LED state. True = On, False = Off.
		 */
		public boolean getState() {
			return getObject().isOn();
		}
		public void setState(boolean b) {
			if (b)
				getObject().on();
			else
				getObject().off();
		}
	}
	@ShortName("Relay")
	public static class RelayWrapper extends AbsObjectWrapper<Relay> {
		/**
		 * Gets or sets the relay state. True = Closed, False = Open.
		 */
		public boolean getState() {
			return getObject().isClosed();
		}
		public void setState(boolean b) {
			if (b)
				getObject().close();
			else
				getObject().open();
		}
	}
	
}
