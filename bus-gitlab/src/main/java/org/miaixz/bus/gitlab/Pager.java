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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.miaixz.bus.gitlab.models.Constants;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.logger.Logger;

/**
 * <p>
 * This class defines an Iterator implementation that is used as a paging iterator for all API methods that return a
 * List of objects. It hides the details of interacting with the GitLab API when paging is involved simplifying
 * accessing large lists of objects.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 *   // Get a Pager instance that will page through the projects with 10 projects per page
 *   Pager&lt;Project&gt; projectPager = gitlabApi.getProjectsApi().getProjectsPager(10);
 *
 *   // Iterate through the pages and print out the name and description
 *   while (projectsPager.hasNext())) {
 *       List&lt;Project&gt; projects = projectsPager.next();
 *       for (Project project : projects) {
 *           System.out.println(project.getName() + " : " + project.getDescription());
 *       }
 *   }
 * </pre>
 *
 * @param <T> the GitLab4J type contained in the List.
 * @author Kimi Liu
 * @since Java 21+
 */
public class Pager<T> implements Iterator<List<T>>, Constants {

    private int itemsPerPage;
    private int totalPages;
    private int totalItems;
    private int currentPage;
    private int kaminariNextPage;

    private List<String> pageParam = new ArrayList<>(1);
    private List<T> currentItems;
    private Stream<T> pagerStream = null;

    private AbstractApi api;
    private MultivaluedMap<String, String> queryParams;
    private Object[] pathArgs;

    private static JacksonJson jacksonJson = new JacksonJson();
    private static ObjectMapper mapper = jacksonJson.getObjectMapper();
    private JavaType javaType;

    /**
     * Creates a Pager instance to access the API through the specified path and query parameters.
     *
     * @param api          the AbstractApi implementation to communicate through
     * @param type         the GitLab4J type that will be contained in the List
     * @param itemsPerPage items per page
     * @param queryParams  HTTP query params
     * @param pathArgs     HTTP path arguments
     * @throws GitLabApiException if any error occurs
     */
    public Pager(AbstractApi api, Class<T> type, int itemsPerPage, MultivaluedMap<String, String> queryParams,
            Object... pathArgs) throws GitLabApiException {

        Logger.info(
                true,
                "GitLab",
                "GitLab pager initialization started: itemType={}, requestedItemsPerPage={}, queryParamCount={}, pathArgCount={}",
                type == null ? "null" : type.getSimpleName(),
                itemsPerPage,
                queryParams == null ? 0 : queryParams.size(),
                pathArgs == null ? 0 : pathArgs.length);
        javaType = mapper.getTypeFactory().constructCollectionType(List.class, type);

        if (itemsPerPage < 1) {
            Logger.debug(
                    true,
                    "GitLab",
                    "GitLab pager using default per-page setting: requestedItemsPerPage={}, defaultItemsPerPage={}",
                    itemsPerPage,
                    api.getDefaultPerPage());
            itemsPerPage = api.getDefaultPerPage();
        }

        // Make sure the per_page parameter is present
        if (queryParams == null) {
            queryParams = new GitLabApiForm().withParam(PER_PAGE_PARAM, itemsPerPage).asMap();
        } else {
            queryParams.remove(PER_PAGE_PARAM);
            queryParams.add(PER_PAGE_PARAM, Integer.toString(itemsPerPage));
        }

        // Set the page param to 1
        pageParam = new ArrayList<>();
        pageParam.add("1");
        queryParams.put(PAGE_PARAM, pageParam);
        Logger.debug(
                true,
                "GitLab",
                "GitLab pager first page request started: itemType={}, itemsPerPage={}, queryParamCount={}, pathArgCount={}",
                type == null ? "null" : type.getSimpleName(),
                itemsPerPage,
                queryParams.size(),
                pathArgs == null ? 0 : pathArgs.length);
        Response response = api.get(Response.Status.OK, queryParams, pathArgs);

        try {
            currentItems = mapper.readValue((InputStream) response.getEntity(), javaType);
        } catch (Exception e) {
            Logger.warn(
                    false,
                    "GitLab",
                    e,
                    "GitLab pager first page decoding failed: itemType={}, status={}, exception={}",
                    type == null ? "null" : type.getSimpleName(),
                    response.getStatus(),
                    e.getClass().getSimpleName());
            throw new GitLabApiException(e);
        }

        if (currentItems == null) {
            Logger.warn(
                    false,
                    "GitLab",
                    "GitLab pager first page rejected: itemType={}, status={}, reason={}",
                    type == null ? "null" : type.getSimpleName(),
                    response.getStatus(),
                    "nullItems");
            throw new GitLabApiException("Invalid response from from GitLab server");
        }

        this.api = api;
        this.queryParams = queryParams;
        this.pathArgs = pathArgs;
        this.itemsPerPage = getIntHeaderValue(response, PER_PAGE);

        // Some API endpoints do not return the "X-Per-Page" header when there is only 1 page, check for that condition
        // and act accordingly
        if (this.itemsPerPage == -1) {
            this.itemsPerPage = itemsPerPage;
            totalPages = 1;
            totalItems = currentItems.size();
            Logger.info(
                    false,
                    "GitLab",
                    "GitLab pager initialized without pagination headers: itemType={}, itemsPerPage={}, currentItemCount={}, totalPages={}, totalItems={}",
                    type == null ? "null" : type.getSimpleName(),
                    this.itemsPerPage,
                    currentItems.size(),
                    totalPages,
                    totalItems);
            return;
        }

        totalPages = getIntHeaderValue(response, TOTAL_PAGES_HEADER);
        totalItems = getIntHeaderValue(response, TOTAL_HEADER);

        // Since GitLab 11.8 and behind the api_kaminari_count_with_limit feature flag,
        // if the number of resources is more than 10,000, the X-Total and X-Total-Page
        // headers as well as the rel="last" Link are not present in the response headers.
        if (totalPages == -1 || totalItems == -1) {

            int nextPage = getIntHeaderValue(response, NEXT_PAGE_HEADER);
            if (nextPage < 2) {
                totalPages = 1;
                totalItems = currentItems.size();
                Logger.info(
                        false,
                        "GitLab",
                        "GitLab pager initialized with truncated total headers: itemType={}, nextPage={}, totalPages={}, totalItems={}, currentItemCount={}",
                        type == null ? "null" : type.getSimpleName(),
                        nextPage,
                        totalPages,
                        totalItems,
                        currentItems.size());
            } else {
                kaminariNextPage = 2;
                Logger.warn(
                        false,
                        "GitLab",
                        "GitLab pager using Kaminari continuation: itemType={}, nextPage={}, itemsPerPage={}, currentItemCount={}",
                        type == null ? "null" : type.getSimpleName(),
                        kaminariNextPage,
                        this.itemsPerPage,
                        currentItems.size());
            }
        }
        Logger.info(
                false,
                "GitLab",
                "GitLab pager initialization completed: itemType={}, itemsPerPage={}, totalPages={}, totalItems={}, currentItemCount={}, kaminariNextPage={}",
                type == null ? "null" : type.getSimpleName(),
                this.itemsPerPage,
                totalPages,
                totalItems,
                currentItems.size(),
                kaminariNextPage);
    }

    /**
     * Get the specified header value from the Response instance.
     *
     * @param response the Response instance to get the value from
     * @param key      the HTTP header key to get the value for
     * @return the specified header value from the Response instance, or null if the header is not present
     * @throws GitLabApiException if any error occurs
     */
    private String getHeaderValue(Response response, String key) throws GitLabApiException {

        String value = response.getHeaderString(key);
        value = (value != null ? value.trim() : null);
        if (value == null || value.length() == 0) {
            return (null);
        }

        return (value);
    }

    /**
     * Get the specified integer header value from the Response instance.
     *
     * @param response the Response instance to get the value from
     * @param key      the HTTP header key to get the value for
     * @return the specified integer header value from the Response instance, or -1 if the header is not present
     * @throws GitLabApiException if any error occurs
     */
    private int getIntHeaderValue(Response response, String key) throws GitLabApiException {

        String value = getHeaderValue(response, key);
        if (value == null) {
            Logger.debug(false, "GitLab", "GitLab pager header missing: header={}", key);
            return -1;
        }

        try {
            return (Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            Logger.warn(
                    false,
                    "GitLab",
                    nfe,
                    "GitLab pager header parsing failed: header={}, valueLength={}, exception={}",
                    key,
                    value.length(),
                    nfe.getClass().getSimpleName());
            throw new GitLabApiException("Invalid '" + key + "' header value (" + value + ") from server");
        }
    }

    /**
     * Sets the "page" query parameter.
     *
     * @param page the value for the "page" query parameter
     */
    private void setPageParam(int page) {
        pageParam.set(0, Integer.toString(page));
        queryParams.put(PAGE_PARAM, pageParam);
    }

    /**
     * Get the items per page value.
     *
     * @return the items per page value
     */
    public int getItemsPerPage() {
        return (itemsPerPage);
    }

    /**
     * Get the total number of pages returned by the GitLab API.
     *
     * @return the total number of pages returned by the GitLab API, or -1 if the Kaminari limit of 10,000 has been
     *         exceeded
     */
    public int getTotalPages() {
        return (totalPages);
    }

    /**
     * Get the total number of items (T instances) returned by the GitLab API.
     *
     * @return the total number of items (T instances) returned by the GitLab API, or -1 if the Kaminari limit of 10,000
     *         has been exceeded
     */
    public int getTotalItems() {
        return (totalItems);
    }

    /**
     * Get the current page of the iteration.
     *
     * @return the current page of the iteration
     */
    public int getCurrentPage() {
        return (currentPage);
    }

    /**
     * Returns the true if there are additional pages to iterate over, otherwise returns false.
     *
     * @return true if there are additional pages to iterate over, otherwise returns false
     */
    @Override
    public boolean hasNext() {
        boolean hasNext = currentPage < totalPages || currentPage < kaminariNextPage;
        Logger.debug(
                false,
                "GitLab",
                "GitLab pager hasNext evaluated: currentPage={}, totalPages={}, kaminariNextPage={}, hasNext={}",
                currentPage,
                totalPages,
                kaminariNextPage,
                hasNext);
        return hasNext;
    }

    /**
     * Returns the next List in the iteration containing the next page of objects.
     *
     * @return the next List in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     * @throws RuntimeException       if a GitLab API error occurs, will contain a wrapped GitLabApiException with the
     *                                details of the error
     */
    @Override
    public List<T> next() {
        Logger.debug(
                true,
                "GitLab",
                "GitLab pager next page requested: currentPage={}, nextPage={}",
                currentPage,
                currentPage + 1);
        return (page(currentPage + 1));
    }

    /**
     * This method is not implemented and will throw an UnsupportedOperationException if called.
     *
     * @throws UnsupportedOperationException when invoked
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the first page of List. Will rewind the iterator.
     *
     * @return the first page of List
     */
    public List<T> first() {
        Logger.debug(true, "GitLab", "GitLab pager first page requested: currentPage={}", currentPage);
        return (page(1));
    }

    /**
     * Returns the last page of List. Will set the iterator to the end.
     *
     * @return the last page of List
     * @throws GitLabApiException if any error occurs
     */
    public List<T> last() throws GitLabApiException {

        if (kaminariNextPage != 0) {
            Logger.warn(
                    false,
                    "GitLab",
                    "GitLab pager last page rejected: reason={}, currentPage={}, kaminariNextPage={}",
                    "kaminariCountLimitExceeded",
                    currentPage,
                    kaminariNextPage);
            throw new GitLabApiException("Kaminari count limit exceeded, unable to fetch last page");
        }

        Logger.debug(true, "GitLab", "GitLab pager last page requested: totalPages={}", totalPages);
        return (page(totalPages));
    }

    /**
     * Returns the previous page of List. Will set the iterator to the previous page.
     *
     * @return the previous page of List
     */
    public List<T> previous() {
        Logger.debug(
                true,
                "GitLab",
                "GitLab pager previous page requested: currentPage={}, previousPage={}",
                currentPage,
                currentPage - 1);
        return (page(currentPage - 1));
    }

    /**
     * Returns the current page of List.
     *
     * @return the current page of List
     */
    public List<T> current() {
        Logger.debug(true, "GitLab", "GitLab pager current page requested: currentPage={}", currentPage);
        return (page(currentPage));
    }

    /**
     * Returns the specified page of List.
     *
     * @param pageNumber the page to get
     * @return the specified page of List
     * @throws NoSuchElementException if the iteration has no more elements
     * @throws RuntimeException       if a GitLab API error occurs, will contain a wrapped GitLabApiException with the
     *                                details of the error
     */
    public List<T> page(int pageNumber) {

        if (currentPage == 0 && pageNumber == 1) {
            currentPage = 1;
            Logger.debug(
                    false,
                    "GitLab",
                    "GitLab pager first page served from initial response: pageNumber={}, itemCount={}",
                    pageNumber,
                    currentItems == null ? 0 : currentItems.size());
            return (currentItems);
        }

        if (currentPage == pageNumber) {
            Logger.debug(
                    false,
                    "GitLab",
                    "GitLab pager page served from cache: pageNumber={}, itemCount={}",
                    pageNumber,
                    currentItems == null ? 0 : currentItems.size());
            return (currentItems);
        }

        if (pageNumber > totalPages && pageNumber > kaminariNextPage) {
            Logger.warn(
                    false,
                    "GitLab",
                    "GitLab pager page rejected: pageNumber={}, currentPage={}, totalPages={}, kaminariNextPage={}, reason={}",
                    pageNumber,
                    currentPage,
                    totalPages,
                    kaminariNextPage,
                    "afterLastPage");
            throw new NoSuchElementException();
        } else if (pageNumber < 1) {
            Logger.warn(
                    false,
                    "GitLab",
                    "GitLab pager page rejected: pageNumber={}, currentPage={}, totalPages={}, reason={}",
                    pageNumber,
                    currentPage,
                    totalPages,
                    "beforeFirstPage");
            throw new NoSuchElementException();
        }

        try {

            setPageParam(pageNumber);
            Logger.debug(
                    true,
                    "GitLab",
                    "GitLab pager page request started: pageNumber={}, itemsPerPage={}, totalPages={}, kaminariNextPage={}",
                    pageNumber,
                    itemsPerPage,
                    totalPages,
                    kaminariNextPage);
            Response response = api.get(Response.Status.OK, queryParams, pathArgs);
            currentItems = mapper.readValue((InputStream) response.getEntity(), javaType);
            currentPage = pageNumber;

            if (kaminariNextPage > 0) {
                kaminariNextPage = getIntHeaderValue(response, NEXT_PAGE_HEADER);
                Logger.debug(
                        false,
                        "GitLab",
                        "GitLab pager Kaminari continuation updated: pageNumber={}, nextPage={}",
                        pageNumber,
                        kaminariNextPage);
            }

            Logger.debug(
                    false,
                    "GitLab",
                    "GitLab pager page request completed: pageNumber={}, itemCount={}, status={}, kaminariNextPage={}",
                    pageNumber,
                    currentItems == null ? 0 : currentItems.size(),
                    response.getStatus(),
                    kaminariNextPage);
            return (currentItems);

        } catch (GitLabApiException | IOException e) {
            Logger.warn(
                    false,
                    "GitLab",
                    e,
                    "GitLab pager page request failed: pageNumber={}, currentPage={}, totalPages={}, exception={}",
                    pageNumber,
                    currentPage,
                    totalPages,
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets all the items from each page as a single List instance.
     *
     * @return all the items from each page as a single List instance
     * @throws GitLabApiException if any error occurs
     */
    public List<T> all() throws GitLabApiException {

        Logger.info(
                true,
                "GitLab",
                "GitLab pager all-items fetch started: currentPage={}, totalPages={}, totalItems={}, kaminariNextPage={}",
                currentPage,
                totalPages,
                totalItems,
                kaminariNextPage);
        // Make sure that current page is 0, this will ensure the whole list is fetched
        // regardless of what page the instance is currently on.
        currentPage = 0;
        List<T> allItems = new ArrayList<>(Math.max(totalItems, 0));

        // Iterate through the pages and append each page of items to the list
        while (hasNext()) {
            allItems.addAll(next());
        }

        Logger.info(
                false,
                "GitLab",
                "GitLab pager all-items fetch completed: itemCount={}, totalPages={}, totalItems={}",
                allItems.size(),
                totalPages,
                totalItems);
        return (allItems);
    }

    /**
     * Builds and returns a Stream instance which is pre-populated with all items from all pages.
     *
     * @return a Stream instance which is pre-populated with all items from all pages
     * @throws IllegalStateException if Stream has already been issued
     * @throws GitLabApiException    if any other error occurs
     */
    public Stream<T> stream() throws GitLabApiException, IllegalStateException {

        if (pagerStream == null) {
            synchronized (this) {
                if (pagerStream == null) {
                    Logger.info(
                            true,
                            "GitLab",
                            "GitLab pager eager stream build started: currentPage={}, totalPages={}, totalItems={}",
                            currentPage,
                            totalPages,
                            totalItems);

                    // Make sure that current page is 0, this will ensure the whole list is streamed
                    // regardless of what page the instance is currently on.
                    currentPage = 0;

                    // Create a Stream.Builder to contain all the items. This is more efficient than
                    // getting a List with all() and streaming that List
                    Stream.Builder<T> streamBuilder = Stream.builder();

                    // Iterate through the pages and append each page of items to the stream builder
                    while (hasNext()) {
                        next().forEach(streamBuilder);
                    }

                    pagerStream = streamBuilder.build();
                    Logger.info(
                            false,
                            "GitLab",
                            "GitLab pager eager stream build completed: totalPages={}, totalItems={}",
                            totalPages,
                            totalItems);
                    return (pagerStream);
                }
            }
        }

        Logger.warn(false, "GitLab", "GitLab pager eager stream rejected: reason={}", "streamAlreadyIssued");
        throw new IllegalStateException("Stream already issued");
    }

    /**
     * Creates a Stream instance for lazily streaming items from the GitLab server.
     *
     * @return a Stream instance for lazily streaming items from the GitLab server
     * @throws IllegalStateException if Stream has already been issued
     */
    public Stream<T> lazyStream() throws IllegalStateException {

        if (pagerStream == null) {
            synchronized (this) {
                if (pagerStream == null) {
                    Logger.info(
                            true,
                            "GitLab",
                            "GitLab pager lazy stream creation started: currentPage={}, totalPages={}, totalItems={}",
                            currentPage,
                            totalPages,
                            totalItems);

                    // Make sure that current page is 0, this will ensure the whole list is streamed
                    // regardless of what page the instance is currently on.
                    currentPage = 0;

                    pagerStream = StreamSupport.stream(new PagerSpliterator<T>(this), false);
                    Logger.info(
                            false,
                            "GitLab",
                            "GitLab pager lazy stream created: totalPages={}, totalItems={}, kaminariNextPage={}",
                            totalPages,
                            totalItems,
                            kaminariNextPage);
                    return (pagerStream);
                }
            }
        }

        Logger.warn(false, "GitLab", "GitLab pager lazy stream rejected: reason={}", "streamAlreadyIssued");
        throw new IllegalStateException("Stream already issued");
    }

}
