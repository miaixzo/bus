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
package org.miaixz.bus.errors.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * Traverses transparent wrappers and diagnostic causes within a fixed operation budget.
 *
 * @author Kimi Liu
 */
public final class ThrowableNormalizer {

    /**
     * Maximum distinct causes traversed for classification or compatibility.
     */
    private static final int MAX_DEPTH = 64;

    /**
     * Prevents construction of the stateless cause inspection utility.
     */
    private ThrowableNormalizer() {
    }

    /**
     * Gives JVM termination and fatal resource failures back to the runtime.
     *
     * @param failure error caught by a failure-handling callback
     */
    public static void rethrowFatal(Throwable failure) {
        if (failure instanceof VirtualMachineError fatal) {
            throw fatal;
        }
        if (failure instanceof ThreadDeath stopped) {
            throw stopped;
        }
    }

    /**
     * Captures a bounded, cycle-safe diagnostic chain in outer-to-inner order.
     *
     * @param failure outer failure, possibly {@code null}
     * @return detached list of unique causes
     */
    public static List<Throwable> causes(Throwable failure) {
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Throwable> causes = new ArrayList<>();
        while (failure != null && causes.size() < MAX_DEPTH && seen.add(failure)) {
            causes.add(failure);
            failure = causeOf(failure);
        }
        return List.copyOf(causes);
    }

    /**
     * Selects the first non-transparent failure, preserving an empty or malformed wrapper.
     *
     * @param failure original failure
     * @return primary failure, or {@code null} for a missing failure
     */
    public static Throwable primary(Throwable failure) {
        if (!transparent(failure)) {
            return failure;
        }
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = failure;
        while (current != null && seen.size() < MAX_DEPTH && seen.add(current)) {
            if (!transparent(current)) {
                return current;
            }
            current = causeOf(current);
        }
        return failure;
    }

    /**
     * Identifies wrappers that expose their causes as the primary failure.
     *
     * @param failure candidate exception, possibly null
     * @return whether the wrapper is transparent
     */
    private static boolean transparent(Throwable failure) {
        return failure instanceof CompletionException || failure instanceof ExecutionException
                || failure instanceof InvocationTargetException || failure instanceof UndeclaredThrowableException;
    }

    /**
     * Reads the cause and isolates a malformed exception implementation.
     *
     * @param failure exception whose cause is requested
     * @return cause, or null if it is unavailable
     */
    private static Throwable causeOf(Throwable failure) {
        try {
            return failure.getCause();
        } catch (RuntimeException malformed) {
            return null;
        }
    }

    /**
     * Finds a diagnostic cause for a legacy compatibility mapping.
     *
     * @param causes bounded chain
     * @param type   expected cause type
     * @param <T>    expected cause type
     * @return first matching cause, or {@code null}
     */
    public static <T extends Throwable> T find(List<Throwable> causes, Class<T> type) {
        for (Throwable cause : causes) {
            if (type.isInstance(cause)) {
                return type.cast(cause);
            }
        }
        return null;
    }

}
