package util;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

public class TestUtil {
  public static int hitCount(IndexSearcher searcher, Query query) throws IOException {
    return searcher.search(query, 1).totalHits;
  }

  public static Directory getBookIndexDirectory() throws IOException {
    return FSDirectory.open(new File("src/test/index"));
  }
}
