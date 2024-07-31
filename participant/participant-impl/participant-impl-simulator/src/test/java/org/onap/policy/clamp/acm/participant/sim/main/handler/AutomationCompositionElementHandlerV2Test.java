/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023-2024 Nordix Foundation.
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.onap.policy.clamp.acm.participant.intermediary.api.CompositionDto;
import org.onap.policy.clamp.acm.participant.intermediary.api.CompositionElementDto;
import org.onap.policy.clamp.acm.participant.intermediary.api.InstanceElementDto;
import org.onap.policy.clamp.acm.participant.intermediary.api.ParticipantIntermediaryApi;
import org.onap.policy.clamp.acm.participant.sim.comm.CommonTestData;
import org.onap.policy.clamp.models.acm.concepts.AcTypeState;
import org.onap.policy.clamp.models.acm.concepts.DeployState;
import org.onap.policy.clamp.models.acm.concepts.LockState;
import org.onap.policy.clamp.models.acm.concepts.StateChangeResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

class AutomationCompositionElementHandlerV2Test {

    private static final CompositionElementDto COMPOSITION_ELEMENT =
            new CompositionElementDto(UUID.randomUUID(), new ToscaConceptIdentifier(), Map.of(), Map.of());
    private static final InstanceElementDto INSTANCE_ELEMENT =
            new InstanceElementDto(UUID.randomUUID(), UUID.randomUUID(), null, Map.of(), Map.of());
    private static final CompositionDto COMPOSITION = new CompositionDto(UUID.randomUUID(), Map.of(), Map.of());

    @Test
    void testDeploy() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        acElementHandler.deploy(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), DeployState.DEPLOYED,
                null, StateChangeResult.NO_ERROR, "Deployed");

        config.setDeploySuccess(false);
        acElementHandler.deploy(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), DeployState.UNDEPLOYED,
                null, StateChangeResult.FAILED, "Deploy failed!");
    }

    @Test
    void testUndeploy() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        acElementHandler.undeploy(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), DeployState.UNDEPLOYED,
                null, StateChangeResult.NO_ERROR, "Undeployed");

        config.setUndeploySuccess(false);
        acElementHandler.undeploy(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), DeployState.DEPLOYED,
                null, StateChangeResult.FAILED, "Undeploy failed!");
    }

    @Test
    void testLock() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        acElementHandler.lock(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), null, LockState.LOCKED,
                StateChangeResult.NO_ERROR, "Locked");

        config.setLockSuccess(false);
        acElementHandler.lock(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), null, LockState.UNLOCKED,
                StateChangeResult.FAILED, "Lock failed!");
    }

    @Test
    void testUnlock() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        acElementHandler.unlock(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), null, LockState.UNLOCKED,
                StateChangeResult.NO_ERROR, "Unlocked");

        config.setUnlockSuccess(false);
        acElementHandler.unlock(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), null, LockState.LOCKED,
                StateChangeResult.FAILED, "Unlock failed!");
    }

    @Test
    void testUpdate() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        var instanceElementUpdated = new InstanceElementDto(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), null,
                Map.of("key", "value"), Map.of());
        acElementHandler.update(COMPOSITION_ELEMENT, INSTANCE_ELEMENT, instanceElementUpdated);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                DeployState.DEPLOYED, null, StateChangeResult.NO_ERROR, "Updated");

        config.setUpdateSuccess(false);
        acElementHandler.update(COMPOSITION_ELEMENT, INSTANCE_ELEMENT, instanceElementUpdated);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                DeployState.DEPLOYED, null, StateChangeResult.FAILED, "Update failed!");
    }

    @Test
    void testDelete() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        acElementHandler.delete(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), DeployState.DELETED,
                null, StateChangeResult.NO_ERROR, "Deleted");

        config.setDeleteSuccess(false);
        acElementHandler.delete(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), DeployState.UNDEPLOYED,
                null, StateChangeResult.FAILED, "Delete failed!");
    }

    @Test
    void testPrime() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        acElementHandler.prime(COMPOSITION);
        verify(intermediaryApi).updateCompositionState(
                COMPOSITION.compositionId(), AcTypeState.PRIMED, StateChangeResult.NO_ERROR, "Primed");

        config.setPrimeSuccess(false);
        acElementHandler.prime(COMPOSITION);
        verify(intermediaryApi).updateCompositionState(
                COMPOSITION.compositionId(), AcTypeState.COMMISSIONED, StateChangeResult.FAILED, "Prime failed!");
    }

    @Test
    void testDeprime() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        acElementHandler.deprime(COMPOSITION);
        verify(intermediaryApi).updateCompositionState(
                COMPOSITION.compositionId(), AcTypeState.COMMISSIONED, StateChangeResult.NO_ERROR, "Deprimed");

        config.setDeprimeSuccess(false);
        acElementHandler.deprime(COMPOSITION);
        verify(intermediaryApi).updateCompositionState(
                COMPOSITION.compositionId(), AcTypeState.PRIMED, StateChangeResult.FAILED, "Deprime failed!");
    }

    @Test
    void testMigrate() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        var compositionElementTarget = new CompositionElementDto(UUID.randomUUID(), new ToscaConceptIdentifier(),
                Map.of(), Map.of());
        var instanceElementMigrated = new InstanceElementDto(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                null, Map.of("key", "value"), Map.of());
        acElementHandler
            .migrate(COMPOSITION_ELEMENT, compositionElementTarget, INSTANCE_ELEMENT, instanceElementMigrated);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                DeployState.DEPLOYED, null, StateChangeResult.NO_ERROR, "Migrated");

        config.setMigrateSuccess(false);
        acElementHandler
            .migrate(COMPOSITION_ELEMENT, compositionElementTarget, INSTANCE_ELEMENT, instanceElementMigrated);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                DeployState.DEPLOYED, null, StateChangeResult.FAILED, "Migrate failed!");
    }

    @Test
    void testMigrateAdd() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        var compoElTargetAdd = new CompositionElementDto(UUID.randomUUID(), new ToscaConceptIdentifier(),
                Map.of(), Map.of(), true, false);
        var inElMigratedAdd = new InstanceElementDto(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                null, Map.of("key", "value"), Map.of(), true, false);
        acElementHandler
                .migrate(COMPOSITION_ELEMENT, compoElTargetAdd, INSTANCE_ELEMENT, inElMigratedAdd);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                DeployState.DEPLOYED, null, StateChangeResult.NO_ERROR, "Migrated");
    }

    @Test
    void testMigrateRemove() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);

        var compoElTargetRemove = new CompositionElementDto(UUID.randomUUID(), new ToscaConceptIdentifier(),
                Map.of(), Map.of(), false, true);
        var inElMigratedRemove = new InstanceElementDto(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                null, Map.of("key", "value"), Map.of(), false, true);
        acElementHandler
                .migrate(COMPOSITION_ELEMENT, compoElTargetRemove, INSTANCE_ELEMENT, inElMigratedRemove);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                DeployState.UNDEPLOYED, null, StateChangeResult.NO_ERROR, "Undeployed");
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                DeployState.DELETED, null, StateChangeResult.NO_ERROR, "Deleted");
    }

    @Test
    void testMigratePrecheck() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        var compositionElementTarget = new CompositionElementDto(UUID.randomUUID(), new ToscaConceptIdentifier(),
                Map.of(), Map.of());
        var instanceElementMigrated = new InstanceElementDto(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                null, Map.of("key", "value"), Map.of());
        acElementHandler.migratePrecheck(COMPOSITION_ELEMENT, compositionElementTarget,
                INSTANCE_ELEMENT, instanceElementMigrated);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                DeployState.DEPLOYED, null,
                StateChangeResult.NO_ERROR, "Migration precheck completed");

        config.setMigratePrecheck(false);
        acElementHandler.migratePrecheck(COMPOSITION_ELEMENT, compositionElementTarget,
                INSTANCE_ELEMENT, instanceElementMigrated);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(),
                DeployState.DEPLOYED, null,
                StateChangeResult.FAILED, "Migration precheck failed");
    }

    @Test
    void testPrepare() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        acElementHandler.prepare(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), DeployState.UNDEPLOYED,
                null, StateChangeResult.NO_ERROR, "Prepare completed");

        config.setPrepare(false);
        acElementHandler.prepare(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), DeployState.UNDEPLOYED,
                null, StateChangeResult.FAILED, "Prepare failed");
    }

    @Test
    void testReview() {
        var config = CommonTestData.createSimConfig();
        var intermediaryApi = mock(ParticipantIntermediaryApi.class);
        var simulatorService = new SimulatorService(intermediaryApi);
        var acElementHandler = new AutomationCompositionElementHandlerV2(intermediaryApi, simulatorService);
        simulatorService.setConfig(config);
        acElementHandler.review(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), DeployState.DEPLOYED,
                null, StateChangeResult.NO_ERROR, "Review completed");

        config.setReview(false);
        acElementHandler.review(COMPOSITION_ELEMENT, INSTANCE_ELEMENT);
        verify(intermediaryApi).updateAutomationCompositionElementState(
                INSTANCE_ELEMENT.instanceId(), INSTANCE_ELEMENT.elementId(), DeployState.DEPLOYED,
                null, StateChangeResult.FAILED, "Review failed");
    }
}
