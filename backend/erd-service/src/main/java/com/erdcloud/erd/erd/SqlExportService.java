package com.erdcloud.erd.erd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SqlExportService {

    private final ObjectMapper objectMapper;

    public String export(String contentJson, String dialect) {
        try {
            JsonNode root = objectMapper.readTree(contentJson);
            List<String> statements = new ArrayList<>();
            JsonNode entities = root.path("entities");
            for (JsonNode entity : entities) {
                statements.add(buildCreateTable(entity, dialect));
            }
            return String.join("\n\n", statements);
        } catch (Exception ex) {
            throw new IllegalArgumentException("ERD JSON을 SQL로 변환할 수 없습니다.");
        }
    }

    private String buildCreateTable(JsonNode entity, String dialect) {
        String tableName = entity.path("name").asText("table_" + entity.path("id").asText());
        List<String> columns = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();

        Iterator<JsonNode> fields = entity.path("fields").elements();
        while (fields.hasNext()) {
            JsonNode field = fields.next();
            String name = field.path("name").asText("column_name");
            String type = mapType(field.path("dataType").asText("varchar"), dialect);
            boolean nullable = field.path("nullable").asBoolean(true);
            boolean primaryKey = field.path("primaryKey").asBoolean(false);
            StringBuilder line = new StringBuilder(name).append(" ").append(type);
            if (!nullable) {
                line.append(" NOT NULL");
            }
            if (field.hasNonNull("defaultValue") && !field.path("defaultValue").asText().isBlank()) {
                line.append(" DEFAULT ").append(field.path("defaultValue").asText());
            }
            columns.add(line.toString());
            if (primaryKey) {
                primaryKeys.add(name);
            }
        }

        if (!primaryKeys.isEmpty()) {
            columns.add("PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
        }

        return "CREATE TABLE " + tableName + " (\n  " + String.join(",\n  ", columns) + "\n);";
    }

    private String mapType(String dataType, String dialect) {
        String normalized = dataType.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "string", "varchar" -> "VARCHAR(255)";
            case "text" -> switch (dialect.toLowerCase(Locale.ROOT)) {
                case "oracle" -> "CLOB";
                default -> "TEXT";
            };
            case "int", "integer" -> "INTEGER";
            case "long", "bigint" -> "BIGINT";
            case "boolean" -> switch (dialect.toLowerCase(Locale.ROOT)) {
                case "oracle" -> "NUMBER(1)";
                default -> "BOOLEAN";
            };
            case "datetime" -> "TIMESTAMP";
            case "date" -> "DATE";
            default -> "VARCHAR(255)";
        };
    }
}
