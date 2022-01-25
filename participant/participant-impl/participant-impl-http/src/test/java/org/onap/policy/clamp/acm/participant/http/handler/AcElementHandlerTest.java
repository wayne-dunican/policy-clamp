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

package org.onap.policy.clamp.acm.participant.http.handler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.policy.clamp.acm.participant.http.main.handler.AutomationCompositionElementHandler;
import org.onap.policy.clamp.acm.participant.http.main.models.ConfigRequest;
import org.onap.policy.clamp.acm.participant.http.utils.CommonTestData;
import org.onap.policy.clamp.acm.participant.http.utils.ToscaUtils;
import org.onap.policy.clamp.acm.participant.intermediary.api.ParticipantIntermediaryApi;
import org.onap.policy.clamp.models.acm.concepts.AutomationCompositionElement;
import org.onap.policy.clamp.models.acm.concepts.AutomationCompositionOrderedState;
import org.onap.policy.clamp.models.acm.concepts.AutomationCompositionState;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class AcElementHandlerTest {

    @InjectMocks
    @Spy
    private AutomationCompositionElementHandler automationCompositionElementHandler =
        new AutomationCompositionElementHandler();

    @Mock
    private ParticipantIntermediaryApi participantIntermediaryApi;

    private CommonTestData commonTestData = new CommonTestData();

    private static ToscaServiceTemplate serviceTemplate;
    private static final String HTTP_AUTOMATION_COMPOSITION_ELEMENT =
        "org.onap.domain.database.Http_PMSHMicroserviceAutomationCompositionElement";

    @BeforeAll
    static void init() throws CoderException {
        serviceTemplate = ToscaUtils.readAutomationCompositionFromTosca();
    }

    @Test
    void test_automationCompositionElementeStateChange() throws IOException {
        var automationCompositionId = commonTestData.getAutomationCompositionId();
        var element = commonTestData.getAutomationCompositionElement();
        var automationCompositionElementId = element.getId();

        var config = Mockito.mock(ConfigRequest.class);
        assertDoesNotThrow(() -> automationCompositionElementHandler.invokeHttpClient(config));

        assertDoesNotThrow(() -> automationCompositionElementHandler.automationCompositionElementStateChange(
            automationCompositionId, automationCompositionElementId, AutomationCompositionState.PASSIVE,
            AutomationCompositionOrderedState.PASSIVE));

        assertDoesNotThrow(() -> automationCompositionElementHandler.automationCompositionElementStateChange(
            automationCompositionId, automationCompositionElementId, AutomationCompositionState.PASSIVE,
            AutomationCompositionOrderedState.UNINITIALISED));

        assertDoesNotThrow(() -> automationCompositionElementHandler.automationCompositionElementStateChange(
            automationCompositionId, automationCompositionElementId, AutomationCompositionState.PASSIVE,
            AutomationCompositionOrderedState.RUNNING));

        automationCompositionElementHandler.close();
    }

    @Test
    void test_AutomationCompositionElementUpdate() throws Exception {
        doNothing().when(automationCompositionElementHandler).invokeHttpClient(any());
        AutomationCompositionElement element = commonTestData.getAutomationCompositionElement();

        Map<String, ToscaNodeTemplate> nodeTemplatesMap = serviceTemplate.getToscaTopologyTemplate().getNodeTemplates();

        assertDoesNotThrow(() -> automationCompositionElementHandler.automationCompositionElementUpdate(
            commonTestData.getAutomationCompositionId(), element,
            nodeTemplatesMap.get(HTTP_AUTOMATION_COMPOSITION_ELEMENT)));
    }
}
