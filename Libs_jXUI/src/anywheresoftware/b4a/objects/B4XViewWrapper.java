
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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.imgscalr.Scalr.Rotation;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CustomClass;
import anywheresoftware.b4a.BA.CustomClasses;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.IterableList;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.streams.File;
import anywheresoftware.b4j.objects.ImageViewWrapper;
import anywheresoftware.b4j.objects.ImageViewWrapper.ImageWrapper;
import anywheresoftware.b4j.objects.JFX;
import anywheresoftware.b4j.objects.JFX.Colors;
import anywheresoftware.b4j.objects.JFX.FontWrapper;
import anywheresoftware.b4j.objects.NodeWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ConcreteNodeWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper.ConcreteControlWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;
import anywheresoftware.b4j.objects.ScrollPaneWrapper;
import anywheresoftware.b4j.xui.CSSUtils;
@CustomClasses(values = {
	@CustomClass(fileNameWithoutExtension="customview", name="Custom View (XUI)")	
})
/**
 * A generic view. Any view can be treated as a B4XView.
 */
@Version(2.11f)
@ShortName("B4XView")
public class B4XViewWrapper extends AbsObjectWrapper<Object>{
	public static final int TOUCH_ACTION_DOWN = 0;
	public static final int TOUCH_ACTION_UP = 1;
	/**
	 * Equivalent to MouseDragged in B4J.
	 */
	public static final int TOUCH_ACTION_MOVE = 2;
	/**
	 * Equivalent to MouseMoved in B4J (will never be raised in B4A or B4i).
	 */
	public static final int TOUCH_ACTION_MOVE_NOTOUCH = 100;
	private ConcreteNodeWrapper nodeWrapper = new ConcreteNodeWrapper();
	private ConcreteNodeWrapper asViewWrapper() {
		nodeWrapper.setObject((Node)getNodeObject());
		return nodeWrapper;
	}
	private Node getNodeObject() {
		return (Node)getObject();
	}
	@Override
	public void setObject(Object o) {
		if (o instanceof ObjectWrapper) {
			o = ((ObjectWrapper<?>)o).getObjectOrNull();
		}
		super.setObject(o);
	}
	private ConcreteControlWrapper asControlWrapper() {
		return (ConcreteControlWrapper)AbsObjectWrapper.ConvertToWrapper(new ConcreteControlWrapper(), getObject());
	}
	private ConcretePaneWrapper asPaneWrapper() {
		return (ConcretePaneWrapper)AbsObjectWrapper.ConvertToWrapper(new ConcretePaneWrapper(), getObject());
	}
	private ImageViewWrapper asImageViewWrapper() {
		return (ImageViewWrapper)AbsObjectWrapper.ConvertToWrapper(new ImageViewWrapper(), getObject());
	}
	/**
	 * Gets or sets whether the view is visible.
	 */
	public boolean getVisible() {
		return asViewWrapper().getVisible();
	}
	public void setVisible(boolean b) {
		asViewWrapper().setVisible(b);
	}
	/**
	 * Gets or sets whether the view is enabled. Does nothing if the view does not support this property.
	 */
	public boolean getEnabled() {
		return asViewWrapper().getEnabled();
	}
	public void setEnabled(boolean b) {
		asViewWrapper().setEnabled(b);
	}
	/**
	 * Gets or sets the left position.
	 */
	public void setLeft(double d) {
		asViewWrapper().setLeft(Math.round(d));
	}
	/**
	 * Gets or sets the top position.
	 */
	public void setTop(double d) {
		asViewWrapper().setTop(Math.round(d));
	}
	/**
	 * Gets or sets the view's width.
	 */
	public void setWidth(double d) {
		ConcreteNodeWrapper c = asViewWrapper();
		c.SetSize(Math.round(d), c.getPrefHeight());
	}
	/**
	 * Gets or sets the view's height.
	 */
	public void setHeight(double d) {
		ConcreteNodeWrapper c = asViewWrapper();
		c.SetSize(c.getPrefWidth(), Math.round(d));
	}
	public double getLeft() {
		return asViewWrapper().getLeft();
	}
	public double getTop() {
		return asViewWrapper().getTop();
	}
	public double getWidth() {
		double w = asViewWrapper().getPrefWidth();
		if (w < 0 && getObject() instanceof Pane) {
			return ((Pane)getObject()).getWidth();
		}
		return w;
	}
	public double getHeight() {
		double h = asViewWrapper().getPrefHeight();
		if (h < 0 && getObject() instanceof Pane) {
			return ((Pane)getObject()).getHeight();
		}
		return h;
	}

	/**
	 * Sets the view size and position.
	 *Duration - Animation duration in milliseconds. Pass 0 to make the change immediately.
	 */
	public void SetLayoutAnimated(int Duration, double Left, double Top, double Width, double Height) {
		Left = Math.round(Left);
		Top = Math.round(Top);
		Width = Math.round(Width);
		Height = Math.round(Height);
		Node n = getNodeObject();
		if (n instanceof Control)
			asControlWrapper().SetLayoutAnimated(Duration, Left, Top, Width, Height);
		else if (n instanceof Pane)
			asPaneWrapper().SetLayoutAnimated(Duration, Left, Top, Width, Height);
		else if (n instanceof ImageView)
			asImageViewWrapper().SetLayoutAnimated(Duration, Left, Top, Width, Height);
		else {
			NodeWrapper.SetLayout(n, new double[] {Left, Top, Width, Height});
		}
	}
	/**
	 * Fades in or fades out the view.
	 */
	public void SetVisibleAnimated(BA ba, int Duration, boolean Visible) {
		ConcreteNodeWrapper nw = asViewWrapper();
		
		if (Visible) {
			nw.setAlpha(0);
			nw.setVisible(true);
			nw.SetAlphaAnimated(Duration, 1);
		} else {
			nw.setAlpha(1);
			nw.SetAlphaAnimated(Duration, 0);
			EventHandler<ActionEvent> e = new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					getNodeObject().setVisible(false);
				}
			};
			if (Duration > 0) {
				@SuppressWarnings("unchecked")
				Animation anim = ((WeakReference<Animation>)AbsObjectWrapper.extraTagsGetValueIfAvailable(getObject(), "animation_SetAlphaAnimated")).get();
				anim.setOnFinished(e);
			}
			else
				e.handle(null);
			
			
			
		}
	}
	/**
	 * Returns the parent. The object returned will be uninitialized if there is no parent.
	 */
	public B4XViewWrapper getParent() {
		return (B4XViewWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XViewWrapper(),asViewWrapper().getParent().getObjectOrNull());
	}
	/**
	 * Removes the view from its parent.
	 */
	public void RemoveViewFromParent() {
		asViewWrapper().RemoveNodeFromParent();
	}
	/**
	 * Requests focus to be set to this view. Returns True if the focus has shifted.
	 *Always returns True in B4J.
	 */
	public boolean RequestFocus() {
		asViewWrapper().RequestFocus();
		return true;
	}
	/**
	 * Gets or sets the progress value. The progress value is scaled between 0 to 100 (this is different than the native views range in B4J and B4i). 
	 * Supported types:
	 * B4A - ProgressBar
	 * B4J - ProgressView, ProgressIndicator
	 * B4i - ProgressView
	 *<b>Value should be between 0 to 100.</b>
	 */
	public void setProgress(int i) {
		ProgressIndicator pb = (ProgressIndicator)getObject();
		pb.setProgress(i / 100d);
	}
	public int getProgress() {
		return (int) Math.round(((ProgressIndicator)getObject()).getProgress() * 100);
	}
	/**
	 * Gets or sets the hint or prompt text. Supported types:
	 *B4A - EditText
	 *B4i - TextField
	 *B4J - TextArea, TextField
	 */
	public String getEditTextHint() {
		return ((TextInputControl)getObject()).getPromptText();
	}
	public void setEditTextHint(String s) {
		((TextInputControl)getObject()).setPromptText(s);
	}
	/**
	 * Gets or sets the text. Supported types: 
	 *B4A - EditText, Label, Button, CheckBox, RadioButton, ToggleButton
	 *B4i - TextField, TextView, Button, Label
	 *B4J - TextField, TextArea, Label, Button, CheckBox, RadioButton, ToggleButton
	 */
	public void setText(String s) {
		Node n = getNodeObject();
		if (n instanceof Labeled)
			((Labeled)n).setText(s);
		else if (n instanceof TextInputControl)
			((TextInputControl)n).setText(s);
		else
			throw typeDoesNotMatch();
	}
	public String getText() {
		Node n = getNodeObject();
		if (n instanceof Labeled)
			return ((Labeled)n).getText();
		else if (n instanceof TextInputControl)
			return ((TextInputControl)n).getText();
		else
			throw typeDoesNotMatch();
	}
	/**
	 * Gets or sets the text colors. Supported types:
	 * B4A - EditText, Label, Button, CheckBox, RadioButton,  ToggleButton
	 * B4i - TextField, TextView, Label
	 * B4J - All types. Based on the native TextColor property if available or -fx-text-fill CSS attribute.
	 */
	public void setTextColor(int c) {
		Node n = getNodeObject();
		if (n instanceof Labeled)
			((Labeled)n).setTextFill(Colors.From32Bit(c));
		else
			XUI.css.SetStyleProperty(n, "-fx-text-fill", XUI.css.ColorToHex(Colors.From32Bit(c)));
	}
	public int getTextColor() {
		Node n = getNodeObject();
		if (n instanceof Labeled)
			return Colors.To32Bit(((Labeled)n).getTextFill());
		else {
			return XUI.css.GetColorFromProperty(getNodeObject(), "-fx-text-fill", 0);
		}
		
	}
	/**
	 * Sets the text size.
	 *Supported types:
	 *B4A - EditText, Label, Button, CheckBox, RadioButton, ToggleButton
	 *B4i - TextField, TextView, Button, Label. <b>Only labels are animated.</b>
	 *B4J - Sets the TextSize property if available and the CSS attribute for other types.
	 */
	public void SetTextSizeAnimated(int Duration, double TextSize) {
		if (Duration == 0) {
			asViewWrapper().raiseAnimationCompletedEvent(null, "SetTextSizeAnimated");
			setTextSize(TextSize);
			return;
		}
		final Node n = getNodeObject(); 
		boolean isLabeled = n instanceof Labeled;
		String fontName = isLabeled ? ((Labeled)n).getFont().getName() : null;
		SimpleObjectProperty<Double> prop = new SimpleObjectProperty<>(getTextSize());
		prop.addListener(new ChangeListener<Double>() {

			@Override
			public void changed(ObservableValue<? extends Double> observable,
					Double oldValue, Double newValue) {
				if (isLabeled)
					((Labeled)n).setFont(new Font(fontName, newValue));
				else
					XUI.css.SetStyleProperty(n, "-fx-font-size", String.format(BA.cul,"%.2f", newValue));
			}
		});
		KeyValue a = new KeyValue(prop, TextSize);
		KeyFrame frame = new KeyFrame(javafx.util.Duration.millis(Duration), a);
		Timeline timeline = new Timeline(frame);
		asViewWrapper().raiseAnimationCompletedEvent(timeline, "SetTextSizeAnimated");
		timeline.play();
	}
	/**
	 * Gets or sets the text size.
	 *Supported types:
	 *B4A - EditText, Label, Button, CheckBox, RadioButton, ToggleButton
	 *B4i - TextField, TextView, Button, Label
	 *B4J - Returns the TextSize property if available and the CSS attribute for other types. Returns 0 if attribute not available.
	 */
	public double getTextSize() {
		if (getNodeObject() instanceof Labeled)
			return ((Labeled)getObject()).getFont().getSize();
		String size = XUI.css.GetStyleProperty(getNodeObject(),"-fx-font-size");
		if (size.length() != 0)
			return Double.parseDouble(size);

		return 0;
	}
	public void setTextSize(double d) {
		if (getNodeObject() instanceof Labeled)
			((Labeled)getObject()).setFont(new Font(((Labeled)getObject()).getFont().getName(), d));
		else
			XUI.css.SetStyleProperty(getNodeObject(), "-fx-font-size", String.format(BA.cul,"%.2f", d));
	}
	/**
	 * Gets or sets the font (typeface and text size).
	 *Supported types:
	 *B4A - EditText, Label, Button, CheckBox, RadioButton, ToggleButton
	 *B4i - TextField, TextView, Button, Label
	 *B4J - Sets the Font property if available. Otherwise sets the CSS attribute.
	 */
	public B4XFont getFont() {
		Font f = null;
		if (getNodeObject() instanceof Labeled)
			f = ((Labeled)getObject()).getFont();
		else {
			String FontName = XUI.css.GetStyleProperty(getNodeObject(), "-fx-font-family");
			if (FontName.length() != 0)
				f = new Font(FontName, getTextSize());
		}
		return (B4XFont) AbsObjectWrapper.ConvertToWrapper(new B4XFont(), f);
	}
	public void setFont(B4XFont f) {
		if (getNodeObject() instanceof Labeled)
			((Labeled)getObject()).setFont(f.getObject());
		else {
			XUI.css.SetStyleProperty(getNodeObject(), "-fx-font-size", String.format(BA.cul,"%.2f", f.getObject().getSize()));
			XUI.css.SetStyleProperty(getNodeObject(), "-fx-font-family", f.getObject().getName());
		}
	}
	/**
	 * Sets the text alignment.
	 *Vertical - TOP, CENTER or BOTTOM.
	 *Horizontal - LEFT, CENTER or RIGHT.
	 *
	 *In B4i the vertical alignment has no effect.
	 * Supported types:
	 * B4A - EditText, Label, Button, CheckBox, RadioButton, ToggleButton
	 * B4J - Label, Button, Checkbox, RadioButton, ToggleButton, TextField
	 */
	public void SetTextAlignment(String Vertical, String Horizontal) {
		String s = Vertical + "_" + Horizontal;
		if (s.equals("CENTER_CENTER"))
			s = "CENTER";
		Node n = getNodeObject();
		if (n instanceof Labeled)
			((Labeled)n).setAlignment(Enum.valueOf(Pos.class, s));
		else if (n instanceof TextField)
			((TextField)n).setAlignment(Enum.valueOf(Pos.class, s));
		else
			throw typeDoesNotMatch();
	}
	/**
	 * Gets or sets the checked state (also named selected or value). 
	 *Supported types:
	 *B4A - CheckBox, RadioButton, ToggleButton, Switch
	 *B4i - Switch
	 *B4J: CheckBox, RadioButton, ToggleButton.
	 */
	public void setChecked(boolean b) {
		Node n = getNodeObject();
		if (n instanceof CheckBox)
			((CheckBox)n).setSelected(b);
		else if (n instanceof RadioButton)
			((RadioButton)n).setSelected(b);
		else if (n instanceof ToggleButton)
			((ToggleButton)n).setSelected(b);
		else
			throw typeDoesNotMatch();
	}
	public boolean getChecked() {
		Node n = getNodeObject();
		if (n instanceof CheckBox)
			return ((CheckBox)n).isSelected();
		else if (n instanceof RadioButton)
			return ((RadioButton)n).isSelected();
		else if (n instanceof ToggleButton)
			return ((ToggleButton)n).isSelected();
		else
			throw typeDoesNotMatch();
	}
	/**
	 * Returns an iterator that iterates over all the child views including views that were added to other child views.
	 *Make sure to check the view type as it might return subviews as well.
	 *Example:<code>
	 *For Each v As B4XView In Panel1.GetAllViewsRecursive
	 *	...
	 *Next</code>
	 *Supported types
	 *B4A - Activity, Panel
	 *B4i - Panel
	 *B4J - Pane
	 */
	public IterableList GetAllViewsRecursive() {
		return asPaneWrapper().GetAllViewsRecursive();
	}
	/**
	 * Loads the layout file.
	 *Supported types
	 *B4A - Panel
	 *B4i - Panel
	 *B4J - Pane
	 */
	@RaisesSynchronousEvents
	public void LoadLayout(String LayoutFile, BA ba) throws Exception {
		asPaneWrapper().LoadLayout(ba, LayoutFile);
	}
	/**
	 * Returns the view at the given index.
	 *Supported types
	 *B4A - Activity, Panel
	 *B4i - Panel
	 *B4J - Pane
	 */
	public B4XViewWrapper GetView(int Index) {
		return (B4XViewWrapper) AbsObjectWrapper.ConvertToWrapper(new B4XViewWrapper(),asPaneWrapper().GetNode(Index).getObject());
	}
	/**
	 * Adds a view.
	 *Supported types
	 *B4A - Activity, Panel
	 *B4i - Panel
	 *B4J - Pane
	 */
	public void AddView(Node View, double Left, double Top, double Width, double Height) {
		asPaneWrapper().AddNode(View, Left, Top, Width, Height);
	}
	/**
	 * Removes all views.
	 *Supported types
	 *B4A - Activity, Panel
	 *B4i - Panel
	 *B4J - Pane
	 */
	public void RemoveAllViews() {
		asPaneWrapper().RemoveAllNodes();
	}
	/**
	 * Returns the number of direct child views.
	 *Supported types
	 *B4A - Activity, Panel
	 *B4i - Panel
	 *B4J - Pane
	 */
	public int getNumberOfViews() {
		return asPaneWrapper().getNumberOfNodes();
	}
	/**
	 *Captures the views appearance.
	 */
	public B4XBitmapWrapper Snapshot() {
		return (B4XBitmapWrapper) AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), asViewWrapper().Snapshot2(Colors.Transparent).getObjectOrNull());
	}
	/**
	 * Sets the view's bitmap.
	 * Supported types:
	 * B4A - All views. The view's Drawable will be set to a BitmapDrawable with Gravity set to CENTER.
	 * B4i - ImageView. ContentMode set to Fit.
	 * B4J - ImageView. PreserveRatio is set to True. 
	 */
	public void SetBitmap(Image Bitmap) {
		ImageViewWrapper i = asImageViewWrapper();
		i.SetImage(Bitmap);
		i.setPreserveRatio(true);
	}
	/**
	 * Gets the view's bitmap.
	 * Supported types:
	 * B4A - All views when the background drawable is a BitmapDrawable.
	 * B4i and B4J - ImageView
	 */
	public B4XBitmapWrapper GetBitmap() {
		return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), asImageViewWrapper().GetImage().getObjectOrNull());
	}
	private RuntimeException typeDoesNotMatch() {
		return new RuntimeException("Type does not match (" + getObject().getClass() + ")");
	}
	/**
	 * Gets or sets the background color. Returns 0 if the color is not available.
	 */
	public void setColor(int Color) {
		XUI.css.SetBackgroundColor(getNodeObject(), Colors.From32Bit(Color));
	}
	public int getColor() {
		return XUI.css.GetColorFromProperty(getNodeObject(), "-fx-background-color", 0);
	}
	
	/**
	 * Changes the background color with a transition animation between the FromColor and the ToColor colors.
	 *Duration - Animation duration measured in milliseconds.
	 *
	 *Note that the animation does not work with labels in B4i. It will change immediately.
	 *You can put a transparent label inside a panel and animate the panel color instead.
	 */
	public void SetColorAnimated(int Duration, int FromColor, int ToColor) {
		if (Duration == 0) {
			asViewWrapper().raiseAnimationCompletedEvent(null, "SetColorAnimated");
			setColor(ToColor);
			return;
		}
		final Node n = getNodeObject(); 
		SimpleObjectProperty<Color> prop = new SimpleObjectProperty<>(Colors.From32Bit(FromColor));
		prop.addListener(new ChangeListener<Color>() {

			@Override
			public void changed(ObservableValue<? extends Color> observable,
					Color oldValue, Color newValue) {
				XUI.css.SetBackgroundColor(n, newValue);
			}
		});
		KeyValue a = new KeyValue(prop, Colors.From32Bit(ToColor));
		KeyFrame frame = new KeyFrame(javafx.util.Duration.millis(Duration), a);
		Timeline timeline = new Timeline(frame);
		asViewWrapper().raiseAnimationCompletedEvent(timeline, "SetColorAnimated");
		timeline.play();
		
	}
	
	/**
	 * Sets the background color and border.
	 * B4A - The view's drawable will be set to ColorDrawable.
	 */
	public void SetColorAndBorder(int BackgroundColor, double BorderWidth, int BorderColor, double BorderCornerRadius) {
		XUI.css.SetBackgroundColor(getNodeObject(),  Colors.From32Bit(BackgroundColor));
		XUI.css.SetBorder(getNodeObject(), BorderWidth,  Colors.From32Bit(BorderColor), BorderCornerRadius);
	}
	private ScrollPaneWrapper asScrollPaneWrapper() {
		return (ScrollPaneWrapper)AbsObjectWrapper.ConvertToWrapper(new ScrollPaneWrapper(), getObject());
	}
	public double getScrollViewOffsetY() {
		ScrollPaneWrapper s = asScrollPaneWrapper();
		return s.getVPosition() * (((Pane)s.getObject().getContent()).getPrefHeight() - s.getHeight());
	}
	/**
	 * Gets or sets the vertical scroll position.
	 *Supported types:
	 *B4A - ScrollView (returns 0 for HorizontalScrollView).
	 *B4i - ScrollView
	 *B4J - ScrollPane
	 */
	public void setScrollViewOffsetY(double d) {
		ScrollPaneWrapper s = asScrollPaneWrapper();
		s.setVPosition(d / (((Pane)s.getObject().getContent()).getPrefHeight() - s.getHeight()));
	}
	/**
	 * Gets or sets the horizontal scroll position.
	 *Supported types:
	 *B4A - HorizontalScrollView (returns 0 for ScrollView).
	 *B4i - ScrollView
	 *B4J - ScrollPane
	 */
	public double getScrollViewOffsetX() {
		ScrollPaneWrapper s = asScrollPaneWrapper();
		return s.getHPosition() * (((Pane)s.getObject().getContent()).getPrefWidth() - s.getWidth());
	}
	public void setScrollViewOffsetX(double d) {
		ScrollPaneWrapper s = asScrollPaneWrapper();
		s.setHPosition(d / (((Pane)s.getObject().getContent()).getPrefWidth() - s.getWidth()));
	}
	/**
	 * Gets or sets the scroll view inner panel.
	 *Supported types:
	 *B4A - HorizontalScrollView, ScrollView
	 *B4i - ScrollView
	 *B4J - ScrollPane
	 */
	public B4XViewWrapper getScrollViewInnerPanel() {
		return (B4XViewWrapper) AbsObjectWrapper.ConvertToWrapper(new B4XViewWrapper(),asScrollPaneWrapper().getInnerNode().getObjectOrNull());
	}
	/**
	 * Gets or set the scroll view inner panel height.
	 *Supported types:
	 *B4A - HorizontalScrollView, ScrollView
	 *B4i - ScrollView
	 *B4J - ScrollPane
	 */
	public double getScrollViewContentHeight() {
		return asScrollPaneWrapper().getInnerNode().getPrefHeight();
	}
	public void setScrollViewContentHeight(double d) {
		asScrollPaneWrapper().getInnerNode().setPrefHeight(d);
	}
	public double getScrollViewContentWidth() {
		return asScrollPaneWrapper().getInnerNode().getPrefWidth();
	}
	/**
	 * Gets or set the scroll view inner panel width.
	 *Supported types:
	 *B4A - HorizontalScrollView, ScrollView
	 *B4i - ScrollView
	 *B4J - ScrollPane
	 */
	public void setScrollViewContentWidth(double d) {
		asScrollPaneWrapper().getInnerNode().setPrefWidth(d);
	}
	/**
	 * Gets or sets the view's tag object.
	 */
	public Object getTag() {
		return asViewWrapper().getTag();
	}
	public void setTag(Object o) {
		asViewWrapper().setTag(o);
	}
	/**
	 * Changes the Z order of this view and sends it to the back.
	 */
	public void SendToBack() {
		getNodeObject().toBack();
	}
	/**
	 * Changes the Z order of this view and brings it to the front.
	 */
	public void BringToFront() {
		getNodeObject().toFront();
	}
	/**
	 * Rotates the view with animation.
	 *Duration - Animation duration in milliseconds.
	 *Degree - Rotation degree.
	 */
	public void SetRotationAnimated(int Duration, double Degree) {
		if (Duration == 0) {
			setRotation(Degree);
			asViewWrapper().raiseAnimationCompletedEvent(null, "SetRotationAnimated");
		}
		Node v = getNodeObject();
		KeyValue a = new KeyValue(v.rotateProperty(), Degree);
		KeyFrame frame = new KeyFrame(javafx.util.Duration.millis(Duration), a);
		Timeline timeline = new Timeline(frame);
		timeline.play();
		asViewWrapper().raiseAnimationCompletedEvent(timeline, "SetRotationAnimated");
	}
	/**
	 * Gets or sets the view's rotation transformation (in degrees).
	 */
	public double getRotation() {
		return getNodeObject().getRotate();
	}
	public void setRotation(double f) {
		getNodeObject().setRotate(f);
	}
	
	/**
	 * Gets or sets the selection start index.
	 *See also: B4XView.SetSelection.
	 *Supported types:
	 *B4A - EditText
	 *B4i - TextField and TextView
	 *B4J - TextField and TextArea
	 */
	public int getSelectionStart() {
		return ((TextInputControl)getObject()).getSelection().getStart();
	}
	public void setSelectionStart(int s) {
		SetSelection(s, 0);
	}
	/**
	 * Gets the selection length.
	 *See also: B4XView.SetSelection.
	 *Supported types:
	 *B4A - EditText
	 *B4i - TextField and TextView
	 *B4J - TextField and TextArea
	 */
	public int getSelectionLength() {
		return ((TextInputControl)getObject()).getSelection().getLength();
	}
	/**
	 * Sets the selection.
	 *Supported types:
	 *B4A - EditText
	 *B4i - TextField and TextView
	 *B4J - TextField and TextArea
	 */
	public void SetSelection(int Start, int Length) {
		((TextInputControl)getObject()).selectRange(Start, Start + Length);
	}
	/**
	 * Selects all text.
	 *Supported types:
	 *B4A - EditText
	 *B4i - TextField and TextView
	 *B4J - TextField and TextArea
	 */
	public void SelectAll() {
		((TextInputControl)getObject()).selectAll();
	}
	/**
	 * Represents a loaded image. Similar to B4A Bitmap, B4i Bitmap and B4J Image.
	 */
	@ShortName("B4XBitmap")
	public static class B4XBitmapWrapper extends AbsObjectWrapper<Image> {
		/**
		 * Returns the bitmap's width.
		 */
		public double getWidth() {
			return getObject().getWidth();
		}
		/**
		 * Returns the bitmap's height.
		 */
		public double getHeight() {
			return getObject().getHeight();
		}
		/**
		 * Writes the bitmap to the output stream.
		 *Quality - Value between 0 (smaller size, lower quality) to 100 (larger size, higher quality), 
		 *which is a hint for the compressor for the required quality.
		 *Format - JPEG or PNG.
		 * 
		 *Example:<code>
		 *Dim Out As OutputStream
		 *Out = File.OpenOutput(XUI.DefaultFolder, "Test.png", False)
		 *Bitmap1.WriteToStream(out, 100, "PNG")
		 *Out.Close</code>
		 */
		public void WriteToStream(OutputStream Out, int Quality, String Format) throws IOException {
			if (Format.equals("PNG"))
				((ImageWrapper)AbsObjectWrapper.ConvertToWrapper(new ImageWrapper(), getObject())).WriteToStream(Out);
			else if (Format.equals("JPEG")) {
				BufferedImage imageRGB =  new BufferedImage((int)getObject().getWidth(), (int)getObject().getHeight(), 
						BufferedImage.OPAQUE); 
				Graphics2D graphics = imageRGB.createGraphics();
				graphics.drawImage(
						SwingFXUtils.fromFXImage(getObject(), null), 0, 0, null);
				JPEGImageWriteParam param = new JPEGImageWriteParam(null);
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(Quality / 100f);
				ImageWriter iw = ImageIO.getImageWritersByFormatName("jpg").next();
				iw.setOutput(ImageIO.createImageOutputStream(Out));
				iw.write(null, new IIOImage(imageRGB, null, null),
						param);
			}
			else
				throw new RuntimeException("Unknown format: " + Format);
		}
		/**
		 * Returns a <b>new</b> bitmap with the given width and height.
		 */
		public B4XBitmapWrapper Resize(int Width, int Height, boolean KeepAspectRatio) {
			BufferedImage bi = SwingFXUtils.fromFXImage(getObject(), null);
			BufferedImage res = Scalr.resize(bi, KeepAspectRatio ? Mode.BEST_FIT_BOTH : Mode.FIT_EXACT, Width, Height);
			bi.flush();
			Image i = SwingFXUtils.toFXImage(res, null);
			res.flush();
			return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), i);
		}
		/**
		 * Returns a <b>new</b> rotated bitmap. The bitmap will be rotated clockwise.
		 *The following values are supported on all three platforms: 0, 90, 180, 270.
		 */
		public B4XBitmapWrapper Rotate(int Degrees) {
			Degrees = (Degrees + 360) % 360;
			Degrees = (Degrees / 90) * 90;
			if (Degrees == 0)
				return this;
			Rotation r = Rotation.valueOf("CW_" + Degrees);
			BufferedImage bi = SwingFXUtils.fromFXImage(getObject(), null);
			BufferedImage res = Scalr.rotate(bi, r);
			bi.flush();
			Image i = SwingFXUtils.toFXImage(res, null);
			res.flush();
			return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), i);
		}
		/**
		 * Returns a <b>new</b> cropped bitmap.
		 */
		public B4XBitmapWrapper Crop(int Left, int Top, int Width, int Height) {
			BufferedImage bi = SwingFXUtils.fromFXImage(getObject(), null);
			BufferedImage res = Scalr.crop(bi, Left, Top, (int)Math.min(Width, getObject().getWidth() - Left), 
					(int)Math.min(Height, getObject().getHeight() - Top));
			bi.flush();
			Image i = SwingFXUtils.toFXImage(res, null);
			res.flush();
			return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), i);
		}
		/**
		 * Returns the bitmap scale. It will always be 1 in B4J and B4i.
		 */
		public float getScale() {
			return 1f;
		}

	}
	/**
	 * An object that holds a typeface and size.
	 */
	@ShortName("B4XFont")
	public static class B4XFont extends AbsObjectWrapper<Font>{
		public double getSize() {
			return getObject().getSize();
		}
		/**
		 * Returns a native font object representing the same font.
		 */
		public FontWrapper ToNativeFont() {
			return (FontWrapper)AbsObjectWrapper.ConvertToWrapper(new FontWrapper(), getObject());
		}
	}
	/**
	 * The XUI object includes various methods and utilities.
	 */
	@ShortName("XUI")
	public static class XUI {
		@Hide
		public static CSSUtils css = new CSSUtils();  
		public static final int Color_Black       = (0xFF000000);
		public static final int Color_DarkGray      = (0xFF444444);
		public static final int Color_Gray        = (0xFF888888);
		public static final int Color_LightGray      = (0xFFCCCCCC);
		public static final int Color_White       = (0xFFFFFFFF);
		public static final int Color_Red         = (0xFFFF0000);
		public static final int Color_Green       = (0xFF00FF00);
		public static final int Color_Blue        = (0xFF0000FF);
		public static final int Color_Yellow      = (0xFFFFFF00);
		public static final int Color_Cyan       = (0xFF00FFFF);
		public static final int Color_Magenta     = (0xFFFF00FF);
		public static final int Color_Transparent = (0); 
		private static String DataFolderName;
		/**
		 * Loads a bitmap. In most cases you should use LoadBitmapResize instead.
		 */
		public static B4XBitmapWrapper LoadBitmap(String Dir, String FileName) throws IOException {
			ImageWrapper iw = new ImageWrapper();
			iw.Initialize(Dir, FileName);
			return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), iw.getObject());
		}
		/**
		 * Loads and resizes a bitmap.
		 */
		public static B4XBitmapWrapper LoadBitmapResize(String Dir, String FileName, int Width, int Height, boolean KeepAspectRatio) throws IOException {
			return LoadBitmap(Dir, FileName).Resize(Width, Height, KeepAspectRatio);
		}
		/**
		 * B4A, B4i - Does nothing.
		 * B4J - Sets the subfolder name on Windows and Mac. The actual path will be similar to: C:\Users\[user name]\AppData\Roaming\[AppName].
		 */
		public static void SetDataFolder(String AppName) {
			DataFolderName = AppName;
		}
		/**
		 *B4A - Same as File.DirInternal.
		 *B4i - Same as File.DirDocuments.
		 *B4J - Same as File.DirData. You must first call SetDataFolder once before you can use this folder.
		 */
		public static String getDefaultFolder() {
			if (DataFolderName == null)
				throw new RuntimeException("SetDataFolder must be called before using this method.");
			try {
				return File.DirData(DataFolderName);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		/**
		 * B4A, B4i - Does nothing.
		 * B4J - Converts a Paint object to an Int color. Does not do anything if the color is already an Int color.
		 */
		public static int PaintOrColorToColor(Object Color) {
			if (Color instanceof Paint)
				return Colors.To32Bit((Paint)Color);
			else
				return (int)Color;
		}
		/**
		 * Same as SubExists keyword. Adds an additional parameter that is required in B4i (number of parameters).
		 */
		public static boolean SubExists(BA ba, Object Component, String Sub, int NotUsed) throws IllegalArgumentException, SecurityException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
			return Common.SubExists(ba, Component, Sub);
		}
		/**
		 * Creates a new B4XFont from the given font and size. 
		 *Do NOT use DIP units with font sizes.
		 */
		public static B4XFont CreateFont(Font Font, float Size) {
			return (B4XFont)AbsObjectWrapper.ConvertToWrapper(new B4XFont(), new Font(Font.getName(), Size));
		}
		/**
		 * Creates a new B4XFont from the given B4XFont and size.
		 *Do NOT use DIP units with font sizes.
		 */
		public static B4XFont CreateFont2(B4XFont B4XFont, float Size) {
			return CreateFont(B4XFont.getObject(), Size);
		}
		/**
		 * Creates a new B4XFont based on FontAwesome font.
		 */
		public static B4XFont CreateFontAwesome(float Size) throws IOException {
			return CreateFont(JFX.CreateFontAwesome(Size).getObject(), Size);
		}
		/**
		 * Creates a new B4XFont based on Material Icons font.
		 */
		public static B4XFont CreateMaterialIcons(float Size) throws IOException {
			return CreateFont(JFX.CreateMaterialIcons(Size).getObject(), Size);
		}
		/**
		 * Create a new B4XFont with the default typeface.
		 *Do NOT use DIP units with font sizes.
		 */
		public static B4XFont CreateDefaultFont(float Size) {
			return (B4XFont)AbsObjectWrapper.ConvertToWrapper(new B4XFont(), new Font(Size));
		}
		/**
		 * Create a new B4XFont with the default bold typeface.
		 *Do NOT use DIP units with font sizes.
		 */
		public static B4XFont CreateDefaultBoldFont(float Size) {
			return (B4XFont)AbsObjectWrapper.ConvertToWrapper(new B4XFont(), Font.font(null, FontWeight.BOLD, Size));
		}
		/**
		 * Returns True in B4A.
		 */
		public static boolean getIsB4A() {
			return false;
		}
		/**
		 * Returns True in B4i.
		 */
		public static boolean getIsB4i() {
			return false;
		}
		/**
		 * Returns True in B4J.
		 */
		public static boolean getIsB4J() {
			return true;
		}
		/**
		 * Returns the screen normalized scale.
		 *Always returns 1 in B4J and B4i.
		 *Returns the same value as 100dip / 100 in B4A.
		 */
		public static float getScale() {
			return 1;
		}
		/**
		 * Creates a Panel (or Pane in B4J).
		 *Note that the panel created will clip its child views. 
		 *In B4A, this method can only be called from an Activity context.
		 */
		public static B4XViewWrapper CreatePanel(BA ba, String EventName) {
			ConcretePaneWrapper p = new ConcretePaneWrapper();
			p.Initialize(ba, EventName);
			final Rectangle clipRectangle = new Rectangle();
			p.getObject().setClip(clipRectangle);
			p.getObject().layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {

				@Override
				public void changed(
						ObservableValue<? extends Bounds> arg0,
						Bounds arg1, Bounds arg2) {
					clipRectangle.setWidth(arg2.getWidth());
					clipRectangle.setHeight(arg2.getHeight());
				}
			});
			return (B4XViewWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XViewWrapper(), p.getObject());
		}
		/**
		 * Returns the color value from the components. Values should be between 0 to 255.
		 */
		public static int Color_RGB(int R, int G, int B) {
			return Color_ARGB(0xff, R, G, B);
		}
		/**
		 * Returns the color value from the components. Values should be between 0 to 255.
		 */
		public static int Color_ARGB(int Alpha, int R, int G, int B) {
			return (Alpha << 24) | (R << 16) | (G << 8) | B;
		}
		public static final int DialogResponse_Positive = -1;
		public static final int DialogResponse_Cancel = -3;
		public static final int DialogResponse_Negative = -2;
		
		/**
		 * Shows a non-modal Msgbox.
		 *Returns an object that can be used as the sender filter parameter for the optional Msgbox_Result event.
		 *Example:<code>xui.MsgboxAsync("Hello", "World")</code>
		 */
		public static Object MsgboxAsync(BA ba, String Message, String Title) throws Exception {
			return Msgbox2AsyncImpl(ba, Message, Title, "OK", "", "", null, AlertType.INFORMATION);
		}
		/**
		 * Shows a non-modal Msgbox.
		 *Returns an object that can be used as the sender filter parameter for the Msgbox_Result event.
		 *Message - Dialog message.
		 *Title - Dialog title.
		 *Positive - Positive button text. Pass an empty string to remove button.
		 *Cancel - Cancel button text. Pass an empty string to remove button.
		 *Negative - Negative button text. Pass an empty string to remove button.
		 *Icon - Dialog icon. Pass Null to remove. Does nothing in B4i.
		 * Example:<code>
		 *Dim sf As Object = xui.Msgbox2Async("Delete file?", "Title", "Yes", "Cancel", "No", Null)
		 *Wait For (sf) Msgbox_Result (Result As Int)
		 *If Result = xui.DialogResponse_Positive Then
		 *	Log("Deleted!!!")
		 *End If</code>
		 */
		public static Object Msgbox2Async(BA ba, String Message, String Title, String Positive, String Cancel, String Negative, Image Icon) throws Exception {
			return Msgbox2AsyncImpl(ba, Message, Title, Positive, Cancel, Negative, Icon, AlertType.NONE);
			
		}
		@Hide
		public static Object Msgbox2AsyncImpl(BA ba, String Message, String Title, String Positive, String Cancel, String Negative, Image Icon, AlertType at) throws Exception {
			Stage owner = findActiveStage();
			Alert alrt = new Alert(at);
			setOwnerAndIcon(alrt, owner);
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
			if (Icon != null) {
				ImageView iv = new ImageView();
				iv.setImage(Icon);
				alrt.setGraphic(iv);
			}
			alrt.resultProperty().addListener(new ChangeListener<ButtonType>() {

				@Override
				public void changed(
						ObservableValue<? extends ButtonType> observable,
						ButtonType oldValue, ButtonType newValue) {
					ba.raiseEventFromUI(alrt, "msgbox_result",res.get(newValue.getButtonData()) );
				}
			});
			alrt.show();
			
			return alrt;
		}
		@SuppressWarnings("rawtypes")
		@Hide
		public static Stage findActiveStage() throws Exception{
			Stage owner = null;
			Iterable windows;
			if (System.getProperty("java.version").startsWith("1.8"))
				windows = (Iterable)Class.forName("com.sun.javafx.stage.StageHelper").getMethod("getStages").invoke(null);
			else {
				windows = (Iterable)Class.forName("javafx.stage.Window").getMethod("getWindows").invoke(null);
			}
			for (Object win : windows) {
				if (win instanceof Stage == false) {
					continue;
				}
				Stage stg = (Stage)win;
				if (stg.isIconified())
					continue;
				if (stg.isShowing())
					owner = stg;
				if (stg.isFocused()) {
					owner = stg;
					break;
				}
			}
			return owner;
		}
		private static void setOwnerAndIcon(Dialog<?> d, Stage owner) {
			if (owner != null) {
				d.initOwner(owner);
				if (owner.getIcons().size() > 0)
					((Stage)d.getDialogPane().getScene().getWindow()).getIcons().add(owner.getIcons().get(0));
			}
		}
		/**
		 * Returns a file uri. This can be used with WebView to access local resources.
		 *The FileName parameter will be url encoded.
		 *Example: <code>
		 *WebView1.LoadHtml($"<img src="${xui.FileUri(File.DirAssets, "smiley.png")}" />"$)
		 *'or:
		 *WebView1.LoadUrl($"${xui.FileUri(File.DirAssets, "smiley.png")}"$)
		 *</code>
		 */
		public static String FileUri(String Dir, String FileName) {
			return File.GetUri(Dir, FileName);
		}
		
	}

}
