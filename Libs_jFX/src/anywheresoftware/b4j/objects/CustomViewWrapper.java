
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import anywheresoftware.b4a.B4AClass;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper.NonResizePane;

@Hide
public class CustomViewWrapper extends NodeWrapper<Pane> {
	public Object customObject;
	public HashMap<String, Object> props;
	private String eventName;
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		this.ba = ba;
		//don't call parent
		this.eventName = eventName;
	}
	public void AfterDesignerScript() throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class<?> c = customObject.getClass();
		boolean userClass = customObject instanceof B4AClass;
		Map m = new Map();
		m.Initialize();
		if (props.containsKey("customProperties")) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> cp = (HashMap<String, Object>)props.get("customProperties");
			for (Entry<String, Object> e : cp.entrySet()) {
				Object value = e.getValue();
				if (e.getValue() instanceof byte[])
					value = NodeWrapper.ColorFromBytes(value);
				m.Put(e.getKey(), value);
			}
		}
		ConcretePaneWrapper pw = new ConcretePaneWrapper();
		pw.setObject(getObject());
		LabelWrapper lw = new LabelWrapper();
		lw.setObject((Label)getTag());
		pw.setTag(props.get("tag"));
		Form f = Form.getFormFromNode(pw.getObject());
		if (f != null)
			m.Put("Form", f);
		Object target = ba.eventsTarget != null ? ba.eventsTarget : Class.forName(ba.className);
		if (BA.isShellModeRuntimeCheck(ba) && userClass) {
			ba.raiseEvent2(null, true, "CREATE_CUSTOM_VIEW", true, customObject, ba, target, eventName, pw, lw, m);
		}
		else {
			c.getMethod("_initialize", BA.class, Object.class, String.class).invoke(customObject, ba, target, eventName);
			if (userClass) {
				B4AClass bc = (B4AClass)customObject;
				bc.getBA().raiseEvent2(null, true, "designercreateview", true, pw, lw, m);
				pw.innerInitialize(bc.getBA(), "base", true);

			}
			else {
				((DesignerCustomView)customObject).DesignerCreateView(pw, lw, m);
			}
		}
		
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		if (prev == null) {
			prev = NodeWrapper.buildNativeView(NonResizePane.class, props, designer);
		}
		Pane v = (Pane) ConcretePaneWrapper.build(prev, props, designer, tag);
		Label lbl = (Label) new Label();
		String text = (String)props.get("text");
		if (designer && text.length() == 0)
			text = (String)props.get("name");
		lbl.setText(text);
		lbl.setWrapText((Boolean)props.get("wrapText"));
		Color c = NodeWrapper.ColorFromBytes((byte[])props.get("textColor"));
		if (c != null)
			lbl.setTextFill(c);
		lbl.setAlignment(Enum.valueOf(Pos.class, (String)props.get("alignment")));
		if (!designer) {
			StringBuilder sb = new StringBuilder();
			NodeWrapper.buildFont(props, lbl, (java.util.Map<String, Object>) props.get("font"), sb, designer, true);
			lbl.setStyle(sb.toString());
		}
		String tt = (String) props.get("toolTip");
		if (tt.length() > 0)
			lbl.setTooltip(new Tooltip(tt));
		ArrayList<Menu> l1 = MenuItemWrapper.MenuBarWrapper.parseMenusJson((BA)props.get("ba"),
				(String)props.get("contextMenu"), (String)props.get("eventName"));
		if (l1.size() > 0) {
			ContextMenu cm = new ContextMenu();
			cm.getItems().addAll(l1);
			lbl.setContextMenu(cm);
		} else {
			lbl.setContextMenu(null);
		}
		v.setUserData(lbl);
		NodeWrapper.SetLayout(lbl, new double[] {0, 0, v.getPrefWidth(), v.getPrefHeight()});
		if (designer) {
			v.getChildren().clear();
			v.getChildren().add(lbl);
		}
		return v;
	}
	@Hide
	public interface DesignerCustomView {
		void DesignerCreateView(ConcretePaneWrapper base, LabelWrapper lw, anywheresoftware.b4a.objects.collections.Map props);
		void _initialize(BA ba, Object target, String EventName);

	}


}
