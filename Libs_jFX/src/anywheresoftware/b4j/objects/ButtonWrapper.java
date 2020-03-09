
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
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.NodeWrapper.ButtonBaseWrapper;

@ShortName("Button")
public class ButtonWrapper extends ButtonBaseWrapper<Button>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new Button());
		super.innerInitialize(ba, eventName, true);
	}
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		Button vg = (Button) prev;
		if (vg == null) 
			vg = NodeWrapper.buildNativeView(Button.class, props, designer);
		return LabeledWrapper.build(vg, props, designer);
	}
	
	@ShortName("ToggleButton")
	@DontInheritEvents
	@Events(values={"SelectedChange(Selected As Boolean)"})
	public static class ToggleButtonWrapper extends ButtonBaseWrapper<ToggleButton> {
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new ToggleButton());
			super.innerInitialize(ba, eventName, true);
			if (ba.subExists(eventName + "_selectedchange")) {
				getObject().selectedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> arg0,
							Boolean arg1, Boolean arg2) {
						ba.raiseEventFromUI(getObject(), eventName + "_selectedchange", arg2.booleanValue());
					}
				});
			}
		}
		/**
		 * Gets or sets whether the button is selected (pressed).
		 */
		public boolean getSelected() {
			return getObject().isSelected();
		}
		public void setSelected(boolean b) {
			getObject().setSelected(b);
		}
		public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
			ToggleButton vg = (ToggleButton) prev;
			if (vg == null) 
				vg = NodeWrapper.buildNativeView(ToggleButton.class, props, designer);
			vg.setSelected((Boolean)props.get("selected"));
			return LabeledWrapper.build(vg, props, designer);
		}
	}
	/**
	 * RadioButtons can be either selected or not selected. When the RadioButtons are grouped then only one item can be selected at any time.
	 *Call GroupRadioButtons to group a list of RadioButtons.
	 */
	@ShortName("RadioButton")
	@DontInheritEvents
	@Events(values={"SelectedChange(Selected As Boolean)"})
	public static class RadioButtonWrapper extends ButtonBaseWrapper<RadioButton> {
		@Hide
		public static HashMap<String, ToggleGroup> automaticGroups = new HashMap<String, ToggleGroup>();
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new RadioButton());
			super.innerInitialize(ba, eventName, true);
			if (ba.subExists(eventName + "_selectedchange")) {
				getObject().selectedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0,
							Boolean arg1, Boolean arg2) {
						ba.raiseEventFromUI(getObject(), eventName + "_selectedchange", arg2.booleanValue());
					}
				});
			}
		}
		/**
		 * Gets or sets whether the button is selected (pressed).
		 */
		public boolean getSelected() {
			return getObject().isSelected();
		}
		public void setSelected(boolean b) {
			getObject().setSelected(b);
		}
		/**
		 * Creates a group from the RadioButtons that are stored in the list.
		 *Only one of the group buttons can be selected at any time.
		 *Note that RadioButtons added with the internal designer are grouped based on their parent.
		 */
		public static void GroupRadioButtons(List RadioButtons) {
			ToggleGroup tg = new ToggleGroup();
			for (Object rb : RadioButtons.getObject()) {
				((RadioButton)rb).setToggleGroup(tg);
			}
		}
		@Hide
		public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
			RadioButton vg = (RadioButton) prev;
			if (vg == null) 
				vg = NodeWrapper.buildNativeView(RadioButton.class, props, designer);
			vg.setSelected((Boolean)props.get("selected"));
			String parentName = (String)props.get("parent");
			ToggleGroup tg = automaticGroups.get(parentName);
			if (tg == null) {
				tg = new ToggleGroup();
				automaticGroups.put(parentName, tg);
			}
			vg.setToggleGroup(tg);
			return LabeledWrapper.build(vg, props, designer);
		}
	}
}
