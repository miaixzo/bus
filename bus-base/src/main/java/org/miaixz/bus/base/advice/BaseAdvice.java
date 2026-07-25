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
package org.miaixz.bus.base.advice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import org.miaixz.bus.core.basic.advice.ErrorAdvice;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.spring.Controller;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.*;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Global exception handling advice class.
 * <p>
 * This class centralizes exception handling across the entire application, intercepting exceptions thrown by
 * controllers and returning a consistent, structured error response to the client. It improves user experience and
 * simplifies debugging by providing a unified error handling mechanism.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ControllerAdvice
@RestControllerAdvice
@ConditionalOnWebApplication
public class BaseAdvice extends Controller {

    /**
     * Constructs a new BaseAdvice instance.
     */
    public BaseAdvice() {
        // No initialization required.
    }

    /**
     * Initializes a data binder for all controller methods.
     * <p>
     * This method is called before any @RequestMapping method to allow for custom property editors, validators, etc.,
     * to be registered. Currently, it is empty but can be implemented as needed.
     * </p>
     *
     * @param binder the data binder to initialize
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Can be used to register custom formatters, e.g., binder.registerCustomEditor(Date.class, new
        // CustomDateEditor(...));
    }

    /**
     * Adds attributes to the model that will be available to all @RequestMapping methods globally.
     * <p>
     * This method can be used to populate the model with common data, such as user information or application settings.
     * Currently, it is empty but can be implemented as needed.
     * </p>
     *
     * @param model the Model object to add attributes to
     */
    @ModelAttribute
    public void addAttributes(Model model) {
        // Can be used to add global attributes, e.g., model.addAttribute("currentUser", getCurrentUser());
    }

    /**
     * Handles all uncaught exceptions that are not specifically handled by other methods in this class.
     * <p>
     * This is the fallback handler, ensuring that any unexpected exception results in a generic error response rather
     * than a server error page.
     * </p>
     *
     * @param e the caught exception
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Message<Void> defaultException(Exception e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._FAILURE);
    }

    /**
     * Handles {@link InternalException}, which typically indicates a system-level or internal logic error.
     *
     * @param e the caught InternalException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = InternalException.class)
    public Message<Void> internalException(InternalException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100805);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Handles {@link BusinessException}, which represents errors in business logic.
     * <p>
     * This exception typically triggers a transaction rollback.
     * </p>
     *
     * @param e the caught BusinessException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public Message<Void> businessException(BusinessException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100807);
        }
        return write(e.getErrcode());
    }

    /**
     * Handles {@link CrontabException}, which occurs during the execution of scheduled tasks.
     *
     * @param e the caught CrontabException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = CrontabException.class)
    public Message<Void> crontabException(CrontabException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100808);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Handles {@link ValidateException}, which is thrown when data validation fails.
     *
     * @param e the caught ValidateException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = ValidateException.class)
    public Message<Void> validateException(ValidateException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100805);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Handles {@link AuthorizedException}, which is thrown when an authorization check fails.
     *
     * @param e the caught AuthorizedException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = AuthorizedException.class)
    public Message<Void> authorizedException(AuthorizedException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100806);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Handles {@link SignatureException}, which is thrown when a request signature (e.g., API signature) is invalid.
     *
     * @param e the caught SignatureException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = SignatureException.class)
    public Message<Void> signatureException(SignatureException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100109);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Handles {@link UncheckedException}, a wrapper for runtime exceptions that need to be handled in a specific way.
     *
     * @param e the caught UncheckedException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = UncheckedException.class)
    public Message<Void> uncheckedException(UncheckedException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100805);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Handles {@link RelevantException}, often used for I/O or other external system-related errors.
     *
     * @param e the caught RelevantException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = RelevantException.class)
    public Message<Void> relevantException(RelevantException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100805);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Handles {@link HttpRequestMethodNotSupportedException}, which occurs when the HTTP method is not supported for
     * the requested endpoint.
     *
     * @param e the caught HttpRequestMethodNotSupportedException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public Message<Void> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._100802);
    }

    /**
     * Handles {@link HttpMediaTypeNotSupportedException}, which occurs when the request's Content-Type is not
     * supported.
     *
     * @param e the caught HttpMediaTypeNotSupportedException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public Message<Void> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._100803);
    }

    /**
     * Handles {@link NoHandlerFoundException}, which occurs when no controller method is found for the requested URL.
     *
     * @param e the caught NoHandlerFoundException
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = NoHandlerFoundException.class)
    public Message<Void> noHandlerFoundException(NoHandlerFoundException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._100804);
    }

    /**
     * Request-body validation exception handler.
     *
     * @param e the caught validation exception
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Message<Void> handleBodyValidException(MethodArgumentNotValidException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._100809);
    }

    /**
     * Request-parameter binding exception handler.
     *
     * @param e the caught binding exception
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(BindException.class)
    public Message<Void> handleBindException(BindException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._100809);
    }

    /**
     * A default exception handler that logs the exception and delegates to a custom {@link ErrorAdvice} service.
     * <p>
     * This method is called by all specific exception handlers to ensure consistent logging and to allow for pluggable
     * error handling logic. It is wrapped in a try-catch block to prevent failures in the error handling logic itself
     * from causing a cascade of errors.
     * </p>
     *
     * @param ex the exception to handle
     */
    public void defaultExceptionHandler(Exception ex) {
        try {
            Logger.error(
                    false,
                    "Base",
                    "Default exception handler captured error: exceptionType={}, status=failure, error={}",
                    ex == null ? null : ex.getClass().getName(),
                    ex == null ? null : ex.getMessage(),
                    ex);
            Instances.singletion(ErrorAdvice.class).handler(ex);
        } catch (RuntimeException ignore) {
            // Prevents the exception handler itself from crashing the application.
            Logger.error(
                    false,
                    "Base",
                    "Default exception handler failed while delegating error advice: handler={}, status=failure, error={}",
                    "ErrorAdvice",
                    ignore.getMessage(),
                    ignore);
        }
    }

}
