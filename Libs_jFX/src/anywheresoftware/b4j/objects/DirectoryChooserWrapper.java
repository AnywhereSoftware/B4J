
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

import javafx.stage.DirectoryChooser;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;

/**
 * A dialog that allows the user to choose a directory.
 */
@ShortName("DirectoryChooser")
public class DirectoryChooserWrapper extends AbsObjectWrapper<DirectoryChooser>{
	/**
	 * Initializes the object.
	 */
	public void Initialize() {
		setObject(new DirectoryChooser());
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
		return getObject().getInitialDirectory().toString();
	}
	
	public void setInitialDirectory(String s) {
		getObject().setInitialDirectory(new File(s));
	}
	/**
	 * Shows the modal dialog. Returns the selected directory or an empty string.
	 */
	public String Show(Form Owner) {
		File f = getObject().showDialog(Owner.stage);
		if (f == null)
			return "";
		setInitialDirectory(f.toString());
		return f.toString();
	}
}
