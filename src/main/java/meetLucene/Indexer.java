package meetLucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;

public class Indexer {
  private IndexWriter writer;

  public Indexer(String indexDir) throws IOException {
    Directory dir = FSDirectory.open(new File(indexDir));
    writer = new IndexWriter(dir,   // 색인을 저장할 Directory 인스턴스. 여기서는 FSDirectory이므로 파일 저장.
            new StandardAnalyzer(Version.LUCENE_30),  // 하위 호환성을 위해 버전 지정. 버그 조차 유지되어 있으므로 주의.
            true,
            IndexWriter.MaxFieldLength.UNLIMITED);
  }

  public void close() throws IOException {
    writer.close();
  }

  public int index(String dataDir, FileFilter filter) throws IOException {
    File[] files = new File(dataDir).listFiles();
    for (File f : files) {
      if (!f.isDirectory() &&
              !f.isHidden() &&
              f.exists() &&
              f.canRead() &&
              (filter == null || filter.accept(f))) {
        indexFile(f);
      }
    }

    return writer.numDocs();  // 색인 된 문서 건 수 리턴
  }

  protected Document getDocument(File f) throws IOException {
    Document doc = new Document();
    doc.add(new Field("contents", new FileReader(f)));
    doc.add(new Field("filename", f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    doc.add(new Field("fullpath", f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    return doc;
  }

  private void indexFile(File f) throws IOException {
    System.out.println("Indexing " + f.getCanonicalPath());
    Document doc = getDocument(f);
    writer.addDocument(doc);
  }
}
