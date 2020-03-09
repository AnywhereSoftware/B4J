
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

import java.io.DataInputStream;
import java.io.IOException;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.ConnectorUtils;

/**
 * Holds values related to the display.
 */
@ShortName("LayoutValues")
public class LayoutValues {
	/**
	 * The device scale value which is equal to 'dots per inch' / 96.
	 */
	public float Scale;
	/**
	 * The display width (pixels).
	 */
	public int Width;
	/**
	 * The display height (pixels).
	 */
	public int Height;
	/**
	 * Returns the approximate screen size.
	 */
	public double getApproximateScreenSize() {
		return Math.sqrt(Math.pow(Width / Scale, 2) + Math.pow(Height / Scale, 2)) / 160;
	}
	@Hide
	public static LayoutValues readFromStream(DataInputStream din) throws IOException {
		LayoutValues lv = new LayoutValues();
		lv.Scale = Float.intBitsToFloat(ConnectorUtils.readInt(din));
		lv.Width = ConnectorUtils.readInt(din);
		lv.Height = ConnectorUtils.readInt(din);
		return lv;
	}
	@Hide
	public float calcDistance(LayoutValues device) {
		float fixedScale = device.Scale / Scale;
		float w = Width * fixedScale;
		float h = Height * fixedScale;
		if (w > device.Width * 1.2)
			return Float.MAX_VALUE;
		if (h > device.Height * 1.2)
			return Float.MAX_VALUE;
		if (w > device.Width)
			w += 50;
		if (h > device.Height)
			h += 50;
		return Math.abs(w - device.Width) + Math.abs(h - device.Height) + 100 * Math.abs(Scale - device.Scale);
	}
	@Override
	public String toString() {
		return ""+  Width + " x " + Height
        + ", scale = " + Scale + " (" + (int)(Scale * 96) + " dpi)";
	}
	
}
