
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

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

/**
 * A control that shows data in a table.
 *The table data is stored in a List. Each item in the list (which represents a row) is an array of objects. One object for each column.
 *Changing the data in the list will change the data in the table.
 *Set SingleCellSelection to True if you want to allow selection of single cells instead of rows.
 */
@ShortName("TableView")
@Events(values={"SelectedRowChanged(Index As Int, Row() As Object)",
		"SelectedCellChanged (RowIndex As Int, ColIndex As Int, Cell As Object)"})
public class TableViewWrapper extends ControlWrapper<TableView<Object[]>> {
	@SuppressWarnings("rawtypes")
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new TableView<Object[]>());
		super.innerInitialize(ba, eventName, true);
		if (ba.subExists(eventName + "_selectedrowchanged")) {
			getObject().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2) {
					if (getObject().getSelectionModel().isCellSelectionEnabled() == true)
						return;
					Object[] row = null;
					if (arg2.intValue() > -1)
						row = getObject().getItems().get(arg2.intValue());
					ba.raiseEventFromUI(getObject(), eventName + "_selectedrowchanged", arg2.intValue(), row);
				}
			});
		}
		if (ba.subExists(eventName + "_selectedcellchanged")) {
			getObject().getSelectionModel().getSelectedCells().addListener(new ListChangeListener<TablePosition>() {

				@Override
				public void onChanged(
						javafx.collections.ListChangeListener.Change<? extends TablePosition> arg0) {
					if (getObject().getSelectionModel().isCellSelectionEnabled() == false)
						return;
					if (arg0.getList().size() == 0)
						return;
					TablePosition tp = arg0.getList().get(0);
					ba.raiseEventFromUI(getObject(), eventName + "_selectedcellchanged", tp.getRow(), tp.getColumn(), 
							((Object[])tp.getTableView().getItems().get(tp.getRow()))[tp.getColumn()]);
				}
				
			});
		}
	}
	/**
	 * Gets or sets whether single cell selection is enabled.
	 */
	public void setSingleCellSelection(boolean b) {
		getObject().getSelectionModel().setCellSelectionEnabled(b);
	}
	public boolean getSingleCellSelection() {
		return getObject().getSelectionModel().isCellSelectionEnabled();
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
			TableColumn<Object[], Object> tc = new TableColumn<Object[], Object>(String.valueOf(colName));
			tc.setPrefWidth(w);
			tc.setCellValueFactory(new MyCellValueFactory(colIndex));
			getObject().getColumns().add(tc);
			colIndex++;
		}
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
	 * Sets whether the column is sortable. Sortable means that the user can click on the header to sort it.
	 */
	public void SetColumnSortable(int Index, boolean Sortable) {
		getObject().getColumns().get(Index).setSortable(Sortable);
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
	 * Gets or sets the index of the selected row.
	 */
	public int getSelectedRow() {
		return getObject().getSelectionModel().getSelectedIndex();
	}
	public void setSelectedRow(int row) {
		getObject().getSelectionModel().clearAndSelect(row);
	}
	/**
	 * Clears selection.
	 */
	public void ClearSelection() {
		getObject().getSelectionModel().clearSelection();
	}
	/**
	 * Selects a single cell. Make sure to first set SingleCellSelection to True.
	 */
	public void SelectCell(int Row, int Column) {
		getObject().getSelectionModel().clearAndSelect(Row, getObject().getColumns().get(Column));
	}
	/**
	 * Gets the values of the selected row or sets the selected row based on the given values.
	 */
	public Object[] getSelectedRowValues() {
		return getObject().getSelectionModel().getSelectedItem();
	}
	public void setSelectedRowValues(Object[] r) {
		getObject().getSelectionModel().clearSelection();
		getObject().getSelectionModel().select(r);
	}
	/**
	 * Scrolls to the given row index.
	 */
	public void ScrollTo(int Index) {
		getObject().scrollTo(Index);
	}
	/**
	 * Gets or sets the list that holds the table data.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setItems(List list) {
		getObject().setItems(FXCollections.observableList((java.util.List<Object[]>)(java.util.List)list.getObject()));
	}
	public List getItems() {
		return (List)AbsObjectWrapper.ConvertToWrapper(new List(), getObject().getItems());
	}
	private static class MyCellValueFactory implements Callback<CellDataFeatures<Object[], Object>, ObservableValue<Object>>{
		private final int colIndex;
		public MyCellValueFactory(int colIndex) {
			this.colIndex = colIndex;
		}
		@Override
		public ObservableValue<Object> call(
				CellDataFeatures<Object[], Object> arg0) {
			return new ReadOnlyObjectWrapper<Object>(arg0.getValue()[colIndex]);
		}
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		TableView<Object[]> vg = (TableView<Object[]>) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(TableView.class, props, designer);
		}
		 ControlWrapper.build(vg, props, designer);
		String rawColumns = (String)props.get("columns");
		TableViewWrapper tvw = new TableViewWrapper();
		tvw.setObject(vg);
		List l1 = new List();
		l1.Initialize();
		for (String s : rawColumns.split(",")) {
			s = s.trim();
			if (s.length() > 0)
				l1.Add(s);
		}
		tvw.SetColumns(l1);
		
//		if (designer) {
//			List items = new List();
//			items.Initialize();
//			for (int row = 0;row < 10;row++) {
//				Object[] al = new Object[l1.getSize()];
//				for (int col = 0;col < l1.getSize();col++)
//					al[col] = "Row: " + row + ", Col: " + col;
//				items.Add(al);
//			}
//			tvw.setItems(items);
//		}
		return vg;
	}
	
}
