
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

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.B4aDebuggable;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.streams.File;
import anywheresoftware.b4j.objects.ImageViewWrapper.ImageWrapper;
import anywheresoftware.b4j.objects.JFX.Colors;
import anywheresoftware.b4j.objects.MenuItemWrapper.ContextMenuWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper.ConcreteControlWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ResizeEventManager;

@Hide
@Events(values={"MouseClicked (EventData As MouseEvent)",
		"MouseMoved (EventData As MouseEvent)",
		"MouseDragged (EventData As MouseEvent)",
		"MousePressed (EventData As MouseEvent)",
		"MouseReleased (EventData As MouseEvent)",
		"MouseEntered (EventData As MouseEvent)",
		"MouseExited (EventData As MouseEvent)",
		"FocusChanged (HasFocus As Boolean)",
		"AnimationCompleted"})
public class NodeWrapper<T extends Node> extends AbsObjectWrapper<T> implements B4aDebuggable{
	static final int LEFT = 0, RIGHT = 1, BOTH = 2, TOP = 0, BOTTOM = 1;
	protected BA ba;
	/**
	 * Initializes the object and sets the subs that will handle the events.
	 *Nodes added with the designer should NOT be initialized. These views are initialized when the layout is loaded.
	 */
	public void Initialize(final BA ba, String EventName) {
		innerInitialize(ba, EventName.toLowerCase(BA.cul), false);

	}
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		this.ba = ba;
		if (ba == null)
			throw new RuntimeException("Parent class was not initialized.");
		final Object sender = getObject();
		if (ba.subExists(eventName + "_mouseclicked")) {
			getObject().setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(sender, eventName + "_mouseclicked", 
							AbsObjectWrapper.ConvertToWrapper(new MouseEventWrapper(), arg0));
				}

			});
		}
		if (ba.subExists(eventName + "_mousepressed")) {
			getObject().setOnMousePressed(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(sender, eventName + "_mousepressed", 
							AbsObjectWrapper.ConvertToWrapper(new MouseEventWrapper(), arg0));
				}

			});
		}
		if (ba.subExists(eventName + "_mousereleased")) {
			getObject().setOnMouseReleased(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(sender, eventName + "_mousereleased", 
							AbsObjectWrapper.ConvertToWrapper(new MouseEventWrapper(), arg0));
				}

			});
		}
		if (ba.subExists(eventName + "_mousemoved")) {
			getObject().setOnMouseMoved(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(sender, eventName + "_mousemoved", 
							AbsObjectWrapper.ConvertToWrapper(new MouseEventWrapper(), arg0));
				}

			});
		}
		if (ba.subExists(eventName + "_mousedragged")) {
			getObject().setOnMouseDragged(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(sender, eventName + "_mousedragged", 
							AbsObjectWrapper.ConvertToWrapper(new MouseEventWrapper(), arg0));
				}

			});
		}
		if (ba.subExists(eventName + "_mouseentered")) {
			getObject().setOnMouseEntered(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(sender, eventName + "_mouseentered", 
							AbsObjectWrapper.ConvertToWrapper(new MouseEventWrapper(), arg0));
				}

			});
		}
		if (ba.subExists(eventName + "_mouseexited")) {
			getObject().setOnMouseExited(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(sender, eventName + "_mouseexited", 
							AbsObjectWrapper.ConvertToWrapper(new MouseEventWrapper(), arg0));
				}

			});
		}
		if (ba.subExists(eventName + "_focuschanged")) {
			getObject().focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> arg0,
						Boolean arg1, Boolean arg2) {
					ba.raiseEvent(sender, eventName + "_focuschanged", 
							arg2.booleanValue());
				}
			});
		}
		
		if (ba.subExists(eventName + "_animationcompleted")) {
			AbsObjectWrapper.getExtraTags(getObject()).put("animationcompleted", new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					ba.raiseEventFromUI(sender, eventName + "_animationcompleted");
				}
				
			});
		}
	}
	@Hide
	@Override
	public Object[] debug(int limit, boolean[] outShouldAddReflectionFields) {
		Object[] res = new Object[1 * 2];
		res[0] = "ToString";
		res[1] = toString();
		outShouldAddReflectionFields[0] = true;
		return res;
	}
	/**
	 * Gets or sets the mouse cursor that will be used when the mouse is in the node's bounds.
	 *Example:<code>
	 *MainForm.RootPane.MouseCursor = fx.Cursors.HAND</code>
	 */
	public Cursor getMouseCursor() {
		return getObject().getCursor();
	}
	public void setMouseCursor(Cursor c) {
		getObject().setCursor(c);
	}
	/**
	 * Gets or sets whether the node is enabled.
	 */
	public boolean getEnabled() {
		return !getObject().isDisabled();
	}
	public void setEnabled(boolean b) {
		getObject().setDisable(!b);
	}
	/**
	 * Gets or sets the node alpha level: 0 - transparent, 1 (default) fully opaque.
	 */
	public double getAlpha() {
		return getObject().getOpacity();
	}
	public void setAlpha(double d) {
		getObject().setOpacity(d);
	}
	/**
	 * Animates the nodes alpha level.
	 *Duration - Animation duration in milliseconds.
	 *Alpha - Value between 0 to 1 (transparent to opaque).
	 */
	public void SetAlphaAnimated(int Duration, double Alpha) {
		if (Duration == 0) {
			setAlpha(Alpha);
			raiseAnimationCompletedEvent(null, "SetAlphaAnimated");
			return;
		}
		KeyValue a = new KeyValue(getObject().opacityProperty(), Alpha);
		KeyFrame frame = new KeyFrame(javafx.util.Duration.millis(Duration), a);
		Timeline timeline = new Timeline(frame);
		raiseAnimationCompletedEvent(timeline, "SetAlphaAnimated"); //check XUI.SetVisibleAnimated if changing
		timeline.play();
	}
	/**
	 * Gets or sets whether the node is visible.
	 */
	public boolean getVisible() {
		return getObject().isVisible();
	}
	public void setVisible(boolean b) {
		getObject().setVisible(b);
	}
	/**
	 * Gets or sets whether mouse events are intercepted in transparent parts of the node.
	 *Default value is False.
	 */
	public boolean getPickOnBounds() {
		return getObject().isPickOnBounds();
	}
	public void setPickOnBounds(boolean b) {
		getObject().setPickOnBounds(b);
	}
	/**
	 * Returns a List with the node's style classes.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getStyleClasses() {
		ObservableList ol = getObject().getStyleClass();
		List l1 = new List();
		l1.setObject(ol);
		return l1;
	}
	/**
	 * Gets or sets the node inline style.
	 */
	public String getStyle() {
		return getObject().getStyle();
	}
	/**
	 * Returns the node's parent. The object returned will be uninitialized if there is no parent.
	 */
	public ConcreteNodeWrapper getParent() {
		return (ConcreteNodeWrapper)AbsObjectWrapper.ConvertToWrapper(new ConcreteNodeWrapper(), getObject().getParent());
	}

	public void setStyle(String s) {
		getObject().setStyle(s);
	}
	/**
	 * Gets or sets the node's tag. This is a placeholder for any object you need to tie to the node.
	 */
	public Object getTag() {
		Object o =  getObject().getUserData();
		return o == null ? "" : o;
	}
	public void setTag(Object o) {
		getObject().setUserData(o);
	}
	/**
	 * Gets or sets the node id. Returns an empty string if the id was not set.
	 */
	public String getId() {
		return getObject().getId() == null ? "" : getObject().getId();
	}
	public void setId(String s) {
		getObject().setId(s);
	}
	/**
	 * Gets or sets the Left property of the node (related to its parent).
	 */
	public double getLeft() {
		return getObject().getLayoutX() + getObject().getLayoutBounds().getMinX();
	}
	/**
	 * Gets or sets the Top property of the node (related to its parent).
	 */
	public double getTop() {
		return getObject().getLayoutY() + getObject().getLayoutBounds().getMinY();
	}
	public void setLeft(double d) {
		getObject().setLayoutX(d - getObject().getLayoutBounds().getMinX());
	}
	public void setTop(double d) {
		getObject().setLayoutY(d - getObject().getLayoutBounds().getMinY());
	}

	/**
	 * Requests the focus to be set on this node.
	 */
	public void RequestFocus() {
		getObject().requestFocus();
	}
	/**
	 * Removes the node from its parent.
	 */
	public void RemoveNodeFromParent() {
		Parent p = getObject().getParent();
		if (p instanceof Pane)
			((Pane)p).getChildren().remove(getObject());
	}
	/**
	 * Captures the node appearance and returns the rendered image.
	 */
	public ImageWrapper Snapshot() {
		return Snapshot2(Colors.White);
	}
	/**
	 * Similar to Snapshot. Allows you to set the background color.
	 */
	public ImageWrapper Snapshot2(Paint BackgroundColor) {
		ImageWrapper iw = new ImageWrapper();
		SnapshotParameters sp = new SnapshotParameters();
		sp.setFill(BackgroundColor);
		iw.setObject(getObject().snapshot(sp, null));
		return iw;
	}

	/**
	 * Gets or sets the node's preferred width.
	 */
	public double getPrefWidth() {
		Node n = getObject();
		if (n instanceof Control)
			return ((Control)n).getPrefWidth();
		else if (n instanceof Region){
			return ((Region)n).getPrefWidth();
		} else if (n instanceof ImageView) {
			return ((ImageView)n).getFitWidth();
		}
		else if (n instanceof Canvas) {
			return ((Canvas)n).getWidth();
		} else if ((n.getClass().getName().equals("javafx.scene.web.WebView"))) {
			try {
				return (double) n.getClass().getMethod("getWidth").invoke(n);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		else
			return -1;
	}
	/**
	 * Gets or sets the node's preferred height.
	 */
	public double getPrefHeight() {
		Node n = getObject();
		if (n instanceof Control) 
			return ((Control)n).getPrefHeight();
		else if (n instanceof Region){
			return ((Region)n).getPrefHeight();
		} else if (n instanceof ImageView) {
			return ((ImageView)n).getFitHeight();
		}
		else if (n instanceof Canvas) {
			return ((Canvas)n).getHeight();
		} else if ((n.getClass().getName().equals("javafx.scene.web.WebView"))) {
			try {
				return (double) n.getClass().getMethod("getHeight").invoke(n);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		else
			return -1;
	}
	public void setPrefWidth(double d) {
		SetSize(getObject(), d, null);
	}
	public void setPrefHeight(double d) {
		SetSize(getObject(), null, d);
	}
	@Hide
	public void raiseAnimationCompletedEvent(Animation anim, String animType) {
		cancelAnimation(animType);
		if (anim != null) {
			AbsObjectWrapper.getExtraTags(getObject()).put("animation_" + animType, new WeakReference<Animation>(anim));
		}
		@SuppressWarnings("unchecked")
		EventHandler<ActionEvent> e = (EventHandler<ActionEvent>) AbsObjectWrapper.extraTagsGetValueIfAvailable(getObject(), "animationcompleted");
		if (e != null) {
			if (anim == null)
				e.handle(null); //duration = 0
			else
				anim.setOnFinished(e);
		}
	}
	@Hide
	public void cancelAnimation(String animType) {
		@SuppressWarnings("unchecked")
		WeakReference<Animation> prevAnim = (WeakReference<Animation>) AbsObjectWrapper.extraTagsGetValueIfAvailable(getObject(), "animation_" + animType);
		if (prevAnim != null) {
			Animation prev = prevAnim.get();
			if (prev != null)
			{
				//System.out.println("cancelling prev anim: " + animType + ", " + prev.getStatus());
				if (prev.getStatus() == Animation.Status.RUNNING)
					prev.stop();
			}
		}
	}
	

	/**
	 * Sets the width and height of the node.
	 */
	public void SetSize(double Width, double Height) {
		SetSize(this.getObject(), Width, Height);
	}
	@Hide
	public static void SetLayout(Node n, double[] layout) {
		n.setLayoutX(layout[0] - n.getLayoutBounds().getMinX());
		n.setLayoutY(layout[1] - n.getLayoutBounds().getMinY());
		SetSize(n, layout[2], layout[3]);
	}
	@Hide
	public static void SetSize(Node n, Double Width, Double Height) {
		Node Node = n;
		if (Node instanceof Control) {
			if (Width != null)
				((Control)Node).setPrefWidth(Width > 0 ? Width : Control.USE_COMPUTED_SIZE);
			if (Height != null)
				((Control)Node).setPrefHeight(Height > 0 ? Height : Control.USE_COMPUTED_SIZE);

		}
		else if (Node instanceof Region){
			if (Width != null) {
				((Region)Node).setPrefWidth(Width > 0 ? Width : Control.USE_COMPUTED_SIZE);
			}
			if (Height != null)
				((Region)Node).setPrefHeight(Height > 0 ? Height : Control.USE_COMPUTED_SIZE);
		} else if (Node instanceof ImageView) {
			if (Width != null)
				((ImageView)Node).setFitWidth(Width);
			if (Height != null)
				((ImageView)Node).setFitHeight(Height);
		}
		else if (Node instanceof Canvas) {
			if (Width != null)
				((Canvas)Node).setWidth(Width);
			if (Height != null)
				((Canvas)Node).setHeight(Height);
		} else if (Node.getClass().getName().equals("javafx.scene.web.WebView")) {
			try {
			if (Width != null)
				Node.getClass().getMethod("setPrefWidth", double.class).invoke(Node, Width);
			if (Height != null)
				Node.getClass().getMethod("setPrefHeight", double.class).invoke(Node, Height);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}


	@Hide
	public static Node build(Object prev, Map<String, Object> props, boolean designer) throws Exception {
		Node v = (Node) prev;
		ConcreteNodeWrapper cnw = new ConcreteNodeWrapper();
		cnw.setObject(v);
		boolean isMain = "Main".equals(props.get("name"));
		if (!isMain) {
			buildResize(v, props, 0f);
		}
		cnw.setEnabled((Boolean)props.get("enabled"));
		if (!designer) {
			cnw.setVisible((Boolean)props.get("visible"));
			v.setUserData(props.get("tag"));
		}
		StringBuilder sb = new StringBuilder();
		buildFont(props, v, (Map<String, Object>)props.get("font"), sb, designer, false);
		buildDrawable(v, (Map<String, Object>)props.get("drawable"), sb, designer);
		v.setOpacity((Float)props.get("alpha"));
		appendColorProp(sb, "-fx-border-color", props.get("borderColor"));
		Number radius = (Number) props.get("cornerRadius");
		if (radius != null && radius.floatValue() != 0) {
			appendProp(sb, "-fx-border-radius", radius);
			appendProp(sb, "-fx-background-radius", radius);
		}
		appendProp(sb, "-fx-border-width", props.get("borderWidth"));
		sb.append((String)props.get("extraCss"));
		v.setStyle(sb.toString());
		buildShadow(v, (Map<String, Object>) props.get("shadow"));
		return v;
	}
	private static void buildShadow(Node prev, Map<String, Object>props) throws Exception{
		if (props == null)
			return;
		int shadow = BA.gm(props, "stype", 0);
		if (shadow == 0) {
			prev.setEffect(null);
			return;
		}
		Effect d = shadow == 1 ? new DropShadow() : new InnerShadow();
		d.getClass().getDeclaredMethod("setColor", Color.class).invoke(d, ColorFromBytes(props.get("shadowColor")));
		d.getClass().getDeclaredMethod("setOffsetX", double.class).invoke(d, props.get("offsetX"));
		d.getClass().getDeclaredMethod("setOffsetY", double.class).invoke(d, props.get("offsetY"));
		d.getClass().getDeclaredMethod("setRadius", double.class).invoke(d, props.get("radius"));
		prev.setEffect(d);

	}
	@Hide
	public static void buildResize(Node prev, Map<String, Object> props, float autoscale) {
		int Width = (Integer)props.get("width");
		int Height = (Integer)props.get("height");
		int Left = (Integer)props.get("left");
		int Top = (Integer)props.get("top");
		int pw = (Integer)props.get("pw");
		int ph = (Integer)props.get("ph");
		int duration = (Integer)props.get("duration");
		int hanchor = (Integer)props.get("hanchor");
		int vanchor = (Integer)props.get("vanchor");
		if (hanchor == RIGHT) {
			int right = Left;
			Left = pw - right - Width;
		} else if (hanchor == BOTH) {
			int right = Width;
			Width = pw-right - Left;
		}
		if (vanchor == BOTTOM) {
			int bottom = Top;
			Top = ph - bottom - Height;
		} else if (vanchor == BOTH) {
			int bottom = Height;
			Height = ph - bottom - Top;
		}
		if (prev instanceof Control) {
			ConcreteControlWrapper ccw = new ConcreteControlWrapper();
			ccw.setObject((Control) prev);
			ccw.SetLayoutAnimatedImpl(duration, Left, Top, Width, Height, false);
		} else if (prev instanceof Pane) {
			ConcretePaneWrapper cpw = new ConcretePaneWrapper();
			cpw.setObject((Pane) prev);
			cpw.SetLayoutAnimatedImpl(duration, Left, Top, Width, Height, false);
		}
		else {
			SetSize(prev, (double)Width, (double)Height);
			ConcreteNodeWrapper cnw = new ConcreteNodeWrapper();
			cnw.setObject(prev);
			cnw.setLeft(Left);
			cnw.setTop(Top);
		}
	}
	@Hide
	public static void appendProp(StringBuilder sb, String Key, Object value) {
		if (value == null)
			return;
		if (value instanceof Float)
			value = String.format(BA.cul, "%.2f", value);
		sb.append(Key).append(":").append(String.valueOf(value)).append(";");
	}
	@Hide
	public static void appendColorProp(StringBuilder sb, String Key, Object value) {
		if (isDefaultColor(value))
			return;
		sb.append(Key).append(":");
		appendHexString(sb, value);
		sb.append(";\n");
	}
	@Hide
	public static boolean isDefaultColor(Object value) {
		if (value == null)
			return true;
		byte[] b = (byte[])value;
		return (b[0] == -1 && b[1] == -16 && b[2] == -8 && b[3] == -1);
	}

	@Hide
	public static Color ColorFromBytes(Object value) {
		if (isDefaultColor(value))
			return null;
		byte[] bb = (byte[])value;
		return Colors.ARGB(bb[0] & 0xFF, bb[1] & 0xFF, bb[2] & 0xFF, bb[3] & 0xFF);
	}
	@Hide
	public static void appendHexString(StringBuilder sb, Object bytes) {

		byte[] bb = (byte[])bytes;
		int alpha = bb[0] & 0xFF;
		if (alpha == 255) {
			sb.append("#");
			for (int i = 1;i < bb.length;i++) {
				sb.append(String.format(BA.cul,"%02X", bb[i]));
			}
		}else {
			sb.append("rgba(");
			for (int i = 1;i < bb.length;i++) {
				sb.append((bb[i] & 0xFF)).append(",");
			}
			sb.append(String.format(BA.cul,"%.2f",alpha / 255.0f));
			sb.append(")");
		}
	}
	@Hide
	public static StringBuilder getStyleAsStringBuilder(Node n) {
		String s = n.getStyle();
		StringBuilder sb = new StringBuilder(s == null ? "" : s);
		return sb;
	}
	
	@Hide
	protected static void buildFont(Map<String, Object> origProps, Node n, Map<String, Object> fontProps, StringBuilder sb, boolean designer,
			boolean customView) {
		if (fontProps == null)
			return;

		String font = (String)fontProps.get("fontName");
		boolean fontAwesome = font.equals("FontAwesome");
		boolean materialIcons = font.equals("Material Icons");
		if (font.equals("DEFAULT"))
			font = null;
		else if (origProps.containsKey("fontAwesome")) {
			if (fontAwesome)
				origProps.put("text", origProps.get("fontAwesome"));
			else if (materialIcons)
				origProps.put("text", origProps.get("materialIcons"));
		}
		try {
			if (fontAwesome)
				JFX.loadFontAwesome();
			if (materialIcons)
				JFX.loadMaterialIcons();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		float fontSize = (Float)fontProps.get("fontSize");
		boolean italic = (Boolean)fontProps.get("italic"), bold = (Boolean)fontProps.get("bold");
		boolean isLabeled = n instanceof Labeled;
		if (isLabeled) {
			Labeled lbl = (Labeled)n;
			lbl.setFont(Font.font(font, bold ? FontWeight.BOLD : FontWeight.NORMAL, 
					italic ? FontPosture.ITALIC : FontPosture.REGULAR, fontSize));
		}
		if (!isLabeled || customView) {
			if (font != null) {
				if (font.contains(" "))
					font = "'" + font + "'";
				appendProp(sb, "-fx-font-family", font);
			}
			appendProp(sb, "-fx-font-size", fontSize);
			if (italic)
				appendProp(sb, "-fx-font-style", "italic");
			if (bold)
				appendProp(sb, "-fx-font-weight", "bold");
		}


	}
	private static HashMap<String, String> gradientOrientation;
	private static HashMap<String, String> bitmapDrawableOrientations;
	private static void buildDrawable(Node n, Map<String, Object> props, StringBuilder sb, boolean designer) {
		if (props == null)
			return;
		String type = (String)props.get("type");
		if (type.equals("ColorDrawable")) {
			String key = (String)props.get("colorKey");
			appendColorProp(sb, key, props.get("color"));
		} else if (type.equals("BitmapDrawable")) {

			String uri = getImageUri((String)props.get("file"), designer);
			if (uri == null)
				return;
			if (n instanceof ImageView) {
				((ImageView)n).setImage(new Image(uri));
			}
			else {
				String value = "url(" + escapeUriForCSS(uri) + ")";
				String gravity = (String)props.get("gravity");
				String size = null;
				String position = "center";
				String repeat = "no-repeat";
				if (gravity.equals("Fill")) {
					size = "stretch";
				} else {
					size = "auto";
					if (gravity.equals("Tile")) {
						repeat = "repeat";
						position = "top left";
					} else {
						position = gravity.toLowerCase(BA.cul).replace("-", " ");
					}

				}

				appendProp(sb, "-fx-background-size", size);
				appendProp(sb, "-fx-background-position", position);
				appendProp(sb, "-fx-background-repeat", repeat);
				appendProp(sb, "-fx-background-image", value);
			}
		} else if (type.equals("GradientDrawable")) {
			String orientation = (String)props.get("orientation");
			if (gradientOrientation == null) {
				gradientOrientation = new HashMap<String, String>();
				gradientOrientation.put("TOP_BOTTOM", "from 50% 0% to 50% 100%");
				gradientOrientation.put("TR_BL", "from 100% 0% to 0% 100%");
				gradientOrientation.put("RIGHT_LEFT", "from 100% 50% to 0% 50%");
				gradientOrientation.put("BR_TL", "from 100% 100% to 0% 0%");
				gradientOrientation.put("BOTTOM_TOP", "from 50% 100% to 50% 0%");
				gradientOrientation.put("BL_TR", "from 0% 100% to 100% 0%");
				gradientOrientation.put("LEFT_RIGHT", "from 0% 50% to 100% 50%");
				gradientOrientation.put("TL_BR", "from 0% 0% to 100% 100%");
			}
			StringBuilder lg = new StringBuilder();
			lg.append("linear-gradient(").append(gradientOrientation.get(orientation));
			lg.append(", ");
			appendHexString(lg, props.get("firstColor"));
			lg.append(" 0%, ");
			appendHexString(lg, props.get("secondColor"));
			lg.append(" 100%)");
			appendProp(sb, "-fx-background-color", lg.toString());
		}
	}
	@Hide
	public static String escapeUriForCSS(String uri) {
		return uri.replace("(", "\\(").replace(")", "\\)");
	}
	@Hide
	public static String getImageUri(String file, boolean designer) {
		if (file == null || file.length() == 0)
			return null;
		String uri;
		if (designer) {
			String assetsFolder = System.getProperty("b4j_assets");
			uri = new java.io.File(File.Combine(assetsFolder, file)).toURI().toString();
		} else {
			uri = File.GetUri(File.getDirAssets(), file);
		}
		return uri;
	}
	@Hide
	@SuppressWarnings("unchecked")
	public static <T> T buildNativeView(Class<T> cls, HashMap<String, Object> props, boolean designer) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		String overideClass = (String) props.get("nativeClass");
		if (overideClass != null && overideClass.startsWith(".")) {
			overideClass = BA.packageName + overideClass;

		}
		Class<?> c;
		try {
			c = designer || overideClass == null || overideClass.length() == 0 ? cls : Class.forName(overideClass);
		} catch (ClassNotFoundException e) {
			int i = overideClass.lastIndexOf(".");
			c = Class.forName(overideClass.substring(0, i) + "$" + overideClass.substring(i + 1));
		}
		return (T) c.getConstructor().newInstance();
	}
	@SuppressWarnings("unchecked")
	@Hide
	public static Object getDefault(Node v, String key, Object defaultValue) {
		HashMap<String, Object> map = (HashMap<String, Object>) v.getUserData();
		if (map.containsKey(key))
			return map.get(key);
		else {
			map.put(key, defaultValue);
			return defaultValue;
		}
	}

	/**
	 * A special type that can hold any type of node.
	 */
	@ShortName("Node")
	public static class ConcreteNodeWrapper extends NodeWrapper<Node> {

	}

	@Hide
	public static class ParentWrapper<T extends Parent> extends NodeWrapper<T> {

	}





	@Hide
	@Events(values={"Click"})
	public static class ButtonBaseWrapper<T extends ButtonBase> extends LabeledWrapper<T> {
		@Hide
		@Override
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			super.innerInitialize(ba, eventName, keepOldObject);
			final Object sender = getObject();
			if (ba.subExists(eventName + "_action")) {
				getObject().setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {
						ba.raiseEventFromUI(sender, eventName + "_action");
						arg0.consume();
					}
				});
			}
			if (ba.subExists(eventName + "_click")) {
				getObject().setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {
						ba.raiseEventFromUI(sender, eventName + "_click");
						arg0.consume();
					}
				});
			}
			getObject().setMnemonicParsing(false);

		}
	}
	/**
	 * Holds the information related to the current mouse event.
	 */
	@ShortName("MouseEvent")
	public static class MouseEventWrapper extends EventWrapper<MouseEvent> {
		/**
		 * Returns the number of clicks associated with this event.
		 */
		public int getClickCount() {
			return getObject().getClickCount();
		}
		/**
		 * Returns the X coordinate related to the node bounds.
		 */
		public double getX() {
			return getObject().getX();
		}
		/**
		 * Returns the Y coordinate related to the node bounds.
		 */
		public double getY() {
			return getObject().getY();
		}
		/**
		 * Returns true if the primary button is currently down.
		 */
		public boolean getPrimaryButtonDown() {
			return getObject().isPrimaryButtonDown();
		}
		/**
		 * Returns true if the middle button is currently down.
		 */
		public boolean getMiddleButtonDown() {
			return getObject().isMiddleButtonDown();
		}
		/**
		 * Returns true if the secondary button is currently down.
		 */
		public boolean getSecondaryButtonDown() {
			return getObject().isSecondaryButtonDown();
		}
		/**
		 * Returns true if the primary button was responsible for raising the current click event.
		 */
		public boolean getPrimaryButtonPressed() {
			return getObject().getButton() == MouseButton.PRIMARY;
		}
		/**
		 * Returns true if the secondary button was responsible for raising the current click event.
		 */
		public boolean getSecondaryButtonPressed() {
			return getObject().getButton() == MouseButton.SECONDARY;
		}
		/**
		 * Returns true if the primary button was responsible for raising the current click event.
		 */
		public boolean getMiddleButtonPressed() {
			return getObject().getButton() == MouseButton.MIDDLE;
		}
	}
	@Hide
	public static class EventWrapper<T extends Event> extends AbsObjectWrapper<T> {
		/**
		 * Consumes the current event and prevent it from being handled by the nodes parent.
		 */
		public void Consume() {
			getObject().consume();
		}
	}
	@ShortName("Event")
	public static class ConcreteEventWrapper extends EventWrapper<Event> {

	}

	@Hide
	@Events(values={"Resize (Width As Double, Height As Double)"})
	public static class ControlWrapper<T extends Control> extends ParentWrapper<T> {
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			super.innerInitialize(ba, eventName, keepOldObject);
			final Control control = getObject();
			if (ba.subExists(eventName + "_resize")) {
				new ResizeEventManager(null, control, new Runnable() {

					@Override
					public void run() {
						ba.raiseEvent(getObject(), eventName + "_resize", control.getWidth(), control.getHeight());
					}
				});

			}
		}
		/**
		 * Gets or sets the control preferred height.
		 */
		public double getPrefHeight() {
			return getObject().getPrefHeight();
		}
		public void setPrefHeight(double d) {
			getObject().setPrefHeight(d);
		}
		/**
		 * Gets or sets the control preferred width.
		 */
		public double getPrefWidth() {
			return getObject().getPrefWidth();
		}
		public void setPrefWidth(double d) {
			getObject().setPrefWidth(d);
		}
		/**
		 * Gets the node height. This value will only be available after the node's parent has finished measuring and drawing the node.
		 *Consider using PrefHeight instead.
		 */
		public double getHeight() {
			return getObject().getHeight();
		}
		/**
		 * Gets the node width. This value will only be available after the node's parent has finished measuring and drawing the node.
		 *Consider using PrefWidth instead.
		 */
		public double getWidth() {
			return getObject().getWidth();
		}
		/**
		 * Gets or sets the tooltip text that will appear when the mouse hovers over the control.
		 */
		public void setTooltipText(String s) {
			getObject().setTooltip(new Tooltip(s));
		}
		public String getTooltipText() {
			Tooltip tt = getObject().getTooltip();
			return tt == null ? "" : tt.getText();
		}
		public void setContextMenu(ContextMenuWrapper c) {
			getObject().setContextMenu(c.getObject());
		}
		
		/**
		 * Gets or sets the context menu that will appear when the user right clicks on the control.
		 */
		public ContextMenuWrapper getContextMenu() {
			return (ContextMenuWrapper)AbsObjectWrapper.ConvertToWrapper(new ContextMenuWrapper(), getObject().getContextMenu());
		}
		@Hide
		public void SetLayoutAnimatedImpl (int Duration, double Left, double Top, double PrefWidth, double PrefHeight, boolean raiseAnimationCompleted) {
			if (Duration == 0) {
				setTop(Top);
				setLeft(Left);
				setPrefWidth(PrefWidth);
				setPrefHeight(PrefHeight);
				if (raiseAnimationCompleted)
					raiseAnimationCompletedEvent(null, "SetLayoutAnimated");
				return;
			}
			KeyValue left = new KeyValue(getObject().layoutXProperty(), Left - getObject().getLayoutBounds().getMinX());
			KeyValue top = new KeyValue(getObject().layoutYProperty(), Top - getObject().getLayoutBounds().getMinY());
			KeyValue width = new KeyValue(getObject().prefWidthProperty(), PrefWidth);
			KeyValue height = new KeyValue(getObject().prefHeightProperty(), PrefHeight);
			KeyFrame frame;
			if (getObject() instanceof ProgressBar) {
				frame = new KeyFrame(javafx.util.Duration.millis(Duration), left, top);
				setPrefWidth(PrefWidth);
				setPrefHeight(PrefHeight);
			}
			else {
				frame = new KeyFrame(javafx.util.Duration.millis(Duration), left, top, width, height);
			}
			Timeline timeline = new Timeline(frame);
			if (raiseAnimationCompleted)
				raiseAnimationCompletedEvent(timeline, "SetLayoutAnimated");
			timeline.play();
		}
		/**
		 * Changes the node Top, Left, PrefWidth and PrefHeight properties with an animation effect.
		 * Duration - Animation duration in milliseconds.
		 */
		public void SetLayoutAnimated(int Duration, double Left, double Top, double PrefWidth, double PrefHeight) {
			SetLayoutAnimatedImpl (Duration, Left, Top, PrefWidth, PrefHeight, true);
		}

		@Hide
		public static Node build(Object prev, HashMap<String, Object> props, boolean designer) throws Exception{
			Control vg = (Control) prev;
			String tt = (String) props.get("toolTip");
			if (tt.length() > 0)
				vg.setTooltip(new Tooltip(tt));
			ArrayList<Menu> l1 = MenuItemWrapper.MenuBarWrapper.parseMenusJson((BA)props.get("ba"),
					(String)props.get("contextMenu"), (String)props.get("eventName"));
			if (l1.size() > 0) {
				ContextMenu cm = new ContextMenu();
				cm.getItems().addAll(l1);
				vg.setContextMenu(cm);
			} else {
				vg.setContextMenu(null);
			}
			NodeWrapper.build(vg, props, designer);
			return vg;
		}

		/**
		 * A special type that can hold any type of control object.
		 */
		@ShortName("Control")
		public static class ConcreteControlWrapper extends ControlWrapper<Control> {

		}
	}



}
