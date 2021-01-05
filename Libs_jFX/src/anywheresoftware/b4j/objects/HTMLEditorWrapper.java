
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

import com.sun.javafx.scene.web.skin.HTMLEditorSkin;

import javafx.scene.Node;
import javafx.scene.web.HTMLEditor;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.NativeAndWrapper;

/**
 * HTMLEditor is a built-in text editor that is based on a Html engine.
 *HTMLEditor makes it easy for the user to format their text.
 */
@ShortName("HTMLEditor")
public class HTMLEditorWrapper extends ControlWrapper<HTMLEditor>{
	static {
		PaneWrapper.nativeToWrapper.addFirst(new NativeAndWrapper(HTMLEditor.class, HTMLEditorWrapper.class));
	}
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new HTMLEditor());
		super.innerInitialize(ba, eventName, true);
	}
	/**
	 * Gets or sets the Html formatted text.
	 */
	public void setHtmlText(String s) {
		getObject().setHtmlText(s);
	}
	public String getHtmlText() {
		return getObject().getHtmlText();
	}
	
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		HTMLEditor vg = (HTMLEditor) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(HTMLEditor.class, props, designer);
			
		}
		vg.setHtmlText(((String)props.get("text")));
		return ControlWrapper.build(vg, props, designer);
	}
}
