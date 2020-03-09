
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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;

/**
 * MediaPlayer allows you to play audio media files.
 *The file is set once when you MediaPlayer is initialized.
 *Note that wav files in the assets folder will not work in Release mode.
 *The Complete event is raised when the playback reaches the end of the media. The player is still consider playing.
 *You can restart playback by setting the position to 0.
 */
@Events(values={"Complete"})
@ShortName("MediaPlayer")
public class MediaPlayerWrapper extends AbsObjectWrapper<MediaPlayer>{
	/**
	 * Initializes the object, sets the subs that will handle the events and loads the media resource.
	 */
	public void Initialize(final BA ba, String EventName, String MediaUri) throws URISyntaxException, UnsupportedEncodingException {
		Media m = new Media(MediaUri);
		final MediaPlayer mp = new MediaPlayer(m);
		setObject(mp);
		final String eventName = EventName.toLowerCase(BA.cul);
		if (ba.subExists(eventName + "_complete")) {
			getObject().setOnEndOfMedia(new Runnable() {
				
				@Override
				public void run() {
					ba.raiseEvent(mp, eventName + "_complete");
				}
			});
		}
		getObject().setOnError(new Runnable() {
			
			@Override
			public void run() {
				getObject().getError().printStackTrace();
			}
		});
	}
	/**
	 * Starts (or resumes) playing the media file.
	 */
	public void Play() {
		getObject().play();
	}
	/**
	 * Pauses the playback.
	 */
	public void Pause() {
		getObject().pause();
	}
	/**
	 * Stops the playback. Calling Play will start from the beginning.
	 */
	public void Stop() {
		getObject().stop();
	}
	/**
	 * Gets or sets the volume (between 0 to 1).
	 */
	public double getVolume() {
		return getObject().getVolume();
	}
	public void setVolume(double d) {
		getObject().setVolume(d);
	}
	/**
	 * Gets the total duration, measured in milliseconds.
	 */
	public double getDuration() {
		return getObject().getTotalDuration().toMillis();
	}
	/**
	 * Gets or sets the playback position, measured in milliseconds.
	 */
	public double getPosition() {
		return getObject().getCurrentTime().toMillis();
	}
	public void setPosition(double d) {
		getObject().seek(Duration.millis(d));
	}
	/**
	 * Gets or sets the number of times the playback will repeat. The default value is 1.
	 *Setting this value to -1 means that the playback will repeat indefinitely.
	 */
	public int getCycleCount() {
		return getObject().getCycleCount();
	}
	public void setCycleCount(int i) {
		getObject().setCycleCount(i);
	}
}
