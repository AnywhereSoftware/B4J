
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.input.DataFormat;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.streams.File;
import anywheresoftware.b4j.objects.ImageViewWrapper.ImageWrapper;
import anywheresoftware.b4a.BA.CustomClasses;
import anywheresoftware.b4a.BA.CustomClass;

@CustomClasses(values = {
		@CustomClass(name = "Standard Class", fileNameWithoutExtension = "standard", priority = 1000),
		@CustomClass(name = "Custom View", fileNameWithoutExtension = "customview")
})
/**
 * A class that holds utility methods related to JavaFX apps.
 */
@Version(8.80f)
@DependsOn(values = {"Json"})
@ShortName("JFX")
public class JFX {
	public static Colors Colors;
	/**
	 * Creates a new Font object.
	 */
	public static FontWrapper CreateFont(String FamilyName, double Size, boolean Bold, boolean Italic) {
		FontWrapper fw = new FontWrapper();
		Font f = Font.font(FamilyName, Bold ? FontWeight.BOLD : FontWeight.NORMAL, 
				Italic ? FontPosture.ITALIC : FontPosture.REGULAR, Size);
		fw.setObject(f);
		return fw;
	}
	private static boolean fontAwesomeLoaded, materialIconsLoaded;
	@Hide
	public static void loadFontAwesome() throws IOException {
		if (fontAwesomeLoaded == false) {
			fontAwesomeLoaded = true;
			JFX.LoadFont(File.getDirAssets(), "FontAwesome.otf", 20);
		}
	}
	@Hide
	public static void loadMaterialIcons() throws IOException {
		if (materialIconsLoaded == false) {
			materialIconsLoaded = true;
			JFX.LoadFont(File.getDirAssets(), "MaterialIcons.ttf", 20);
		}
	}
	/**
	 * Returns the FontAwesome font.
	 */
	public static FontWrapper CreateFontAwesome(double Size) throws IOException {
		loadFontAwesome();
		return CreateFont("FontAwesome", Size, false, false);
		
	}
	/**
	 * Returns the Material Icons font.
	 */
	public static FontWrapper CreateMaterialIcons(double Size) throws IOException {
		loadMaterialIcons();
		return CreateFont("Material Icons", Size, false, false);
	}
	/**
	 * Returns a list with all the installed font family names.
	 */
	public static List GetAllFontFamilies() {
		return (List)AbsObjectWrapper.ConvertToWrapper(new List(), Font.getFamilies());
	}
	/**
	 * Returns a Screen object that represents the primary screen.
	 */
	public static ScreenWrapper getPrimaryScreen() {
		return (ScreenWrapper)AbsObjectWrapper.ConvertToWrapper(new ScreenWrapper(), Screen.getPrimary());
	}
	/**
	 * Returns a List with all the screens. Each element in the list is a Screen object.
	 */
	public static List getScreens() {
		List l = new List();
		l.Initialize();
		for (Screen s : Screen.getScreens()) {
			l.Add(s);
		}
		return l;
	}
	/**
	 * Tries to open the given Uri with the default app.
	 *No error will be raised if the file cannot be opened.
	 *Note that you cannot use this method with asset files. It will fail in release mode.
	 *Example:<code>
	 *fx.ShowExternalDocument(File.GetUri("C:\Users\H\Documents", "Document.pdf"))
	 *fx.ShowExternalDocument("http://www.basic4ppc.com")</code>
	 */
	public void ShowExternalDocument(String DocUri) {
		FxBA.application.getHostServices().showDocument(DocUri);
	}
	/**
	 * Returns the system default font.
	 */
	public static FontWrapper DefaultFont(double Size) {
		return (FontWrapper)AbsObjectWrapper.ConvertToWrapper(new FontWrapper(), new Font(null, Size));
	}
	/**
	 * Loads a font resource from the given file. Returns an uninitialized font if loading has failed.
	 *After the font was loaded you can call CreateFont with the font family name to create other variants of the given font. 
	 */
	public static FontWrapper LoadFont(String Dir, String FileName, double Size) throws IOException {
		FontWrapper fw = new FontWrapper();
		try (InputStream in = File.OpenInput(Dir, FileName).getObject()) {
			Font f = Font.loadFont(in, Size);
			fw.setObject(f);
			return fw;
		}
	}
	/**
	 * Loads an image (similar to Image.Initialize).
	 */
	public static ImageWrapper LoadImage(String Dir, String FileName) throws IOException {
		ImageWrapper iw = new ImageWrapper();
		iw.Initialize(Dir, FileName);
		return iw;
	}
	/**
	 * Loads a resized image (similar to Image.InitializeSample).
	 */
	public static ImageWrapper LoadImageSample (String Dir, String FileName, double Width, double Height) throws IOException {
		ImageWrapper iw = new ImageWrapper();
		iw.InitializeSample(Dir, FileName, Width, Height);
		return iw;
	}
	
	private static void setOwnerAndIcon(Dialog<?> d, Form f) {
		if (f != null) {
			d.initOwner(f.stage);
			if (f.getIcon().IsInitialized()) {
				((Stage)d.getDialogPane().getScene().getWindow()).getIcons().add(f.getIcon().getObject());
			}
		}
	}
	/**
	 * Dialogs related constants.
	 */
	public static final DialogResponse DialogResponse = null;
	
	public static Object MSGBOX_INFORMATION = AlertType.INFORMATION;
	public static Object MSGBOX_CONFIRMATION = AlertType.CONFIRMATION;
	public static Object MSGBOX_ERROR = AlertType.ERROR;
	public static Object MSGBOX_NONE = AlertType.NONE;
	public static Object MSGBOX_WARNING = AlertType.WARNING;
	/**
	 * Shows a modal message box.
	 *Owner - The Form that will be set as the window owner. Pass Null if there is no owner.
	 *Message - The dialog message.
	 *Title - The dialog title.
	 *Example:<code>fx.Msgbox(MainForm, "Message", "Title")</code>
	 */
	@RaisesSynchronousEvents
	public static void Msgbox(Form Owner,String Message, String Title) {
		Msgbox2(Owner, Message, Title, "OK", "", "", AlertType.INFORMATION);
	}
	/**
	 * Shows a modal message box that returns a value.
	 *Owner - The Form that will be set as the window owner. Pass Null if there is no owner.
	 *Message - The dialog message.
	 *Title - The dialog title.
	 *Positive - The text to show for the "positive" button. Pass "" if you don't want to show the button.
	 *Cancel - The text to show for the "cancel" button. Pass "" if you don't want to show the button.
	 *Negative - The text to show for the "negative" button. Pass "" if you don't want to show the button.
	 *Style - One of the MSGBOX constants.
	 *Example:<code>
	 *If fx.Msgbox2(MainForm, "Do you want to save changes?", "Example", "Yes", "Cancel", "No", _
	 *	fx.MSGBOX_CONFIRMATION) = fx.DialogResponse.POSITIVE Then
	 *	Log("saving file...")
	 *End If	</code>
	 */
	@RaisesSynchronousEvents
	public static int Msgbox2(Form Owner, String Message, String Title, String Positive, String Cancel, String Negative, Object Style) {
		Alert alrt = new Alert((AlertType)Style);
		setOwnerAndIcon(alrt, Owner);
		String[] texts = new String[] {Positive, Cancel, Negative};
		ButtonData[] datas = new ButtonData[] {ButtonData.YES, ButtonData.CANCEL_CLOSE, ButtonData.NO};
		HashMap<ButtonData, Integer> res = new HashMap<ButtonData, Integer>();
		res.put(ButtonData.YES, anywheresoftware.b4j.objects.DialogResponse.POSITIVE);
		res.put(ButtonData.CANCEL_CLOSE, anywheresoftware.b4j.objects.DialogResponse.CANCEL);
		res.put(ButtonData.NO, anywheresoftware.b4j.objects.DialogResponse.NEGATIVE);
		alrt.getButtonTypes().clear();
		for (int i = 0;i < texts.length;i++) {
			if (texts[i].length() > 0)
				alrt.getButtonTypes().add(new ButtonType(texts[i], datas[i]));
		}
		alrt.setTitle(Title);
		alrt.setContentText(Message);
		alrt.setHeaderText("");
		Optional<ButtonType> bt = alrt.showAndWait();
		if (bt.isPresent() == false)
			return anywheresoftware.b4j.objects.DialogResponse.CANCEL;
		return res.get(bt.get().getButtonData());
	}
	/**
	 * Shows a modal dialog with a list of items. The user can choose one of the items.
	 *Owner - The Form that will be set as the window owner. Pass Null if there is no owner.
	 *List - Items to display.
	 *Message - Dialog message.
	 *Title - Dialog title.
	 *DefaultItem - The index of the default item. Pass -1 for no default.
	 *Example:<code>
	 *Dim items As List = Array("Red", "Green", "Blue")
	 *Dim res As Int = fx.InputList(MainForm, items, "Choose favorite color", "", -1)
	 *If res <> fx.DialogResponse.CANCEL Then
	 *	Log($"Favorite color: ${items.Get(res)}"$)
	 *End If</code>
	 */
	@RaisesSynchronousEvents
	public static int InputList(Form Owner, List Items, String Message, String Title, int DefaultItem) {
		ChoiceDialog<Object> cd;
		if (DefaultItem > -1)
			cd = new ChoiceDialog<Object>(Items.Get(DefaultItem), Items.getObject());
		else
			cd = new ChoiceDialog<Object>(null, Items.getObject());
		setOwnerAndIcon(cd, Owner);
		cd.setTitle(Title);
		cd.setHeaderText(Message);
		Optional<Object> result = cd.showAndWait();
		if (result.isPresent() && result.get() != null) {
			return Items.IndexOf(result.get());
		}
		return anywheresoftware.b4j.objects.DialogResponse.CANCEL;
		
	}
	/**
	 * Provides access to the OS Clipboard.
	 */
	public static final ClipboardWrapper Clipboard = null;
	/**
	 * Returns the available mouse cursors.
	 */
	public static final Cursors Cursors = null;

	@ShortName("Font")
	public static class FontWrapper extends AbsObjectWrapper<Font> {
		/**
		 * Gets the font size.
		 */
		public double getSize() {
			return getObject().getSize();
		}
		/**
		 * Gets the font family name.
		 */
		public String getFamilyName() {
			return getObject().getFamily();
		}
	}
	public static class Cursors {
		public static Cursor DEFAULT = Cursor.DEFAULT;
		public static final Cursor CROSSHAIR = Cursor.CROSSHAIR;
		public static final Cursor TEXT = Cursor.TEXT;
		public static final Cursor WAIT = Cursor.WAIT;
		public static final Cursor OPEN_HAND = Cursor.OPEN_HAND;
		public static final Cursor CLOSED_HAND = Cursor.CLOSED_HAND;
		public static final Cursor HAND = Cursor.HAND;
		public static final Cursor MOVE = Cursor.MOVE;
		public static final Cursor DISAPPEAR = Cursor.DISAPPEAR;
		public static final Cursor NONE = Cursor.NONE;
	}
	public static class Colors {
		public static final Paint Black       = From32Bit(0xFF000000);
		public static final Paint DarkGray      = From32Bit(0xFF444444);
		public static final Paint Gray        = From32Bit(0xFF888888);
		public static final Paint LightGray      = From32Bit(0xFFCCCCCC);
		public static final Paint White       = From32Bit(0xFFFFFFFF);
		public static final Paint Red         = From32Bit(0xFFFF0000);
		public static final Paint Green       = From32Bit(0xFF00FF00);
		public static final Paint Blue        = From32Bit(0xFF0000FF);
		public static final Paint Yellow      = From32Bit(0xFFFFFF00);
		public static final Paint Cyan       = From32Bit(0xFF00FFFF);
		public static final Paint Magenta     = From32Bit(0xFFFF00FF);
		public static final Paint Transparent = From32Bit(0); 
		/**
		 * Creates a new color from a 32 bit number(1 byte for Alpha, R , G and B channels).
		 */
		public static Color From32Bit(int Color) {
			double a = ((Color & 0xff000000) >>> 24) / 255d;
			double r = ((Color & 0xff0000) >>> 16) / 255d;
			double g = ((Color & 0xff00) >>> 8) / 255d;
			double b = ((Color & 0xff)) / 255d;
			return new Color(r, g, b, a);
		}
		/**
		 * Converts the color to a 32 bit number (1 byte for Alpha, R, G and B channels).
		 */
		public static int To32Bit(Paint Color) {
			if (Color instanceof Color == false)
				return 0;
			Color c = (Color)Color;
			int r = (int)Math.round(c.getRed() * 255.0);
			int g = (int)Math.round(c.getGreen() * 255.0);
			int b = (int)Math.round(c.getBlue() * 255.0);
			int a = (int)Math.round(c.getOpacity() * 255.0);
			int i = a;
			i = i << 8;
			i = i | r;
			i = i << 8;
			i = i | g;
			i = i << 8;
			i = i | b;
			return i;
		}
		/**
		 * Creates a new color from the three color components.
		 *Values should be between 0 to 255.
		 */
		public static Color RGB (int R, int G, int B) {
			return Color.rgb(R, G, B);
		}
		/**
		 * Creates a new color from the three components and alpha level.
		 *Values should be between 0 to 255.
		 */
		public static Color ARGB (int Alpha, int R, int G, int B) {
			return Color.rgb(R, G, B, Alpha / 255d);
		}


	}
	/**
	 * Paint object usually represent a color. It can however also represent other types of Paints.
	 */
	@ShortName("Paint")
	public static class PaintWrapper extends AbsObjectWrapper<Paint> {

	}
	/**
	 * Represents a connected monitor.
	 */
	@ShortName("Screen")
	public static class ScreenWrapper extends AbsObjectWrapper<Screen> {
		/**
		 * Returns the screen left boundary X coordinate.
		 */
		public double getMinX() {
			return getObject().getVisualBounds().getMinX();
		}
		/**
		 * Returns the screen top boundary Y coordinate.
		 */
		public double getMinY() {
			return getObject().getVisualBounds().getMinY();
		}
		/**
		 * Returns the screen right boundary X coordinate.
		 */
		public double getMaxX() {
			return getObject().getVisualBounds().getMaxX();
		}
		/**
		 * Returns the screen bottom boundary Y coordinate.
		 */
		public double getMaxY() {
			return getObject().getVisualBounds().getMaxY();
		}
	}
	/**
	 * Allows you to get or set items from the system clipboard.
	 *Use fx.Clipboard to access this object.
	 */
	public static class ClipboardWrapper {
		/**
		 * Copies the given string to the system clipboard.
		 */
		public static void SetString(String Text) {
			HashMap<DataFormat, Object> m = new HashMap<DataFormat, Object>();
			m.put(DataFormat.PLAIN_TEXT, Text);
			javafx.scene.input.Clipboard.getSystemClipboard().setContent(m);
		}
		/**
		 * Checks whether there is a string set in the system clipboard.
		 */
		public static boolean HasString() {
			return javafx.scene.input.Clipboard.getSystemClipboard().hasString();
		}
		/**
		 * Gets a string from the system clipboard.
		 */
		public static String GetString() {
			String s = javafx.scene.input.Clipboard.getSystemClipboard().getString();
			return s == null ? "" : s;
		}
		/**
		 * Copies the image to the system clipboard.
		 */
		public static void SetImage(Image Img) {
			HashMap<DataFormat, Object> m = new HashMap<DataFormat, Object>();
			m.put(DataFormat.IMAGE, Img);
			javafx.scene.input.Clipboard.getSystemClipboard().setContent(m);
		}
		/**
		 * Checks whether there is an image in the system clipboard.
		 */
		public static boolean HasImage() {
			return javafx.scene.input.Clipboard.getSystemClipboard().hasImage();
		}
		/**
		 * Gets an image from the system clipboard.
		 */
		public static ImageWrapper GetImage() {
			ImageWrapper iw = new ImageWrapper();
			iw.setObject(javafx.scene.input.Clipboard.getSystemClipboard().getImage());
			return iw;
		}
		/**
		 * Puts the files in the system clipboard.
		 *Example:<code>
	 *fx.Clipboard.SetFiles(Array As String(File.Combine(File.DirApp, "readme.txt")))</code>
		 */
		public static void SetFiles(List Files) {
			HashMap<DataFormat, Object> m = new HashMap<DataFormat, Object>();
			java.util.List<java.io.File> f = new ArrayList<java.io.File>();
			for (Object s : Files.getObject()) {
				f.add(new java.io.File(String.valueOf(s)));
			}
			m.put(DataFormat.FILES, f);
			javafx.scene.input.Clipboard.getSystemClipboard().setContent(m);
		}
		/**
		 * Checks whether there are files in the system clipboard.
		 */
		public static boolean HasFiles() {
			return javafx.scene.input.Clipboard.getSystemClipboard().hasFiles();
		}
		/**
		 * Returns a list with the files paths.
		 *Each item in the list is a full path to a file.
		 */
		public static List GetFiles() {
			List iw = new List();
			iw.Initialize();
			for (java.io.File f : javafx.scene.input.Clipboard.getSystemClipboard().getFiles())
				iw.Add(f.toString());
			return iw;
		}
		
	}
}
