/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022-2023 Nordix Foundation.
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

package org.onap.policy.clamp.acm.runtime.contract;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onap.policy.clamp.acm.runtime.util.rest.CommonRestController;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test", "stub" })
class CommissioningControllerStubTest extends CommonRestController {

    private static final String COMMISSIONING_ENDPOINT = "compositions";
    private static final String COMPOSITION_ID = "1aeed185-a98b-45b6-af22-8d5d20485ea3";
    private static ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();

    @LocalServerPort
    private int randomServerPort;

    @BeforeEach
    public void setUpPort() {
        super.setHttpPrefix(randomServerPort);
    }

    @Test
    void testQuery() {
        var invocationBuilder = super.sendRequest(COMMISSIONING_ENDPOINT);
        var respPost = invocationBuilder.get();
        assertThat(Response.Status.OK.getStatusCode()).isEqualTo(respPost.getStatus());
    }

    @Test
    void testGet() {
        var invocationBuilder = super.sendRequest(COMMISSIONING_ENDPOINT + "/" + COMPOSITION_ID);
        var respPost = invocationBuilder.get();
        assertThat(Response.Status.OK.getStatusCode()).isEqualTo(respPost.getStatus());
    }

    @Test
    void testPut() {
        var invocationBuilder = super.sendRequest(COMMISSIONING_ENDPOINT + "/" + COMPOSITION_ID);
        var respPost = invocationBuilder.put(Entity.json(serviceTemplate));
        assertThat(Response.Status.ACCEPTED.getStatusCode()).isEqualTo(respPost.getStatus());
    }

    @Test
    void testPost() {
        var invocationBuilder = super.sendRequest(COMMISSIONING_ENDPOINT);
        var respPost = invocationBuilder.post(Entity.json(serviceTemplate));
        assertThat(Response.Status.OK.getStatusCode()).isEqualTo(respPost.getStatus());
    }

    @Test
    void testDelete() {
        var invocationBuilder = super.sendRequest(COMMISSIONING_ENDPOINT + "/" + COMPOSITION_ID);
        var respPost = invocationBuilder.delete();
        assertThat(Response.Status.OK.getStatusCode()).isEqualTo(respPost.getStatus());
    }
}
