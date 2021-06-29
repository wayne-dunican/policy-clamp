/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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
import lombok.Getter;
import org.onap.policy.clamp.controlloop.models.messages.dmaap.participant.ParticipantMessage;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.client.TopicSinkClient;

public abstract class AbstractParticipantPublisher<E extends ParticipantMessage> {

    private final TopicSinkClient topicSinkClient;

    @Getter
    private final long intervalSec;

    /**
     * Constructor.
     *
     * @param topicSinks the topic sinks
     * @param intervalSec time interval to send ParticipantStateChange messages
     */
    protected AbstractParticipantPublisher(final List<TopicSink> topicSinks, long intervalSec) {
        this.topicSinkClient = new TopicSinkClient(topicSinks.get(0));
        this.intervalSec = intervalSec;
    }

    /**
     * Terminates the current timer.
     */
    public void terminate() {
        // Nothing to terminate, this publisher does not have a timer
    }

    /**
     * Method to send Participant message to participants on demand.
     *
     * @param participantMessage the Participant message
     */
    public void send(final E participantMessage) {
        topicSinkClient.send(participantMessage);
    }
}
