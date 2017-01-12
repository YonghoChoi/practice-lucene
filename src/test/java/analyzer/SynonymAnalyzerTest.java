package analyzer;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.junit.Test;
import util.AnalyzerUtils;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class SynonymAnalyzerTest {
    // 유사어 검색 테스트
    @Test
    public void testJumps() throws IOException {
        SynonymAnalyzer synonyAnalyzer = new SynonymAnalyzer(new TestSynonymEngine());
        TokenStream stream = synonyAnalyzer.tokenStream("contents",
                new StringReader("jumps"));
        TermAttribute term = stream.addAttribute(TermAttribute.class);
        PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);

        int i = 0;
        String[] expected = new String[]{
                "jumps",
                "hops",
                "leaps"
        };

        while (stream.incrementToken()) {
            assertEquals(expected[i], term.term()); // 유사어가 제대로 들어가있는지 확인.

            // 유사어의 위치 확인.
            // 위치 증가 값이 모두 0이어야 한다.
            int expectedPos;
            if (i == 0) {
                expectedPos = 1;
            } else {
                expectedPos = 0;
            }

            assertEquals(expectedPos, posIncr.getPositionIncrement());
            i++;
        }

        assertEquals(3, i);
    }

    // 유사어 토큰 확인
    @Test
    public void testSynonymAnalyzerView() throws IOException {
        SynonymEngine engine = new TestSynonymEngine();

        // 유사어의 위치 증가값(PositionIncrement)이 0으로 설정되어 있기 때문에 아래와 같은 형식으로 출력됨.
        // ex) 2: [quick] [speedy] [fast]
        AnalyzerUtils.displayTokensWithPositions(new SynonymAnalyzer(engine),
                "The quick brown fox jumps over the lazy dog");
    }
}
