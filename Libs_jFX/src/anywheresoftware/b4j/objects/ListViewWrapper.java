
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.util.Callback;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

/**
 * A control that shows a list of items.
 *ListView supports multiple selection.
 *ListView.Items returns a List with the items. This list can be modified.
 *SelectedIndexChanged event is raised when the selected index changes. A value of -1 means that there is no selection.
 */
@ShortName("ListView")
@Events(values={"SelectedIndexChanged(Index As Int)"})
public class ListViewWrapper extends ControlWrapper<ListView<Object>>{
	
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new ListView<Object>());
		super.innerInitialize(ba, eventName, true);
		getObject().setItems(FXCollections.observableArrayList());
		if (ba.subExists(eventName + "_selectedindexchanged")) {
			getObject().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2) {
					ba.raiseEventFromUI(getObject(), eventName + "_selectedindexchanged", arg2.intValue());
				}
			});
		}
		getObject().setCellFactory(new MyCallBack());
		
	}
	@Hide
	public static class MyCallBack implements Callback<ListView<Object>, ListCell<Object>> {
		@Override
		public ListCell<Object> call(ListView<Object> arg0) {
			return new ListCell<Object>() {
				 protected void updateItem(Object item, boolean empty) {
					 super.updateItem(item, empty);
					 if (!empty) {
						if (item instanceof Node) {
							setText("");
							setGraphic((Node)item);
						}
						else {
							setText(String.valueOf(item));
							setGraphic(null);
						}
					 }
					 else {
						 setText("");
						 setGraphic(null);
					 }
				 }
			};
		}
	}
	/**
	 * Clears selection.
	 */
	public void ClearSelection() {
		getObject().getSelectionModel().clearSelection();
	}
	/**
	 * Gets or sets the selected index.
	 */
	public int getSelectedIndex() {
		return getObject().getSelectionModel().getSelectedIndex();
	}
	/**
	 * Gets the selected item.
	 */
	public Object getSelectedItem() {
		return getObject().getSelectionModel().getSelectedItem();
	}
	public void setSelectedIndex(int i) {
		getObject().getSelectionModel().clearSelection();
		getObject().getSelectionModel().select(i);
	}
	/**
	 * Sets whether multiple selection is enabled.
	 */
	public void setMultipleSelection(boolean b) {
		if (b)
			getObject().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		else
			getObject().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	}
	/**
	 * Returns a List with the selected indices.
	 */
	public List GetSelectedIndices() {
		ObservableList<Integer> ol = getObject().getSelectionModel().getSelectedIndices();
		List l1 = new List();
		l1.Initialize();
		for (int i = 0;i < ol.size() ;i++) {
			l1.Add(ol.get(i));
		}
		return l1;
	}
	/**
	 * Sets the selected indices.
	 */
	public void SetSelectedIndices(List Indices) {
		getObject().getSelectionModel().clearSelection();
		for (Object o : Indices.getObject()) {
			getObject().getSelectionModel().select(((Integer)o).intValue());
		}
		
	}
	/**
	 * Scrolls the list to the specified index.
	 */
	public void ScrollTo(int Index) {
		getObject().scrollTo(Index);
	}
	/**
	 * Returns a List with the ListView items. You can add or remove items from this list.
	 *If the item added is a Node (any type of Node) then the Node will be used to visualize the item.
	 */
	public List getItems() {
		List l = new List();
		l.setObject(getObject().getItems());
		return l;
	}
	@SuppressWarnings("unchecked")
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		ListView<Object> vg = (ListView<Object>) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(ListView.class, props, designer);
			for (int i = 1;i <= 10;i++) {
				vg.getItems().add("Item #" + i);
			}
		}
		SelectionMode sm;
		if ((Boolean)props.get("multipleSelection"))
			sm = SelectionMode.MULTIPLE;
		else
			sm = SelectionMode.SINGLE;
		vg.getSelectionModel().setSelectionMode(sm);
		return ControlWrapper.build(vg, props, designer);
	}
	
}
