
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;


@ShortName("PoiRow")
public class PoiRowWrapper extends AbsObjectWrapper<Row>{
	/**
	 * Returns a List with PoiCell items representing the row defined cells.
	 */
	public List getCells() {
		List l1 = new List();
		l1.Initialize();
		for (Cell c : getObject()) {
			l1.Add(c);
		}
		return l1;
	}
	/**
	 * Returns the cell at the given index.
	 *The object will not uninitialized if there is no defined cell.
	 */
	public PoiCellWrapper GetCell(int Column) {
		return (PoiCellWrapper)AbsObjectWrapper.ConvertToWrapper(new PoiCellWrapper(), getObject().getCell(Column));
	}
	/**
	 * Gets or sets the row number.
	 */
	public int getRowNumber() {
		return getObject().getRowNum();
	}
	public void setRowNumber(int r) {
		getObject().setRowNum(r);
	}
	/**
	 * Gets or sets the row height.
	 */
	public float getHeight() {
		return getObject().getHeightInPoints();
	}
	public void setHeight(float f) {
		getObject().setHeightInPoints(f);
	}
	/**
	 * Gets or sets the row style.
	 *The object returned will be uninitialized if the style was not set before.
	 */
	public PoiCellStyleWrapper getRowStyle() {
		return (PoiCellStyleWrapper)AbsObjectWrapper.ConvertToWrapper(new PoiCellStyleWrapper(), getObject().getRowStyle());
	}
	public void setRowStyle(PoiCellStyleWrapper s) {
		getObject().setRowStyle(s.getObject());
	}
	/**
	 * Creates a string cell at the given index.
	 */
	public PoiCellWrapper CreateCellString(int CellIndex, String Value) {
		PoiCellWrapper pcw = createCell(CellIndex, CellType.STRING);
		pcw.getObject().setCellValue(Value);
		return pcw;
	}
	/**
	 * Creates a numeric cell at the given index.
	 */
	public PoiCellWrapper CreateCellNumeric(int CellIndex, double Value) {
		PoiCellWrapper pcw = createCell(CellIndex, CellType.NUMERIC);
		pcw.getObject().setCellValue(Value);
		return pcw;
	}
	/**
	 * Creates a blank cell.
	 */
	public PoiCellWrapper CreateCellBlank(int CellIndex) {
		PoiCellWrapper pcw = createCell(CellIndex, CellType.BLANK);
		return pcw;
	}
	
	/**
	 * Creates a boolean cell at the given index.
	 */
	public PoiCellWrapper CreateCellBoolean(int CellIndex, boolean Value) {
		PoiCellWrapper pcw = createCell(CellIndex, CellType.BOOLEAN);
		pcw.getObject().setCellValue(Value);
		return pcw;
	}
	/**
	 * Creates a formula cell at the given index.
	 */
	public PoiCellWrapper CreateCellFormula(int CellIndex, String Value) {
		PoiCellWrapper pcw = createCell(CellIndex, CellType.FORMULA);
		pcw.getObject().setCellFormula(Value);
		return pcw;
	}
	private PoiCellWrapper createCell(int CellIndex, CellType CellType) {
		Cell c = getObject().createCell(CellIndex, CellType);
		return (PoiCellWrapper)AbsObjectWrapper.ConvertToWrapper(new PoiCellWrapper(), c);
	}
}
