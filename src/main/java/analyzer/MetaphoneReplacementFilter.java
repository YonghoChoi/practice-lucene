package analyzer;

import org.apache.commons.codec.language.Metaphone;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

public class MetaphoneReplacementFilter extends TokenFilter {
    public static final String METAHONE = "metaphone";

    private Metaphone metaphoner = new Metaphone();
    private TermAttribute termAttr;
    private TypeAttribute typeAttr;

    public MetaphoneReplacementFilter(TokenStream input) {
        super(input);
        termAttr = addAttribute(TermAttribute.class);
        typeAttr = addAttribute(TypeAttribute.class);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {  // 다음 토큰으로 이동
            return false;
        }

        String encoded;
        encoded = metaphoner.encode(termAttr.term());   // 메타폰 알고리즘으로 텀 변환
        termAttr.setTermBuffer(encoded);    // 변환된 텀을 토큰에 재설정
        typeAttr.setType(METAHONE); // 토큰 종류 지정
        return true;
    }
}
