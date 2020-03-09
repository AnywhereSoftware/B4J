
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.streams.File;
import anywheresoftware.b4j.objects.ButtonWrapper.RadioButtonWrapper;
import anywheresoftware.b4j.objects.ButtonWrapper.ToggleButtonWrapper;
import anywheresoftware.b4j.objects.MenuItemWrapper.ContextMenuWrapper;
import anywheresoftware.b4j.objects.MenuItemWrapper.MenuBarWrapper;
import anywheresoftware.b4j.objects.MenuItemWrapper.MenuWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ButtonBaseWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ConcreteNodeWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ParentWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.AnchorPaneWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.NativeAndWrapper;
import anywheresoftware.b4j.objects.ProgressIndicatorWrapper.ProgressBarWrapper;
import anywheresoftware.b4j.objects.TabPaneWrapper.TabWrapper;
import anywheresoftware.b4j.objects.TextInputControlWrapper.TextAreaWrapper;
import anywheresoftware.b4j.objects.TextInputControlWrapper.TextFieldWrapper;

@Hide
public class FXMLBuilder {
	
	static {
		//order is important
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(TreeView.class, TreeViewWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(TableView.class, TableViewWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(Slider.class, SliderWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(ListView.class, ListViewWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(ComboBox.class, ComboBoxWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(Button.class, ButtonWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(CheckBox.class, CheckboxWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(RadioButton.class, RadioButtonWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(ToggleButton.class, ToggleButtonWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(ButtonBase.class, ButtonBaseWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(Label.class, LabelWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(Labeled.class, LabeledWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(TextField.class, TextFieldWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(TextArea.class, TextAreaWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(TextInputControl.class, TextInputControlWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(ListView.class, ListViewWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(ImageView.class, ImageViewWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(ProgressBar.class, ProgressBarWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(ProgressIndicator.class, ProgressIndicatorWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(MenuBar.class, MenuBarWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(TabPane.class, TabPaneWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(ScrollPane.class, ScrollPaneWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(Control.class, ControlWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(AnchorPane.class, AnchorPaneWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(Pane.class, ConcretePaneWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(Parent.class, ParentWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(Canvas.class, CanvasWrapper.class));
		PaneWrapper.nativeToWrapper.add(new NativeAndWrapper(Node.class, ConcreteNodeWrapper.class));

	}
	@SuppressWarnings({ "rawtypes" })
	@RaisesSynchronousEvents
	public static void LoadLayout(Pane pane, BA tba, String LayoutFile) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(FXMLBuilder.class.getResource("/Files/" + LayoutFile));
		try (InputStream in = File.OpenInput(File.getDirAssets(), LayoutFile).getObject()) {
			Parent p = (Parent)loader.load(in);
			Map<String, Object> viewsToSend = null;
			viewsToSend = new HashMap<String, Object>();
			setEventsAndVariables(tba, Arrays.asList(p), viewsToSend);
			if (BA.isShellModeRuntimeCheck(tba)) {
				tba.raiseEvent2(null, true, "SEND_VIEWS_AFTER_LAYOUT", true, viewsToSend);
			}
			Class<?> cls = Class.forName(tba.className);
			for (Entry<String, Object> e : viewsToSend.entrySet()) {
				setNodeField(cls, e.getKey(),(ObjectWrapper) e.getValue(), tba);
			}
			AnchorPane.setBottomAnchor(p, 0d);
			AnchorPane.setLeftAnchor(p, 0d);
			AnchorPane.setRightAnchor(p, 0d);
			AnchorPane.setTopAnchor(p, 0d);
			pane.getChildren().add(p);
		}
	}
	@SuppressWarnings("unchecked")
	public static void setNodeField(Class<?> cls, String name, @SuppressWarnings("rawtypes") ObjectWrapper wrapper, BA tba) throws IllegalAccessException {
		try {
			Field field = cls.getField("_" + name);
			if (field != null) { //object was declared in Sub Globals
				try {
					@SuppressWarnings("rawtypes")
					ObjectWrapper ow = (ObjectWrapper<?>) field.get(tba.eventsTarget);
					if (ow != null) //it will be null in debug when the sub is "dirty".
						ow.setObject(wrapper.getObject());
				} catch (IllegalArgumentException ee) {
					throw new RuntimeException("Field " + name  + " was declared with the wrong type.");
				}
			}
		} catch (NoSuchFieldException eee) {
			//don't do anything
		}
	}
	private static void setEventsAndVariables(BA tba, Iterable<? extends Node> nodes , Map<String, Object> viewsToSend) throws InstantiationException, IllegalAccessException {
		for (Node n : nodes) {
			handleNode(tba, n, viewsToSend);
			if (n instanceof Control) {
				Control c = (Control)n;
				ContextMenu cm = c.getContextMenu();
				if (cm != null) {
					if (cm.getId() != null) {
						String name = cm.getId().toLowerCase(BA.cul);
						ContextMenuWrapper cmw = new ContextMenuWrapper();
						cmw.setObject(cm);
						cmw.innerInitialize(tba, name, true);
						viewsToSend.put(name, cmw);
					}
					handleMenus(tba, cm.getItems(), viewsToSend);
				}
			}
			if (n instanceof MenuBar) {
				MenuBar mb = (MenuBar)n;
				handleMenus(tba, mb.getMenus(), viewsToSend);
			}
			else if (n instanceof MenuButton) {
				handleMenus(tba, ((MenuButton)n).getItems(), viewsToSend);
			}

			Iterable<? extends Node> kids = null;
			if (n instanceof ToolBar)
				kids = ((ToolBar)n).getItems();
			else if (n instanceof TabPane) {
				kids = new ArrayList<Node>();
				for (Tab t : ((TabPane)n).getTabs()) {
					if (t.getId() != null) {
						String name = t.getId().toLowerCase(BA.cul);
						TabWrapper tw = new TabWrapper();
						tw.setObject(t);
						viewsToSend.put(name, tw);
					}
					if (t.getContent() != null)
						((ArrayList<Node>)kids).add(t.getContent());
				}
			}
			else if (n instanceof SplitPane) {
				kids = ((SplitPane)n).getItems();
			}
			else if (n instanceof Accordion) {
				kids =  new ArrayList<Node>(((Accordion)n).getPanes());
			}
			else if (n instanceof TitledPane) {
				kids = Arrays.asList(((TitledPane)n).getContent());
			}
			else if (n instanceof javafx.scene.control.ScrollPane)
				kids = Arrays.asList(((javafx.scene.control.ScrollPane)n).getContent());
			else if (n.getClass().getName().equals("javafx.scene.web.HTMLEditor")) //raspberry pi compatibility
				kids = null;
			else if (n instanceof Parent)
				kids = ((Parent)n).getChildrenUnmodifiable();

			if (kids != null)
				setEventsAndVariables(tba, kids, viewsToSend);

		}
	}
	@SuppressWarnings("rawtypes")
	private static void handleNode(BA tba, Node n, Map<String, Object> viewsToSend) throws InstantiationException, IllegalAccessException {
		if (n.getId() != null) {
			NodeWrapper nodeWrapper = null;
			for (NativeAndWrapper nw : PaneWrapper.nativeToWrapper) {
				if (nw.nativeClass.isAssignableFrom(n.getClass())) {
					nodeWrapper = (NodeWrapper)AbsObjectWrapper.ConvertToWrapper((ObjectWrapper)nw.wrapperClass.newInstance(), n);
					break;
				}
			}
			String name = n.getId().toLowerCase(BA.cul);
			viewsToSend.put(name, nodeWrapper);
			nodeWrapper.innerInitialize(tba, name , true);

		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <TT extends MenuItem> void  handleMenus (BA tba, List<TT> menus, Map<String, Object> viewsToSend) {
		for (MenuItem mi : menus) {
			if (mi instanceof Menu) {
				Menu menu = (Menu) mi;
				if (menu.getId() != null) {
					String name = menu.getId().toLowerCase(BA.cul);
					MenuWrapper mw = new MenuWrapper();
					mw.setObject(menu);
					mw.innerInitialize(tba, name , true);
					viewsToSend.put(name, mw);
				}
				handleMenus(tba, menu.getItems(), viewsToSend);
			}
			else if (mi.getId() != null) {
				String name = mi.getId().toLowerCase(BA.cul);
				MenuItemWrapper miw;
				if (mi instanceof CheckMenuItem)
					miw = new MenuItemWrapper.CheckMenuItemWrapper();
				else
					miw = new MenuItemWrapper();
				miw.setObject(mi);
				miw.innerInitialize(tba, name, true);
				viewsToSend.put(name, miw);
			}
		}
	}
}
