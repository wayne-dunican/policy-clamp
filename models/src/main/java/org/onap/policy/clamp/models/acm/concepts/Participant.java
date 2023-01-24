/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021-2023 Nordix Foundation.
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

package org.onap.policy.clamp.models.acm.concepts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Class to represent details of a running participant instance.
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class Participant {

    @NonNull
    private UUID participantId;

    @NonNull
    private ParticipantState participantState = ParticipantState.ON_LINE;

    @NonNull
    private ToscaConceptIdentifier participantType = new ToscaConceptIdentifier();

    @NonNull
    private Map<UUID, ParticipantSupportedElementType> participantSupportedElementTypes = new HashMap<>();

    /**
     * Copy constructor.
     *
     * @param otherParticipant the participant to copy from
     */
    public Participant(Participant otherParticipant) {
        this.participantState = otherParticipant.participantState;
        this.participantType = otherParticipant.participantType;
        this.participantId = otherParticipant.participantId;
        this.participantSupportedElementTypes = PfUtils.mapMap(otherParticipant.getParticipantSupportedElementTypes(),
                ParticipantSupportedElementType::new);
    }
}
