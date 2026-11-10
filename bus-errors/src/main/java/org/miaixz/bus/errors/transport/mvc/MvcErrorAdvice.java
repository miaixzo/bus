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
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.exception.*;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.errors.*;
import org.miaixz.bus.errors.transport.http.PublicHttpError;
import org.miaixz.bus.errors.transport.servlet.ServletErrorFilter;
import org.miaixz.bus.errors.transport.servlet.ServletErrorHandler;

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
        var operation = ServletErrorHandler.operation(boundary, currentRequest(null));
        var decision = operation.decide(CONTEXT, facts(failure), failure);
        ErrorBoundary.runCallback(CONTEXT, "mvc-error-advice", requestId, () -> legacyHook.accept(failure));
        return operation.complete(CONTEXT, decision, failure, requestId, () -> {
            resetAndApprove();
            if (selected != Exception.class && failure instanceof UncheckedException legacy
                    && StringKit.isNotBlank(legacy.getCode())) {
                var error = PublicHttpError.resolve(legacy.getCode(), DEFAULTS.get(selected));
                return selected == BusinessException.class ? codeWriter.apply(error.code())
                        : detailWriter.apply(error.code(), error.description());
            }
            if (selected == RelevantException.class && failure instanceof RelevantException legacy
                    && StringKit.isNotBlank(legacy.getCode())) {
                var error = PublicHttpError.resolve(legacy.getCode(), DEFAULTS.get(selected));
                return detailWriter.apply(error.code(), error.description());
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
     * @param failure   original application exception
     * @param renderer  existing response mapping
     * @param <T>       application response body type
     * @return existing response with its verified status and headers
     */
    public static <T> ResponseEntity<T> handleResponse(
            String component,
            Exception failure,
            Supplier<ResponseEntity<T>> renderer) {
        return handleResponse(component, failure, null, renderer, ignored -> {
        });
    }

    /**
     * Handles a controller-local error and keeps its existing response mapping and optional direct write.
     *
     * @param component stable owner of the application error contract
     * @param failure   original application exception
     * @param request   current servlet request, or null to use the bound request
     * @param renderer  existing response mapping
     * @param writer    existing direct response write
     * @param <T>       application response body type
     * @param <X>       checked write exception preserved for MVC
     * @return existing response with its verified status and headers
     * @throws X if the direct response write fails
     */
    public static <T, X extends Exception> ResponseEntity<T> handleResponse(
            String component,
            Exception failure,
            HttpServletRequest request,
            Supplier<ResponseEntity<T>> renderer,
            ResponseWriter<T, X> writer) throws X {
        var context = new ErrorContext(component, "http", ErrorContext.Phase.OPEN);
        var operation = ServletErrorHandler.operation(ErrorBoundary.standard(), currentRequest(request));
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
            resetAndApprove();
            writer.write(response);
            return response;
        });
    }

    /**
     * Obtains correlation from the servlet container without making response handling depend on it.
     *
     * @param request current request, or null to use the bound request
     * @return server-generated identifier, or null
     */
    static String requestId(HttpServletRequest request) {
        try {
            if (request == null
                    && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes current) {
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

    /**
     * Renders a legacy business failure whose wire contract uses an HTTP success status.
     *
     * @param component   stable owner of the business error contract
     * @param failure     original failure for diagnostic observation
     * @param publicError already projected public code and description
     * @param renderer    existing business envelope renderer
     * @param <T>         legacy response body type
     * @return safe response in the existing business format
     */
    public static <T> T handleBusiness(
            String component,
            Exception failure,
            PublicHttpError publicError,
            Function<PublicHttpError, T> renderer) {
        var context = new ErrorContext(component, "http", ErrorContext.Phase.OPEN);
        var operation = ServletErrorHandler.operation(ErrorBoundary.standard(), currentRequest(null));
        var decision = operation.decide(context, FailureFacts.UNKNOWN, failure);
        return operation.complete(context, decision, failure, requestId(null), () -> {
            approve();
            return renderer.apply(publicError);
        });
    }

    /**
     * Provides a final safe MVC error after specific controller and advice handlers had an opportunity to resolve it.
     *
     * @param request  active Servlet request
     * @param response active Servlet response
     * @param handler  selected MVC handler, if any
     * @param failure  unhandled MVC failure
     * @return resolved empty view, or null after commitment or a failed connection write
     */
    public static ModelAndView resolveFallback(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception failure) {
        if (response.isCommitted()) {
            return null;
        }
        var mapping = AnnotatedElementUtils.findMergedAnnotation(failure.getClass(), ResponseStatus.class);
        int status = mapping == null || !mapping.code().isError() ? 500 : mapping.code().value();
        HttpHeaders headers = HttpHeaders.EMPTY;
        if (failure instanceof ErrorResponse error && error.getStatusCode().isError()) {
            status = error.getStatusCode().value();
            headers = error.getHeaders();
        }
        try {
            response.reset();
            ServletErrorHandler.fallback(request, response, status, failure, headers);
            return new ModelAndView();
        } catch (java.io.IOException disconnected) {
            return null;
        }
    }

    /**
     * Discards diagnostic headers or buffered content before a replacement error response is prepared.
     */
    private static void resetAndApprove() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes current
                && current.getResponse() != null && !current.getResponse().isCommitted()) {
            current.getResponse().reset();
            ServletErrorFilter.approve(current.getResponse());
        }
    }

    /**
     * Approves a compatibility response when Spring writes it through the current Servlet exchange.
     */
    private static void approve() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes current
                && current.getResponse() != null) {
            ServletErrorFilter.approve(current.getResponse());
        }
    }

    /**
     * Resolves the Servlet request supplied by the caller or bound to the current MVC thread.
     *
     * @param request explicit request, when available
     * @return active request, or null outside MVC dispatch
     */
    private static HttpServletRequest currentRequest(HttpServletRequest request) {
        return request == null
                && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes current
                        ? current.getRequest()
                        : request;
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

}
