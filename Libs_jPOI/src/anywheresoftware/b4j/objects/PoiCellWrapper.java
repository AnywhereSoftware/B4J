
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

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;

@ShortName("PoiCell")
public class PoiCellWrapper extends AbsObjectWrapper<Cell>{
	public static final Object TYPE_BLANK = CellType.BLANK;
	public static final Object TYPE_STRING = CellType.STRING;
	public static final Object TYPE_BOOLEAN = CellType.BOOLEAN;
	public static final Object TYPE_ERROR = CellType.ERROR;
	public static final Object TYPE_FORMULA = CellType.FORMULA;
	public static final Object TYPE_NUMERIC = CellType.NUMERIC;
	
	/**
	 * Gets or sets the cell style object.
	 *PoiCellStyle objects can be shared by multiple cells (if the style is the same).
	 */
	public PoiCellStyleWrapper getCellStyle() {
		return (PoiCellStyleWrapper)AbsObjectWrapper.ConvertToWrapper(new PoiCellStyleWrapper(), getObject().getCellStyle());
	}
	public void setCellStyle(PoiCellStyleWrapper s) {
		getObject().setCellStyle(s.getObject());
	}
	
	/**
	 * Gets the cell column index.
	 */
	public int getColumnIndex() {
		return getObject().getColumnIndex();
	}
	/**
	 * Gets or sets the cell's type. The value will be one of the TYPE constants.
	 */
	public Object getCellType() {
		return getObject().getCellType();
	}
	public void setCellType(Object t) {
		getObject().setCellType((CellType) t);
	}
	/**
	 * Gets or sets the value of a string cell.
	 */
	public String getValueString() {
		return getObject().getStringCellValue();
	}
	public void setValueString(String s) {
		getObject().setCellValue(s);
	}
	/**
	 * Gets or sets the value of a date cell.
	 */
	public long getValueDate() {
		return getObject().getDateCellValue().getTime();
	}
	public void setValueDate(long l) {
		getObject().setCellValue(new Date(l));
	}
	/**
	 * Gets or sets the value of a numeric cell.
	 */
	public double getValueNumeric() {
		return getObject().getNumericCellValue();
	}
	public void setValueNumeric(double d) {
		getObject().setCellValue(d);
	}
	/**
	 * Gets or sets the value of a boolean cell.
	 */
	public boolean getValueBoolean() {
		return getObject().getBooleanCellValue();
	}
	public void setValueBoolean(boolean b) {
		getObject().setCellValue(b);
	}
	/**
	 * Gets or sets the value of a formula cell.
	 */
	public String getValueFormula() {
		return getObject().getCellFormula();
	}
	public void setValueFormula(String b) {
		getObject().setCellFormula(b);
	}
	/**
	 * Returns the cell value based on the cell type.
	 */
	public Object getValue() {
		Cell cell = getObject();
		 switch (getObject().getCellType()) {
         case STRING:
             return cell.getRichStringCellValue().getString();
         case NUMERIC:
             if (DateUtil.isCellDateFormatted(cell)) {
                 return cell.getDateCellValue();
             } else {
                 return cell.getNumericCellValue();
             }
         case BOOLEAN:
             return cell.getBooleanCellValue();
         case FORMULA:
             return cell.getCellFormula();
         default:
             return "";
     }
	}
}
