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
package org.miaixz.bus.auth.nimble.microsoft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Microsoft authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum MicrosoftScope implements AuthorizeScope {

    /**
     * Allows the app to view the user's basic profile (name, picture, username). The meaning of {@code scope} is
     * subject to {@code description}.
     */
    PROFILE("profile", "Allows the app to view the user's basic personal profile (name, picture, username)", true),
    /**
     * Allows the app to read the user's primary email address.
     */
    EMAIL("email", "Allows the app to read the user's primary email address", true),
    /**
     * Allows users to sign in to the app with their work or school account, and allows the app to view the user's basic
     * profile information.
     */
    OPENID("openid",
            "Allows users to sign in to the app with their work or school account, and allows the app to view the user's basic profile information",
            true),
    /**
     * Allows the app to read and update user data, even when the user is not currently using the app.
     */
    OFFLINE_ACCESS("offline_access",
            "Allows the app to read and update user data, even when the user is not currently using this app", true),

    /**
     * Sign in and read user profile.
     */
    USER_READ("User.Read", "Sign in and read user profile", false),
    /**
     * Read and write permissions to user profile.
     */
    USER_READWRITE("User.ReadWrite", "Read and write permissions to user profile", false),
    /**
     * Read basic profiles of all users.
     */
    USER_READBASIC_ALL("User.ReadBasic.All", "Read basic profiles of all users", false),
    /**
     * Read full profiles of all users.
     */
    USER_READ_ALL("User.Read.All", "Read full profiles of all users", false),
    /**
     * Read and write full profiles of all users.
     */
    USER_READWRITE_ALL("User.ReadWrite.All", "Read and write full profiles of all users", false),
    /**
     * Invite guest users to the organization.
     */
    USER_INVITE_ALL("User.Invite.All", "Invite guest users to the organization", false),
    /**
     * Export user data.
     */
    USER_EXPORT_ALL("User.Export.All", "Export user data", false),
    /**
     * Manage all user identities.
     */
    USER_MANAGEIDENTITIES_ALL("User.ManageIdentities.All", "Manage all user identities", false),

    /**
     * Read and write app activities to the user's activity feed.
     */
    USERACTIVITY_READWRITE_CREATEDBYAPP("UserActivity.ReadWrite.CreatedByApp",
            "Read and write app activities to the user's activity feed", false),

    /**
     * Allows the app to read files of the signed-in user.
     */
    FILES_READ("Files.Read", "Allows the app to read files of the signed-in user", false),
    /**
     * Allows the app to read all files accessible to the signed-in user.
     */
    FILES_READ_ALL("Files.Read.All", "Allows the app to read all files accessible to the signed-in user", false),
    /**
     * Allows the app to read, create, update, and delete files of the signed-in user.
     */
    FILES_READWRITE("Files.ReadWrite", "Allows the app to read, create, update, and delete files of the signed-in user",
            false),
    /**
     * Allows the app to read, create, update, and delete all files accessible to the signed-in user.
     */
    FILES_READWRITE_ALL("Files.ReadWrite.All",
            "Allows the app to read, create, update, and delete all files accessible to the signed-in user", false),
    /**
     * Allows the app to read, create, update, and delete files in the application folder.
     */
    FILES_READWRITE_APPFOLDER("Files.ReadWrite.AppFolder",
            "Allows the app to read, create, update, and delete files in the application folder", false),
    /**
     * Allows the app to read files selected by the user. The app has access for several hours after the user selects
     * files.
     */
    FILES_READ_SELECTED("Files.Read.Selected",
            "Allows the app to read files selected by the user. The app has access for several hours after the user selects files",
            false),
    /**
     * Allows the app to read and write files selected by the user. The app has access for several hours after the user
     * selects files.
     */
    FILES_READWRITE_SELECTED("Files.ReadWrite.Selected",
            "Allows the app to read and write files selected by the user. The app has access for several hours after the user selects files",
            false),

    /**
     * Allows the app to read all organizational contacts on behalf of the signed-in user. These contacts are managed by
     * the organization and are different from the user's personal contacts.
     */
    ORGCONTACT_READ_ALL("OrgContact.Read.All",
            "Allows the app to read all organizational contacts on behalf of the signed-in user. These contacts are managed by the organization and are different from the user's personal contacts",
            false),

    /**
     * Allows the app to read emails in the user's mailbox.
     */
    MAIL_READ("Mail.Read", "Allows the app to read emails in the user's mailbox", false),
    /**
     * Allows the app to read the signed-in user's mailbox, but not the body, bodyPreview, uniqueBody, attachments,
     * extensions, and any extended properties. Does not include mail search permissions.
     */
    MAIL_READBASIC("Mail.ReadBasic",
            "Allows the app to read the signed-in user's mailbox, but not the body, bodyPreview, uniqueBody, attachments, extensions, and any extended properties. Does not include mail search permissions",
            false),
    /**
     * Allows the app to create, read, update, and delete emails in the user's mailbox. Does not include permission to
     * send emails.
     */
    MAIL_READWRITE("Mail.ReadWrite",
            "Allows the app to create, read, update, and delete emails in the user's mailbox. Does not include permission to send emails",
            false),
    /**
     * Allows the app to read mail accessible to the user, including the user's personal mail and shared mail.
     */
    MAIL_READ_SHARED("Mail.Read.Shared",
            "Allows the app to read mail accessible to the user, including the user's personal mail and shared mail",
            false),
    /**
     * Allows the app to create, read, update, and delete mail accessible to the user, including the user's personal
     * mail and shared mail. Does not include permission to send mail.
     */
    MAIL_READWRITE_SHARED("Mail.ReadWrite.Shared",
            "Allows the app to create, read, update, and delete mail accessible to the user, including the user's personal mail and shared mail. Does not include mail sending permissions",
            false),
    /**
     * Allows the app to send mail as an organizational user.
     */
    MAIL_SEND("Mail.Send", "Allows the app to send mail as an organizational user", false),
    /**
     * Allows the app to send mail as the signed-in user, including sending mail on behalf of others.
     */
    MAIL_SEND_SHARED("Mail.Send.Shared",
            "Allows the app to send mail as the signed-in user, including sending mail on behalf of others", false),
    /**
     * Allows the app to read the user's mailbox settings. Does not include mail sending permissions.
     */
    MAILBOXSETTINGS_READ("MailboxSettings.Read",
            "Allows the app to read the user's mailbox settings. Does not include mail sending permissions", false),
    /**
     * Allows the app to create, read, update, and delete user mailbox settings. Does not include direct mail sending
     * permissions, but allows the app to create rules that can forward or redirect mail.
     */
    MAILBOXSETTINGS_READWRITE("MailboxSettings.ReadWrite",
            "Allows the app to create, read, update, and delete user mailbox settings. Does not include direct mail sending permissions, but allows the app to create rules that can forward or redirect mail",
            false),

    /**
     * Allows the app to read OneNote notebooks and section titles on behalf of the signed-in user and create new pages,
     * notebooks, and sections.
     */
    NOTES_READ("Notes.Read",
            "Allows the app to read OneNote notebooks and section titles on behalf of the signed-in user and create new pages, notebooks, and sections",
            false),
    /**
     * Allows the app to create user OneNote notebooks.
     */
    NOTES_CREATE("Notes.Create", "Allows the app to create user OneNote notebooks", false),
    /**
     * Allows the app to read, share, and modify OneNote notebooks on behalf of the signed-in user.
     */
    NOTES_READWRITE("Notes.ReadWrite",
            "Allows the app to read, share, and modify OneNote notebooks on behalf of the signed-in user", false),
    /**
     * Allows the app to read OneNote notebooks accessible to the signed-in user in the organization.
     */
    NOTES_READ_ALL("Notes.Read.All",
            "Allows the app to read OneNote notebooks accessible to the signed-in user in the organization", false),
    /**
     * Allows the app to read, share, and modify OneNote notebooks accessible to the signed-in user in the organization.
     */
    NOTES_READWRITE_ALL("Notes.ReadWrite.All",
            "Allows the app to read, share, and modify OneNote notebooks accessible to the signed-in user in the organization",
            false);

    /**
     * The scope string as defined by Microsoft.
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
