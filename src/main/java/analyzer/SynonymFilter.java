package analyzer;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.IOException;
import java.util.Stack;

public class SynonymFilter extends TokenFilter {
    public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";

    private Stack<String> synonymStack; // 유사어를 담아 둘 버퍼.
    private SynonymEngine engine;
    private AttributeSource.State current;

    private final TermAttribute termAtt;
    private final PositionIncrementAttribute posIncrAtt;

    public SynonymFilter(TokenStream input, SynonymEngine engine) {
        super(input);
        this.engine = engine;
        this.synonymStack = new Stack<>();

        this.termAtt = addAttribute(TermAttribute.class);
        this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        // 버퍼에 유사어가 모두 소진될 때까지 버퍼에 담겨있는 유사어를 뽑아낸다.
        if (synonymStack.size() > 0) {
            String syn = synonymStack.pop();
            restoreState(current);  // 유사어의 경우에도 Term으로 인식 되므로 원본과 동일한 상태를 가지도록 restoreState를 해줌.
            termAtt.setTermBuffer(syn);
            posIncrAtt.setPositionIncrement(0); // 위치 증가값을 0으로 지정. PhraseQuery를 통해 질의를 할 때 slop의 기본 값이 0이기 때문.
            return true;
        }

        if (!input.incrementToken()) {  // 버퍼가 모두 소진되면 다음 토큰을 읽는다.
            return false;
        }

        if (addAliasesToStack()) {
            current = captureState();   // 현재 토큰에 대한 유사어가 있는 경우 현재 토큰의 상세 정보를 저장.
        }

        return true;
    }

    private boolean addAliasesToStack() throws IOException {
        String[] synonyms = engine.getSynonyms(termAtt.term()); // 유사어 추출
        if (synonyms == null) {
            return false;
        }

        for(String synonym : synonyms) {
            synonymStack.push(synonym);
        }
        return true;
    }
}
