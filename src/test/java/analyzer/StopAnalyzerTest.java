package analyzer;

import org.junit.Test;
import util.AnalyzerUtils;

import java.io.IOException;

public class StopAnalyzerTest {
    @Test
    public void testStopAnalyzer2() throws IOException {
        AnalyzerUtils.assertAnalyzesTo(new StopAnalyzer2(),
                "The quick brown...",
                new String[]{"quick", "brown"});
    }
}