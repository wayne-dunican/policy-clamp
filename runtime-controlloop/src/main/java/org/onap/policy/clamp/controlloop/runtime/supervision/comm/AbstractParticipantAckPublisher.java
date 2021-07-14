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
import javax.ws.rs.core.Response.Status;
import org.onap.policy.clamp.controlloop.common.exception.ControlLoopRuntimeException;
import org.onap.policy.clamp.controlloop.models.messages.dmaap.participant.ParticipantAckMessage;
import org.onap.policy.clamp.controlloop.runtime.config.messaging.Publisher;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.client.TopicSinkClient;

public abstract class AbstractParticipantAckPublisher<E extends ParticipantAckMessage> implements Publisher {

    private TopicSinkClient topicSinkClient;
    private boolean active = false;

    /**
     * Method to send Participant message to participants on demand.
     *
     * @param participantMessage the Participant message
     */
    public void send(final E participantMessage) {
        if (!active) {
            throw new ControlLoopRuntimeException(Status.NOT_ACCEPTABLE, "Not Active!");
        }
        topicSinkClient.send(participantMessage);
    }


    @Override
    public void active(List<TopicSink> topicSinks) {
        if (topicSinks.size() != 1) {
            throw new IllegalArgumentException("Topic Sink must be one");
        }
        this.topicSinkClient = new TopicSinkClient(topicSinks.get(0));
        active = true;
    }

    @Override
    public void stop() {
        active = false;
    }
}
