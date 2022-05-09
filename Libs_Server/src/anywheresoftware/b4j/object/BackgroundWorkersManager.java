
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

import java.lang.reflect.Method;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;

@Hide
public class BackgroundWorkersManager {
	private final boolean debug;
	private final BA ba;
	public BackgroundWorkersManager(BA ba, boolean debug) {
		this.debug = debug;
		this.ba = ba;
	}
	public void startWorker(final Class<?> className) {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					Method initializeMethod = JServlet.getInitializeMethod(className);
					JServlet.createInstance(className, initializeMethod);
					System.out.println("Worker ended (" + className + ")");
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		if (debug) {
			ba.postRunnable(r);
		}
		else {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.start();
		}
	}
}
