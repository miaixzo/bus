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

import java.util.Date;
import java.util.List;

import org.miaixz.bus.gitlab.models.PersonalAccessToken;
import org.miaixz.bus.gitlab.support.ISO8601;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

/**
 * This class provides an entry point to all the GitLab API personal access token calls.
 *
 * @see <a href="https://docs.gitlab.com/ce/api/personal_access_tokens.html">Personal access token API at GitLab</a>
 */
public class PersonalAccessTokenApi extends AbstractApi {

    public PersonalAccessTokenApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Rotates the given personal access token. The token is revoked and a new one which will expire in one week is
     * created to replace it. Only working with GitLab 16.0 and above.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /personal_access_tokens/self/rotate</code>
     * </pre>
     *
     * @return the newly created PersonalAccessToken.
     * @throws GitLabApiException if any exception occurs
     */
    public PersonalAccessToken rotatePersonalAccessToken() throws GitLabApiException {
        return rotatePersonalAccessToken(null);
    }

    /**
     * Rotates the personal access token used in the request header. The token is revoked and a new one which will
     * expire at the given expiresAt-date is created to replace it. Only working with GitLab 16.0 and above.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /personal_access_tokens/self/rotate</code>
     * </pre>
     *
     * @param expiresAt Expiration date of the access token
     * @return the newly created PersonalAccessToken.
     * @throws GitLabApiException if any exception occurs
     */
    public PersonalAccessToken rotatePersonalAccessToken(Date expiresAt) throws GitLabApiException {
        return rotatePersonalAccessToken("self", expiresAt);
    }

    /**
     * Rotates a specific personal access token. The token is revoked and a new one which will expire at the given
     * expiresAt-date is created to replace it. Only working with GitLab 16.0 and above.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /personal_access_tokens/:id/rotate</code>
     * </pre>
     *
     * @param id        ID of the personal access token
     * @param expiresAt Expiration date of the access token
     * @return the newly created PersonalAccessToken.
     * @throws GitLabApiException if any exception occurs
     */
    public PersonalAccessToken rotatePersonalAccessToken(String id, Date expiresAt) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("expires_at", ISO8601.dateOnly(expiresAt));

        Response response = post(Response.Status.OK, formData, "personal_access_tokens", id, "rotate");
        return (response.readEntity(PersonalAccessToken.class));
    }

    /**
     * Get information about the personal access token used in the request header. Only working with GitLab 16.0 and
     * above.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /personal_access_tokens</code>
     * </pre>
     *
     * @return the specified PersonalAccessToken.
     * @throws GitLabApiException if any exception occurs
     */
    public List<PersonalAccessToken> getPersonalAccessTokens() throws GitLabApiException {
        Response response = get(Response.Status.OK, null, "personal_access_tokens");
        return response.readEntity(new GenericType<List<PersonalAccessToken>>() {
        });
    }

    /**
     * Get information about the personal access token used in the request header. Only working with GitLab 16.0 and
     * above.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /personal_access_tokens/self</code>
     * </pre>
     *
     * @return the specified PersonalAccessToken.
     * @throws GitLabApiException if any exception occurs
     */
    public PersonalAccessToken getPersonalAccessToken() throws GitLabApiException {
        return getPersonalAccessToken("self");
    }

    /**
     * Get a specific personal access token. Only working with GitLab 16.0 and above.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /personal_access_tokens/:id</code>
     * </pre>
     *
     * @param id ID of the personal access token
     * @return the specified PersonalAccessToken.
     * @throws GitLabApiException if any exception occurs
     */
    public PersonalAccessToken getPersonalAccessToken(String id) throws GitLabApiException {
        Response response = get(Response.Status.OK, null, "personal_access_tokens", id);
        return (response.readEntity(PersonalAccessToken.class));
    }

    /**
     * Revokes a personal access token. Available only for admin users.
     *
     * <pre>
     * <code>GitLab Endpoint: DELETE /personal_access_tokens/:token_id</code>
     * </pre>
     * 
     * @param tokenId the personal access token ID to revoke
     * @throws GitLabApiException if any exception occurs
     */
    public void revokePersonalAccessToken(Long tokenId) throws GitLabApiException {
        if (tokenId == null) {
            throw new RuntimeException("tokenId cannot be null");
        }
        delete(Response.Status.NO_CONTENT, null, "personal_access_tokens", tokenId);
    }

}
