import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import util.TestUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BasicSearchingTest {
    @Test
    public void testTerm() throws IOException {
        Directory dir = TestUtil.getBookIndexDirectory();

        // IndexSearcher를 생성할 때 색인 정보를 읽어와서 내부적으로 검색에
        // 필요한 기본 자료구조를 구성해야 하기 때문에 검색 질의가 들어올때마다
        // IndexSearcher를 열면 성능이 크게 떨어진다.
        IndexSearcher searcher = new IndexSearcher(dir);

        Term t = new Term("contents", "ant");
        Query query = new TermQuery(t);
        TopDocs docs = searcher.search(new TermQuery(t), 10);
        assertEquals("Ant in Action", 1, docs.totalHits);

        t = new Term("contents", "in Action");
        docs = searcher.search(new TermQuery(t), 10);
        System.out.println(searcher.toString());
        assertEquals("Ant in Action, JUnit in Action, Second Edition", 2, docs.totalHits);

        searcher.close();
        dir.close();
    }

    @Test
    public void testQueryParser() throws ParseException, IOException {
        Directory dir = TestUtil.getBookIndexDirectory();
        IndexSearcher searcher = new IndexSearcher(dir);

        QueryParser parser = new QueryParser(Version.LUCENE_30,
                                                "contents",
                                                    new SimpleAnalyzer());

        Query query = parser.parse("+JUNIT +ANT -MOCK");
        TopDocs docs = searcher.search(query, 10);
        assertEquals(1, docs.totalHits);

        Document d = searcher.doc(docs.scoreDocs[0].doc);
        assertEquals("Ant in Action", d.get("title"));

        query = parser.parse("mock OR junit");
        docs = searcher.search(query, 10);
        assertEquals("Ant in Action, JUnit in Action, Second Edition", 2, docs.totalHits);

        searcher.close();
        dir.close();
    }
}
