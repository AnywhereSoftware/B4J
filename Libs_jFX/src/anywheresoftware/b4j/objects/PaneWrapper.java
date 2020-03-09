
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
import java.util.LinkedList;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Control;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.IterableList;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4j.objects.LayoutBuilder.LayoutData;

@Hide
@Events(values={"Resize (Width As Double, Height As Double)", "Touch (Action As Int, X As Float, Y As Float)"})

public class PaneWrapper<T extends Pane> extends NodeWrapper<T> implements IterableList{
	public static Object ORIENTATION_RIGHT_TO_LEFT = NodeOrientation.RIGHT_TO_LEFT;
	public static Object ORIENTATION_LEFT_TO_RIGHT = NodeOrientation.LEFT_TO_RIGHT;
	public static Object ORIENTATION_INHERIT = NodeOrientation.INHERIT;
	@Hide
	public final static LinkedList<NativeAndWrapper> nativeToWrapper = new LinkedList<NativeAndWrapper>();
	@Hide
	public static class NativeAndWrapper {
		public final Class<?> nativeClass;
		public final Class<?> wrapperClass;

		public NativeAndWrapper(Class<?> nativeClass, Class<?> wrapperClass) {
			this.nativeClass = nativeClass;
			this.wrapperClass = wrapperClass;
		}

	}

	/**
	 * Loads a layout file.
	 */
	@RaisesSynchronousEvents
	public LayoutValues LoadLayout(BA tba, String LayoutFile) throws Exception {
		String filelower = LayoutFile.toLowerCase(BA.cul);
		boolean fxml = true;
		if (filelower.contains(".") == false) {
			if (getClass().getResourceAsStream("/Files/" + LayoutFile + ".fxml") != null)
				LayoutFile += ".fxml";
			else {
				LayoutFile += ".bjl";
				fxml = false;
			}

		} else {
			if (filelower.endsWith(".bjl"))
				fxml = false;
		}
		if (fxml) {
			FXMLBuilder.LoadLayout(this.getObject(), tba, LayoutFile);
			return new LayoutValues();
		}
		else {
			LayoutBuilder lb = new LayoutBuilder(null);
			return lb.loadLayout(LayoutFile, tba, getObject());
		}

	}
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		super.innerInitialize(ba, eventName, keepOldObject);
		final Pane obj = getObject();
		final boolean raiseEvent = ba.subExists(eventName + "_resize");
		new ResizeEventManager(obj, null, new Runnable() {

			@Override
			public void run() {
				LayoutData ld = (LayoutData) AbsObjectWrapper.getExtraTags(obj).get("layoutdata");
				if (ld != null) {
					
					new LayoutBuilder(ld).resizeLayout(ba, obj);
				}
				if (raiseEvent) {
					ba.raiseEvent(obj, eventName + "_resize", obj.getWidth(), obj.getHeight());
				}
			}
		});
		if (ba.subExists(eventName + "_touch")) {
			getObject().setOnMousePressed(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(obj, eventName + "_touch", 0, (float)arg0.getX(), (float)arg0.getY());
				}

			});
			getObject().setOnMouseReleased(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(obj, eventName + "_touch", 1, (float)arg0.getX(), (float)arg0.getY());
				}

			});
			getObject().setOnMouseDragged(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(obj, eventName + "_touch", 2, (float)arg0.getX(), (float)arg0.getY());
				}

			});
			getObject().setOnMouseMoved(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					ba.raiseEvent(obj, eventName + "_touch", 100, (float)arg0.getX(), (float)arg0.getY());
				}

			});
		}
	}
	@Hide
	public static class ResizeEventManager {
		private double lastWidth, lastHeight;

		public ResizeEventManager(final Pane pane, final Control control, final Runnable eventHandler) {

			final Runnable r = new Runnable() {

				@Override
				public void run() {
					double w = pane != null ? pane.getWidth() : control.getWidth();
					double h = pane != null ? pane.getHeight() : control.getHeight();
					if (w != lastWidth || h != lastHeight) {
						lastWidth = w;
						lastHeight = h;
						if (w != 0 && h != 0) {
							eventHandler.run();
						}
					}
				}
			};
			ReadOnlyDoubleProperty widthProp = pane != null ? pane.widthProperty() : control.widthProperty();
			widthProp.addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2) {
					if (pane != null && pane.getScene() != null && pane.getScene().getRoot() != pane)
						r.run();
					else
						Platform.runLater(r);
				}
			});
			ReadOnlyDoubleProperty heightProp = pane != null ? pane.heightProperty() : control.heightProperty();
			heightProp.addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2) {
					if (pane != null && pane.getScene() != null && pane.getScene().getRoot() != pane)
						r.run();
					else
						Platform.runLater(r);
				}
			});

		}
	}
	/**
	 * Returns an iterator that iterates over all the child nodes including nodes that were added to other child nodes.
	 *Example:<code>
	 *For Each n As Node In MainForm.RootPane.GetAllViewsRecursive
	 *
	 *Next
	 *</code>
	 */
	public IterableList GetAllViewsRecursive() {
		return new AllViewsIterator(this.getObject());
	}

	@Hide
	public static class AllViewsIterator implements IterableList {
		private ArrayList<Node> views = new ArrayList<Node>();
		public AllViewsIterator(Parent parent) {
			addViews(parent);
		}
		private void addViews(Parent parent) {
			for (Node n : parent.getChildrenUnmodifiable()) {
				views.add(n);
				if (n instanceof Parent)
					addViews((Parent) n);
			}
		}
		@Override
		public Object Get(int index) {
			return views.get(index);
		}

		@Override
		public int getSize() {
			return views.size();
		}

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
		KeyFrame frame = new KeyFrame(javafx.util.Duration.millis(Duration), left, top, width, height);
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
		SetLayoutAnimatedImpl(Duration, Left, Top, PrefWidth, PrefHeight, true);
	}
	/**
	 * Gets or sets the pane preferred height.
	 */
	public double getPrefHeight() {
		return getObject().getPrefHeight();
	}
	public void setPrefHeight(double d) {
		getObject().setPrefHeight(d);
	}
	/**
	 * Gets or sets the pane preferred width.
	 */
	public double getPrefWidth() {
		return getObject().getPrefWidth();
	}
	public void setPrefWidth(double d) {
		getObject().setPrefWidth(d);
	}
	/**
	 * Gets the pane height. This value will only be available after the node's parent has finished measuring and drawing the node.
	 *Consider using PrefHeight instead.
	 */
	public double getHeight() {
		return getObject().getHeight();
	}
	/**
	 * Gets the pane width. This value will only be available after the node's parent has finished measuring and drawing the node.
	 *Consider using PrefWidth instead.
	 */
	public double getWidth() {
		return getObject().getWidth();
	}
	/**
	 * Adds a Node to this pane.
	 *Node - The node to add.
	 *Left - The x coordinate of the top-left corner.
	 *Top - The y coordinate of the top-left corner.
	 *Width - The node's width. Setting this value to -1 means that the size will be calculated automatically.
	 *Height - The node's height. Setting this value to -1 means that the size will be calculated automatically.
	 */
	public void AddNode(Node Node, double Left, double Top, double Width, double Height) {
		InsertNode(getNumberOfNodes(), Node, Left, Top, Width, Height);

	}
	/**
	 * Removes all child nodes.
	 */
	public void RemoveAllNodes() {
		getObject().getChildren().clear();
	}
	/**
	 * Removes the node at the specified index.
	 */
	public void RemoveNodeAt(int Index) {
		getObject().getChildren().remove(Index);
	}
	/**
	 * Inserts a node at the specified index.
	 */
	public void InsertNode(int Index, Node Node, double Left, double Top, double Width, double Height) {
		getObject().getChildren().add(Index, Node);
		if (Left != -1)
			Node.setLayoutX(Left - Node.getLayoutBounds().getMinX());
		if (Top != -1)
			Node.setLayoutY(Top - Node.getLayoutBounds().getMinY());
		if (Node instanceof MenuBar) {
			MenuBar mb = (MenuBar)Node;
			AnchorPane.setLeftAnchor(mb, 0d);
			AnchorPane.setRightAnchor(mb, 0d);
		}
		else if (Node instanceof Control) {
			((Control)Node).setPrefSize(Width > 0 ? Width : Control.USE_COMPUTED_SIZE,
					Height > 0 ? Height : Control.USE_COMPUTED_SIZE);
		} else if (Node instanceof Region){
			((Region)Node).setPrefSize(Width > 0 ? Width : Control.USE_COMPUTED_SIZE,
					Height > 0 ? Height : Control.USE_COMPUTED_SIZE);
		} else if (Node instanceof ImageView) {
			((ImageView)Node).setFitWidth(Width);
			((ImageView)Node).setFitHeight(Height);
		}
		else if (Node instanceof Canvas) {
			((Canvas)Node).setWidth(Width);
			((Canvas)Node).setHeight(Height);
		}
	}
	/**
	 * Gets or sets the pane orientation (left to right or right to left).
	 *The default value is ORIENTATION_INHERIT which means that it inherits the orientation of its parent.
	 */
	public Object getOrientation() {
		return getObject().getNodeOrientation();
	}
	public void setOrientation(Object o) {
		getObject().setNodeOrientation((NodeOrientation)o);
	}
	/**
	 * Gets the number of child nodes.
	 */
	public int getNumberOfNodes() {
		return getObject().getChildren().size();
	}
	/**
	 * Returns the node at the given index.
	 */
	public ConcreteNodeWrapper GetNode(int Index) {
		return (ConcreteNodeWrapper)AbsObjectWrapper.ConvertToWrapper(new ConcreteNodeWrapper(),
				getObject().getChildren().get(Index));
	}
	@Override
	@Hide
	public int getSize() {
		return getNumberOfNodes();
	}
	@Hide
	@Override
	public Object Get(int index) {
		return getObject().getChildren().get(index);
	}
	@Hide
	public static int[] getDesignerWidthAndHeight(Pane pane) {
		int w = (int)pane.getPrefWidth(), h = (int)pane.getPrefHeight();
		if (w < 0 || h < 0)
		{
			w = (int)pane.getWidth();
			h = (int)pane.getHeight();
		}
		return new int[] {w, h};
	}
	/**
	 * Pane is an object that can hold any type of Pane.
	 */
	@ShortName("Pane")
	public static class ConcretePaneWrapper extends PaneWrapper<Pane> {
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject) {
				setObject(new NonResizePane());
			}
			super.innerInitialize(ba, eventName, true);


		} 

		@Hide
		public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
			Pane vg = (Pane) prev;
			if (vg == null) {
				vg = NodeWrapper.buildNativeView(NonResizePane.class, props, designer);
				final Rectangle clipRectangle = new Rectangle();
				vg.setClip(clipRectangle);
				vg.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {

					@Override
					public void changed(
							ObservableValue<? extends Bounds> arg0,
							Bounds arg1, Bounds arg2) {
						clipRectangle.setWidth(arg2.getWidth());
						clipRectangle.setHeight(arg2.getHeight());
					}
				});
			}
			vg = (Pane) NodeWrapper.build(vg, props, designer);
			vg.prefWidth((Integer) props.get("width"));
			vg.prefHeight((Integer) props.get("height"));
			setRTLOrientation(vg, props);
			return vg;
		}
		@Hide
		public static void setRTLOrientation(Node pane, Map<String, Object> props) {
			String orientation = BA.gm(props, "orientation", "INHERIT");
			pane.setNodeOrientation(BA.getEnumFromString(NodeOrientation.class, orientation));
		}
		@Hide
		public static class NonResizePane extends Pane {
			protected double computePrefHeight(double width) {
				return getPrefWidth();
			}
			protected double computePrefWidth(double width) {
				return getPrefWidth();
			}
		}
	}
	/**
	 * A pane with an anchoring feature. Child views can be anchored to one or more of the pane boundaries.
	 *The anchored distances will not change when the parent is resized.
	 */
	@ShortName("AnchorPane")
	public static class AnchorPaneWrapper extends PaneWrapper<AnchorPane> {
		@Override
		@Hide
		public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
			if (!keepOldObject)
				setObject(new AnchorPane());
			super.innerInitialize(ba, eventName, true);
		} 
		/**
		 * Sets the child anchors. A value of -1 means that an anchor will not be set.
		 */
		public void SetAnchors(Node Child, double Left, double Top, double Right, double Bottom) {
			checkResize(Child);
			SetLeftAnchor(Child, Left);
			SetTopAnchor(Child, Top);
			SetRightAnchor(Child, Right);
			SetBottomAnchor(Child, Bottom);

		}
		/**
		 * A utility method that anchors the child node left and right distances.
		 */
		public void FillHorizontally(Node Child, double DistanceFromLeft, double DistanceFromRight) {
			checkResize(Child);
			SetLeftAnchor(Child, DistanceFromLeft);
			SetRightAnchor(Child, DistanceFromRight);
		}
		/**
		 * A utility method that anchors the child node top and bottom distances.
		 */
		public void FillVertically(Node Child, double DistanceFromTop, double DistanceFromBottom) {
			checkResize(Child);
			SetTopAnchor(Child, DistanceFromTop);
			SetBottomAnchor(Child, DistanceFromBottom);
		}
		/**
		 * Gets the left anchor constraint or -1 if not set.
		 */
		public double GetLeftAnchor(Node Child) {
			Double d = AnchorPane.getLeftAnchor(Child);
			return d == null ? -1 : d;
		}
		/**
		 * Gets the top anchor constraint or -1 if not set.
		 */
		public double GetTopAnchor(Node Child) {
			Double d = AnchorPane.getTopAnchor(Child);
			return d == null ? -1 : d;
		}
		/**
		 * Gets the bottom anchor constraint or -1 if not set.
		 */
		public double GetBottomAnchor(Node Child) {
			Double d = AnchorPane.getBottomAnchor(Child);
			return d == null ? -1 : d;
		}
		/**
		 * Gets the right anchor constraint or -1 if not set.
		 */
		public double GetRightAnchor(Node Child) {
			Double d = AnchorPane.getRightAnchor(Child);
			return d == null ? -1 : d;
		}
		private void checkResize(Node Child) {
			if (Child.isResizable() == false)
				BA.Log("Child is not resizable: " + Child);
		}
		/**
		 * Sets the left anchor constraint (distance from the node left edge to the container edge). Pass -1 to clear this constraint.
		 */
		public void SetLeftAnchor(Node Child, double Offset) {
			checkResize(Child);
			AnchorPane.setLeftAnchor(Child, Offset == -1 ? null : Offset);
		}
		/**
		 * Sets the top anchor constraint (distance from the node top edge to the container edge). Pass -1 to clear this constraint.
		 */
		public void SetTopAnchor(Node Child, double Offset) {
			checkResize(Child);

			AnchorPane.setTopAnchor(Child, Offset == -1 ? null : Offset);
		}
		/**
		 * Sets the bottom anchor constraint (distance from the node bottom edge to the container edge). Pass -1 to clear this constraint.
		 */
		public void SetBottomAnchor(Node Child, double Offset) {
			checkResize(Child);

			AnchorPane.setBottomAnchor(Child, Offset == -1 ? null : Offset);
		}
		/**
		 * Sets the right anchor constraint (distance from the node right edge to the container edge). Pass -1 to clear this constraint.
		 */
		public void SetRightAnchor(Node Child, double Offset) {
			checkResize(Child);

			AnchorPane.setRightAnchor(Child, Offset == -1 ? null : Offset);
		}




	}


}
