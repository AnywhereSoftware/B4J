
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

import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;

/**
 * A circular progress indicator. A progress value of -1 means that the indicator is in indeterminate mode.
 */
@ShortName("ProgressIndicator")
public class ProgressIndicatorWrapper extends ControlWrapper<ProgressIndicator> {
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new ProgressIndicator());
	}
	/**
	 * Gets or sets the progress. The value should be between 0 to 1. A value of -1 means that the indicator is in indeterminate mode.
	 */
	public double getProgress() {
		return getObject().getProgress();
	}
	public void setProgress(double d) {
		getObject().setProgress(d);
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		Node vg = (Node) prev;
		if (vg == null) 
			vg = NodeWrapper.buildNativeView(ProgressIndicator.class, props, designer);
		ProgressIndicator pb = (ProgressIndicator) ControlWrapper.build(vg, props, designer);
		float progress = (Float)props.get("progress");
		if (designer)
			progress = Math.max(0, progress);
		pb.setProgress(progress);
		return pb;
	}
	/**
	 * A horizontal progress bar. A progress value of -1 means that the indicator is in indeterminate mode.
	 */
	@ShortName("ProgressBar")
	public static class ProgressBarWrapper extends ControlWrapper<ProgressBar> {
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new ProgressBar());
		}
		/**
		 * Gets or sets the progress. The value should be between 0 to 1. A value of -1 means that the indicator is in indeterminate mode.
		 */
		public double getProgress() {
			return getObject().getProgress();
		}
		public void setProgress(double d) {
			getObject().setProgress(d);
		}
		@Hide
		public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
			Node vg = (Node) prev;
			if (vg == null) 
				vg = NodeWrapper.buildNativeView(ProgressBar.class, props, designer);
			ProgressBar pb = (ProgressBar) ControlWrapper.build(vg, props, designer);
			float progress = (Float)props.get("progress");
			if (designer)
				progress = Math.max(0, progress);
			pb.setProgress(progress);
			return pb;
		}
	}
}
