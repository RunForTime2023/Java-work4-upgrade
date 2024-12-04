package org.webapp.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
public class CustomizeUtils {
    public static Map<String, String> convertPojoToMap(Object object) {
        Map<String, String> map = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field each : fields) {
            each.setAccessible(true);
            try {
                map.put(StringUtils.camelToUnderline(each.getName()), each.get(object).toString());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                map.clear();
                log.error("Fail to convert from {} to map.", object.getClass().getName(), e);
                break;
            }
        }
        return map;
    }

    public static <T> T convertMapToPojo(Map<Object, Object> map, Class<T> objectClass) {
        if (map.isEmpty()) {
            return null;
        }
        try {
            T object = objectClass.getConstructor().newInstance();
            Field[] fields = objectClass.getDeclaredFields();
            for (Field each : fields) {
                each.setAccessible(true);
                Object value = map.get(StringUtils.camelToUnderline(each.getName()));
                if (value != null) {
                    switch (each.getType().getName()) {
                        case "int" -> each.set(object, Integer.parseInt(value.toString()));
                        case "boolean" -> each.set(object, Boolean.parseBoolean(value.toString()));
                        case "java.time.LocalDateTime" -> each.set(object, LocalDateTime.parse(value.toString()));
                        default -> each.set(object, value);
                    }
                }
            }
            return object;
        } catch (Exception e) {
            log.error("Fail to convert the map to {}.", objectClass.getName(), e);
            return null;
        }
    }

    public static ObjectMapper customizedObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        objectMapper.registerModule(javaTimeModule);
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC-8"));
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return objectMapper;
    }

    public static boolean isAuthParamValid(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }
        return true;
    }
}
