/*
 * Copyright 2016 Red Hat Inc.
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
package net.kiar.collectorr.connector.mqtt.mapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kiar.collectorr.config.model.TopicConfig;

/**
 *
 * @author ranSprd
 */
/**
 * Provides matching feature between "wildcarded" topics (for subscription) with
 * a fixed topic (for publishing)
 */
public class TopicMatcher {

    private final List<TopicCache> cache;

    private TopicMatcher(List<TopicCache> cache) {
        this.cache = cache;
    }

    public static TopicMatcher getMatcherFor(List<TopicConfig> topics) {
        if (topics == null) {
            return new TopicMatcher(List.of());
        }

        // build a pre-compiled list of topics and remember the mapping to the topic
        List<TopicCache> preCompiled = topics.stream().map(topic -> TopicCache.buildPattern(topic))
                .filter(pattern -> pattern.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toList());

        return new TopicMatcher(preCompiled);
    }

    public Optional<TopicProcessor> find(String topic) {

        if (topic == null || topic.isBlank()) {
            return Optional.empty();
        }

        return cache.stream()
                .filter(preCompiled -> preCompiled.isMatch(topic))
                .map(preCompiled -> preCompiled.getTopicProcessor())
                .findAny();
    }

}
