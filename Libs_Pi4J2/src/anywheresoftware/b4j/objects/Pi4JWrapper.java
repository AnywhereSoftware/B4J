package anywheresoftware.b4j.objects;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import com.pi4j.io.gpio.digital.PullResistance;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

@Version(1.0f)
@ShortName("Pi4J")
@DependsOn(values= {"pi4j-plugin-raspberrypi-2.1.0", "pi4j-core-2.1.0", "slf4j-api-1.7.30", "slf4j-jdk14-1.7.25",
		"pi4j-plugin-pigpio-2.1.0", "pi4j-library-pigpio"})
public class Pi4JWrapper extends AbsObjectWrapper<Context>{
	public void Initialize(String EventName) {
		setObject(Pi4J.newAutoContext());
	}
	
	@ShortName("DigitalInput")
	@Events(values = {"StateChange (State As Boolean)"})
	public static class DigitalInputWrapper extends AbsObjectWrapper<DigitalInput> {
		/**
		 * Initializes the DigitalInput.
		 *Pi4J - Pi4J object.
		 *EventName - Sets the sub that will handle the StateChange event.
		 *Pin - Pin BCM address.
		 *PullResistance - OFF, PULL_UP or PULL_DOWN
		 *DebounceMicroSeconds - Debounce duration in microseconds.
		 */
		public void Initialize(BA ba, Pi4JWrapper Pi4J, String EventName, int Pin, String PullResistance, long DebounceMicroSeconds) {
			DigitalInputConfigBuilder builder = DigitalInput.newConfigBuilder(Pi4J.getObject());
			builder.address(Pin);
			builder.debounce(DebounceMicroSeconds).pull(Enum.valueOf(PullResistance.class, PullResistance)).provider("pigpio-digital-input");
			Intiailize2(ba, Pi4J, EventName, builder);
		}
		public void Intiailize2(BA ba, Pi4JWrapper Pi4J, String EventName, DigitalInputConfigBuilder ConfigurationBuilder) {
			final DigitalInput di = Pi4J.getObject().create(ConfigurationBuilder);
			setObject(di);
			final String eventName = EventName.toLowerCase(BA.cul);
			getObject().addListener(new DigitalStateChangeListener() {
				
				@Override
				public void onDigitalStateChange(@SuppressWarnings("rawtypes") DigitalStateChangeEvent event) {
					ba.raiseEventFromDifferentThread(di, null, 0, eventName + "_statechange", false, new Object[] {
							event.state() == DigitalState.HIGH});
				}
			});
		}
		/**
		 * Gets the current state.
		 */
		public boolean getState() {
			return getObject().state() == DigitalState.HIGH;
		}
	}
	@ShortName("DigitalOutput")
	public static class DigitalOutputWrapper extends AbsObjectWrapper<DigitalOutput> {
		/**
		 * Initializes the DigitalOutput.
		 *Pi4J - Pi4J object.
		 *Pin - Pin BCM address.
		 */
		public void Initialize(BA ba, Pi4JWrapper Pi4J, int Pin) {
			var buttonConfig = DigitalOutput.newConfigBuilder(Pi4J.getObject())
		      .address(Pin)
		      .shutdown(DigitalState.LOW)
		      .initial(DigitalState.LOW)
		      .provider("pigpio-digital-output");
			setObject(Pi4J.getObject().create(buttonConfig));
		}
		/**
		 * Gets or sets Sets the pin state.
		 */
		public void setState(boolean s) {
			getObject().setState(s);
		}
		public boolean getState() {
			return getObject().state() == DigitalState.HIGH;
		}
	}
}
