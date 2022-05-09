
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4j.objects.LayoutBuilder.B4JTextControl;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

@Hide
@DontInheritEvents
@Events(values={"TextChanged (Old As String, New As String)",
		"MouseClicked (EventData As MouseEvent)",
		"FocusChanged (HasFocus As Boolean)"})
public class TextInputControlWrapper<T extends TextInputControl> extends ControlWrapper<T> implements B4JTextControl{
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		super.innerInitialize(ba, eventName, keepOldObject);
		if (ba.subExists(eventName + "_textchanged")) {
			getObject().textProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> arg0,
						String old, String newValue) {
					ba.raiseEventFromUI(getObject(), eventName + "_textchanged", old, newValue);
				}
			});
		}
	}
	/**
	 * Gets or sets the text.
	 */
	public String getText() {
		return getObject().getText();
	}
	public void setText(String s) {
		getObject().setText(s);
	}
	/**
	 * Gets or sets the prompt text. This text appears when there is no text set.
	 */
	public String getPromptText() {
		return getObject().getPromptText();
	}
	public void setPromptText(String s) {
		getObject().setPromptText(s);
	}
	/**
	 * Returns the selection start index.
	 */
	public int getSelectionStart() {
		return getObject().getSelection().getStart();
	}
	/**
	 * Return the selection end index.
	 */
	public int getSelectionEnd() {
		return getObject().getSelection().getEnd();
	}
	/**
	 * Sets the selection.
	 */
	public void SetSelection(int StartIndex, int EndIndex) {
		getObject().selectRange(StartIndex, EndIndex);
	}
	/**
	 * Selects all the text.
	 */
	public void SelectAll() {
		getObject().selectAll();
	}
	@Override
	public String toString() {
		return super.toString() + ", Text: " + getText() + ", " ;
	}
	/**
	 * Gets or sets whether the text control is editable.
	 */
	public boolean getEditable() {
		return getObject().isEditable();
	}
	public void setEditable(boolean b) {
		getObject().setEditable(b);
	}
	
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer) throws Exception{
		TextInputControl vg = (TextInputControl) prev;
		ControlWrapper.build(vg, props, designer);
		vg.setText((String)props.get("text"));
		vg.setPromptText((String)props.get("prompt"));
		vg.setEditable((Boolean)props.get("editable"));
		return vg;
	}
	/**
	 * A single line editable text field.
	 */
	@ShortName("TextField")
	@Events(values="Action")
	public static class TextFieldWrapper extends TextInputControlWrapper<TextField> {
		/**
		 * Creates a new password field.
		 */
		public void InitializePassword(BA ba, String EventName) {
			setObject(new PasswordField());
			innerInitialize(ba, EventName.toLowerCase(BA.cul), true);
		}
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new TextField());
			super.innerInitialize(ba, eventName, true);
			if (ba.subExists(eventName + "_action")) {
				getObject().setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {
						ba.raiseEventFromUI(getObject(), eventName + "_action");
						arg0.consume();
					}
				});
			}
		}
		
		/**
		 * Gets or sets the content alignment.
		 *The possible values are: TOP_LEFT, TOP_CENTER, TOP_RIGHT,
		 *CENTER_LEFT, CENTER, CENTER_RIGHT,
		 *BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT,
		 *BASELINE_LEFT, BASELINE_CENTER and BASELINE_RIGHT.
		 */
		public String getAlignment() {
			return getObject().getAlignment().toString();
		}
		public void setAlignment(String s) {
			getObject().setAlignment(Enum.valueOf(Pos.class, s));
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Hide
		public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
			Node vg = (Node) prev;
			boolean password = BA.gm(props, "password", false);
			if (vg == null || (vg instanceof PasswordField != password)) 
				vg = (Node) NodeWrapper.buildNativeView((Class)(password ? PasswordField.class : TextField.class), props, designer);
			vg = TextInputControlWrapper.build(vg, props, designer);
			if (props.containsKey("alignment"))
				((TextField)vg).setAlignment(Enum.valueOf(Pos.class, (String)props.get("alignment")));
			return vg;
		}
	}
	/**
	 * Multiline editable text field.
	 */
	@ShortName("TextArea")
	public static class TextAreaWrapper extends TextInputControlWrapper<TextArea> {

		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new TextArea());
			super.innerInitialize(ba, eventName, true);
		}
		/**
		 * Gets or sets whether the text will wrap if needed.
		 */
		public boolean getWrapText() {
			return getObject().isWrapText();
		}
		public void setWrapText(boolean b) {
			getObject().setWrapText(b);
		}
		/**
		 * Gets or sets the vertical scroll top position.
		 */
		public double getScrollTopPosition() {
			return getObject().getScrollTop();
		}
		public void setScrollTopPosition(double d) {
			getObject().setScrollTop(d);
		}
		/**
		 * Gets or sets the horizontal left position.
		 */
		public double getScrollLeftPosition() {
			return getObject().getScrollLeft();
		}
		public void setScrollLeftPosition(double d) {
			getObject().setScrollLeft(d);
		}
		
		@Hide
		public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
			Node vg = (Node) prev;
			if (vg == null) 
				vg = NodeWrapper.buildNativeView(TextArea.class, props, designer);
			vg = TextInputControlWrapper.build(vg, props, designer);
			((TextArea)vg).setWrapText((Boolean)props.get("wrapText"));
			return vg;
		}
	}

}
