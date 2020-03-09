
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
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
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

/**
 * A container of TitledPanes. These are panes with title. It is similar to TabPane.
 */
@ShortName("Accordion")
@Events(values={"PaneChanged (ExpandedPane As TitledPane)"})
public class AccordionWrapper extends ControlWrapper<Accordion>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new Accordion());
		super.innerInitialize(ba, eventName, true);
		final Object sender = getObject();
		if (ba.subExists(eventName + "_panechanged")) {
			getObject().expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {

				@Override
				public void changed(
						ObservableValue<? extends TitledPane> observable,
						TitledPane oldValue, TitledPane newValue) {
					ba.raiseEventFromUI(sender, eventName + "_panechanged", AbsObjectWrapper.ConvertToWrapper(new TitledPaneWrapper(), newValue));
				}
			});
		}
	}
	/**
	 * Gets a list with the TitledPanes.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getPanes() {
		List l1 = new List();
		l1.setObject((java.util.List)getObject().getPanes());
		return l1;
	}
	/**
	 * Gets the expanded TitledPane. Returns an uninitialized object if no pane is expanded. 
	 */
	public TitledPaneWrapper getSelectedItem() {
		return (TitledPaneWrapper)AbsObjectWrapper.ConvertToWrapper(new TitledPaneWrapper(), getObject().getExpandedPane());
	}
	/**
	 * Gets or sets the expanded pane.
	 *Returns -1 if no pane is expanded. Pass -1 to close the expanded pane.
	 */
	public int getSelectedIndex() {
		return getObject().getPanes().indexOf(getObject().getExpandedPane());
	}
	
	public void setSelectedIndex(int i) {
		if (i == -1)
			getObject().setExpandedPane(null);
		else {
			getObject().setExpandedPane(getObject().getPanes().get(i));
			getObject().expandedPaneProperty().get().setExpanded(true);
		}
	}
	/**
	 * Creates a new TitledPane. The LayoutFile is loaded to the pane.
	 *LayoutFile - The layout file will be loaded to the page content.
	 *Title - The tab page header text.
	 */
	@RaisesSynchronousEvents
	public TitledPaneWrapper LoadLayout(BA ba, String LayoutFile, String Title) throws Exception {
		ConcretePaneWrapper cnw = new ConcretePaneWrapper();
		cnw.Initialize(ba, "");
		cnw.LoadLayout(ba, LayoutFile);
		TitledPane tp = new TitledPane(Title, cnw.getObject());
		getObject().getPanes().add(tp);
		return (TitledPaneWrapper) AbsObjectWrapper.ConvertToWrapper(new TitledPaneWrapper(), tp);
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		Accordion vg = (Accordion) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(Accordion.class, props, designer);
			if (designer) {
				for (int i = 1;i <= 3;i++) {
					vg.getPanes().add(new TitledPane("Tab #" + i, new Pane()));
				}
			}
		}
		return ControlWrapper.build(vg, props, designer);
	}
	/**
	 * A pane with a title.
	 *Use Accordion.LoadLayout to create new TitledPanes.
	 */
	@ShortName("TitledPane")
	public static class TitledPaneWrapper extends AbsObjectWrapper<TitledPane> {
		public void Initialize() {
			setObject(new TitledPane());
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
