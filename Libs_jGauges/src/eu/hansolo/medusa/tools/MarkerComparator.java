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

package eu.hansolo.medusa.tools;

import eu.hansolo.medusa.Marker;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Created by hansolo on 26.01.16.
 */
public class MarkerComparator implements Comparator<Marker>, Serializable {
    @Override public int compare(final Marker MARKER_1, final Marker MARKER_2) {
        return MARKER_1.compareTo(MARKER_2);
    }
}
