import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.PriorityQueue;




public class Summarizer {
	private String PATH;
	private ArrayList<File> files;
	
	private Hashtable<String, Integer> FileSize; 
	private Hashtable<String, Pointers> unigram;
	
	public Summarizer(String path){
		
		this.PATH = path;
		//initialize variables
		FileSize = new Hashtable<String, Integer>();
		unigram = new Hashtable<String, Pointers>();
		
		//initialize the arrayList
		files = new ArrayList<File>();
		File dir = new File(PATH);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if(child.getName().contains("DS_Store")) continue;
				files.add(child);
			}
		} else {
		    System.out.println("Invalid Directory!");
		    System.exit(1);
		}
	}
	
	public void initializeGrams(){
		for(File file : files){
			String content;
			try {
				content = readFile(PATH+file.getName(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				System.out.println("ERROR Opening file " + file.getName());
				e.printStackTrace();
				return;
			}
			
			String[] uniTokens = getUniToken(content);
			
			summarize(uniTokens, 1, file);
			
			//store the file size;
			FileSize.put(file.getName(), uniTokens.length);
			
		}
	}
	
	public void rankOriginalCorpus(){
		RM rm = new RM(files, FileSize, unigram);
		
		rm.rankBM25();
		System.out.println("Finished Ranking BM25");
		
		rm.rankBM25_Derivants();
		System.out.println("Finished Ranking BM25_Derivants");
		
		rm.rankBM25_Synonym();
		System.out.println("Finished Ranking BM25_Synonym");
		
		rm.rankTFIDF();
		System.out.println("Finished Ranking TFIDF");
	}
	
	public void rankStoppedCorpus(){
		RM rm = new RM(files, FileSize, unigram);
		
		rm.rankStop();
		System.out.println("Finished Ranking Stop");
		
		rm.rankStopExtend();
		System.out.println("Finished Ranking StopExtend");
		
	}
	
	public void rankStemmedCorpus(){
		RM rm = new RM(files, FileSize, unigram);
		rm.rankStem();
		System.out.println("Finished Ranking Stem");
	}
	
	private String[] getUniToken(String content){
		return content.split("\\s+?");
	}

	private void summarize(String[] tokens, int count, File file){
		Hashtable<String, Pointers> current;
		switch(count){
			case 1:
				current = this.unigram;
				break;
			default:
				return;
				
		}
		Hashtable<String, Integer> wordCount = new Hashtable<String, Integer>();
		for(String token : tokens){
			if(token.equals("") || token.equals("\\s")) continue;
			if(wordCount.containsKey(token)) 
				wordCount.replace(token, wordCount.get(token)+1);
			else
				wordCount.put(token, 1);
		}
		for(Entry<String, Integer> entry : wordCount.entrySet()){
			if(!current.containsKey(entry.getKey()))
				current.put(entry.getKey(), new Pointers());
			current.get(entry.getKey()).addPointer(new Pointer(file.getName(), entry.getValue()));
		}
	}
	
	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	
}

class WordTF{
	public String word;
	public int tf;
	
	public WordTF(String word, int tf){
		this.word = word;
		this.tf = tf;
	}
}

class WordDF{
	public String word;
	public Pointers pointers;
	
	public WordDF(String word, Pointers p){
		this.word = word;
		this.pointers = p;
	}
}

class Pointers{
	public int total;
	public ArrayList<Pointer> pointers;
	
	public Pointers(){
		total = 0;
		pointers = new ArrayList<Pointer>();
	}
	
	public void addPointer(Pointer p){
		pointers.add(p);
		total += p.tf;
	}
}

class Pointer{
	public String docID;
	int tf;
	
	public Pointer(String doc, int tf){
		this.docID = doc;
		this.tf = tf;
	}
}

class WTFComparator implements Comparator<WordTF>
{
    public int compare(WordTF x, WordTF y)
    {
        return - x.tf + y.tf;
    }
}

class WDFComparator implements Comparator<WordDF>
{
    public int compare(WordDF x, WordDF y)
    {
        return x.word.compareTo(y.word);
    }
}

