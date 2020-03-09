
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

@Version(1.30f)
@DependsOn(values = {"poi-4.1.0", "poi-ooxml-4.1.0", "poi-ooxml-schemas-4.1.0", "xmlbeans-3.1.0", "commons-collections4-4.3", "commons-compress-1.18", "commons-math3-3.6.1"})
@ShortName("PoiWorkbook")
@Events(values={"Ready (Success As Boolean)"})
public class PoiWorkbookWrapper extends AbsObjectWrapper<Workbook>{
	public static short PICTURE_TYPE_PNG = Workbook.PICTURE_TYPE_PNG;
	public static short PICTURE_TYPE_JPEG = Workbook.PICTURE_TYPE_JPEG;
	/**
	 * Creates a new workbook.
	 *XLSXFormat - True to use the new Excel 2007 format (xlsx).
	 */
	public void InitializeNew(boolean XLSXFormat) {
		Workbook w = XLSXFormat ? new SXSSFWorkbook() : new HSSFWorkbook();
		setObject(w);
	}
	/**
	 * Initializes the workbook and reads the data from the specified file.
	 *Password - The workbook password. Pass an empty string if there is no password.
	 */
	public void InitializeExisting(String Dir, String FileName, String Password) throws EncryptedDocumentException, InvalidFormatException, IOException {
		File f = new File(Dir, FileName);
		ByteArrayOutputStream bout = new ByteArrayOutputStream((int) f.length());
		anywheresoftware.b4a.objects.streams.File.Copy2(anywheresoftware.b4a.objects.streams.File.OpenInput(Dir, FileName).getObject(), 
				bout);
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		Workbook w = WorkbookFactory.create(bin, Password.length() > 0 ? Password : null);
		setObject(w);
	}
	/**
	 * Similar to InitializeExisting. This method is non-blocking. The Ready event will be fired when the workbook is ready.
	 */
	public void InitializeExistingAsync(BA ba, final String EventName, final String Dir,  final String FileName, final String Password) {
		BA.runAsync(ba, this, EventName + "_ready", new Object[] {false}, new Callable<Object[]>() {
			
			@Override
			public Object[] call() throws Exception {
				InitializeExisting(Dir, FileName, Password);
				return new Object[] {true};
			}
		});
	}
	/**
	 * Closes the Workbook.
	 */
	public void Close() throws IOException {
		if (IsInitialized()) {
			getObject().close();
			setObject(null);
		}
	}
	/**
	 * Adds a new sheet at the specified index.
	 */
	public PoiSheetWrapper AddSheet(String Name, int Index) {
		Sheet s = getObject().createSheet(WorkbookUtil.createSafeSheetName(Name));
		getObject().setSheetOrder(s.getSheetName(), Index);
		return (PoiSheetWrapper)AbsObjectWrapper.ConvertToWrapper(new PoiSheetWrapper(), 
				s);
	}
	/**
	 * Returns the number of sheets.
	 */
	public int getNumberOfSheets() {
		return getObject().getNumberOfSheets();
	}
	/**
	 * Returns the sheet at the specified index (first sheet index is 0).
	 */
	public PoiSheetWrapper GetSheet(int Index) {
		return (PoiSheetWrapper)AbsObjectWrapper.ConvertToWrapper(new PoiSheetWrapper(), 
				getObject().getSheetAt(Index));
	}
	/**
	 * Returns an array with the sheets names.
	 */
	public String[] GetSheetNames() {
		String[] s = new String[getObject().getNumberOfSheets()];
		for (int i = 0;i < s.length;i++)
			s[i] = getObject().getSheetName(i);
		return s;
	}
	/**
	 * Saves the workbook to the given file.
	 */
	public void Save (String Dir, String FileName) throws IOException {
		OutputStream out = anywheresoftware.b4a.objects.streams.File.OpenOutput(Dir, FileName, false).getObject();
		getObject().write(out);
		out.close();
	}
	/**
	 * Adds an image to the workbook. Returns the image index.
	 *Use PoiSheet.SetImage to put this image on a sheet.
	 *Supported formats: png and jpeg (file extension must be correct).
	 */
	public int AddImage(String Dir, String FileName) throws IOException {
		InputStream in = anywheresoftware.b4a.objects.streams.File.OpenInput(Dir, FileName).getObject();
		int res = getObject().addPicture(IOUtils.toByteArray(in), 
				FileName.toLowerCase(BA.cul).endsWith("png") ? Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG);
		in.close();
		return res;
	}
	
	
	
}
