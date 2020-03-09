
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

/** Creates a request that can be passed in to the {@link ElevationService} to 
 * get the elevations for a number of locations.
 *
 * @author Geoff Capper
 */
public class LocationElevationRequest extends JavascriptObject {
    
    public LocationElevationRequest() {
        super(GMapObjectType.OBJECT);
    }
    
    public LocationElevationRequest(LatLong[] locations) {
        super(GMapObjectType.OBJECT);
        getJSObject().setMember("locations", getJSObject().eval("[]"));
        for (int i = 0; i < locations.length; i++) {
            getJSObject().eval(getVariableName()+".locations.push("+locations[i].getVariableName()+")");
        }
    }
    
}
