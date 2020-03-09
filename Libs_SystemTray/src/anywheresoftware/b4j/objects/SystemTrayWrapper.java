
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

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;

@Version(1.10f)
@ShortName("SystemTray")
public class SystemTrayWrapper  {
	@Hide
	public SystemTray st;
	/**
	 * Initializes the system tray.
	 */
	public void Initialize() {
		if (SystemTray.isSupported())
			st = SystemTray.getSystemTray();
	}
	/**
	 * Tests whether the system tray is supported.
	 */
	public boolean getSupported() {
		return SystemTray.isSupported();
	}

	/**
	 * Adds a tray icon to the system tray.
	 */
	public void AddTrayIcon(TrayIconWrapper TrayIcon) throws AWTException {
		if (st.getTrayIcons().length == 0)
			Platform.setImplicitExit(false);
		st.add(TrayIcon.getObject());
	}
	/**
	 * Removes a tray icon from the system tray.
	 */
	public void RemoveTrayIcon(TrayIconWrapper TrayIcon) {
		st.remove(TrayIcon.getObject());
		if (st.getTrayIcons().length == 0)
			Platform.setImplicitExit(true);
	}
	@ShortName("TrayIcon")
	@Events(values={"DoubleClick", "MenuClick (Text As String)", "Click"})
	public static class TrayIconWrapper extends AbsObjectWrapper<TrayIcon> {
		/**
		 * Initializes the tray icon.
		 *EventName - Sets the subs that will handle the events.
		 *Image - Icon image.
		 *MenuItems - A list of strings that will show as a popup menu when the user right clicks on the icon.
		 */
		public void Initialize(final BA ba, String EventName, Image Image, List MenuItems) {
			final TrayIcon ti = new TrayIcon(SwingFXUtils.fromFXImage(Image, null));
			setObject(ti);
			ti.setImageAutoSize(true);
			final String eventName = EventName.toLowerCase(BA.cul);
			if (ba.subExists(eventName + "_doubleclick")) {
				ti.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ba.raiseEventFromDifferentThread(ti, null, 0, eventName + "_doubleclick", false, null);
					}
				});
			}
			if (ba.subExists(eventName + "_click")) {
				ti.addMouseListener(new MouseAdapter() {
				  public void mouseClicked(MouseEvent e) {
				        if (e.getClickCount() == 1) {
				        	ba.raiseEventFromDifferentThread(ti, null, 0, eventName + "_click", false, null);
				        }
				    }
				});
			}

			if (MenuItems != null && MenuItems.IsInitialized()) {
				PopupMenu menu = new PopupMenu();
				for (Object s : MenuItems.getObject()) {
					final MenuItem mi = new MenuItem(String.valueOf(s));
					menu.add(mi);
					mi.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							ba.raiseEventFromDifferentThread(ti, null, 0, eventName + "_menuclick", true, new Object[] {mi.getLabel()});
						}
					});
				}
				ti.setPopupMenu(menu);
			}

		}
		/**
		 * Sets the icon image.
		 */
		public void SetImage(Image Image) {
			getObject().setImage(SwingFXUtils.fromFXImage(Image, null));
		}
		/**
		 * Gets or sets the icon tooltip.
		 */
		public void setToolTip(String s) {
			getObject().setToolTip(s);
		}
		public String getToolTip() {
			return getObject().getToolTip();
		}

	}
}
