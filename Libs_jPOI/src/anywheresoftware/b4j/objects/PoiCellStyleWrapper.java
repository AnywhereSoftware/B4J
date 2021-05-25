
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


import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;

@ShortName("PoiCellStyle")
public class PoiCellStyleWrapper extends AbsObjectWrapper<CellStyle>{
	public static HorizontalAlignment HORIZONTAL_CENTER = HorizontalAlignment.CENTER;
	public static HorizontalAlignment HORIZONTAL_FILL = HorizontalAlignment.FILL;
	/**
	 * The default alignment.
	 */
	public static HorizontalAlignment HORIZONTAL_GENERAL = HorizontalAlignment.GENERAL;
	public static HorizontalAlignment HORIZONTAL_JUSTIFY = HorizontalAlignment.JUSTIFY;
	public static HorizontalAlignment HORIZONTAL_LEFT = HorizontalAlignment.LEFT;
	public static HorizontalAlignment HORIZONTAL_RIGHT = HorizontalAlignment.RIGHT;
	
	public static BorderStyle BORDER_DASH_DOT = BorderStyle.DASH_DOT;
	public static BorderStyle BORDER_DASH_DOT_DOT = BorderStyle.DASH_DOT_DOT;
	public static BorderStyle BORDER_DASHED = BorderStyle.DASHED;
	public static BorderStyle BORDER_DOTTED = BorderStyle.DOTTED;
	public static BorderStyle BORDER_DOUBLE = BorderStyle.DOUBLE;
	public static BorderStyle BORDER_HAIR = BorderStyle.HAIR;
	public static BorderStyle BORDER_MEDIUM = BorderStyle.MEDIUM;
	public static BorderStyle BORDER_MEDIUM_DASH_DOT = BorderStyle.MEDIUM_DASH_DOT;
	public static BorderStyle BORDER_MEDIUM_DASH_DOT_DOT = BorderStyle.MEDIUM_DASH_DOT_DOT;
	public static BorderStyle BORDER_MEDIUM_DASHED = BorderStyle.MEDIUM_DASHED;
	public static BorderStyle BORDER_NONE = BorderStyle.NONE;
	public static BorderStyle BORDER_SLANTED_DASH_DOT = BorderStyle.SLANTED_DASH_DOT;
	public static BorderStyle BORDER_THICK = BorderStyle.THICK;
	public static BorderStyle BORDER_THIN = BorderStyle.THIN;
	
	public static VerticalAlignment VERTICAL_BOTTOM = VerticalAlignment.BOTTOM;
	public static VerticalAlignment VERTICAL_CENTER = VerticalAlignment.CENTER;
	public static VerticalAlignment VERTICAL_JUSTIFY = VerticalAlignment.JUSTIFY;
	public static VerticalAlignment VERTICAL_TOP = VerticalAlignment.TOP;
	
	
	
	/**
	 * Creates a new CellStyle. Note that you should avoid creating unnecessary PoiCellStyles objects.  
	 */
	public void Initialize(PoiWorkbookWrapper Workbook) {
		setObject(Workbook.getObject().createCellStyle());
	}
	/**
	 * Creates a new CellStyle which is a copy of the given CellStyle.
	 *This is useful if you want to create another style similar to an existing style. 
	 */
	public void InitializeCopy(PoiWorkbookWrapper Workbook, PoiCellStyleWrapper ExistingStyle) {
		CellStyle cs = Workbook.getObject().createCellStyle();
		cs.cloneStyleFrom(ExistingStyle.getObject());
		setObject(cs);
	}
	/**
	 * Gets or sets the horizontal alignment. One of the HORIZONTAL constants.
	 */
	public HorizontalAlignment getHorizontalAlignment() {
		return getObject().getAlignment();
	}
	public void setHorizontalAlignment(HorizontalAlignment s) {
		getObject().setAlignment(s);
	}
	/**
	 * Gets or sets the vertical alignment. One of the VERTICAL constants.
	 */
	public VerticalAlignment getVerticalAlignment() {
		return getObject().getVerticalAlignment();
	}
	public void setVerticalAlignment(VerticalAlignment s) {
		getObject().setVerticalAlignment(s);
	}
	/**
	 * Gets or sets the border style. One of the BORDER constants.
	 */
	public BorderStyle getBorderBottom() {
		return getObject().getBorderBottom();
	}
	public void setBorderBottom(BorderStyle s) {
		getObject().setBorderBottom(s);
	}
	/**
	 * Gets or sets the border style. One of the BORDER constants.
	 */
	public BorderStyle getBorderLeft() {
		return getObject().getBorderLeft();
	}
	public void setBorderLeft(BorderStyle s) {
		getObject().setBorderLeft(s);
	}
	/**
	 * Gets or sets the border style. One of the BORDER constants.
	 */
	public BorderStyle getBorderTop() {
		return getObject().getBorderTop();
	}
	public void setBorderTop(BorderStyle s) {
		getObject().setBorderTop(s);
	}
	/**
	 * Gets or sets the border style. One of the BORDER constants.
	 */
	public BorderStyle getBorderRight() {
		return getObject().getBorderRight();
	}
	public void setBorderRight(BorderStyle s) {
		getObject().setBorderRight(s);
	}
//	/**
//	 * Gets or sets whether the cells using this style are locked.
//	 */
//	public boolean getLocked() {
//		return getObject().getLocked();
//	}
//	public void setLocked(boolean b) {
//		getObject().setLocked(b);
//	}
	/**
	 * Gets or sets whether the cells will be auto sized to fit.
	 */
	public boolean getShrinkToFit() {
		return getObject().getShrinkToFit();
	}
	public void setShrinkToFit(boolean b) {
		getObject().setShrinkToFit(b);
	}
	/**
	 * Gets or sets whether the text should be wrapped.
	 */
	public boolean getWrapText() {
		return getObject().getWrapText();
	}
	public void setWrapText(boolean b) {
		getObject().setWrapText(b);
	}
	/**
	 * Gets or sets the degree of rotation (between -90 to 90).
	 */
	public short getRotation() {
		return getObject().getRotation();
	}
	public void setRotation(short s) {
		getObject().setRotation(s);
	}
	public void SetFont(PoiFontWrapper Font) {
		getObject().setFont(Font.getObject());
	}
	/**
	 * Sets the data format string.
	 */
	public void SetDataFormat(PoiWorkbookWrapper Workbook, String Format) {
		CreationHelper helper = Workbook.getObject().getCreationHelper();
		getObject().setDataFormat(helper.createDataFormat().getFormat(Format));
	}
	/**
	 * Sets the cell's color.
	 *This method is only supported when working with xlsx workbooks (nothing will happen with xls format).
	 */
	public void setForegroundColor(int Color) {
		if (this.getObject() instanceof XSSFCellStyle) {
			XSSFCellStyle cs = (XSSFCellStyle)getObject();
			cs.setFillForegroundColor(createColor(Color));
			cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		} else {
			System.err.println("Can only set color of xlsx workbooks.");
		}
	}
	@Hide
	public static XSSFColor createColor(int Color) {
		return new XSSFColor(new java.awt.Color(Color), new DefaultIndexedColorMap());
	}
	
}
