
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

/** Parties interested in receiving the results from a call to the 
 * {@link ElevationService} must implement this interface. The results may be 
 * an empty array if the status is anything other than ElevationStatus.OK
 *
 * @author Geoff Capper
 */
public interface ElevationServiceCallback {
    
    public void elevationsReceived(ElevationResult[] results, ElevationStatus status);
    
}
