
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
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4j.objects.NodeWrapper.ButtonBaseWrapper;

/**
 * Checkbox control. The CheckedChange event will be raised when the Checkbox is checked or unchecked.
 */
@ShortName("CheckBox")
@DontInheritEvents
@Events(values={"CheckedChange(Checked As Boolean)"})
public class CheckboxWrapper extends ButtonBaseWrapper<CheckBox>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new CheckBox());
		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_checkedchange")) {
			getObject().selectedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> arg0,
						Boolean arg1, Boolean arg2) {
					ba.raiseEventFromUI(getObject(), eventName + "_checkedchange", arg2.booleanValue());
				}
			});
		}
	}
	/**
	 * Gets or sets the checked state.
	 */
	public boolean getChecked() {
		return getObject().isSelected();
	}
	public void setChecked(boolean b) {
		getObject().setSelected(b);
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		CheckBox vg = (CheckBox) prev;
		if (vg == null) 
			vg = NodeWrapper.buildNativeView(CheckBox.class, props, designer);
		vg.setSelected((Boolean)props.get("checked"));
		return LabeledWrapper.build(vg, props, designer);
	}
}
