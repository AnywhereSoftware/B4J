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

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by hansolo on 01.11.16.
 */
public class MovingAverage {
    public  static final int         MAX_PERIOD     = 1000;
    private static final int         DEFAULT_PERIOD = 10;
    private        final Queue<Data> window;
    private              int         numberPeriod;
    private              double      sum;


    // ******************** Constructors **************************************
    public MovingAverage() {
        this(DEFAULT_PERIOD);
    }
    public MovingAverage(final int NUMBER_PERIOD) {
        numberPeriod = Helper.clamp(0, MAX_PERIOD, NUMBER_PERIOD);
        window       = new ConcurrentLinkedQueue<>();
    }


    // ******************** Methods *******************************************
    public void addData(final Data DATA) {
        sum += DATA.getValue();
        window.add(DATA);
        if (window.size() > numberPeriod) {
            sum -= window.remove().getValue();
        }
    }
    public void addValue(final double VALUE) {
        addData(new Data(VALUE));
    }

    public Queue<Data> getWindow() { return new LinkedList<>(window); }

    public double getAverage() {
        if (window.isEmpty()) return 0; // technically the average is undefined
        return (sum / window.size());
    }

    public double getTimeBasedAverageOf(final Duration DURATION) {
        assert !DURATION.isNegative() : "Time period must be positive";
        Instant now     = Instant.now();
        double  average = window.stream()
                                .filter(v -> v.getTimestamp().isAfter(now.minus(DURATION)))
                                .mapToDouble(Data::getValue)
                                .average()
                                .getAsDouble();
        return average;
    }

    public void reset() { window.clear(); }
}
