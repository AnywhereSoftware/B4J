
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
 
 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lynden.gmapsfx.javascript;

import javafx.concurrent.Worker;

/**
 * Provides an abstraction to the web engine, so that a mock engine can be utilized in unit tests, or a different web engine
 * could be utilized in future versions of this framework.
 * 
 * @author Rob Terpilowski
 */
public interface IWebEngine {

    /**
     * Executes the specified JavaScript Command
     *
     * @param command The command to execute
     * @return The object returned by the script (if any).
     */
    public Object executeScript(String command);

    /**
     * Gets a worked which will be notified when a web page has finished
     * loading.
     *
     * @return The worker
     */
    public Worker<Void> getLoadWorker();

    /**
     * Loads the specified URL
     *
     * @param url The URL to load in the engine.
     */
    public void load(String url);

    /**
     * Loads the given HTML content directly.
     *
     * @param content The HTML text to load in the engine.
     */
    public void loadContent(String content);
}
