
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

import io.moquette.broker.IRetainedRepository;
import io.moquette.broker.RetainedMessage;
import io.moquette.broker.subscriptions.Topic;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class H2RetainedRepository implements IRetainedRepository {

    private final MVMap<Topic, RetainedMessage> queueMap;

    public H2RetainedRepository(MVStore mvStore) {
        this.queueMap = mvStore.openMap("retained_store");
    }

    @Override
    public void cleanRetained(Topic topic) {
        queueMap.remove(topic);
    }

    @Override
    public void retain(Topic topic, MqttPublishMessage msg) {
        final ByteBuf payload = msg.content();
        byte[] rawPayload = new byte[payload.readableBytes()];
        payload.getBytes(0, rawPayload);
        final RetainedMessage toStore = new RetainedMessage(msg.fixedHeader().qosLevel(), rawPayload);
        queueMap.put(topic, toStore);
    }

    @Override
    public boolean isEmpty() {
        return queueMap.isEmpty();
    }

    @Override
    public List<RetainedMessage> retainedOnTopic(String topic) {
        final Topic searchTopic = new Topic(topic);
        final List<RetainedMessage> matchingMessages = new ArrayList<>();
        for (Map.Entry<Topic, RetainedMessage> entry : queueMap.entrySet()) {
            final Topic scanTopic = entry.getKey();
            if (searchTopic.match(scanTopic)) {
                matchingMessages.add(entry.getValue());
            }
        }

        return matchingMessages;
    }
}
