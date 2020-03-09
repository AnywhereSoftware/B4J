
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
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;


/**
 * A control that shows a range of numbers or string items. The user can switch between the values with the up and down arrows.
 *You can use SetNumericItems to set the range of numbers or SetListItems to explicitly set the items (can be text or numbers).
 *If the spinner is editable then the user can manually enter values. Only existing values will be accepted. 
 */
@ShortName("Spinner")
@Events(values={"ValueChanged (Value As Object)"})
@DontInheritEvents
public class SpinnerWrapper extends ControlWrapper<Spinner<Object>> {
	
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new Spinner<Object>(0, 100, 0));
		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_valuechanged")) {
			ChangeListener<Object> cl = new ChangeListener<Object>() {

				@Override
				public void changed(ObservableValue<? extends Object> arg0,
						Object arg1, Object arg2) {
					ba.raiseEventFromUI(getObject(), eventName + "_valuechanged", arg2);
				}
			};
			AbsObjectWrapper.getExtraTags(getObject()).put("changelistener", cl);
			addListner();
		}
		

	}
	/**
	 * Gets or sets the spinner value.
	 */
	public Object getValue() {
		return getObject().getValue();
	}
	public void setValue(Object o) {
		SpinnerValueFactory fac =  getObject().getValueFactory();
		if (fac instanceof SpinnerValueFactory.DoubleSpinnerValueFactory)
			fac.setValue(((Number)o).doubleValue());
		else if (fac instanceof SpinnerValueFactory.IntegerSpinnerValueFactory)
			fac.setValue(((Number)o).intValue());
		else
			fac.setValue(o);
	}
	/**
	 * Sets the items from the provided list. The items can be numbers or strings.
	 *Example: <code>Spinner1.SetListItems(Array ("Item #1", "Item #2"))</code>
	 */
	public void SetListItems (List Items) {
		removeListener();
		ObservableList<Object> l = FXCollections.observableArrayList();
		l.addAll(Items.getObject());
		getObject().setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<Object>(l));
		addListner();
		
	}
	private void removeListener() {
		ChangeListener<Object> cl = (ChangeListener<Object>) AbsObjectWrapper.getExtraTags(getObject()).get("changelistener");
		if (cl != null)
			getObject().valueProperty().removeListener(cl);
	}
	private void addListner() {
		ChangeListener<Object> cl = (ChangeListener<Object>) AbsObjectWrapper.getExtraTags(getObject()).get("changelistener");
		if (cl != null)
			getObject().valueProperty().addListener(cl);
	}
	/**
	 * Sets the items. If all values are integers then only integers will be allowed. 
	 *MinValue - Minimum value.
	 *MaxValue - Maximum value.
	 *Step - Increment step.
	 *InitialValue - Initial value.
	 */
	public void SetNumericItems(double MinValue, double MaxValue, double Step, double InitialValue) {
		removeListener();
		setNumericItems(getObject(), MinValue, MaxValue, Step, InitialValue);
		addListner();
			
	}
	private static void setNumericItems(Spinner<Object> s, double MinValue, double MaxValue, double Step, double InitialValue) {
		if (Math.floor(Step) == Step && Math.floor(MinValue) == MinValue && Math.floor(MaxValue) == MaxValue && Math.floor(InitialValue) == InitialValue)
			s.setValueFactory((SpinnerValueFactory)new SpinnerValueFactory.IntegerSpinnerValueFactory((int)MinValue, (int)MaxValue, (int)InitialValue, (int)Step));
		else
			s.setValueFactory((SpinnerValueFactory)new SpinnerValueFactory.DoubleSpinnerValueFactory(MinValue, MaxValue, InitialValue, Step));
	}
	/**
	 * Gets or sets whether the user can edit the text field. Only valid values will be accepted.
	 */
	public boolean getEditable() {
		return getObject().isEditable();
	}
	public void setEditable(boolean b) {
		getObject().setEditable(b);
	}
	/**
	 * Gets or sets whether the values wrap around (circular).
	 */
	public boolean getWrapAround() {
		return getObject().getValueFactory().isWrapAround();
	}
	public void setWrapAround(boolean b) {
		getObject().getValueFactory().setWrapAround(b);
	}
	@SuppressWarnings("unchecked")
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		Spinner<Object> vg = (Spinner<Object>) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(Spinner.class, props, designer);
		}
		setNumericItems(vg, (Float)props.get("minValue"), (Float)props.get("maxValue"), 
				(Float)props.get("step"), (Float)props.get("initValue"));
		
		vg.getValueFactory().setWrapAround((Boolean)props.get("wraparound"));
		vg.setEditable((Boolean)props.get("editable"));
		return ControlWrapper.build(vg, props, designer);
	}
	

}
