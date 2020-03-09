
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
 
 package anywheresoftware.b4j.xui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.Node;
import javafx.scene.paint.Paint;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.keywords.Bit;
import anywheresoftware.b4a.keywords.Regex;
import anywheresoftware.b4a.keywords.Regex.MatcherWrapper;
import anywheresoftware.b4j.objects.JFX.Colors;

@Hide
public class CSSUtils {
	private static Pattern rgba;
	public void SetBackgroundColor(Node node, Paint color) {
		SetStyleProperty(node, "-fx-background-color", ColorToHex(color));
	}
	public void SetBorder(Node node, double Width, Paint Color, double CornerRadius) {
		SetStyleProperty(node, "-fx-border-color", ColorToHex(Color));
		SetStyleProperty(node, "-fx-border-width", "" + Width);
		SetStyleProperty(node, "-fx-border-radius", "" + CornerRadius);
		SetStyleProperty(node, "-fx-background-radius", "" + CornerRadius);
	}
	public int GetColorFromProperty(Node node, String property, int defaultValue) {
		//#803535
		//rgba(128,53,53,0.93)
		String hex = GetStyleProperty(node, property);
		if (hex == "")
			return defaultValue;
		int a = 255;
		int r, g, b;
		if (hex.startsWith("#")) {
			r = Integer.parseInt(hex.substring(1, 3), 16);
			g = Integer.parseInt(hex.substring(3, 5), 16);
			b = Integer.parseInt(hex.substring(5, 7), 16);
		}
		else {
			if (rgba == null)
				rgba = Pattern.compile("rgba\\((\\d+),(\\d+),(\\d+),([\\d.]+)\\)");
			Matcher m = rgba.matcher(hex);
			if (m.find()) {
				r = Integer.parseInt(m.group(1));
				g = Integer.parseInt(m.group(2));
				b = Integer.parseInt(m.group(3));
				a = Math.round(Float.parseFloat(m.group(4)) * 255);
			} else {
				System.out.println("Cannot parse color string: " + hex);
				return defaultValue;
			}
		}
		return a << 24 | r << 16 | g << 8 | b;
	}
	public String  ColorToHex(Paint _color) {
		int _c = 0;
		int _alpha = 0;
		int _red = 0;
		int _green = 0;
		int _blue = 0;
		anywheresoftware.b4a.keywords.StringBuilderWrapper _sb = null;
		_c = Colors.To32Bit((javafx.scene.paint.Paint)(_color));
		_alpha = Bit.UnsignedShiftRight(_c,(int) (24));
		_red = Bit.And(Bit.UnsignedShiftRight(_c,(int) (16)),(int) (0xff));
		_green = Bit.And(Bit.UnsignedShiftRight(_c,(int) (8)),(int) (0xff));
		_blue = Bit.And(_c,(int) (0xff));
		_sb = new anywheresoftware.b4a.keywords.StringBuilderWrapper();
		_sb.Initialize();
		_sb.Append("rgba(").Append(BA.NumberToString(_red)).Append(",").Append(BA.NumberToString(_green)).Append(",").Append(BA.NumberToString(_blue)).Append(",");
		_sb.Append(anywheresoftware.b4a.keywords.Common.NumberFormat2(_alpha/(double)255,(int) (1),(int) (2),(int) (2),anywheresoftware.b4a.keywords.Common.False)).Append(")");
		return _sb.ToString();
	}
	public void SetStyleProperty(Node node, String key, String value) {
		String att = key + ":" + value + ";";
		MatcherWrapper m = Regex.Matcher(key + ":[^;]+;", node.getStyle());
		String newStyle;
		if (m.Find()) {
			newStyle = node.getStyle().substring(0, m.GetStart(0));
			newStyle = newStyle + att + node.getStyle().substring(m.GetEnd(0));
		} else
			newStyle = node.getStyle() + att;

		node.setStyle(newStyle);

	}
	public String GetStyleProperty(Node node, String key) {
		MatcherWrapper m = Regex.Matcher(key + ":([^;]+);", node.getStyle());
		if (m.Find())
			return m.Group(1);
		else
			return "";
	}
}
