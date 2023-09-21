package anywheresoftware.b4j.objects;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import anywheresoftware.b4a.B4AClass;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4j.objects.LayoutBuilder.LayoutData;
import javafx.scene.Node;

@ShortName("DesignerArgs")
public class DesignerArgs {
	@Hide
	public LayoutData views;
	private List args;
	private int width, height;
	private Node parent;
	private LayoutValues variant;
	private Object layoutTarget;
	@Hide
	public final static HashMap<String, B4AClass> targetsCache = new HashMap<String, B4AClass>();

	/**
	 * Shorthand for GetViewByName(Args.Get(Index)).
	 */
	public Node GetViewFromArgs(int Index) {
		return GetViewByName((String) args.Get(Index));
	}
	/**
	 * Returns the view with the set name.
	 *Throws an error if no such view exists. 
	 */
	public Node GetViewByName(String Name) {
		try {
			return (Node) views.get(Name.toLowerCase(BA.cul)).getObjectOrNull();
		} catch (NullPointerException npe) {
			throw new RuntimeException("No such view: " + Name);
		}
	}
	/**
	 * Returns the layout parent width.
	 */
	public int getParentWidth() {
		return width;
	}
	/**
	 * Returns the layout parent height.
	 */
	public int getParentHeight() {
		return height;
	}
	/**
	 * Returns the passed arguments.
	 */
	public List getArguments() {
		return args;
	}
	/**
	 * Returns the layout parent.
	 */
	public Node getParent() {
		return parent;
	}
	/**
	 * Returns the parameters of the chosen variant.
	 */
	public LayoutValues getChosenVariant() {
		return variant;
	}
	/**
	 * Returns the low level layout data.
	 */
	public Map getDesignerProperties() {
		Map m = new Map();
		m.setObject(views.props);
		return m;
	}
	/**
	 * Returns a list with the views names.
	 */
	public List getViewsNames() {
		List l = new List();
		l.Initialize();
		for (String name : views.viewsMap.keySet())
			l.Add(name);
		return l;
	}
	/**
	 * Returns a list with the layout views.
	 */
	public List getViews() {
		List l = new List();
		l.Initialize();
		for (WeakReference<Node> nn : views.viewsMap.values()) {
			Node n = nn.get();
			if (n != null)
				l.Add(n);
		}
		return l;
	}
	/**
	 * Returns true if the layout is being created right now.
	 *It will always be true in B4A as there is no resize event.
	 */
	public boolean getFirstRun() {
		return views.firstRun;
	}
	/**
	 * Gets a reference to the module that the layout is being loaded to. Can be used with CallSub.
	 *Returns Null if the layout is being resized.
	 */
	public Object getLayoutModule() {
		return layoutTarget;
	}
	@Hide
	public static String callsub(BA ba, Node parent, LayoutValues lv, String module, String method, int width, int height, LayoutData views, Object[] args) throws Exception{
		DesignerArgs da = new DesignerArgs();
		da.views = views;
		da.args = Common.ArrayToList(args);
		da.width = width;
		da.height = height;
		da.parent = parent;
		da.variant = lv;
		da.layoutTarget = views.firstRun ?  ba.eventsTarget : null;
		B4AClass target = targetsCache.get(module);
		if (target == null) {
			Class<?> cls = Class.forName(BA.packageName + "." + module);
			target = (B4AClass) cls.newInstance();
			if (BA.isShellModeRuntimeCheck(ba)) {
				ba.raiseEvent2(null, true, "CREATE_CLASS_INSTANCE", true, target, ba);
			}
			else {
				cls.getMethod("_initialize", BA.class).invoke(target, ba);
			}
			
			targetsCache.put(module, target);
		}
		return String.valueOf(target.getBA().raiseEvent2(null, false, method, true, da));
	}
}
