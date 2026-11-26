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
package org.miaixz.bus.spring.jdbc;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Applies Spring transaction advice and translates JDBC and transaction infrastructure failures into Bus exceptions.
 * <p>
 * Translation surrounds the complete transaction invocation, covering failures during transaction begin, method
 * execution, rollback and commit. Spring evaluates rollback rules against the original application failure before any
 * exception escaping the transaction advice is translated.
 * <p>
 * Unrelated runtime exceptions retain their identity. Checked exceptions and errors propagate unchanged.
 *
 * @author Kimi Liu
 */
public final class JdbcTransactionInterceptor extends TransactionInterceptor {

    /**
     * Invokes the target method through Spring's transaction advice and translates escaping runtime failures using
     * {@link JdbcErrorTranslator}.
     *
     * @param invocation intercepted method invocation
     * @return target method result, or {@code null} for a void method
     * @throws Throwable if transaction processing or the target method fails; classified runtime infrastructure
     *                   failures are translated, while other failures propagate unchanged
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return super.invoke(invocation);
        } catch (RuntimeException failure) {
            // Translate only after Spring has applied rollback rules and completed transaction processing.
            throw JdbcErrorTranslator.translate(failure);
        }
    }

}
