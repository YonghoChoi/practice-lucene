package meetLucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

public class Searcher {
  public static void search(String indexDir, String q) throws IOException, ParseException {
    Directory dir = FSDirectory.open(new File(indexDir));
    IndexSearcher is = new IndexSearcher(dir);  // 색인 열기

    QueryParser parser = new QueryParser(
            Version.LUCENE_30,  // 하위 호환성을 위해 버전 지정.
            "contents", // 검색할 필드명.
            new StandardAnalyzer(Version.LUCENE_30) // 분석기로 StandardAnalyzer 사용.
    );

    Query query = parser.parse(q);  // 질의 분석

    long start = System.currentTimeMillis();

    // 색인의 내용을 검색하여 결과를 TopDocs 객체로 받아온다.
    // TocDocs 객체에는 실제 결과 문서에 대한 참조만 들어있다.
    // 필요할 때 IndexSearcher.doc(int) 메소드를 호출하면 실제 Document 객체를 불러온다.
    TopDocs hits = is.search(query, 10);

    long end = System.currentTimeMillis();

    System.err.println("Found " + hits.totalHits + " document(s) (in "
            + (end - start) + "ms) that matched query '" + q + "':");

    for(ScoreDoc scoreDoc : hits.scoreDocs) {
      Document doc = is.doc(scoreDoc.doc);  // 결과 문서 확보
      System.out.println(doc.get("fullpath"));
    }

    is.close();
  }
}
