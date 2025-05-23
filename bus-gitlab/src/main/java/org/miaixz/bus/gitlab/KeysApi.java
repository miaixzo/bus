/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
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
package org.miaixz.bus.gitlab;

import java.util.Collections;

import org.miaixz.bus.gitlab.models.Key;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

/**
 * See: <a href="https://docs.gitlab.com/ee/api/keys.html">GitLab Key API Documentaion</a>
 */
public class KeysApi extends AbstractApi {
    public KeysApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * @param fingerprint The md5 hash of a ssh public key with : separating the bytes Or SHA256:$base64hash
     * @return The Key which includes the user who owns the key
     * @throws GitLabApiException If anything goes wrong
     */
    public Key getUserBySSHKeyFingerprint(String fingerprint) throws GitLabApiException {
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
        queryParams.put("fingerprint", Collections.singletonList(fingerprint));
        Response response = get(Response.Status.OK, queryParams, "keys");
        return response.readEntity(Key.class);
    }

    /**
     * Get a single key by id.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /keys/:id</code>
     * </pre>
     *
     * @param keyId the IID of the key to get
     * @return a Key instance
     * @throws GitLabApiException if any exception occurs
     */
    public Key getKey(String keyId) throws GitLabApiException {
        Response response = get(Response.Status.OK, null, "keys", keyId);
        return response.readEntity(Key.class);
    }

}
