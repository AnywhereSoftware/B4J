
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
 
 package io.moquette.persistence;

import io.moquette.BrokerConstants;
import io.moquette.broker.IQueueRepository;
import io.moquette.broker.IRetainedRepository;
import io.moquette.broker.ISubscriptionsRepository;
import io.moquette.broker.config.IConfig;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class H2Builder {

    private static final Logger LOG = LoggerFactory.getLogger(H2Builder.class);

    private final String storePath;
    private final int autosaveInterval; // in seconds
    private final ScheduledExecutorService scheduler;
    private MVStore mvStore;

    public H2Builder(IConfig props, ScheduledExecutorService scheduler) {
        this.storePath = props.getProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, "");
        final String autosaveProp = props.getProperty(BrokerConstants.AUTOSAVE_INTERVAL_PROPERTY_NAME, "30");
        this.autosaveInterval = Integer.parseInt(autosaveProp);
        this.scheduler = scheduler;
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    public H2Builder initStore() {
        LOG.info("Initializing H2 store");
        if (storePath == null || storePath.isEmpty()) {
            throw new IllegalArgumentException("H2 store path can't be null or empty");
        }
        mvStore = new MVStore.Builder()
            .fileName(storePath)
            .autoCommitDisabled()
            .open();

        LOG.trace("Scheduling H2 commit task");
        scheduler.scheduleWithFixedDelay(() -> {
            LOG.trace("Committing to H2");
            mvStore.commit();
        }, autosaveInterval, autosaveInterval, TimeUnit.SECONDS);
        return this;
    }

    public ISubscriptionsRepository subscriptionsRepository() {
        return new H2SubscriptionsRepository(mvStore);
    }

    public void closeStore() {
        mvStore.close();
    }

    public IQueueRepository queueRepository() {
        return new H2QueueRepository(mvStore);
    }

    public IRetainedRepository retainedRepository() {
        return new H2RetainedRepository(mvStore);
    }
}
