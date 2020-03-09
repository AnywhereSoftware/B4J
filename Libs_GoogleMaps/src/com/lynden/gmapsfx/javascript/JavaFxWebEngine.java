
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
import javafx.event.EventHandler;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;

/**
 * This class provides an implementation of the IWebEngine interface utilizing 
 * a javafx.scene.web.WebEngine as the underlying engine.
 * 
 * @author Rob Terpilowski
 */
public class JavaFxWebEngine implements IWebEngine {
    
    
    protected WebEngine webEngine;
    
    /**
     * Builds a new engine utilizing the specified JavaFX WebEngine
     * @param engine The JavaFX WebEngine to use.
     */
    public JavaFxWebEngine( WebEngine engine ) {
        this.webEngine = engine;
    }

    
    /**
     * Executes the specified JavaScript Command
     * @param command The command to execute
     * @return The object returned by the script (if any).
     */
    @Override
    public Object executeScript(String command) {
        return webEngine.executeScript(command);
    }

    /**
     * Gets a worked which will be notified when a web page has finished loading.
     * @return The worker 
     */
    @Override
    public Worker<Void> getLoadWorker() {
        return webEngine.getLoadWorker();
    }

    /**
     * Loads the specified URL
     * @param url The URL to load in the engine.
     */
    @Override
    public void load(String url) {
        webEngine.load(url);
    }

    /**
     * Loads the given HTML content directly.
     * @param content The HTML text to load in the engine.
     */
    @Override
    public void loadContent(String content) {
        webEngine.loadContent(content);
    }

    public void setOnAlert(EventHandler<WebEvent<String>> eventHandler) {
        webEngine.setOnAlert(eventHandler);
    }

    public void setOnError(EventHandler<WebErrorEvent> eventHandler) {
        webEngine.setOnError(eventHandler);
    }
}
