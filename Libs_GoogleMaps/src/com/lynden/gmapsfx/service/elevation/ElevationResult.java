
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
import com.lynden.gmapsfx.javascript.object.LatLong;
import netscape.javascript.JSObject;

/** Encapsulates the response from the ElevationService for one particular 
 * location.
 *
 * @author Geoff Capper
 */
public class ElevationResult extends JavascriptObject {
    
    private LatLong location;
    
    public ElevationResult(JSObject jsObject) {
        super(GMapObjectType.ELEVATION_RESULT, jsObject);
    }
    
    /** The elevation returned from the ElevationService.
     * 
     * @return The elevation in metres.
     */
    public double getElevation() {
        return (double) getJSObject().getMember("elevation");
    }
    
    /** The location for this elevation.
     * 
     * @return 
     */
    public LatLong getLocation() {
        if (location == null) {
            location = new LatLong((JSObject) (getJSObject().getMember("location")));
        }
        return location;
    }
    
    /** The resolution for the elevation, which is the distance in metres
     * between the points that were used to interpolate the elevation.
     * 
     * @return 
     */
    public double getResolution() {
        return (double) getJSObject().getMember("resolution");
    }
    
}
