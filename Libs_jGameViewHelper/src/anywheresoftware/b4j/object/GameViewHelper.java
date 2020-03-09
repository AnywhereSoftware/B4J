
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
 
 package anywheresoftware.b4j.object;


import java.util.HashMap;

import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4j.objects.CanvasWrapper;
import anywheresoftware.b4j.objects.Form;

@ShortName("GameViewHelper")
@Version(1.03f)
@Events(values={"KeyPressed (KeyCode As String) As Boolean", "KeyReleased (KeyCode As String) As Boolean"})
public class GameViewHelper {
	public static final int FLIP_NONE = 0, FLIP_VERTICALLY = 1, FLIP_HORIZONTALLY = 2, FLIP_BOTH = 3;
	@Hide
	public HashMap<Integer, AudioClip> sounds = new HashMap<Integer, AudioClip>();
	/**
	 * This method can improve the drawings smoothness.
	 */
	public void SetBoxBlur(CanvasWrapper Canvas) {
		GraphicsContext gc = Canvas.getObject().getGraphicsContext2D();
		BoxBlur blur = new BoxBlur();
		blur.setWidth(1);
		blur.setHeight(1);
		blur.setIterations(1);
		gc.setEffect(blur);
	}
	/**
	 * Loads a short audio file and returns the stored id.
	 */
	public int LoadAudioClip(String Uri) {
		AudioClip ac = new AudioClip(Uri);
		sounds.put(sounds.size() + 1, ac);
		return sounds.size();
	}
	/**
	 * Plays a previously loaded audio file. Volume should be between 0 to 1.
	 */
	public void PlayAudioClip(int Id, double Volume) {
		AudioClip ac = sounds.get(Id);
		ac.play(Volume);
	}
	/**
	 * Stops playback.
	 */
	public void StopAudioClip(int id) {
		AudioClip ac = sounds.get(id);
		ac.stop();
	}
	/**
	 * Draws an image. The image can be rotated and flipped. The Flip value should be one of the FLIP constants.
	 */
	public void DrawImageRotateAndFlipped (CanvasWrapper Canvas, Image Image, Rect SrcRect, Rect DestRect, double Degree, int Flip) {
		GraphicsContext gc = Canvas.getObject().getGraphicsContext2D();
		gc.save();
		try {
			Transform r = null;
			if (Degree != 0) {
				r = new Rotate(Degree, DestRect.getCenterX(), DestRect.getCenterY());
			}
			if (Flip != 0) {
				Scale s = new Scale((Flip & FLIP_HORIZONTALLY) == FLIP_HORIZONTALLY ? -1 : 1,
						(Flip & FLIP_VERTICALLY) == FLIP_VERTICALLY ? -1 : 1,
								DestRect.getCenterX(), DestRect.getCenterY());
				if (r == null)
					r = s;
				else
					r = r.createConcatenation(s);
			}
			if (r != null)
				gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());

			gc.drawImage(Image, SrcRect.Left, SrcRect.Top, SrcRect.Width, SrcRect.Height, 
					DestRect.Left, DestRect.Top, DestRect.Width, DestRect.Height);
		} finally {
			gc.restore();
		}
	}
	/**
	 * Adds a key listener. The KeyPressed and KeyReleased events will be raised.
	 * Return True to consume the event.
	 */
	public void AddKeyListener(final BA ba, String EventName, Form Form) {
		final String eventName = EventName.toLowerCase(BA.cul);
		Form.scene.addEventFilter(KeyEvent.ANY, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				Boolean b = null;
				if (ke.getEventType() == KeyEvent.KEY_PRESSED)
					b = (Boolean) ba.raiseEvent(GameViewHelper.this, eventName + "_keypressed", ke.getCode().getName());
				else if (ke.getEventType() == KeyEvent.KEY_RELEASED)
					b = (Boolean) ba.raiseEvent(GameViewHelper.this, eventName + "_keyreleased", ke.getCode().getName());
				if (b != null && b == true)
					ke.consume();
			}
		});
	}
	
	@ShortName("Rect")
	public static class Rect {
		public double Left, Top, Width, Height;
		public void Initialize(double Left, double Top, double Width, double Height) {
			this.Left = Left;
			this.Top = Top;
			this.Width = Width;
			this.Height = Height;
					
		}
		public double getCenterX() {
			return Left + Width / 2;
		}
		public double getCenterY() {
			return Top + Height / 2;
		}
		public double getRight() {
			return Left + Width;
		}
		public double getBottom() {
			return Top + Height;
		}
	}
}
