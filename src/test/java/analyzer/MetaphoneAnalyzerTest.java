package analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import util.AnalyzerUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

// 유사 발음 검색 테스트
public class MetaphoneAnalyzerTest {
    // Apache Commons Codec에 포함되어 있는 Metaphone 알고리즘을 통해 유사 발음 검색 테스트
    @Test
    public void testKoolKat() throws IOException, ParseException {
        RAMDirectory directory = new RAMDirectory();
        Analyzer analyzer = new MetaphoneReplacementAnalyzer();
        IndexWriter writer = new IndexWriter(directory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);

        Document doc = new Document();
        doc.add(new Field("contents",
                "cool cat",
                Field.Store.YES,
                Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(directory);

        // 검색어로 원문에 포함되어 있지 않은 kool kat을 전달했는데 cool cat을 찾아낸다. (유사 발음 검색)
        Query query = new QueryParser(Version.LUCENE_30,
                "contents", analyzer).parse("kool kat");    // 질의어 파싱

        TopDocs hits = searcher.search(query, 1);
        assertEquals(1, hits.totalHits);    // 검색 결과 확인
        int docID = hits.scoreDocs[0].doc;
        assertEquals("cool cat", doc.get("contents"));  // 원문 텍스트 확인
        searcher.close();
    }

    @Test
    public void testMetaphoneAnalyzer() throws IOException {
        MetaphoneReplacementAnalyzer analyzer = new MetaphoneReplacementAnalyzer();
        AnalyzerUtils.displayTokens(analyzer, "The quick brown phonx jumped over the lazy dag");
        System.out.println();
        // 오타가 존재하지만 위와 결과가 동일.
        AnalyzerUtils.displayTokens(analyzer, "The quick brown phonx jumpd ovvar tha lazi dag");
    }
}
