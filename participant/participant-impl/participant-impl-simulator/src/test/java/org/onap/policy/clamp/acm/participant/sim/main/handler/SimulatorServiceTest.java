/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Nordix Foundation.
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

package org.onap.policy.clamp.acm.participant.sim.main.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.onap.policy.clamp.acm.participant.intermediary.api.ParticipantIntermediaryApi;
import org.onap.policy.clamp.acm.participant.sim.comm.CommonTestData;
import org.onap.policy.clamp.models.acm.concepts.AutomationCompositionElementDefinition;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;

class SimulatorServiceTest {

    @Test
    void testGetAutomationCompositions() {
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);

        var map = CommonTestData.getTestAutomationCompositionMap();
        when(intermediaryApi.getAutomationCompositions()).thenReturn(map);
        var result = simulatorService.getAutomationCompositions();
        assertEquals(map.values().iterator().next(), result.getAutomationCompositionList().get(0));
    }

    @Test
    void testGetAutomationComposition() {
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);

        var instance = CommonTestData.getTestAutomationCompositionMap().values().iterator().next();
        when(intermediaryApi.getAutomationComposition(instance.getInstanceId())).thenReturn(instance);
        var result = simulatorService.getAutomationComposition(instance.getInstanceId());
        assertEquals(instance, result);
    }

    @Test
    void testSetOutProperties() {
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);

        var instanceId = UUID.randomUUID();
        var elementId = UUID.randomUUID();
        var useState = "useState";
        var operationalState = "operationalState";
        Map<String, Object> map = Map.of("id", "1234");

        simulatorService.setOutProperties(instanceId, elementId, useState, operationalState, map);
        verify(intermediaryApi).sendAcElementInfo(instanceId, elementId, useState, operationalState, map);
    }

    @Test
    void testGetDataList() {
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);

        var map = CommonTestData.getTestAutomationCompositionMap();
        when(intermediaryApi.getAutomationCompositions()).thenReturn(map);
        var result = simulatorService.getDataList();
        var data = result.getList().get(0);
        var automationcomposition = map.values().iterator().next();
        assertEquals(automationcomposition.getInstanceId(), data.getAutomationCompositionId());
        var element = automationcomposition.getElements().values().iterator().next();
        assertEquals(element.getId(), data.getAutomationCompositionElementId());
    }

    @Test
    void testGetCompositionDataList() {
        var acElementDefinition = new AutomationCompositionElementDefinition();
        var toscaConceptIdentifier = new ToscaConceptIdentifier("code", "1.0.0");
        acElementDefinition.setAcElementDefinitionId(toscaConceptIdentifier);
        acElementDefinition.setAutomationCompositionElementToscaNodeTemplate(new ToscaNodeTemplate());
        Map<String, Object> outProperties = Map.of("code", "value");
        Map<String, Object> inProperties = Map.of("key", "value");
        acElementDefinition.getAutomationCompositionElementToscaNodeTemplate().setProperties(inProperties);
        acElementDefinition.setOutProperties(outProperties);
        var elementsDefinitions = Map.of(toscaConceptIdentifier, acElementDefinition);
        var compositionId = UUID.randomUUID();
        var map = Map.of(compositionId, elementsDefinitions);
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        when(intermediaryApi.getAcElementsDefinitions()).thenReturn(map);
        var simulatorService = new SimulatorService(intermediaryApi);

        var result = simulatorService.getCompositionDataList();
        assertThat(result.getList()).hasSize(1);
        assertEquals(result.getList().get(0).getCompositionId(), compositionId);
        assertEquals(result.getList().get(0).getCompositionDefinitionElementId(), toscaConceptIdentifier);
        assertEquals(result.getList().get(0).getOutProperties(), outProperties);
        assertEquals(result.getList().get(0).getIntProperties(), inProperties);
    }

    @Test
    void testSetCompositionData() {
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);

        var compositionId = UUID.randomUUID();
        simulatorService.setCompositionOutProperties(compositionId, null, Map.of());
        verify(intermediaryApi).sendAcDefinitionInfo(compositionId, null, Map.of());
    }
}
