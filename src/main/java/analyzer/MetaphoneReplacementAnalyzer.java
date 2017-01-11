package analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LetterTokenizer;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;

public class MetaphoneReplacementAnalyzer extends Analyzer {
    @Override
    public final TokenStream tokenStream(String fieldName, Reader reader) {
        return new MetaphoneReplacementFilter(
                // 메타폰 알고리즘 특성상 토큰으로 글자만 인식하기 때문에 LetterTokenizer 사용
                // 소문자 변환은 메타폰 알고리즘을 통해 변환됨.
                new LetterTokenizer(reader)
        );
    }

    @Override
    public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
        return super.reusableTokenStream(fieldName, reader);
    }
}
