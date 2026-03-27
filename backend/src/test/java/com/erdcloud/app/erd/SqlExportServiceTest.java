package com.erdcloud.app.erd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SqlExportServiceTest {

    private final SqlExportService sqlExportService = new SqlExportService(new ObjectMapper());

    @Test
    void exportsCreateTableStatement() {
        String json = """
            {
              "entities": [
                {
                  "id": "user",
                  "name": "users",
                  "fields": [
                    { "name": "id", "dataType": "bigint", "nullable": false, "primaryKey": true },
                    { "name": "email", "dataType": "varchar", "nullable": false, "primaryKey": false }
                  ]
                }
              ],
              "relationships": [],
              "notes": []
            }
            """;

        String sql = sqlExportService.export(json, "postgresql");

        assertThat(sql).contains("CREATE TABLE users");
        assertThat(sql).contains("id BIGINT NOT NULL");
        assertThat(sql).contains("PRIMARY KEY (id)");
    }
}
