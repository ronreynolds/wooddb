package com.ronreynolds.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.core.util.Separators.Spacing;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

/**
 * ObjectMappers are thread-safe if you don't mess with their config while they're in use; so the
 * idea is to provide a collection of pre-configured singleton ObjectMappers and a few utility
 * methods to make it easier to use them
 *
 * copied from my own util-core library v2.0.2 to avoid pulling in the whole lib (for now and because it's not public)
 */
public class ObjectMappers {
    public static class TypeSerDeTuple<T> {
        final Class<T> type;
        final StdSerializer<T> serializer;
        final StdDeserializer<? extends T> deserializer;

        public TypeSerDeTuple(Class<T> type, StdSerializer<T> serializer, StdDeserializer<T> deserializer) {
            this.type = type;
            this.serializer = serializer;
            this.deserializer = deserializer;
        }

        public static <T> TypeSerDeTuple<T> of(Class<T> type, StdSerializer<T> serializer, StdDeserializer<T> deserializer) {
            return new TypeSerDeTuple<>(type, serializer, deserializer);
        }

        public void addToModule(SimpleModule module) {
            module.addSerializer(type, serializer);
            module.addDeserializer(type, deserializer);
        }
    }

    // don't auto-close writers after they're read
    private static final ObjectMapper DEFAULT = new ObjectMapper().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

    // supports /*comments*/, 'single-quotes', fieldsWithoutQuotes:, and trailing commas,
    private static final ObjectMapper RELAXED = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
                    true) // - not available in 2.4.4, Feature.ALLOW_TRAILING_COMMA)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                    DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    // removes a lot of excess spacing (but not all of it)
    public static final PrettyPrinter compactPrettyPrinter = new DefaultPrettyPrinter()
            .withSeparators(
                    Separators.createDefaultInstance()
                            .withArrayEmptySeparator("")
                            .withObjectEmptySeparator("")
                            .withArrayValueSpacing(Spacing.NONE)
                            .withObjectFieldValueSpacing(Spacing.NONE)
                            .withObjectEntrySpacing(Spacing.NONE)
            )
            .withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
            .withObjectIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

    public static ObjectMapper defaultMapper() {
        return DEFAULT;
    }

    public static ObjectMapper relaxedMapper() {
        return RELAXED;
    }

    public static ObjectMapper yamlMapper() {
        return new ObjectMapper(new YAMLFactory());
    }

    public static void addSerDe(TypeSerDeTuple<?>... tuples) {
        SimpleModule module = new SimpleModule();
        for (TypeSerDeTuple<?> tuple : tuples) {
            tuple.addToModule(module);
        }
        DEFAULT.registerModule(module);
        RELAXED.registerModule(module);
    }

    public static String toPrettyJson(Object o) {
        try {
            return defaultMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (IOException jpx) {
            throw new IllegalStateException("failed to generate JSON", jpx);
        }
    }

    public static void toPrettyJson(Object o, Writer writer) throws IOException {
        defaultMapper().writerWithDefaultPrettyPrinter().writeValue(writer, o);
    }

    public static void toPrettyJson(Object o, OutputStream outputStream) throws IOException {
        defaultMapper().writerWithDefaultPrettyPrinter().writeValue(outputStream, o);
    }

    public static void toPrettyYaml(Object o, Writer writer) throws IOException {
        yamlMapper().writerWithDefaultPrettyPrinter().writeValues(writer).write(o).close();
    }

    public static void toPrettyYaml(Object o, OutputStream outputStream) throws IOException {
        yamlMapper().writerWithDefaultPrettyPrinter().writeValues(outputStream).write(o).close();
    }

    public static String toCompactJson(Object o) {
        try {
            return defaultMapper().writeValueAsString(o);
        } catch (IOException jpx) {
            throw new IllegalStateException("failed to generate JSON", jpx);
        }
    }

    public static void toCompactJson(Object o, Writer writer) throws IOException {
        defaultMapper().writeValue(writer, o);
    }

    public static void toCompactJson(Object o, OutputStream outputStream) throws IOException {
        defaultMapper().writeValue(outputStream, o);
    }

    public static JavaType getMapType(Class<? extends Map<?, ?>> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return defaultMapper().getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }
}