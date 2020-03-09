
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

package com.lynden.gmapsfx.zoom;

import com.lynden.gmapsfx.javascript.JavascriptObject;
import com.lynden.gmapsfx.javascript.object.GMapObjectType;
import com.lynden.gmapsfx.javascript.object.LatLong;
import netscape.javascript.JSObject;

/**
 *
 * @author Geoff Capper
 */
public class MaxZoomService extends JavascriptObject {
    
    private MaxZoomServiceCallback callback;
    
    public MaxZoomService() {
        super(GMapObjectType.MAX_ZOOM_SERVICE);
    }
    
    public void getMaxZoomAtLatLng(LatLong loc, MaxZoomServiceCallback callback) {
        
        this.callback = callback;
        
        JSObject doc = (JSObject) getJSObject().eval("document");
        doc.setMember(getVariableName(), this);
        
        StringBuilder r = new StringBuilder(getVariableName())
              .append(".")
              .append("getMaxZoomAtLatLng(")
              .append(loc.getVariableName())
              .append(", ")
              .append("function(result) {document.")
              .append(getVariableName())
              .append(".processResponse(result);});");
        
//        System.out.println("MaxZoomService direct call: " + r.toString());
        
        getJSObject().eval(r.toString());
        
    }
    
    /** Processess the Javascript response and generates the required objects 
     * that are then passed back to the original callback.
     * 
     * @param result
     */
    public void processResponse(Object result) {
        if (result instanceof JSObject) {
            MaxZoomResult mzr = new MaxZoomResult((JSObject) result);
            callback.maxZoomReceived(mzr);
        }
    }
    
}
