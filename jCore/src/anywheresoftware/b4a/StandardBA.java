
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
 
 package anywheresoftware.b4a;

import java.util.concurrent.ConcurrentHashMap;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.keywords.SimpleMessageLoop;

@Hide
public class StandardBA extends BA{
	private final Thread ownerThread;
	
	private static final ConcurrentHashMap<Thread, SimpleMessageLoop> loops = new ConcurrentHashMap<Thread, SimpleMessageLoop>();
	public StandardBA(String packageName, String className, Object eventsTarget) {
		super(packageName, className, eventsTarget);
		ownerThread = Thread.currentThread();
		synchronized (loops) {
			if (loops.containsKey(ownerThread) == false) {
				loops.put(ownerThread, new SimpleMessageLoop());
			}
		}
	}
	public void startMessageLoop() throws InterruptedException {
		if (ownerThread != Thread.currentThread())
			throw new RuntimeException("StartMessageLoop called from wrong thread.");
		SimpleMessageLoop loop = loops.get(ownerThread);
		loop.runMessageLoop();
	}

	@Override
	public void postRunnable(Runnable runnable) {
		SimpleMessageLoop loop = loops.get(ownerThread);
		loop.put(runnable);
	}
	@Override
	public void stopMessageLoop() {
		postRunnable(SimpleMessageLoop.STOP);
	}
	@Override
	public Thread getOwnerThread() {
		return ownerThread;
	}
	@Override
	public void cleanMessageLoop() throws InterruptedException {
		SimpleMessageLoop loop = loops.get(ownerThread);
		loop.clear();
		
	}

}
