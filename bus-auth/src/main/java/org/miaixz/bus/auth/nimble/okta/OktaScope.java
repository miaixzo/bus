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
package org.miaixz.bus.auth.nimble.okta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Okta authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@AllArgsConstructor
public enum OktaScope implements AuthorizeScope {

    /**
     * Signals that a provider is an OpenID provider. The meaning of {@code scope} is subject to {@code description}.
     */
    OPENID("openid", "Signals that a provider is an OpenID provider.", true),
    /**
     * The exact data varies based on what profile information you have provided, such as: name, time zone, picture, or
     * birthday.
     */
    PROFILE("profile",
            "The exact data varies based on what profile information you have provided, such as: name, time zone, picture, or birthday.",
            true),
    /**
     * This allows the app to view your email address.
     */
    EMAIL("email", "This allows the app to view your email address.", true),
    /**
     * This allows the app to view your address, such as: street address, city, state, and zip code.
     */
    ADDRESS("address", "This allows the app to view your address, such as: street address, city, state, and zip code.",
            true),
    /**
     * This allows the app to view your phone number.
     */
    PHONE("phone", "This allows the app to view your phone number.", true),
    /**
     * This keeps you signed in to the app, even when you are not using it.
     */
    OFFLINE_ACCESS("offline_access", "This keeps you signed in to the app, even when you are not using it.", true),
    /**
     * Allows the app to create and manage users and read all profile and credential information for users.
     */
    OKTA_USERS_MANAGE("okta.users.manage",
            "Allows the app to create and manage users and read all profile and credential information for users",
            false),
    /**
     * Allows the app to read any user's profile and credential information.
     */
    OKTA_USERS_READ("okta.users.read", "Allows the app to read any user's profile and credential information", false),
    /**
     * Allows the app to manage the currently signed-in user's profile. Currently only supports user profile attribute
     * updates.
     */
    OKTA_USERS_MANAGE_SELF("okta.users.manage.self",
            "Allows the app to manage the currently signed-in user's profile. Currently only supports user profile attribute updates.",
            false),
    /**
     * Allows the app to read the currently signed-in user's profile and credential information.
     */
    OKTA_USERS_READ_SELF("okta.users.read.self",
            "Allows the app to read the currently signed-in user's profile and credential information", false),
    /**
     * Allows the app to create and manage Apps in your Okta organization.
     */
    OKTA_APPS_MANAGE("okta.apps.manage", "Allows the app to create and manage Apps in your Okta organization", false),
    /**
     * Allows the app to read information about Apps in your Okta organization.
     */
    OKTA_APPS_READ("okta.apps.read", "Allows the app to read information about Apps in your Okta organization", false),
    /**
     * Allows the app to manage authorization servers.
     */
    OKTA_AUTHORIZATIONSERVERS_MANAGE("okta.authorizationServers.manage",
            "Allows the app to manage authorization servers", false),
    /**
     * Allows the app to read authorization server information.
     */
    OKTA_AUTHORIZATIONSERVERS_READ("okta.authorizationServers.read",
            "Allows the app to read authorization server information", false),
    /**
     * Allows the app to manage all OAuth/OIDC clients and to create new clients.
     */
    OKTA_CLIENTS_MANAGE("okta.clients.manage",
            "Allows the app to manage all OAuth/OIDC clients and to create new clients", false),
    /**
     * Allows the app to read information for all OAuth/OIDC clients.
     */
    OKTA_CLIENTS_READ("okta.clients.read", "Allows the app to read information for all OAuth/OIDC clients", false),
    /**
     * Allows the app to register (create) new OAuth/OIDC clients (but not read information about existing clients).
     */
    OKTA_CLIENTS_REGISTER("okta.clients.register",
            "Allows the app to register (create) new OAuth/OIDC clients (but not read information about existing clients)",
            false),
    /**
     * Allows the app to create and manage Event Hooks in your Okta organization.
     */
    OKTA_EVENTHOOKS_MANAGE("okta.eventHooks.manage",
            "Allows the app to create and manage Event Hooks in your Okta organization", false),
    /**
     * Allows the app to read information about Event Hooks in your Okta organization.
     */
    OKTA_EVENTHOOKS_READ("okta.eventHooks.read",
            "Allows the app to read information about Event Hooks in your Okta organization", false),
    /**
     * Allows the app to manage all admin operations for org factors (for example, activate, deactive, read).
     */
    OKTA_FACTORS_MANAGE("okta.factors.manage",
            "Allows the app to manage all admin operations for org factors (for example, activate, deactive, read)",
            false),
    /**
     * Allows the app to read org factors information.
     */
    OKTA_FACTORS_READ("okta.factors.read", "Allows the app to read org factors information", false),
    /**
     * Allows the app to manage groups in your Okta organization.
     */
    OKTA_GROUPS_MANAGE("okta.groups.manage", "Allows the app to manage groups in your Okta organization", false),
    /**
     * Allows the app to read information about groups and their members in your Okta organization.
     */
    OKTA_GROUPS_READ("okta.groups.read",
            "Allows the app to read information about groups and their members in your Okta organization", false),
    /**
     * Allows the app to create and manage Identity Providers in your Okta organization.
     */
    OKTA_IDPS_MANAGE("okta.idps.manage",
            "Allows the app to create and manage Identity Providers in your Okta organization", false),
    /**
     * Allows the app to read information about Identity Providers in your Okta organization.
     */
    OKTA_IDPS_READ("okta.idps.read",
            "Allows the app to read information about Identity Providers in your Okta organization", false),
    /**
     * Allows the app to create and manage Inline Hooks in your Okta organization.
     */
    OKTA_INLINEHOOKS_MANAGE("okta.inlineHooks.manage",
            "Allows the app to create and manage Inline Hooks in your Okta organization.", false),
    /**
     * Allows the app to read information about Inline Hooks in your Okta organization.
     */
    OKTA_INLINEHOOKS_READ("okta.inlineHooks.read",
            "Allows the app to read information about Inline Hooks in your Okta organization.", false),
    /**
     * Allows the app to manage Linked Object definitions in your Okta organization.
     */
    OKTA_LINKEDOBJECTS_MANAGE("okta.linkedObjects.manage",
            "Allows the app to manage Linked Object definitions in your Okta organization.", false),
    /**
     * Allows the app to read Linked Object definitions in your Okta organization.
     */
    OKTA_LINKEDOBJECTS_READ("okta.linkedObjects.read",
            "Allows the app to read Linked Object definitions in your Okta organization.", false),
    /**
     * Allows the app to read information about System Log entries in your Okta organization.
     */
    OKTA_LOGS_READ("okta.logs.read",
            "Allows the app to read information about System Log entries in your Okta organization", false),
    /**
     * Allows the app to create and manage Administrator Roles in your Okta organization.
     */
    OKTA_ROLES_MANAGE("okta.roles.manage",
            "Allows the app to create and manage Administrator Roles in your Okta organization", false),
    /**
     * Allows the app to read information about Administrator Roles in your Okta organization.
     */
    OKTA_ROLES_READ("okta.roles.read",
            "Allows the app to read information about Administrator Roles in your Okta organization", false),
    /**
     * Allows the app to create and manage Schemas in your Okta organization.
     */
    OKTA_SCHEMAS_MANAGE("okta.schemas.manage", "Allows the app to create and manage Schemas in your Okta organization",
            false),
    /**
     * Allows the app to read information about Schemas in your Okta organization.
     */
    OKTA_SCHEMAS_READ("okta.schemas.read", "Allows the app to read information about Schemas in your Okta organization",
            false),
    /**
     * Allows the app to manage all sessions in your Okta organization.
     */
    OKTA_SESSIONS_MANAGE("okta.sessions.manage", "Allows the app to manage all sessions in your Okta organization",
            false),
    /**
     * Allows the app to read all sessions in your Okta organization.
     */
    OKTA_SESSIONS_READ("okta.sessions.read", "Allows the app to read all sessions in your Okta organization", false),
    /**
     * Allows the app to manage all custom templates in your Okta organization.
     */
    OKTA_TEMPLATES_MANAGE("okta.templates.manage",
            "Allows the app to manage all custom templates in your Okta organization", false),
    /**
     * Allows the app to read all custom templates in your Okta organization.
     */
    OKTA_TEMPLATES_READ("okta.templates.read", "Allows the app to read all custom templates in your Okta organization",
            false),
    /**
     * Allows the app to manage all Trusted Origins in your Okta organization.
     */
    OKTA_TRUSTEDORIGINS_MANAGE("okta.trustedOrigins.manage",
            "Allows the app to manage all Trusted Origins in your Okta organization", false),
    /**
     * Allows the app to read all Trusted Origins in your Okta organization.
     */
    OKTA_TRUSTEDORIGINS_READ("okta.trustedOrigins.read",
            "Allows the app to read all Trusted Origins in your Okta organization", false),
    /**
     * Allows the app to manage Policies in your Okta organization.
     */
    OKTA_POLICIES_MANAGE("okta.policies.manage", "Allows the app to manage Policies in your Okta organization", false),
    /**
     * Allows the app to read information about Policies in your Okta organization.
     */
    OKTA_POLICIES_READ("okta.policies.read",
            "Allows the app to read information about Policies in your Okta organization", false);

    /**
     * The scope string as defined by Okta.
     */
    private final String scope;
    /**
     * A description of what the scope grants access to.
     */
    private final String description;
    /**
     * Indicates if this scope is enabled by default.
     */
    private final boolean isDefault;

}
