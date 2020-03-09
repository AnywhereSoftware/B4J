
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

import javafx.scene.paint.Paint;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.DesignerProperties;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Property;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4j.objects.CustomViewWrapper.DesignerCustomView;
import anywheresoftware.b4j.objects.NodeWrapper.ControlWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;

@Version(1.10f)
@DependsOn(values= {"GaugesResources"})
@ShortName("Gauge")
@DesignerProperties(values={
		@Property(key="SkinType", displayName="Skin Value", fieldType="string", defaultValue="SIMPLE_SECTION", 
				list="AMP|BULLET_CHART|DASHBOARD|FLAT|GAUGE|INDICATOR|KPI|MODERN|SIMPLE|SLIM|SPACE_X|QUARTER|HORIZONTAL|VERTICAL|TINY|BATTERY|LEVEL|LINEAR|DIGITAL|SIMPLE_DIGITAL|SECTION|BAR|WHITE|CHARGE|SIMPLE_SECTION|TILE_KPI|TILE_TEXT_KPI|TILE_SPARK_LINE"),
		@Property(key="MinValue", displayName="Min Value", fieldType="float", defaultValue="0"),
		@Property(key="MaxValue", displayName="Max Value", fieldType="float", defaultValue="100"),
		@Property(key="Value", displayName="Value", fieldType="float", defaultValue="50"),
		@Property(key="Title", displayName="Title", fieldType="string", defaultValue=""),
		@Property(key="Unit", displayName="Unit", fieldType="string", defaultValue=""),
		@Property(key="SubTitle", displayName="Sub Title", fieldType="string", defaultValue=""),
		
})
public class GaugeWrapper extends ControlWrapper<Gauge> implements DesignerCustomView {
	@Hide
	@Override
	public void _initialize(BA ba, Object target, String EventName) {
	}
	@Override
	public void DesignerCreateView(final ConcretePaneWrapper base, LabelWrapper label,
			Map args) {
		String type = (String)args.Get("SkinType");
		Gauge g = GaugeBuilder.create()
				.skinType(BA.getEnumFromString(SkinType.class, type))
				.minValue((Float)args.Get("MinValue"))
				.maxValue((Float)args.Get("MaxValue"))
				.title((String)args.Get("Title"))
				.unit((String)args.Get("Unit"))
				.subTitle((String)args.Get("SubTitle"))
				.value((Float)args.Get("Value"))
				.animated(type.equals("CHARGE") == false)
				.build();
		g.setMaxValue((Float)args.Get("MaxValue"));
		setObject(g);
		
		base.AddNode(getObject(), 0, 0, base.getWidth(), base.getHeight());
		
		new PaneWrapper.ResizeEventManager(base.getObject(), null, new Runnable() {
			
			@Override
			public void run() {
				SetLayoutAnimated(0, 0, 0, base.getWidth(), base.getHeight());
			}
		});

		
	}
	/**
	 * Returns the base pane. When the control is added as a custom view it is added to a base pane.
	 *You can use BaseView to move or remove the view.
	 */
	public ConcretePaneWrapper getBaseView() {
		return (ConcretePaneWrapper)AbsObjectWrapper.ConvertToWrapper(new ConcretePaneWrapper(), getObject().getParent());
	}
	/**
	 * Gets or sets the current value.
	 */
	public double getValue() {
		return getObject().getValue();
	}
	public void setValue(double d) {
		getObject().setValue(d);
	}
	/**
	 * Adds a colored section between Start and Stop values.
	 */
	public void AddSection(double Start, double Stop, Paint Color) {
		Section s = new Section(Start, Stop, (javafx.scene.paint.Color) Color);
		s.setText("Sdfsdf");
		getObject().addSection(s);
		getObject().setSectionsVisible(true);
	}
	

}
