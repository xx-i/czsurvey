package com.github.czsurvey.extra.data.converter;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author YanYu
 */
@Converter
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode jsonNode) {
        return jsonNode == null ? null : jsonNode.toPrettyString();
    }

    @Override
    @SneakyThrows
    public JsonNode convertToEntityAttribute(String jsonStr) {
        return StrUtil.isBlank(jsonStr) ? null : objectMapper.readTree(jsonStr);
    }
}
