package com.raytheon.uf.ooi.plugin.instrumentagent;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

public class JsonHelper {
    private static final IUFStatusHandler log = UFStatus
            .getHandler(JsonHelper.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    protected static final TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {
    };
    protected static final TypeReference<List<Object>> listType = new TypeReference<List<Object>>() {
    };
    protected static final TypeReference<List<Map<String, Object>>> mapListType = new TypeReference<List<Map<String, Object>>>() {
    };

    static {
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        mapper.configure(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS,
                false);
    }

    private JsonHelper() {
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException ignore) {
            try {
                return mapper.writeValueAsString(obj.toString());
            } catch (IOException e) {
                log.error("Unable to generate JSON response", e);
                return "\"Failed to encode response as JSON; see log for details.\"";
            }
        }
    }

    public static Map<String, Object> toMap(String json) throws IOException {
        return mapper.readValue(json, mapType);
    }

    public static List<Object> toList(String json) throws IOException {
        JsonNode node = mapper.readTree(json);
        if (node.isArray())
            return mapper.readValue(json, listType);
        return null;
    }

    /**
     * Converts Json list of dictionaries to list of Map
     *
     * @param json
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> toMapList(String json)
            throws IOException {
        JsonNode node = mapper.readTree(json);
        if (node.isArray())
            return mapper.readValue(json, mapListType);
        return null;
    }

    public static Object toObject(String json) throws IOException {
        JsonNode node = mapper.readTree(json);
        if (node.isArray()) {
            return toList(json);
        } else if (node.isObject()) {
            return toMap(json);
        } else {
            return node.getTextValue();
        }
    }
}
