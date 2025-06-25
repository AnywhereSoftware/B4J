
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

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.ImageViewWrapper.ImageWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

/**
 * A combination of TreeView and TableView. Similar to TreeView where each item shows additional columns.
 *Each value can be a string, number or a Node.
 */
@ShortName("TreeTableView")
@Events(values={"SelectedItemChanged (SelectedItem As TreeTableItem)"})
public class TreeTableViewWrapper extends ControlWrapper<TreeTableView<anywheresoftware.b4j.objects.TreeTableViewWrapper.TreeTableColType[]>>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new TreeTableView<TreeTableColType[]>());
		super.innerInitialize(ba, eventName, true);
		final TreeTableView<TreeTableColType[]> ttv = getObject();
		if (ba.subExists(eventName + "_selecteditemchanged")) {
			getObject().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<TreeTableColType[]>>() {

				@Override
				public void changed(
						ObservableValue<? extends TreeItem<TreeTableColType[]>> arg0,
								TreeItem<TreeTableColType[]> arg1, TreeItem<TreeTableColType[]> arg2) {
					ba.raiseEventFromUI(ttv, eventName + "_selecteditemchanged", 
							AbsObjectWrapper.ConvertToWrapper(new TreeTableItemWrapper(),arg2));

				}
			});
		}
	}
	/**
	 * Gets or sets the fixed row height. -1 = automatic height.
	 */
	public void setRowHeight(double d) {
		getObject().setFixedCellSize(d);
	}
	public double getRowHeight() {
		return getObject().getFixedCellSize();
	}
	/**
	 * Sets whether the column is sortable. Sortable means that the user can click on the header to sort it.
	 *Columns are not sortable by default.
	 */
	public void SetColumnSortable(int Index, boolean Sortable) {
		getObject().getColumns().get(Index).setSortable(Sortable);
	}
	/**
	 * Returns the number of columns.
	 */
	public int getColumnsCount() {
		return getObject().getColumns().size();
	}
	/**
	 * Gets the column header.
	 *Index - Column index (first column index is 0).
	 */
	public String GetColumnHeader(int Index) {
		return getObject().getColumns().get(Index).getText();
	}
	/**
	 * Sets the column header.
	 * Index - Column index (first column index is 0).
	 */
	public void SetColumnHeader(int Index, String Header) {
		getObject().getColumns().get(Index).setText(Header);
	}
	/**
	 * Gets the column width.
	 *Index - Column index (first column index is 0).
	 */
	public double GetColumnWidth(int Index) {
		return getObject().getColumns().get(Index).getWidth();
	}
	/**
	 * Sets the column width.
	 *Index - Column index (first column index is 0).
	 */
	public void SetColumnWidth(int Index, double Width) {
		getObject().getColumns().get(Index).setPrefWidth(Width);
	}

	/**
	 * Sets whether the column is visible.
	 */
	public void SetColumnVisible(int Index, boolean Visible) {
		getObject().getColumns().get(Index).setVisible(Visible);
	}
	/**
	 * Gets whether the column is visible.
	 */
	public boolean GetColumnVisible(int Index) {
		return getObject().getColumns().get(Index).isVisible();
	}
	/**
	 * Gets or sets the selected item.
	 */
	public TreeTableItemWrapper getSelectedItem() {
		return (TreeTableItemWrapper)AbsObjectWrapper.ConvertToWrapper(new TreeTableItemWrapper(), getObject().getSelectionModel().getSelectedItem());
	}
	public void setSelectedItem(TreeTableItemWrapper ttt) {
		getObject().getSelectionModel().select(ttt.getObject());
	}
	/**
	 * Clears selection.
	 */
	public void ClearSelection() {
		getObject().getSelectionModel().clearSelection();
	}
	/**
	 * Sets the table columns.
	 *Columns - A List (or array) with the columns titles.
	 */
	public void SetColumns(List Columns) {
		getObject().getColumns().clear();
		if (Columns == null || Columns.IsInitialized() == false)
			return;
		int colIndex = 0;
		double w = (getPrefWidth() - Common.DipToCurrent(20)) / (double)Columns.getSize();
		for (Object colName : Columns.getObject()) {
			TreeTableColumn<TreeTableColType[], Object> tc = new TreeTableColumn<TreeTableColType[],Object>(String.valueOf(colName));
			tc.setPrefWidth(w);
			tc.setCellValueFactory(new MyCellValueFactory(colIndex));
			tc.setSortable(false);
			getObject().getColumns().add(tc);
			colIndex++;
		}
		getObject().setRoot(new TreeItem<TreeTableColType[]>(null));
		AbsObjectWrapper.getExtraTags(getObject().getRoot()).put("tree", getObject());
		getObject().setShowRoot(false);
	}
	/**
	 * Gets root item. This item is not visible.
	 */
	public TreeTableItemWrapper getRoot() {
		return (TreeTableItemWrapper)AbsObjectWrapper.ConvertToWrapper(new TreeTableItemWrapper(),
				getObject().getRoot());
	}
	@Hide
	@SuppressWarnings("unchecked")
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		TreeTableView<TreeTableColType[]> vg = (TreeTableView<TreeTableColType[]>) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(TreeTableView.class, props, designer);
		}
		ControlWrapper.build(vg, props, designer);
		String rawColumns = (String)props.get("columns");
		TreeTableViewWrapper tvw = new TreeTableViewWrapper();
		tvw.setObject(vg);
		List l1 = new List();
		l1.Initialize();
		for (String s : rawColumns.split(",")) {
			s = s.trim();
			if (s.length() > 0)
				l1.Add(s);
		}

		if (designer) {
			boolean alreadySet = false;
			if (tvw.getColumnsCount() == l1.getSize()) {
				alreadySet = true;
				for (int i = 0;i < l1.getSize();i++) {
					if (tvw.GetColumnHeader(i).equals(l1.Get(i)) == false)
						alreadySet = false;
				}
				tvw.SetColumnHeader(0, "set");
			}
			if (alreadySet == false) {
				tvw.SetColumns(l1);
				TreeTableItemWrapper ttw = tvw.getRoot();
				for (int i = 1;i < 6;i++) {
					TreeTableItemWrapper t = new TreeTableItemWrapper();
					Object[] o = new Object[tvw.getColumnsCount()];
					for (int c = 0;c < o.length;c++)
						o[c] = i;
					t.setObject(new TreeItem<TreeTableColType[]>(t.ArrayTo(o)));
					if (i == 5)
						ttw = tvw.getRoot();
					ttw.getChildren().Add(t.getObject());
					t.setExpanded(true);
					ttw = t;
				}
			}
		}
		else {
			tvw.SetColumns(l1);
		}
		return vg;
	}

	private static class MyCellValueFactory implements Callback<TreeTableColumn.CellDataFeatures<TreeTableColType[], Object>, ObservableValue<Object>>{
		private final int colIndex;
		public MyCellValueFactory(int colIndex) {
			this.colIndex = colIndex;
		}
		@Override
		public ObservableValue<Object> call(
				CellDataFeatures<TreeTableColType[], Object> param) {
			return param.getValue().getValue()[colIndex];
		}
	}
	@Hide
	public static class TreeTableColType extends SimpleObjectProperty<Object> {
		public TreeTableColType(Object initialValue) {
			super(initialValue);
		}
	}

	@ShortName("TreeTableItem")
	@Events(values={"ExpandedChanged(Expanded As Boolean)"})
	public static class TreeTableItemWrapper extends AbsObjectWrapper<TreeItem<TreeTableColType[]>> {
		/**
		 * Initializes the TreeTableItem.
		 *EventName - Sets the subs that will handle the events.
		 *Values - Array of objects with the column values. The values can be strings, numbers or Nodes.
		 */
		public void Initialize(final BA ba, final String EventName, Object[] Values) {
			final String eventName = EventName.toLowerCase(BA.cul);
			setObject(new TreeItem<TreeTableColType[]>(ArrayTo(Values)));
			final TreeItem<TreeTableColType[]> me = getObject();
			getObject().expandedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(
						ObservableValue<? extends Boolean> arg0,
						Boolean arg1, Boolean arg2) {
					if (getChildren().getSize() == 0) {
						//fix for a bug where the expanded property changes instead of the selection.
						TreeTableView<TreeTableColType[]> t = getTreeFromRoot(me);
						if (t != null && t.getSelectionModel().getSelectedItem() != me) {
							t.getSelectionModel().select(me);
						}
					} else {
						ba.raiseEventFromUI(me,eventName + "_expandedchanged", arg2.booleanValue());
					}

				}
			});
		}
		@SuppressWarnings("unchecked")
		@Hide
		public static TreeTableView<TreeTableColType[]> getTreeFromRoot(TreeItem<TreeTableColType[]> me) {
			TreeItem<TreeTableColType[]> parent = me;
			while (parent.getParent() != null)
				parent = parent.getParent();
			return (TreeTableView<TreeTableColType[]>) AbsObjectWrapper.extraTagsGetValueIfAvailable(parent, "tree");
		}
		

		@Hide
		public TreeTableColType[] ArrayTo(Object[] list) {
			TreeTableColType[] t = new TreeTableColType[list.length];
			for (int i = 0;i < t.length;i++)
				t[i] = new TreeTableColType(list[i]);
			return t;
		}
		/**
		 * Gets the value of the given column (first column index is 0).
		 */
		public Object GetValue(int Column) {
			return getObject().getValue()[Column].get();
		}
		/**
		 * Sets the value of the given column (first column index is 0).
		 */
		public void SetValue(int Column, Object Value) {
			getObject().getValue()[Column].set(Value);
		}
		/**
		 * Returns the TreeTableItem parent. Will return an uninitialized TreeTableItem if this is a root item.
		 */
		public TreeTableItemWrapper getParent() {
			return (TreeTableItemWrapper)AbsObjectWrapper.ConvertToWrapper(new TreeTableItemWrapper(),
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
		 * Returns a list with the TreeTableItem children.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public List getChildren() {
			List l1 = new List();
			l1.setObject((java.util.List)getObject().getChildren());
			return l1;
		}
		/**
		 * Gets or sets the image that is displayed before the text in the first column.
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
}
