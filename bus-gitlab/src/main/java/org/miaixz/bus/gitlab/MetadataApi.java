/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab;

import org.miaixz.bus.gitlab.models.Metadata;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * This class implements the client side API for the Gitlab metadata call.
 *
 * @see <a href="https://https://docs.gitlab.com/ee/api/metadata.html">Metadata API at Gitlab</a>
 */
public class MetadataApi extends AbstractApi {

    public MetadataApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get Gitlab metadata
     *
     * <pre>
     * <code>Gitlab Endpoint: GET /metadata</code>
     * </pre>
     *
     * @return Gitlab metadata
     * @throws GitLabApiException if any exception occurs
     */
    public Metadata getMetadata() throws GitLabApiException {
        Response response = get(Status.OK, null, "metadata");
        return (response.readEntity(Metadata.class));
    }

}
