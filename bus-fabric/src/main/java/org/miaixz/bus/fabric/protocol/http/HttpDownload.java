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
package org.miaixz.bus.fabric.protocol.http;

import static org.miaixz.bus.fabric.Builder.BYTES_64_KIB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Resumable HTTP download task with Range, progress, pause and cancel support.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpDownload {

    /**
     * HTTP executor.
     */
    private final Exchange exchange;

    /**
     * Source request.
     */
    private final HttpRequest request;

    /**
     * Target file.
     */
    private final Path target;

    /**
     * Cancellation scope.
     */
    private final Cancellation cancellation;

    /**
     * Progress callback.
     */
    private final BiConsumer<Long, Long> progress;

    /**
     * Resume flag.
     */
    private final boolean resume;

    /**
     * Pause state.
     */
    private final AtomicBoolean paused;

    /**
     * Cancel state.
     */
    private final AtomicBoolean cancelled;

    /**
     * Creates a download task.
     *
     * @param exchange     HTTP executor
     * @param request      request
     * @param target       target file
     * @param cancellation cancellation scope
     * @param progress     progress callback
     * @param resume       whether partial files may be resumed
     */
    private HttpDownload(final Exchange exchange, final HttpRequest request, final Path target,
            final Cancellation cancellation, final BiConsumer<Long, Long> progress, final boolean resume) {
        this.exchange = require(exchange, "HTTP download exchange");
        this.request = require(request, "HTTP request");
        this.target = validateTarget(target);
        this.cancellation = require(cancellation, "Cancellation");
        this.progress = progress;
        this.resume = resume;
        this.paused = new AtomicBoolean();
        this.cancelled = new AtomicBoolean();
        Assert.isTrue(request.method() == HTTP.Method.GET, () -> new ValidateException("HTTP download requires GET"));
    }

    /**
     * Creates a builder that executes with a Context.
     *
     * @param context context
     * @return builder
     */
    public static Builder builder(final Context context) {
        final Context current = require(context, "Context");
        return new Builder((request, cancellation) -> HttpRunner.create(current, request).run(cancellation));
    }

    /**
     * Creates a builder with a custom exchange, useful for adapters and tests.
     *
     * @param exchange exchange
     * @return builder
     */
    public static Builder builder(final Exchange exchange) {
        return new Builder(exchange);
    }

    /**
     * Executes the download.
     *
     * @return completed target
     */
    public Path execute() {
        cancellation.throwIfCancelled();
        final Path part = part();
        final Path meta = meta();
        Logger.info(
                true,
                "Fabric",
                "HTTP download started: method={}, scheme={}, host={}, port={}, path={}, target={}, resume={}",
                request.method().value(),
                request.url().scheme(),
                request.url().host(),
                request.url().port(),
                request.url().path(),
                target,
                resume);
        try {
            createParent(target);
            long offset = resume && Files.exists(part) ? Files.size(part) : 0L;
            if (!resume) {
                deleteQuietly(part);
                deleteQuietly(meta);
                offset = 0L;
            }
            Logger.info(
                    false,
                    "Fabric",
                    "HTTP download resume state: target={}, part={}, offset={}, resume={}",
                    target,
                    part,
                    offset,
                    resume);
            final HttpRequest current = rangedRequest(offset, readValidator(meta));
            try (HttpResponse response = exchange.execute(current, cancellation)) {
                final boolean append = offset > 0L && response.code() == HTTP.HTTP_PARTIAL;
                if (!append) {
                    offset = 0L;
                    deleteQuietly(part);
                }
                validateResponse(response, append);
                writeValidator(meta, validator(response));
                final long total = totalLength(response, offset, append);
                Logger.info(
                        false,
                        "Fabric",
                        "HTTP download response accepted: code={}, append={}, offset={}, total={}, target={}",
                        response.code(),
                        append,
                        offset,
                        total,
                        target);
                copy(response, part, offset, total, append);
            }
            Files.move(part, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            deleteQuietly(meta);
            Logger.info(false, "Fabric", "HTTP download completed: target={}, bytes={}", target, Files.size(target));
            return target;
        } catch (final CancellationException e) {
            if (cancelled.get() && !paused.get()) {
                deleteQuietly(part);
                deleteQuietly(meta);
            }
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "HTTP download cancelled: target={}, paused={}, cancelled={}",
                    target,
                    paused.get(),
                    cancelled.get());
            throw e;
        } catch (final IOException e) {
            deleteQuietly(part);
            deleteQuietly(meta);
            Logger.error(
                    false,
                    "Fabric",
                    e,
                    "HTTP download failed: target={}, exception={}",
                    target,
                    e.getClass().getSimpleName());
            throw new InternalException("Unable to complete HTTP download", e);
        } catch (final RuntimeException e) {
            if (!paused.get()) {
                deleteQuietly(part);
                deleteQuietly(meta);
            }
            Logger.error(
                    false,
                    "Fabric",
                    e,
                    "HTTP download failed: target={}, exception={}",
                    target,
                    e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Pauses this download and keeps the part file.
     *
     * @return true when the pause changed state
     */
    public boolean pause() {
        paused.set(true);
        final boolean changed = cancellation.cancel(new CancellationException("HTTP download paused"));
        Logger.info(false, "Fabric", "HTTP download pause requested: target={}, changed={}", target, changed);
        return changed;
    }

    /**
     * Cancels this download and removes the part file.
     *
     * @return true when the cancel changed state
     */
    public boolean cancel() {
        cancelled.set(true);
        final boolean changed = cancellation.cancel(new CancellationException("HTTP download cancelled"));
        Logger.info(false, "Fabric", "HTTP download cancel requested: target={}, changed={}", target, changed);
        return changed;
    }

    /**
     * Returns the target file.
     *
     * @return target
     */
    public Path target() {
        return target;
    }

    /**
     * Returns the part file.
     *
     * @return part file
     */
    public Path part() {
        return target.resolveSibling(target.getFileName() + ".part");
    }

    /**
     * Returns the metadata sidecar file.
     *
     * @return metadata file
     */
    public Path meta() {
        return target.resolveSibling(target.getFileName() + ".part.meta");
    }

    /**
     * Creates a ranged request when a partial body exists.
     *
     * @param offset    offset
     * @param validator validator
     * @return request
     */
    private HttpRequest rangedRequest(final long offset, final String validator) {
        if (offset <= 0L) {
            return request;
        }
        Headers headers = request.headers().with(HTTP.RANGE, "bytes=" + offset + "-");
        if (validator != null) {
            headers = headers.with(HTTP.IF_RANGE, validator);
        }
        return request.toBuilder().headers(headers).build();
    }

    /**
     * Copies response body to the part file.
     *
     * @param response response
     * @param part     part file
     * @param offset   existing offset
     * @param total    total expected length
     * @param append   whether append mode is used
     */
    private void copy(
            final HttpResponse response,
            final Path part,
            final long offset,
            final long total,
            final boolean append) {
        final StandardOpenOption[] options = append
                ? new StandardOpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND }
                : new StandardOpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
        try (Source input = response.body().source(); Sink output = IoKit.sink(part, options)) {
            final Buffer buffer = new Buffer();
            long written = offset;
            progress(written, total);
            while (true) {
                cancellation.throwIfCancelled();
                final long read = input.read(buffer, BYTES_64_KIB);
                cancellation.throwIfCancelled();
                if (read < 0) {
                    break;
                }
                output.write(buffer, read);
                written += read;
                progress(written, total);
            }
            output.flush();
        } catch (final IOException e) {
            throw new SocketException("Unable to stream HTTP download", e);
        }
    }

    /**
     * Validates the HTTP response.
     *
     * @param response response
     * @param append   append mode
     */
    private static void validateResponse(final HttpResponse response, final boolean append) {
        final int code = response.code();
        if (append && code != HTTP.HTTP_PARTIAL) {
            throw new ProtocolException("HTTP resume requires 206 Partial Content");
        }
        if (!append && code != HTTP.HTTP_OK && code != HTTP.HTTP_PARTIAL) {
            throw new ProtocolException("HTTP download failed with status " + code);
        }
    }

    /**
     * Returns expected total length.
     *
     * @param response response
     * @param offset   offset
     * @param append   append mode
     * @return total or -1
     */
    private static long totalLength(final HttpResponse response, final long offset, final boolean append) {
        final long rangeTotal = contentRangeTotal(response.headers().get(HTTP.CONTENT_RANGE));
        if (rangeTotal >= 0L) {
            return rangeTotal;
        }
        final int length = response.headers().contentLength();
        if (length >= 0) {
            return append ? offset + length : length;
        }
        return -1L;
    }

    /**
     * Parses total length from Content-Range.
     *
     * @param value header value
     * @return total or -1
     */
    private static long contentRangeTotal(final String value) {
        if (value == null) {
            return -1L;
        }
        final int slash = value.lastIndexOf(Symbol.C_SLASH);
        if (slash < 0 || slash == value.length() - 1) {
            return -1L;
        }
        final String total = value.substring(slash + 1).trim();
        if (Symbol.STAR.equals(total)) {
            return -1L;
        }
        try {
            return Long.parseLong(total);
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid Content-Range total", e);
        }
    }

    /**
     * Returns a resumable validator.
     *
     * @param response response
     * @return validator or null
     */
    private static String validator(final HttpResponse response) {
        final String etag = response.headers().get(HTTP.ETAG);
        return etag == null ? response.headers().get(HTTP.LAST_MODIFIED) : etag;
    }

    /**
     * Reads a validator sidecar.
     *
     * @param meta sidecar
     * @return validator or null
     * @throws IOException when reading fails
     */
    private static String readValidator(final Path meta) throws IOException {
        if (!Files.exists(meta)) {
            return null;
        }
        final String value = Files.readString(meta).trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * Writes a validator sidecar.
     *
     * @param meta      sidecar
     * @param validator validator
     * @throws IOException when writing fails
     */
    private static void writeValidator(final Path meta, final String validator) throws IOException {
        if (validator == null || validator.isBlank()) {
            deleteQuietly(meta);
            return;
        }
        createParent(meta);
        Files.writeString(meta, validator);
    }

    /**
     * Emits download progress only when the caller supplied a progress callback.
     *
     * @param written bytes written to the target file
     * @param total   expected total bytes, or {@code -1} when unknown
     */
    private void progress(final long written, final long total) {
        if (progress != null) {
            progress.accept(written, total);
        }
    }

    /**
     * Ensures the target parent directory exists before body bytes are written.
     *
     * @param path target or sidecar path
     * @throws IOException when the directory cannot be created
     */
    private static void createParent(final Path path) throws IOException {
        final Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    /**
     * Removes an incomplete target or validator sidecar without hiding the original transfer failure.
     *
     * @param path path to remove
     */
    private static void deleteQuietly(final Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (final IOException ignored) {
            // Best-effort cleanup.
        }
    }

    /**
     * Validates that a download target names a concrete file path.
     *
     * @param target candidate target
     * @return validated target
     */
    private static Path validateTarget(final Path target) {
        final Path checked = Assert
                .notNull(target, () -> new ValidateException("HTTP download target must not be null"));
        Assert.notNull(checked.getFileName(), () -> new ValidateException("HTTP download target must not be null"));
        return checked;
    }

    /**
     * Validates required builder and execution inputs.
     *
     * @param value value
     * @param name  field name used in validation messages
     * @param <T>   value type
     * @return validated value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * HTTP exchange abstraction.
     */
    @FunctionalInterface
    public interface Exchange {

        /**
         * Executes an HTTP request.
         *
         * @param request      request
         * @param cancellation cancellation
         * @return response
         */
        HttpResponse execute(HttpRequest request, Cancellation cancellation);

    }

    /**
     * Builder for download tasks.
     */
    public static final class Builder {

        /**
         * HTTP exchange.
         */
        private final Exchange exchange;

        /**
         * Request candidate.
         */
        private HttpRequest request;

        /**
         * Target candidate.
         */
        private Path target;

        /**
         * Cancellation candidate.
         */
        private Cancellation cancellation = Cancellation.create();

        /**
         * Progress callback candidate.
         */
        private BiConsumer<Long, Long> progress;

        /**
         * Resume candidate.
         */
        private boolean resume = true;

        /**
         * Creates a builder bound to the exchange function that performs the actual HTTP call.
         *
         * @param exchange HTTP exchange function
         */
        private Builder(final Exchange exchange) {
            this.exchange = require(exchange, "HTTP download exchange");
        }

        /**
         * Sets the HTTP request used to obtain download bytes.
         *
         * @param request request
         * @return this builder
         */
        public Builder request(final HttpRequest request) {
            this.request = require(request, "HTTP request");
            return this;
        }

        /**
         * Sets the target file path for the downloaded payload.
         *
         * @param target target file
         * @return this builder
         */
        public Builder target(final Path target) {
            this.target = validateTarget(target);
            return this;
        }

        /**
         * Sets the cancellation token checked before and during transfer.
         *
         * @param cancellation cancellation token
         * @return this builder
         */
        public Builder cancellation(final Cancellation cancellation) {
            this.cancellation = require(cancellation, "Cancellation");
            return this;
        }

        /**
         * Sets a progress callback receiving written bytes and total bytes when known.
         *
         * @param progress progress callback
         * @return this builder
         */
        public Builder progress(final BiConsumer<Long, Long> progress) {
            this.progress = progress;
            return this;
        }

        /**
         * Sets whether an existing partial file may be resumed with a range request.
         *
         * @param resume true to resume partial downloads
         * @return this builder
         */
        public Builder resume(final boolean resume) {
            this.resume = resume;
            return this;
        }

        /**
         * Builds an immutable download task.
         *
         * @return download task
         */
        public HttpDownload build() {
            return new HttpDownload(exchange, request, target, cancellation, progress, resume);
        }

        /**
         * Builds and executes the download task.
         *
         * @return downloaded file
         */
        public Path execute() {
            return build().execute();
        }

    }

}
