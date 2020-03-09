
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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
/**
 * A Timer object generates ticks events at specified intervals.
 *Using a timer is a good alternative to a long loop, as it allows the UI thread to handle other events and messages.
 *Note that the timer events will not fire while the UI thread is busy running other code.
 *The timer Enabled property is set to False by default. To make it start working you should change it to True.
 */
@ShortName("Timer")
@Events(values={"Tick"})
public class Timer implements CheckForReinitialize{
	private long interval;
	private boolean enabled = false;
	private int relevantTimer = 0;
	private BA ba;
	private String eventName;
	/**
	 * Initializes the timer with the event sub prefix and the specified interval (measured in milliseconds).
	 *Example:<code>
	 *Timer1.Initialize("Timer1", 1000)
	 *Timer1.Enabled = True
	 *
	 *Sub Timer1_Tick
	 * 'Handle tick events
	 *End Sub
	 *</code>
	 */
	public void Initialize(BA ba, String EventName, long Interval) {
		this.interval = Interval;
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul) + "_tick";
	}
	@Override
	public boolean IsInitialized() {
		return ba != null;
	}
	/**
	 * Gets or sets whether the timer is enabled (ticking).
	 */
	public boolean getEnabled() {
		return enabled;
	}
	/**
	 * Gets or sets the interval between tick events, measured in milliseconds.
	 */
	public void setInterval(long Interval) {
		if (this.interval == Interval)
			return;
		this.interval = Interval;
		if (this.enabled) {
			stopTicking();
			startTicking();
		}
	}
	public long getInterval() {
		return interval;
	}
	private void startTicking() {
		Thread t = new Thread(new TickTack(relevantTimer));
		t.setDaemon(true);
		t.start();
	}

	public void setEnabled(boolean Enabled) {
		if (Enabled == this.enabled)
			return;
		if (Enabled == true){ //to true
			if (interval <= 0)
				throw new IllegalStateException("Interval must be larger than 0.");
			startTicking();
		}
		else {
			stopTicking();
		}
		this.enabled = Enabled;
	}

	class TickTack implements Runnable {
		private final int currentTimer;
		private final boolean debugMode;
		private final Semaphore semaphore;
		public TickTack(int currentTimer) {
			this.currentTimer = currentTimer;
			debugMode = BA.isShellModeRuntimeCheck(ba);
			if (debugMode)
				semaphore = new Semaphore(1);
			else
				semaphore = null;
		}
		@Override
		public void run() {
			while (true) {
				try {
					if (currentTimer != Timer.this.relevantTimer) //old messages in the queue
						return;
					Thread.sleep(interval);
					if (semaphore != null) {
						if (semaphore.tryAcquire(10000, TimeUnit.MILLISECONDS) == false)
							continue;
					}
				} catch (InterruptedException ie) {
					throw new RuntimeException(ie);
				}
				ba.postRunnable(new Runnable() {

					@Override
					public void run() {
						try {
							if (currentTimer != Timer.this.relevantTimer) //old messages in the queue
								return;
							ba.raiseEvent2(Timer.this, false, eventName, true);
						} finally {
							if (semaphore != null)
								semaphore.release();
						}

					}
				});
			}


		}
	}
	private void stopTicking() {
		relevantTimer++;
	}

}
