
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
 
 package anywheresoftware.b4j.googlemaps;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.MapStateEventType;
import com.lynden.gmapsfx.javascript.event.StateEventHandler;
import com.lynden.gmapsfx.javascript.event.UIEventHandler;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.InfoWindowOptions;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.LatLongBounds;
import com.lynden.gmapsfx.javascript.object.MVCArray;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.shapes.Circle;
import com.lynden.gmapsfx.shapes.CircleOptions;
import com.lynden.gmapsfx.shapes.Polygon;
import com.lynden.gmapsfx.shapes.PolygonOptions;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.JFX.Colors;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;
import javafx.scene.paint.Paint;
import netscape.javascript.JSObject;

/**
 *Uses GoogleMap JavaScript V3 API to show a map inside WebView.
 *GoogleMap.AsPane returns a Pane which should be added to the nodes tree.
 *The map is only ready after Ready event is raised.
 *Requires Java 8+.
 */
@ShortName("GoogleMap")
@Events(values={
		"Ready", 
		"CameraChange (Position As CameraPosition)",
		"Click (Point As LatLng)",
		"CircleClick (SelectedCircle As MapCircle)",
		"MarkerClick (SelectedMarker As Marker)",
		"PolygonClick (SelectedPolygon As MapPolygon)",
		"PolylineClick (SelectedPolyline As MapPolyline)"
})
@Version(1.82f)
public class GoogleMapWrapper
{
	@Hide
	public GoogleMap map;
	@Hide
	public GoogleMapView mapView;
	private BA ba;
	private String eventName;
	private boolean isReady;

	/**
	 * Initializes the map. The Ready event will be raised when the map is ready.
	 *EventName - Sets the subs that will handle the events.
	 *Options - MapOptions object. Pass Null for the default options.
	 */
	public void Initialize(BA ba, String EventName, MapOptionsWrapper Options) {
		Initialize2(ba, EventName, Options, null);
	}
	/**
	 * Similar to Initialize. Allows setting the <link>api key|https://developers.google.com/maps/documentation/javascript/get-api-key#key</link>.
	 *Example: <code>map.Initialize2("map", Null, "AIzattttttt_tttttttthGujrM")</code>
	 */
	public void Initialize2(BA ba, String EventName, MapOptionsWrapper Options, String ApiKey) {
		mapView = new GoogleMapView(null, ApiKey);
		this.eventName = EventName.toLowerCase(BA.cul);
		this.ba = ba;
		mapView.addMapInializedListener(new MapComponentInitializedListener() {
			@Override
			public void mapInitialized()
			{
				isReady = false;
				MapOptions mapOptions;
				mapOptions = new MapOptions();
				mapOptions.center(new LatLong(20, 20)).zoom(2);
				if (Options != null) {
					mapOptions.streetViewControl(Options.StreetViewControl).overviewMapControl(Options.OverviewMapControl)
					.panControl(Options.PanControl).zoomControl(Options.ZoomControl)
					.mapTypeControl(Options.MapTypeControl).scrollWheel(Options.ScrollWheel);
				}
				
				mapOptions.jsObject.setMember("fullscreenControl", false);
				map = mapView.createMap(mapOptions);
				mapOptions.mapType(MapTypeIdEnum.SATELLITE);
				if (ba.subExists(eventName + "_camerachange")) {
					map.addStateEventHandler(MapStateEventType.bounds_changed, new StateEventHandler() {
						@Override
						public void handle() {
							ba.raiseEventFromUI(GoogleMapWrapper.this, eventName + "_camerachange", getCameraPosition());
						}
					});
				}
				isReady = true;
				ba.raiseEvent(GoogleMapWrapper.this, eventName + "_ready");
				if (ba.subExists(eventName + "_click")) {
					map.addUIEventHandler(UIEventType.click, new UIEventHandler() {
						@Override
						public void handle(JSObject obj) {
							Object o = obj.getMember("latLng");
							if (o instanceof JSObject) {
								JSObject latLng = (JSObject)o;
								LatLong ll = new LatLong(latLng);
								ba.raiseEventFromUI(GoogleMapWrapper.this, eventName + "_click", 
										AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), new LatLong(ll.getLatitude(), ll.getLongitude())));
							}
						}
					});
				}
			}
		});
	}

	/**
	 * MapOptions is passed to GoogleMap.Initialize method.
	 *It allows you to choose which controls will be available.
	 */
	@ShortName("MapOptions")
	public static class MapOptionsWrapper
	{
		public boolean PanControl = true;
		public boolean StreetViewControl = true;
		public boolean OverviewMapControl = true;
		public boolean MapTypeControl = true;
		public boolean ScrollWheel = true;
		public boolean ZoomControl = true;
	}

	/**
	 * Holds latitude and longitude values.
	 */
	@ShortName("LatLng")
	public static class LatLngWrapper extends AbsObjectWrapper<LatLong>
	{
		/**
		 * Returns the latitude value.
		 */
		public double getLatitude() {
			return getObject().getLatitude();
		}
		/**
		 * Returns the longitude value.
		 */
		public double getLongitude() {
			return getObject().getLongitude();
		}
		/**
		 * Initializes a new object.
		 */
		public void Initialize(double Latitude, double Longitude) {
			setObject(new LatLong(Latitude, Longitude));
		}
	}

	/** Returns True when the map is ready. */
	public boolean IsReady()
	{
		return isReady;
	}

	//------------------------------------------- CAMERA -------------------------------------------

	/**
	 * The map view is modeled as a camera looking down on a flat plane.
	 *See this <link>link|https://developers.google.com/maps/documentation/android/views#the_camera_position</link> for more information about the possible values.
	 */
	@ShortName("CameraPosition")
	public static class CameraPositionWrapper
	{
		LatLong ll;
		int zoom;
		/**
		 * Initializes the camera position with the given latitude, longitude and zoom.
		 */
		public void Initialize(double Lat, double Lng, int Zoom)
		{
			ll = new LatLong(Lat, Lng);
			zoom = Zoom;
		}

		/**
		 * Returns the location that the camera is pointing at.
		 */
		public LatLngWrapper getTarget()
		{
			return (LatLngWrapper)AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), ll);
		}

		/**
		 * Returns the zoom level.
		 */
		public float getZoom()
		{
			return zoom;
		}

		@Override
		public String toString()
		{
			return String.valueOf(ll) + ", zoom: " + zoom;
		}
	}

	/**
	 * Moves the camera position.
	 */
	public void MoveCamera(CameraPositionWrapper NewPosition)
	{
		map.setCenter(NewPosition.ll);
		map.setZoom(NewPosition.zoom);
	}

	/**
	 * Moves the camera position.
	 */
	public void MoveCamera2(LatLngWrapper NewPosition)
	{
		map.setCenter(NewPosition.getObject());
	}

	/**
	 * Returns the current camera position.
	 */
	public CameraPositionWrapper getCameraPosition()
	{
		CameraPositionWrapper cpw = new CameraPositionWrapper();
		cpw.ll = map.getCenter();
		cpw.zoom = map.getZoom();
		return cpw;
	}

	/** Sets the zoom level. */
	public void setZoom(int zoom)
	{
		map.setZoom(zoom);
	}

	//------------------------------------------- BOUNDS -------------------------------------------

	@BA.ShortName("LatLongBounds")
	public static class LatLongBoundsWrapper extends AbsObjectWrapper<LatLongBounds>
	{
		public LatLngWrapper getNorthEast()
		{
			return (LatLngWrapper)AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), getObject().getNorthEast());
		}

		public LatLngWrapper getSouthWest()
		{
			return (LatLngWrapper)AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), getObject().getSouthWest());
		}

		public void Initialize(LatLngWrapper NorthEast, LatLngWrapper SouthWest)
		{
			setObject(new LatLongBounds(SouthWest.getObject(), NorthEast.getObject()));
		}
	}

	/**
	 *Returns the LatLongBounds of the visual area. It is recommended to read Bounds only inside the CameraChange event because its values may be incorrect otherwise.
	 */
	public LatLongBoundsWrapper getBounds() {
		return (LatLongBoundsWrapper)AbsObjectWrapper.ConvertToWrapper(new LatLongBoundsWrapper(), map.getBounds());
	}

	/**Moves the map to ensure the given bounds fit within the viewport.
	 *Note that the Google Maps API will add a margin around these bounds.
	 */
	public void FitBounds(LatLongBoundsWrapper bounds ) {
		map.fitBounds(bounds.getObject());
	}

	/** Pans the map by the minimum amount necessary to contain the given LatLongBounds. It makes no guarantee where on the map the bounds will be, except that as much of the bounds as possible will be visible. */
	public void PanToBounds(LatLongBoundsWrapper bounds) {
		map.panToBounds(bounds.getObject());
	} 

	//------------------------------------------- MARKER -------------------------------------------

	@ShortName("Marker")
	public static class MarkerWrapper extends AbsObjectWrapper<Marker>
	{
		
		/**
		 * Get or sets the marker position.
		 */
		public void setPosition(LatLngWrapper value)
		{
			getObject().setPosition(value.getObject());
		}
		public LatLngWrapper getPosition() {
			return (LatLngWrapper) AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), getObject().getPosition());
		}
		
		/**
		 * Gets or sets whether the marker is visible.
		 */
		public void setVisible(boolean v) {
			getObject().setVisible(v);
		}
		public boolean getVisible() {
			return getObject().getVisible();
		}
		/**
		 * Sets the marker title.
		 */
		public void setTitle(String v) {
			getObject().setTitle(v);
		}
	}

	/**
	 * Adds a marker to the map.
	 */
	public MarkerWrapper AddMarker(double Lat, double Lon, String Title)
	{
		return AddMarker2(Lat, Lon, Title, "");
	}

	/**
	 * Adds a marker to the map with a custom icon.
	 *The IconUri must point to an online image or an image from the assets folder.
	 *<b>In the later case the custom icon will only appear in Release mode.</b>
	 *Example: <code>
	 * gmap.AddMarker2(10, 10, "This is a test", _
	 *	 	File.GetUri(File.DirAssets, "SomeIcon.png"))</code>
	 */
	public MarkerWrapper AddMarker2(double Lat, double Lon, String Title, String IconUri)
	{
		MarkerOptions mo = new MarkerOptions().position(new LatLong(Lat, Lon))
				.title(Title);
		if (IconUri.length() > 0) {
			if (!IconUri.startsWith("file") || BA.isShellModeRuntimeCheck(ba) == false)
				mo.icon(IconUri);
		}
		final Marker m = new Marker(mo);
		map.addMarker(m);
		if (ba.subExists(eventName + "_markerclick")) {
			map.addUIEventHandler(m, UIEventType.click, new UIEventHandler() {
				@Override
				public void handle(JSObject obj) {
					ba.raiseEventFromUI(GoogleMapWrapper.this, eventName + "_markerclick", 
							(MarkerWrapper)AbsObjectWrapper.ConvertToWrapper(new MarkerWrapper(), m));
				}
			});
		}
		return (MarkerWrapper)AbsObjectWrapper.ConvertToWrapper(new MarkerWrapper(), m);
	}
	

//	/**
//	 *Adds a marker to the map with a custom icon and sets the origin and anchor positions of this marker (in pixels).
//	 *The IconUri must point to an online image or an image from the assets folder.
//	 *<b>In the later case the custom icon will only appear in Release mode.</b>
//	 *Example: <code>
//	 * gmap.AddMarker3(10, 10, "This is a test", _
//	 *	 	File.GetUri(File.DirAssets, "SomeIcon.png"), 0, 0, 0, 32)</code>
//	 */
//	public MarkerWrapper AddMarker3(double Lat, double Lon, String Title, String IconUri, double OriginX, double OriginY, double AnchorX, double AnchorY)
//	{
//		MarkerOptions mo = new MarkerOptions().position(new LatLong(Lat, Lon))
//				.title(Title);
//		if (IconUri.length() > 0) {
//			if (!IconUri.startsWith("file") || BA.isShellModeRuntimeCheck(ba) == false)
//				mo.icon2(IconUri, OriginX, OriginY, AnchorX, AnchorY);
//		}
//		final Marker m = new Marker(mo);
//		map.addMarker(m);
//		if (ba.subExists(eventName + "_markerclick")) {
//			map.addUIEventHandler(m, UIEventType.click, new UIEventHandler() {
//				@Override
//				public void handle(JSObject obj) {
//					ba.raiseEventFromUI(GoogleMapWrapper.this, eventName + "_markerclick", 
//							(MarkerWrapper)AbsObjectWrapper.ConvertToWrapper(new MarkerWrapper(), m));
//				}
//			});
//		}
//		return (MarkerWrapper)AbsObjectWrapper.ConvertToWrapper(new MarkerWrapper(), m);
//	}

	/**
	 * Removes the specified marker from the map.
	 */
	public void RemoveMarker(MarkerWrapper Marker) {
		map.removeMarker(Marker.getObject());
	}

	//------------------------------------------- INFO WINDOW -------------------------------------------

	@ShortName("MapInfoWindow")
	public static class InfoWindowWrapper extends AbsObjectWrapper<InfoWindow>
	{
		/** Gets or sets the HTML content of this info window. */
		public String getContent()
		{
			return getObject().getContent();
		}
		public void setContent(String Content)
		{
			getObject().setContent(Content);
		}
		/** Gets or sets the position of this info window. */
		public LatLngWrapper getPosition()
		{
			return (LatLngWrapper)AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), getObject().getPosition());
		}
		public void setPosition(LatLngWrapper Position)
		{
			getObject().setPosition(Position.getObject());
		}
	}

	/** Opens an info window with the given HTML content at the specified position. */
	public InfoWindowWrapper AddInfoWindow(String Content, LatLngWrapper Position)
	{
		return AddInfoWindow2(Content, Position, -1);
	}
	/** Opens an info window with the given HTML content at the specified position. It cannot be larger than MaxWidth (in pixels). */
	public InfoWindowWrapper AddInfoWindow2(String Content, LatLngWrapper Position, int MaxWidth)
	{
		InfoWindowOptions io = new InfoWindowOptions();
		io.content(Content);
		if (MaxWidth > 0)
			io.maxWidth(MaxWidth);
		io.position(Position.getObject());
		InfoWindow Info = new InfoWindow(io);
		Info.open(map);
		return (InfoWindowWrapper)AbsObjectWrapper.ConvertToWrapper(new InfoWindowWrapper(), Info);
	}
	/** Opens an info window with the given HTML content above the specified marker. */
	public InfoWindowWrapper AddInfoWindowToMarker(String Content, MarkerWrapper Marker)
	{
		InfoWindowOptions io = new InfoWindowOptions();
		io.content(Content);
		InfoWindow Info = new InfoWindow(io);
		Info.open(map, Marker.getObject());
		return (InfoWindowWrapper)AbsObjectWrapper.ConvertToWrapper(new InfoWindowWrapper(), Info);
	}
	/** Closes the specified info window. */
	public void CloseInfoWindow(InfoWindowWrapper InfoWindow)
	{
		InfoWindow.getObject().close();
	}

	//------------------------------------------- SHAPES -------------------------------------------

	@ShortName("MapPolyline")
	public static class PolylineWrapper extends AbsObjectWrapper<Polyline>
	{
		/**
		 * Gets or sets whether this polyline is visible.
		 */
		public boolean getVisible() {
			return getObject().getVisible();
		}
		public void setVisible(boolean v) {
			getObject().setVisible(v);
		}
	}

	/**
	 * Adds a polyline to the map.
	 *Points - A list or array of LatLng points.
	 *Width - Line width.
	 *Color - Line color.
	 */
	public PolylineWrapper AddPolyline(List Points, float Width, Paint Color)
	{
		PolylineOptions po = new PolylineOptions();
		po.strokeWeight(Width);
		po.strokeColor(htmlColor(Color));
		po.path(new MVCArray(Points.getObject().toArray()));
		boolean b = ba.subExists(eventName + "_polylineclick");
		if (!b)
			po.clickable(false);
		Polyline line = new Polyline(po);
		map.addMapShape(line);
		if (b) {
			map.addUIEventHandler(line, UIEventType.click, new UIEventHandler() {
				@Override
				public void handle(JSObject obj) {
					ba.raiseEventFromUI(GoogleMapWrapper.this, eventName + "_polylineclick", 
							(PolylineWrapper)AbsObjectWrapper.ConvertToWrapper(new PolylineWrapper(), line));
				}
			});
		}
		return (PolylineWrapper)AbsObjectWrapper.ConvertToWrapper(new PolylineWrapper(), line);
	}
	/** Removes the specified polyline from the map. */
	public void RemovePolyline(PolylineWrapper Polyline) {
		map.removeMapShape(Polyline.getObject());
	}

	@ShortName("MapPolygon")
	public static class PolygonWrapper extends AbsObjectWrapper<Polygon>
	{
		/**
		 * Gets or sets whether this polygon is visible.
		 */
		public boolean getVisible() {
			return getObject().getVisible();
		}
		public void setVisible(boolean v) {
			getObject().setVisible(v);
		}
	}

	/**
	 * Adds a polygon to the map.
	 *Points - A list or array of LatLng points.
	 *StrokeWidth - Stroke width.
	 *StrokeColor - Stroke color.
	 *FillColor - Inner color.
	 *Opacity - Inner color opacity. Value between 0 to 1. 
	 */
	public PolygonWrapper AddPolygon(List Points, float StrokeWidth, Paint StrokeColor, Paint FillColor, double Opacity)
	{
		PolygonOptions po = new PolygonOptions();
		po.strokeWeight(StrokeWidth);
		po.fillColor(htmlColor(FillColor));
		po.strokeColor(htmlColor(StrokeColor));
		po.fillOpacity(Opacity);
		boolean eventExist = ba.subExists(eventName + "_polygonclick");
		if (eventExist == false)
			po.clickable(false);
		po.paths(new MVCArray(Points.getObject().toArray()));
		Polygon gon = new Polygon(po);
		map.addMapShape(gon);
		if (eventExist) {
			map.addUIEventHandler(gon, UIEventType.click, new UIEventHandler() {
				@Override
				public void handle(JSObject obj) {
					ba.raiseEventFromUI(GoogleMapWrapper.this, eventName + "_polygonclick", 
							(PolygonWrapper)AbsObjectWrapper.ConvertToWrapper(new PolygonWrapper(), gon));
				}
			});
		}
		return (PolygonWrapper)AbsObjectWrapper.ConvertToWrapper(new PolygonWrapper(), gon);

	}
	/** Removes the specified polygon from the map. */
	public void RemovePolygon(PolygonWrapper Polygon) {
		map.removeMapShape(Polygon.getObject());
	}

	@ShortName("MapCircle")
	public static class CircleWrapper extends AbsObjectWrapper<Circle>
	{
		/**
		 * Gets or sets whether this circle is visible.
		 */
		public boolean getVisible() {
			return getObject().getVisible();
		}
		public void setVisible(boolean v) {
			getObject().setVisible(v);
		}
		/**
		 * Gets the circle bounds.
		 */
		public LatLongBoundsWrapper getBounds() {
			return (LatLongBoundsWrapper)AbsObjectWrapper.ConvertToWrapper(new LatLongBoundsWrapper(), getObject().getBounds());
		}
		/**
		 * Gets or sets the circle center.
		 */
		public LatLngWrapper getCenter() {
			return (LatLngWrapper)AbsObjectWrapper.ConvertToWrapper(new LatLngWrapper(), getObject().getCenter());
		}
		public void setCenter(LatLngWrapper Center) {
			getObject().setCenter(Center.getObject());
		}
		/**
		 * Gets or sets the circle radius.
		 */
		public double getRadius() {
			return getObject().getRadius();
		}
		public void setRadius(double Radius) {
			getObject().setRadius(Radius);
		}
	}

	/**
	 * Adds a circle to the map.
	 *Center - Position of the circle center.
	 *Radius - Circle radius.
	 *StrokeWidth - Stroke width.
	 *StrokeColor - Stroke color.
	 *FillColor - Inner color.
	 *Opacity - Inner color opacity. Value between 0 to 1. 
	 */
	public CircleWrapper AddCircle(LatLngWrapper Center, double Radius, float StrokeWidth, Paint StrokeColor, Paint FillColor, double Opacity)
	{
		CircleOptions co = new CircleOptions();
		co.center(Center.getObject());
		co.radius(Radius);
		co.strokeWeight(StrokeWidth);
		co.fillColor(htmlColor(FillColor));
		co.strokeColor(htmlColor(StrokeColor));
		co.fillOpacity(Opacity);
		boolean b = ba.subExists(eventName + "_circleclick");
		if (!b)
			co.clickable(false);
		Circle circle = new Circle(co);
		map.addMapShape(circle);
		if (b) {
			map.addUIEventHandler(circle, UIEventType.click, new UIEventHandler() {
				@Override
				public void handle(JSObject obj) {
					ba.raiseEventFromUI(GoogleMapWrapper.this, eventName + "_circleclick", 
							(CircleWrapper)AbsObjectWrapper.ConvertToWrapper(new CircleWrapper(), circle));
				}
			});
		}
		return (CircleWrapper)AbsObjectWrapper.ConvertToWrapper(new CircleWrapper(), circle);
	}
	/** Removes the specified circle from the map. */
	public void RemoveCircle(CircleWrapper Circle)
	{
		map.removeMapShape(Circle.getObject());
	}

	//------------------------------------------- MISCELLANEOUS -------------------------------------------

	@Hide
	public String htmlColor(Paint Color) {
		return String.format("#%06X", (0xFFFFFF & Colors.To32Bit(Color)));
	}

	public static final MapTypeIdEnum MAP_TYPE_NORMAL = MapTypeIdEnum.ROADMAP;
	public static final MapTypeIdEnum MAP_TYPE_SATELLITE = MapTypeIdEnum.SATELLITE;
	public static final MapTypeIdEnum MAP_TYPE_TERRAIN = MapTypeIdEnum.TERRAIN;
	public static final MapTypeIdEnum MAP_TYPE_HYBRID = MapTypeIdEnum.HYBRID;
	/**
	 * Sets the map type. The value should be one of the MAP_TYPE constants. 
	 */
	public void setMapType(Object v) {
		map.setMapType((MapTypeIdEnum) v);
	}

	/**
	 * Returns the pane that holds the map.
	 */
	public ConcretePaneWrapper getAsPane() {
		return (ConcretePaneWrapper) AbsObjectWrapper.ConvertToWrapper(new ConcretePaneWrapper(), mapView);
	}
}
