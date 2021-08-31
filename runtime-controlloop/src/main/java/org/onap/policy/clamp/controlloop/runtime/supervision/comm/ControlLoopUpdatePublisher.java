/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 Nordix Foundation.
 * ================================================================================
 * Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ControlLoop;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ControlLoopElement;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ParticipantUpdates;
import org.onap.policy.clamp.controlloop.models.messages.dmaap.participant.ControlLoopUpdate;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class is used to send ControlLoopUpdate messages to participants on DMaaP.
 */
@Component
@AllArgsConstructor
public class ControlLoopUpdatePublisher extends AbstractParticipantPublisher<ControlLoopUpdate> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlLoopUpdatePublisher.class);
    private static final String POLICY_TYPE_ID = "policy_type_id";
    private static final String POLICY_ID = "policy_id";
    private final PolicyModelsProvider modelsProvider;

    /**
     * Send ControlLoopUpdate to Participant.
     *
     * @param controlLoop the ControlLoop
     */
    public void send(ControlLoop controlLoop) {
        var controlLoopUpdateMsg = new ControlLoopUpdate();
        controlLoopUpdateMsg.setControlLoopId(controlLoop.getKey().asIdentifier());
        controlLoopUpdateMsg.setMessageId(UUID.randomUUID());
        controlLoopUpdateMsg.setTimestamp(Instant.now());
        ToscaServiceTemplate toscaServiceTemplate;
        try {
            toscaServiceTemplate = modelsProvider.getServiceTemplateList(null, null).get(0);
        } catch (PfModelException pfme) {
            LOGGER.warn("Get of tosca service template failed, cannot send participantupdate", pfme);
            return;
        }

        List<ParticipantUpdates> participantUpdates = new ArrayList<>();
        for (ControlLoopElement element : controlLoop.getElements().values()) {
            ToscaNodeTemplate toscaNodeTemplate = toscaServiceTemplate
                .getToscaTopologyTemplate().getNodeTemplates().get(element.getDefinition().getName());
            // If the ControlLoopElement has policy_type_id or policy_id, identify it as a PolicyControlLoopElement
            // and pass respective PolicyTypes or Policies as part of toscaServiceTemplateFragment
            if ((toscaNodeTemplate.getProperties().get(POLICY_TYPE_ID) != null)
                    || (toscaNodeTemplate.getProperties().get(POLICY_ID) != null)) {
                // ControlLoopElement for policy framework, send policies and policyTypes to participants
                if ((toscaServiceTemplate.getPolicyTypes() != null)
                        || (toscaServiceTemplate.getToscaTopologyTemplate().getPolicies() != null)) {
                    ToscaServiceTemplate toscaServiceTemplateFragment = new ToscaServiceTemplate();
                    toscaServiceTemplateFragment.setPolicyTypes(toscaServiceTemplate.getPolicyTypes());

                    ToscaTopologyTemplate toscaTopologyTemplate = new ToscaTopologyTemplate();
                    toscaTopologyTemplate.setPolicies(toscaServiceTemplate.getToscaTopologyTemplate().getPolicies());
                    toscaServiceTemplateFragment.setToscaTopologyTemplate(toscaTopologyTemplate);

                    toscaServiceTemplateFragment.setDataTypes(toscaServiceTemplate.getDataTypes());

                    element.setToscaServiceTemplateFragment(toscaServiceTemplateFragment);
                }
            }
            prepareParticipantUpdate(element, participantUpdates);
        }
        controlLoopUpdateMsg.setParticipantUpdatesList(participantUpdates);

        LOGGER.debug("ControlLoopUpdate message sent {}", controlLoopUpdateMsg);
        super.send(controlLoopUpdateMsg);
    }

    private void prepareParticipantUpdate(ControlLoopElement clElement,
        List<ParticipantUpdates> participantUpdates) {
        if (participantUpdates.isEmpty()) {
            participantUpdates.add(getControlLoopElementList(clElement));
        } else {
            var participantExists = false;
            for (ParticipantUpdates participantUpdate : participantUpdates) {
                if (participantUpdate.getParticipantId().equals(clElement.getParticipantId())) {
                    participantUpdate.getControlLoopElementList().add(clElement);
                    participantExists = true;
                }
            }
            if (!participantExists) {
                participantUpdates.add(getControlLoopElementList(clElement));
            }
        }
    }

    private ParticipantUpdates getControlLoopElementList(ControlLoopElement clElement) {
        var participantUpdate = new ParticipantUpdates();
        List<ControlLoopElement> controlLoopElementList = new ArrayList<>();
        participantUpdate.setParticipantId(clElement.getParticipantId());
        controlLoopElementList.add(clElement);
        participantUpdate.setControlLoopElementList(controlLoopElementList);
        return participantUpdate;
    }
}
