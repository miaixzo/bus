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
package org.miaixz.bus.gitlab;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.miaixz.bus.gitlab.models.LicenseTemplate;

import jakarta.ws.rs.core.Response;

/**
 * This class provides an entry point to all the GitLab API licenses calls.
 * 
 * @see <a href="https://docs.gitlab.com/ce/api/templates/licenses.html">Licenses API</a>
 */
public class LicenseTemplatesApi extends AbstractApi {

    public LicenseTemplatesApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get a List of all license templates.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /templates/licenses</code>
     * </pre>
     *
     * @return a List of LicenseTemplate instances
     * @throws GitLabApiException if any exception occurs
     */
    public List<LicenseTemplate> getLicenseTemplates() throws GitLabApiException {
        return (getLicenseTemplates(false, getDefaultPerPage()).all());
    }

    /**
     * Get a Stream of all license templates.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /templates/licenses</code>
     * </pre>
     *
     * @return a Stream of LicenseTemplate instances
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<LicenseTemplate> getLicenseTemplatesStream() throws GitLabApiException {
        return (getLicenseTemplates(false, getDefaultPerPage()).stream());
    }

    /**
     * Get a Pager of all license templates.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /templates/licenses</code>
     * </pre>
     *
     * @param itemsPerPage the number of LicenseTemplate instances that will be fetched per page
     * @return a Pager of LicenseTemplate instances
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<LicenseTemplate> getLicenseTemplates(int itemsPerPage) throws GitLabApiException {
        return (getLicenseTemplates(false, itemsPerPage));
    }

    /**
     * Get a List of popular license templates.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /templates/licenses?popular=true</code>
     * </pre>
     *
     * @return a List of popular LicenseTemplate instances
     * @throws GitLabApiException if any exception occurs
     */
    public List<LicenseTemplate> getPopularLicenseTemplates() throws GitLabApiException {
        return (getLicenseTemplates(true, getDefaultPerPage()).all());
    }

    /**
     * Get a Stream of popular license templates.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /templates/licenses?popular=true</code>
     * </pre>
     *
     * @return a Stream of popular LicenseTemplate instances
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<LicenseTemplate> getPopularLicenseTemplatesStream() throws GitLabApiException {
        return (getLicenseTemplates(true, getDefaultPerPage()).stream());
    }

    /**
     * Get a Pager of license templates.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /templates/licenses</code>
     * </pre>
     *
     * @param popular      if true, returns only popular licenses.
     * @param itemsPerPage the number of LicenseTemplate instances that will be fetched per page
     * @return a Pager of LicenseTemplate instances
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<LicenseTemplate> getLicenseTemplates(Boolean popular, int itemsPerPage) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("popular", popular);
        return (new Pager<LicenseTemplate>(this, LicenseTemplate.class, itemsPerPage, formData.asMap(), "templates",
                "licenses"));
    }

    /**
     * Get a single license template.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /templates/licenses/:key</code>
     * </pre>
     *
     * @param key The key of the license template
     * @return a LicenseTemplate instance
     * @throws GitLabApiException if any exception occurs
     */
    public LicenseTemplate getLicenseTemplate(String key) throws GitLabApiException {
        Response response = get(Response.Status.OK, null, "licenses", key);
        return (response.readEntity(LicenseTemplate.class));
    }

    /**
     * Get a single license template as the value of an Optional.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /templates/licenses/:key</code>
     * </pre>
     *
     * @param key The key of the license template
     * @return a single license template as the value of an Optional.
     */
    public Optional<LicenseTemplate> getOptionalLicenseTemplate(String key) {
        try {
            return (Optional.ofNullable(getLicenseTemplate(key)));
        } catch (GitLabApiException glae) {
            return (GitLabApi.createOptionalFromException(glae));
        }
    }

}
