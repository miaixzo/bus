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
package org.miaixz.bus.auth.nimble.douyin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Douyin (TikTok) authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum DouyinScope implements AuthorizeScope {

    /**
     * Returns public information of Douyin users. No application required, enabled by default. The meaning of
     * {@code scope} is subject to {@code description}.
     */
    USER_INFO("user_info", "Returns public information of Douyin users", true),
    /**
     * Douyin sharing functionality. No application required, enabled by default.
     */
    AWEME_SHARE("aweme.share", "Douyin sharing", false),
    /**
     * Share with Douyin friends. Requires application in the management center.
     */
    IM_SHARE("im.share", "Share with Douyin friends", false),
    /**
     * Dynamically renew the authorization validity period.
     */
    RENEW_REFRESH_TOKEN("renew_refresh_token", "Dynamically renew the authorization validity period", false),
    /**
     * Get the user's following list.
     */
    FOLLOWING_LIST("following.list", "Get the user's following list", false),
    /**
     * Get the user's fan list.
     */
    FANS_LIST("fans.list", "Get the user's fan list", false),
    /**
     * Video publishing and management.
     */
    VIDEO_CREATE("video.create", "Video publishing and management", false),
    /**
     * Delete content.
     */
    VIDEO_DELETE("video.delete", "Delete content", false),
    /**
     * Query authorized user's Douyin video data.
     */
    VIDEO_DATA("video.data", "Query authorized user's Douyin video data", false),
    /**
     * Query video data for specific Douyin videos.
     */
    VIDEO_LIST("video.list", "Query video data for specific Douyin videos", false),
    /**
     * Special permission. Disabled by default, requires application in the management center. Share with source tag,
     * users can click the tag to enter the conversion page.
     */
    SHARE_WITH_SOURCE("share_with_source",
            "Share with source tag, users can click the tag to enter the conversion page", false),
    /**
     * Log in to a third-party platform with a Douyin account to get the user's mobile number on Douyin.
     */
    MOBILE("mobile", "Log in to a third-party platform with a Douyin account to get the user's mobile number on Douyin",
            false),
    /**
     * Log in to a third-party platform with a Douyin account to get the user's mobile number on Douyin.
     */
    MOBILE_ALERT("mobile_alert",
            "Log in to a third-party platform with a Douyin account to get the user's mobile number on Douyin", false),
    /**
     * Keyword video management.
     */
    VIDEO_SEARCH("video.search", "Keyword video management", false),
    /**
     * Query POI information.
     */
    POI_SEARCH("poi.search", "Query POI information", false),
    /**
     * Silently authorize to directly get the user's open ID.
     */
    LOGIN_ID("login_id", "Silently authorize to directly get the user's open ID", false),
    /**
     * Douyin data permissions. Disabled by default, requires application in the management center. Query user's likes,
     * comments, shares, homepage visits, and other related data.
     */
    DATA_EXTERNAL_USER("data.external.user",
            "Query user's likes, comments, shares, homepage visits, and other related data", false),
    /**
     * Query likes, comments, shares, and other related data for works.
     */
    DATA_EXTERNAL_ITEM("data.external.item", "Query likes, comments, shares, and other related data for works", false),
    /**
     * Get user fan portrait data.
     */
    FANS_DATA("fans.data", "Get user fan portrait data", false),
    /**
     * Get Douyin hot content.
     */
    HOTSEARCH("hotsearch", "Get Douyin hot content", false),
    /**
     * Star chart talent and corresponding index evaluation scores, as well as talent rankings in the 6 major hot
     * dimensions of the star chart.
     */
    STAR_TOP_SCORE_DISPLAY("star_top_score_display",
            "Star chart talent and corresponding index evaluation scores, as well as talent rankings in the 6 major hot dimensions of the star chart",
            false),
    /**
     * Star chart talent and corresponding index evaluation scores, as well as talent rankings in the 6 major hot
     * dimensions of the star chart.
     */
    STAR_TOPS("star_tops",
            "Star chart talent and corresponding index evaluation scores, as well as talent rankings in the 6 major hot dimensions of the star chart",
            false),
    /**
     * Star chart talent and corresponding index evaluation scores, as well as talent rankings in the 6 major hot
     * dimensions of the star chart.
     */
    STAR_AUTHOR_SCORE_DISPLAY("star_author_score_display",
            "Star chart talent and corresponding index evaluation scores, as well as talent rankings in the 6 major hot dimensions of the star chart",
            false),
    /**
     * Get user's video sharing data through sharing SDK.
     */
    notes("data.external.sdk_share", "Get user's video sharing data through sharing SDK", false),
    /**
     * Targeted opening. Disabled by default, requires targeted opening. Query Douyin movie charts, Douyin drama charts,
     * Douyin variety show charts data.
     */
    DISCOVERY_ENT("discovery.ent", "Query Douyin movie charts, Douyin drama charts, Douyin variety show charts data",
            false);

    /**
     * The scope string as defined by Douyin.
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
