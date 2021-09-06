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

package org.onap.policy.clamp.controlloop.participant.dcae.main.rest;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ControlLoop;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ControlLoopElement;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ControlLoopElementDefinition;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ControlLoopOrderedState;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ControlLoopState;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ParticipantDefinition;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ParticipantUpdates;
import org.onap.policy.clamp.controlloop.models.messages.dmaap.participant.ControlLoopStateChange;
import org.onap.policy.clamp.controlloop.models.messages.dmaap.participant.ControlLoopUpdate;
import org.onap.policy.clamp.controlloop.models.messages.dmaap.participant.ParticipantUpdate;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

public class TestListenerUtils {

    private static final YamlJsonTranslator yamlTranslator = new YamlJsonTranslator();
    private static final Coder CODER = new StandardCoder();
    private static final String TOSCA_TEMPLATE_YAML = "examples/controlloop/PMSubscriptionHandling.yaml";
    private static final String CONTROL_LOOP_ELEMENT = "org.onap.policy.clamp.controlloop.ControlLoopElement";

    /**
     * Method to create a controlLoop from a yaml file.
     *
     * @return ControlLoop controlloop
     */
    public static ControlLoop createControlLoop() {
        ControlLoop controlLoop = new ControlLoop();
        Map<UUID, ControlLoopElement> elements = new LinkedHashMap<>();
        ToscaServiceTemplate toscaServiceTemplate = testControlLoopRead();
        Map<String, ToscaNodeTemplate> nodeTemplatesMap =
                toscaServiceTemplate.getToscaTopologyTemplate().getNodeTemplates();
        for (Map.Entry<String, ToscaNodeTemplate> toscaInputEntry : nodeTemplatesMap.entrySet()) {
            ControlLoopElement clElement = new ControlLoopElement();
            clElement.setId(UUID.randomUUID());

            ToscaConceptIdentifier clElementParticipantId = new ToscaConceptIdentifier();
            clElementParticipantId.setName(toscaInputEntry.getKey());
            clElementParticipantId.setVersion(toscaInputEntry.getValue().getVersion());
            clElement.setParticipantId(clElementParticipantId);

            clElement.setDefinition(clElementParticipantId);
            clElement.setState(ControlLoopState.UNINITIALISED);
            clElement.setDescription(toscaInputEntry.getValue().getDescription());
            clElement.setOrderedState(ControlLoopOrderedState.UNINITIALISED);
            elements.put(clElement.getId(), clElement);
        }
        controlLoop.setElements(elements);
        controlLoop.setName("PMSHInstance0");
        controlLoop.setVersion("1.0.0");

        ToscaConceptIdentifier definition = new ToscaConceptIdentifier();
        definition.setName("PMSHInstance0");
        definition.setVersion("1.0.0");
        controlLoop.setDefinition(definition);

        return controlLoop;
    }

    /**
     * Method to create ControlLoopStateChange message from the arguments passed.
     *
     * @param controlLoopOrderedState controlLoopOrderedState
     *
     * @return ControlLoopStateChange message
     */
    public static ControlLoopStateChange createControlLoopStateChangeMsg(
            final ControlLoopOrderedState controlLoopOrderedState) {
        final ControlLoopStateChange clStateChangeMsg = new ControlLoopStateChange();

        ToscaConceptIdentifier controlLoopId = new ToscaConceptIdentifier();
        controlLoopId.setName("PMSHInstance0");
        controlLoopId.setVersion("1.0.0");

        ToscaConceptIdentifier participantId = new ToscaConceptIdentifier();
        participantId.setName("DCAEParticipant0");
        participantId.setVersion("1.0.0");

        clStateChangeMsg.setControlLoopId(controlLoopId);
        clStateChangeMsg.setParticipantId(participantId);
        clStateChangeMsg.setTimestamp(Instant.now());
        clStateChangeMsg.setOrderedState(controlLoopOrderedState);

        return clStateChangeMsg;
    }

    /**
     * Method to create ControlLoopUpdateMsg.
     *
     * @return ControlLoopUpdate message
     */
    public static ControlLoopUpdate createControlLoopUpdateMsg() {
        final ControlLoopUpdate clUpdateMsg = new ControlLoopUpdate();
        ToscaConceptIdentifier controlLoopId =
            new ToscaConceptIdentifier("PMSHInstance0", "1.0.0");
        ToscaConceptIdentifier participantId =
            new ToscaConceptIdentifier("org.onap.PM_Policy", "0.0.0");

        clUpdateMsg.setControlLoopId(controlLoopId);
        clUpdateMsg.setParticipantId(participantId);
        clUpdateMsg.setMessageId(UUID.randomUUID());
        clUpdateMsg.setTimestamp(Instant.now());

        Map<UUID, ControlLoopElement> elements = new LinkedHashMap<>();
        ToscaServiceTemplate toscaServiceTemplate = testControlLoopRead();
        Map<String, ToscaNodeTemplate> nodeTemplatesMap =
                toscaServiceTemplate.getToscaTopologyTemplate().getNodeTemplates();
        for (Map.Entry<String, ToscaNodeTemplate> toscaInputEntry : nodeTemplatesMap.entrySet()) {
            if (checkIfNodeTemplateIsControlLoopElement(toscaInputEntry.getValue(), toscaServiceTemplate)) {
                ControlLoopElement clElement = new ControlLoopElement();
                clElement.setId(UUID.randomUUID());
                ToscaConceptIdentifier clParticipantType;
                try {
                    clParticipantType = CODER.decode(
                            toscaInputEntry.getValue().getProperties().get("participantType").toString(),
                            ToscaConceptIdentifier.class);
                } catch (CoderException e) {
                    throw new RuntimeException("cannot get ParticipantType from toscaNodeTemplate", e);
                }

                clElement.setParticipantId(clParticipantType);
                clElement.setParticipantType(clParticipantType);

                clElement.setDefinition(new ToscaConceptIdentifier(toscaInputEntry.getKey(),
                    toscaInputEntry.getValue().getVersion()));
                clElement.setState(ControlLoopState.UNINITIALISED);
                clElement.setDescription(toscaInputEntry.getValue().getDescription());
                clElement.setOrderedState(ControlLoopOrderedState.PASSIVE);
                elements.put(clElement.getId(), clElement);
            }
        }

        List<ParticipantUpdates> participantUpdates = new ArrayList<>();
        for (ControlLoopElement element : elements.values()) {
            prepareParticipantUpdateForControlLoop(element, participantUpdates);
        }
        clUpdateMsg.setParticipantUpdatesList(participantUpdates);
        return clUpdateMsg;
    }

    private static boolean checkIfNodeTemplateIsControlLoopElement(ToscaNodeTemplate nodeTemplate,
            ToscaServiceTemplate toscaServiceTemplate) {
        if (nodeTemplate.getType().contains(CONTROL_LOOP_ELEMENT)) {
            return true;
        } else {
            var nodeType = toscaServiceTemplate.getNodeTypes().get(nodeTemplate.getType());
            if (nodeType != null) {
                var derivedFrom = nodeType.getDerivedFrom();
                if (derivedFrom != null) {
                    return derivedFrom.contains(CONTROL_LOOP_ELEMENT) ? true : false;
                }
            }
        }
        return false;
    }

    private static void prepareParticipantUpdateForControlLoop(ControlLoopElement clElement,
        List<ParticipantUpdates> participantUpdates) {
        if (participantUpdates.isEmpty()) {
            participantUpdates.add(getControlLoopElementList(clElement));
        } else {
            boolean participantExists = false;
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

    private static ParticipantUpdates getControlLoopElementList(ControlLoopElement clElement) {
        ParticipantUpdates participantUpdate = new ParticipantUpdates();
        List<ControlLoopElement> controlLoopElementList = new ArrayList<>();
        participantUpdate.setParticipantId(clElement.getParticipantId());
        controlLoopElementList.add(clElement);
        participantUpdate.setControlLoopElementList(controlLoopElementList);
        return participantUpdate;
    }

    /**
     * Method to create participantUpdateMsg.
     *
     * @return ParticipantUpdate message
     */
    public static ParticipantUpdate createParticipantUpdateMsg() {
        final ParticipantUpdate participantUpdateMsg = new ParticipantUpdate();
        ToscaConceptIdentifier participantId = new ToscaConceptIdentifier("org.onap.PM_Policy", "1.0.0");
        ToscaConceptIdentifier participantType = new ToscaConceptIdentifier(
                "org.onap.policy.controlloop.PolicyControlLoopParticipant", "2.3.1");

        participantUpdateMsg.setParticipantId(participantId);
        participantUpdateMsg.setTimestamp(Instant.now());
        participantUpdateMsg.setParticipantType(participantType);
        participantUpdateMsg.setTimestamp(Instant.ofEpochMilli(3000));
        participantUpdateMsg.setMessageId(UUID.randomUUID());

        ToscaServiceTemplate toscaServiceTemplate = testControlLoopRead();

        List<ParticipantDefinition> participantDefinitionUpdates = new ArrayList<>();
        for (Map.Entry<String, ToscaNodeTemplate> toscaInputEntry :
            toscaServiceTemplate.getToscaTopologyTemplate().getNodeTemplates().entrySet()) {
            if (checkIfNodeTemplateIsControlLoopElement(toscaInputEntry.getValue(), toscaServiceTemplate)) {
                ToscaConceptIdentifier clParticipantType;
                try {
                    clParticipantType = CODER.decode(
                            toscaInputEntry.getValue().getProperties().get("participantType").toString(),
                            ToscaConceptIdentifier.class);
                } catch (CoderException e) {
                    throw new RuntimeException("cannot get ParticipantType from toscaNodeTemplate", e);
                }
                prepareParticipantDefinitionUpdate(clParticipantType, toscaInputEntry.getKey(),
                    toscaInputEntry.getValue(), participantDefinitionUpdates);
            }
        }

        participantUpdateMsg.setParticipantDefinitionUpdates(participantDefinitionUpdates);
        return participantUpdateMsg;
    }

    private static void prepareParticipantDefinitionUpdate(ToscaConceptIdentifier clParticipantType, String entryKey,
        ToscaNodeTemplate entryValue, List<ParticipantDefinition> participantDefinitionUpdates) {

        var clDefinition = new ControlLoopElementDefinition();
        clDefinition.setClElementDefinitionId(new ToscaConceptIdentifier(
            entryKey, entryValue.getVersion()));
        clDefinition.setControlLoopElementToscaNodeTemplate(entryValue);
        List<ControlLoopElementDefinition> controlLoopElementDefinitionList = new ArrayList<>();

        if (participantDefinitionUpdates.isEmpty()) {
            participantDefinitionUpdates.add(getParticipantDefinition(clDefinition, clParticipantType,
                controlLoopElementDefinitionList));
        } else {
            boolean participantExists = false;
            for (ParticipantDefinition participantDefinitionUpdate : participantDefinitionUpdates) {
                if (participantDefinitionUpdate.getParticipantType().equals(clParticipantType)) {
                    participantDefinitionUpdate.getControlLoopElementDefinitionList().add(clDefinition);
                    participantExists = true;
                }
            }
            if (!participantExists) {
                participantDefinitionUpdates.add(getParticipantDefinition(clDefinition, clParticipantType,
                    controlLoopElementDefinitionList));
            }
        }
    }

    private static ParticipantDefinition getParticipantDefinition(ControlLoopElementDefinition clDefinition,
        ToscaConceptIdentifier clParticipantType,
        List<ControlLoopElementDefinition> controlLoopElementDefinitionList) {
        ParticipantDefinition participantDefinition = new ParticipantDefinition();
        participantDefinition.setParticipantType(clParticipantType);
        controlLoopElementDefinitionList.add(clDefinition);
        participantDefinition.setControlLoopElementDefinitionList(controlLoopElementDefinitionList);
        return participantDefinition;
    }

    public static ToscaConceptIdentifier getControlLoopId() {
        return new ToscaConceptIdentifier("PMSHInstance0", "1.0.0");
    }

    /**
     * Method to create a deep copy of ControlLoopUpdateMsg.
     *
     * @return ControlLoopUpdate message
     */
    public static ControlLoopUpdate createCopyControlLoopUpdateMsg(ControlLoopUpdate cpy) {
        return new ControlLoopUpdate(cpy);
    }

    /**
     * Method to create ControlLoopUpdate using the arguments passed.
     *
     * @param jsonFilePath the path of the controlloop content
     *
     * @return ControlLoopUpdate message
     * @throws CoderException exception while reading the file to object
     */
    public static ControlLoopUpdate createParticipantClUpdateMsgFromJson(String jsonFilePath)
            throws CoderException {
        ControlLoopUpdate controlLoopUpdateMsg =
                CODER.decode(new File(jsonFilePath), ControlLoopUpdate.class);
        return controlLoopUpdateMsg;
    }

    private static ToscaServiceTemplate testControlLoopRead() {
        return testControlLoopYamlSerialization(TOSCA_TEMPLATE_YAML);
    }

    private static ToscaServiceTemplate testControlLoopYamlSerialization(String controlLoopFilePath) {
        String controlLoopString = ResourceUtils.getResourceAsString(controlLoopFilePath);
        ToscaServiceTemplate serviceTemplate = yamlTranslator.fromYaml(controlLoopString, ToscaServiceTemplate.class);
        return serviceTemplate;
    }
}
