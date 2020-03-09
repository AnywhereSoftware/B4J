
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
 
 package anywheresoftware.b4a.objects;


import java.lang.reflect.Field;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B4XViewWrapper.B4XBitmapWrapper;
import anywheresoftware.b4a.objects.B4XViewWrapper.B4XFont;
import anywheresoftware.b4j.objects.CanvasWrapper;
import anywheresoftware.b4j.objects.JFX.Colors;

import com.sun.javafx.geom.Arc2D;
import com.sun.javafx.geom.Ellipse2D;
import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.RoundRectangle2D;
import com.sun.javafx.geom.transform.BaseTransform;

/**
 * A cross platform canvas.
 */
@ShortName("B4XCanvas")
public class B4XCanvas {
	@Hide
	public CanvasWrapper cvs;
	private B4XRect targetRect;
	/**
	 * Initializes the canvas.
	 *In B4A and B4i the canvas will draw on the passed view.
	 *In B4J the canvas which is a view by itself is added to the passed pane as the first element. 
	 */
	public void Initialize(BA ba, B4XViewWrapper Pane) {
		cvs = new CanvasWrapper();
		cvs.Initialize(ba, "");
		Pane.AddView(cvs.getObject(), 0, 0, Pane.getWidth(), Pane.getHeight());
		cvs.getObject().toBack();
		targetRect = new B4XRect();
		targetRect.Initialize(0, 0, (float)cvs.getWidth(), (float)cvs.getHeight());
		cvs.getObject().getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
	}
	/**
	 * Resizes the canvas.
	 */
	public void Resize(double Width, double Height) {
		cvs.SetSize(Width, Height);
		targetRect.right = (float)Width;
		targetRect.bottom = (float)Height;
	}
	/**
	 * Returns a B4XRect with the same dimensions as the target view.
	 */
	public B4XRect getTargetRect() {
		return targetRect;
	}
	/**
	 * Returns the target view.
	 */
	public B4XViewWrapper getTargetView() {
		return (B4XViewWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XViewWrapper(), cvs.getObject().getParent());
	}
	/**
	 * Commits the drawings. Must be called for the drawings to be updated.
	 */
	public void Invalidate() {

	}
	/**
	 * Draws a line between x1,y1 to x2,y2.
	 */
	public void DrawLine(float x1, float y1, float x2, float y2, int Color, float StrokeWidth) {
		cvs.DrawLine(x1, y1, x2, y2, Colors.From32Bit(Color), StrokeWidth);
	}
	/**
	 * Returns a copy of the canvas bitmap. In B4A it returns the canvas bitmap itself (not a copy).
	 */
	public B4XBitmapWrapper CreateBitmap() {
		return (B4XBitmapWrapper)AbsObjectWrapper.ConvertToWrapper(new B4XBitmapWrapper(), cvs.Snapshot2(Colors.Transparent).getObject());
	}
	/**
	 * Draws a rectangle.
	 */
	public void DrawRect(B4XRect Rect, int Color, boolean Filled, float StrokeWidth) {
		cvs.DrawRect(Rect.left, Rect.top, Rect.getWidth(), Rect.getHeight(), Colors.From32Bit(Color), Filled, StrokeWidth);
	}
	/**
	 * Draws a circle.
	 */
	public void DrawCircle(float x, float y, float Radius, int Color, boolean Filled, float StrokeWidth) {
		cvs.DrawCircle(x, y, Radius, Colors.From32Bit(Color), Filled, StrokeWidth);
	}
	/**
	 * Draws a bitmap in the given destination. Use B4XBitmap.Crop to draw part of a bitmap.
	 */
	public void DrawBitmap (Image Bitmap, B4XRect Destination) {
		cvs.DrawImage(Bitmap, Destination.left, Destination.top, Destination.getWidth(), Destination.getHeight());
	}
	/**
	 * Similar to DrawBitmap. Draws a rotated bitmap.
	 */
	public void DrawBitmapRotated(Image Bitmap, B4XRect Destination, float Degrees) {
		cvs.DrawImageRotated(Bitmap, Destination.left, Destination.top, Destination.getWidth(), Destination.getHeight(), Degrees);
	}
	/**
	 * Clips the drawings to a closed path.
	 */
	public void ClipPath(B4XPath Path) throws Exception{
		GraphicsContext gc = cvs.getObject().getGraphicsContext2D();
		gc.save();
		gc.beginPath();
		Field pf = GraphicsContext.class.getDeclaredField("path");
		pf.setAccessible(true);
		Path2D p = (Path2D) pf.get(gc);
		p.setTo(Path.getObject());
		gc.closePath();
		gc.clip();
	}
	/**
	 * Removes a previously set clip region.
	 */
	public void RemoveClip() {
		cvs.RemoveClip();
	}
	/**
	 * Draws the given path.
	 *Path - Path shape.
	 *Color - Drawing color.
	 *Filled - Whether to fill the shape or not.
	 *StrokeWidth - Stroke width. Only relevant when Filled is False.
	 *
	 *Note that there is a subtle difference in the way the stroke width affects the drawing between B4J and the other platforms.
	 *In B4J the path defines the stroke edge. In B4A and B4i it defines the stroke center. 
	 */
	public void DrawPath(B4XPath Path, int Color, boolean Filled, float StrokeWidth) throws Exception{
		ClipPath(Path);
		GraphicsContext gc = cvs.getObject().getGraphicsContext2D();
		Paint clr = Colors.From32Bit(Color);
		if (Filled) {
			gc.setFill(clr);
			gc.fill();
		}
		else {
			gc.setStroke(clr);
			gc.setLineWidth(StrokeWidth * 2);
			gc.stroke();
		}
		cvs.RemoveClip();

	}
	/**
	 * Similar to DrawPath. Rotates the path based on the degrees and center parameters.
	 */
	public void DrawPathRotated(B4XPath Path, int Color, boolean Filled, float StrokeWidth, float Degrees, float CenterX, float CenterY) throws Exception{
		Path.getObject().transform(BaseTransform.getRotateInstance(Degrees / 180 * Math.PI, CenterX, CenterY));
		DrawPath(Path, Color, Filled, StrokeWidth);
		Path.getObject().transform(BaseTransform.getRotateInstance(-Degrees / 180 * Math.PI, CenterX, CenterY));
	}
	@Hide
	public void DrawPolygonImpl(double[] xx, double[] yy, int Color, boolean Filled, float StrokeWidth) throws Exception{
		GraphicsContext gc = cvs.getObject().getGraphicsContext2D();
		Paint clr = Colors.From32Bit(Color);
		if (Filled) {
			gc.setFill(clr);
			gc.fillPolygon(xx, yy, xx.length);
		}
		else {
			gc.setStroke(clr);
			gc.setLineWidth(StrokeWidth * 2);
			gc.strokePolygon(xx, yy, xx.length);
		}
		cvs.RemoveClip();

	}
	/**
	 * Clears the given rectangle. Does not work in B4J with clipped paths.
	 */
	public void ClearRect(B4XRect Rect) {
		cvs.ClearRect(Rect.left, Rect.top, Rect.getWidth(), Rect.getHeight());
	}
	/**
	 * Draws the text.
	 *Text - The text that will be drawn.
	 *x - The origin X coordinate.
	 *y - The origin Y coordinate.
	 *Font - The text font.
	 *Color - Drawing color.
	 *Alignment - Sets the alignment relative to the origin. One of the following values: LEFT, CENTER, RIGHT. 
	 */
	public void DrawText(String Text, double x, double y, B4XFont Font, int Color, TextAlignment Alignment)  {
		GraphicsContext gc = cvs.getObject().getGraphicsContext2D();
		//gc.setTextBaseline(VPos.TOP);
		cvs.DrawText(Text, x, y, Font.getObject(), Colors.From32Bit(Color), Alignment);
	}
	/**
	 * Similar to DrawText. Rotates the text before it is drawn.
	 */
	public void DrawTextRotated(String Text, double x, double y, B4XFont Font, int Color, TextAlignment Alignment, float Degree)  {
		cvs.DrawTextRotated(Text, x, y, Font.getObject(), Colors.From32Bit(Color), Alignment, Degree);
	}
	/**
	 * Releases native resources related to the canvas. Does nothing in B4A and B4J.
	 */
	public void Release() {

	}
	/**
	 * Measures single line texts and returns their width, height and the height above the baseline.
	 *-Rect.Top returns the height above the baseline.
	 *Code to draw center aligned text:<code>
	 *Dim r As B4XRect = cvs1.MeasureText(Text, Fnt)
	 *Dim BaseLine As Int = CenterY - r.Height / 2 - r.Top
	 *cvs1.DrawText(Text, CenterX, BaseLine, Fnt, Clr, "CENTER")</code>
	 */
	public static B4XRect MeasureText(String Text, B4XFont Font) {
		if (Text.startsWith(" "))
			Text = "." + Text.substring(1);
		if (Text.endsWith(" "))
			Text = Text.substring(0, Text.length() - 1) + ".";
		Text t = new Text(Text);
		t.setFont(Font.getObject());
		t.setLineSpacing(0);
		t.setWrappingWidth(0);
		t.setBoundsType(TextBoundsType.VISUAL);
		B4XRect r = new B4XRect();
		Bounds b = t.getLayoutBounds();
		r.Initialize((float)b.getMinX(), (float)b.getMinY(), (float)b.getMaxX(), (float)b.getMaxY());
		return r;
	}

	@ShortName("B4XRect")
	public static class B4XRect {
		private float left, top, right, bottom;
		public void Initialize(float Left, float Top, float Right, float Bottom) {
			left = Left;
			top = Top;
			right = Right;
			bottom = Bottom;
		}


		public float getLeft() {
			return left;
		}


		public void setLeft(float left) {
			this.left = left;
		}


		public float getTop() {
			return top;
		}


		public void setTop(float top) {
			this.top = top;
		}


		public float getRight() {
			return right;
		}


		public void setRight(float right) {
			this.right = right;
		}


		public float getBottom() {
			return bottom;
		}


		public void setBottom(float bottom) {
			this.bottom = bottom;
		}

		/**
		 * Gets or sets the rectangle width.
		 */
		public float getWidth() {
			return right - left;
		}
		public void setWidth(int w) {
			right = left + w;
		}
		/**
		 * Gets or sets the rectangle height.
		 */
		public float getHeight() {
			return bottom - top;
		}
		public void setHeight(float h) {
			bottom = top + h;
		}
		/**
		 * Returns the horizontal center.
		 */
		public float getCenterX() {return (left + right) * 0.5f;};
		/**
		 * Returns the vertical center.
		 */
		public float getCenterY() {return (top + bottom) * 0.5f;};
		@Hide
		@Override
		public String toString() {
			return "(" + left + ", " + top + ", " + right + ", " + bottom + ")";
		}

	}
	@ShortName("B4XPath")
	public static class B4XPath extends AbsObjectWrapper<Path2D> {
		/**
		 * Initializes the path and sets the value of the first point.
		 */
		public B4XPath Initialize(float x, float y) {
			Path2D p = new Path2D();
			p.moveTo(x, y);
			setObject(p);
			return this;
		}
		/**
		 * Initializes the path and sets the current path shape to an oval.
		 *Rect - The oval framing rectangle.
		 */
		public B4XPath InitializeOval(B4XRect Rect) {
			Path2D p = new Path2D();
			p.append(new Ellipse2D(Rect.left, Rect.top, Rect.getWidth(), Rect.getHeight()), true);
			setObject(p);
			return this;
		}
		/**
		 * Initializes the path and sets the current path shape to an arc. 
		 * x / y - Arc center.
		 * Radius - Arc radius.
		 * StartingAngle - The starting angle. 0 equals to hour 3.
		 * SweepAngle - Sweep angle. Positive = clockwise.
		 */
		public B4XPath InitializeArc(float x, float y, float Radius, float StartingAngle, float SweepAngle) {
			Path2D p = new Path2D();
			p.append(new Arc2D(x - Radius, y - Radius, 2 * Radius, 2 * Radius, -StartingAngle, -SweepAngle, Arc2D.PIE), true);
			setObject(p);
			return this;
		}
		/**
		 * Initializes the path and sets the current path shape to a rectangle with rounded corners.
		 *Rect - Rectangle.
		 *CornersRadius - Corners radius.
		 */
		public B4XPath InitializeRoundedRect (B4XRect Rect, float CornersRadius) {
			Path2D p = new Path2D();
			p.append(new RoundRectangle2D(Rect.left, Rect.top, Rect.getWidth(), Rect.getHeight(), 
					CornersRadius * 2, CornersRadius * 2), true);
			setObject(p);
			return this;
		}

		/**
		 * Adds a line from the last point to the specified point.
		 */
		public B4XPath LineTo(float x, float y) {
			getObject().lineTo(x, y);
			return this;
		}
	}
}
