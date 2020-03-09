
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
 
 package anywheresoftware.b4a.objects;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
/**
 * Holds a thrown exception.
 *You can access the last thrown exception by calling LastException.
 *For example:<code>
 *Try
 *   Dim in As InputStream
 *   in = File.OpenInput(File.DirInternal, "SomeMissingFile.txt")
 *   '...
 *Catch
 *   Log(LastException.Message)
 *End Try
 *If in.IsInitialized Then in.Close</code>
 */
@ShortName("Exception")
public class B4AException extends AbsObjectWrapper<Exception>{
	public String getMessage() {
		return getObject().toString();
	}
}
