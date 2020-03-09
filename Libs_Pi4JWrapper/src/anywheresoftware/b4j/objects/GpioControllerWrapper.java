
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

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;

import com.pi4j.gpio.extension.piface.PiFaceGpioProvider;
import com.pi4j.gpio.extension.piface.PiFacePin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

@Version(2.00f)
@ShortName("GpioController")
@DependsOn(values={"pi4j-core", "pi4j-device", "pi4j-gpio-extension"})
public class GpioControllerWrapper extends AbsObjectWrapper<GpioController>{
	private static boolean piFace;
	@Hide
	public static Pin pinFromNumber(int n, boolean output) {
		try {
			Field f;
			if (!piFace)
				f = RaspiPin.class.getField("GPIO_" + Common.NumberFormat2(n, 2, 0, 0, false));
			else
				f = PiFacePin.class.getField((output ? "OUTPUT_" : "INPUT_") + Common.NumberFormat2(n, 2, 0, 0, false));
			return (Pin)f.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
	}
	/**
	 * Initializes the controller.
	 */
	public void Initialize() {
		
		setObject(GpioFactory.getInstance());
	}
	/**
	 * Initializes the PiFace controller.
	 *Example:<code>
	 *controller.InitializePiFace(0x40, 0)</code>
	 */
	public void InitializePiFace(byte SpiAddress, int SpiChannel) throws IOException {
		piFace = true;
		GpioFactory.setDefaultProvider(new PiFaceGpioProvider(SpiAddress, SpiChannel));
	}
	/**
	 * Shuts down the controller and removes all listeners.
	 */
	public void Shutdown() {
		getObject().shutdown();
	}
	@ShortName("GpioPinAnalogInput")
	public static class GpioPinAnalogInputWrapper extends AbsObjectWrapper<GpioPinAnalogInput> {
		public void Initialize(int PinNumber) {
			setObject(GpioFactory.getInstance().provisionAnalogInputPin(pinFromNumber(PinNumber, false)));
		}
		public double getValue() {
			return getObject().getValue();
		}
	}
	@Hide
	public static class GpioPinDigitalWrapper<T extends GpioPinDigital> extends AbsObjectWrapper<T> {
		/**
		 * Sets the pin pull resistance.
		 *Resistance - One of the following string values: OFF, PULL_UP, PULL_DOWN. 
		 */
		public void SetPinPullResistance(PinPullResistance Resistance) {
			getObject().setPullResistance(Resistance);
		}
		/**
		 * Gets or sets the pin's friendly name.
		 */
		public String getName() {
			return BA.ReturnString(getObject().getName());
		}
		public void setName(String s) {
			getObject().setName(s);
		}
		/**
		 * Configures the settings which will be set when the controller is shutdown.
		 *Resistance should be one of the following values: OFF, PULL_UP, PULL_DOWN. 
		 */
		public void SetShutdownOptions(boolean Unexport, boolean State, PinPullResistance Resistance) {
			getObject().setShutdownOptions(Unexport, State ? PinState.HIGH : PinState.LOW, Resistance);
		}
	}
	
	@ShortName("GpioPinDigitalOutput")
	public static class GpioPinDigitalOutputWrapper extends GpioPinDigitalWrapper<GpioPinDigitalOutput> {
		/**
		 * Initializes the pin and provision it to a digital output pin.
		 *PinNumber - 1-29.
		 *State - True for high, False for low.
		 */
		public void Initialize(int PinNumber, boolean State) {
			setObject(GpioFactory.getInstance().provisionDigitalOutputPin(pinFromNumber(PinNumber, true), State ? PinState.HIGH : PinState.LOW));
		}
		/**
		 * Gets or sets the pin state.
		 */
		public void setState(boolean b) {
			getObject().setState(b);
		}
		public boolean getState() {
			return getObject().getState() == PinState.HIGH;
		}
		/**
		 *The pin state will be set to low for the specified interval (measured in milliseconds).
		 */
		public void Blink(long Delay) {
			getObject().blink(Delay);
		}
		/**
		 *The pin state will be set to high for the specified interval (measured in milliseconds).
		 */
		public void Pulse(long Delay) {
			getObject().pulse(Delay);
		}
		/**
		 * Changes the pin mode to input and returns a new GpioPinDigitalInput object.
		 *Note that for the StateChange event to work the pin must have been first initialized as an input pin.
		 */
		public GpioPinDigitalInputWrapper ChangeToInput() {
			getObject().setMode(PinMode.DIGITAL_OUTPUT);
			return (GpioPinDigitalInputWrapper)AbsObjectWrapper.ConvertToWrapper(new GpioPinDigitalInputWrapper(), getObject());
		}
	}
	@ShortName("GpioPinDigitalInput")
	@Events(values={"StateChange(State As Boolean)"})
	public static class GpioPinDigitalInputWrapper extends GpioPinDigitalWrapper<GpioPinDigitalInput> {
		/**
		 * Initializes the pin and provisions it to be a digital input pin.
		 *EventName - Sets the sub that will handle the StateChange event.
		 *PinNumber - 1-29.
		 */
		public void Initialize(final BA ba, String EventName, int PinNumber) {
			GpioPinDigitalInput pin = GpioFactory.getInstance().provisionDigitalInputPin(pinFromNumber(PinNumber, false));	
			
			setObject(pin);
			final String eventName = EventName.toLowerCase(BA.cul);
			final Object sender = getObject();
			if (ba.subExists(eventName + "_statechange")) {
				getObject().addListener(new GpioPinListenerDigital () {

					@Override
					public void handleGpioPinDigitalStateChangeEvent(
							GpioPinDigitalStateChangeEvent event) {
						ba.raiseEventFromDifferentThread(sender, null, 0, eventName + "_statechange", false, new Object[] {
								event.getState() == PinState.HIGH});
					}
					
				});
			}
		}
		/**
		 * Gets the current state.
		 */
		public boolean getState() {
			return getObject().getState() == PinState.HIGH;
		}
		/**
		 * Changes the pin mode to output and returns a new GpioPinDigitalOutput object.
		 */
		public GpioPinDigitalOutputWrapper ChangeToOutput() {
			getObject().setMode(PinMode.DIGITAL_OUTPUT);
			return (GpioPinDigitalOutputWrapper)AbsObjectWrapper.ConvertToWrapper(new GpioPinDigitalOutputWrapper(), getObject());
		}
	}

}
