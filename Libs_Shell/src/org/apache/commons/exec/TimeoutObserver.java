/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.commons.exec;

/**
 * Interface for classes that want to be notified by Watchdog.
 * 
 * @see org.apache.commons.exec.Watchdog
 *
 * @version $Id: TimeoutObserver.java 1556869 2014-01-09 16:51:11Z britter $
 */
public interface TimeoutObserver {

    /**
     * Called when the watchdog times out.
     * 
     * @param w the watchdog that timed out.
     */
    void timeoutOccured(Watchdog w);
}
