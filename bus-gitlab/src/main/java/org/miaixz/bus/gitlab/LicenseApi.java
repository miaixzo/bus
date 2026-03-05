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

import org.miaixz.bus.gitlab.models.License;

import jakarta.ws.rs.core.Response;

/**
 * This class provides an entry point to all the GitLab API license calls.
 * 
 * @see <a href="https://docs.gitlab.com/ce/api/license.html">License API</a>
 */
public class LicenseApi extends AbstractApi {

    public LicenseApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Retrieve information about the current license.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /license</code>
     * </pre>
     *
     * @return a License instance holding info about the current license
     * @throws GitLabApiException if any exception occurs
     */
    public License getLicense() throws GitLabApiException {
        Response response = get(Response.Status.OK, null, "license");
        return (response.readEntity(License.class));
    }

    /**
     * Retrieve information about the current license as the value of an Optional.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /license</code>
     * </pre>
     *
     * @return the current license as the value of an Optional.
     */
    public Optional<License> getOptionalLicense() {
        try {
            return (Optional.ofNullable(getLicense()));
        } catch (GitLabApiException glae) {
            return (GitLabApi.createOptionalFromException(glae));
        }
    }

    /**
     * Retrieve information about all licenses.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /licenses</code>
     * </pre>
     *
     * @return a List of License instances
     * @throws GitLabApiException if any exception occurs
     */
    public List<License> getAllLicenses() throws GitLabApiException {
        return (getAllLicenses(getDefaultPerPage()).all());
    }

    /**
     * Get a Stream of all licenses.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /licenses</code>
     * </pre>
     *
     * @return a Stream of License instances
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<License> getAllLicensesStream() throws GitLabApiException {
        return (getAllLicenses(getDefaultPerPage()).stream());
    }

    /**
     * Get a Pager of all licenses.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /licenses</code>
     * </pre>
     *
     * @param itemsPerPage the number of LicenseTemplate instances that will be fetched per page
     * @return a Pager of license template
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<License> getAllLicenses(int itemsPerPage) throws GitLabApiException {
        return (new Pager<License>(this, License.class, itemsPerPage, null, "licenses"));
    }

    /**
     * Add a new license.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /license</code>
     * </pre>
     *
     * @param licenseString the license string for the license
     * @return a License instance for the added license
     * @throws GitLabApiException if any exception occurs
     */
    public License addLicense(String licenseString) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("license", licenseString, true);
        Response response = post(Response.Status.CREATED, formData, "license");
        return (response.readEntity(License.class));
    }

    /**
     * Deletes a license.
     *
     * <pre>
     * <code>GitLab Endpoint: DELETE /license/:id</code>
     * </pre>
     *
     * @param licenseId the ID of the license to delete
     * @return a License instance for the delete license
     * @throws GitLabApiException if any exception occurs
     */
    public License deleteLicense(Long licenseId) throws GitLabApiException {
        Response response = delete(Response.Status.OK, null, "license", licenseId);
        return (response.readEntity(License.class));
    }

}
