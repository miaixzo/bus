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

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.TransactionException;

import org.miaixz.bus.core.lang.exception.DuplicateException;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.core.lang.exception.RetryableException;

/**
 * Converts persistence infrastructure failures without changing application exceptions.
 *
 * @author Kimi Liu
 */
public final class JdbcErrorTranslator {

    /**
     * Prevents instantiation of the shared persistence error translator.
     */
    private JdbcErrorTranslator() {
        // No initialization required.
    }

    /**
     * Converts a classified infrastructure failure, preserving the original cause and retry category. Unrelated runtime
     * exceptions retain their exact identity.
     *
     * @param failure original failure whose public error or infrastructure category must be preserved
     * @return Bus infrastructure exception or the unchanged unrelated runtime failure
     */
    public static RuntimeException translate(RuntimeException failure) {
        if (failure instanceof DuplicateKeyException)
            return new DuplicateException(failure.getMessage(), failure);
        if (failure instanceof TransientDataAccessException)
            return new RetryableException(failure.getMessage(), failure);
        if (failure instanceof DataAccessException || failure instanceof TransactionException)
            return new MapperException(failure.getMessage(), failure);
        return failure;
    }

}
