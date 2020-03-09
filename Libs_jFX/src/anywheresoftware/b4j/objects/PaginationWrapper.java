
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
import java.util.HashMap;
import java.util.Random;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;

/**
 * A container of pages (panes). Includes a ruler of page indicators.
 */
@ShortName("Pagination")
@Events(values={"PageChanged (PageIndex As Int)"})
public class PaginationWrapper extends ControlWrapper<Pagination>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new Pagination());
		super.innerInitialize(ba, eventName, true);
		final B4JPageFactory fac = new B4JPageFactory();
		getObject().setPageFactory(fac);
		fac.panes.addListener(new ListChangeListener<Pane>() {

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends Pane> c) {
				getObject().setPageCount(fac.panes.size());
			}
			
		});
		
		final Object sender = getObject();
		if (ba.subExists(eventName + "_pagechanged")) {
			getObject().currentPageIndexProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(
						ObservableValue<? extends Number> observable,
						Number oldValue, Number newValue) {
					ba.raiseEventFromUI(sender, eventName + "_pagechanged", newValue.intValue());
					
				}
			});
		}
	}
	/**
	 * Gets a list with the panes.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getPanes() {
		List l1 = new List();
		l1.setObject((ObservableList)getRawPanes());
		return l1;
	}
	private ObservableList<Pane> getRawPanes() {
		return ((B4JPageFactory)getObject().getPageFactory()).panes;
	}
	/**
	 * Gets the current visible pane.
	 */
	public ConcretePaneWrapper getSelectedItem() {
		return (ConcretePaneWrapper)AbsObjectWrapper.ConvertToWrapper(new ConcretePaneWrapper(), getRawPanes().get(getObject().getCurrentPageIndex()));
	}
	/**
	 * Gets or sets the maximum number of indicators that will be displayed.
	 */
	public int getPageIndicatorCount() {
		return getObject().getMaxPageIndicatorCount();
	}
	public void setPageIndicatorCount(int i) {
		getObject().setMaxPageIndicatorCount(i);
	}
	/**
	 * Gets or sets the selected pane.
	 */
	public int getSelectedIndex() {
		return getObject().getCurrentPageIndex();
	}
	
	public void setSelectedIndex(int i) {
		getObject().setCurrentPageIndex(i);
	}
	/**
	 * Creates a new page. The LayoutFile is loaded to the pane.
	 *LayoutFile - The layout file will be loaded to the page content.
	 */
	@RaisesSynchronousEvents
	public ConcretePaneWrapper LoadLayout(BA ba, String LayoutFile) throws Exception {
		ConcretePaneWrapper cnw = new ConcretePaneWrapper();
		cnw.Initialize(ba, "");
		cnw.LoadLayout(ba, LayoutFile);
		getRawPanes().add(cnw.getObject());
		
		return cnw;
		
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		Pagination vg = (Pagination) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(Pagination.class, props, designer);
			if (designer) {
				B4JPageFactory f = new B4JPageFactory();
				vg.pageFactoryProperty().setValue(f);
				Random r = new Random();
				for (int i = 1;i <= 20;i++) {
					Pane p = new Pane();
					StringBuilder sb = new StringBuilder();
					NodeWrapper.appendColorProp(sb, "-fx-background-color", new byte[] {(byte) 255,(byte) r.nextInt(255),(byte) r.nextInt(255),(byte) r.nextInt(255)}); 
					p.setStyle(sb.toString());
					f.panes.add(p);
				}
				vg.setPageCount(20);
			} else {
				vg.setPageCount(1);
			}
		}
		vg.setMaxPageIndicatorCount((int)props.get("pageIndicators"));
		return ControlWrapper.build(vg, props, designer);
	}
	
	@Hide
	public static class B4JPageFactory implements Callback<Integer, Node> {
		public ObservableList<Pane> panes = FXCollections.observableArrayList();
		private Pane defaultPane = new Pane();
		@Override
		public Node call(Integer param) {
			if (panes.size() == 0)
				return defaultPane;
			return panes.get(param);
		}
		
	}


}
