
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

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;

@ShortName("SplitPane")
public class SplitPaneWrapper extends ControlWrapper<SplitPane>{
	
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new SplitPane());
		super.innerInitialize(ba, eventName, true);
	
	}
	public boolean getVertical() {
		return getObject().getOrientation() == Orientation.VERTICAL;
	}
	public void setVertical(boolean b) {
		getObject().setOrientation(b ? Orientation.VERTICAL : Orientation.HORIZONTAL);
	}
	/**
	 * Gets or sets the dividers positions. The positions are stored in an array of doubles.
	 *Each value should be between 0 to 1.
	 */
	public double[] getDividerPositions() {
		return getObject().getDividerPositions();
	}
	public void setDividerPositions(double[] d) {
		getObject().setDividerPositions(d);
	}
	@RaisesSynchronousEvents
	public void LoadLayout(BA ba, String LayoutFile) throws Exception {
		ConcretePaneWrapper cnw = new ConcretePaneWrapper();
		cnw.Initialize(ba, "");
		getObject().getItems().add(cnw.getObject());
		cnw.LoadLayout(ba, LayoutFile);
	}
	/**
	 * Sets the layout minimum and maximum sizes.
	 *MinSize - The layout minimum size (width for horizontal orientation and height for vertical orientation).
	 *MaxSize - The layout maximum size. Pass 0 for no limit.
	 */
	public void SetSizeLimits(int LayoutIndex, double MinSize, double MaxSize) {
		Pane n = (Pane) getObject().getItems().get(LayoutIndex);
		if (getObject().getOrientation() == Orientation.HORIZONTAL) {
			n.setMinWidth(MinSize);
			if (MaxSize > 0)
				n.setMaxWidth(MaxSize);
		}
		else {
			n.setMinHeight(MinSize);
			if (MaxSize > 0)
				n.setMaxHeight(MaxSize);
		}
	}
	
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		SplitPane vg = (SplitPane) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(SplitPane.class, props, designer);
			
			if (designer) {
				for (int i = 1;i <= 2;i++) {
					Pane p = new Pane();
					vg.getItems().add(p);
				}
			}
		}
		vg.setOrientation((Boolean)props.get("vertical") ? Orientation.VERTICAL : Orientation.HORIZONTAL);
		return ControlWrapper.build(vg, props, designer);
	}
	

}
