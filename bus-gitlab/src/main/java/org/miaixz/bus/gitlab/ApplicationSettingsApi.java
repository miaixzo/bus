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

import java.text.ParseException;
import java.util.Iterator;

import jakarta.ws.rs.core.Response;

import org.miaixz.bus.core.lang.exception.RelevantException;
import org.miaixz.bus.gitlab.models.ApplicationSettings;
import org.miaixz.bus.gitlab.models.Setting;
import org.miaixz.bus.gitlab.support.ISO8601;
import org.miaixz.bus.logger.Logger;

import tools.jackson.databind.JsonNode;

/**
 * This class implements the client side API for the GitLab Application Settings API. See
 * <a href="https://docs.gitlab.com/ee/api/settings.html">Application Settings API at GitLab</a> for more information.
 *
 * @author Kimi Liu
 */
public class ApplicationSettingsApi extends AbstractApi {

    /**
     * Executes the application settings api operation.
     *
     * @param gitLabApi the git lab api value
     */

    public ApplicationSettingsApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get the current application settings of the GitLab instance.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /api/v4/application/settings</code>
     * </pre>
     *
     * @return an ApplicationSettings instance containing the current application settings of the GitLab instance.
     * @throws RelevantException if any exception occurs
     */
    public ApplicationSettings getApplicationSettings() throws RelevantException {

        Response response = get(Response.Status.OK, null, "application", "settings");
        JsonNode root = response.readEntity(JsonNode.class);
        return (parseApplicationSettings(root));
    }

    /**
     * Update the application settings of the GitLab instance with the settings in the provided ApplicationSettings
     * instance.
     *
     * <pre>
     * <code>GitLab Endpoint: PUT /api/v4/application/settings</code>
     * </pre>
     *
     * @param appSettings the ApplicationSettings instance holding the settings and values to update
     * @return the updated application settings in an ApplicationSettings instance
     * @throws RelevantException if any exception occurs
     */
    public ApplicationSettings updateApplicationSettings(ApplicationSettings appSettings) throws RelevantException {

        if (appSettings == null || appSettings.getSettings().isEmpty()) {
            throw GitLabFailure.exception("ApplicationSettings cannot be null or empty.");
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
     * <pre>
     * <code>GitLab Endpoint: PUT /api/v4/application/settings</code>
     * </pre>
     *
     * @param setting the ApplicationSetting to update
     * @param value   the new value for the application setting
     * @return the updated application settings in an ApplicationSettings instance
     * @throws RelevantException if any exception occurs
     */
    public ApplicationSettings updateApplicationSetting(Setting setting, Object value) throws RelevantException {

        if (setting == null) {
            throw GitLabFailure.exception("setting cannot be null.");
        }

        return (updateApplicationSetting(setting.toString(), value));
    }

    /**
     * Update a single application setting of the GitLab instance with the provided settings and value.
     *
     * <pre>
     * <code>GitLab Endpoint: PUT /api/v4/application/settings</code>
     * </pre>
     *
     * @param setting the ApplicationSetting to update
     * @param value   the new value for the application setting
     * @return the updated application settings in an ApplicationSettings instance
     * @throws RelevantException if any exception occurs
     */
    public ApplicationSettings updateApplicationSetting(String setting, Object value) throws RelevantException {

        if (setting == null || setting.trim().isEmpty()) {
            throw GitLabFailure.exception("setting cannot be null or empty.");
        }

        GitLabApiForm form = new GitLabApiForm().withParam(setting, value);
        Response response = put(Response.Status.OK, form.asMap(), "application", "settings");
        JsonNode root = response.readEntity(JsonNode.class);
        return (parseApplicationSettings(root));
    }

    /**
     * Parses the returned JSON and returns an ApplicationSettings instance.
     *
     * @param root the root JsonNode
     * @return the populated ApplicationSettings instance
     * @throws RelevantException if any error occurs
     */
    public static ApplicationSettings parseApplicationSettings(JsonNode root) throws RelevantException {

        ApplicationSettings appSettings = new ApplicationSettings();

        Iterator<String> fieldNames = root.propertyNames().iterator();
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
                        Logger.warn(
                                false,
                                "GitLab",
                                pe,
                                "GitLab application setting date parsing failed: fieldName={}, valueLength={}, exception={}",
                                fieldName,
                                root.path(fieldName).asText().length(),
                                pe.getClass().getSimpleName());
                        throw GitLabFailure.exception(pe);
                    }
                    break;

                case "updated_at":
                    try {
                        String value = root.path(fieldName).asText();
                        appSettings.setUpdatedAt(ISO8601.toDate(value));
                    } catch (ParseException pe) {
                        Logger.warn(
                                false,
                                "GitLab",
                                pe,
                                "GitLab application setting date parsing failed: fieldName={}, valueLength={}, exception={}",
                                fieldName,
                                root.path(fieldName).asText().length(),
                                pe.getClass().getSimpleName());
                        throw GitLabFailure.exception(pe);
                    }
                    break;

                default:
                    Setting setting = Setting.forValue(fieldName);
                    if (setting != null) {
                        appSettings.addSetting(setting, root.path(fieldName));
                    } else {
                        Logger.warn(
                                false,
                                "GitLab",
                                "GitLab application setting preserved as unknown field: fieldName={}, nodeType={}",
                                fieldName,
                                root.path(fieldName).getClass().getSimpleName());
                        appSettings.addSetting(fieldName, root.path(fieldName));
                    }

                    break;
            }
        }

        return (appSettings);
    }

}
