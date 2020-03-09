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

package eu.hansolo.medusa;

import eu.hansolo.medusa.Alarm.AlarmMarkerEvent;
import eu.hansolo.medusa.Alarm.Repetition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;

import java.time.ZonedDateTime;
import java.util.HashMap;


/**
 * Created by hansolo on 31.01.16.
 */
public class AlarmBuilder<B extends AlarmBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected AlarmBuilder() {}


    // ******************** Methods *******************************************
    public static final AlarmBuilder create() {
        return new AlarmBuilder();
    }

    public final B time(final ZonedDateTime DATE_TIME) {
        properties.put("time", new SimpleObjectProperty<ZonedDateTime>(DATE_TIME));
        return (B)this;
    }

    public final B repetition(final Repetition REPETITION) {
        properties.put("repetition", new SimpleObjectProperty<Repetition>(REPETITION));
        return (B)this;
    }

    public final B text(final String TEXT) {
        properties.put("text", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B armed(final boolean ARMED) {
        properties.put("armed", new SimpleBooleanProperty(ARMED));
        return (B)this;
    }

    public final B command(final Command COMMAND) {
        properties.put("command", new SimpleObjectProperty<>(COMMAND));
        return (B)this;
    }

    public final B color(final Color COLOR) {
        properties.put("color", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B onAlarmMarkerPressed(final EventHandler<AlarmMarkerEvent> HANDLER) {
        properties.put("onAlarmMarkerPressed", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onAlarmMarkerReleased(final EventHandler<AlarmMarkerEvent> HANDLER) {
        properties.put("onAlarmMarkerReleased", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final Alarm build() {
        final Alarm ALARM = new Alarm();
        for (String key : properties.keySet()) {
            if ("time".equals(key)) {
                ALARM.setTime(((ObjectProperty<ZonedDateTime>) properties.get(key)).get());
            } else if("repetition".equals(key)) {
                ALARM.setRepetition(((ObjectProperty<Repetition>) properties.get(key)).get());
            } else if("text".equals(key)) {
                ALARM.setText(((StringProperty) properties.get(key)).get());
            } else if("armed".equals(key)) {
                ALARM.setArmed(((BooleanProperty) properties.get(key)).get());
            } else if ("command".equals(key)) {
                ALARM.setCommand(((ObjectProperty<Command>) properties.get(key)).get());
            } else if ("color".equals(key)) {
                ALARM.setColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("onAlarmMarkerPressed".equals(key)) {
                ALARM.setOnMarkerPressed(((ObjectProperty<EventHandler>) properties.get(key)).get());
            } else if ("onAlarmMarkerReleased".equals(key)) {
                ALARM.setOnMarkerReleased(((ObjectProperty<EventHandler>) properties.get(key)).get());
            }
        }
        return ALARM;
    }
}

