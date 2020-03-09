
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
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

/**
 * ComboBox control. Allows the user to select a value from a dropdown list.
 *If the ComboBox is editable then the user can also write any value instead of selecting a predefined value.
 *SelectedIndexChanged event is raised when the selected index is changed. The Index will be -1 if there is no selection.
 *ValueChanged event is raised when the current value has changed. This event will be raised when the selected item has changed or
 *if the ComboBox is editable then it will be raised when the user has edited the value.
 */
@ShortName("ComboBox")
@DontInheritEvents
@Events(values={"ValueChanged (Value As Object)", "SelectedIndexChanged(Index As Int, Value As Object)"})
public class ComboBoxWrapper extends ControlWrapper<ComboBox<Object>>{

	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new ComboBox<Object>());
		super.innerInitialize(ba, eventName, true);
		getObject().setItems(FXCollections.observableArrayList());
		if (ba.subExists(eventName + "_valuechanged")) {
			getObject().valueProperty().addListener(new ChangeListener<Object>() {

				@Override
				public void changed(ObservableValue<? extends Object> arg0,
						Object arg1, Object arg2) {
					ba.raiseEventFromUI(getObject(), eventName + "_valuechanged", arg2);
				}
			});
		}
		if (ba.subExists(eventName + "_selectedindexchanged")) {
			getObject().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2) {
					Object value = null;
					if (arg2.intValue() >= 0)
						value = getObject().getItems().get(arg2.intValue());
					ba.raiseEventFromUI(getObject(), eventName + "_selectedindexchanged", arg2.intValue(), 
							value);
				}
			});
		}

	}
	/**
	 * Gets or sets the selected index. A value of -1 means that there is no selected item.
	 */
	public int getSelectedIndex() {
		return getObject().getSelectionModel().getSelectedIndex();
	}
	public void setSelectedIndex(int i) {
		getObject().getSelectionModel().select(i);
	}
	
	/**
	 * Gets or sets the current value.
	 */
	public Object getValue() {
		return getObject().getValue();
	}
	public void setValue(Object s) {
		getObject().setValue(s);
	}
	/**
	 * Gets or sets whether the ComboBox is editable. The default value is False.
	 */
	public boolean getEditable() {
		return getObject().isEditable();
	}
	public void setEditable(boolean b) {
		getObject().setEditable(b);
	}
	/**
	 * Returns a List with the items.
	 */
	public List getItems() {
		List l = new List();
		l.setObject(getObject().getItems());
		return l;
	}
	@SuppressWarnings("unchecked")
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		ComboBox<Object> vg = (ComboBox<Object>) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(ComboBox.class, props, designer);
			for (int i = 1;i <= 10;i++) {
				vg.getItems().add("Item #" + i);
			}
		}
		vg.setEditable((Boolean)props.get("editable"));
		return ControlWrapper.build(vg, props, designer);
	}
}
