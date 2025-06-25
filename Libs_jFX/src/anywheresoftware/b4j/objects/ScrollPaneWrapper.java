
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.AnchorPane;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4j.objects.NodeWrapper.ConcreteNodeWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;

/**
 * ScrollPane is a scrollable container. The InnerNode will return the node that actually holds content node which is scrolled.
 *By default the inner node is an AnchorPane.
 *The Position parameter in VScrollChanged and HScrollChanged events is a value between 0 (minimum) to 1 (maximum).
 */
@Events(values={"VScrollChanged (Position As Double)", "HScrollChanged (Position As Double)"})
@ShortName("ScrollPane")
public class ScrollPaneWrapper extends ControlWrapper<ScrollPane>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject) {
			setObject(new ScrollPane());
			getObject().setContent(new AnchorPane());
		}
		super.innerInitialize(ba, eventName, true);
		final Object sender = getObject();
		if (ba.subExists(eventName + "_vscrollchanged")) {
			getObject().vvalueProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2) {
					ba.raiseEventFromUI(sender, eventName + "_vscrollchanged", arg2.doubleValue());
				}
			});
		}
		if (ba.subExists(eventName + "_vscrollchanged")) {
			getObject().hvalueProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2) {
					ba.raiseEventFromUI(sender, eventName + "_hscrollchanged", arg2.doubleValue());
				}
			});
		}
	}
	/**
	 * Gets or sets the inner node.
	 */
	public ConcreteNodeWrapper getInnerNode() {
		return (ConcreteNodeWrapper)AbsObjectWrapper.ConvertToWrapper(new ConcreteNodeWrapper(), getObject().getContent());
	}
	public void setInnerNode(Node n) {
		getObject().setContent(n);
	}
	/**
	 * Sets the inner node to be a Pane and loads the layout file to the pane.
	 *Layout - Layout file.
	 *Width - The inner pane width. Set to -1 to fill the ScrollPane.
	 *Height - The inner pane height. Set to -1 to fill the ScrollPane.
	 */
	@RaisesSynchronousEvents
	public void LoadLayout(BA ba, String LayoutFile, double Width, double Height) throws Exception {
		ConcretePaneWrapper cpw = new ConcretePaneWrapper();
		cpw.Initialize(ba, "");
		setFitToWidth(Width == -1);
		setFitToHeight(Height == -1);
		setInnerNode(cpw.getObject());
		cpw.SetSize(Width, Height);
		cpw.LoadLayout(ba, LayoutFile);
	}
	/**
	 * Gets or sets the vertical scroll position. Value between 0 to 1.
	 */
	public double getVPosition() {
		return getObject().getVvalue();
	}
	public void setVPosition(double d) {
		getObject().setVvalue(d);
	}
	/**
	 * Gets or sets the horizontal scroll position. Value between 0 to 1.
	 */
	public double getHPosition() {
		return getObject().getHvalue();
	}
	public void setHPosition(double d) {
		getObject().setHvalue(d);
	}
	/**
	 * Gets or sets whether the ScrollPane can be scrolled using the mouse.
	 */
	public boolean getPannable() {
		return getObject().isPannable();
	}
	public void setPannable(boolean b) {
		getObject().setPannable(b);
	}

	/**
	 * Sets the vertical scroll bar visibility.
	 *Policy - One of the following string values: NEVER, ALWAYS, AS_NEEDED. 
	 */
	public void SetVScrollVisibility (ScrollBarPolicy Policy) {
		getObject().setVbarPolicy(Policy);
	}
	/**
	 * Sets the horizontal scroll bar visibility.
	 *Policy - One of the following string values: NEVER, ALWAYS, AS_NEEDED. 
	 */
	public void SetHScrollVisibility (ScrollBarPolicy Policy) {
		getObject().setHbarPolicy(Policy);
	}
	/**
	 * Gets or sets whether the inner node should be resized to match the ScrollPane height.
	 *This property only affects resizable nodes.
	 */
	public void setFitToHeight(boolean b) {
		getObject().setFitToHeight(b);
	}
	public boolean getFitToHeight() {
		return getObject().isFitToHeight();
	}
	/**
	 * Gets or sets whether the inner node should be resized to match the ScrollPane width.
	 *This property only affects resizable nodes.
	 */
	public void setFitToWidth(boolean b) {
		getObject().setFitToWidth(b);
	}
	public boolean getFitToWidth() {
		return getObject().isFitToWidth();
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		ScrollPane vg = (ScrollPane) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(ScrollPane.class, props, designer);
		}
		vg.setVbarPolicy(ScrollBarPolicy.valueOf((String)props.get("vbar")));
		vg.setHbarPolicy(ScrollBarPolicy.valueOf((String)props.get("hbar")));
		vg.setPannable((Boolean)props.get("pannable"));
		return ControlWrapper.build(vg, props, designer);
	}

}
