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
package org.miaixz.bus.errors;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

import org.miaixz.bus.errors.ErrorDecision.Action;
import org.miaixz.bus.errors.ErrorDecision.Basis;
import org.miaixz.bus.errors.internal.ClassifierRegistry;
import org.miaixz.bus.errors.internal.ErrorCatalog;
import org.miaixz.bus.errors.internal.ErrorExposurePolicy;
import org.miaixz.bus.errors.internal.ThrowableNormalizer;
import org.miaixz.bus.errors.spi.ErrorClassifier;
import org.miaixz.bus.errors.spi.ErrorDefinition;

/**
 * Classifies structured failures and determines the legal boundary action without performing I/O.
 *
 * @author Kimi Liu
 */
public final class ErrorEngine {

    /**
     * Default engine for standard classifications and safe projections.
     */
    private static final ErrorEngine STANDARD = new ErrorEngine(List.of());

    /**
     * Ordered extension classifiers, isolated from the original operation.
     */
    private final ClassifierRegistry classifiers;
    /**
     * Public error definitions used to project approved diagnostic fields.
     */
    private final ErrorCatalog catalog;

    /**
     * Creates an engine using registered classifiers in deterministic precedence order.
     *
     * @param classifiers additional classifiers, highest priority first
     */
    public ErrorEngine(List<ErrorClassifier> classifiers) {
        this(classifiers, List.of());
    }

    /**
     * Creates an engine with application classification and public exposure definitions.
     *
     * @param classifiers ordered classifiers
     * @param definitions public rendering definitions, unique by type
     */
    public ErrorEngine(List<ErrorClassifier> classifiers, List<ErrorDefinition> definitions) {
        this.classifiers = new ClassifierRegistry(classifiers);
        this.catalog = new ErrorCatalog(definitions);
    }

    /**
     * Returns the engine with standard classifications and safe projections.
     *
     * @return the engine for standard Bus classifications
     */
    public static ErrorEngine standard() {
        return STANDARD;
    }

    /**
     * Classifies a verified HTTP status. Status 503 alone identifies unavailability, while admission supplies a
     * specific overload descriptor.
     *
     * @param status HTTP error status
     * @return standard semantic type
     */
    public static ErrorType typeForStatus(int status) {
        return switch (status) {
            case 400, 422 -> ErrorType.INVALID_REQUEST;
            case 401 -> ErrorType.UNAUTHENTICATED;
            case 403 -> ErrorType.FORBIDDEN;
            case 404 -> ErrorType.ROUTE_NOT_FOUND;
            case 405 -> ErrorType.METHOD_NOT_ALLOWED;
            case 406 -> ErrorType.NOT_ACCEPTABLE;
            case 413 -> ErrorType.PAYLOAD_TOO_LARGE;
            case 415 -> ErrorType.UNSUPPORTED_MEDIA_TYPE;
            case 429 -> ErrorType.RATE_LIMITED;
            case 502, 503 -> ErrorType.DEPENDENCY_UNAVAILABLE;
            case 408, 504 -> ErrorType.TIMEOUT;
            default -> ErrorType.UNKNOWN;
        };
    }

    /**
     * Builds a descriptor using only a stable registered type.
     *
     * @param type semantic type
     * @return descriptor without diagnostic data
     */
    public static ErrorDescriptor descriptor(ErrorType type) {
        return new ErrorDescriptor(type, null, type.id(), Map.of());
    }

    /**
     * Projects an internal descriptor to its public contract.
     *
     * @param error internal error descriptor
     * @return safe descriptor using registered fields
     */
    public ErrorDescriptor publicError(ErrorDescriptor error) {
        return ErrorExposurePolicy.project(this.catalog, error);
    }

    /**
     * Determines a failure action. Only transparent wrappers influence semantic classification.
     *
     * @param context execution boundary and phase
     * @param facts   facts established by the boundary
     * @param failure original failure, possibly {@code null}
     * @return error decision
     */
    public ErrorDecision decide(ErrorContext context, FailureFacts facts, Throwable failure) {
        return decide(context, facts, failure, ignored -> {
        });
    }

    /**
     * Classifies the original failure while allowing an operation boundary to report faulty extensions.
     *
     * @param context execution boundary and phase
     * @param facts verified failure facts
     * @param failure original failure, or null
     * @param diagnostics recipient of isolated classifier failures
     * @return error decision
     */
    ErrorDecision decide(ErrorContext context, FailureFacts facts, Throwable failure, Consumer<Throwable> diagnostics) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(facts, "facts");
        Throwable primary = ThrowableNormalizer.primary(failure);
        if (primary instanceof InterruptedException || primary instanceof CancellationException) {
            return new ErrorDecision(Action.PROPAGATE, descriptor(ErrorType.CANCELLED), facts.status(),
                    Basis.CANCELLATION, context.policyVersion());
        }
        // A local boundary may already own a verified archive or optional provider failure.
        // Transport handlers leave linkage failures to their runtime.
        if (primary instanceof VirtualMachineError || primary instanceof ThreadDeath || (primary instanceof LinkageError
                && (context.phase() != ErrorContext.Phase.LOCAL || facts.descriptor() == null))) {
            return new ErrorDecision(Action.PROPAGATE, descriptor(ErrorType.UNKNOWN), facts.status(),
                    Basis.FATAL_SIGNAL, context.policyVersion());
        }
        if (facts.source() == FailureFacts.Source.UPSTREAM_RESPONSE) {
            return new ErrorDecision(Action.PASS_THROUGH, facts.descriptor(), facts.status(), Basis.UPSTREAM,
                    context.policyVersion());
        }
        Action action = switch (context.phase()) {
            case LOCAL -> Action.PROPAGATE;
            case OPEN -> Action.RENDER;
            case COMMITTED -> Action.TERMINATE;
        };
        if (facts.descriptor() != null) {
            return new ErrorDecision(action, facts.descriptor(), facts.status(), Basis.VERIFIED_FACT,
                    context.policyVersion());
        }
        if (primary instanceof BusException bus) {
            return new ErrorDecision(action, bus.error(), facts.status(), Basis.STRUCTURED_EXCEPTION,
                    context.policyVersion());
        }
        ErrorDescriptor classified = this.classifiers.classify(context, facts, primary, diagnostics);
        if (classified != null) {
            return new ErrorDecision(action, classified, facts.status(), Basis.CLASSIFIER, context.policyVersion());
        }
        return new ErrorDecision(action, descriptor(typeForStatus(facts.status())), facts.status(), Basis.STANDARD,
                context.policyVersion());
    }

}
