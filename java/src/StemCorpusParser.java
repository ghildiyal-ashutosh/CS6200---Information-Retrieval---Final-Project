import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


public class StemCorpusParser {
	private static String SRC = "cacm_stem.txt";
	private static String DST = "stemmedCorpus/";
	
	public StemCorpusParser(){
	}
	
	public void parse(){
		String content;
		try {
			content = readFile(SRC, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		String[] contents = content.split("#\\s(\\d+)");
		System.out.println("Size: " + contents.length);
		int i=0; 
		for(String s : contents){
			File file = new File(DST + i + ".txt");
			PrintWriter pw;
			try {
				pw = new PrintWriter(file);
				pw.println(s);
				pw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			i++;
		}
	}
	
	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
