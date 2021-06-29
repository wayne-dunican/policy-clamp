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

package org.onap.policy.clamp.controlloop.participant.dcae.config;

import org.onap.policy.clamp.controlloop.participant.dcae.main.handler.ControlLoopElementHandler;
import org.onap.policy.clamp.controlloop.participant.intermediary.api.ParticipantIntermediaryApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParticipantConfig {

    /**
     * Register ControlLoopElementListener.
     *
     * @param intermediaryApi the ParticipantIntermediaryApi
     * @param clElementHandler the ControlLoop Element Handler
     */
    @Autowired
    public void registerControlLoopElementListener(ParticipantIntermediaryApi intermediaryApi,
            ControlLoopElementHandler clElementHandler) {
        intermediaryApi.registerControlLoopElementListener(clElementHandler);
        clElementHandler.setIntermediaryApi(intermediaryApi);
    }
}
