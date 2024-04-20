/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.gitlab;

import com.fasterxml.jackson.databind.JsonNode;
import org.miaixz.bus.gitlab.models.ApplicationSettings;
import org.miaixz.bus.gitlab.models.Setting;
import org.miaixz.bus.gitlab.support.ISO8601;

import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.Iterator;

/**
 * This class implements the client side API for the GitLab Application Settings API.
 * See <a href="https://docs.gitlab.com/ee/api/settings.html">Application Settings API at GitLab</a> for more information.
 */
public class ApplicationSettingsApi extends AbstractApi {

    public ApplicationSettingsApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Parses the returned JSON and returns an ApplicationSettings instance.
     *
     * @param root the root JsonNode
     * @return the populated ApplicationSettings instance
     * @throws GitLabApiException if any error occurs
     */
    public static final ApplicationSettings parseApplicationSettings(JsonNode root) throws GitLabApiException {

        ApplicationSettings appSettings = new ApplicationSettings();

        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            switch (fieldName) {
                case "id":
                    appSettings.setId(root.path(fieldName).asLong());
                    break;

                case "created_at":
                    try {
                        String value = root.path(fieldName).asText();
                        appSettings.setCreatedAt(ISO8601.toDate(value));
                    } catch (ParseException pe) {
                        throw new GitLabApiException(pe);
                    }
                    break;

                case "updated_at":
                    try {
                        String value = root.path(fieldName).asText();
                        appSettings.setUpdatedAt(ISO8601.toDate(value));
                    } catch (ParseException pe) {
                        throw new GitLabApiException(pe);
                    }
                    break;

                default:

                    Setting setting = Setting.forValue(fieldName);
                    if (setting != null) {
                        appSettings.addSetting(setting, root.path(fieldName));
                    } else {
                        GitLabApi.getLogger().warning(String.format("Unknown setting: %s, type: %s",
                                fieldName, root.path(fieldName).getClass().getSimpleName()));
                        appSettings.addSetting(fieldName, root.path(fieldName));
                    }

                    break;
            }
        }

        return (appSettings);
    }

    /**
     * Get the current application settings of the GitLab instance.
     *
     * <pre><code>GitLab Endpoint: GET /api/v4/application/settings</code></pre>
     *
     * @return an ApplicationSettings instance containing the current application settings of the GitLab instance.
     * @throws GitLabApiException if any exception occurs
     */
    public ApplicationSettings getApplicationSettings() throws GitLabApiException {

        Response response = get(Response.Status.OK, null, "application", "settings");
        JsonNode root = response.readEntity(JsonNode.class);
        return (parseApplicationSettings(root));
    }

    /**
     * Update the application settings of the GitLab instance with the settings in the
     * provided ApplicationSettings instance.
     *
     * <pre><code>GitLab Endpoint: PUT /api/v4/application/settings</code></pre>
     *
     * @param appSettings the ApplicationSettings instance holding the settings and values to update
     * @return the updated application settings in an ApplicationSettings instance
     * @throws GitLabApiException if any exception occurs
     */
    public ApplicationSettings updateApplicationSettings(ApplicationSettings appSettings) throws GitLabApiException {

        if (appSettings == null || appSettings.getSettings().isEmpty()) {
            throw new GitLabApiException("ApplicationSettings cannot be null or empty.");
        }

        final GitLabApiForm form = new GitLabApiForm();
        appSettings.getSettings().forEach((s, v) -> form.withParam(s, v));
        Response response = put(Response.Status.OK, form.asMap(), "application", "settings");
        JsonNode root = response.readEntity(JsonNode.class);
        return (parseApplicationSettings(root));
    }

    /**
     * Update a single application setting of the GitLab instance with the provided settings and value.
     *
     * <pre><code>GitLab Endpoint: PUT /api/v4/application/settings</code></pre>
     *
     * @param setting the ApplicationSetting to update
     * @param value   the new value for the application setting
     * @return the updated application settings in an ApplicationSettings instance
     * @throws GitLabApiException if any exception occurs
     */
    public ApplicationSettings updateApplicationSetting(Setting setting, Object value) throws GitLabApiException {

        if (setting == null) {
            throw new GitLabApiException("setting cannot be null.");
        }

        return (updateApplicationSetting(setting.toString(), value));
    }

    /**
     * Update a single application setting of the GitLab instance with the provided settings and value.
     *
     * <pre><code>GitLab Endpoint: PUT /api/v4/application/settings</code></pre>
     *
     * @param setting the ApplicationSetting to update
     * @param value   the new value for the application setting
     * @return the updated application settings in an ApplicationSettings instance
     * @throws GitLabApiException if any exception occurs
     */
    public ApplicationSettings updateApplicationSetting(String setting, Object value) throws GitLabApiException {

        if (setting == null || setting.trim().isEmpty()) {
            throw new GitLabApiException("setting cannot be null or empty.");
        }

        GitLabApiForm form = new GitLabApiForm().withParam(setting, value);
        Response response = put(Response.Status.OK, form.asMap(), "application", "settings");
        JsonNode root = response.readEntity(JsonNode.class);
        return (parseApplicationSettings(root));
    }
}
