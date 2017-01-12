package analyzer;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;

public class SynonymAnalyzer extends Analyzer {
    private SynonymEngine engine;

    public SynonymAnalyzer(SynonymEngine engine) {
        this.engine = engine;
    }

    @Override
    public final TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = new SynonymFilter(
                new StopFilter(true,
                        new LowerCaseFilter(
                                new StandardFilter(
                                        new StandardTokenizer(Version.LUCENE_30, reader)
                                )
                        ), StopAnalyzer.ENGLISH_STOP_WORDS_SET),
                engine
        );
        return result;
    }

    @Override
    public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
        return super.reusableTokenStream(fieldName, reader);
    }
}
