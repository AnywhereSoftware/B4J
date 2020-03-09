
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

package com.lynden.gmapsfx.service.elevation;

import com.lynden.gmapsfx.javascript.JavascriptObject;
import com.lynden.gmapsfx.javascript.object.GMapObjectType;
import netscape.javascript.JSObject;

/** Makes a request to the Google Maps Elevation Service.
 * <p>
 * Instances of this class are not safe for re-use at this stage. A new 
 * one should be created for each request.
 *
 * @author Geoff Capper
 */
public class ElevationService extends JavascriptObject {
    
    private ElevationServiceCallback callback;
    
    public ElevationService() {
        super(GMapObjectType.ELEVATION_SERVICE);
    }
    
    /** Create a request for elevations for multiple locations.
     * 
     * @param req
     * @param callback 
     */
    public void getElevationForLocations(LocationElevationRequest req, ElevationServiceCallback callback) {
        
        this.callback = callback;
        
        JSObject doc = (JSObject) getJSObject().eval("document");
        doc.setMember(getVariableName(), this);
        
        StringBuilder r = new StringBuilder(getVariableName())
              .append(".")
              .append("getElevationForLocations(")
              .append(req.getVariableName())
              .append(", ")
              .append("function(results, status) {alert('rec:'+status);\ndocument.")
              .append(getVariableName())
              .append(".processResponse(results, status);});");
        
        System.out.println("ElevationService direct call: " + r.toString());
        
        getJSObject().eval(r.toString());
        
    }

    /** Create a request for elevations for samples along a path.
     * 
     * @param req
     * @param callback 
     */
    public void getElevationAlongPath(PathElevationRequest req, ElevationServiceCallback callback) {
        
        this.callback = callback;
        
        JSObject doc = (JSObject) getJSObject().eval("document");
        doc.setMember(getVariableName(), this);
        
        StringBuilder r = new StringBuilder(getVariableName())
              .append(".")
              .append("getElevationAlongPath(")
              .append(req.getVariableName())
              .append(", ")
              .append("function(results, status) {document.")
              .append(getVariableName())
              .append(".processResponse(results, status);});");
        
        getJSObject().eval(r.toString());
        
    }
    
    /** Processess the Javascript response and generates the required objects 
     * that are then passed back to the original callback.
     * 
     * @param results
     * @param status 
     */
    public void processResponse(Object results, Object status) {
        ElevationStatus pStatus = ElevationStatus.UNKNOWN_ERROR;
        
        if (status instanceof String && results instanceof JSObject) {
            pStatus = ElevationStatus.valueOf((String) status);
            if (ElevationStatus.OK.equals(pStatus)) {
                JSObject jsres = (JSObject) results;
                Object len = jsres.getMember("length");
                if (len instanceof Number) {
                    int n = ((Number)len).intValue();
                    ElevationResult[] ers = new ElevationResult[n];
                    for (int i = 0; i < n; i++) {
                        Object obj = jsres.getSlot(i);
                        if (obj instanceof JSObject) {
                            ers[i] = new ElevationResult((JSObject) obj);
                        }
                    }
                    callback.elevationsReceived(ers, pStatus);
                    return;
                }
            }
        }
        callback.elevationsReceived(new ElevationResult[]{}, pStatus);
    }
}
