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

package org.onap.policy.clamp.controlloop.participant.simulator.main.handler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ControlLoopElement;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ControlLoopOrderedState;
import org.onap.policy.clamp.controlloop.models.controlloop.concepts.ControlLoopState;
import org.onap.policy.clamp.controlloop.participant.intermediary.api.ParticipantIntermediaryApi;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

class ControlLoopElementHandlerTest {

    private static final String ID_NAME = "org.onap.PM_CDS_Blueprint";
    private static final String ID_VERSION = "1.0.1";
    private static final UUID controlLoopElementId = UUID.randomUUID();
    private static final ToscaConceptIdentifier controlLoopId = new ToscaConceptIdentifier(ID_NAME, ID_VERSION);

    @Test
    void testSimulatorHandlerExceptions() throws PfModelException {
        ControlLoopElementHandler handler = getTestingHandler();

        assertDoesNotThrow(() -> handler
                .controlLoopElementStateChange(controlLoopId,
                        controlLoopElementId,
                        ControlLoopState.UNINITIALISED,
                        ControlLoopOrderedState.PASSIVE));

        assertDoesNotThrow(() -> handler
                .controlLoopElementStateChange(controlLoopId,
                        controlLoopElementId,
                        ControlLoopState.RUNNING,
                        ControlLoopOrderedState.UNINITIALISED));

        assertDoesNotThrow(() -> handler
                .controlLoopElementStateChange(controlLoopId,
                        controlLoopElementId,
                        ControlLoopState.PASSIVE,
                        ControlLoopOrderedState.RUNNING));
        var element = getTestingClElement();
        var clElementDefinition = Mockito.mock(ToscaNodeTemplate.class);

        assertDoesNotThrow(() -> handler
                .controlLoopElementUpdate(controlLoopId, element, clElementDefinition));

        assertDoesNotThrow(() -> handler
                .handleStatistics(controlLoopElementId));
    }

    ControlLoopElementHandler getTestingHandler() {
        var handler = new ControlLoopElementHandler();
        var intermediaryApi = Mockito.mock(ParticipantIntermediaryApi.class);
        var element = getTestingClElement();
        when(intermediaryApi.getControlLoopElement(controlLoopElementId)).thenReturn(element);
        handler.setIntermediaryApi(intermediaryApi);
        return handler;
    }

    ControlLoopElement getTestingClElement() {
        var element = new ControlLoopElement();
        element.setDefinition(controlLoopId);
        element.setDescription("Description");
        element.setId(controlLoopElementId);
        element.setOrderedState(ControlLoopOrderedState.UNINITIALISED);
        element.setParticipantId(controlLoopId);
        element.setState(ControlLoopState.UNINITIALISED);
        var template = Mockito.mock(ToscaServiceTemplate.class);
        element.setToscaServiceTemplateFragment(template);
        return element;
    }

}
