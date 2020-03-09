
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

import javafx.scene.paint.Paint;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFFont;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;

@ShortName("PoiFont")
public class PoiFontWrapper extends AbsObjectWrapper<Font>{
	public void Initialize(PoiWorkbookWrapper Workbook) {
		setObject(Workbook.getObject().createFont());
	}
	/**
	 * Gets or sets the font size.
	 */
	public short getSize() {
		return getObject().getFontHeightInPoints();
	}
	public void setSize(short s) {
		getObject().setFontHeightInPoints(s);
	}
	/**
	 * Gets or sets the font name.
	 */
	public String getName() {
		return getObject().getFontName();
	}
	public void setName(String s) {
		getObject().setFontName(s);
	}
	
	public boolean getBold() {
		return getObject().getBold();
	}
	public void setBold(boolean b) {
		getObject().setBold(b);
	}
	public boolean getItalic() {
		return getObject().getItalic();
	}
	public void setItalic(boolean b) {
		getObject().setItalic(b);
	}
	public boolean getUnderline() {
		return getObject().getUnderline() != Font.U_NONE;
	}
	public void setUnderline(boolean b) {
		getObject().setUnderline(b ? Font.U_SINGLE : Font.U_NONE);
	}
	public void SetColor(Paint Color) {
		if (this.getObject() instanceof XSSFFont) {
			((XSSFFont)this.getObject()).setColor(PoiCellStyleWrapper.createColor(Color));
		}
	}
}
