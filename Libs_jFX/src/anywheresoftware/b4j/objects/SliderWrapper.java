
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

import java.util.HashMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

/**
 * A slider control that allows the user to choose a value by dragging the "thumb".
 *The default minimum and maximum values are 0 and 100.
 */
@DontInheritEvents
@ShortName("Slider")
@Events(values={"ValueChange (Value As Double)"})
public class SliderWrapper extends ControlWrapper<Slider>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new Slider());
		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_valuechange")) {
			getObject().valueProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2) {
					ba.raiseEventFromUI(getObject(), eventName + "_valuechange", arg2.doubleValue());
					
				}
			});
		}
	}
	/**
	 * Gets or sets the slider value.
	 */
	public double getValue() {
		return getObject().getValue();
	}
	public void setValue(double d) {
		getObject().setValue(d);
	}
	/**
	 * Gets or sets the minimum value. The default value is 0.
	 */
	public double getMinValue() {
		return getObject().getMin();
	}
	public void setMinValue(double d) {
		getObject().setMin(d);
	}
	/**
	 * Gets or sets the maximum value. The default value is 100.
	 */
	public double getMaxValue() {
		return getObject().getMax();
	}
	public void setMaxValue(double d) {
		getObject().setMax(d);
	}
	/**
	 * Gets or sets the slider orientation. The default value is False (horizontal).
	 */
	public boolean getVertical() {
		return getObject().getOrientation() == Orientation.VERTICAL;
	}
	public void setVertical(boolean b) {
		getObject().setOrientation(b ? Orientation.VERTICAL : Orientation.HORIZONTAL);
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		Slider vg = (Slider) prev;
		if (vg == null) 
			vg = NodeWrapper.buildNativeView(Slider.class, props, designer);
		vg.setMin((Float)props.get("min"));
		vg.setMax((Float)props.get("max"));
		vg.setValue((Float)props.get("value"));
		vg.setOrientation((Boolean)props.get("vertical") ? Orientation.VERTICAL : Orientation.HORIZONTAL);
		return ControlWrapper.build(vg, props, designer);
	}
}
