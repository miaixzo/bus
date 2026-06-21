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
package org.miaixz.bus.gitlab.support;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.miaixz.bus.gitlab.models.User;
import org.miaixz.bus.logger.Logger;

import tools.jackson.core.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.type.CollectionType;

/**
 * Jackson JSON Configuration and utility class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Produces(MediaType.APPLICATION_JSON)
public class JacksonJson implements ContextResolver<ObjectMapper> {

    private static final SimpleDateFormat iso8601UtcFormat;

    static {
        iso8601UtcFormat = new SimpleDateFormat(ISO8601.UTC_PATTERN);
        iso8601UtcFormat.setLenient(true);
        iso8601UtcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code JacksonJson} instance.
     */

    public JacksonJson() {
        this(PropertyNamingStrategies.SNAKE_CASE, Include.NON_NULL);
    }

    /**
     * Constructs a new {@code JacksonJson} instance with the supplied naming and inclusion policy.
     *
     * @param propertyNamingStrategy the property naming strategy
     * @param include                the inclusion policy
     */
    private JacksonJson(PropertyNamingStrategy propertyNamingStrategy, Include include) {
        objectMapper = createObjectMapper(propertyNamingStrategy, include);
    }

    /**
     * Creates the Jackson 3 mapper used by this GitLab JSON adapter.
     *
     * @param propertyNamingStrategy the property naming strategy
     * @param include                the inclusion policy
     * @return the configured mapper
     */
    private static ObjectMapper createObjectMapper(PropertyNamingStrategy propertyNamingStrategy, Include include) {
        SimpleModule module = new SimpleModule("GitLabApiJsonModule");
        module.addSerializer(Date.class, new JsonDateSerializer());
        module.addDeserializer(Date.class, new JsonDateDeserializer());

        return JsonMapper.builder().propertyNamingStrategy(propertyNamingStrategy)
                .changeDefaultPropertyInclusion(value -> JsonInclude.Value.construct(include, include))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).addModule(module).build();
    }

    /**
     * Gets a the supplied object output as a formatted JSON string. Null properties will result in the value of the
     * property being null. This is meant to be used for toString() implementations of GitLab4J classes.
     *
     * @param <T>    the generics type for the provided object
     * @param object the object to output as a JSON string
     * @return a String containing the JSON for the specified object
     */
    public static <T> String toJsonString(final T object) {
        return (JacksonJsonSingletonHelper.JACKSON_JSON.marshal(object));
    }

    /**
     * Parse the provided String into a JsonNode instance.
     *
     * @param jsonString a String containing JSON to parse
     * @return a JsonNode with the String parsed into a JSON tree
     * @throws IOException if any IO error occurs
     */
    public static JsonNode toJsonNode(String jsonString) throws IOException {
        return (JacksonJsonSingletonHelper.JACKSON_JSON.objectMapper.readTree(jsonString));
    }

    /**
     * Reads and parses the String containing JSON data and returns a JsonNode tree representation.
     *
     * @param postData a String holding the POST data
     * @return a JsonNode instance containing the parsed JSON
     * @throws JacksonException if a JSON error occurs
     * @throws IOException      if an error occurs reading the JSON data
     */
    public JsonNode readTree(String postData) throws JacksonException, IOException {
        return (objectMapper.readTree(postData));
    }

    /**
     * Reads and parses the JSON data on the specified Reader instance to a JsonNode tree representation.
     *
     * @param reader the Reader instance that contains the JSON data
     * @return a JsonNode instance containing the parsed JSON
     * @throws JacksonException if a JSON error occurs
     * @throws IOException      if an error occurs reading the JSON data
     */
    public JsonNode readTree(Reader reader) throws JacksonException, IOException {
        return (objectMapper.readTree(reader));
    }

    /**
     * Unmarshal the JsonNode (tree) to an instance of the provided class.
     *
     * @param <T>        the generics type for the return value
     * @param returnType an instance of this type class will be returned
     * @param tree       the JsonNode instance that contains the JSON data
     * @return an instance of the provided class containing the data from the tree
     * @throws JacksonException if a JSON error occurs
     * @throws IOException      if an error occurs reading the JSON data
     */
    public <T> T unmarshal(Class<T> returnType, JsonNode tree) throws JacksonException, IOException {
        ObjectMapper objectMapper = getContext(returnType);
        return (objectMapper.treeToValue(tree, returnType));
    }

    /**
     * Unmarshal the JSON data on the specified Reader instance to an instance of the provided class.
     *
     * @param <T>        the generics type for the return value
     * @param returnType an instance of this type class will be returned
     * @param reader     the Reader instance that contains the JSON data
     * @return an instance of the provided class containing the parsed data from the Reader
     * @throws JacksonException if a JSON error occurs
     * @throws IOException      if an error occurs reading the JSON data
     */
    public <T> T unmarshal(Class<T> returnType, Reader reader) throws JacksonException, IOException {
        ObjectMapper objectMapper = getContext(returnType);
        return (objectMapper.readValue(reader, returnType));
    }

    /**
     * Unmarshal the JSON data contained by the string and populate an instance of the provided returnType class.
     *
     * @param <T>        the generics type for the return value
     * @param returnType an instance of this type class will be returned
     * @param postData   a String holding the POST data
     * @return an instance of the provided class containing the parsed data from the string
     * @throws JacksonException if a JSON error occurs
     * @throws IOException      if an error occurs reading the JSON data
     */
    public <T> T unmarshal(Class<T> returnType, String postData) throws JacksonException, IOException {
        ObjectMapper objectMapper = getContext(returnType);
        return (objectMapper.readValue(postData, returnType));
    }

    /**
     * Unmarshal the JSON data on the specified Reader instance and populate a List of instances of the provided
     * returnType class.
     *
     * @param <T>        the generics type for the List
     * @param returnType an instance of this type class will be contained in the returned List
     * @param reader     the Reader instance that contains the JSON data
     * @return a List of the provided class containing the parsed data from the Reader
     * @throws JacksonException if a JSON error occurs
     * @throws IOException      if an error occurs reading the JSON data
     */
    public <T> List<T> unmarshalList(Class<T> returnType, Reader reader) throws JacksonException, IOException {
        ObjectMapper objectMapper = getContext(null);
        CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, returnType);
        return (objectMapper.readValue(reader, javaType));
    }

    /**
     * Unmarshal the JSON data contained by the string and populate a List of instances of the provided returnType
     * class.
     *
     * @param <T>        the generics type for the List
     * @param returnType an instance of this type class will be contained in the returned List
     * @param postData   a String holding the POST data
     * @return a List of the provided class containing the parsed data from the string
     * @throws JacksonException if a JSON error occurs
     * @throws IOException      if an error occurs reading the JSON data
     */
    public <T> List<T> unmarshalList(Class<T> returnType, String postData) throws JacksonException, IOException {
        ObjectMapper objectMapper = getContext(null);
        CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, returnType);
        return (objectMapper.readValue(postData, javaType));
    }

    /**
     * Unmarshal the JSON data on the specified Reader instance and populate a Map of String keys and values of the
     * provided returnType class.
     *
     * @param <T>        the generics type for the Map value
     * @param returnType an instance of this type class will be contained the values of the Map
     * @param reader     the Reader instance that contains the JSON data
     * @return a Map containing the parsed data from the Reader
     * @throws JacksonException if a JSON error occurs
     * @throws IOException      if an error occurs reading the JSON data
     */
    public <T> Map<String, T> unmarshalMap(Class<T> returnType, Reader reader) throws JacksonException, IOException {
        ObjectMapper objectMapper = getContext(null);
        return (objectMapper.readValue(reader, new TypeReference<Map<String, T>>() {
        }));
    }

    /**
     * Returns the context.
     *
     * @param objectType the object type value
     * @return the result
     */

    @Override
    public ObjectMapper getContext(Class<?> objectType) {
        return (objectMapper);
    }

    /**
     * Marshals the supplied object out as a formatted JSON string.
     *
     * @param <T>    the generics type for the provided object
     * @param object the object to output as a JSON string
     * @return a String containing the JSON for the specified object
     */
    public <T> String marshal(final T object) {

        if (object == null) {
            throw new IllegalArgumentException("object parameter is null");
        }

        ObjectWriter writer = objectMapper.writer().withDefaultPrettyPrinter();
        String results = null;
        try {
            results = writer.writeValueAsString(object);
        } catch (JacksonException e) {
            Logger.warn(
                    false,
                    "GitLab",
                    e,
                    "JSON serialization failed: objectType={}, exception={}",
                    object.getClass().getName(),
                    e.getClass().getSimpleName());
        }

        return (results);
    }

    /**
     * JsonSerializer for serializing dates s yyyy-mm-dd in UTC timezone.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class DateOnlySerializer extends ValueSerializer<Date> {

        /**
         * Executes the serialize operation.
         *
         * @param date    the date value
         * @param gen     the gen value
         * @param context the context value
         * @throws JacksonException if the operation fails
         */

        @Override
        public void serialize(Date date, JsonGenerator gen, SerializationContext context) throws JacksonException {
            String dateString = ISO8601.dateOnly(date);
            gen.writeString(dateString);
        }

    }

    /**
     * JsonSerializer for serializing ISO8601 formatted dates.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class JsonDateSerializer extends ValueSerializer<Date> {

        /**
         * Executes the serialize operation.
         *
         * @param date    the date value
         * @param gen     the gen value
         * @param context the context value
         * @throws JacksonException if the operation fails
         */

        @Override
        public void serialize(Date date, JsonGenerator gen, SerializationContext context) throws JacksonException {
            String iso8601String = ISO8601.toString(date);
            gen.writeString(iso8601String);
        }

    }

    /**
     * JsonDeserializer for deserializing ISO8601 formatted dates.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class JsonDateDeserializer extends ValueDeserializer<Date> {

        /**
         * Executes the deserialize operation.
         *
         * @param jsonparser the jsonparser value
         * @param context    the context value
         * @return the result
         * @throws JacksonException if the operation fails
         */

        @Override
        public Date deserialize(JsonParser jsonparser, DeserializationContext context) throws JacksonException {

            try {
                return (ISO8601.toDate(jsonparser.getString()));
            } catch (ParseException e) {
                Logger.warn(
                        false,
                        "GitLab",
                        e,
                        "GitLab JSON date deserialization failed: valueLength={}, exception={}",
                        jsonparser.getString() == null ? -1 : jsonparser.getString().length(),
                        e.getClass().getSimpleName());
                throw new IllegalArgumentException(e);
            }
        }

    }

    /**
     * Serializer for the odd User instances in the "approved_by" array in the merge_request JSON.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class UserListSerializer extends ValueSerializer<List<User>> {

        /**
         * Executes the serialize operation.
         *
         * @param value   the value value
         * @param jgen    the jgen value
         * @param context the context value
         * @throws JacksonException if the operation fails
         */

        @Override
        public void serialize(List<User> value, JsonGenerator jgen, SerializationContext context)
                throws JacksonException {

            jgen.writeStartArray();
            for (User user : value) {
                jgen.writeStartObject();
                jgen.writeName("user");
                jgen.writePOJO(user);
                jgen.writeEndObject();
            }
            jgen.writeEndArray();
        }

    }

    /**
     * Deserializer for the odd User instances in the "approved_by" array in the merge_request JSON.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class UserListDeserializer extends ValueDeserializer<List<User>> {

        /**
         * Executes the deserialize operation.
         *
         * @param jsonParser the json parser value
         * @param context    the context value
         * @return the result
         * @throws JacksonException if the operation fails
         */

        @Override
        public List<User> deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {

            JsonNode tree = jsonParser.readValueAsTree();
            int numUsers = tree.size();
            List<User> users = new ArrayList<>(numUsers);
            for (int i = 0; i < numUsers; i++) {
                JsonNode node = tree.get(i);
                JsonNode userNode = node.get("user");
                User user = context.readTreeAsValue(userNode, User.class);
                users.add(user);
            }

            return (users);
        }

    }

    /**
     * Gets the ObjectMapper contained by this instance.
     *
     * @return the ObjectMapper contained by this instance
     */
    public ObjectMapper getObjectMapper() {
        return (objectMapper);
    }

    /**
     * Unmarshal the JSON data and populate a Map of String keys and values of the provided returnType class.
     *
     * @param <T>        the generics type for the Map value
     * @param returnType an instance of this type class will be contained the values of the Map
     * @param jsonData   the String containing the JSON data
     * @return a Map containing the parsed data from the String
     * @throws JacksonException if a JSON error occurs
     * @throws IOException      if an error occurs reading the JSON data
     */
    public <T> Map<String, T> unmarshalMap(Class<T> returnType, String jsonData) throws JacksonException, IOException {
        ObjectMapper objectMapper = getContext(null);
        return (objectMapper.readValue(jsonData, new TypeReference<Map<String, T>>() {
        }));
    }

    /**
     * This class is used to create a thread-safe singleton instance of JacksonJson customized to be used by
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class JacksonJsonSingletonHelper {

        private static final JacksonJson JACKSON_JSON = new JacksonJson(PropertyNamingStrategies.LOWER_CAMEL_CASE,
                Include.ALWAYS);

    }

}
