/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.clamp.controlloop.runtime.supervision.comm;

import java.util.List;
import org.onap.policy.clamp.controlloop.models.messages.dmaap.participant.ParticipantControlLoopStateChange;
import org.onap.policy.common.endpoints.event.comm.TopicSink;

/**
 * This class is used to send ParticipantControlLoopStateChangePublisher messages to participants on DMaaP.
 */
public class ParticipantControlLoopStateChangePublisher
        extends AbstractParticipantPublisher<ParticipantControlLoopStateChange> {

    /**
     * Constructor for instantiating ParticipantControlLoopStateChangePublisherPublisher.
     *
     * @param topicSinks the topic sinks
     * @param interval time interval to send ParticipantControlLoopStateChangePublisher messages
     */
    public ParticipantControlLoopStateChangePublisher(final List<TopicSink> topicSinks, final long interval) {
        super(topicSinks, interval);
    }
}
