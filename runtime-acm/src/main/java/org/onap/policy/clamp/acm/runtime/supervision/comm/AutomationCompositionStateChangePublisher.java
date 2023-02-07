/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021-2023 Nordix Foundation.
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

package org.onap.policy.clamp.acm.runtime.supervision.comm;

import io.micrometer.core.annotation.Timed;
import java.util.UUID;
import org.onap.policy.clamp.models.acm.concepts.AutomationComposition;
import org.onap.policy.clamp.models.acm.messages.dmaap.participant.AutomationCompositionStateChange;
import org.onap.policy.clamp.models.acm.messages.rest.instantiation.DeployOrder;
import org.onap.policy.clamp.models.acm.messages.rest.instantiation.LockOrder;
import org.springframework.stereotype.Component;

/**
 * This class is used to send AutomationCompositionStateChangePublisher messages to participants on DMaaP.
 */
@Component
public class AutomationCompositionStateChangePublisher
        extends AbstractParticipantPublisher<AutomationCompositionStateChange> {

    /**
     * Send undeploy message to to Participant.
     *
     * @param automationComposition the AutomationComposition
     * @param startPhase the startPhase
     */
    @Timed(
            value = "publisher.automation_composition_state_change",
            description = "AUTOMATION_COMPOSITION_STATE_CHANGE messages published")
    public void undeploy(AutomationComposition automationComposition, int startPhase, boolean firstStartPhase) {
        send(automationComposition, startPhase, firstStartPhase, DeployOrder.UNDEPLOY, LockOrder.NONE);
    }

    /**
     * Send unlock message to to Participant.
     *
     * @param automationComposition the AutomationComposition
     * @param startPhase the startPhase
     */
    @Timed(
            value = "publisher.automation_composition_state_change",
            description = "AUTOMATION_COMPOSITION_STATE_CHANGE messages published")
    public void unlock(AutomationComposition automationComposition, int startPhase, boolean firstStartPhase) {
        send(automationComposition, startPhase, firstStartPhase, DeployOrder.NONE, LockOrder.UNLOCK);
    }

    /**
     * Send lock message to to Participant.
     *
     * @param automationComposition the AutomationComposition
     * @param startPhase the startPhase
     */
    @Timed(
            value = "publisher.automation_composition_state_change",
            description = "AUTOMATION_COMPOSITION_STATE_CHANGE messages published")
    public void lock(AutomationComposition automationComposition, int startPhase, boolean firstStartPhase) {
        send(automationComposition, startPhase, firstStartPhase, DeployOrder.NONE, LockOrder.LOCK);
    }

    /**
     * Send undeploy message to to Participant.
     *
     * @param automationComposition the AutomationComposition
     * @param startPhase the startPhase
     * @param deployOrder the DeployOrder
     * @param lockOrder the LockOrder
     */
    private void send(AutomationComposition automationComposition, int startPhase, boolean firstStartPhase,
            DeployOrder deployOrder, LockOrder lockOrder) {
        var acsc = new AutomationCompositionStateChange();
        acsc.setCompositionId(automationComposition.getCompositionId());
        acsc.setAutomationCompositionId(automationComposition.getInstanceId());
        acsc.setMessageId(UUID.randomUUID());
        acsc.setDeployOrderedState(deployOrder);
        acsc.setLockOrderedState(lockOrder);
        acsc.setStartPhase(startPhase);
        acsc.setFirstStartPhase(firstStartPhase);

        super.send(acsc);
    }

    /**
     * Send AutomationCompositionStateChange to Participant.
     *
     * @param automationComposition the AutomationComposition
     * @param startPhase the startPhase
     */
    @Timed(
            value = "publisher.automation_composition_state_change",
            description = "AUTOMATION_COMPOSITION_STATE_CHANGE messages published")
    public void send(AutomationComposition automationComposition, int startPhase) {
        var acsc = new AutomationCompositionStateChange();
        acsc.setCompositionId(automationComposition.getCompositionId());
        acsc.setAutomationCompositionId(automationComposition.getInstanceId());
        acsc.setMessageId(UUID.randomUUID());
        acsc.setOrderedState(automationComposition.getOrderedState());
        acsc.setStartPhase(startPhase);

        super.send(acsc);
    }
}
