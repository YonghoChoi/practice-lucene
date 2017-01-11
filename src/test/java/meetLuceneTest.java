import meetLucene.Indexer;
import meetLucene.Searcher;
import org.apache.lucene.queryParser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MeetLuceneTest {
  String indexDir = "src/test/index";   // 지정한 디렉토리에 색인 생성
  String dataDir = "src/test/data";    // 지정한 디렉토리에 담긴 txt 파일

  @Test
  public void test1_Indexer() throws IOException {
    long start = System.currentTimeMillis();
    Indexer indexer = new Indexer(indexDir);
    int numIndexed;

    try {
      numIndexed = indexer.index(dataDir, new TextFilesFilter());
    } finally {
      indexer.close();
    }

    long end = System.currentTimeMillis();
    // 색인 속도는 매우 중요. 하지만 한번 만들고 나면 계속 사용할 수 있으므로 검색 속도가 더 중요하다.
    System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " ms");
  }

  @Test
  public void test2_Searcher() throws IOException, ParseException {
    final String q = "Definitions";
    Searcher.search(indexDir, q);
  }

  private static class TextFilesFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
      // FileFilter를 사용하여 색인할 txt 파일만 추려냄
      return pathname.getName().toLowerCase().endsWith(".txt");
    }
  }
}
