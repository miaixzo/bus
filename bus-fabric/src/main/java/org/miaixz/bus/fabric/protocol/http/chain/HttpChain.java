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
package org.miaixz.bus.fabric.protocol.http.chain;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Internal immutable HTTP stage chain with an index cursor.
 *
 * <p>
 * Protocol users extend HTTP behavior through builder {@code filter(...)}, {@code guard(...)}, and
 * {@code observe(...)} hooks. This chain coordinates built-in runtime stages only.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpChain {

    /**
     * Ordered stages.
     */
    private final List<HttpStage> stages;

    /**
     * Current stage index.
     */
    private final int index;

    /**
     * Current leased connection.
     */
    private final ConnectionLease lease;

    /**
     * Current network connection.
     */
    private final Connection connection;

    /**
     * Shared cancellation scope.
     */
    private final Cancellation cancellation;

    /**
     * Creates a chain.
     *
     * @param stages     stages
     * @param index      current index
     * @param lease      leased connection
     * @param connection network connection
     */
    private HttpChain(final List<HttpStage> stages, final int index, final ConnectionLease lease,
            final Connection connection, final Cancellation cancellation) {
        this.stages = List.copyOf(validateStages(stages));
        this.index = validateIndex(index, this.stages.size());
        this.lease = lease;
        this.connection = connection;
        this.cancellation = require(cancellation, "Cancellation");
    }

    /**
     * Creates a chain at index zero.
     *
     * @param stages stages
     * @return chain
     */
    public static HttpChain create(final List<HttpStage> stages) {
        return create(stages, Cancellation.create());
    }

    /**
     * Creates a chain at index zero with a shared cancellation scope.
     *
     * @param stages       stages
     * @param cancellation cancellation scope
     * @return chain
     */
    public static HttpChain create(final List<HttpStage> stages, final Cancellation cancellation) {
        return new HttpChain(stages, Normal._0, null, null, cancellation);
    }

    /**
     * Proceeds to the next stage.
     *
     * @param request request
     * @return response
     */
    public HttpResponse proceed(final HttpRequest request) {
        final HttpRequest current = Assert
                .notNull(request, () -> new ValidateException("HTTP request must not be null"));
        if (index >= stages.size()) {
            throw new StatefulException("HTTP chain is exhausted");
        }
        cancellation.throwIfCancelled();
        final HttpStage stage = stages.get(index);
        return stage.execute(current, new HttpChain(stages, index + 1, lease, connection, cancellation));
    }

    /**
     * Returns a new chain with an appended stage.
     *
     * @param stage stage
     * @return new chain
     */
    public HttpChain add(final HttpStage stage) {
        validateStage(stage);
        final ArrayList<HttpStage> copy = new ArrayList<>(stages);
        copy.add(stage);
        return new HttpChain(copy, index, lease, connection, cancellation);
    }

    /**
     * Returns the current stage index.
     *
     * @return index
     */
    public int index() {
        return index;
    }

    /**
     * Returns stage snapshot.
     *
     * @return stages
     */
    public List<HttpStage> stages() {
        return List.copyOf(stages);
    }

    /**
     * Returns a chain that carries a leased connection for downstream stages.
     *
     * @param lease      lease
     * @param connection connection
     * @return contextual chain
     */
    HttpChain withConnection(final ConnectionLease lease, final Connection connection) {
        final ConnectionLease currentLease = Assert
                .notNull(lease, () -> new ValidateException("Connection lease must not be null"));
        final Connection currentConnection = Assert
                .notNull(connection, () -> new ValidateException("Network connection must not be null"));
        return new HttpChain(stages, index, currentLease, currentConnection, cancellation);
    }

    /**
     * Returns the current connection lease.
     *
     * @return lease or null
     */
    ConnectionLease lease() {
        return lease;
    }

    /**
     * Returns the current network connection.
     *
     * @return connection or null
     */
    Connection connection() {
        return connection;
    }

    /**
     * Returns the shared cancellation scope.
     *
     * @return cancellation scope
     */
    Cancellation cancellation() {
        return cancellation;
    }

    /**
     * Validates stage list.
     *
     * @param stages stages
     * @return stages
     */
    private static List<HttpStage> validateStages(final List<HttpStage> stages) {
        final List<HttpStage> source = Assert
                .notNull(stages, () -> new ValidateException("HTTP stages must not be null"));
        for (final HttpStage stage : source) {
            validateStage(stage);
        }
        return source;
    }

    /**
     * Validates a stage.
     *
     * @param stage stage
     */
    private static void validateStage(final HttpStage stage) {
        Assert.notNull(stage, () -> new ValidateException("HTTP stage must not be null"));
    }

    /**
     * Validates a stage index.
     *
     * @param index index
     * @param size  stage size
     * @return index
     */
    private static int validateIndex(final int index, final int size) {
        Assert.isTrue(
                index >= Normal._0 && index <= size,
                () -> new ValidateException("HTTP chain index is out of range"));
        return index;
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
