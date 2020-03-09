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

import java.time.ZonedDateTime;
import java.util.EventObject;


/**
 * Created by hansolo on 24.02.16.
 */
public class TimeEvent extends EventObject {
    public enum TimeEventType { HOUR, MINUTE, SECOND };
    public final ZonedDateTime TIME;
    public final TimeEventType TYPE;


    // ******************** Constructors **************************************
    public TimeEvent(final Object SRC, final ZonedDateTime TIME, final TimeEventType TYPE) {
        super(SRC);
        this.TIME = TIME;
        this.TYPE = TYPE;
    }
}
