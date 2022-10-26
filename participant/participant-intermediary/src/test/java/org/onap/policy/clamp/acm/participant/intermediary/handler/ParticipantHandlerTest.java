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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.onap.policy.clamp.acm.participant.intermediary.comm.ParticipantMessagePublisher;
import org.onap.policy.clamp.acm.participant.intermediary.main.parameters.CommonTestData;
import org.onap.policy.clamp.models.acm.concepts.ParticipantDefinition;
import org.onap.policy.clamp.models.acm.concepts.ParticipantHealthStatus;
import org.onap.policy.clamp.models.acm.concepts.ParticipantState;
import org.onap.policy.clamp.models.acm.messages.dmaap.participant.ParticipantAckMessage;
import org.onap.policy.clamp.models.acm.messages.dmaap.participant.ParticipantMessage;
import org.onap.policy.clamp.models.acm.messages.dmaap.participant.ParticipantMessageType;
import org.onap.policy.clamp.models.acm.messages.dmaap.participant.ParticipantRegisterAck;
import org.onap.policy.clamp.models.acm.messages.dmaap.participant.ParticipantUpdate;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

class ParticipantHandlerTest {

    private final CommonTestData commonTestData = new CommonTestData();
    private static final String ID_NAME = "org.onap.PM_CDS_Blueprint";
    private static final String ID_VERSION = "1.0.1";

    @Test
    void mockParticipantHandlerTest() {
        var participantHandler = commonTestData.getMockParticipantHandler();
        assertNull(participantHandler.getParticipant(null, null));
        assertEquals("org.onap.PM_CDS_Blueprint 1.0.1", participantHandler.getParticipantId().toString());

        var id = new ToscaConceptIdentifier(ID_NAME, ID_VERSION);
        assertEquals(id, participantHandler.getParticipantId());
        assertEquals(id, participantHandler.getParticipantType());
        assertThat(participantHandler.getAcElementDefinitionCommonProperties(id)).isEmpty();

    }

    @Test
    void handleUpdateTest() {
        var parameters = CommonTestData.getParticipantParameters();
        var automationCompositionHander = commonTestData.getMockAutomationCompositionHandler();
        var publisher = new ParticipantMessagePublisher();
        var emptyParticipantHandler =
                new ParticipantHandler(parameters, publisher, automationCompositionHander);
        var participantUpdateMsg = new ParticipantUpdate();

        assertThatThrownBy(() ->
                emptyParticipantHandler.handleParticipantUpdate(participantUpdateMsg))
                .isInstanceOf(RuntimeException.class);

        var participantHandler = commonTestData.getMockParticipantHandler();

        var id = new ToscaConceptIdentifier(ID_NAME, ID_VERSION);
        participantUpdateMsg.setAutomationCompositionId(id);
        participantUpdateMsg.setParticipantId(id);
        participantUpdateMsg.setParticipantType(id);
        participantUpdateMsg.setMessageId(UUID.randomUUID());
        participantUpdateMsg.setTimestamp(Instant.ofEpochMilli(3000));

        var heartbeatF = participantHandler.makeHeartbeat(false);
        assertEquals(id, heartbeatF.getParticipantId());
        assertThat(heartbeatF.getAutomationCompositionInfoList()).isEmpty();

        participantHandler.handleParticipantUpdate(participantUpdateMsg);
        assertThat(participantHandler.getAcElementDefinitionCommonProperties(id)).isEmpty();

        var heartbeatT = participantHandler.makeHeartbeat(true);
        assertEquals(id, heartbeatT.getParticipantId());
        assertThat(heartbeatT.getParticipantDefinitionUpdates()).isNotEmpty();
        assertEquals(id, heartbeatT.getParticipantDefinitionUpdates().get(0).getParticipantId());

        var pum = setListParticipantDefinition(participantUpdateMsg);
        participantHandler.handleParticipantUpdate(pum);
        var heartbeatTAfterUpdate = participantHandler.makeHeartbeat(true);
        assertEquals(id, heartbeatTAfterUpdate.getParticipantId());
    }

    private ParticipantUpdate setListParticipantDefinition(ParticipantUpdate participantUpdateMsg) {
        var id = new ToscaConceptIdentifier(ID_NAME, ID_VERSION);
        List<ParticipantDefinition> participantDefinitionUpdates = new ArrayList<>();
        var def = new ParticipantDefinition();
        def.setParticipantId(id);
        def.setParticipantType(id);
        participantDefinitionUpdates.add(def);
        participantUpdateMsg.setParticipantDefinitionUpdates(participantDefinitionUpdates);
        return participantUpdateMsg;
    }

    @Test
    void handleParticipantTest() {
        var participantHandler = commonTestData.getMockParticipantHandler();
        var id = new ToscaConceptIdentifier(ID_NAME, ID_VERSION);
        var p = participantHandler.getParticipant(id.getName(), id.getVersion());
        assertEquals(ParticipantState.UNKNOWN, p.getParticipantState());

        participantHandler.updateParticipantState(id, ParticipantState.PASSIVE);
        var p2 = participantHandler.getParticipant(id.getName(), id.getVersion());
        assertEquals(ParticipantState.PASSIVE, p2.getParticipantState());

        var participantRegisterAckMsg = new ParticipantRegisterAck();
        participantRegisterAckMsg.setState(ParticipantState.TERMINATED);
        participantHandler.handleParticipantRegisterAck(participantRegisterAckMsg);
        assertEquals(ParticipantHealthStatus.HEALTHY, participantHandler.makeHeartbeat(false).getHealthStatus());

        var emptyid = new ToscaConceptIdentifier("", ID_VERSION);
        assertNull(participantHandler.updateParticipantState(emptyid, ParticipantState.PASSIVE));

        var sameid = new ToscaConceptIdentifier(ID_NAME, ID_VERSION);
        var participant = participantHandler.updateParticipantState(sameid, ParticipantState.PASSIVE);
        assertEquals(participant.getDefinition(), sameid);

    }

    @Test
    void checkAppliesTo() {
        var participantHandler = commonTestData.getMockParticipantHandler();
        var participantAckMsg =
                new ParticipantAckMessage(ParticipantMessageType.AUTOMATION_COMPOSITION_UPDATE);
        assertTrue(participantHandler.appliesTo(participantAckMsg));

        var participantMsg =
                new ParticipantMessage(ParticipantMessageType.PARTICIPANT_STATUS);
        assertTrue(participantHandler.appliesTo(participantMsg));

        var emptyid = new ToscaConceptIdentifier("", ID_VERSION);
        participantMsg.setParticipantType(emptyid);
        assertFalse(participantHandler.appliesTo(participantMsg));

    }

    @Test
    void getAutomationCompositionInfoListTest() throws CoderException {
        var participantHandler = commonTestData.getParticipantHandlerAutomationCompositions();
        var id = new ToscaConceptIdentifier(ID_NAME, ID_VERSION);
        participantHandler.sendHeartbeat();
        assertEquals(id, participantHandler.makeHeartbeat(false)
                .getAutomationCompositionInfoList()
                .get(0)
                .getAutomationCompositionId());

    }

}
