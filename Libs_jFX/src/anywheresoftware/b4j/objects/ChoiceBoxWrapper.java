
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
import javafx.scene.control.ChoiceBox;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

/**
 * ChoiceBox is a simplified version of ComboBox.
 *It is optimized for a small number of options.
 *It can only show string items.
 */
@ShortName("ChoiceBox")
@DontInheritEvents
@Events(values={"SelectedIndexChanged(Index As Int, Value As Object)"})
public class ChoiceBoxWrapper extends ControlWrapper<ChoiceBox<Object>>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new ChoiceBox<Object>());
		super.innerInitialize(ba, eventName, true);
		getObject().setItems(FXCollections.observableArrayList());
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
	 * Returns a List with the items.
	 */
	public List getItems() {
		List l = new List();
		l.setObject(getObject().getItems());
		return l;
	}
	/**
	 * Opens the list of choices.
	 */
	public void ShowChoices() {
		getObject().show();
	}
	/**
	 * Closes the list of choices.
	 */
	public void HideChoices() {
		getObject().hide();
	}
	@Hide
	@SuppressWarnings("unchecked")
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		ChoiceBox<Object> vg = (ChoiceBox<Object>) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(ChoiceBox.class, props, designer);
			for (int i = 1;i <= 10;i++) {
				vg.getItems().add("Item #" + i);
			}
		}
		return ControlWrapper.build(vg, props, designer);
	}
}
