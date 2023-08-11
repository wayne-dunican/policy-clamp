/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 Nordix Foundation.
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

package org.onap.policy.clamp.common.acm.exception;

import jakarta.ws.rs.core.Response;
import java.io.Serial;
import lombok.Getter;
import lombok.ToString;
import org.onap.policy.models.errors.concepts.ErrorResponse;
import org.onap.policy.models.errors.concepts.ErrorResponseInfo;
import org.onap.policy.models.errors.concepts.ErrorResponseUtils;

/**
 * This class is a base automation composition run time exception from which all automation composition run time
 * exceptions are sub classes.
 */
@Getter
@ToString
public class AutomationCompositionRuntimeException extends RuntimeException implements ErrorResponseInfo {

    @Serial
    private static final long serialVersionUID = -8507246953751956974L;

    // The error response of the exception
    private final ErrorResponse errorResponse = new ErrorResponse();

    // The object on which the exception was thrown
    private final transient Object object;

    /**
     * Instantiates a new automation composition runtime exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     */
    public AutomationCompositionRuntimeException(final Response.Status statusCode, final String message) {
        this(statusCode, message, null);
    }

    /**
     * Instantiates a new automation composition runtime exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     * @param object the object that the exception was thrown on
     */
    public AutomationCompositionRuntimeException(final Response.Status statusCode, final String message,
        final Object object) {
        super(message);
        this.object = object;
        errorResponse.setResponseCode(statusCode);
        ErrorResponseUtils.getExceptionMessages(errorResponse, this);
    }

    /**
     * Instantiates a new automation composition runtime exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     * @param exception the exception that caused this automation composition exception
     */
    public AutomationCompositionRuntimeException(final Response.Status statusCode, final String message,
        final Exception exception) {
        this(statusCode, message, exception, null);
    }

    /**
     * Instantiates a new model runtime exception from an AutomationCompositionException instance.
     *
     * @param exception the exception that caused this automation composition exception
     */
    public AutomationCompositionRuntimeException(final AutomationCompositionException exception) {
        super(exception.getMessage(), exception);
        this.object = exception.getObject();
        errorResponse.setResponseCode(exception.getErrorResponse().getResponseCode());
        ErrorResponseUtils.getExceptionMessages(errorResponse, this);
    }

    /**
     * Instantiates a new automation composition runtime exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     * @param exception the exception that caused this automation composition exception
     * @param object the object that the exception was thrown on
     */
    public AutomationCompositionRuntimeException(final Response.Status statusCode, final String message,
        final Exception exception, final Object object) {
        super(message, exception);
        this.object = object;
        errorResponse.setResponseCode(statusCode);
        ErrorResponseUtils.getExceptionMessages(errorResponse, this);
    }
}
