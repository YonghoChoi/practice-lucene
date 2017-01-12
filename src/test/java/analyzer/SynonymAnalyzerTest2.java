package analyzer;

import analyzer.SynonymAnalyzer;
import analyzer.TestSynonymEngine;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.TestUtil;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SynonymAnalyzerTest2 {
    private IndexSearcher searcher;

    private static SynonymAnalyzer synonymAnalyzer = new SynonymAnalyzer(new TestSynonymEngine());

    @Before
    public void setUp() throws IOException {
        RAMDirectory directory = new RAMDirectory();

        IndexWriter writer = new IndexWriter(directory, synonymAnalyzer, IndexWriter.MaxFieldLength.UNLIMITED);
        Document doc = new Document();
        doc.add(new Field("content",
                "The quick brown fox jumps over the lazy dog",
                Field.Store.YES,
                Field.Index.ANALYZED
        ));
        writer.addDocument(doc);
        writer.close();

        searcher = new IndexSearcher(directory, true);
    }

    @After
    public void tearDown() throws IOException {
        searcher.close();
    }

    @Test
    public void testSearchByAPI() throws IOException {
        TermQuery tq = new TermQuery(new Term("content", "hops"));
        assertEquals(1, TestUtil.hitCount(searcher, tq));

        // PhraseQuery는 위치 정보를 활용해 원하는 텀이 일정 거리 안에 존재하는 문서를 찾아준다.
        // 텀 사이의 최대 거리인 slop은 기본 값으로 0.
        // SynonymFilter에서 PositionIncrementAttribute의 값을 0으로 지정해주었기 때문에 유사어 검색이 가능하다.
        PhraseQuery pq = new PhraseQuery();
        pq.add(new Term("content", "fox"));
        pq.add(new Term("content", "hops"));
        assertEquals(1, TestUtil.hitCount(searcher, pq));
    }

    @Test
    public void testWithQueryParser() throws IOException, ParseException {
        Query query = new QueryParser(Version.LUCENE_30, "content", synonymAnalyzer).parse("\"fox jumps\"");
        assertEquals(1, TestUtil.hitCount(searcher, query));
        System.out.println("With SynonymAnalyzer, \"fox jumps\" parses to " + query.toString("content"));

        query = new QueryParser(Version.LUCENE_30, "content", new StandardAnalyzer(Version.LUCENE_30)).parse("\"fox jumps\"");
        assertEquals(1, TestUtil.hitCount(searcher, query));
        System.out.println("With StandardAnalyzer, \"fox jumps\" parses to " + query.toString("content"));
    }
}
