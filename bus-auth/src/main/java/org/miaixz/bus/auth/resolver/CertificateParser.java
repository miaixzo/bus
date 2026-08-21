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
package org.miaixz.bus.auth.resolver;

import java.time.Instant;

import org.miaixz.bus.auth.worker.CertificateLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Pure parser for project-loaded certificate material.
 */
public final class CertificateParser {

    public CertificateMaterial parse(final CertificateLoader.Request request, final CertificateLoader.Record record) {
        final CertificateLoader.Request expected = Assert.notNull(request, "Certificate request must not be null");
        final CertificateLoader.Record loaded = Assert.notNull(record, "Loaded certificate record must not be null");
        if (!expected.issuer()
                .equals(Assert.notBlank(loaded.issuer(), "Loaded certificate issuer must not be blank"))) {
            throw new ValidateException("Loaded certificate issuer does not match the requested issuer");
        }
        if (!expected.use().equals(Assert.notBlank(loaded.use(), "Loaded certificate use must not be blank"))) {
            throw new ValidateException("Loaded certificate use does not match the requested use");
        }
        final Instant notBefore = Assert
                .notNull(loaded.notBefore(), "Loaded certificate not-before instant must not be null");
        final Instant notAfter = Assert
                .notNull(loaded.notAfter(), "Loaded certificate not-after instant must not be null");
        if (expected.at().isBefore(notBefore) || !expected.at().isBefore(notAfter)) {
            throw new ValidateException("Loaded certificate is not valid at the requested instant");
        }
        return new CertificateMaterial(loaded.chain(), loaded.trustRoots());
    }
}
