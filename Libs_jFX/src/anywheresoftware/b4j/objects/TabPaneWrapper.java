
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
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.ImageViewWrapper.ImageWrapper;
import anywheresoftware.b4j.objects.MenuItemWrapper.ContextMenuWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;

@ShortName("TabPane")
@Events(values={"TabChanged (SelectedTab As TabPage)"})
public class TabPaneWrapper extends ControlWrapper<TabPane> {
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new TabPane());
		super.innerInitialize(ba, eventName, true);
		final Object sender = getObject();
		if (ba.subExists(eventName + "_tabchanged")) {
			getObject().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

				@Override
				public void changed(ObservableValue<? extends Tab> arg0,
						Tab arg1, Tab arg2) {
					ba.raiseEventFromUI(sender, eventName + "_tabchanged", AbsObjectWrapper.ConvertToWrapper(new TabWrapper(), arg2));
				}
			});
		}
		getObject().setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
	}
	/**
	 * Sets the tabs position. The possible values are: "TOP", "LEFT", "RIGHT", "BOTTOM". 
	 */
	public void SetSide(Side Side) {
		getObject().setSide(Side);
	}
	/**
	 * Gets a list with the TabPane tabs.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getTabs() {
		List l1 = new List();
		l1.setObject((java.util.List)getObject().getTabs());
		return l1;
	}
	/**
	 * Gets the selected tab page.
	 */
	public TabWrapper getSelectedItem() {
		return (TabWrapper)AbsObjectWrapper.ConvertToWrapper(new TabWrapper(), getObject().getSelectionModel().getSelectedItem());
	}
	/**
	 * Gets or sets the selected index.
	 */
	public int getSelectedIndex() {
		return getObject().getSelectionModel().getSelectedIndex();
	}
	
	public void setSelectedIndex(int i) {
		getObject().getSelectionModel().select(i);
	}
	
	/**
	 * Creates a new tab page with a pane as its content. The LayoutFile is loaded to the pane.
	 *LayoutFile - The layout file will be loaded to the page content.
	 *TabText - The tab page header text.
	 */
	@RaisesSynchronousEvents
	public TabWrapper LoadLayout(BA ba, String LayoutFile, String TabText) throws Exception {
		TabWrapper tw = new TabWrapper();
		tw.Initialize();
		tw.setText(TabText);
		ConcretePaneWrapper cnw = new ConcretePaneWrapper();
		cnw.Initialize(ba, "");
		tw.setContent(cnw.getObject());
		cnw.LoadLayout(ba, LayoutFile);
		getTabs().Add(tw.getObject());
		return tw;
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		TabPane vg = (TabPane) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(TabPane.class, props, designer);
			if (designer) {
				for (int i = 1;i <= 3;i++) {
					vg.getTabs().add(new Tab("Tab #" + i));
				}
			}
			vg.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		}
		vg.setSide(Side.valueOf((String)props.get("side")));
		return ControlWrapper.build(vg, props, designer);
	}
	
	@ShortName("TabPage")
	public static class TabWrapper extends AbsObjectWrapper<Tab> {
		public void Initialize() {
			setObject(new Tab());
		}
		
		/**
		 * Gets or sets the tab text.
		 */
		public String getText() {
			return getObject().getText();
		}
		public void setText(String s) {
			getObject().setText(s);
		}
		/**
		 * Gets or sets the image that is displayed in the tab.
		 */
		public void setImage(Image Image) {
			ImageView iv = new ImageView(Image);
			getObject().setGraphic(iv);
		}
		public ImageWrapper getImage() {
			ImageWrapper iv = new ImageWrapper();
			if (getObject().getGraphic() instanceof ImageView)
				iv.setObject(((ImageView)getObject().getGraphic()).getImage());
			return iv;
		}
		/**
		 * Gets or sets the tab content pane.
		 */
		public ConcretePaneWrapper getContent() {
			return (ConcretePaneWrapper)AbsObjectWrapper.ConvertToWrapper(new ConcretePaneWrapper(), getObject().getContent());
		}
		public void setContent(Pane Pane) {
			getObject().setContent(Pane);
		}
		/**
		 * Gets or sets the tab's tag. This is a placeholder for any object you need to tie to the node.
		 */
		public Object getTag() {
			Object o =  getObject().getUserData();
			return o == null ? "" : o;
		}
		public void setTag(Object o) {
			getObject().setUserData(o);
		}
		/**
		 * Gets or sets the tab id. Returns an empty string if the id was not set.
		 */
		public String getId() {
			return getObject().getId() == null ? "" : getObject().getId();
		}
		public void setId(String s) {
			getObject().setId(s);
		}
		public void setContextMenu(ContextMenuWrapper c) {
			getObject().setContextMenu(c.getObject());
		}
		/**
		 * Gets or sets the context menu that will appear when the user right clicks on the control.
		 */
		public ContextMenuWrapper getContextMenu() {
			return (ContextMenuWrapper)AbsObjectWrapper.ConvertToWrapper(new ContextMenuWrapper(), getObject().getContextMenu());
		}
		
	}

}
