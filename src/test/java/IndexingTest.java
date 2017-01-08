import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import util.TestUtil;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IndexingTest {
  String[] ids = {"1", "2"};
  String[] unindexed = {"Netherlands", "Italy"};
  String[] unstored = {"Amsterdam has lots of bridges", "Venice has lots of canals"};
  String[] text = {"Amsterdam", "Venice"};
  Directory directory;

  @Before
  public void setUp() throws IOException {
    directory = new RAMDirectory();

    IndexWriter writer = getWriter();

    for (int i = 0; i < ids.length; i++) {
      Document doc = new Document();
      doc.add(new Field("id", ids[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("country", unindexed[i], Field.Store.YES, Field.Index.NO));
      doc.add(new Field("contents", unstored[i], Field.Store.NO, Field.Index.ANALYZED));
      doc.add(new Field("city", text[i], Field.Store.YES, Field.Index.ANALYZED));
      writer.addDocument(doc);
    }

    writer.close(); // 닫을 때 변경 사항을 저장공간에 반영.
  }

  private IndexWriter getWriter() throws IOException {
    // 인자로 create 여부를 전달하지 않는 경우 지정된 Directory안에 색인이 없는 경우 생성, 있으면 그대로 열어서 사용.
    return new IndexWriter(directory,
            new WhitespaceAnalyzer(), // white space 단위로 토큰 생성.
            IndexWriter.MaxFieldLength.UNLIMITED  // 모든 텍스트를 색인하도록 필드 길이에 UNLIMITED 지정.
    );
  }

  private int getHitCount(String fieldName, String searchString) throws IOException {
    IndexSearcher searcher = new IndexSearcher(directory);
    Term t = new Term(fieldName, searchString);
    Query query = new TermQuery(t);
    int hitCount = TestUtil.hitCount(searcher, query);
    searcher.close();
    return hitCount;
  }

  @Test
  public void testIndexWriter() throws IOException {
    IndexWriter writer = getWriter();
    assertTrue(ids.length == writer.numDocs());
    writer.close();
  }

  @Test
  public void testIndexReader() throws IOException {
    IndexReader reader = IndexReader.open(directory);
    assertTrue(ids.length == reader.maxDoc());
    assertTrue(ids.length == reader.numDocs());
    reader.close();
  }

  @Test
  public void testDeleteBeforeOptimize() throws IOException {
    IndexWriter writer = getWriter();

    IndexReader reader = IndexReader.open(writer.getDirectory());

    Map<String, String> data = reader.getCommitUserData();
    for (String key : data.keySet()) {
      System.out.println(data.get(key));
    }

    assertTrue(2 == writer.numDocs());  // 색인에 두 개의 문서가 들어있는지 확인
    writer.deleteDocuments(new Term("id", "1"));  // Term 객체를 이용해 삭제하는 경우 모든 문서에 특정 이름의 Field를 추가하고 유일해야 한다.
    writer.commit();  // commit이나 close 시에 삭제된 내용이 반영되지만 실제 문서가 제거된 것은 아니고, 삭제되었다고 표시만 해둔다.
    assertTrue(writer.hasDeletions());  // 삭제된 문서가 있는지 검증
    assertTrue(2 == writer.maxDoc());   // maxDoc은 삭제한 문서까지 포함.
    assertTrue(1 == writer.numDocs());  // numDocs는 삭제되지 않은 문서만.
    writer.close();
  }

  @Test
  public void testDeleteAfterOptimize() throws IOException {
    IndexWriter writer = getWriter();
    assertTrue(2 == writer.numDocs());  // 색인에 두 개의 문서가 들어있는지 확인
    writer.deleteDocuments(new Term("id", "1"));
    writer.optimize();  // 강제로 최적화 작업 수행
    writer.commit();
    assertFalse(writer.hasDeletions());
    assertTrue(1 == writer.maxDoc()); // 삭제 처리되어 maxDoc에도 삭제된 문서가 카운팅되지 않음.
    assertTrue(1 == writer.numDocs());
    writer.close();
  }

  @Test
  public void testUpdate() throws IOException {
    assertTrue(1 == getHitCount("city", "Amsterdam"));

    IndexWriter writer = getWriter();

    Document doc = new Document();
    doc.add(new Field("id", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));
    doc.add(new Field("country", "Netherlands", Field.Store.YES, Field.Index.NO));
    doc.add(new Field("contents", "Den Haag has a lot of museums", Field.Store.NO, Field.Index.ANALYZED));
    doc.add(new Field("city", "Den Haag", Field.Store.YES, Field.Index.ANALYZED));

    // updateDocument() = deleteDocument() + addDocument()
    writer.updateDocument(new Term("id", "1"), doc);

    writer.close();

    assertTrue(0 == getHitCount("city", "Amsterdam"));
    assertTrue(1 == getHitCount("city", "Haag"));
  }
}
