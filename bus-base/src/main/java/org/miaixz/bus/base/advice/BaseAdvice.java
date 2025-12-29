/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.base.advice;

import org.miaixz.bus.core.basic.advice.ErrorAdvice;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.spring.Controller;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.*;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Global exception handling advice class.
 * <p>
 * This class centralizes exception handling across the entire application, intercepting exceptions thrown by
 * controllers and returning a consistent, structured error response to the client. It improves user experience and
 * simplifies debugging by providing a unified error handling mechanism.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ControllerAdvice
@RestControllerAdvice
@ConditionalOnWebApplication
public class BaseAdvice extends Controller {

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
    public Object defaultException(Exception e) {
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
    public Object internalException(InternalException e) {
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
    public Object businessException(BusinessException e) {
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
    public Object crontabException(CrontabException e) {
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
    public Object validateException(ValidateException e) {
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
    public Object authorizedException(AuthorizedException e) {
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
    public Object signatureException(SignatureException e) {
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
    public Object uncheckedException(UncheckedException e) {
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
    public Object relevantException(RelevantException e) {
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
    public Object httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
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
    public Object httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
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
    public Object noHandlerFoundException(NoHandlerFoundException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._100804);
    }

    /**
     * Parameter binding exception handler for {@link MethodArgumentNotValidException} and {@link BindException}. This
     * handles exceptions that occur during parameter binding and validation.
     *
     * @param e the caught validation exception
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public Object handleBodyValidException(MethodArgumentNotValidException e) {
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
            // Since this is an error handler, it's an "exit" (isEntry = false)
            Logger.error(false, "Advice", "Errors: {}", ex.getMessage(), ex);
            Instances.singletion(ErrorAdvice.class).handler(ex);
        } catch (RuntimeException ignore) {
            // Prevents the exception handler itself from crashing the application.
            Logger.error(false, "Advice", "Exception occurred in the defaultExceptionHandler itself.", ignore);
        }
    }

}
