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

import org.springframework.beans.factory.annotation.Autowired;
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
import org.miaixz.bus.core.basic.spring.Controller;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.*;
import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.transport.mvc.MvcErrorAdvice;

/**
 * Global exception handling advice class.
 * <p>
 * This class centralizes exception handling across the entire application, intercepting exceptions thrown by
 * controllers and returning a consistent, structured error response to the client. It improves user experience and
 * simplifies debugging by providing a unified error handling mechanism.
 * </p>
 *
 * @author Kimi Liu
 */
@ControllerAdvice
@RestControllerAdvice
@ConditionalOnWebApplication
public class BaseAdvice extends Controller {

    /**
     * Uses the application error boundary when registered, including in inherited Advice classes.
     */
    private ErrorBoundary errorBoundary = ErrorBoundary.standard();

    /**
     * Supplies an optional shared application error boundary.
     *
     * @param boundary classifier and observer used by this entry point
     */
    @Autowired(required = false)
    public void setErrorBoundary(ErrorBoundary boundary) {
        this.errorBoundary = java.util.Objects.requireNonNull(boundary, "boundary");
    }

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
        return this.handle(e, Exception.class);
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
        return this.handle(e, InternalException.class);
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
        return this.handle(e, BusinessException.class);
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
        return this.handle(e, CrontabException.class);
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
        return this.handle(e, ValidateException.class);
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
        return this.handle(e, AuthorizedException.class);
    }

    /**
     * Handles {@link ForbiddenException}, which indicates that access to a resource or operation is forbidden.
     *
     * @param e the caught forbidden-access failure
     * @return a unified forbidden response object
     */
    @ResponseBody
    @ExceptionHandler(value = ForbiddenException.class)
    public Message<Void> forbiddenException(ForbiddenException e) {
        return this.handle(e, ForbiddenException.class);
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
        return this.handle(e, SignatureException.class);
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
        return this.handle(e, UncheckedException.class);
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
        return this.handle(e, RelevantException.class);
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
        return this.handle(e, HttpRequestMethodNotSupportedException.class);
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
        return this.handle(e, HttpMediaTypeNotSupportedException.class);
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
        return this.handle(e, NoHandlerFoundException.class);
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
        return this.handle(e, MethodArgumentNotValidException.class);
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
        return this.handle(e, BindException.class);
    }

    /**
     * Delegates the MVC exception contract selected by Spring while retaining overridable hooks and response writers.
     *
     * @param failure  exception passed to the selected handler
     * @param selected exception type declared by the selected handler
     * @return legacy MVC response with the original application code and description
     */
    private Message<Void> handle(Exception failure, Class<? extends Exception> selected) {
        return MvcErrorAdvice.handle(
                this.errorBoundary, failure, selected, this::defaultExceptionHandler, this::write, this::write, this::write);
    }

    /**
     * Delegates to the legacy {@link ErrorAdvice} service through the shared callback boundary.
     *
     * @param ex the original exception
     */
    public void defaultExceptionHandler(Exception ex) {
        MvcErrorAdvice.invokeLegacyService(() -> Instances.singletion(ErrorAdvice.class).handler(ex));
    }

}
