
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
 
 package anywheresoftware.b4a.keywords;

import java.util.concurrent.LinkedBlockingQueue;

import anywheresoftware.b4a.BA.Hide;

@Hide
public class SimpleMessageLoop {
	private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	public static final Runnable STOP = new Runnable() {
		
		@Override
		public void run() {
			
		}
	};
	
	public void clear() {
		queue.clear();
	}
	public void put (Runnable runnable) {
		queue.add(runnable);
	}
	public void runMessageLoop() throws InterruptedException {
		while (true) {
			Runnable r = queue.take();
			if (r == STOP) {
				break;
			}
			r.run();
		}
	}
}
