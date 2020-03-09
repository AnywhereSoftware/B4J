
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

import java.lang.reflect.Method;
import java.util.HashMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.ImageViewWrapper.ImageWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

@ShortName("TreeView")
@Events(values={"SelectedItemChanged (SelectedItem As TreeItem)"})
public class TreeViewWrapper extends ControlWrapper<TreeView<String>>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new TreeView<String>());
		super.innerInitialize(ba, eventName, true);
		final TreeView<String> tv = getObject();

		if (ba.subExists(eventName + "_selecteditemchanged")) {
			getObject().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {

				@Override
				public void changed(
						ObservableValue<? extends TreeItem<String>> arg0,
								TreeItem<String> arg1, TreeItem<String> arg2) {
					ba.raiseEventFromUI(tv, eventName + "_selecteditemchanged", 
							AbsObjectWrapper.ConvertToWrapper(new ConcreteTreeItemWrapper(),arg2));

				}
			});
		}
		getObject().setRoot(new TreeItem<String>("root"));
		getObject().setShowRoot(false);
		getObject().getRoot().setExpanded(true);
	}
	/**
	 * Clears selection.
	 */
	public void ClearSelection() {
		getObject().getSelectionModel().clearSelection();
	}
	/**
	 * Shows a checkbox before each item in the TreeView.
	 *Items should be CheckboxTreeItems.
	 */
	public void SetCheckBoxesMode() {
		getObject().setCellFactory(CheckBoxTreeCell.<String>forTreeView());
	}

	/**
	 * Gets root item. This item is not visible.
	 */
	public ConcreteTreeItemWrapper getRoot() {
		return (ConcreteTreeItemWrapper)AbsObjectWrapper.ConvertToWrapper(new ConcreteTreeItemWrapper(),
				getObject().getRoot());
	}
	
	@SuppressWarnings("unchecked")
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		TreeView<String> vg = (TreeView<String>) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(TreeView.class, props, designer);
			if (designer) {
				vg.setRoot(new TreeItem<String>("root"));
				vg.setShowRoot(false);
				
				for (int i = 1;i <= 3; i++) {
					TreeItem<String> ti = new TreeItem<String>("Item #" + i);
					vg.getRoot().getChildren().add(ti);
					for (int x = 1;x <= 2;x++) {
						ti.getChildren().add(new TreeItem<String>("Child #" + x));
					}
				}
				vg.getRoot().setExpanded(true);
			}
		}
		return ControlWrapper.build(vg, props, designer);
	}

	@Hide
	@Events(values={"ExpandedChanged(Expanded As Boolean)"})
	public static class TreeItemWrapper<T extends TreeItem<String>> extends AbsObjectWrapper<T> {
		public void Initialize(final BA ba, final String eventName) {
			final TreeItem<String> me = getObject();
			if (ba.subExists(eventName + "_expandedchanged")) {
				getObject().expandedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(
							ObservableValue<? extends Boolean> arg0,
							Boolean arg1, Boolean arg2) {
						ba.raiseEventFromUI(me,eventName + "_expandedchanged", arg2.booleanValue());

					}
				});
			}
		}
		/**
		 * Gets or sets the TreeItem text.
		 */
		public String getText() {
			return getObject().getValue();
		}
		public void setText(String s) {
			getObject().setValue(s);
		}
		/**
		 * Returns the TreeItem parent. Will return an uninitialized TreeItem if this is a root item.
		 */
		public ConcreteTreeItemWrapper getParent() {
			return (ConcreteTreeItemWrapper)AbsObjectWrapper.ConvertToWrapper(new ConcreteTreeItemWrapper(),
					getObject().getParent());
		}
		/**
		 * Tests whether this TreeItem is a root item.
		 */
		public boolean getRoot() {
			return getObject().getParent() == null;
		}
		/**
		 * Gets or sets whether the tree item is expanded.
		 */
		public void setExpanded(boolean b) {
			getObject().setExpanded(b);
		}
		public boolean getExpanded() {
			return getObject().isExpanded();
		}
		/**
		 * Returns a list with the TreeItem children.
		 */
		public List getChildren() {
			List l1 = new List();
			l1.setObject((java.util.List)getObject().getChildren());
			return l1;
		}
		/**
		 * Gets or sets the image that is displayed before the text.
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
	}
	@ShortName("TreeItem")
	public static class ConcreteTreeItemWrapper extends TreeItemWrapper<TreeItem<String>> {
		/**
		 * Initializes the TreeItem.
		 */
		public void Initialize(BA ba, String EventName, String Text) {
			setObject(new TreeItem<String>(Text));
			super.Initialize(ba, EventName.toLowerCase(BA.cul));
		}
	}
	@Events(values={"CheckedChange(Checked As Boolean)"})
	@ShortName("CheckboxTreeItem")
	public static class CheckBoxTreeItemWrapper extends TreeItemWrapper<CheckBoxTreeItem<String>> {
		/**
		 * Initializes the item. Make sure to call TreeView.ChangeToCheckBoxesMode.
		 *Otherwise the checkboxes will not be visible.
		 */
		public void Initialize(final BA ba, String EventName, String Text) throws Exception {
			setObject(new CheckBoxTreeItem<String>(Text));

			final String eventName = EventName.toLowerCase(BA.cul);
			super.Initialize(ba, EventName.toLowerCase(BA.cul));
			final CheckBoxTreeItem<String> me = getObject();
			Method m = null;
			try {
				m = CheckBoxTreeItem.class.getMethod("setIndependent", boolean.class);
			} catch (NoSuchMethodException e) {
				m = CheckBoxTreeItem.class.getMethod("setIndependent", Boolean.class);
			}
			m.invoke(me, true);
			if (ba.subExists(eventName + "_checkedchange")) {
				getObject().selectedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(
							ObservableValue<? extends Boolean> arg0,
							Boolean arg1, Boolean arg2) {
						ba.raiseEventFromUI(me,eventName + "_checkedchange", arg2.booleanValue());

					}
				});
			}
		}
		/**
		 * Gets or sets the checked state.
		 */
		public boolean getChecked() {
			try {
				Method m = CheckBoxTreeItem.class.getDeclaredMethod("isSelected");
				Object o = m.invoke(getObject());
				return ((Boolean)o).booleanValue();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		public void setChecked(boolean b) {
			try {
				Method m;
				try {
					m = CheckBoxTreeItem.class.getMethod("setSelected", boolean.class);
				} catch (NoSuchMethodException e) {
					m = CheckBoxTreeItem.class.getMethod("setSelected", Boolean.class);
				}
				m.invoke(getObject(), b);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
