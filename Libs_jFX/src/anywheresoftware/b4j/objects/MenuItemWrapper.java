
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.layout.AnchorPane;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DontInheritEvents;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.ImageViewWrapper.ImageWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;
import anywheresoftware.b4j.objects.collections.JSONParser;

@Events(values={"Action"})
@DontInheritEvents
@Hide
public class MenuItemWrapper<T extends MenuItem> extends AbsObjectWrapper<T> {
	protected BA ba;
	/**
	 * Initializes a menu item with the given text.
	 */
	public void Initialize(final BA ba, String Text, String EventName) {
		innerInitialize(ba, EventName.toLowerCase(BA.cul), false);
		getObject().setText(Text);
	}
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		this.ba = ba;
		if (ba == null)
			return;
		if (ba.subExists(eventName + "_action")) {
			getObject().setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent arg0) {
					ba.raiseEventFromUI(getObject(), eventName + "_action");
					arg0.consume();
				}
			});
		}
	}
	/**
	 * Gets or sets the menu text.
	 */
	public String getText() {
		return getObject().getText();
	}
	public void setText(String s) {
		getObject().setText(s);
	}
	/**
	 * Gets or sets the tag object tied to this menu item.
	 */
	public Object getTag() {
		return getObject().getUserData();
	}
	public void setTag(Object o) {
		getObject().setUserData(o);
	}
	/**
	 * Gets or sets whether the menu item is enabled.
	 */
	public boolean getEnabled() {
		return !getObject().isDisable();
	}
	public void setEnabled(boolean b) {
		getObject().setDisable(!b);
	}
	/**
	 * Gets or sets whether the menu item is visible.
	 */
	public boolean getVisible() {
		return getObject().isVisible();
	}
	public void setVisible(boolean v) {
		getObject().setVisible(v);
	}
	/**
	 * Gets or sets the image that is displayed before the text.
	 *The image size is typically 16x16.
	 */
	public void setImage(Image Image) {
		ImageView iv = new ImageView(Image);
		getObject().setGraphic(iv);
	}
	public ImageWrapper getImage() {
		ImageWrapper iv = new ImageWrapper();
		if (getObject().getGraphic() instanceof ImageView)
			iv.setObject(((ImageView)getObject().getGraphic()).getImage());
		return iv;
	}
	/**
	 * Returns the menu item parent. The object returned will be uninitialized if there is no parent.
	 */
	public MenuWrapper getParentMenu() {
		return (MenuWrapper)AbsObjectWrapper.ConvertToWrapper(new MenuWrapper(), getObject().getParentMenu());
	}
	
	/**
	 * Menu holds a List of MenuItems. A Menu can also hold other Menu objects.
	 */
	@ShortName("Menu")
	public static class MenuWrapper extends AbsObjectWrapper<Menu> {
		public void Initialize(BA ba,String Text, String EventName) {
			setObject(new Menu());
			getObject().setText(Text);
			innerInitialize(ba, EventName.toLowerCase(BA.cul), true);
		}
		
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			
		}
		/**
		 * Returns a List with the child items. You can modify this list and add new items. 
		 */
		public List getMenuItems() {
			return (List)AbsObjectWrapper.ConvertToWrapper(new List(), getObject().getItems());
		}
		/**
		 * Gets or sets the menu text.
		 */
		public String getText() {
			return getObject().getText();
		}
		public void setText(String s) {
			getObject().setText(s);
		}
		/**
		 * Gets or sets the tag object tied to this menu.
		 */
		public Object getTag() {
			return getObject().getUserData();
		}
		public void setTag(Object o) {
			getObject().setUserData(o);
		}
		/**
		 * Returns the menu parent. The object returned will be uninitialized if there is no parent.
		 */
		public MenuWrapper getParentMenu() {
			return (MenuWrapper)AbsObjectWrapper.ConvertToWrapper(new MenuWrapper(), getObject().getParentMenu());
		}
	}
	@ShortName("MenuItem")
	public static class ConcreteMenuItemWrapper extends MenuItemWrapper<MenuItem> {
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new MenuItem());
			super.innerInitialize(ba, eventName, true);
		} 
	}
	/**
	 * A checked menu item.
	 */
	@Events(values={"SelectedChange (Selected As Boolean)"})
	@ShortName("CheckMenuItem")
	public static class CheckMenuItemWrapper extends MenuItemWrapper<CheckMenuItem> {
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new CheckMenuItem());
			super.innerInitialize(ba, eventName, true);
			if (ba == null)
				return;
			if (ba.subExists(eventName + "_selectedchange")) {
				getObject().selectedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(
							ObservableValue<? extends Boolean> arg0,
							Boolean arg1, Boolean arg2) {
						ba.raiseEventFromUI(getObject(), eventName + "_selectedchange", arg2.booleanValue());
					}
				});
			}
		}
		/***
		 * Gets or sets whether the menu item is checked.
		 */
		public boolean getSelected() {
			return getObject().isSelected();
		}
		public void setSelected(boolean b) {
			getObject().setSelected(b);
		}
			
	}
	/**
	 * A bar that holds Menus.
	 *The two events are raised by menu items added with the internal designer.
	 */
	@DontInheritEvents
	@Events(values={"Action", "SelectedChange (Selected As Boolean)"})
	@ShortName("MenuBar")
	public static class MenuBarWrapper extends ControlWrapper<MenuBar> {
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new MenuBar());
			super.innerInitialize(ba, eventName, true);
			AnchorPane.setLeftAnchor(getObject(), 0d);
			AnchorPane.setRightAnchor(getObject(), 0d);
		}
		/**
		 * Returns a List with the Menus.
		 */
		public List getMenus() {
			return (List) AbsObjectWrapper.ConvertToWrapper(new List(), getObject().getMenus());
		}
		
		@Hide
		public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
			MenuBar vg = (MenuBar) prev;
			if (vg == null) {
				vg = NodeWrapper.buildNativeView(MenuBar.class, props, designer);
			}
			vg.getMenus().setAll(parseMenusJson((BA)props.get("ba"),(String)props.get("menuItems"), (String)props.get("eventName")));
			vg = (MenuBar) ControlWrapper.build(vg, props, designer);
			return vg;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Hide
		public static ArrayList<Menu> parseMenusJson(BA ba, String json, String defaultEventName) {
			String menuItems = json.trim();
			if (menuItems.length() > 0) {
				List items = null;
				try {
					JSONParser parser = new JSONParser();
					parser.Initialize(menuItems);
					items = parser.NextArray();
				}
				catch (Exception e) {
					e.printStackTrace();
					return new ArrayList<Menu>(Arrays.asList(new Menu("Error parsing JSON string")));
				}
				return (ArrayList<Menu>)(ArrayList)(createMenus(ba, items, defaultEventName).getObject());
			}
			return new ArrayList<Menu>();
		}
		@Hide
		public static List createMenus(BA ba, List items, String defaultEventName) {
			List menus = new List();
			menus.Initialize();
			for (Object o : items.getObject()) {
				menus.Add(createMenuItem(ba, o, defaultEventName));
			}
			return menus;
		}
		private static MenuItem createMenuItem(BA ba, Object item, String defaultEventName) {
			if (item instanceof String) {
				if (((String)item).equals("-"))
					return new SeparatorMenuItem();
				ConcreteMenuItemWrapper miw = new ConcreteMenuItemWrapper();
				miw.Initialize(ba, (String)item, defaultEventName);
				return miw.getObject();
			} else {
				Map<String, Object> map = (Map<String, Object>)item;
				String text = (String)map.get("Text");
				String eventName = (String) map.get("EventName");
				String tag = (String)map.get("Tag");
				Boolean enabled = (Boolean)map.get("Enabled");
				if (eventName == null)
					eventName = defaultEventName;
				String image = (String)map.get("Image");
				Image img = null;
				if (image != null) {
					String imageUri = NodeWrapper.getImageUri(image, ba == null);
					if (imageUri != null)
						img = new Image(imageUri);
					
				}
				ArrayList<Object> children = (ArrayList<Object>) map.get("Children");
				if (children != null) {
					MenuWrapper mw = new MenuWrapper();
					mw.Initialize(ba, text, eventName);
					List items = mw.getMenuItems();
					for (Object o : children) {
						items.Add(createMenuItem(ba, o, defaultEventName));
					}
					mw.setTag(tag);
					if (img != null) {
						ImageView iv = new ImageView();
						iv.setImage(img);
						mw.getObject().setGraphic(iv);
					}
					return mw.getObject();
				}
				MenuItemWrapper<?> mmm;

				
				Boolean checked = (Boolean)map.get("Selected");
				
				if (checked != null) {
					CheckMenuItemWrapper cmi = new CheckMenuItemWrapper();
					cmi.Initialize(ba, text, eventName);
					cmi.setSelected(checked);
					mmm = cmi;
				}
				else {
					ConcreteMenuItemWrapper miw2 = new ConcreteMenuItemWrapper();
					miw2.Initialize(ba, text, eventName);
					mmm = miw2;
				}
				if (enabled != null)
					mmm.setEnabled(enabled);
				if (img != null)
					mmm.setImage(img);
				mmm.setTag(tag);
				
				Map<String, Object> shortcut = (Map<String, Object>)map.get("Shortcut");
				if (shortcut != null) {
					String keyCode = (String)shortcut.get("Key");
					if (Character.isDigit(keyCode.charAt(0)))
						keyCode = "DIGIT" + keyCode.charAt(0);
					KeyCode kc = KeyCode.valueOf(keyCode.toUpperCase(BA.cul));
					Object modifier = shortcut.get("Modifier");
					KeyCodeCombination combine = null;
					if (modifier instanceof String) {
						combine = new KeyCodeCombination(kc, stringToModifier((String)modifier));
					} else if (modifier instanceof java.util.List) {
						java.util.List<Object> modifiers = (java.util.List<Object>)modifier;
						Modifier[] modArray = new Modifier[modifiers.size()];
						for (int i = 0;i < modifiers.size();i++)
							modArray[i] = stringToModifier((String)modifiers.get(i));
						combine = new KeyCodeCombination(kc, modArray);
					}
					else
						combine = new KeyCodeCombination(kc);
					mmm.getObject().setAccelerator(combine);
				}
				return mmm.getObject();
				
			}
		}
		private static KeyCombination.Modifier stringToModifier(String s) {
			s = s.toUpperCase(BA.cul);
			if (s.equals("ALT"))
				return KeyCombination.ALT_DOWN;
			else if (s.equals("CONTROL"))
				return KeyCombination.CONTROL_DOWN;
			else if (s.equals("SHIFT"))
				return KeyCombination.SHIFT_DOWN;
			else if (s.equals("SHORTCUT"))
				return KeyCombination.SHORTCUT_DOWN;
			BA.LogError("Invalid modifier: " + s);
			return null;
		}
	}
	/**
	 * A special Menu that is tied to a control and appears when the user "right clicks" on the control.
	 */
	@ShortName("ContextMenu")
	public static class ContextMenuWrapper extends AbsObjectWrapper<ContextMenu> {
		public void Initialize(BA ba, String EventName) {
			innerInitialize(ba, EventName.toLowerCase(BA.cul), false);
		}
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new ContextMenu());
		}
		/**
		 * Returns a List with the menu items.
		 */
		public List getMenuItems() {
			return (List) AbsObjectWrapper.ConvertToWrapper(new List(), getObject().getItems());
		}
	}
	
}
