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

package org.onap.policy.clamp.controlloop.common.exception;

import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.ToString;
import org.onap.policy.models.errors.concepts.ErrorResponse;
import org.onap.policy.models.errors.concepts.ErrorResponseInfo;
import org.onap.policy.models.errors.concepts.ErrorResponseUtils;

/**
 * This class is a base exception from which all control loop exceptions are sub classes.
 */
@Getter
@ToString
public class ControlLoopException extends Exception implements ErrorResponseInfo {
    private static final long serialVersionUID = -8507246953751956974L;

    // The error response of the exception
    private final ErrorResponse errorResponse = new ErrorResponse();

    // The object on which the exception was thrown
    private final transient Object object;

    /**
     * Instantiates a new control loop exception.
     *
     * @param statusCode the status code for the response as a HTTP status code
     * @param message the message on the exception
     */
    public ControlLoopException(final Response.Status statusCode, final String message) {
        this(statusCode, message, null);
    }

    /**
     * Instantiates a new control loop exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     * @param object the object that the exception was thrown on
     */
    public ControlLoopException(final Response.Status statusCode, final String message, final Object object) {
        super(message);
        errorResponse.setResponseCode(statusCode);
        ErrorResponseUtils.getExceptionMessages(errorResponse, this);
        this.object = object;
    }

    /**
     * Instantiates a new control loop exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     * @param exception the exception that caused this exception
     */
    public ControlLoopException(final Response.Status statusCode, final String message, final Exception exception) {
        this(statusCode, message, exception, null);
    }

    /**
     * Instantiates a new exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     * @param exception the exception that caused this exception
     * @param object the object that the exception was thrown on
     */
    public ControlLoopException(final Response.Status statusCode, final String message, final Exception exception,
            final Object object) {
        super(message, exception);
        errorResponse.setResponseCode(statusCode);
        ErrorResponseUtils.getExceptionMessages(errorResponse, this);
        this.object = object;
    }
}
