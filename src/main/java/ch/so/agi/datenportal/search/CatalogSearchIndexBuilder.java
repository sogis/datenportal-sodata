package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CatalogSearchIndexBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogSearchIndexBuilder.class);

    private final CatalogDocumentMapper documentMapper;

    public CatalogSearchIndexBuilder(CatalogDocumentMapper documentMapper) {
        this.documentMapper = Objects.requireNonNull(documentMapper, "documentMapper must not be null");
    }

    public CatalogSearchIndex build(List<CatalogEntry> visibleEntries) {
        Objects.requireNonNull(visibleEntries, "visibleEntries must not be null");
        if (visibleEntries.isEmpty()) {
            return CatalogSearchIndex.empty();
        }

        var started = System.nanoTime();
        Directory directory = new ByteBuffersDirectory();
        Analyzer analyzer = new GermanAnalyzer();

        try {
            try (var writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
                for (CatalogEntry entry : visibleEntries) {
                    writer.addDocument(documentMapper.toDocument(entry));
                }
                writer.commit();
            }

            var reader = DirectoryReader.open(directory);
            var durationMillis = (System.nanoTime() - started) / 1_000_000;
            LOGGER.info("Built Lucene catalog index with {} entries in {} ms.", visibleEntries.size(), durationMillis);
            return new LuceneCatalogSearchIndex(directory, analyzer, reader);
        } catch (IOException | RuntimeException ex) {
            closeQuietly(analyzer, directory);
            LOGGER.warn("Could not build Lucene catalog index.", ex);
            throw new CatalogSearchIndexBuildException("Could not build Lucene catalog index.", ex);
        }
    }

    private static void closeQuietly(Analyzer analyzer, Directory directory) {
        analyzer.close();
        try {
            directory.close();
        } catch (IOException ignored) {
            // Best-effort cleanup after failed index construction.
        }
    }
}
