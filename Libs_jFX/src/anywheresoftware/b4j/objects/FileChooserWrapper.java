
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

import java.io.File;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;

/**
 * A dialog that allows the user to choose one or more files.
 */
@ShortName("FileChooser")
public class FileChooserWrapper extends AbsObjectWrapper<FileChooser> {
	/**
	 * Initializes the object.
	 */
	public void Initialize() {
		setObject(new FileChooser());
	}
	/**
	 * Gets or sets the dialog title.
	 */
	public String getTitle() {
		return getObject().getTitle();
	}
	public void setTitle(String title) {
		getObject().setTitle(title);
	}
	/**
	 * Gets or sets the initial directory.
	 */
	public String getInitialDirectory() {
		return getObject().getInitialDirectory() == null ? "" :getObject().getInitialDirectory().toString(); 
	}
	
	public void setInitialDirectory(String s) {
		getObject().setInitialDirectory(new File(s));
	}
	/**
	 * Gets or sets the initial file name.
	 *Relevant only in "save" dialogs.
	 */
	public String getInitialFileName() {
		try {
			return BA.ReturnString(getObject().getInitialFileName());
		} catch (NoSuchMethodError e) {
			return "";
		}
	}
	
	public void setInitialFileName(String s) {
		try {
			getObject().setInitialFileName(s);
		} catch (NoSuchMethodError e) {
			//nothing
		}
	}
	/**
	 * Sets an extension filter. The chooser will only accept files that end with one of the specified extensions.
	 *Example:<code>
	 *fc.setExtensionFilter("Image", Array As String("*.jpg", "*.png"))</code>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void SetExtensionFilter(String Description, List Extensions) {
		getObject().getExtensionFilters().clear();
		getObject().getExtensionFilters().add(new ExtensionFilter(Description, (java.util.List)Extensions.getObject()));
	}
	/**
	 * Shows the dialog. The user will be asked to choose a single, existing file.
	 *Returns an empty string if no file was chosen.
	 */
	@RaisesSynchronousEvents
	public String ShowOpen(Form Owner) {
		File f = getObject().showOpenDialog(Owner.stage);
		setFolderForNextTime(f);
		String s;
		if (f == null)
			s = "";
		else
			s = f.toString();
		return s;
	}
	/**
	 * Shows the dialog. The user can choose one or more files.
	 *Returns an uninitialized list if the user has not chosen any file.
	 */
	@RaisesSynchronousEvents
	public List ShowOpenMultiple(Form Owner) {
		java.util.List<File> files = getObject().showOpenMultipleDialog(Owner.stage);
		List l1 = new List();
		if (files != null) {
			l1.Initialize();
			for (File f : files) {
				if (l1.getSize() == 0)
					setFolderForNextTime(f);
				l1.Add(f.toString());
			}
		}
		return l1;
	}
	private void setFolderForNextTime(File f) {
		if (f != null && f.getParent() != null)
			setInitialDirectory(f.getParent());
	}
	/**
	 * Shows the dialog. The user can either choose an existing or non-existing file.
	 */
	@RaisesSynchronousEvents
	public String ShowSave(Form Owner) {
		File f = getObject().showSaveDialog(Owner.stage);
		setFolderForNextTime(f);
		String s;
		if (f == null)
			s = "";
		else
			s = f.toString();
		return s;
	}

}
