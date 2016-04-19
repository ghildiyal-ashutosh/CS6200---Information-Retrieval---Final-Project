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
		
		Summarizer s = new Summarizer();
		s.initializeGrams();
		s.rank();
		
		/*double socre1 = getScorePerDoc(500000, 40000, 1, 15, getKForDoc(0));
		double socre2 = getScorePerDoc(500000, 300, 1, 25, getKForDoc(0));
		System.out.println("Score1: " + socre1 );
		System.out.println("Score2: " + socre2 );
		System.out.println("Score: "  + (socre1+socre2));*/
	}
	
	private static double getKForDoc(int dl){
		return 1.2 * (1 - 0.75 + 0.75 * 0.9);
	}
	
	private static double getScorePerDoc(int N, int n, int qf, int f, double K){
		double score = 0;
		score = Math.log((0.5/0.5)/((n+0.5)/(N-n+0.5))) * (1.2+1)*f/(K+f) * (100+1)*qf/(100+qf);
		return score;
	}
}
