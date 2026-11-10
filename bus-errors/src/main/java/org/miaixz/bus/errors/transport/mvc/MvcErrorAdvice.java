/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.errors.transport.mvc;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.NoHandlerFoundException;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.lang.exception.BusinessException;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.lang.exception.ForbiddenException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.RelevantException;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.errors.*;

/**
 * Implements the legacy mappings selected by the existing BaseAdvice methods.
 *
 * @author Kimi Liu
 */
public final class MvcErrorAdvice {

    /**
     * Stable label and phase for legacy MVC advice observations.
     */
    private static final ErrorContext CONTEXT = new ErrorContext("mvc", "http", ErrorContext.Phase.OPEN);
    /**
     * Default legacy response codes indexed by the exception signature selected by Spring.
     */
    private static final Map<Class<? extends Exception>, Errors> DEFAULTS = Map.ofEntries(
            Map.entry(Exception.class, ErrorCode._FAILURE),
            Map.entry(InternalException.class, ErrorCode._100805),
            Map.entry(BusinessException.class, ErrorCode._100807),
            Map.entry(CrontabException.class, ErrorCode._100808),
            Map.entry(ValidateException.class, ErrorCode._100805),
            Map.entry(AuthorizedException.class, ErrorCode._100806),
            Map.entry(ForbiddenException.class, ErrorCode._403),
            Map.entry(SignatureException.class, ErrorCode._100109),
            Map.entry(UncheckedException.class, ErrorCode._100805),
            Map.entry(RelevantException.class, ErrorCode._100805),
            Map.entry(HttpRequestMethodNotSupportedException.class, ErrorCode._100802),
            Map.entry(HttpMediaTypeNotSupportedException.class, ErrorCode._100803),
            Map.entry(NoHandlerFoundException.class, ErrorCode._100804),
            Map.entry(MethodArgumentNotValidException.class, ErrorCode._100809),
            Map.entry(BindException.class, ErrorCode._100809));

    /**
     * Prevents construction of the legacy MVC advice utility.
     */
    private MvcErrorAdvice() {
    }

    /**
     * Executes the mapping selected by Spring while preserving legacy hooks and virtual writer overloads.
     *
     * @param boundary     shared classifier and observer for MVC
     * @param failure      exception supplied by the selected handler
     * @param selected     signature of the handler that Spring selected
     * @param legacyHook   existing ErrorService hook
     * @param errorWriter  writer for registered defaults
     * @param codeWriter   writer that resolves a code through the legacy registry
     * @param detailWriter writer for an explicit legacy code and description
     * @param <T>          existing response type
     * @return legacy response
     */
    public static <T> T handle(
            ErrorBoundary boundary,
            Exception failure,
            Class<? extends Exception> selected,
            Consumer<Exception> legacyHook,
            Function<Errors, T> errorWriter,
            Function<String, T> codeWriter,
            BiFunction<String, String, T> detailWriter) {
        String requestId = requestId(null);
        var operation = boundary.open();
        var decision = operation.decide(CONTEXT, facts(failure), failure);
        ErrorBoundary.runCallback(CONTEXT, "mvc-error-advice", requestId, () -> legacyHook.accept(failure));
        return operation.complete(CONTEXT, decision, failure, requestId, () -> {
            if (selected != Exception.class && failure instanceof UncheckedException legacy
                    && StringKit.isNotBlank(legacy.getCode())) {
                return selected == BusinessException.class ? codeWriter.apply(legacy.getCode())
                        : detailWriter.apply(legacy.getCode(), legacy.getDesc());
            }
            if (selected == RelevantException.class && failure instanceof RelevantException legacy
                    && StringKit.isNotBlank(legacy.getCode())) {
                return detailWriter.apply(legacy.getCode(), legacy.getDesc());
            }
            return errorWriter.apply(DEFAULTS.get(selected));
        });
    }

    /**
     * Runs the legacy service callback with the same isolation for direct calls and MVC dispatch.
     *
     * @param callback legacy error service invocation
     */
    public static void invokeLegacyService(Runnable callback) {
        ErrorBoundary.runCallback(CONTEXT, "mvc-error-advice", requestId(null), callback);
    }

    /**
     * Handles a controller-local MVC error whose return value is written by Spring.
     *
     * @param component stable owner of the application error contract
     * @param failure original application exception
     * @param renderer existing response mapping
     * @param <T> application response body type
     * @return existing response with its verified status and headers
     */
    public static <T> ResponseEntity<T> handleResponse(String component, Exception failure,
            Supplier<ResponseEntity<T>> renderer) {
        return handleResponse(component, failure, null, renderer, ignored -> {
        });
    }

    /**
     * Handles a controller-local error and keeps its existing response mapping and optional direct write.
     *
     * @param component stable owner of the application error contract
     * @param failure original application exception
     * @param request current servlet request, or null to use the bound request
     * @param renderer existing response mapping
     * @param writer existing direct response write
     * @param <T> application response body type
     * @param <X> checked write exception preserved for MVC
     * @return existing response with its verified status and headers
     * @throws X if the direct response write fails
     */
    public static <T, X extends Exception> ResponseEntity<T> handleResponse(String component, Exception failure,
            HttpServletRequest request, Supplier<ResponseEntity<T>> renderer, ResponseWriter<T, X> writer) throws X {
        var context = new ErrorContext(component, "http", ErrorContext.Phase.OPEN);
        var operation = ErrorBoundary.standard().open();
        String requestId = requestId(request);
        ResponseEntity<T> response;
        try {
            response = renderer.get();
        } catch (RuntimeException | Error secondary) {
            var decision = operation.decide(context, facts(failure), failure);
            ErrorBoundary.diagnose(context, "error-response", secondary, requestId);
            operation.observe(context, decision, true, failure, requestId);
            throw secondary;
        }
        var verified = new FailureFacts(FailureFacts.Source.LOCAL, null, response.getStatusCode().value());
        var decision = operation.decide(context, verified, failure);
        return operation.complete(context, decision, failure, requestId, () -> {
            writer.write(response);
            return response;
        });
    }

    /**
     * Writes a previously selected application error response through its servlet or protocol contract.
     *
     * @param <T> response body type
     * @param <X> checked write exception
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface ResponseWriter<T, X extends Exception> {

        /**
         * Writes the selected response.
         *
         * @param response existing application response
         * @throws X if the response cannot be written
         */
        void write(ResponseEntity<T> response) throws X;

    }

    /**
     * Obtains correlation from the servlet container without making response handling depend on it.
     *
     * @param request current request, or null to use the bound request
     * @return server-generated identifier, or null
     */
    static String requestId(HttpServletRequest request) {
        try {
            if (request == null && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes current) {
                request = current.getRequest();
            }
            return request == null ? null : request.getRequestId();
        } catch (RuntimeException | Error failure) {
            ErrorBoundary.diagnose(CONTEXT, "mvc-request-id", failure, null);
            return null;
        }
    }

    /**
     * Extracts verified Spring status or a stable legacy failure type for MVC observation.
     *
     * @param failure exception received by the selected Advice method
     * @return verified failure facts, or unknown when no mapping applies
     */
    private static FailureFacts facts(Exception failure) {
        if (failure instanceof ErrorResponse response && response.getStatusCode().isError()) {
            return new FailureFacts(FailureFacts.Source.LOCAL, null, response.getStatusCode().value());
        }
        ErrorType type = switch (failure) {
            case AuthorizedException ignored -> ErrorType.UNAUTHENTICATED;
            case ForbiddenException ignored -> ErrorType.FORBIDDEN;
            case ValidateException ignored -> ErrorType.INVALID_REQUEST;
            case null, default -> null;
        };
        return type == null ? FailureFacts.UNKNOWN
                : new FailureFacts(FailureFacts.Source.LOCAL, ErrorEngine.descriptor(type), 0);
    }

}
