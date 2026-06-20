package ch.so.agi.datenportal.search;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LuceneCatalogSearchIndex implements CatalogSearchIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneCatalogSearchIndex.class);

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
        var tokens = CatalogSearchFields.tokenizeNormalized(userQuery);
        if (normalized.isBlank() || tokens.isEmpty()) {
            return new MatchNoDocsQuery("query has no searchable terms");
        }

        var query = new BooleanQuery.Builder();
        query.add(boostedTermQuery(CatalogSearchFields.IDENTIFIER_EXACT, normalized, 200.0f), BooleanClause.Occur.SHOULD);
        query.add(boostedTermQuery(CatalogSearchFields.TITLE_EXACT, normalized, 120.0f), BooleanClause.Occur.SHOULD);
        query.add(new BoostQuery(new PrefixQuery(new Term(CatalogSearchFields.TITLE_EXACT, normalized)), 80.0f), BooleanClause.Occur.SHOULD);

        for (String token : tokens) {
            query.add(tokenQuery(token), BooleanClause.Occur.MUST);
        }

        return query.build();
    }

    private Query tokenQuery(String token) {
        var tokenQuery = new BooleanQuery.Builder();
        tokenQuery.setMinimumNumberShouldMatch(1);

        tokenQuery.add(boostedSubstringQuery(CatalogSearchFields.IDENTIFIER_SUBSTRING, token, 100.0f), BooleanClause.Occur.SHOULD);
        tokenQuery.add(boostedSubstringQuery(CatalogSearchFields.TITLE_SUBSTRING, token, 24.0f), BooleanClause.Occur.SHOULD);
        tokenQuery.add(boostedSubstringQuery(CatalogSearchFields.KEYWORD_SUBSTRING, token, 16.0f), BooleanClause.Occur.SHOULD);
        tokenQuery.add(boostedSubstringQuery(CatalogSearchFields.ISSUE_IDENTIFIER_SUBSTRING, token, 12.0f), BooleanClause.Occur.SHOULD);
        tokenQuery.add(boostedSubstringQuery(CatalogSearchFields.ISSUE_TITLE_SUBSTRING, token, 10.0f), BooleanClause.Occur.SHOULD);
        tokenQuery.add(boostedSubstringQuery(CatalogSearchFields.ISSUE_LABEL_SUBSTRING, token, 10.0f), BooleanClause.Occur.SHOULD);
        tokenQuery.add(boostedSubstringQuery(CatalogSearchFields.ISSUE_KEYWORD_SUBSTRING, token, 8.0f), BooleanClause.Occur.SHOULD);

        tokenQuery.add(boostedTermQuery(CatalogSearchFields.THEME_TERMS, token, 6.0f), BooleanClause.Occur.SHOULD);
        tokenQuery.add(boostedTermQuery(CatalogSearchFields.OFFICE_TERMS, token, 6.0f), BooleanClause.Occur.SHOULD);
        tokenQuery.add(boostedTermQuery(CatalogSearchFields.FORMAT_TERMS, token, 4.0f), BooleanClause.Occur.SHOULD);
        tokenQuery.add(boostedTermQuery(CatalogSearchFields.DESCRIPTION_TERMS, token, 2.5f), BooleanClause.Occur.SHOULD);
        tokenQuery.add(boostedTermQuery(CatalogSearchFields.ISSUE_DESCRIPTION_TERMS, token, 2.0f), BooleanClause.Occur.SHOULD);

        return tokenQuery.build();
    }

    private Query boostedSubstringQuery(String field, String token, float boost) {
        return new BoostQuery(new WildcardQuery(new Term(field, "*" + token + "*")), boost);
    }

    private Query boostedTermQuery(String field, String token, float boost) {
        return new BoostQuery(new TermQuery(new Term(field, token)), boost);
    }
}
