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

    private Stack<String> synonymStack;
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
        if (synonymStack.size() > 0) {   // 버퍼에 담겨있는 유사어를 뽑아낸다.
            String syn = synonymStack.pop();
            restoreState(current);
            termAtt.setTermBuffer(syn);
            posIncrAtt.setPositionIncrement(0); // 위치 증가값을 0으로 지정
            return true;
        }

        if (!input.incrementToken()) {
            return false;
        }

        if (addAliasesToStack()) {
            current = captureState();
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
