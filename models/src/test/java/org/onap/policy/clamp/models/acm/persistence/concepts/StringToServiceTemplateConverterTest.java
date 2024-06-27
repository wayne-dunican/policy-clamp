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

package org.onap.policy.clamp.models.acm.persistence.concepts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.onap.policy.clamp.models.acm.document.concepts.DocToscaServiceTemplate;
import org.onap.policy.clamp.models.acm.utils.CommonTestData;
import org.onap.policy.models.base.PfModelRuntimeException;

class StringToServiceTemplateConverterTest {

    private static final String TOSCA_SERVICE_TEMPLATE_YAML_PROP =
            "clamp/acm/test/tosca-template-additional-properties.yaml";

    @Test
    void testConvert() {
        var inputServiceTemplateProperties = CommonTestData.getToscaServiceTemplate(TOSCA_SERVICE_TEMPLATE_YAML_PROP);
        var docServiceTemplate = new DocToscaServiceTemplate(inputServiceTemplateProperties);
        var stringToServiceTemplateConverter = new StringToServiceTemplateConverter();
        var dbData  = stringToServiceTemplateConverter.convertToDatabaseColumn(docServiceTemplate);
        var result = stringToServiceTemplateConverter.convertToEntityAttribute(dbData);
        assertThat(docServiceTemplate.compareTo(result)).isEqualByComparingTo(0);
    }

    @Test
    void testNull() {
        var stringToServiceTemplateConverter = new StringToServiceTemplateConverter();
        var dbData = stringToServiceTemplateConverter.convertToDatabaseColumn(null);
        assertThat(dbData).isNull();
        var docServiceTemplate = stringToServiceTemplateConverter.convertToEntityAttribute(null);
        assertThat(docServiceTemplate).isNotNull();
        docServiceTemplate = stringToServiceTemplateConverter.convertToEntityAttribute("");
        assertThat(docServiceTemplate).isNull();
        assertThatThrownBy(() -> stringToServiceTemplateConverter.convertToEntityAttribute("1"))
                .isInstanceOf(PfModelRuntimeException.class);
    }
}
