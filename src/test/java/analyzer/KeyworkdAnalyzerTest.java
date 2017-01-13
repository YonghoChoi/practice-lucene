package analyzer;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;
import util.TestUtil;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

// 분석하지 않은 필드 검색 테스트
public class KeyworkdAnalyzerTest {
    private IndexSearcher searcher;

    @Before
    public void setUp() throws IOException {
        Directory directory = new RAMDirectory();

        IndexWriter writer = new IndexWriter(directory,
                new SimpleAnalyzer(),
                IndexWriter.MaxFieldLength.UNLIMITED);
        Document doc = new Document();
        doc.add(new Field("partnum",
                "Q36",
                Field.Store.NO,
                Field.Index.NOT_ANALYZED_NO_NORMS));
        doc.add(new Field("description",
                "Illidium Space Modulator",
                Field.Store.YES,
                Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        searcher = new IndexSearcher(directory);
    }

    @Test
    public void testTermQuery() throws IOException {
        Query query = new TermQuery(new Term("partnum", "Q36"));
        assertEquals(1, TestUtil.hitCount(searcher, query));
    }

    @Test
    public void testBasicQueryParser() throws ParseException, IOException {
        Query query = new QueryParser(Version.LUCENE_30,
                "description",
                new SimpleAnalyzer()).parse("partnum:Q36 AND SPACE");
        // SimpleAnalyzer를 거치면 Q36은 q로 파싱됨.
        // 하지만 색인 시 partnum은 분석기를 사용하지 않았으므로 Q36 그대로 색인되는데,
        // 검색 시에 이를 분석하여 검색을 시도하므로 찾지 못하는 문제가 발생한다.
        assertEquals("note Q36 -> q", "+partnum:q +space", query.toString("description"));
        assertEquals("doc not found : (", 0, TestUtil.hitCount(searcher, query));
    }

    @Test
    public void testPerFieldAnalyzer() throws ParseException, IOException {
        // 위 testBasicQueryParser에서 문제된 부분을 PerFieldAnalyzerWrapper를 사용하여 해결.
        // 필드별 분석기 설정.
        // 여기서는 partnum 필드를 KeywordAnalyzer로 분석하도록 하여 Q36으로 동일하게 검색됨.
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new SimpleAnalyzer());
        analyzer.addAnalyzer("partnum", new KeywordAnalyzer());

        Query query = new QueryParser(Version.LUCENE_30,
                "description", analyzer).parse("partnum:Q36 AND SPACE");

        assertEquals("Q36 kept as-is", "+partnum:Q36 +space", query.toString("description"));
        assertEquals("doc found!", 1, TestUtil.hitCount(searcher, query));
    }
}
