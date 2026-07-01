package ch.so.agi.datenportal.explore;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public final class ExploreContextJsonWriter {

    public String write(ExploreContextDto context) {
        var json = new StringBuilder(4096);
        json.append('{');
        field(json, "version", context.version());
        comma(json);
        field(json, "datasetId", context.datasetId());
        comma(json);
        field(json, "title", context.title());
        optionalStringField(json, "description", context.description());
        comma(json);
        field(json, "canonicalUrl", context.canonicalUrl());
        optionalStringField(json, "updatedAt", context.updatedAt());
        optionalStringField(json, "license", context.license());
        comma(json);
        execution(json, context.execution());
        comma(json);
        tables(json, context.tables());
        comma(json);
        recipes(json, context.recipes());
        comma(json);
        snippets(json, context.codeSnippets());
        comma(json);
        featureFlags(json, context.featureFlags());
        json.append('}');
        return json.toString();
    }

    private static void execution(StringBuilder json, ExploreExecutionDto execution) {
        name(json, "execution").append('{');
        field(json, "engine", execution.engine());
        comma(json);
        field(json, "mode", execution.mode());
        comma(json);
        field(json, "maxPreviewRows", execution.maxPreviewRows());
        comma(json);
        field(json, "maxResultRows", execution.maxResultRows());
        comma(json);
        field(json, "queryTimeoutMs", execution.queryTimeoutMs());
        json.append('}');
    }

    private static void tables(StringBuilder json, List<ExploreTableDto> tables) {
        name(json, "tables").append('[');
        for (int index = 0; index < tables.size(); index++) {
            if (index > 0) {
                comma(json);
            }
            table(json, tables.get(index));
        }
        json.append(']');
    }

    private static void table(StringBuilder json, ExploreTableDto table) {
        json.append('{');
        field(json, "id", table.id());
        comma(json);
        field(json, "name", table.name());
        comma(json);
        field(json, "title", table.title());
        optionalStringField(json, "description", table.description());
        comma(json);
        field(json, "parquetUrl", table.parquetUrl());
        optionalLongField(json, "sizeBytes", table.sizeBytes());
        optionalLongField(json, "rowCountEstimate", table.rowCountEstimate());
        comma(json);
        field(json, "primary", table.primary());
        comma(json);
        columns(json, table.columns());
        json.append('}');
    }

    private static void columns(StringBuilder json, List<ExploreColumnDto> columns) {
        name(json, "columns").append('[');
        for (int index = 0; index < columns.size(); index++) {
            if (index > 0) {
                comma(json);
            }
            column(json, columns.get(index));
        }
        json.append(']');
    }

    private static void column(StringBuilder json, ExploreColumnDto column) {
        json.append('{');
        field(json, "name", column.name());
        comma(json);
        field(json, "type", column.type());
        optionalBooleanField(json, "nullable", column.nullable());
        optionalBooleanField(json, "required", column.required());
        optionalStringField(json, "description", column.description());
        optionalStringField(json, "example", column.example());
        comma(json);
        name(json, "roles").append('[');
        for (int index = 0; index < column.roles().size(); index++) {
            if (index > 0) {
                comma(json);
            }
            string(json, column.roles().get(index).value());
        }
        json.append(']');
        json.append('}');
    }

    private static void recipes(StringBuilder json, List<ExploreRecipeDto> recipes) {
        name(json, "recipes").append('[');
        for (int index = 0; index < recipes.size(); index++) {
            if (index > 0) {
                comma(json);
            }
            recipe(json, recipes.get(index));
        }
        json.append(']');
    }

    private static void recipe(StringBuilder json, ExploreRecipeDto recipe) {
        json.append('{');
        field(json, "id", recipe.id());
        comma(json);
        field(json, "title", recipe.title());
        comma(json);
        field(json, "description", recipe.description());
        comma(json);
        field(json, "tableId", recipe.tableId());
        comma(json);
        field(json, "category", recipe.category().value());
        comma(json);
        field(json, "sql", recipe.sql());
        recipe.preferredChart().ifPresent(chart -> {
            comma(json);
            chart(json, chart);
        });
        json.append('}');
    }

    private static void chart(StringBuilder json, ExploreChartConfigDto chart) {
        name(json, "preferredChart").append('{');
        field(json, "type", chart.type().value());
        optionalStringField(json, "x", chart.x());
        optionalStringField(json, "y", chart.y());
        optionalStringField(json, "color", chart.color());
        optionalStringField(json, "title", chart.title());
        json.append('}');
    }

    private static void snippets(StringBuilder json, List<ExploreCodeSnippetDto> snippets) {
        name(json, "codeSnippets").append('[');
        for (int index = 0; index < snippets.size(); index++) {
            if (index > 0) {
                comma(json);
            }
            snippet(json, snippets.get(index));
        }
        json.append(']');
    }

    private static void snippet(StringBuilder json, ExploreCodeSnippetDto snippet) {
        json.append('{');
        field(json, "id", snippet.id());
        comma(json);
        field(json, "title", snippet.title());
        comma(json);
        field(json, "language", snippet.language().value());
        comma(json);
        field(json, "code", snippet.code());
        json.append('}');
    }

    private static void featureFlags(StringBuilder json, ExploreFeatureFlagsDto flags) {
        name(json, "featureFlags").append('{');
        field(json, "charts", flags.charts());
        comma(json);
        field(json, "localHistory", flags.localHistory());
        comma(json);
        field(json, "aiAssistant", flags.aiAssistant());
        comma(json);
        field(json, "webR", flags.webR());
        comma(json);
        field(json, "vega", flags.vega());
        comma(json);
        field(json, "mosaic", flags.mosaic());
        comma(json);
        field(json, "geospatial", flags.geospatial());
        json.append('}');
    }

    private static void optionalStringField(StringBuilder json, String fieldName, Optional<String> value) {
        value.ifPresent(text -> {
            comma(json);
            field(json, fieldName, text);
        });
    }

    private static void optionalLongField(StringBuilder json, String fieldName, Optional<Long> value) {
        value.ifPresent(number -> {
            comma(json);
            field(json, fieldName, number);
        });
    }

    private static void optionalBooleanField(StringBuilder json, String fieldName, Optional<Boolean> value) {
        value.ifPresent(bool -> {
            comma(json);
            field(json, fieldName, bool);
        });
    }

    private static void field(StringBuilder json, String fieldName, String value) {
        name(json, fieldName);
        string(json, value);
    }

    private static void field(StringBuilder json, String fieldName, int value) {
        name(json, fieldName).append(value);
    }

    private static void field(StringBuilder json, String fieldName, long value) {
        name(json, fieldName).append(value);
    }

    private static void field(StringBuilder json, String fieldName, boolean value) {
        name(json, fieldName).append(value);
    }

    private static StringBuilder name(StringBuilder json, String fieldName) {
        return string(json, fieldName).append(':');
    }

    private static StringBuilder string(StringBuilder json, String value) {
        json.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
                }
            }
        }
        return json.append('"');
    }

    private static void comma(StringBuilder json) {
        json.append(',');
    }
}
