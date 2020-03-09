/*
 * Copyright (c) 2016 by Gerrit Grunwald
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

package eu.hansolo.medusa.events;

import eu.hansolo.medusa.Alarm;

import java.util.EventObject;


/**
 * Created by hansolo on 28.01.16.
 */
public class AlarmEvent extends EventObject {
    public final Alarm ALARM;


    // ******************** Constructors **************************************
    public AlarmEvent(final Object SRC, final Alarm ALARM) {
        super(SRC);
        this.ALARM = ALARM;
    }
}
