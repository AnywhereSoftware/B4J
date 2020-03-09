
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

import java.util.HashMap;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4j.objects.JFX.Colors;

/**
 * A special node that can be drawn on.
 *The Canvas node will not be resized automatically when its parent is resized.
 */
@ShortName("Canvas")
public class CanvasWrapper extends NodeWrapper<javafx.scene.canvas.Canvas>{
	@Override
	@Hide
	public void innerInitialize(final BA ba, final String eventName, boolean keepOldObject) {
		if (!keepOldObject)
			setObject(new Canvas());
		super.innerInitialize(ba, eventName, true);
	}
	/**
	 * Gets or sets the canvas height.
	 */
	public double getHeight() {
		return getObject().getHeight();
	}
	/**
	 * Gets or sets the canvas width.
	 */
	public double getWidth() {
		return getObject().getWidth();
	}
	public void setHeight(double d) {
		getObject().setHeight(d);
	}
	public void setWidth(double d) {
		getObject().setWidth(d);
	}
	/**
	 * Clips the drawings to a closed path.
	 *Points - A list with the path points. Each item in the list should be an array of doubles with the X and Y coordinates.
	 *For example:<code>
	 *Dim Path As List
	 *Path.Initialize
	 *Path.Add(Array As Double(100, 100))
	 *Path.Add(Array As Double(100, 200))
	 *Path.Add(Array As Double(200, 200))
	 *cvs.ClipPath(Path)</code>
	 */
	public void ClipPath(List Points) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.save();
		gc.beginPath();
		double[] p = (double[])Points.Get(0);
		gc.moveTo(p[0], p[1]);
		for (int i = 1;i < Points.getSize();i++) {
			double[] p2 = (double[])Points.Get(i);
			gc.lineTo(p2[0], p2[1]);
		}
		gc.closePath();
		gc.clip();
	}
	/**
	 * Removes a previously set clip region.
	 */
	public void RemoveClip() {
		getObject().getGraphicsContext2D().restore();
	}
	/**
	 * Draws an image on the canvas.
	 *Image - The image that will be drawn.
	 *x - The top left corner x coordinate.
	 *y - The top left corner y coordinate.
	 *Width - The width of the destination rectangle.
	 *Height - The height of the destination rectangle.
	 *Example:<code>
	 *cvs.DrawImage(image1, 10dip, 10dip, image1.Width, image1.Height)</code>
	 */
	public void DrawImage(Image Image, double x, double y, double Width, double Height) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.drawImage(Image, x, y, Width, Height);
	}
	/**
	 * Similar to DrawImage. Rotates the image before it is drawn.
	 *Degree - Angle of rotation (measured in degrees).
	 */
	public void DrawImageRotated(Image Image, double x, double y, double Width, double Height, double Degree) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.save();
		try {
			rotate(gc, Degree, x + Width / 2, y + Height / 2);
			DrawImage(Image, x, y, Width, Height);
		} finally {
			gc.restore();
		}
	}
	/**
	 * Similar to DrawImage. Allows you to set both the source rectangle dimensions (in the image) and the destination rectangle dimensions. 
	 */
	public void DrawImage2(Image Image, double SourceX, double SourceY, double SourceWidth, double SourceHeight,
			double DestX, double DestY, double DestWidth, double DestHeight) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.drawImage(Image, SourceX, SourceY, SourceWidth, SourceHeight, DestX, DestY, DestWidth, DestHeight);
	}
	/**
	 * Draws a circle.
	 *x - The center X coordinate.
	 *y - The center Y coordinate.
	 *Radius - The circle radius.
	 *Paint - The circle fill or stroke paint.
	 *Filled - Whether the circle will be filled or not.
	 *StrokeWidth - The circle stroke width (when Filled is False).
	 */
	public void DrawCircle(double x, double y, double Radius, Paint Paint, boolean Filled, double StrokeWidth) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.save();
		try {
			if (Filled) {
				gc.setFill(Paint);
				gc.fillOval(x - Radius, y - Radius, 2 * Radius, 2 * Radius);
			} else {
				gc.setStroke(Paint);
				gc.setLineWidth(StrokeWidth);
				gc.strokeOval(x - Radius, y - Radius, 2 * Radius, 2 * Radius);
			}
		} finally {
			gc.restore();
		}
	}
	/**
	 * Draws a rectangle.
	 *x - Top left corner X coordinate.
	 *y - Top left corner Y coordinate.
	 *Width - The rectangle width.
	 *Height - The rectangle height.
	 *Paint - The rectangle fill or stroke paint.
	 *Filled - Whether the rectangle should be filled.
	 *StrokeWidth - The rectangle stroke (when Filled is False).
	 */
	public void DrawRect(double x, double y, double Width, double Height, Paint Paint, boolean Filled, double StrokeWidth) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.save();
		try {
			if (Filled) {
				gc.setFill(Paint);
				gc.fillRect(x, y, Width, Height);
			} else {
				gc.setStroke(Paint);
				gc.setLineWidth(StrokeWidth);
				gc.strokeRect(x, y, Width, Height);
			}
		} finally {
			gc.restore();
		}
	}
	@Hide
	public void rotate(GraphicsContext gc, double angle, double px, double py) {
		Rotate r = new Rotate(angle, px, py);
		gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
	}
	/**
	 * Similar to DrawRect. Draws a rotated rectangle.
	 */
	public void DrawRectRotated(double x, double y, double Width, double Height, Paint Paint, boolean Filled, double StrokeWidth, double Degree) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.save();
		try {
			rotate(gc, Degree, x + Width / 2, y + Height / 2);
			DrawRect(x, y, Width, Height, Paint, Filled, StrokeWidth);
		} finally {
			gc.restore();
		}
	}
	/**
	 * Draws a line from (x1, y1) to (x2, y2).
	 */
	public void DrawLine(double x1, double y1, double x2, double y2, Paint Paint, double StrokeWidth) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.save();
		try {
			gc.setStroke(Paint);
			gc.setLineWidth(StrokeWidth);
			gc.strokeLine(x1, y1, x2, y2);
		} finally {
			gc.restore();
		}
	}
	/**
	 * Draws the text.
	 *Text - The text that will be drawn.
	 *x - The origin X coordinate.
	 *y - The origin Y coordinate.
	 *Font - The text font.
	 *Paint - Drawing color.
	 *TextAlignment - Sets the alignment relative to the origin. One of the following values: LEFT, CENTER, RIGHT. 
	 */
	public void DrawText(String Text, double x, double y, Font Font, Paint Paint, TextAlignment Alignment) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.save();
		try {
			gc.setFill(Paint);
			gc.setFont(Font);
			gc.setTextAlign(Alignment);
			gc.fillText(Text, x, y);
		} finally {
			gc.restore();
		}
	}
	/**
	 * Similar to DrawText. Rotates the text before it is drawn.
	 */
	public void DrawTextRotated(String Text, double x, double y, Font Font, Paint Paint, TextAlignment Alignment, double Degree) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.save();
		try {
			rotate(gc, Degree, x, y);
			DrawText(Text, x, y, Font, Paint, Alignment);
		} finally {
			gc.restore();
		}
	}
	/**
	 * Similar to DrawText. MaxWidth defines the text bounds. The text will be wrapped if it is longer.
	 */
	public void DrawText2(String Text, double x, double y, Font Font, Paint Paint, TextAlignment Alignment, double MaxWidth) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.save();
		try {
			gc.setFill(Paint);
			gc.setFont(Font);
			gc.setTextAlign(Alignment);
			gc.fillText(Text, x, y, MaxWidth);
		} finally {
			gc.restore();
		}
	}
	//	public void DrawText2Rotated(String Text, double x, double y, Font Font, Paint Paint, TextAlignment Alignment, double MaxWidth,
	//			double Degree) {
	//		GraphicsContext gc = getObject().getGraphicsContext2D();
	//		gc.save();
	//		try {
	//			rotate(gc, Degree, x, y);
	//			DrawText2(Text, x, y, Font, Paint, Alignment, MaxWidth);
	//		} finally {
	//			gc.restore();
	//		}
	//	}
	/**
	 * Clears the drawings in the given rectangle.
	 */
	public void ClearRect(double x, double y, double Width, double Height) {
		GraphicsContext gc = getObject().getGraphicsContext2D();
		gc.clearRect(x, y, Width, Height);
	}
	@Hide
	public static Node build(Object prev, HashMap<String, Object> props, boolean designer, Object tag) throws Exception{
		Canvas vg = (Canvas) prev;
		if (vg == null)  {
			vg = NodeWrapper.buildNativeView(Canvas.class, props, designer);
		}
		NodeWrapper.build(vg, props, designer);
		if (designer) {
			GraphicsContext gc = vg.getGraphicsContext2D();
			gc.save();
			try {
				gc.setStroke(Colors.Red);
				gc.setLineWidth(2);
				gc.clearRect(0, 0, vg.getWidth(), vg.getHeight());
				gc.strokeLine(0, 0, vg.getWidth(), vg.getHeight());
				gc.strokeLine(vg.getWidth(), 0, 0, vg.getHeight());
			} finally {
				gc.restore();
			}
		}
		return vg;
	}



}
