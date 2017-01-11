package analyzer;

import org.apache.lucene.analysis.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

public class StopAnalyzer2 extends Analyzer {
    private final Set stopWords;

    public StopAnalyzer2() {
        stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
    }

    public StopAnalyzer2(String[] stopWords) {
        this.stopWords = StopFilter.makeStopSet(stopWords);
    }

//    @Override
//    public final TokenStream tokenStream(String fieldName, Reader reader) {
//        return new StopFilter(true,
//                new LowerCaseFilter(
//                    new LetterTokenizer(reader)),
//                stopWords);
//    }

    @Override
    public final TokenStream tokenStream(String fieldName, Reader reader) {
        return new LowerCaseFilter(
                new StopFilter(true,
                        new LetterTokenizer(reader),
                        stopWords));
    }

    @Override
    public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
        return super.reusableTokenStream(fieldName, reader);
    }
}
