package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ExploreSqlNameSanitizerTest {

    private final ExploreSqlNameSanitizer sanitizer = new ExploreSqlNameSanitizer();

    @Test
    void normalizesRawNamesToSafeTableNames() {
        assertThat(sanitizer.toSafeTableName("Gemeindegrenzen")).isEqualTo("gemeindegrenzen");
        assertThat(sanitizer.toSafeTableName("2024 Statistik")).isEqualTo("t_2024_statistik");
        assertThat(sanitizer.toSafeTableName("select")).isEqualTo("select_table");
        assertThat(sanitizer.toSafeTableName("  A---B___C  ")).isEqualTo("a_b_c");
    }

    @Test
    void quotesIdentifiers() {
        assertThat(sanitizer.quoteIdentifier("name")).isEqualTo("\"name\"");
        assertThat(sanitizer.quoteIdentifier("a\"b")).isEqualTo("\"a\"\"b\"");
    }

    @Test
    void rejectsUnsafeNames() {
        assertThatThrownBy(() -> sanitizer.assertSafeTableName("2024"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsafe SQL table name");
        assertThatThrownBy(() -> sanitizer.assertSafeTableName("drop table"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsafe SQL table name");
    }
}
