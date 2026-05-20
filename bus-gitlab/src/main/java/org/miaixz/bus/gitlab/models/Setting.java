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
package org.miaixz.bus.gitlab.models;

import java.lang.reflect.Array;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * This enum provides constants and value validation for the available GitLab application settings. See
 * <a href="https://docs.gitlab.com/ce/api/settings.html#list-of-settings-that-can-be-accessed-via-api-calls"> List of
 * settings that can be accessed via API calls</a> for more information.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Setting {

    /**
     * The admin mode provides.
     */
    ADMIN_MODE(Boolean.class),
    /**
     * The abuse notification email provides.
     */
    ABUSE_NOTIFICATION_EMAIL(String.class),
    /**
     * The notify on unknown sign in provides.
     */
    NOTIFY_ON_UNKNOWN_SIGN_IN(Boolean.class),
    /**
     * The after sign out path provides.
     */
    AFTER_SIGN_OUT_PATH(String.class),
    /**
     * The after sign up text provides.
     */
    AFTER_SIGN_UP_TEXT(String.class),
    /**
     * The akismet api key provides.
     */
    AKISMET_API_KEY(String.class),
    /**
     * The akismet enabled provides.
     */
    AKISMET_ENABLED(Boolean.class),
    /**
     * The allow group owners to manage ldap provides.
     */
    ALLOW_GROUP_OWNERS_TO_MANAGE_LDAP(Boolean.class),
    /**
     * The allow local requests from system hooks provides.
     */
    ALLOW_LOCAL_REQUESTS_FROM_SYSTEM_HOOKS(Boolean.class),
    /**
     * The allow local requests from web hooks and services provides.
     */
    ALLOW_LOCAL_REQUESTS_FROM_WEB_HOOKS_AND_SERVICES(Boolean.class),
    /**
     * The allow project creation for guest and below provides.
     */
    ALLOW_PROJECT_CREATION_FOR_GUEST_AND_BELOW(Boolean.class),
    /**
     * The allow runner registration token provides.
     */
    ALLOW_RUNNER_REGISTRATION_TOKEN(Boolean.class),
    /**
     * The archive builds in human readable provides.
     */
    ARCHIVE_BUILDS_IN_HUMAN_READABLE(String.class),
    /**
     * The asset proxy enabled provides.
     */
    ASSET_PROXY_ENABLED(Boolean.class),
    /**
     * The asset proxy secret key provides.
     */
    ASSET_PROXY_SECRET_KEY(String.class),
    /**
     * The asset proxy url provides.
     */
    ASSET_PROXY_URL(String.class),
    /**
     * The asset proxy allowlist provides.
     */
    ASSET_PROXY_ALLOWLIST(new Class<?>[] { String.class, String[].class }),
    /**
     * The authorized keys enabled provides.
     */
    AUTHORIZED_KEYS_ENABLED(Boolean.class),
    /**
     * The auto devops domain provides.
     */
    AUTO_DEVOPS_DOMAIN(String.class),
    /**
     * The auto devops enabled provides.
     */
    AUTO_DEVOPS_ENABLED(Boolean.class),
    /**
     * The check namespace plan provides.
     */
    CHECK_NAMESPACE_PLAN(Boolean.class),
    /**
     * The commit email hostname provides.
     */
    COMMIT_EMAIL_HOSTNAME(String.class),
    /**
     * The container registry token expire delay provides.
     */
    CONTAINER_REGISTRY_TOKEN_EXPIRE_DELAY(Integer.class),
    /**
     * The default artifacts expire in provides.
     */
    DEFAULT_ARTIFACTS_EXPIRE_IN(String.class),
    /**
     * The default branch protection provides.
     */
    DEFAULT_BRANCH_PROTECTION(Integer.class),
    /**
     * The default ci config path provides.
     */
    DEFAULT_CI_CONFIG_PATH(String.class),
    /**
     * The default group visibility provides.
     */
    DEFAULT_GROUP_VISIBILITY(String.class),
    /**
     * The default project creation provides.
     */
    DEFAULT_PROJECT_CREATION(Integer.class),
    /**
     * The default project visibility provides.
     */
    DEFAULT_PROJECT_VISIBILITY(String.class),
    /**
     * The default projects limit provides.
     */
    DEFAULT_PROJECTS_LIMIT(Integer.class),
    /**
     * The default snippet visibility provides.
     */
    DEFAULT_SNIPPET_VISIBILITY(String.class),
    /**
     * The diff max patch bytes provides.
     */
    DIFF_MAX_PATCH_BYTES(Integer.class),
    /**
     * The disabled oauth sign in sources provides.
     */
    DISABLED_OAUTH_SIGN_IN_SOURCES(String[].class),
    /**
     * The dns rebinding protection enabled provides.
     */
    DNS_REBINDING_PROTECTION_ENABLED(Boolean.class),
    /**
     * The domain blacklist provides.
     */
    DOMAIN_BLACKLIST(String[].class),
    /**
     * The domain blacklist enabled provides.
     */
    DOMAIN_BLACKLIST_ENABLED(Boolean.class),
    /**
     * The domain whitelist provides.
     */
    DOMAIN_WHITELIST(String[].class),
    /**
     * The dsa key restriction provides.
     */
    DSA_KEY_RESTRICTION(Integer.class),
    /**
     * The ecdsa key restriction provides.
     */
    ECDSA_KEY_RESTRICTION(Integer.class),
    /**
     * The ed25519 key restriction provides.
     */
    ED25519_KEY_RESTRICTION(Integer.class),
    /**
     * The elasticsearch aws provides.
     */
    ELASTICSEARCH_AWS(Boolean.class),
    /**
     * The elasticsearch aws access key provides.
     */
    ELASTICSEARCH_AWS_ACCESS_KEY(String.class),
    /**
     * The elasticsearch aws region provides.
     */
    ELASTICSEARCH_AWS_REGION(String.class),
    /**
     * The elasticsearch aws secret access key provides.
     */
    ELASTICSEARCH_AWS_SECRET_ACCESS_KEY(String.class),
    /**
     * The eks access key id provides.
     */
    EKS_ACCESS_KEY_ID(String.class),
    /**
     * The eks account id provides.
     */
    EKS_ACCOUNT_ID(String.class),
    /**
     * The eks integration enabled provides.
     */
    EKS_INTEGRATION_ENABLED(Boolean.class),
    /**
     * The eks secret access key provides.
     */
    EKS_SECRET_ACCESS_KEY(String.class),
    /**
     * The elasticsearch indexing provides.
     */
    ELASTICSEARCH_INDEXING(Boolean.class),
    /**
     * The elasticsearch limit indexing provides.
     */
    ELASTICSEARCH_LIMIT_INDEXING(Boolean.class),
    /**
     * The elasticsearch namespace ids provides.
     */
    ELASTICSEARCH_NAMESPACE_IDS(Integer[].class),
    /**
     * The elasticsearch project ids provides.
     */
    ELASTICSEARCH_PROJECT_IDS(Integer[].class),
    /**
     * The elasticsearch search provides.
     */
    ELASTICSEARCH_SEARCH(Boolean.class),
    /**
     * The elasticsearch url provides.
     */
    ELASTICSEARCH_URL(String[].class),
    /**
     * The email additional text provides.
     */
    EMAIL_ADDITIONAL_TEXT(String.class),
    /**
     * The email author in body provides.
     */
    EMAIL_AUTHOR_IN_BODY(Boolean.class),
    /**
     * The enabled git access protocol provides.
     */
    ENABLED_GIT_ACCESS_PROTOCOL(String.class),
    /**
     * The enforce terms provides.
     */
    ENFORCE_TERMS(Boolean.class),
    /**
     * The external auth client cert provides.
     */
    EXTERNAL_AUTH_CLIENT_CERT(String.class),
    /**
     * The external auth client key provides.
     */
    EXTERNAL_AUTH_CLIENT_KEY(String.class),
    /**
     * The external auth client key pass provides.
     */
    EXTERNAL_AUTH_CLIENT_KEY_PASS(String.class),
    /**
     * The external authorization service default label provides.
     */
    EXTERNAL_AUTHORIZATION_SERVICE_DEFAULT_LABEL(String.class),
    /**
     * The external authorization service enabled provides.
     */
    EXTERNAL_AUTHORIZATION_SERVICE_ENABLED(Boolean.class),
    /**
     * The external authorization service timeout provides.
     */
    EXTERNAL_AUTHORIZATION_SERVICE_TIMEOUT(Float.class),
    /**
     * The external authorization service url provides.
     */
    EXTERNAL_AUTHORIZATION_SERVICE_URL(String.class),
    /**
     * The file template project id provides.
     */
    FILE_TEMPLATE_PROJECT_ID(Integer.class),
    /**
     * The first day of week provides.
     */
    FIRST_DAY_OF_WEEK(Integer.class),
    /**
     * The geo node allowed ips provides.
     */
    GEO_NODE_ALLOWED_IPS(String.class),
    /**
     * The geo status timeout provides.
     */
    GEO_STATUS_TIMEOUT(Integer.class),
    /**
     * The gitaly timeout default provides.
     */
    GITALY_TIMEOUT_DEFAULT(Integer.class),
    /**
     * The gitaly timeout fast provides.
     */
    GITALY_TIMEOUT_FAST(Integer.class),
    /**
     * The gitaly timeout medium provides.
     */
    GITALY_TIMEOUT_MEDIUM(Integer.class),
    /**
     * The grafana enabled provides.
     */
    GRAFANA_ENABLED(Boolean.class),
    /**
     * The grafana url provides.
     */
    GRAFANA_URL(String.class),
    /**
     * The gravatar enabled provides.
     */
    GRAVATAR_ENABLED(Boolean.class),
    /**
     * The hashed storage enabled provides.
     */
    HASHED_STORAGE_ENABLED(Boolean.class),
    /**
     * The help page hide commercial content provides.
     */
    HELP_PAGE_HIDE_COMMERCIAL_CONTENT(Boolean.class),
    /**
     * The help page support url provides.
     */
    HELP_PAGE_SUPPORT_URL(String.class),
    /**
     * The help page text provides.
     */
    HELP_PAGE_TEXT(String.class),
    /**
     * The help text provides.
     */
    HELP_TEXT(String.class),
    /**
     * The hide third party offers provides.
     */
    HIDE_THIRD_PARTY_OFFERS(Boolean.class),
    /**
     * The home page url provides.
     */
    HOME_PAGE_URL(String.class),
    /**
     * The housekeeping bitmaps enabled provides.
     */
    HOUSEKEEPING_BITMAPS_ENABLED(Boolean.class),
    /**
     * The housekeeping enabled provides.
     */
    HOUSEKEEPING_ENABLED(Boolean.class),
    /**
     * The housekeeping full repack period provides.
     */
    HOUSEKEEPING_FULL_REPACK_PERIOD(Integer.class),
    /**
     * The housekeeping gc period provides.
     */
    HOUSEKEEPING_GC_PERIOD(Integer.class),
    /**
     * The housekeeping incremental repack period provides.
     */
    HOUSEKEEPING_INCREMENTAL_REPACK_PERIOD(Integer.class),
    /**
     * The html emails enabled provides.
     */
    HTML_EMAILS_ENABLED(Boolean.class),
    /**
     * The import sources provides.
     */
    IMPORT_SOURCES(String[].class),
    /**
     * The instance statistics visibility private provides.
     */
    INSTANCE_STATISTICS_VISIBILITY_PRIVATE(Boolean.class),
    /**
     * The local markdown version provides.
     */
    LOCAL_MARKDOWN_VERSION(Integer.class),
    /**
     * The login recaptcha protection enabled provides.
     */
    LOGIN_RECAPTCHA_PROTECTION_ENABLED(Boolean.class),
    /**
     * The max artifacts size provides.
     */
    MAX_ARTIFACTS_SIZE(Integer.class),
    /**
     * The max attachment size provides.
     */
    MAX_ATTACHMENT_SIZE(Integer.class),
    /**
     * The max import size provides.
     */
    MAX_IMPORT_SIZE(Integer.class),
    /**
     * The max pages size provides.
     */
    MAX_PAGES_SIZE(Integer.class),
    /**
     * The metrics enabled provides.
     */
    METRICS_ENABLED(Boolean.class),
    /**
     * The metrics host provides.
     */
    METRICS_HOST(String.class),
    /**
     * The metrics method call threshold provides.
     */
    METRICS_METHOD_CALL_THRESHOLD(Integer.class),
    /**
     * The metrics packet size provides.
     */
    METRICS_PACKET_SIZE(Integer.class),
    /**
     * The metrics pool size provides.
     */
    METRICS_POOL_SIZE(Integer.class),
    /**
     * The metrics port provides.
     */
    METRICS_PORT(Integer.class),
    /**
     * The metrics sample interval provides.
     */
    METRICS_SAMPLE_INTERVAL(Integer.class),
    /**
     * The metrics timeout provides.
     */
    METRICS_TIMEOUT(Integer.class),
    /**
     * The mirror available provides.
     */
    MIRROR_AVAILABLE(Boolean.class),
    /**
     * The mirror capacity threshold provides.
     */
    MIRROR_CAPACITY_THRESHOLD(Integer.class),
    /**
     * The mirror max capacity provides.
     */
    MIRROR_MAX_CAPACITY(Integer.class),
    /**
     * The mirror max delay provides.
     */
    MIRROR_MAX_DELAY(Integer.class),
    /**
     * The outbound local requests whitelist provides.
     */
    OUTBOUND_LOCAL_REQUESTS_WHITELIST(String[].class),
    /**
     * The outbound local requests whitelist raw provides.
     */
    OUTBOUND_LOCAL_REQUESTS_WHITELIST_RAW(String.class),
    /**
     * The pages domain verification enabled provides.
     */
    PAGES_DOMAIN_VERIFICATION_ENABLED(Boolean.class),
    /**
     * The password authentication enabled for git provides.
     */
    PASSWORD_AUTHENTICATION_ENABLED_FOR_GIT(Boolean.class),
    /**
     * The password authentication enabled for web provides.
     */
    PASSWORD_AUTHENTICATION_ENABLED_FOR_WEB(Boolean.class),
    /**
     * The performance bar allowed group path provides.
     */
    PERFORMANCE_BAR_ALLOWED_GROUP_PATH(String.class),
    /**
     * The plantuml enabled provides.
     */
    PLANTUML_ENABLED(Boolean.class),
    /**
     * The plantuml url provides.
     */
    PLANTUML_URL(String.class),
    /**
     * The polling interval multiplier provides.
     */
    POLLING_INTERVAL_MULTIPLIER(String.class),
    /**
     * The project export enabled provides.
     */
    PROJECT_EXPORT_ENABLED(Boolean.class),
    /**
     * The project jobs api rate limit provides.
     */
    PROJECT_JOBS_API_RATE_LIMIT(Integer.class),
    /**
     * The prometheus metrics enabled provides.
     */
    PROMETHEUS_METRICS_ENABLED(Boolean.class),
    /**
     * The protected ci variables provides.
     */
    PROTECTED_CI_VARIABLES(Boolean.class),
    /**
     * The pseudonymizer enabled provides.
     */
    PSEUDONYMIZER_ENABLED(Boolean.class),
    /**
     * The push event hooks limit provides.
     */
    PUSH_EVENT_HOOKS_LIMIT(Integer.class),
    /**
     * The push event activities limit provides.
     */
    PUSH_EVENT_ACTIVITIES_LIMIT(Integer.class),
    /**
     * The recaptcha enabled provides.
     */
    RECAPTCHA_ENABLED(Boolean.class),
    /**
     * The recaptcha private key provides.
     */
    RECAPTCHA_PRIVATE_KEY(String.class),
    /**
     * The recaptcha site key provides.
     */
    RECAPTCHA_SITE_KEY(String.class),
    /**
     * The receive max input size provides.
     */
    RECEIVE_MAX_INPUT_SIZE(Integer.class),
    /**
     * The repository checks enabled provides.
     */
    REPOSITORY_CHECKS_ENABLED(Boolean.class),
    /**
     * The repository size limit provides.
     */
    REPOSITORY_SIZE_LIMIT(Integer.class),
    /**
     * The repository storages provides.
     */
    REPOSITORY_STORAGES(String[].class),
    /**
     * The require two factor authentication provides.
     */
    REQUIRE_TWO_FACTOR_AUTHENTICATION(Boolean.class),
    /**
     * The restricted visibility levels provides.
     */
    RESTRICTED_VISIBILITY_LEVELS(String[].class),
    /**
     * The rsa key restriction provides.
     */
    RSA_KEY_RESTRICTION(Integer.class),
    /**
     * The send user confirmation email provides.
     */
    SEND_USER_CONFIRMATION_EMAIL(Boolean.class),
    /**
     * The session expire delay provides.
     */
    SESSION_EXPIRE_DELAY(Integer.class),
    /**
     * The shared runners enabled provides.
     */
    SHARED_RUNNERS_ENABLED(Boolean.class),
    /**
     * The shared runners minutes provides.
     */
    SHARED_RUNNERS_MINUTES(Integer.class),
    /**
     * The shared runners text provides.
     */
    SHARED_RUNNERS_TEXT(String.class),
    /**
     * The sign in text provides.
     */
    SIGN_IN_TEXT(String.class),
    /**
     * The signup enabled provides.
     */
    SIGNUP_ENABLED(Boolean.class),
    /**
     * The slack app enabled provides.
     */
    SLACK_APP_ENABLED(Boolean.class),
    /**
     * The slack app id provides.
     */
    SLACK_APP_ID(String.class),
    /**
     * The slack app secret provides.
     */
    SLACK_APP_SECRET(String.class),
    /**
     * The slack app verification token provides.
     */
    SLACK_APP_VERIFICATION_TOKEN(String.class),
    /**
     * The snowplow app id provides.
     */
    SNOWPLOW_APP_ID(String.class),
    /**
     * The snowplow collector hostname provides.
     */
    SNOWPLOW_COLLECTOR_HOSTNAME(String.class),
    /**
     * The snowplow cookie domain provides.
     */
    SNOWPLOW_COOKIE_DOMAIN(String.class),
    /**
     * The snowplow enabled provides.
     */
    SNOWPLOW_ENABLED(Boolean.class),
    /**
     * The snowplow iglu registry url provides.
     */
    SNOWPLOW_IGLU_REGISTRY_URL(String.class),
    /**
     * The snowplow site id provides.
     */
    SNOWPLOW_SITE_ID(String.class),
    /**
     * The sourcegraph enabled provides.
     */
    SOURCEGRAPH_ENABLED(Boolean.class),
    /**
     * The sourcegraph public only provides.
     */
    SOURCEGRAPH_PUBLIC_ONLY(Boolean.class),
    /**
     * The sourcegraph url provides.
     */
    SOURCEGRAPH_URL(String.class),
    /**
     * The spam check endpoint enabled provides.
     */
    SPAM_CHECK_ENDPOINT_ENABLED(Boolean.class),
    /**
     * The spam check endpoint url provides.
     */
    SPAM_CHECK_ENDPOINT_URL(String.class),
    /**
     * The pendo url provides.
     */
    PENDO_URL(String.class),
    /**
     * The pendo enabled provides.
     */
    PENDO_ENABLED(Boolean.class),
    /**
     * The static objects external storage auth token provides.
     */
    STATIC_OBJECTS_EXTERNAL_STORAGE_AUTH_TOKEN(String.class),
    /**
     * The static objects external storage url provides.
     */
    STATIC_OBJECTS_EXTERNAL_STORAGE_URL(String.class),
    /**
     * The terminal max session time provides.
     */
    TERMINAL_MAX_SESSION_TIME(Integer.class),
    /**
     * The terms provides.
     */
    TERMS(String.class),
    /**
     * The throttle authenticated api enabled provides.
     */
    THROTTLE_AUTHENTICATED_API_ENABLED(Boolean.class),
    /**
     * The throttle authenticated api period in seconds provides.
     */
    THROTTLE_AUTHENTICATED_API_PERIOD_IN_SECONDS(Integer.class),
    /**
     * The throttle authenticated api requests per period provides.
     */
    THROTTLE_AUTHENTICATED_API_REQUESTS_PER_PERIOD(Integer.class),
    /**
     * The throttle authenticated web enabled provides.
     */
    THROTTLE_AUTHENTICATED_WEB_ENABLED(Boolean.class),
    /**
     * The throttle authenticated web period in seconds provides.
     */
    THROTTLE_AUTHENTICATED_WEB_PERIOD_IN_SECONDS(Integer.class),
    /**
     * The throttle authenticated web requests per period provides.
     */
    THROTTLE_AUTHENTICATED_WEB_REQUESTS_PER_PERIOD(Integer.class),
    /**
     * The throttle unauthenticated enabled provides.
     */
    THROTTLE_UNAUTHENTICATED_ENABLED(Boolean.class),
    /**
     * The throttle unauthenticated period in seconds provides.
     */
    THROTTLE_UNAUTHENTICATED_PERIOD_IN_SECONDS(Integer.class),
    /**
     * The throttle unauthenticated requests per period provides.
     */
    THROTTLE_UNAUTHENTICATED_REQUESTS_PER_PERIOD(Integer.class),
    /**
     * The time tracking limit to hours provides.
     */
    TIME_TRACKING_LIMIT_TO_HOURS(Boolean.class),
    /**
     * The two factor grace period provides.
     */
    TWO_FACTOR_GRACE_PERIOD(Integer.class),
    /**
     * The unique ips limit enabled provides.
     */
    UNIQUE_IPS_LIMIT_ENABLED(Boolean.class),
    /**
     * The unique ips limit per user provides.
     */
    UNIQUE_IPS_LIMIT_PER_USER(Integer.class),
    /**
     * The unique ips limit time window provides.
     */
    UNIQUE_IPS_LIMIT_TIME_WINDOW(Integer.class),
    /**
     * The usage ping enabled provides.
     */
    USAGE_PING_ENABLED(Boolean.class),
    /**
     * The user default external provides.
     */
    USER_DEFAULT_EXTERNAL(Boolean.class),
    /**
     * The user default internal regex provides.
     */
    USER_DEFAULT_INTERNAL_REGEX(String.class),
    /**
     * The user oauth applications provides.
     */
    USER_OAUTH_APPLICATIONS(Boolean.class),
    /**
     * The user show add ssh key message provides.
     */
    USER_SHOW_ADD_SSH_KEY_MESSAGE(Boolean.class),
    /**
     * The version check enabled provides.
     */
    VERSION_CHECK_ENABLED(Boolean.class),
    /**
     * The web ide clientside preview enabled provides.
     */
    WEB_IDE_CLIENTSIDE_PREVIEW_ENABLED(Boolean.class),
    /**
     * The concurrent github import jobs limit provides.
     */
    CONCURRENT_GITHUB_IMPORT_JOBS_LIMIT(Integer.class),
    /**
     * The concurrent bitbucket import jobs limit provides.
     */
    CONCURRENT_BITBUCKET_IMPORT_JOBS_LIMIT(Integer.class),
    /**
     * The concurrent bitbucket server import jobs limit provides.
     */
    CONCURRENT_BITBUCKET_SERVER_IMPORT_JOBS_LIMIT(Integer.class),
    /**
     * The container registry expiration policies caching provides.
     */
    CONTAINER_REGISTRY_EXPIRATION_POLICIES_CACHING(Boolean.class),
    /**
     * The decompress archive file timeout provides.
     */
    DECOMPRESS_ARCHIVE_FILE_TIMEOUT(Integer.class),
    /**
     * The default branch name provides.
     */
    DEFAULT_BRANCH_NAME(String.class),
    /**
     * The default branch protection defaults provides.
     */
    DEFAULT_BRANCH_PROTECTION_DEFAULTS(HashMap.class),
    /**
     * The default preferred language provides.
     */
    DEFAULT_PREFERRED_LANGUAGE(String.class),
    /**
     * The default syntax highlighting theme provides.
     */
    DEFAULT_SYNTAX_HIGHLIGHTING_THEME(Integer.class),
    /**
     * The delete inactive projects provides.
     */
    DELETE_INACTIVE_PROJECTS(Boolean.class),
    /**
     * The disable admin oauth scopes provides.
     */
    DISABLE_ADMIN_OAUTH_SCOPES(Boolean.class),
    /**
     * The disable feed token provides.
     */
    DISABLE_FEED_TOKEN(Boolean.class),
    /**
     * The domain denylist provides.
     */
    DOMAIN_DENYLIST(String[].class),
    /**
     * The domain denylist enabled provides.
     */
    DOMAIN_DENYLIST_ENABLED(Boolean.class),
    /**
     * The domain allowlist provides.
     */
    DOMAIN_ALLOWLIST(String[].class),
    /**
     * The ecdsa sk key restriction provides.
     */
    ECDSA_SK_KEY_RESTRICTION(Integer.class),
    /**
     * The ed25519 sk key restriction provides.
     */
    ED25519_SK_KEY_RESTRICTION(Integer.class),
    /**
     * The email confirmation setting provides.
     */
    EMAIL_CONFIRMATION_SETTING(String.class),
    /**
     * The external pipeline validation service timeout provides.
     */
    EXTERNAL_PIPELINE_VALIDATION_SERVICE_TIMEOUT(Integer.class),
    /**
     * The external pipeline validation service token provides.
     */
    EXTERNAL_PIPELINE_VALIDATION_SERVICE_TOKEN(String.class),
    /**
     * The external pipeline validation service url provides.
     */
    EXTERNAL_PIPELINE_VALIDATION_SERVICE_URL(String.class),
    /**
     * The failed login attempts unlock period in minutes provides.
     */
    FAILED_LOGIN_ATTEMPTS_UNLOCK_PERIOD_IN_MINUTES(Integer.class),
    /**
     * The gitpod enabled provides.
     */
    GITPOD_ENABLED(Boolean.class),
    /**
     * The gitpod url provides.
     */
    GITPOD_URL(String.class),
    /**
     * The housekeeping optimize repository period provides.
     */
    HOUSEKEEPING_OPTIMIZE_REPOSITORY_PERIOD(Integer.class),
    /**
     * The inactive projects delete after months provides.
     */
    INACTIVE_PROJECTS_DELETE_AFTER_MONTHS(Integer.class),
    /**
     * The inactive projects min size mb provides.
     */
    INACTIVE_PROJECTS_MIN_SIZE_MB(Integer.class),
    /**
     * The inactive projects send warning email after months provides.
     */
    INACTIVE_PROJECTS_SEND_WARNING_EMAIL_AFTER_MONTHS(Integer.class),
    /**
     * The include optional metrics in service ping provides.
     */
    INCLUDE_OPTIONAL_METRICS_IN_SERVICE_PING(Boolean.class),
    /**
     * The invisible captcha enabled provides.
     */
    INVISIBLE_CAPTCHA_ENABLED(Boolean.class),
    /**
     * The jira connect application key provides.
     */
    JIRA_CONNECT_APPLICATION_KEY(String.class),
    /**
     * The jira connect public key storage enabled provides.
     */
    JIRA_CONNECT_PUBLIC_KEY_STORAGE_ENABLED(Boolean.class),
    /**
     * The jira connect proxy url provides.
     */
    JIRA_CONNECT_PROXY_URL(String.class),
    /**
     * The max decompressed archive size provides.
     */
    MAX_DECOMPRESSED_ARCHIVE_SIZE(Integer.class),
    /**
     * The max export size provides.
     */
    MAX_EXPORT_SIZE(Integer.class),
    /**
     * The max import remote file size provides.
     */
    MAX_IMPORT_REMOTE_FILE_SIZE(Integer.class),
    /**
     * The max login attempts provides.
     */
    MAX_LOGIN_ATTEMPTS(Integer.class),
    /**
     * The max terraform state size bytes provides.
     */
    MAX_TERRAFORM_STATE_SIZE_BYTES(Integer.class),
    /**
     * The max yaml size bytes provides.
     */
    MAX_YAML_SIZE_BYTES(Integer.class),
    /**
     * The max yaml depth provides.
     */
    MAX_YAML_DEPTH(Integer.class),
    /**
     * The personal access token prefix provides.
     */
    PERSONAL_ACCESS_TOKEN_PREFIX(String.class),
    /**
     * The kroki enabled provides.
     */
    KROKI_ENABLED(Boolean.class),
    /**
     * The kroki url provides.
     */
    KROKI_URL(String.class),
    /**
     * Additional formats supported by the Kroki instance.
     */
    KROKI_FORMATS(HashMap.class),

    /**
     * (If enabled, requires diagramsnet_url) Enable Diagrams.net integration. Default is true.
     */
    DIAGRAMSNET_ENABLED(Boolean.class),

    /**
     * The Diagrams.net instance URL for integration.
     */
    DIAGRAMSNET_URL(String.class),

    /**
     * When enabled, any user that signs up for an account using the registration form is placed under a Pending
     * approval state and has to be explicitly approved by an administrator.
     */
    REQUIRE_ADMIN_APPROVAL_AFTER_USER_SIGNUP(Boolean.class),

    /**
     * Allow administrators to require 2FA for all administrators on the instance.
     */
    REQUIRE_ADMIN_TWO_FACTOR_AUTHENTICATION(Boolean.class),

    /**
     * Enable Remember me setting. Introduced in GitLab 16.0.
     */
    REMEMBER_ME_ENABLED(Boolean.class),

    /**
     * Enable Silent mode. Default is false.
     */
    SILENT_MODE_ENABLED(Boolean.class),

    /**
     * The signing secret of the GitLab for Slack app. Used for authenticating API requests from the app.
     */
    SLACK_APP_SIGNING_SECRET(String.class),

    /**
     * API key used by GitLab for accessing the Spam Check service endpoint.
     */
    SPAM_CHECK_API_KEY(String.class),

    /**
     * (If enabled, requires: throttle_authenticated_packages_api_period_in_seconds and
     * throttle_authenticated_packages_api_requests_per_period) Enable authenticated API request rate limit. Helps
     * reduce request volume (for example, from crawlers or abusive bots). View Package Registry rate limits for more
     * details.
     */
    THROTTLE_AUTHENTICATED_PACKAGES_API_ENABLED(Boolean.class),

    /**
     * Rate limit period (in seconds). View Package Registry rate limits for more details.
     */
    THROTTLE_AUTHENTICATED_PACKAGES_API_PERIOD_IN_SECONDS(Integer.class),

    /**
     * Maximum requests per period per user. View Package Registry rate limits for more details.
     */
    THROTTLE_AUTHENTICATED_PACKAGES_API_REQUESTS_PER_PERIOD(Integer.class),

    /**
     * (If enabled, requires: throttle_unauthenticated_api_period_in_seconds and
     * throttle_unauthenticated_api_requests_per_period) Enable unauthenticated API request rate limit. Helps reduce
     * request volume (for example, from crawlers or abusive bots).
     */
    THROTTLE_UNAUTHENTICATED_API_ENABLED(Boolean.class),

    /**
     * Rate limit period in seconds.
     */
    THROTTLE_UNAUTHENTICATED_API_PERIOD_IN_SECONDS(Integer.class),

    /**
     * Max requests per period per IP.
     */
    THROTTLE_UNAUTHENTICATED_API_REQUESTS_PER_PERIOD(Integer.class),

    /**
     * (If enabled, requires: throttle_unauthenticated_packages_api_period_in_seconds and
     * throttle_unauthenticated_packages_api_requests_per_period) Enable authenticated API request rate limit. Helps
     * reduce request volume (for example, from crawlers or abusive bots). View Package Registry rate limits for more
     * details.
     */
    THROTTLE_UNAUTHENTICATED_PACKAGES_API_ENABLED(Boolean.class),

    /**
     * Rate limit period (in seconds). View Package Registry rate limits for more details.
     */
    THROTTLE_UNAUTHENTICATED_PACKAGES_API_PERIOD_IN_SECONDS(Integer.class),

    /**
     * Maximum requests per period per user. View Package Registry rate limits for more details.
     */
    THROTTLE_UNAUTHENTICATED_PACKAGES_API_REQUESTS_PER_PERIOD(Integer.class),

    /**
     * Fetch GitLab Runner release version data from GitLab.com. For more information, see how to determine which
     * runners need to be upgraded.
     */
    UPDATE_RUNNER_VERSIONS_ENABLED(Boolean.class),

    /**
     * Maximum files in a diff.
     */
    DIFF_MAX_FILES(Integer.class),

    /**
     * Maximum lines in a diff.
     */
    DIFF_MAX_LINES(Integer.class),

    /**
     * The Mailgun HTTP webhook signing key for receiving events from webhook.
     */
    MAILGUN_SIGNING_KEY(String.class),

    /**
     * Enable Mailgun event receiver.
     */
    MAILGUN_EVENTS_ENABLED(Boolean.class),

    /**
     * The Snowplow collector for database events hostname. (for example, db-snowplow.trx.gitlab.net)
     */
    SNOWPLOW_DATABASE_COLLECTOR_HOSTNAME(String.class),

    /**
     * Maximum wiki page content size in bytes. Default: 52428800 Bytes (50 MB). The minimum value is 1024 bytes.
     */
    WIKI_PAGE_MAX_CONTENT_BYTES(Integer.class),

    /**
     * The maximum time, in seconds, that the cleanup process can take to delete a batch of tags for cleanup policies.
     */
    CONTAINER_REGISTRY_DELETE_TAGS_SERVICE_TIMEOUT(Integer.class),

    /**
     * When rate limiting is enabled via the throttle_* settings, send this plain text response when a rate limit is
     * exceeded. 'Retry later' is sent if this is blank.
     */
    RATE_LIMITING_RESPONSE_TEXT(String.class),

    /**
     * Enable to allow anyone to pull from Package Registry visible and changeable.
     */
    PACKAGE_REGISTRY_ALLOW_ANYONE_TO_PULL_OPTION(Boolean.class),

    /**
     * Number of workers assigned to the packages cleanup policies.
     */
    PACKAGE_REGISTRY_CLEANUP_POLICIES_WORKER_CAPACITY(Integer.class),

    /**
     * Number of workers for cleanup policies.
     */
    CONTAINER_REGISTRY_EXPIRATION_POLICIES_WORKER_CAPACITY(Integer.class),

    /**
     * The maximum number of tags that can be deleted in a single execution of cleanup policies.
     */
    CONTAINER_REGISTRY_CLEANUP_TAGS_SERVICE_MAX_LIST_SIZE(Integer.class),

    /**
     * Prevent the deletion of the artifacts from the most recent successful jobs, regardless of the expiry time.
     * Enabled by default.
     */
    KEEP_LATEST_ARTIFACT(Boolean.class),

    /**
     * What’s new variant, possible values: all_tiers, current_tier, and disabled.
     */
    WHATS_NEW_VARIANT(String.class),

    /**
     * Send an email to users upon account deactivation.
     */
    USER_DEACTIVATION_EMAILS_ENABLED(Boolean.class),

    /**
     * track or compress. Sets the behavior for Sidekiq job size limits. Default: 'compress'.
     */
    SIDEKIQ_JOB_LIMITER_MODE(String.class),

    /**
     * The threshold in bytes at which Sidekiq jobs are compressed before being stored in Redis. Default: 100,000 bytes
     * (100 KB).
     */
    SIDEKIQ_JOB_LIMITER_COMPRESSION_THRESHOLD_BYTES(Integer.class),

    /**
     * The threshold in bytes at which Sidekiq jobs are rejected. Default: 0 bytes (doesn’t reject any job).
     */
    SIDEKIQ_JOB_LIMITER_LIMIT_BYTES(Integer.class),

    /**
     * Enable pipeline suggestion banner.
     */
    SUGGEST_PIPELINE_ENABLED(Boolean.class),

    /**
     * Show the external redirect page that warns you about user-generated content in GitLab Pages.
     */
    ENABLE_ARTIFACT_EXTERNAL_REDIRECT_WARNING_PAGE(Boolean.class),

    /**
     * Max number of requests per minute for performing a search while authenticated. Default: 30. To disable throttling
     * set to 0.
     */
    SEARCH_RATE_LIMIT(Integer.class),

    /**
     * Max number of requests per minute for performing a search while unauthenticated. Default: 10. To disable
     * throttling set to 0.
     */
    SEARCH_RATE_LIMIT_UNAUTHENTICATED(Integer.class),

    /**
     * Set the expiration time (in seconds) of authentication tokens of newly registered instance runners. Minimum value
     * is 7200 seconds. For more information, see Automatically rotate authentication tokens.
     */
    RUNNER_TOKEN_EXPIRATION_INTERVAL(Integer.class),

    /**
     * Set the expiration time (in seconds) of authentication tokens of newly registered group runners. Minimum value is
     * 7200 seconds. For more information, see Automatically rotate authentication tokens.
     */
    GROUP_RUNNER_TOKEN_EXPIRATION_INTERVAL(Integer.class),

    /**
     * Set the expiration time (in seconds) of authentication tokens of newly registered project runners. Minimum value
     * is 7200 seconds. For more information, see Automatically rotate authentication tokens.
     */
    PROJECT_RUNNER_TOKEN_EXPIRATION_INTERVAL(Integer.class),

    /**
     * Maximum number of pipeline creation requests per minute per user and commit. Disabled by default.
     */
    PIPELINE_LIMIT_PER_PROJECT_USER_SHA(Integer.class),

    /**
     * Indicates whether users can create top-level groups. Introduced in GitLab 15.5. Defaults to true.
     */
    CAN_CREATE_GROUP(Boolean.class),

    /**
     * Maximum simultaneous Direct Transfer batches to process.
     */
    BULK_IMPORT_CONCURRENT_PIPELINE_BATCH_LIMIT(Integer.class),

    /**
     * Enable migrating GitLab groups by direct transfer. Introduced in GitLab 15.8. Setting also available in the Admin
     * area.
     */
    BULK_IMPORT_ENABLED(Boolean.class),

    /**
     * Maximum download file size when importing from source GitLab instances by direct transfer. Introduced in GitLab
     * 16.3.
     */
    BULK_IMPORT_MAX_DOWNLOAD_FILE_SIZE(Integer.class),

    /**
     * Enable Silent admin exports. Default is false.
     */
    SILENT_ADMIN_EXPORTS_ENABLED(Boolean.class),

    /**
     * Newly created users have private profile by default. Introduced in GitLab 15.8. Defaults to false.
     */
    USER_DEFAULTS_TO_PRIVATE_PROFILE(Boolean.class),

    /**
     * Introduced in GitLab 15.10. Max number of requests per 10 minutes per IP address for unauthenticated requests to
     * the list all projects API. Default: 400. To disable throttling set to 0.
     */
    PROJECTS_API_RATE_LIMIT_UNAUTHENTICATED(Integer.class),

    /**
     * Indicates whether the instance was provisioned for GitLab Dedicated.
     */
    GITLAB_DEDICATED_INSTANCE(Boolean.class),

    /**
     * Indicates whether the instance was provisioned with the GitLab Environment Toolkit for Service Ping reporting.
     */
    GITLAB_ENVIRONMENT_TOOLKIT_INSTANCE(Boolean.class),

    /**
     * The maximum number of includes per pipeline. Default is 150.
     */
    CI_MAX_INCLUDES(Integer.class),

    /**
     * Enable users to delete their accounts.
     */
    ALLOW_ACCOUNT_DELETION(Boolean.class),

    /**
     * Maximum number of Git operations per minute a user can perform. Default: 600. Introduced in GitLab 16.2.
     */
    GITLAB_SHELL_OPERATION_LIMIT(Integer.class),

    /**
     * The maximum amount of memory, in bytes, that can be allocated for the pipeline configuration, with all included
     * YAML configuration files.
     */
    CI_MAX_TOTAL_YAML_SIZE_BYTES(Integer.class),

    /**
     * Public security contact information. Introduced in GitLab 16.7.
     */
    SECURITY_TXT_CONTENT(String.class),

    /**
     * Maximum downstream pipeline trigger rate. Default: 0 (no restriction). Introduced in GitLab 16.10.
     */
    DOWNSTREAM_PIPELINE_TRIGGER_LIMIT_PER_PROJECT_USER_SHA(Integer.class),

    /**
     * Maximum limit of AsciiDoc include directives being processed in any one document. Default: 32. Maximum: 64.
     */
    ASCIIDOC_MAX_INCLUDES(Integer.class),

    /**
     * When enabled, users must set an expiration date when creating a group or project access token, or a personal
     * access token owned by a non-service account.
     */
    REQUIRE_PERSONAL_ACCESS_TOKEN_EXPIRY(Boolean.class),

    /**
     * Enable automatic deactivation of dormant users.
     */
    DEACTIVATE_DORMANT_USERS(Boolean.class),

    /**
     * Length of time (in days) after which a user is considered dormant. Introduced in GitLab 15.3.
     */
    DEACTIVATE_DORMANT_USERS_PERIOD(Integer.class),

    /**
     * Indicates whether to skip metadata URL validation for the NuGet package. Introduced in GitLab 17.0.
     */
    NUGET_SKIP_METADATA_URL_VALIDATION(Boolean.class),

    /**
     * Enabling this permits automatic allocation of purchased storage in a namespace. Relevant only to EE
     * distributions.
     */
    AUTOMATIC_PURCHASED_STORAGE_ALLOCATION(Boolean.class),

    /**
     * Maximum size of text fields to index by Elasticsearch. 0 value means no limit. This does not apply to repository
     * and wiki indexing. Premium and Ultimate only.
     */
    ELASTICSEARCH_INDEXED_FIELD_LENGTH_LIMIT(Integer.class),

    /**
     * Maximum size of repository and wiki files that are indexed by Elasticsearch. Premium and Ultimate only.
     */
    ELASTICSEARCH_INDEXED_FILE_SIZE_LIMIT_KB(Integer.class),

    /**
     * Enable automatic requeuing of indexing workers. This improves non-code indexing throughput by enqueuing Sidekiq
     * jobs until all documents are processed. Premium and Ultimate only.
     */
    ELASTICSEARCH_REQUEUE_WORKERS(Boolean.class),

    /**
     * Number of indexing worker shards. This improves non-code indexing throughput by enqueuing more parallel Sidekiq
     * jobs. Default is 2. Premium and Ultimate only.
     */
    ELASTICSEARCH_WORKER_NUMBER_OF_SHARDS(Integer.class),

    /**
     * Maximum concurrency of Elasticsearch bulk requests per indexing operation. This only applies to repository
     * indexing operations. Premium and Ultimate only.
     */
    ELASTICSEARCH_MAX_BULK_CONCURRENCY(Integer.class),

    /**
     * Maximum size of Elasticsearch bulk indexing requests in MB. This only applies to repository indexing operations.
     * Premium and Ultimate only.
     */
    ELASTICSEARCH_MAX_BULK_SIZE_MB(Integer.class),

    /**
     * Maximum concurrency of Elasticsearch code indexing background jobs. This only applies to repository indexing
     * operations. Premium and Ultimate only.
     */
    ELASTICSEARCH_MAX_CODE_INDEXING_CONCURRENCY(Integer.class),

    /**
     * The username of your Elasticsearch instance. Premium and Ultimate only.
     */
    ELASTICSEARCH_USERNAME(String.class),

    /**
     * The password of your Elasticsearch instance. Premium and Ultimate only.
     */
    ELASTICSEARCH_PASSWORD(String.class),

    /**
     * Enabling this permits enforcement of namespace storage limits.
     */
    ENFORCE_NAMESPACE_STORAGE_LIMIT(Boolean.class),

    /**
     * Maximum allowable lifetime for access tokens in days. When left blank, default value of 365 is applied. When set,
     * value must be 365 or less. When changed, existing access tokens with an expiration date beyond the maximum
     * allowable lifetime are revoked. Self-managed, Ultimate only.
     */
    MAX_PERSONAL_ACCESS_TOKEN_LIFETIME(Integer.class),

    /**
     * Maximum allowable lifetime for SSH keys in days. Self-managed, Ultimate only.
     */
    MAX_SSH_KEY_LIFETIME(Integer.class),

    /**
     * List of package registry metadata to sync. See the list of the available values. Self-managed, Ultimate only.
     */
    PACKAGE_METADATA_PURL_TYPES(Integer[].class),

    /**
     * Whether to look up merge request approval policy approval groups globally or within project hierarchies.
     */
    SECURITY_POLICY_GLOBAL_GROUP_APPROVERS_ENABLED(Boolean.class),

    /**
     * Maximum number of active merge request approval policies per security policy project. Default: 5. Maximum: 20
     */
    SECURITY_APPROVAL_POLICIES_LIMIT(Integer.class),

    /**
     * Enables ClickHouse as a data source for analytics reports. ClickHouse must be configured for this setting to take
     * effect. Available on Premium and Ultimate only.
     */
    USE_CLICKHOUSE_FOR_ANALYTICS(Boolean.class),

    /**
     * Indicates whether GitLab Duo features are enabled for this instance. Introduced in GitLab 16.10. Self-managed,
     * Premium and Ultimate only.
     */
    DUO_FEATURES_ENABLED(Boolean.class),

    /**
     * Indicates whether the GitLab Duo features enabled setting is enforced for all subgroups. Introduced in GitLab
     * 16.10. Self-managed, Premium and Ultimate only.
     */
    LOCK_DUO_FEATURES_ENABLED(Boolean.class),

    /**
     * List of types which are allowed to register a GitLab Runner. Can be [], ['group'], ['project'] or ['group',
     * 'project'].
     */
    VALID_RUNNER_REGISTRARS(String[].class),

    /**
     * (If enabled, requires: throttle_unauthenticated_web_period_in_seconds and
     * throttle_unauthenticated_web_requests_per_period) Enable unauthenticated web request rate limit. Helps reduce
     * request volume (for example, from crawlers or abusive bots).
     */
    THROTTLE_UNAUTHENTICATED_WEB_ENABLED(Boolean.class),

    /**
     * Rate limit period in seconds.
     */
    THROTTLE_UNAUTHENTICATED_WEB_PERIOD_IN_SECONDS(Integer.class),

    /**
     * Max requests per period per IP.
     */
    THROTTLE_UNAUTHENTICATED_WEB_REQUESTS_PER_PERIOD(Integer.class),

    /**
     * Hash of names of taken from gitlab.yml to weights. New projects are created in one of these stores, chosen by a
     * weighted random selection.
     */
    REPOSITORY_STORAGES_WEIGHTED(HashMap.class),

    /**
     * Prevent editing approval rules in projects and merge requests.
     */
    DISABLE_OVERRIDING_APPROVERS_PER_MERGE_REQUEST(Boolean.class),

    /**
     * Prevent approval by author
     */
    PREVENT_MERGE_REQUESTS_AUTHOR_APPROVAL(Boolean.class),

    /**
     * Prevent approval by committers to merge requests
     */
    PREVENT_MERGE_REQUESTS_COMMITTERS_APPROVAL(Boolean.class),

    /**
     * Indicates whether passwords require at least one number. Introduced in GitLab 15.1. Premium and Ultimate only.
     */
    PASSWORD_NUMBER_REQUIRED(Boolean.class),

    /**
     * Indicates whether passwords require at least one symbol character. Introduced in GitLab 15.1. Premium and
     * Ultimate only.
     */
    PASSWORD_SYMBOL_REQUIRED(Boolean.class),

    /**
     * Indicates whether passwords require at least one uppercase letter. Introduced in GitLab 15.1. Premium and
     * Ultimate only.
     */
    PASSWORD_UPPERCASE_REQUIRED(Boolean.class),

    /**
     * Indicates whether passwords require at least one lowercase letter. Introduced in GitLab 15.1. Premium and
     * Ultimate only.
     */
    PASSWORD_LOWERCASE_REQUIRED(Boolean.class),

    /**
     * Enable default project deletion protection so only administrators can delete projects. Default is false.
     * Self-managed, Premium and Ultimate only.
     */
    DEFAULT_PROJECT_DELETION_PROTECTION(Boolean.class),

    /**
     * Number of days to wait before deleting a project or group that is marked for deletion. Value must be between 1
     * and 90. Defaults to 7. Self-managed, Premium and Ultimate only.
     */
    DELETION_ADJOURNED_PERIOD(Integer.class),

    /**
     * Disable personal access tokens. Introduced in GitLab 15.7. Self-managed, Premium and Ultimate only. There is no
     * method available to enable a personal access token that’s been disabled through the API. This is a known issue.
     * For more information about available workarounds, see Workaround.
     */
    DISABLE_PERSONAL_ACCESS_TOKENS(Boolean.class),

    /**
     * Disable user profile name changes.
     */
    UPDATING_NAME_DISABLED_FOR_USERS(Boolean.class),

    /**
     * Use repo.maven.apache.org as a default remote repository when the package is not found in the GitLab Package
     * Registry for Maven. Premium and Ultimate only.
     */
    MAVEN_PACKAGE_REQUESTS_FORWARDING(Boolean.class),

    /**
     * Use npmjs.org as a default remote repository when the package is not found in the GitLab Package Registry for
     * npm. Premium and Ultimate only.
     */
    NPM_PACKAGE_REQUESTS_FORWARDING(Boolean.class),

    /**
     * Use pypi.org as a default remote repository when the package is not found in the GitLab Package Registry for
     * PyPI. Premium and Ultimate only.
     */
    PYPI_PACKAGE_REQUESTS_FORWARDING(Boolean.class),

    /**
     * Prevent overrides of default branch protection. Self-managed, Premium and Ultimate only.
     */
    GROUP_OWNERS_CAN_MANAGE_DEFAULT_BRANCH_PROTECTION(Boolean.class),

    /**
     * When instance is in maintenance mode, non-administrative users can sign in with read-only access and make
     * read-only API requests. Premium and Ultimate only.
     */
    MAINTENANCE_MODE(Boolean.class),

    /**
     * Message displayed when instance is in maintenance mode. Premium and Ultimate only.
     */
    MAINTENANCE_MODE_MESSAGE(String.class),

    /**
     * Flag to indicate if token expiry date can be optional for service account users
     */
    SERVICE_ACCESS_TOKENS_EXPIRATION_ENFORCED(Boolean.class),

    /**
     * Specifies whether users who have not confirmed their email should be deleted. Default is false. When set to true,
     * unconfirmed users are deleted after unconfirmed_users_delete_after_days days. Introduced in GitLab 16.1.
     * Self-managed, Premium and Ultimate only.
     */
    DELETE_UNCONFIRMED_USERS(Boolean.class),

    /**
     * Specifies how many days after sign-up to delete users who have not confirmed their email. Only applicable if
     * delete_unconfirmed_users is set to true. Must be 1 or greater. Default is 7. Introduced in GitLab 16.1.
     * Self-managed, Premium and Ultimate only.
     */
    UNCONFIRMED_USERS_DELETE_AFTER_DAYS(Integer.class),

    /*
     * Undocumented settings as of GitLab 12.4 These are reported but not documented.
     */
    CUSTOM_HTTP_CLONE_URL_ROOT(String.class), PROTECTED_PATHS_RAW(String.class),
    THROTTLE_PROTECTED_PATHS_ENABLED(Boolean.class), THROTTLE_PROTECTED_PATHS_PERIOD_IN_SECONDS(Integer.class),
    THROTTLE_PROTECTED_PATHS_REQUESTS_PER_PERIOD(Integer.class),

    /*
     * Undocumented settings as of GitLab 12.8 These are reported but not documented.
     */
    FORCE_PAGES_ACCESS_CONTROL(Boolean.class), MINIMUM_PASSWORD_LENGTH(Integer.class),
    SNIPPET_SIZE_LIMIT(Integer.class),

    /*
     * Undocumented settings as of GitLab 12.9 These are reported but not documented.
     */
    EMAIL_RESTRICTIONS_ENABLED(Boolean.class), EMAIL_RESTRICTIONS(String.class),

    /*
     * Undocumented settings as of GitLab 13.0 These are reported but not documented.
     */
    CONTAINER_EXPIRATION_POLICIES_ENABLE_HISTORIC_ENTRIES(Boolean.class), ISSUES_CREATE_LIMIT(Integer.class),
    RAW_BLOB_REQUEST_LIMIT(Integer.class),

    /*
     * Undocumented settings as of GitLab 17.3 These are reported but not documented.
     */
    ALLOW_POSSIBLE_SPAM(Boolean.class), DENY_ALL_REQUESTS_EXCEPT_ALLOWED(Boolean.class),
    DOMAIN_DENYLIST_RAW(String.class), DOMAIN_ALLOWLIST_RAW(String.class),
    OUTBOUND_LOCAL_REQUESTS_ALLOWLIST_RAW(String.class), ERROR_TRACKING_ENABLED(Boolean.class),
    ERROR_TRACKING_API_URL(String.class), FLOC_ENABLED(Boolean.class), HELP_PAGE_DOCUMENTATION_BASE_URL(String.class),
    MATH_RENDERING_LIMITS_ENABLED(Boolean.class), MAX_ARTIFACTS_CONTENT_INCLUDE_SIZE(Integer.class),
    MAX_PAGES_CUSTOM_DOMAINS_PER_PROJECT(Integer.class), THROTTLE_AUTHENTICATED_GIT_LFS_ENABLED(Boolean.class),
    THROTTLE_AUTHENTICATED_GIT_LFS_PERIOD_IN_SECONDS(Integer.class),
    THROTTLE_AUTHENTICATED_GIT_LFS_REQUESTS_PER_PERIOD(Integer.class),
    THROTTLE_AUTHENTICATED_FILES_API_ENABLED(Boolean.class),
    THROTTLE_AUTHENTICATED_FILES_API_PERIOD_IN_SECONDS(Integer.class),
    THROTTLE_AUTHENTICATED_FILES_API_REQUESTS_PER_PERIOD(Integer.class),
    THROTTLE_AUTHENTICATED_DEPRECATED_API_ENABLED(Boolean.class),
    THROTTLE_AUTHENTICATED_DEPRECATED_API_PERIOD_IN_SECONDS(Integer.class),
    THROTTLE_AUTHENTICATED_DEPRECATED_API_REQUESTS_PER_PERIOD(Integer.class),
    THROTTLE_UNAUTHENTICATED_FILES_API_ENABLED(Boolean.class),
    THROTTLE_UNAUTHENTICATED_FILES_API_PERIOD_IN_SECONDS(Integer.class),
    THROTTLE_UNAUTHENTICATED_FILES_API_REQUESTS_PER_PERIOD(Integer.class),
    THROTTLE_UNAUTHENTICATED_GIT_HTTP_ENABLED(Boolean.class),
    THROTTLE_UNAUTHENTICATED_GIT_HTTP_PERIOD_IN_SECONDS(Integer.class),
    THROTTLE_UNAUTHENTICATED_GIT_HTTP_REQUESTS_PER_PERIOD(Integer.class),
    THROTTLE_UNAUTHENTICATED_DEPRECATED_API_ENABLED(Boolean.class),
    THROTTLE_UNAUTHENTICATED_DEPRECATED_API_PERIOD_IN_SECONDS(Integer.class),
    THROTTLE_UNAUTHENTICATED_DEPRECATED_API_REQUESTS_PER_PERIOD(Integer.class),
    PROTECTED_PATHS_FOR_GET_REQUEST_RAW(String.class), USAGE_PING_FEATURES_ENABLED(Boolean.class),
    NOTES_CREATE_LIMIT(Integer.class), NOTES_CREATE_LIMIT_ALLOWLIST_RAW(String.class),
    MEMBERS_DELETE_LIMIT(Integer.class), PROJECT_IMPORT_LIMIT(Integer.class), PROJECT_EXPORT_LIMIT(Integer.class),
    PROJECT_DOWNLOAD_EXPORT_LIMIT(Integer.class), GROUP_IMPORT_LIMIT(Integer.class), GROUP_EXPORT_LIMIT(Integer.class),
    GROUP_DOWNLOAD_EXPORT_LIMIT(Integer.class), WIKI_ASCIIDOC_ALLOW_URI_INCLUDES(Boolean.class),
    SENTRY_ENABLED(Boolean.class), SENTRY_DSN(String.class), SENTRY_CLIENTSIDE_DSN(String.class),
    SENTRY_ENVIRONMENT(String.class), SENTRY_CLIENTSIDE_TRACES_SAMPLE_RATE(Float.class),
    SEARCH_RATE_LIMIT_ALLOWLIST_RAW(String.class), USERS_GET_BY_ID_LIMIT(Integer.class),
    USERS_GET_BY_ID_LIMIT_ALLOWLIST_RAW(String.class), INVITATION_FLOW_ENFORCEMENT(Boolean.class),
    DEACTIVATION_EMAIL_ADDITIONAL_TEXT(String.class), GROUP_API_LIMIT(Integer.class),
    GROUP_INVITED_GROUPS_API_LIMIT(Integer.class), GROUP_SHARED_GROUPS_API_LIMIT(Integer.class),
    GROUP_PROJECTS_API_LIMIT(Integer.class), GROUPS_API_LIMIT(Integer.class), PROJECT_API_LIMIT(Integer.class),
    PROJECTS_API_LIMIT(Integer.class), USER_CONTRIBUTED_PROJECTS_API_LIMIT(Integer.class),
    USER_PROJECTS_API_LIMIT(Integer.class), USER_STARRED_PROJECTS_API_LIMIT(Integer.class),
    NAMESPACE_AGGREGATION_SCHEDULE_LEASE_DURATION_IN_SECONDS(Integer.class), AI_ACTION_API_RATE_LIMIT(Integer.class),
    CODE_SUGGESTIONS_API_RATE_LIMIT(Integer.class), ELASTICSEARCH_CLIENT_REQUEST_TIMEOUT(Integer.class),
    ELASTICSEARCH_PAUSE_INDEXING(Boolean.class), ELASTICSEARCH_REPLICAS(Integer.class),
    ELASTICSEARCH_SHARDS(Integer.class), ELASTICSEARCH_ANALYZERS_SMARTCN_ENABLED(Boolean.class),
    ELASTICSEARCH_ANALYZERS_SMARTCN_SEARCH(Boolean.class), ELASTICSEARCH_ANALYZERS_KUROMOJI_ENABLED(Boolean.class),
    ELASTICSEARCH_ANALYZERS_KUROMOJI_SEARCH(Boolean.class), INSTANCE_LEVEL_AI_BETA_FEATURES_ENABLED(Boolean.class),
    LOCK_MEMBERSHIPS_TO_LDAP(Boolean.class), LOCK_MEMBERSHIPS_TO_SAML(Boolean.class),
    SEARCH_MAX_SHARD_SIZE_GB(Integer.class), SEARCH_MAX_DOCS_DENOMINATOR(Integer.class),
    SEARCH_MIN_DOCS_BEFORE_ROLLOVER(Integer.class), SECRET_DETECTION_TOKEN_REVOCATION_ENABLED(Boolean.class),
    SECRET_DETECTION_TOKEN_REVOCATION_URL(String.class), SECRET_DETECTION_TOKEN_REVOCATION_TOKEN(String.class),
    SECRET_DETECTION_REVOCATION_TOKEN_TYPES_URL(String.class),
    SECURITY_POLICY_SCHEDULED_SCANS_MAX_CONCURRENCY(Integer.class),
    THROTTLE_INCIDENT_MANAGEMENT_NOTIFICATION_ENABLED(Boolean.class),
    THROTTLE_INCIDENT_MANAGEMENT_NOTIFICATION_PER_PERIOD(Integer.class),
    THROTTLE_INCIDENT_MANAGEMENT_NOTIFICATION_PERIOD_IN_SECONDS(Integer.class),
    PRODUCT_ANALYTICS_ENABLED(Boolean.class), PRODUCT_ANALYTICS_DATA_COLLECTOR_HOST(String.class),
    PRODUCT_ANALYTICS_CONFIGURATOR_CONNECTION_STRING(String.class), CUBE_API_BASE_URL(String.class),
    CUBE_API_KEY(String.class), DUO_AVAILABILITY(String.class), ZOEKT_AUTO_INDEX_ROOT_NAMESPACE(Boolean.class),
    ZOEKT_INDEXING_ENABLED(Boolean.class), ZOEKT_INDEXING_PAUSED(Boolean.class), ZOEKT_SEARCH_ENABLED(Boolean.class),
    DUO_WORKFLOW_OAUTH_APPLICATION_ID(String.class), ALLOW_DEPLOY_TOKENS_AND_KEYS_WITH_EXTERNAL_AUTHN(Boolean.class),
    CONTAINER_REGISTRY_IMPORT_MAX_TAGS_COUNT(Integer.class), CONTAINER_REGISTRY_IMPORT_MAX_RETRIES(Integer.class),
    CONTAINER_REGISTRY_IMPORT_START_MAX_RETRIES(Integer.class),
    CONTAINER_REGISTRY_IMPORT_MAX_STEP_DURATION(Integer.class), CONTAINER_REGISTRY_PRE_IMPORT_TAGS_RATE(Integer.class),
    CONTAINER_REGISTRY_PRE_IMPORT_TIMEOUT(Integer.class), CONTAINER_REGISTRY_IMPORT_TIMEOUT(Integer.class),
    CONTAINER_REGISTRY_IMPORT_TARGET_PLAN(String.class), CONTAINER_REGISTRY_IMPORT_CREATED_BEFORE(String.class),;

    private static JacksonJsonEnumHelper<Setting> enumHelper = new JacksonJsonEnumHelper<>(Setting.class);

    /**
     * The type field.
     */
    private Class<?> type;
    private Class<?>[] types;

    private Setting(Class<?> type) {
        this.type = type;
    }

    private Setting(Class<?>[] types) {
        this.types = types;
    }

    /**
     * Returns the value.
     *
     * @param value the value value
     * @return the result
     */

    @JsonCreator
    public static Setting forValue(String value) {
        return enumHelper.forValue(value);
    }

    /**
     * Returns the value.
     *
     * @return the result
     */

    @JsonValue
    public String toValue() {
        return (enumHelper.toString(this));
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (enumHelper.toString(this));
    }

    /**
     * Returns true if the provided value is of the correct type specified by this ApplicationSetting enum, otherwise
     * returns false.
     *
     * @param value the value to validate
     * @return true if the value is of the correct type or null
     */
    public final boolean isValid(Object value) {

        if (value == null) {
            return (true);
        }

        Class<?> valueType = value.getClass();
        if (type != null) {
            return (valueType == type);
        }

        for (Class<?> type : types) {
            if (valueType == type) {
                return (true);
            }
        }

        return (false);
    }

    /**
     * Validates the provided value against the data type of this ApplicationSetting enum. Will throw a
     * GitLabApiException if the value is not of the correct type.
     *
     * @param value the value to validate
     * @throws IllegalStateException if the provided value is not a valid type for the ApplicationSetting
     */
    public final void validate(Object value) {

        if (isValid(value)) {
            return;
        }

        StringBuilder shouldBe;
        if (type != null) {
            shouldBe = new StringBuilder(type.getSimpleName());
        } else {
            shouldBe = new StringBuilder(types[0].getSimpleName());
            for (int i = 1; i < types.length; i++) {
                shouldBe.append(" | ").append(types[i].getSimpleName());
            }
        }

        String errorMsg = String.format(
                "'%s' value is of incorrect type, is %s, should be %s",
                toValue(),
                value.getClass().getSimpleName(),
                shouldBe.toString());
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Executes the empty array value operation.
     *
     * @return the result
     */

    public Object emptyArrayValue() {
        if (type != null) {
            if (type.isArray()) {
                return Array.newInstance(type.getComponentType(), 0);
            }
        } else {
            for (Class<?> possibleType : types) {
                if (possibleType.isArray()) {
                    return Array.newInstance(possibleType.getComponentType(), 0);
                }
            }
        }
        return null;
    }

}
