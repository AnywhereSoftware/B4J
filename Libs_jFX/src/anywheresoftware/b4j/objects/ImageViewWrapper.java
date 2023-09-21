
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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.streams.File;

/**
 * A node that displays an image.
 */
@ShortName("ImageView")
public class ImageViewWrapper extends NodeWrapper<ImageView>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new ImageView());
		super.innerInitialize(ba, eventName, true);
	}
	/**
	 * Gets or sets the ImageView width.
	 */
	public double getWidth() {
		return getObject().getFitWidth();
	}
	/**
	 * Gets or sets the ImageView height.
	 */
	public double getHeight() {
		return getObject().getFitHeight();
	}
	public void setWidth(double d) {
		getObject().setFitWidth(d);
	}
	public void setHeight(double d) {
		getObject().setFitHeight(d);
	}
	/**
	 * Changes the node Top, Left, Width and Height properties with an animation effect.
	 * Duration - Animation duration in milliseconds.
	 */
	public void SetLayoutAnimated(int Duration, double Left, double Top, double Width, double Height) {
		if (Duration == 0) {
			setTop(Top);
			setLeft(Left);
			setWidth(Width);
			setHeight(Height);
			raiseAnimationCompletedEvent(null, "SetLayoutAnimated");
			return;
		}
		KeyValue left = new KeyValue(getObject().layoutXProperty(), Left - getObject().getLayoutBounds().getMinX());
		KeyValue top = new KeyValue(getObject().layoutYProperty(), Top - getObject().getLayoutBounds().getMinY());
		KeyValue width = new KeyValue(getObject().fitWidthProperty(), Width);
		KeyValue height = new KeyValue(getObject().fitHeightProperty(), Height);
		KeyFrame frame = new KeyFrame(javafx.util.Duration.millis(Duration), left, top, width, height);
		Timeline timeline = new Timeline(frame);
		raiseAnimationCompletedEvent(timeline, "SetLayoutAnimated");
		timeline.play();
	}
	/**
	 * Gets or sets whether ImageView should preserve the image ratio.
	 */
	public boolean getPreserveRatio() {
		return getObject().isPreserveRatio();
	}
	public void setPreserveRatio(boolean b) {
		getObject().setPreserveRatio(b);
	}
	/**
	 * Gets the image.
	 */
	public ImageWrapper GetImage() {
		return (ImageWrapper)AbsObjectWrapper.ConvertToWrapper(new ImageWrapper(), getObject().getImage());
	}
	/**
	 * Sets the image.
	 */
	public void SetImage(Image Image) {
		getObject().setImage(Image);
	}
	
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		Node vg = (Node) prev;
		if (vg == null) 
			vg = NodeWrapper.buildNativeView(ImageView.class, props, designer);
		ImageView pb = (ImageView) NodeWrapper.build(vg, props, designer);
		pb.setPreserveRatio((Boolean)props.get("preserveRatio"));
		return pb;
	}
	
	/**
	 * Image represents a graphical image.
	 */
	@ShortName("Image")
	public static class ImageWrapper extends AbsObjectWrapper<Image> {
		/**
		 * Loads the image from the given directory.
		 */
		public void Initialize(String Dir, String FileName) throws IOException {
			try (InputStream in = File.OpenInput(Dir, FileName).getObject()) {
				Initialize2(in);
			}
		}
		/**
		 * Loads the image from the given InputStream.
		 */
		public void Initialize2(InputStream In) {
			Image img = new Image(In);
			setObject(img);
		}
		/**
		 * Loads a resized image.
		 */
		public void InitializeSample(String Dir, String FileName, double Width, double Height) throws IOException {
			try (InputStream in = File.OpenInput(Dir, FileName).getObject()) {
				Image img = new Image(in, Width, Height, false, true);
				setObject(img);
			}
		}
		/**
		 * Loads a resized image from the given InputStream.
		 */
		public void InitializeSample2(InputStream In, double Width, double Height) throws IOException {
			Image img = new Image(In, Width, Height, false, true);
			setObject(img);
		}
		/**
		 * Returns the image width.
		 */
		public double getWidth() {
			return getObject().getWidth();
		}
		/**
		 * Returns the image height.
		 */
		public double getHeight() {
			return getObject().getHeight();
		}
		public int GetPixel(int x, int y) {
			return getObject().getPixelReader().getArgb(x, y);
		}
		/**
		 * Writes the image to the OutputStream formatted as a PNG image.
		 *The OutputStream will not be closed automatically.
		 *Example:<code>Dim Out As OutputStream = File.OpenOutput(File.DirApp, "1.png")
		 *Image1.WriteToStream(Out)
		 *Out.Close</code>
		 */
		public void WriteToStream(OutputStream Out) throws IOException {
			String ImageFormat = "png";
			Iterator<ImageWriter> iiw = ImageIO.getImageWritersByFormatName(ImageFormat);
			if (iiw.hasNext() == false)
				throw new RuntimeException("No writer found for: " + ImageFormat);
			ImageWriter iw = iiw.next();
			ImageWriteParam param = iw.getDefaultWriteParam();
			//			if (Quality >= 0 && param.canWriteCompressed()) {
			//				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			//				param.setCompressionQuality(Quality);
			//			}
			iw.setOutput(ImageIO.createImageOutputStream(Out));
			iw.write(null, new IIOImage(SwingFXUtils.fromFXImage(getObject(), null), null, null),
					param);
			iw.dispose();

		}
	}
}
