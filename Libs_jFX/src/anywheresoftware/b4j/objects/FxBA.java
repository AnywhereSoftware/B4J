
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

import javafx.application.Application;
import javafx.application.Platform;
import anywheresoftware.b4a.BA;


@anywheresoftware.b4a.BA.Hide
public class FxBA extends BA{
	public static Application application;
	public FxBA(String packageName, String className, Object eventsTarget) {
		super(packageName, className, eventsTarget);
	}

	@Override
	public void postRunnable(Runnable runnable) {
		Platform.runLater(runnable);
	}

	@Override
	public void startMessageLoop() throws InterruptedException {
		throw new RuntimeException("StartMessageLoop should only be called in non-UI applications.");
	}

	@Override
	public void stopMessageLoop() {
		throw new RuntimeException("StopMessageLoop should only be called in non-UI applications.");
	}

	@Override
	public Thread getOwnerThread() {
		return null;
	}

	@Override
	public void cleanMessageLoop() throws InterruptedException {
		throw new RuntimeException("not supported");
		
	}

}
