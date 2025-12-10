package vn.com.viettel.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    /**
     * Convert
     * @param input
     * @param dtoClass
     * @return
     */
    public static List<?> convertToEntity(List<Tuple> input, Class<?> dtoClass) {
        List<Object> arrayList = new ArrayList<>();
        input.forEach((Tuple tuple) -> {
            Map<String, Object> temp = new HashMap<>();
            tuple.getElements().
                    forEach(
                            tupleElement -> {
                                Object value = tuple.get(tupleElement.getAlias());
                                temp.put(tupleElement.getAlias().toLowerCase(), value);
                            });


            ObjectMapper map = new ObjectMapper();
            map.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            map.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                //converting to json
                String mapToString = map.writeValueAsString(temp);
                //converting json to entity
                arrayList.add(map.readValue(mapToString, dtoClass));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage());
            }
        });
        return arrayList;
    }
}
