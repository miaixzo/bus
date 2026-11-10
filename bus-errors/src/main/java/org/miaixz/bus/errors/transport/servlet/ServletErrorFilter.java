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
package org.miaixz.bus.errors.transport.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletResponseWrapper;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.springframework.http.HttpHeaders;

/**
 * Guards Servlet HTTP error output before filters, MVC or container error dispatch can write a response.
 *
 * @author Kimi Liu
 */
public final class ServletErrorFilter implements Filter {

    /**
     * Creates the stateless error guard for Servlet dispatches.
     */
    public ServletErrorFilter() {
    }

    /**
     * Routes container failures to the common writer and protects the response during normal and async dispatch.
     *
     * @param request  request being dispatched
     * @param response response owned by the container
     * @param chain    downstream filters and servlet
     * @throws IOException      if the connection cannot complete the response
     * @throws ServletException if a committed or non-HTTP dispatch fails
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }
        var guard = new Guard(httpRequest, httpResponse);
        if (httpRequest.getDispatcherType() == DispatcherType.ERROR && !guard.isCommitted()) {
            Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            Object cause = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
            guard.reset();
            ServletErrorHandler.fallback(
                    httpRequest,
                    guard,
                    status instanceof Integer code ? code : 500,
                    cause instanceof Throwable failure ? failure : null,
                    HttpHeaders.EMPTY);
            return;
        }
        try {
            chain.doFilter(request, guard);
            if (!httpRequest.isAsyncStarted()) {
                guard.rejectIfUnapproved();
            }
        } catch (IOException | ServletException | RuntimeException failure) {
            if (guard.isCommitted()) {
                ServletErrorHandler.terminate(httpRequest, failure);
                throw failure;
            }
            guard.reset();
            ServletErrorHandler.fallback(httpRequest, guard, 500, failure, HttpHeaders.EMPTY);
        }
    }

    /**
     * Authorizes the public error response chosen by an existing Bus HTTP adapter.
     *
     * @param response active Servlet response, potentially wrapped by another filter
     */
    public static void approve(HttpServletResponse response) {
        ServletResponse current = response;
        while (current instanceof ServletResponseWrapper wrapper) {
            if (wrapper instanceof Guard guard) {
                guard.approved = true;
            }
            current = wrapper.getResponse();
        }
    }

    /**
     * Checks whether a typed MVC response already passed through an application error adapter.
     *
     * @param response active Servlet response
     * @return whether an adapter owns the response
     */
    public static boolean isApproved(HttpServletResponse response) {
        ServletResponse current = response;
        while (current instanceof ServletResponseWrapper wrapper) {
            if (wrapper instanceof Guard guard && guard.approved) {
                return true;
            }
            current = wrapper.getResponse();
        }
        return false;
    }

    /**
     * Stops an unapproved error body before either Servlet writer can commit it.
     */
    private static final class Guard extends HttpServletResponseWrapper {

        /** Current request, shared with the fallback writer and observation. */
        private final HttpServletRequest request;
        /** Whether an approved writer has selected the error response. */
        private boolean approved;
        /** Whether the original body has been suppressed. */
        private boolean rejected;
        /** Lazy wrapper around the container's binary writer. */
        private ServletOutputStream stream;
        /** Lazy wrapper around the container's character writer. */
        private PrintWriter writer;

        /**
         * Creates the response guard for one dispatch.
         *
         * @param request  current HTTP request
         * @param response container response
         */
        private Guard(HttpServletRequest request, HttpServletResponse response) {
            super(response);
            this.request = request;
        }

        /**
         * Drops an arbitrary Servlet reason and returns the status through the shared boundary.
         *
         * @param status requested HTTP status
         * @param reason diagnostic reason from the caller
         * @throws IOException if the container cannot complete the response
         */
        @Override
        public void sendError(int status, String reason) throws IOException {
            sendError(status);
        }

        /**
         * Completes a safe Servlet error without dispatching to the default error page.
         *
         * @param status requested HTTP status
         * @throws IOException if the container cannot complete the response
         */
        @Override
        public void sendError(int status) throws IOException {
            if (isCommitted()) {
                throw new IllegalStateException("Error response already committed");
            }
            reset();
            ServletErrorHandler.fallback(this.request, this, status, null, HttpHeaders.EMPTY);
        }

        /**
         * Wraps the binary writer and checks ownership before writing or flushing.
         *
         * @return guarded Servlet stream
         * @throws IOException if the underlying stream cannot be opened
         */
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (this.stream == null) {
                this.stream = new GuardedStream(super.getOutputStream());
            }
            return this.stream;
        }

        /**
         * Wraps the character writer and checks ownership before writing or flushing.
         *
         * @return guarded Servlet writer
         * @throws IOException if the underlying writer cannot be opened
         */
        @Override
        public PrintWriter getWriter() throws IOException {
            if (this.writer == null) {
                this.writer = new PrintWriter(new GuardedWriter(super.getWriter()));
            }
            return this.writer;
        }

        /**
         * Verifies ownership before committing buffered content.
         *
         * @throws IOException if the connection cannot flush
         */
        @Override
        public void flushBuffer() throws IOException {
            if (!rejectIfUnapproved()) {
                super.flushBuffer();
            }
        }

        /**
         * Resets container and guard state before preparing a replacement response.
         */
        @Override
        public void reset() {
            super.reset();
            this.approved = false;
            this.rejected = false;
            this.stream = null;
            this.writer = null;
        }

        /**
         * Replaces an unapproved response before the original body reaches the client.
         *
         * @return whether the original output must be discarded
         * @throws IOException if the replacement cannot be flushed
         */
        private boolean rejectIfUnapproved() throws IOException {
            if (this.rejected || this.approved || isCommitted() || getStatus() < 400 || getStatus() > 599) {
                return this.rejected;
            }
            int status = getStatus();
            reset();
            ServletErrorHandler.fallback(this.request, this, status, null, HttpHeaders.EMPTY);
            this.rejected = true;
            return true;
        }

        /**
         * Writes binary data only while the original response is permitted.
         */
        private final class GuardedStream extends ServletOutputStream {

            /** Container's Servlet stream. */
            private final ServletOutputStream delegate;

            /** Creates a guarded writer for the given container stream. */
            private GuardedStream(ServletOutputStream delegate) {
                this.delegate = delegate;
            }

            /** Returns the container stream's nonblocking write readiness. */
            @Override
            public boolean isReady() {
                return this.delegate.isReady();
            }

            /** Registers a nonblocking callback with the container stream. */
            @Override
            public void setWriteListener(WriteListener listener) {
                this.delegate.setWriteListener(listener);
            }

            /** Writes one byte after checking the response. */
            @Override
            public void write(int value) throws IOException {
                if (!rejectIfUnapproved()) {
                    this.delegate.write(value);
                }
            }

            /** Writes a byte range after checking the response. */
            @Override
            public void write(byte[] bytes, int offset, int length) throws IOException {
                if (!rejectIfUnapproved()) {
                    this.delegate.write(bytes, offset, length);
                }
            }

            /** Flushes only an allowed response. */
            @Override
            public void flush() throws IOException {
                if (!rejectIfUnapproved()) {
                    this.delegate.flush();
                }
            }

            /** Closes only an allowed response writer. */
            @Override
            public void close() throws IOException {
                if (!rejectIfUnapproved()) {
                    this.delegate.close();
                }
            }
        }

        /**
         * Writes character data only while the original response is permitted.
         */
        private final class GuardedWriter extends Writer {

            /** Container's character writer. */
            private final PrintWriter delegate;

            /** Creates a guarded writer for the given container writer. */
            private GuardedWriter(PrintWriter delegate) {
                this.delegate = delegate;
            }

            /** Writes a character range after checking the response. */
            @Override
            public void write(char[] chars, int offset, int length) throws IOException {
                if (!rejectIfUnapproved()) {
                    this.delegate.write(chars, offset, length);
                }
            }

            /** Flushes only an allowed response. */
            @Override
            public void flush() throws IOException {
                if (!rejectIfUnapproved()) {
                    this.delegate.flush();
                }
            }

            /** Closes only an allowed response writer. */
            @Override
            public void close() throws IOException {
                if (!rejectIfUnapproved()) {
                    this.delegate.close();
                }
            }
        }
    }

}
