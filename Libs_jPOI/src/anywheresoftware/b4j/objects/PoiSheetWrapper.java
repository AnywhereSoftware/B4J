
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

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;

@ShortName("PoiSheet")
public class PoiSheetWrapper extends AbsObjectWrapper<Sheet>{
	/**
	 * Gets or sets whether the sheet is in right to left mode.
	 */
	public boolean getRightToLeft() {
		return getObject().isRightToLeft();
	}
	public void setRightToLeft(boolean b) {
		getObject().setRightToLeft(b);
	}
	/**
	 * Returns a List with PoiRow items representing the sheet rows.
	 *Note that empty rows will be excluded from the list.
	 */
	public List getRows() {
		List l1 = new List();
		l1.Initialize();
		for (Row r : getObject()) {
			l1.Add(r);
		}
		return l1;
	}
	/**
	 * Gets or sets the sheet name.
	 */
	public String getName() {
		return getObject().getSheetName();
	}
	public void setName(String s) {
		getObject().getWorkbook().setSheetName(getIndex(), s);
	}
	/**
	 * Returns the sheet index.
	 */
	public int getIndex() {
		return getObject().getWorkbook().getSheetIndex(getObject());
	}
	/**
	 * Creates a new row with the specified index. First row index is 0.
	 */
	public PoiRowWrapper CreateRow(int RowNumber) {
		return (PoiRowWrapper) AbsObjectWrapper.ConvertToWrapper(new PoiRowWrapper(), getObject().createRow(RowNumber));
	}
	/**
	 * Returns the row with the specified index. Returns an uninitialized PoiRow if the row is empty.
	 */
	public PoiRowWrapper GetRow(int RowNumber) {
		return (PoiRowWrapper) AbsObjectWrapper.ConvertToWrapper(new PoiRowWrapper(), getObject().getRow(RowNumber));
	}
	/**
	 * Gets the index of the last row on the sheet.
	 */
	public int getLastRowNumber() {
		return getObject().getLastRowNum();
	}
	/**
	 * Gets the index of the first row on the sheet.
	 */
	public int getFirstRowNumber() {
		return getObject().getFirstRowNum();
	}
	/**
	 * Merges the cells.
	 */
	public void AddMergedRegion(int FirstColumn, int FirstRow, int LastColumn ,  int LastRow) {
		getObject().addMergedRegion(new CellRangeAddress(FirstRow, LastRow, FirstColumn, LastColumn));
	}
	/**
	 * Gets the column width measured in 1 / 256 of a character width.
	 */
	public int GetColumnWidth(int ColumnIndex) {
		return getObject().getColumnWidth(ColumnIndex);
	}
	/**
	 * Sets the column width measured in 1 / 256 of a character width.
	 */
	public void SetColumnWidth(int ColumnIndex, int Width) {
		getObject().setColumnWidth(ColumnIndex, Width);
	}
	/**
	 * Puts an image, previously added with PoiWorkbook.AddImage, on the sheet.
	 *ImageIndex - The value returned from PoiWorkbook.AddImage.
	 *FirstRow, FirstColumn, LastRow, LastColumn - The image cells.
	 */
	public void SetImage (int ImageIndex, int FirstColumn, int FirstRow, int LastColumn ,  int LastRow) {
	    Drawing drawing = getObject().createDrawingPatriarch();
	    CreationHelper helper = getObject().getWorkbook().getCreationHelper();
	    ClientAnchor anchor = helper.createClientAnchor();
	    anchor.setCol1(FirstColumn);
	    anchor.setRow1(FirstRow);
	    anchor.setCol2(LastColumn);
	    anchor.setRow2(LastRow);
	    Picture pict = drawing.createPicture(anchor, ImageIndex);
	    pict.resize(1);
	}
}
