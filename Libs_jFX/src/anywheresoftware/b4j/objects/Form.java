
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

import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.ImageViewWrapper.ImageWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.AnchorPaneWrapper;
import anywheresoftware.b4j.objects.PaneWrapper.ConcretePaneWrapper;

/**
 * A form or window. Form.RootPane returns the root container. You can add or remove nodes from this container.
 *Forms can be either modal or non-modal.
 *The main form 'EventName' is MainForm.
 */
@ShortName("Form")
@Events(values={"CloseRequest (EventData As Event)" , "Closed",
		"FocusChanged (HasFocus As Boolean)",
"IconifiedChanged (Iconified As Boolean)"})
public class Form {
	@Hide
	public Stage stage;
	@Hide
	public Scene scene;
	private AnchorPaneWrapper pane;


	@Hide
	public void initWithStage(BA ba, Stage stage, double w, double h) {
		this.stage = stage;
		shared(ba, "mainform", w, h);
	}
	/**
	 * Initializes the form and set the subs that will handle the form events (including the RootPane events).
	 *Pass -1 to the Width or Height if you want the form size to be calculated automatically based on its contents.
	 */
	public void Initialize(BA ba, String EventName, double Width, double Height) {
		stage = new Stage();
		shared(ba, EventName.toLowerCase(BA.cul), Width, Height);
	}
	private void shared(final BA ba, final String EventName, double w, double h) {
		scene = new Scene(new AnchorPane(), w, h);
		stage.setScene(scene);
		if (ba.subExists(EventName + "_closerequest")) {
			scene.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent ev) {
					ba.raiseEvent(Form.this, EventName + "_closerequest", 
							AbsObjectWrapper.ConvertToWrapper(new NodeWrapper.ConcreteEventWrapper(), ev));
				}
			});
		}
		if (ba.subExists(EventName + "_closed")) {
			scene.getWindow().setOnHidden(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent arg0) {
					ba.raiseEventFromUI(Form.this, EventName + "_closed");
				}
			});
		}
		if (ba.subExists(EventName + "_focuschanged")) {
			stage.focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> arg0,
						Boolean arg1, Boolean arg2) {
					ba.raiseEventFromUI(Form.this, EventName + "_focuschanged", arg2.booleanValue());
				}
			});
		}
		if (ba.subExists(EventName + "_iconifiedchanged")) {
			stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> arg0,
						Boolean arg1, Boolean arg2) {
					ba.raiseEventFromUI(Form.this, EventName + "_iconifiedchanged", arg2.booleanValue());
				}
			});
		}



		pane = new AnchorPaneWrapper();
		pane.setObject(new AnchorPane());
		pane.innerInitialize(ba, EventName, true);
		AbsObjectWrapper.getExtraTags(pane.getObject()).put("form", this);
		scene.setRoot(pane.getObject());
	}
	/**
	 * Tests whether the Form was initialized.
	 */
	public boolean IsInitialized() {
		return stage != null;
	}
	/**
	 * Returns true if the form is showing (open).
	 */
	public boolean getShowing() {
		return stage.isShowing();
	}
	/**
	 * Gets or sets the window width.
	 */
	public double getWindowWidth() {
		return stage.getWidth();
	}
	/**
	 * Gets or sets the window height.
	 */
	public double getWindowHeight() {
		return stage.getHeight();
	}
	/**
	 * Gets or sets the window left position.
	 */
	public double getWindowLeft() {
		return stage.getX();
	}
	/**
	 * Gets or sets the window top position.
	 */
	public double getWindowTop() {
		return stage.getY();
	}
	public void setWindowWidth(double d) {
		stage.setWidth(d);
	}
	public void setWindowHeight(double d) {
		stage.setHeight(d);
	}
	public void setWindowLeft(double d) {
		stage.setX(d);
	}
	public void setWindowTop(double d) {
		stage.setY(d);
	}

	/**
	 * Sets the window maximum and minimum dimensions.
	 */
	public void SetWindowSizeLimits (double MinWidth, double MinHeight, double MaxWidth, double MaxHeight) {
		stage.setMinWidth(MinWidth);
		stage.setMinHeight(MinHeight);
		stage.setMaxWidth(MaxWidth);
		stage.setMaxHeight(MaxHeight);

	}
	/**
	 * Gets or sets the form background fill.
	 *Example:<code>
	 *frm.BackColor = fx.Colors.White</code>
	 */
	public Paint getBackColor() {
		return scene.getFill();
	}
	public void setBackColor(Paint p) {
		scene.setFill(p);
	}
	/**
	 * Gets or sets the form title.
	 */
	public String getTitle() {
		return stage.getTitle();
	}
	public void setTitle(String s) {
		stage.setTitle(s);
	}
	/**
	 * Closes the form.
	 */
	public void Close() {
		stage.close();
	}
	/**
	 * Return the form root container, which is an AnchorPane.
	 */
	public AnchorPaneWrapper getRootPane() {
		return pane;
	}
	/**
	 * Shows the form.
	 */
	@RaisesSynchronousEvents
	public void Show() {
		stage.show();
	}
	/**
	 * Shows the form as a modal form. The current code execution will wait until the form is closed.
	 *Note that you cannot call this method if the form was shown before as a non-modal form. 
	 */
	@RaisesSynchronousEvents
	public void ShowAndWait() {
		if (stage.getModality() == Modality.NONE)
			stage.initModality(Modality.APPLICATION_MODAL);
		stage.showAndWait();
	}
	/**
	 * Sets the form owner. This method should be called before the form is shown.
	 *A form with an owner will appear above its owner.
	 */
	public void SetOwner(Form Owner) {
		stage.initOwner(Owner.stage);
	}
	/**
	 * Gets the form internal width.
	 */
	public double getWidth() {
		return scene.getWidth();
	}
	/**
	 * Gets the form internal height (without the title bar).
	 */
	public double getHeight() {
		return scene.getHeight();
	}
	/**
	 * Gets or sets whether this form is resizable by the user.
	 */
	public boolean getResizable() {
		return stage.isResizable();
	}
	public void setResizable(boolean b) {
		stage.setResizable(b);
	}
	/**
	 * Returns true if the stage is visible.
	 */
	public boolean getVisible() {
		return stage.isShowing();
	}
	/**
	 * Sets the form style. 
	 *The possible values are: 
	 *DECORATED - Default style.
	 *UNDECORATED - Window without decorations.
	 *TRANSPARENT - Transparent window without decorations. 
	 *UTILITY - Window with minimal decorations.
	 */
	public void SetFormStyle(String Style) {
		if (Style.equals("UNIFIED"))
			Style = "DECORATED";
		stage.initStyle(StageStyle.valueOf(Style));
	}
	/**
	 * Gets or sets the form icon.
	 *Example:<code>
	 *MainForm.Icon = fx.LoadImage(File.DirAssets, "image.png")</code>
	 */
	public void setIcon(Image Image) {
		stage.getIcons().clear();
		stage.getIcons().add(Image);
	}
	public ImageWrapper getIcon() {
		ImageWrapper iw = new ImageWrapper();
		if (stage.getIcons().size() > 0)
			iw.setObject(stage.getIcons().get(0));
		return iw;
	}
	/**
	 * Gets or sets whether the form will be kept above all other windows.
	 */
	public void setAlwaysOnTop(boolean b) {
		stage.setAlwaysOnTop(b);
	}
	public boolean getAlwaysOnTop() {
		return stage.isAlwaysOnTop();
	}
	/**
	 * Returns a List with the stylesheets files attached to this form.
	 */
	public List getStylesheets() {
		return (List)AbsObjectWrapper.ConvertToWrapper(new List(), scene.getStylesheets());
	}
	@Hide
	public static Form getFormFromNode(Node n) {
		if (n.getScene() == null || n.getScene().getRoot() == null)
			return null;
		return (Form) (AbsObjectWrapper.getExtraTags(n.getScene().getRoot())).get("form");
	}
	@Hide
	public static Node build(Object prev, Map<String, Object> props, boolean designer) throws Exception{
		PaneWrapper.build(prev, props, designer);
		ConcretePaneWrapper.setRTLOrientation((Node)prev, props);
		Form f = (Form) AbsObjectWrapper.getExtraTags(prev).get("form");
		if (f != null) {
			f.setTitle((String)props.get("title"));
			String uri = NodeWrapper.getImageUri((String)props.get("file"), designer);
			if (uri != null)
				f.setIcon(new Image(uri));
		}
		return (Node) prev;
	}

}
