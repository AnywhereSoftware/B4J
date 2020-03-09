
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

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4j.objects.JFX.FontWrapper;
import anywheresoftware.b4j.objects.LayoutBuilder.B4JTextControl;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

@Hide
public class LabeledWrapper<T extends Labeled> extends ControlWrapper<T> implements B4JTextControl{
	/**
	 * Gets or sets the control text.
	 */
	public String getText() {
		return getObject().getText();
	}
	public void setText(String s) {
		getObject().setText(s);
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
	/**
	 * Gets or sets the text color.
	 */
	public Paint getTextColor() {
		return getObject().getTextFill();
	}
	public void setTextColor(Paint Color) {
		getObject().setTextFill(Color);
	}
	/**
	 * Gets or sets the control font.
	 *Example: <code>
	 *Button1.Font = fx.DefaultFont(14)</code>
	 */
	public void setFont(FontWrapper f) {
		getObject().setFont(f.getObject());
	}
	public FontWrapper getFont() {
		return (FontWrapper)AbsObjectWrapper.ConvertToWrapper(new FontWrapper(),getObject().getFont());
	}
	/**
	 * Gets or sets the text size.
	 */
	public double getTextSize() {
		return getObject().getFont().getSize();
	}
	public void setTextSize(double d) {
		getObject().setFont(Font.font(getObject().getFont().getName(), d));
	}
	/**
	 * Gets or sets whether the text will be wrapped if it exceeds the node's width.
	 */
	public boolean getWrapText() {
		return getObject().isWrapText();
	}
	public void setWrapText(boolean b) {
		getObject().setWrapText(b);
	}
	@Hide
	@Override
	public String toString() {
		return super.toString() + ", Text: " + getText() + ", " ;
	}
	
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer) throws Exception{
		Labeled vg = (Labeled) prev;
		ControlWrapper.build(vg, props, designer);
		vg.setText((String)props.get("text"));
		vg.setWrapText((Boolean)props.get("wrapText"));
		Color c = NodeWrapper.ColorFromBytes((byte[])props.get("textColor"));
		if (c != null)
			vg.setTextFill(c);
		vg.setAlignment(Enum.valueOf(Pos.class, (String)props.get("alignment")));
		if (designer)
			vg.setMnemonicParsing(false);
		return vg;
	}
	
	

}
