
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
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.ConnectorUtils;
import anywheresoftware.b4a.DynamicBuilder;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.streams.File;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;
import anywheresoftware.b4j.objects.ButtonWrapper.RadioButtonWrapper;
import anywheresoftware.b4j.objects.NodeWrapper.ConcreteNodeWrapper;

@Hide
public class LayoutBuilder {
	private LayoutData layoutData;
	private HashMap<String, Object> viewsToSendInShellMode;
	private List<CustomViewWrapper> customViewWrappers = new ArrayList<CustomViewWrapper>();
	private HashMap<String, Field> classFields;
	private static WeakReference<Scene> lastScene;
	private static final int LOADLAYOUT = 1, RESIZE = 2, AUTOSCALE = 3;

	public LayoutBuilder(LayoutData ld) {
		this.layoutData = ld;
	}
	public LayoutValues loadLayout(String file, BA ba, Pane parent) throws Exception {
		if (BA.isShellModeRuntimeCheck(ba))
			viewsToSendInShellMode = new HashMap<String, Object>();
		layoutData = new LayoutData();
		InputStreamWrapper in = File.OpenInput(File.getDirAssets(), file);
		DataInputStream din = new DataInputStream(in.getObject());
		@SuppressWarnings("unused")
		int version = ConnectorUtils.readInt(din);
		int pos = ConnectorUtils.readInt(din);
		while (pos > 0) {
			pos -= din.skip(pos);
		}
		String[] cache = null;
		cache = new String[ConnectorUtils.readInt(din)];
		for (int i = 0;i < cache.length;i++) {
			cache[i] = ConnectorUtils.readString(din);
		}
		int numberOfVariants = ConnectorUtils.readInt(din);
		for (int i = 0;i < numberOfVariants;i++) {
			layoutData.variants.add(LayoutValues.readFromStream(din));
		}
		int chosenIndex = findBestVariant(parent.getScene());
		layoutData.props = ConnectorUtils.readMap(din, cache);
		din.close();
		int[] pwh = PaneWrapper.getDesignerWidthAndHeight(parent);
		int pw, ph;
		if (pwh[0] == 0 || pwh[1] == 0) {
			if (parent.getScene() != null) {
				pw = (int) parent.getScene().getWidth();
				ph = (int) parent.getScene().getHeight();
			} else {
				pw = 300;
				ph = 300;
			}
		} else {
			pw = pwh[0];
			ph = pwh[1];
		}
		loadLayoutHeader(layoutData.props, ba, parent, true, "variant" + chosenIndex, pw, ph, LOADLAYOUT, 0, 1.0f);
		if ((Boolean)layoutData.props.get("handleResizeEvent") == true)
			AbsObjectWrapper.getExtraTags(parent).put("layoutdata", layoutData);
		layoutData.designerScriptName = file.toLowerCase(BA.cul);
		runScripts(parent, layoutData, chosenIndex, pw, ph);
		if (viewsToSendInShellMode != null)
			ba.raiseEvent(null, "SEND_VIEWS_AFTER_LAYOUT", viewsToSendInShellMode);
		
		for (CustomViewWrapper cvw : customViewWrappers) {
			cvw.AfterDesignerScript();
		}
		
		RadioButtonWrapper.automaticGroups.clear();
		return layoutData.variants.get(chosenIndex);

	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadLayoutHeader(Map<String, Object> props, 
			BA ba, Pane parent, boolean firstCall, String currentVariant, int pw, int ph, int buildType, int duration, float autoscale) throws Exception {
		Node o;
		if (firstCall) {
			duration = (Integer)props.get("duration");
			if (buildType == AUTOSCALE)
				duration = 0;
			else if (buildType == RESIZE)
				duration = Math.min(duration, 50);
			o = parent;
			if (buildType == LOADLAYOUT) {
				Form.build(o, props, false);
			}
		} else {
			HashMap<String, Object> variant = (HashMap<String, Object>) props.get(currentVariant);
			for(String p : new String[] {"left", "top", "width", "height", "hanchor", "vanchor"})
				props.put(p, variant.get(p));
			props.put("pw", pw);
			props.put("ph", ph);
			props.put("duration", duration);
			props.put("ba", ba);
			String upperCaseName = (String)props.get("name");
			String name = upperCaseName.toLowerCase(BA.cul);
			if (buildType == LOADLAYOUT) {
				o = DynamicBuilder.build(null, props, false, ba);
				String cls = (String) props.get("type");
				if (cls.startsWith("."))
					cls = "anywheresoftware.b4j.objects" + cls;
				Object assigningObject;
				
				if (classFields == null) {
					classFields = new HashMap<String, Field>();
					for (Field field : Class.forName(ba.className).getDeclaredFields()) {
						if (field.getName().startsWith("_"))
							classFields.put(field.getName(), field);
					}
				}
				Field field = classFields.get("_" + name);
				NodeWrapper ow;
				if (cls.equals("anywheresoftware.b4j.objects.CustomViewWrapper")) {
					CustomViewWrapper cvw = new CustomViewWrapper();
					ow = cvw;
					cvw.setObject((Pane)o);
					customViewWrappers.add(cvw);
					Object customObject = findCustomViewClass(props).newInstance();
					cvw.customObject = customObject;
					cvw.props = new HashMap<String, Object>(props); //create a copy as it can be later modified
					assigningObject = customObject;
				}
				else {
					ow = (NodeWrapper) Class.forName(cls).newInstance(); 
					ow.setObject(o);
					assigningObject = ow;
					if (field != null && field.getType() != assigningObject.getClass()) {
						//field type doesn't match
						if (BA.debugMode) {
							Type t = ow.getClass().getGenericSuperclass();
							if (t instanceof ParameterizedType) {
								ParameterizedType pt = (ParameterizedType)t;
								if (pt.getActualTypeArguments().length > 0) {
								
									ParameterizedType fieldParamType = (ParameterizedType) field.getType().getGenericSuperclass();
									Class actualType;
									if (pt.getActualTypeArguments()[0] instanceof ParameterizedType) {
										actualType = (Class)((ParameterizedType)pt.getActualTypeArguments()[0]).getActualTypeArguments()[0];
									} else {
										actualType = (Class)(pt.getActualTypeArguments()[0]);
									}
									if (((Class)fieldParamType.getActualTypeArguments()[0]).isAssignableFrom(actualType) == false) {
										throw new RuntimeException("Cannot convert: " + ow.getClass() + ", to: " + field.getType());
									}
								}
							}
						}
						ObjectWrapper nw = (ObjectWrapper) field.getType().newInstance();
						nw.setObject(o);
						assigningObject = nw;
					}
				}
				
				if (viewsToSendInShellMode != null)
					viewsToSendInShellMode.put(name, assigningObject);
				layoutData.viewsMap.put(name, new WeakReference<Node>(o));
				//unlike fxmlbuilder this sets the wrapper and not the internal object.
				//this is better as it creates a new wrapper for each layout loaded.
				if (field != null) { //object was declared in Sub Globals
					try {
						field.set(ba.eventsTarget, assigningObject);
					} catch (IllegalArgumentException ee) {
						throw new RuntimeException("Field " + name  + " was declared with the wrong type.");
					}
				}
				ow.innerInitialize(ba, ((String)props.get("eventName")).toLowerCase(BA.cul), true);
				parent.getChildren().add(o);
			} else {
				WeakReference<Node> viewData = layoutData.viewsMap.get(name);
				o = viewData.get();
				if (o == null)
					return;
				NodeWrapper.buildResize(o, props, autoscale);
			}
		}
		Map<String, Object> kids = (Map<String, Object>) props.get(":kids");
		if (kids != null) {
			if (!firstCall) {
				int[] pwh = PaneWrapper.getDesignerWidthAndHeight((Pane)o);
				pw = pwh[0];
				ph = pwh[1];
			}
			for (int i = 0;i < kids.size();i++) {
				loadLayoutHeader((Map<String, Object>)kids.get(Integer.toString(i)), ba, (Pane)o, false,
						currentVariant, pw, ph, buildType, duration, autoscale);
			}
		}
	}
	private Class<?> findCustomViewClass(Map<String, Object> props) throws ClassNotFoundException {
		String cclass = (String)props.get("customType");
		if (cclass == null || cclass.length() == 0)
			throw new RuntimeException("CustomView CustomType property was not set.");
		Class<?> customClass;
		try {
			customClass = Class.forName(cclass);
		} catch (ClassNotFoundException cnfe) {
			int dollar = cclass.lastIndexOf(".");
			if (dollar > -1) {
				String corrected = BA.packageName + cclass.substring(dollar);
				//BA.LogInfo("Class not found: " + cclass + ", trying: " + corrected);
				customClass = Class.forName(corrected);
			}
			else
				throw cnfe;
		}
		return customClass;
	}
	private static void runScripts(Pane parent, LayoutData ld, int chosenIndex, int w, int h) throws IllegalArgumentException, IllegalAccessException {
		lastScene = new WeakReference<Scene>(parent.getScene());
		StringBuilder sb = new StringBuilder();
		sb.append("LS_");
		for (int i = 0;i < ld.designerScriptName.length() - 4;i++) {
			char c = ld.designerScriptName.charAt(i);
			if (Character.isLetterOrDigit(c))
				sb.append(c);
			else
				sb.append("_");
		}
		try {
			Class<?> c = Class.forName(BA.packageName + ".designerscripts." + sb.toString());
			Method m; 
			try {
				//global script
				m = c.getMethod(variantToMethod(null), LayoutData.class, int.class, int.class, float.class);

				m.invoke(null, ld, w, h, 1.0f);
			} catch (NoSuchMethodException e) {
				//do nothing
			}
			m = c.getMethod(variantToMethod(ld.variants.get(chosenIndex)), LayoutData.class, int.class, int.class, float.class);
			m.invoke(null, ld, w, h, 1.0f);
		} catch (ClassNotFoundException e) {
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}


	}
	private static String variantToMethod(LayoutValues lv)
	{
		String variant;
		if (lv == null)
			variant = "general";
		else
			variant = String.valueOf(lv.Width) + "x" + String.valueOf(lv.Height) + "_" + BA.NumberToString(lv.Scale).replace(".", "_");
		return "LS_" + variant;
	}
	private int findBestVariant(Scene scene) {
		LayoutValues device = new LayoutValues();
		if (scene == null)
			return 0;
		device.Width = (int) scene.getWidth();
		device.Height = (int) scene.getHeight();
		device.Scale = Common.Density;
		int chosenIndex = 0;
		LayoutValues chosen = null;
		float distance = Float.MAX_VALUE;
		for (int i = 0;i < layoutData.variants.size();i++) {
			LayoutValues test = layoutData.variants.get(i);
			if (chosen == null) {
				chosen = test;
				distance = test.calcDistance(device);
			}
			else {
				float testDistance = test.calcDistance(device);
				if (testDistance < distance) {
					chosen = test;
					distance = testDistance;
					chosenIndex = i;
				}
			}
		}
		return chosenIndex;
	}
	public void resizeLayout(BA ba, Pane parent) {
		try {
			int chosenIndex = findBestVariant(parent.getScene());
			int[] pwh = PaneWrapper.getDesignerWidthAndHeight(parent);
			loadLayoutHeader(layoutData.props, ba, parent, true, "variant" + chosenIndex, pwh[0], pwh[1], RESIZE, 0, 1.0f);
			runScripts(parent, layoutData, chosenIndex, pwh[0], pwh[1]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static double getScreenSize() {
		Scene sc = lastScene.get();
		if (sc == null) {
			System.out.println("Scene is null!!!");
			return 10.0;
		}
		return Math.sqrt(Math.pow(sc.getWidth(), 2) + Math.pow(sc.getHeight(), 2)) / Screen.getPrimary().getDpi();
	}
	@Hide
	public static class LayoutData {
		public List<LayoutValues> variants = new ArrayList<LayoutValues>();
		public Map<String, Object> props;
		public Map<String, WeakReference<Node>> viewsMap = new HashMap<String, WeakReference<Node>>();
		public String designerScriptName;
		public LayoutData() {

		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public NodeWrapper get(String name) {
			Node d = viewsMap.get(name).get();
			NodeWrapper cnw;
			if (d instanceof Labeled)
				cnw = new LabeledWrapper();
			else if (d instanceof TextInputControl)
				cnw = new TextInputControlWrapper();
			else
				cnw = new ConcreteNodeWrapper();
			cnw.setObject(d);
			return cnw;
		}
	}
	public interface B4JTextControl {
		String getText();
		void setText(String s);
	}
}