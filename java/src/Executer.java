import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Executer {
	public static void main(String[] args){
		/*FileParser fp = new FileParser();
		try {
			fp.parseHtml();
			fp.parseFileName();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		
		/*FileParser fp1 = new FileParser();
		fp1.tokenize();
		*/
		/*StemCorpusParser scp = new StemCorpusParser();
		scp.parse();
		*/
		
		Summarizer s1 = new Summarizer("originalCorpus/");
		s1.initializeGrams();
		s1.rankOriginalCorpus();
		Summarizer s2 = new Summarizer("stoppedCorpus/");
		s2.initializeGrams();
		s2.rankStoppedCorpus();
		Summarizer s3 = new Summarizer("stemmedCorpus/");
		s3.initializeGrams();
		s3.rankStemmedCorpus();
	}
}
