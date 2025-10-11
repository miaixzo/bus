/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.auth.nimble.slack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Slack authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum SlackScope implements AuthorizeScope {

    /**
     * View profile details about people in a workspace. The meaning of {@code scope} is subject to {@code description}.
     */
    USERS_PROFILE_READ("users.profile:read", "View profile details about people in a workspace", true),
    /**
     * View people in a workspace.
     */
    USERS_READ("users:read", "View people in a workspace", true),
    /**
     * View email addresses of people in a workspace.
     */
    USERS_READ_EMAIL("users:read.email", "View email addresses of people in a workspace", true),
    /**
     * Edit a user’s profile information and status.
     */
    USERS_PROFILE_WRITE("users.profile:write", "Edit a user’s profile information and status", false),
    /**
     * Change the user's profile fields.
     */
    USERS_PROFILE_WRITE_USER("users.profile:write:user", "Change the user's profile fields", false),
    /**
     * Set presence for your slack app.
     */
    USERS_WRITE("users:write", "Set presence for your slack app", false),
    /**
     * Administer a workspace.
     */
    ADMIN("admin", "Administer a workspace", false),
    /**
     * Access analytics data about the organization.
     */
    ADMIN_ANALYTICS_READ("admin.analytics:read", "Access analytics data about the organization", false),
    /**
     * View apps and app requests in a workspace.
     */
    ADMIN_APPS_READ("admin.apps:read", "View apps and app requests in a workspace", false),
    /**
     * Manage apps in a workspace.
     */
    ADMIN_APPS_WRITE("admin.apps:write", "Manage apps in a workspace", false),
    /**
     * Read information barriers in the organization.
     */
    ADMIN_BARRIERS_READ("admin.barriers:read", "Read information barriers in the organization", false),
    /**
     * Manage information barriers in the organization.
     */
    ADMIN_BARRIERS_WRITE("admin.barriers:write", "Manage information barriers in the organization", false),
    /**
     * View the channel’s member list, topic, purpose and channel name.
     */
    ADMIN_CONVERSATIONS_READ("admin.conversations:read",
            "View the channel’s member list, topic, purpose and channel name", false),
    /**
     * Start a new conversation, modify a conversation and modify channel details.
     */
    ADMIN_CONVERSATIONS_WRITE("admin.conversations:write",
            "Start a new conversation, modify a conversation and modify channel details", false),
    /**
     * Gain information about invite requests in a Grid organization.
     */
    ADMIN_INVITES_READ("admin.invites:read", "Gain information about invite requests in a Grid organization.", false),
    /**
     * Approve or deny invite requests in a Grid organization.
     */
    ADMIN_INVITES_WRITE("admin.invites:write", "Approve or deny invite requests in a Grid organization.", false),
    /**
     * Access information about a workspace.
     */
    ADMIN_TEAMS_READ("admin.teams:read", "Access information about a workspace", false),
    /**
     * Make changes to a workspace.
     */
    ADMIN_TEAMS_WRITE("admin.teams:write", "Make changes to a workspace", false),
    /**
     * Access information about user groups.
     */
    ADMIN_USERGROUPS_READ("admin.usergroups:read", "Access information about user groups", false),
    /**
     * Make changes to your usergroups.
     */
    ADMIN_USERGROUPS_WRITE("admin.usergroups:write", "Make changes to your usergroups", false),
    /**
     * Access a workspace’s profile information.
     */
    ADMIN_USERS_READ("admin.users:read", "Access a workspace’s profile information", false),
    /**
     * Modify account information.
     */
    ADMIN_USERS_WRITE("admin.users:write", "Modify account information", false),
    /**
     * View messages that directly mention @your_slack_app in conversations that the app is in.
     */
    APP_MENTIONS_READ("app_mentions:read",
            "View messages that directly mention @your_slack_app in conversations that the app is in", false),
    /**
     * View events from all workspaces, channels and users (Enterprise Grid only).
     */
    AUDITLOGS_READ("auditlogs:read", "View events from all workspaces, channels and users (Enterprise Grid only)",
            false),
    /**
     * Add the ability for people to direct message or mention @your_slack_app.
     */
    BOT("bot", "Add the ability for people to direct message or mention @your_slack_app", false),
    /**
     * View information about ongoing and past calls.
     */
    CALLS_READ("calls:read", "View information about ongoing and past calls", false),
    /**
     * Start and manage calls in a workspace.
     */
    CALLS_WRITE("calls:write", "Start and manage calls in a workspace", false),
    /**
     * View messages and other content in public channels that your slack app has been added to.
     */
    CHANNELS_HISTORY("channels:history",
            "View messages and other content in public channels that your slack app has been added to", false),
    /**
     * Join public channels in a workspace.
     */
    CHANNELS_JOIN("channels:join", "Join public channels in a workspace", false),
    /**
     * Manage public channels that your slack app has been added to and create new ones.
     */
    CHANNELS_MANAGE("channels:manage",
            "Manage public channels that your slack app has been added to and create new ones", false),
    /**
     * View basic information about public channels in a workspace.
     */
    CHANNELS_READ("channels:read", "View basic information about public channels in a workspace", false),
    /**
     * Manage a user’s public channels and create new ones on a user’s behalf.
     */
    CHANNELS_WRITE("channels:write", "Manage a user’s public channels and create new ones on a user’s behalf", false),
    /**
     * Post messages in approved channels / conversations.
     */
    CHAT_WRITE("chat:write", "Post messages in approved channels / conversations", false),
    /**
     * Send messages as @your_slack_app with a customized username and avatar.
     */
    CHAT_WRITE_CUSTOMIZE("chat:write.customize",
            "Send messages as @your_slack_app with a customized username and avatar", false),
    /**
     * Send messages to channels @your_slack_app isn't a member of.
     */
    CHAT_WRITE_PUBLIC("chat:write.public", "Send messages to channels @your_slack_app isn't a member of", false),
    /**
     * Send messages as your slack app.
     */
    CHAT_WRITE_BOT("chat:write:bot", "Send messages as your slack app", false),
    /**
     * Send messages on a user’s behalf.
     */
    CHAT_WRITE_USER("chat:write:user", "Send messages on a user’s behalf", false),
    /**
     * Receive all events from a workspace in real time.
     */
    CLIENT("client", "Receive all events from a workspace in real time", false),
    /**
     * Add shortcuts and/or slash commands that people can use.
     */
    COMMANDS("commands", "Add shortcuts and/or slash commands that people can use", false),
    /**
     * Deprecated: Retrieve conversation history for legacy workspace apps.
     */
    CONVERSATIONS_HISTORY("conversations:history",
            "Deprecated: Retrieve conversation history for legacy workspace apps", false),
    /**
     * Deprecated: Retrieve information on conversations for legacy workspace apps.
     */
    CONVERSATIONS_READ("conversations:read",
            "Deprecated: Retrieve information on conversations for legacy workspace apps", false),
    /**
     * Deprecated: Edit conversation attributes for legacy workspace apps.
     */
    CONVERSATIONS_WRITE("conversations:write", "Deprecated: Edit conversation attributes for legacy workspace apps",
            false),
    /**
     * View Do Not Disturb settings for people in a workspace.
     */
    DND_READ("dnd:read", "View Do Not Disturb settings for people in a workspace", false),
    /**
     * Edit a user’s Do Not Disturb settings.
     */
    DND_WRITE("dnd:write", "Edit a user’s Do Not Disturb settings", false),
    /**
     * Change the user's Do Not Disturb settings.
     */
    DND_WRITE_USER("dnd:write:user", "Change the user's Do Not Disturb settings", false),
    /**
     * View custom emoji in a workspace.
     */
    EMOJI_READ("emoji:read", "View custom emoji in a workspace", false),
    /**
     * View files shared in channels and conversations that your slack app has been added to.
     */
    FILES_READ("files:read", "View files shared in channels and conversations that your slack app has been added to",
            false),
    /**
     * Upload, edit, and delete files as your slack app.
     */
    FILES_WRITE("files:write", "Upload, edit, and delete files as your slack app", false),
    /**
     * Upload, edit, and delete files as your slack app.
     */
    FILES_WRITE_USER("files:write:user", "Upload, edit, and delete files as your slack app", false),
    /**
     * View messages and other content in private channels that your slack app has been added to.
     */
    GROUPS_HISTORY("groups:history",
            "View messages and other content in private channels that your slack app has been added to", false),
    /**
     * View basic information about private channels that your slack app has been added to.
     */
    GROUPS_READ("groups:read", "View basic information about private channels that your slack app has been added to",
            false),
    /**
     * Manage private channels that your slack app has been added to and create new ones.
     */
    GROUPS_WRITE("groups:write", "Manage private channels that your slack app has been added to and create new ones",
            false),
    /**
     * View information about a user’s identity.
     */
    IDENTIFY("identify", "View information about a user’s identity", false),
    /**
     * View a user’s Slack avatar.
     */
    IDENTITY_AVATAR("identity.avatar", "View a user’s Slack avatar", false),
    /**
     * View the user's profile picture.
     */
    IDENTITY_AVATAR_READ_USER("identity.avatar:read:user", "View the user's profile picture", false),
    /**
     * View information about a user’s identity.
     */
    IDENTITY_BASIC("identity.basic", "View information about a user’s identity", false),
    /**
     * View a user’s email address.
     */
    IDENTITY_EMAIL("identity.email", "View a user’s email address", false),
    /**
     * This scope is not yet described.
     */
    IDENTITY_EMAIL_READ_USER("identity.email:read:user", "This scope is not yet described.", false),
    /**
     * View a user’s Slack workspace name.
     */
    IDENTITY_TEAM("identity.team", "View a user’s Slack workspace name", false),
    /**
     * View the workspace's name, domain, and icon.
     */
    IDENTITY_TEAM_READ_USER("identity.team:read:user", "View the workspace's name, domain, and icon", false),
    /**
     * This scope is not yet described.
     */
    IDENTITY_READ_USER("identity:read:user", "This scope is not yet described.", false),
    /**
     * View messages and other content in direct messages that your slack app has been added to.
     */
    IM_HISTORY("im:history", "View messages and other content in direct messages that your slack app has been added to",
            false),
    /**
     * View basic information about direct messages that your slack app has been added to.
     */
    IM_READ("im:read", "View basic information about direct messages that your slack app has been added to", false),
    /**
     * Start direct messages with people.
     */
    IM_WRITE("im:write", "Start direct messages with people", false),
    /**
     * Create one-way webhooks to post messages to a specific channel.
     */
    INCOMING_WEBHOOK("incoming-webhook", "Create one-way webhooks to post messages to a specific channel", false),
    /**
     * View URLs in messages.
     */
    LINKS_READ("links:read", "View URLs in messages", false),
    /**
     * Show previews of URLs in messages.
     */
    LINKS_WRITE("links:write", "Show previews of URLs in messages", false),
    /**
     * View messages and other content in group direct messages that your slack app has been added to.
     */
    MPIM_HISTORY("mpim:history",
            "View messages and other content in group direct messages that your slack app has been added to", false),
    /**
     * View basic information about group direct messages that your slack app has been added to.
     */
    MPIM_READ("mpim:read", "View basic information about group direct messages that your slack app has been added to",
            false),
    /**
     * Start group direct messages with people.
     */
    MPIM_WRITE("mpim:write", "Start group direct messages with people", false),
    /**
     * Execute methods without needing a scope.
     */
    NONE("none", "Execute methods without needing a scope", false),
    /**
     * View pinned content in channels and conversations that your slack app has been added to.
     */
    PINS_READ("pins:read", "View pinned content in channels and conversations that your slack app has been added to",
            false),
    /**
     * Add and remove pinned messages and files.
     */
    PINS_WRITE("pins:write", "Add and remove pinned messages and files", false),
    /**
     * Post messages to a workspace.
     */
    POST("post", "Post messages to a workspace", false),
    /**
     * View emoji reactions and their associated content in channels and conversations that your slack app has been
     * added to.
     */
    REACTIONS_READ("reactions:read",
            "View emoji reactions and their associated content in channels and conversations that your slack app has been added to",
            false),
    /**
     * Add and edit emoji reactions.
     */
    REACTIONS_WRITE("reactions:write", "Add and edit emoji reactions", false),
    /**
     * View all content in a workspace.
     */
    READ("read", "View all content in a workspace", false),
    /**
     * View reminders created by your slack app.
     */
    REMINDERS_READ("reminders:read", "View reminders created by your slack app", false),
    /**
     * Access reminders created by a user or for a user.
     */
    REMINDERS_READ_USER("reminders:read:user", "Access reminders created by a user or for a user", false),
    /**
     * Add, remove, or mark reminders as complete.
     */
    REMINDERS_WRITE("reminders:write", "Add, remove, or mark reminders as complete", false),
    /**
     * Add, remove, or complete reminders for the user.
     */
    REMINDERS_WRITE_USER("reminders:write:user", "Add, remove, or complete reminders for the user", false),
    /**
     * View remote files added by the app in a workspace.
     */
    REMOTE_FILES_READ("remote_files:read", "View remote files added by the app in a workspace", false),
    /**
     * Share remote files on a user’s behalf.
     */
    REMOTE_FILES_SHARE("remote_files:share", "Share remote files on a user’s behalf", false),
    /**
     * Add, edit, and delete remote files on a user’s behalf.
     */
    REMOTE_FILES_WRITE("remote_files:write", "Add, edit, and delete remote files on a user’s behalf", false),
    /**
     * Search a workspace’s content.
     */
    SEARCH_READ("search:read", "Search a workspace’s content", false),
    /**
     * View messages and files that your slack app has starred.
     */
    STARS_READ("stars:read", "View messages and files that your slack app has starred", false),
    /**
     * Add or remove stars.
     */
    STARS_WRITE("stars:write", "Add or remove stars", false),
    /**
     * View the name, email domain, and icon for workspaces your slack app is connected to.
     */
    TEAM_READ("team:read", "View the name, email domain, and icon for workspaces your slack app is connected to",
            false),
    /**
     * Execute methods without needing a scope.
     */
    TOKENS_BASIC("tokens.basic", "Execute methods without needing a scope", false),
    /**
     * View user groups in a workspace.
     */
    USERGROUPS_READ("usergroups:read", "View user groups in a workspace", false),
    /**
     * Create and manage user groups.
     */
    USERGROUPS_WRITE("usergroups:write", "Create and manage user groups", false),
    /**
     * Add steps that people can use in Workflow Authorize.
     */
    WORKFLOW_STEPS_EXECUTE("workflow.steps:execute", "Add steps that people can use in Workflow Authorize", false);

    /**
     * The scope string as defined by Slack.
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
