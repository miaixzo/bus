/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org gitlab4j and other contributors.           ~
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

import org.miaixz.bus.gitlab.models.HealthCheckInfo;

import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;

public class HealthCheckApi extends AbstractApi {

    public HealthCheckApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get Health Checks from the liveness endpoint.
     *
     * Requires ip_whitelist, see the following link for more info: See <a href=
     * "https://docs.gitlab.com/ee/administration/monitoring/ip_whitelist.html">https://docs.gitlab.com/ee/administration/monitoring/ip_whitelist.html</a>
     *
     * <pre>
     * <code>GitLab Endpoint: GET /-/liveness</code>
     * </pre>
     *
     * @return HealthCheckInfo instance
     * @throws GitLabApiException if any exception occurs
     */
    public HealthCheckInfo getLiveness() throws GitLabApiException {
        return (getLiveness(null));
    }

    /**
     * Get Health Checks from the liveness endpoint.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /-/liveness</code>
     * </pre>
     *
     * @param token Health Status token
     * @return HealthCheckInfo instance
     * @throws GitLabApiException if any exception occurs
     * @deprecated
     */
    public HealthCheckInfo getLiveness(String token) throws GitLabApiException {
        try {
            URL livenessUrl = getApiClient().getUrlWithBase("-", "liveness");
            GitLabApiForm formData = new GitLabApiForm().withParam("token", token, false);
            Response response = get(Response.Status.OK, formData.asMap(), livenessUrl);
            return (response.readEntity(HealthCheckInfo.class));
        } catch (IOException ioe) {
            throw (new GitLabApiException(ioe));
        }
    }

    /**
     * Get Health Checks from the readiness endpoint.
     *
     * Requires ip_whitelist, see the following link for more info: See <a href=
     * "https://docs.gitlab.com/ee/administration/monitoring/ip_whitelist.html">https://docs.gitlab.com/ee/administration/monitoring/ip_whitelist.html</a>
     *
     * <pre>
     * <code>GitLab Endpoint: GET /-/readiness</code>
     * </pre>
     *
     * @return HealthCheckInfo instance
     * @throws GitLabApiException if any exception occurs
     */
    public HealthCheckInfo getReadiness() throws GitLabApiException {
        return (getReadiness(null));
    }

    /**
     * Get Health Checks from the readiness endpoint.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /-/readiness</code>
     * </pre>
     *
     * @param token Health Status token
     * @return HealthCheckInfo instance
     * @throws GitLabApiException if any exception occurs
     * @deprecated
     */
    public HealthCheckInfo getReadiness(String token) throws GitLabApiException {
        try {
            URL readinessUrl = getApiClient().getUrlWithBase("-", "readiness");
            GitLabApiForm formData = new GitLabApiForm().withParam("token", token, false);
            Response response = get(Response.Status.OK, formData.asMap(), readinessUrl);
            return (response.readEntity(HealthCheckInfo.class));
        } catch (IOException ioe) {
            throw (new GitLabApiException(ioe));
        }
    }

}
