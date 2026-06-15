package ch.so.agi.datenportal.search;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LuceneCatalogSearchIndex implements CatalogSearchIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneCatalogSearchIndex.class);

    private static final String[] QUERY_FIELDS = {
        CatalogSearchFields.IDENTIFIER_TEXT,
        CatalogSearchFields.TITLE,
        CatalogSearchFields.KEYWORDS,
        CatalogSearchFields.ISSUE_TEXT,
        CatalogSearchFields.THEME_TEXT,
        CatalogSearchFields.OFFICE_TEXT,
        CatalogSearchFields.FORMATS,
        CatalogSearchFields.DESCRIPTION,
        CatalogSearchFields.ALL_TEXT
    };

    private static final Map<String, Float> FIELD_BOOSTS = Map.of(
            CatalogSearchFields.IDENTIFIER_TEXT, 8.0f,
            CatalogSearchFields.TITLE, 6.0f,
            CatalogSearchFields.KEYWORDS, 4.0f,
            CatalogSearchFields.ISSUE_TEXT, 3.0f,
            CatalogSearchFields.THEME_TEXT, 2.0f,
            CatalogSearchFields.OFFICE_TEXT, 2.0f,
            CatalogSearchFields.FORMATS, 1.5f,
            CatalogSearchFields.DESCRIPTION, 1.0f,
            CatalogSearchFields.ALL_TEXT, 0.5f);

    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexReader reader;
    private final IndexSearcher searcher;
    private final AtomicBoolean closed = new AtomicBoolean();

    LuceneCatalogSearchIndex(Directory directory, Analyzer analyzer, IndexReader reader) {
        this.directory = Objects.requireNonNull(directory, "directory must not be null");
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer must not be null");
        this.reader = Objects.requireNonNull(reader, "reader must not be null");
        this.searcher = new IndexSearcher(reader);
    }

    @Override
    public List<SearchHit> search(String userQuery, int maxResults) {
        if (closed.get() || maxResults <= 0 || userQuery == null || userQuery.isBlank()) {
            return List.of();
        }

        try {
            var query = buildQuery(userQuery);
            var topDocs = searcher.search(query, maxResults);
            var seen = new LinkedHashSet<String>();
            var hits = new java.util.ArrayList<SearchHit>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                var document = searcher.storedFields().document(scoreDoc.doc);
                var entryId = document.get(CatalogSearchFields.ENTRY_ID);
                if (entryId != null && seen.add(entryId)) {
                    hits.add(SearchHit.withoutHighlight(entryId, scoreDoc.score));
                }
            }
            return List.copyOf(hits);
        } catch (IOException | RuntimeException ex) {
            LOGGER.warn("Lucene catalog search failed; returning no text-search results.", ex);
            return List.of();
        }
    }

    @Override
    public boolean isEmpty() {
        return reader.numDocs() == 0;
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        try {
            reader.close();
        } catch (IOException ex) {
            LOGGER.warn("Could not close Lucene index reader.", ex);
        }
        analyzer.close();
        try {
            directory.close();
        } catch (IOException ex) {
            LOGGER.warn("Could not close Lucene index directory.", ex);
        }
    }

    private Query buildQuery(String userQuery) {
        var normalized = CatalogSearchFields.normalizeExact(userQuery);
        if (normalized.isBlank()) {
            return new MatchNoDocsQuery("query has no searchable terms");
        }

        var query = new BooleanQuery.Builder()
                .add(new BoostQuery(new TermQuery(new Term(CatalogSearchFields.IDENTIFIER_EXACT, normalized)), 20.0f), BooleanClause.Occur.SHOULD)
                .add(new BoostQuery(new TermQuery(new Term(CatalogSearchFields.TITLE_EXACT, normalized)), 12.0f), BooleanClause.Occur.SHOULD)
                .add(new BoostQuery(new PrefixQuery(new Term(CatalogSearchFields.TITLE_EXACT, normalized)), 10.0f), BooleanClause.Occur.SHOULD);

        try {
            query.add(parserQuery(userQuery), BooleanClause.Occur.SHOULD);
        } catch (ParseException ex) {
            LOGGER.debug("Lucene query parser rejected user input; using fallback query. cause={}", ex.getClass().getSimpleName());
            query.add(fallbackQuery(userQuery), BooleanClause.Occur.SHOULD);
        }

        return query.build();
    }

    private Query parserQuery(String userQuery) throws ParseException {
        var parser = new MultiFieldQueryParser(QUERY_FIELDS, analyzer, FIELD_BOOSTS);
        parser.setDefaultOperator(QueryParser.Operator.AND);
        return parser.parse(QueryParser.escape(userQuery));
    }

    private Query fallbackQuery(String userQuery) {
        try {
            var parser = new QueryParser(CatalogSearchFields.ALL_TEXT, analyzer);
            parser.setDefaultOperator(QueryParser.Operator.AND);
            return parser.parse(QueryParser.escape(userQuery));
        } catch (ParseException ex) {
            LOGGER.debug("Lucene fallback query parser rejected user input. cause={}", ex.getClass().getSimpleName());
            return new MatchNoDocsQuery("fallback query could not be parsed");
        }
    }
}
