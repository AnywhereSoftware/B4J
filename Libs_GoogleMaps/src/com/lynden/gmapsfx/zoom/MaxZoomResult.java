
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
import netscape.javascript.JSObject;

/**
 *
 * @author Geoff Capper
 */
public class MaxZoomResult extends JavascriptObject {
	
    private MaxZoomStatus status;
    
    public MaxZoomResult() {
        super(GMapObjectType.MAX_ZOOM_RESULT);
    }
    
    public MaxZoomResult(JSObject obj) {
        super(GMapObjectType.MAX_ZOOM_RESULT, obj);
    }
    
    public MaxZoomResult(MaxZoomStatus status) {
        super(GMapObjectType.MAX_ZOOM_RESULT);
        this.status = status;
    }
    
    public MaxZoomStatus getStatus() {
        if (status == null) {
            status = MaxZoomStatus.valueOf((String) getJSObject().getMember("status"));
        }
        return status;
    }
    
    public int getMaxZoom() {
        return (int) getJSObject().getMember("zoom");
    }
    
}
