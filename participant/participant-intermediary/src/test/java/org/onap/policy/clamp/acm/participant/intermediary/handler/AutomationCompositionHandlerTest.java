/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2022 Nordix Foundation.
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

package org.onap.policy.clamp.acm.participant.intermediary.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onap.policy.clamp.acm.participant.intermediary.api.AutomationCompositionElementListener;
import org.onap.policy.clamp.acm.participant.intermediary.main.parameters.CommonTestData;
import org.onap.policy.clamp.models.acm.concepts.AutomationCompositionElement;
import org.onap.policy.clamp.models.acm.concepts.AutomationCompositionElementDefinition;
import org.onap.policy.clamp.models.acm.concepts.AutomationCompositionOrderedState;
import org.onap.policy.clamp.models.acm.concepts.AutomationCompositionState;
import org.onap.policy.clamp.models.acm.concepts.ParticipantUpdates;
import org.onap.policy.clamp.models.acm.messages.dmaap.participant.AutomationCompositionStateChange;
import org.onap.policy.clamp.models.acm.messages.dmaap.participant.AutomationCompositionUpdate;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class AutomationCompositionHandlerTest {

    private final CommonTestData commonTestData = new CommonTestData();

    @Test
    void automationCompositionHandlerTest() {
        var ach = commonTestData.getMockAutomationCompositionHandler();
        assertNotNull(ach.getAutomationCompositionMap());
        assertNotNull(ach.getElementsOnThisParticipant());

        var elementId1 = UUID.randomUUID();
        var element = new AutomationCompositionElement();
        element.setId(elementId1);
        element.setDefinition(
                new ToscaConceptIdentifier("org.onap.policy.acm.PolicyAutomationCompositionParticipant", "1.0.1"));

        element.setOrderedState(AutomationCompositionOrderedState.PASSIVE);

        AutomationCompositionElementListener listener = mock(AutomationCompositionElementListener.class);
        ach.registerAutomationCompositionElementListener(listener);
        assertThat(ach.getListeners()).contains(listener);
    }

    @Test
    void updateNullAutomationCompositionHandlerTest() {
        var id = UUID.randomUUID();

        var ach = commonTestData.getMockAutomationCompositionHandler();
        assertNull(ach.updateAutomationCompositionElementState(null, null,
                AutomationCompositionOrderedState.UNINITIALISED, AutomationCompositionState.PASSIVE));

        assertNull(ach.updateAutomationCompositionElementState(null, id,
                AutomationCompositionOrderedState.UNINITIALISED, AutomationCompositionState.PASSIVE));
    }

    @Test
    void updateAutomationCompositionHandlerTest() throws CoderException {
        var uuid = UUID.randomUUID();
        var id = CommonTestData.getParticipantId();

        var ach = commonTestData.setTestAutomationCompositionHandler(id, uuid);
        var key = ach.getElementsOnThisParticipant().keySet().iterator().next();
        var value = ach.getElementsOnThisParticipant().get(key);
        assertEquals(AutomationCompositionState.UNINITIALISED, value.getState());
        ach.updateAutomationCompositionElementState(CommonTestData.AC_ID_1, uuid,
                AutomationCompositionOrderedState.UNINITIALISED, AutomationCompositionState.PASSIVE);
        assertEquals(AutomationCompositionState.PASSIVE, value.getState());

        ach.getAutomationCompositionMap().values().iterator().next().getElements().putIfAbsent(key, value);
        ach.updateAutomationCompositionElementState(CommonTestData.AC_ID_1, key,
                AutomationCompositionOrderedState.PASSIVE, AutomationCompositionState.RUNNING);
        assertEquals(AutomationCompositionState.RUNNING, value.getState());

        ach.getElementsOnThisParticipant().remove(key, value);
        ach.getAutomationCompositionMap().values().iterator().next().getElements().clear();
        assertNull(ach.updateAutomationCompositionElementState(CommonTestData.AC_ID_1, key,
                AutomationCompositionOrderedState.PASSIVE, AutomationCompositionState.RUNNING));

    }

    @Test
    void handleAutomationCompositionUpdateExceptionTest() throws CoderException {
        var uuid = UUID.randomUUID();
        var id = CommonTestData.getParticipantId();
        var stateChange = getStateChange(id, uuid, AutomationCompositionOrderedState.RUNNING);
        var ach = commonTestData.setTestAutomationCompositionHandler(id, uuid);
        assertDoesNotThrow(() -> ach
                .handleAutomationCompositionStateChange(mock(AutomationCompositionStateChange.class), List.of()));

        ach.handleAutomationCompositionStateChange(stateChange, List.of());
        var newid = new ToscaConceptIdentifier("id", "1.2.3");
        stateChange.setAutomationCompositionId(UUID.randomUUID());
        stateChange.setParticipantId(newid);
        assertDoesNotThrow(() -> ach.handleAutomationCompositionStateChange(stateChange, List.of()));

        var acd = new AutomationCompositionElementDefinition();
        acd.setAcElementDefinitionId(id);
        var updateMsg = new AutomationCompositionUpdate();
        updateMsg.setAutomationCompositionId(UUID.randomUUID());
        updateMsg.setMessageId(uuid);
        updateMsg.setParticipantId(id);
        updateMsg.setStartPhase(0);
        var acElementDefinitions = List.of(acd);
        assertDoesNotThrow(() -> ach.handleAutomationCompositionUpdate(updateMsg, acElementDefinitions));
        updateMsg.setStartPhase(1);
        assertDoesNotThrow(() -> ach.handleAutomationCompositionUpdate(updateMsg, acElementDefinitions));

        ach.getAutomationCompositionMap().clear();
        updateMsg.setStartPhase(0);
        assertDoesNotThrow(() -> ach.handleAutomationCompositionUpdate(updateMsg, acElementDefinitions));

        updateMsg.setAutomationCompositionId(UUID.randomUUID());
        updateMsg.setParticipantUpdatesList(List.of(mock(ParticipantUpdates.class)));
        assertDoesNotThrow(() -> ach.handleAutomationCompositionUpdate(updateMsg, acElementDefinitions));

        updateMsg.setStartPhase(1);
        var participantUpdate = new ParticipantUpdates();
        participantUpdate.setParticipantId(id);
        var element = new AutomationCompositionElement();
        element.setParticipantType(id);
        element.setDefinition(id);
        participantUpdate.setAutomationCompositionElementList(List.of(element));
        updateMsg.setParticipantUpdatesList(List.of(participantUpdate));

        var acd2 = new AutomationCompositionElementDefinition();
        acd2.setAcElementDefinitionId(id);
        acd2.setAutomationCompositionElementToscaNodeTemplate(mock(ToscaNodeTemplate.class));
        assertDoesNotThrow(() -> ach.handleAutomationCompositionUpdate(updateMsg, List.of(acd2)));

    }

    @Test
    void automationCompositionStateChangeUninitialisedTest() throws CoderException {
        var uuid = UUID.randomUUID();
        var id = CommonTestData.getParticipantId();

        var stateChangeUninitialised = getStateChange(id, uuid, AutomationCompositionOrderedState.UNINITIALISED);

        var ach = commonTestData.setTestAutomationCompositionHandler(id, uuid);
        ach.handleAutomationCompositionStateChange(stateChangeUninitialised, List.of());
        var newid = new ToscaConceptIdentifier("id", "1.2.3");
        stateChangeUninitialised.setAutomationCompositionId(UUID.randomUUID());
        stateChangeUninitialised.setParticipantId(newid);
        assertDoesNotThrow(() -> ach.handleAutomationCompositionStateChange(stateChangeUninitialised, List.of()));
    }

    @Test
    void automationCompositionStateChangePassiveTest() throws CoderException {
        var uuid = UUID.randomUUID();
        var id = CommonTestData.getParticipantId();

        var stateChangePassive = getStateChange(id, uuid, AutomationCompositionOrderedState.PASSIVE);

        var ach = commonTestData.setTestAutomationCompositionHandler(id, uuid);
        ach.handleAutomationCompositionStateChange(stateChangePassive, List.of());
        var newid = new ToscaConceptIdentifier("id", "1.2.3");
        stateChangePassive.setAutomationCompositionId(UUID.randomUUID());
        stateChangePassive.setParticipantId(newid);
        assertDoesNotThrow(() -> ach.handleAutomationCompositionStateChange(stateChangePassive, List.of()));
    }

    private AutomationCompositionStateChange getStateChange(ToscaConceptIdentifier id, UUID uuid,
            AutomationCompositionOrderedState state) {
        var stateChange = new AutomationCompositionStateChange();
        stateChange.setAutomationCompositionId(UUID.randomUUID());
        stateChange.setParticipantId(id);
        stateChange.setMessageId(uuid);
        stateChange.setOrderedState(state);
        stateChange.setCurrentState(AutomationCompositionState.UNINITIALISED);
        stateChange.setTimestamp(Instant.ofEpochMilli(3000));
        return stateChange;
    }

}
