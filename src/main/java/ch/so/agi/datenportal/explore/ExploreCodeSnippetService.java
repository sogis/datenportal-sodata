package ch.so.agi.datenportal.explore;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public final class ExploreCodeSnippetService {

    public List<ExploreCodeSnippetDto> generateSnippets(ExploreContextSource source, List<ExploreTableDto> tables) {
        if (tables.isEmpty()) {
            return List.of();
        }

        ExploreTableDto table = tables.getFirst();
        String url = table.parquetUrl();
        return List.of(
                new ExploreCodeSnippetDto(
                        "duckdb-cli",
                        "DuckDB CLI",
                        ExploreSnippetLanguage.SQL,
                        duckDbSnippet(url)),
                new ExploreCodeSnippetDto(
                        "python-duckdb",
                        "Python mit DuckDB",
                        ExploreSnippetLanguage.PYTHON,
                        pythonSnippet(url)),
                new ExploreCodeSnippetDto(
                        "r-duckdb",
                        "R mit duckdb",
                        ExploreSnippetLanguage.R,
                        rSnippet(url)));
    }

    private static String duckDbSnippet(String url) {
        return "install httpfs;\n"
                + "load httpfs;\n\n"
                + "select *\n"
                + "from read_parquet('" + sqlString(url) + "')\n"
                + "limit 100;";
    }

    private static String pythonSnippet(String url) {
        return "import duckdb\n\n"
                + "url = \"" + pythonString(url) + "\"\n\n"
                + "con = duckdb.connect()\n"
                + "df = con.sql(f\"\"\"\n"
                + "    select *\n"
                + "    from read_parquet('{url}')\n"
                + "    limit 100\n"
                + "\"\"\").df()\n\n"
                + "print(df)";
    }

    private static String rSnippet(String url) {
        return "library(DBI)\n"
                + "library(duckdb)\n\n"
                + "url <- \"" + rString(url) + "\"\n\n"
                + "con <- dbConnect(duckdb::duckdb())\n"
                + "daten <- dbGetQuery(con, sprintf(\"\n"
                + "  select *\n"
                + "  from read_parquet('%s')\n"
                + "  limit 100\n"
                + "\", url))\n\n"
                + "print(daten)";
    }

    private static String sqlString(String value) {
        return value.replace("'", "''");
    }

    private static String pythonString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String rString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
