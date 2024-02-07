/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2023 Nordix Foundation.
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

package org.onap.policy.clamp.acm.participant.http.main.handler;

import jakarta.validation.Validation;
import jakarta.ws.rs.core.Response.Status;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.onap.policy.clamp.acm.participant.http.main.models.ConfigRequest;
import org.onap.policy.clamp.acm.participant.http.main.webclient.AcHttpClient;
import org.onap.policy.clamp.acm.participant.intermediary.api.AutomationCompositionElementListener;
import org.onap.policy.clamp.acm.participant.intermediary.api.ParticipantIntermediaryApi;
import org.onap.policy.clamp.common.acm.exception.AutomationCompositionException;
import org.onap.policy.clamp.models.acm.concepts.AcElementDeploy;
import org.onap.policy.clamp.models.acm.concepts.AcTypeState;
import org.onap.policy.clamp.models.acm.concepts.AutomationCompositionElementDefinition;
import org.onap.policy.clamp.models.acm.concepts.DeployState;
import org.onap.policy.clamp.models.acm.concepts.LockState;
import org.onap.policy.clamp.models.acm.concepts.StateChangeResult;
import org.onap.policy.clamp.models.acm.utils.AcmUtils;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.base.PfModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * This class handles implementation of automationCompositionElement updates.
 */
@Component
@RequiredArgsConstructor
public class AutomationCompositionElementHandler implements AutomationCompositionElementListener {

    private static final Coder CODER = new StandardCoder();

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ParticipantIntermediaryApi intermediaryApi;

    private final AcHttpClient acHttpClient;

    /**
     * Handle a automation composition element state change.
     *
     * @param automationCompositionElementId the ID of the automation composition element
     */
    @Override
    public void undeploy(UUID automationCompositionId, UUID automationCompositionElementId) {
        intermediaryApi.updateAutomationCompositionElementState(automationCompositionId, automationCompositionElementId,
                DeployState.UNDEPLOYED, null, StateChangeResult.NO_ERROR, "");
    }

    /**
     * Callback method to handle an update on a automation composition element.
     *
     * @param automationCompositionId the automationComposition Id
     * @param element the information on the automation composition element
     * @param properties properties Map
     * @throws PfModelException in case of a exception
     */
    @Override
    public void deploy(UUID automationCompositionId, AcElementDeploy element, Map<String, Object> properties)
            throws PfModelException {
        try {
            var configRequest = getConfigRequest(properties);
            var restResponseMap = acHttpClient.run(configRequest);
            var failedResponseStatus = restResponseMap.values().stream()
                    .filter(response -> !HttpStatus.valueOf(response.getKey()).is2xxSuccessful())
                    .toList();
            if (failedResponseStatus.isEmpty()) {
                intermediaryApi.updateAutomationCompositionElementState(automationCompositionId, element.getId(),
                        DeployState.DEPLOYED, null, StateChangeResult.NO_ERROR, "Deployed");
            } else {
                intermediaryApi.updateAutomationCompositionElementState(automationCompositionId, element.getId(),
                        DeployState.UNDEPLOYED, null, StateChangeResult.FAILED,
                        "Error on Invoking the http request: " + failedResponseStatus);
            }
        } catch (AutomationCompositionException e) {
            intermediaryApi.updateAutomationCompositionElementState(automationCompositionId, element.getId(),
                    DeployState.UNDEPLOYED, null, StateChangeResult.FAILED, e.getMessage());
        }
    }

    private ConfigRequest getConfigRequest(Map<String, Object> properties) throws AutomationCompositionException {
        try {
            var configRequest = CODER.convert(properties, ConfigRequest.class);
            var violations = Validation.buildDefaultValidatorFactory().getValidator().validate(configRequest);
            if (!violations.isEmpty()) {
                LOGGER.error("Violations found in the config request parameters: {}", violations);
                throw new AutomationCompositionException(Status.BAD_REQUEST,
                        "Constraint violations in the config request");
            }
            return configRequest;
        } catch (CoderException e) {
            throw new AutomationCompositionException(Status.BAD_REQUEST, "Error extracting ConfigRequest ", e);
        }
    }

    @Override
    public void lock(UUID instanceId, UUID elementId) throws PfModelException {
        intermediaryApi.updateAutomationCompositionElementState(instanceId, elementId, null, LockState.LOCKED,
                StateChangeResult.NO_ERROR, "Locked");
    }

    @Override
    public void unlock(UUID instanceId, UUID elementId) throws PfModelException {
        intermediaryApi.updateAutomationCompositionElementState(instanceId, elementId, null, LockState.UNLOCKED,
                StateChangeResult.NO_ERROR, "Unlocked");
    }

    @Override
    public void delete(UUID instanceId, UUID elementId) throws PfModelException {
        intermediaryApi.updateAutomationCompositionElementState(instanceId, elementId, DeployState.DELETED, null,
                StateChangeResult.NO_ERROR, "Deleted");
    }

    @Override
    public void update(UUID instanceId, AcElementDeploy element, Map<String, Object> properties)
            throws PfModelException {
        intermediaryApi.updateAutomationCompositionElementState(instanceId, element.getId(), DeployState.DEPLOYED, null,
                StateChangeResult.NO_ERROR, "Update not supported");
    }

    @Override
    public void prime(UUID compositionId, List<AutomationCompositionElementDefinition> elementDefinitionList)
            throws PfModelException {
        intermediaryApi.updateCompositionState(compositionId, AcTypeState.PRIMED, StateChangeResult.NO_ERROR, "Primed");
    }

    @Override
    public void deprime(UUID compositionId) throws PfModelException {
        intermediaryApi.updateCompositionState(compositionId, AcTypeState.COMMISSIONED, StateChangeResult.NO_ERROR,
                "Deprimed");
    }

    @Override
    public void handleRestartComposition(UUID compositionId,
            List<AutomationCompositionElementDefinition> elementDefinitionList, AcTypeState state)
            throws PfModelException {
        var finalState = AcTypeState.PRIMED.equals(state) || AcTypeState.PRIMING.equals(state) ? AcTypeState.PRIMED
                : AcTypeState.COMMISSIONED;
        intermediaryApi.updateCompositionState(compositionId, finalState, StateChangeResult.NO_ERROR, "Restarted");
    }

    @Override
    public void handleRestartInstance(UUID automationCompositionId, AcElementDeploy element,
            Map<String, Object> properties, DeployState deployState, LockState lockState) throws PfModelException {
        if (DeployState.DEPLOYING.equals(deployState)) {
            deploy(automationCompositionId, element, properties);
            return;
        }
        deployState = AcmUtils.deployCompleted(deployState);
        lockState = AcmUtils.lockCompleted(deployState, lockState);
        intermediaryApi.updateAutomationCompositionElementState(automationCompositionId, element.getId(), deployState,
                lockState, StateChangeResult.NO_ERROR, "Restarted");
    }

    @Override
    public void migrate(UUID automationCompositionId, AcElementDeploy element, UUID compositionTargetId,
            Map<String, Object> properties) throws PfModelException {
        intermediaryApi.updateAutomationCompositionElementState(automationCompositionId, element.getId(),
                DeployState.DEPLOYED, null, StateChangeResult.NO_ERROR, "Migrated");
    }
}
