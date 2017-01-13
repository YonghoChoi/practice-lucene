package analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;

public class ChineseDemo {
    private static String[] strings = {"崔龍扈", "최용호"};

    private static Analyzer[] analyzers = {
            new SimpleAnalyzer(),
            new StandardAnalyzer(Version.LUCENE_30),
            new ChineseAnalyzer(),
            new CJKAnalyzer(Version.LUCENE_30)  // 한 글자씩 이동하며 현재 위치의 두 글자를 하나의 토큰으로 생성.(중국어의 경우 두글자로 구성되는 단어가 많기 때문)
    };

    public static void main(String[] args) throws IOException {
        for(String string : strings) {
            for(Analyzer analyzer : analyzers) {
                analyzer(string, analyzer);
            }
        }
    }

    private static void analyzer(String string, Analyzer analyzer) throws IOException {
        StringBuffer buffer = new StringBuffer();
        TokenStream stream = analyzer.tokenStream("contents",
                    new StringReader(string));
        TermAttribute term = stream.addAttribute(TermAttribute.class);
        while(stream.incrementToken()) {
            buffer.append("[");
            buffer.append(term.term());
            buffer.append("] ");
        }

//        printToFrame(analyzer.getClass().getSimpleName() + " : " + string,
//                buffer.toString());

        printToConsole(buffer.toString());

    }

    private static void printToFrame(String title, String output) {
        Frame f = new Frame();
        f.setTitle(title);
        f.setResizable(true);

        Font font = new Font(null, Font.PLAIN, 36);
        int width = getWidth(f.getFontMetrics(font), output);

        f.setSize(width < 250 ? 250 : width + 50, 75);

        // 참고 : Label 클래스로 화면에 중국어 글자가 제대로 나타나지 않는다면
        // Label 클래스 대신 javax.swing.JLable 클래스 사용
        Label label = new Label(output);
        label.setSize(width, 75);
        label.setAlignment(Label.CENTER);
        label.setFont(font);
        f.add(label);
        f.setVisible(true);

    }

    private static void printToConsole(String output) {
        System.out.println(output);
    }

    private static int getWidth(FontMetrics metrics, String s) {
        int size = 0;
        int length = s.length();
        for(int i = 0; i < length; i++) {
            size += metrics.charWidth(s.charAt(i));
        }
        return size;
    }
}
