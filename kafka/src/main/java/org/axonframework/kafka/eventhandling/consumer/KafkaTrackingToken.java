/*
 * Copyright (c) 2010-2018. Axon Framework
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

package org.axonframework.kafka.eventhandling.consumer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.kafka.common.TopicPartition;
import org.axonframework.common.Assert;
import org.axonframework.eventsourcing.eventstore.TrackingToken;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Use to track messages consumed & committed from Kafka to Axon.
 *
 * @author Nakul Mishra
 * @since 3.0
 */
public class KafkaTrackingToken implements TrackingToken, Serializable {

    private final Map<Integer, Long> partitionPositions;

    @JsonCreator
    public static KafkaTrackingToken newInstance(
            @JsonProperty("partitionPositions") Map<Integer, Long> partitionPositions) {
        return new KafkaTrackingToken(partitionPositions);
    }

    public static KafkaTrackingToken emptyToken() {
        return newInstance(new HashMap<>());
    }

    private KafkaTrackingToken(Map<Integer, Long> partitionPositions) {
        this.partitionPositions = Collections.unmodifiableMap(new HashMap<>(partitionPositions));
    }

    public Map<Integer, Long> partitionPositions() {
        return partitionPositions;
    }

    public static Collection<TopicPartition> partitions(String topic, KafkaTrackingToken token) {
        return token.partitionPositions.keySet()
                                       .stream()
                                       .map(i -> new org.apache.kafka.common.TopicPartition(topic, i))
                                       .collect(Collectors.toList());
    }

    public static TopicPartition partition(String topic, int partitionNumber) {
        return new TopicPartition(topic, partitionNumber);
    }

    public KafkaTrackingToken advancedTo(int partition, long offset) {
        Assert.isTrue(partition >= 0, () -> "Partition may not be negative");
        Assert.isTrue(offset >= 0, () -> "Offset may not be negative");
        HashMap<Integer, Long> newPositions = new HashMap<>(partitionPositions);
        newPositions.put(partition, offset);
        return new KafkaTrackingToken(newPositions);
    }

    public static boolean isEmpty(KafkaTrackingToken token) {
        return token == null || token.partitionPositions.isEmpty();
    }

    public static boolean isNotEmpty(KafkaTrackingToken token) {
        return !isEmpty(token);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KafkaTrackingToken that = (KafkaTrackingToken) o;
        return Objects.equals(partitionPositions, that.partitionPositions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionPositions);
    }

    @Override
    public String toString() {
        return "KafkaTrackingToken{" +
                "partitionPositions=" + partitionPositions +
                '}';
    }
}
