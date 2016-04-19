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
	//static private String PATH = "originalCorpus/";
	//static private String PATH = "stoppedCorpus/";
	static private String PATH = "stemmedCorpus/";
	private ArrayList<File> files;
	
	private Hashtable<String, Integer> FileSize; 
	private Hashtable<String, Pointers> unigram;
	
	public Summarizer(){
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
			
			/*
			int totalSpace = 0;
			for(String s : uniTokens){
				if(s.equals("") || s.equals("\\s"))
					totalSpace++;
			}
			if(totalSpace != 0)
				System.out.println("Total Spaces are " + totalSpace);
			*/
			
			summarize(uniTokens, 1, file);
			
			//store the file size;
			FileSize.put(file.getName(), uniTokens.length);
			
		}
		Comparator<WordTF> wtfComparator = new WTFComparator();
		PriorityQueue<WordTF> unigram1 = new PriorityQueue<WordTF>(1,wtfComparator);
		
		Comparator<WordDF> wdfComparator = new WDFComparator();
		PriorityQueue<WordDF> unigram2 = new PriorityQueue<WordDF>(1,wdfComparator);
		
		//check unigram
		for(Entry<String, Pointers> entry : unigram.entrySet()){
			unigram1.add(new WordTF(entry.getKey(), entry.getValue().total));
			unigram2.add(new WordDF(entry.getKey(), entry.getValue()));
		}
	}
	
	public void rank(){
		RM rm = new RM(files, FileSize, unigram);
		
		//rm.rankBM25();
		//System.out.println("Finished Ranking BM25");
		
		//rm.rankBM25_Derivants();
		//System.out.println("Finished Ranking BM25_Derivants");
		
		//rm.rankBM25_Synonym();
		//System.out.println("Finished Ranking BM25_Synonym");
		
		//rm.rankTFIDF();
		//System.out.println("Finished Ranking TFIDF");
		
		/***********************************************
		 * If you want to run the following functions, 
		 * please change the PATH value to "stoppedCorpus"
		 ***********************************************/
		/*rm.rankStop();
		System.out.println("Finished Ranking Stop");
		
		rm.rankStopExtend();
		System.out.println("Finished Ranking StopExtend");*/
		
		/***********************************************
		 * If you want to run the following functions, 
		 * please change the PATH value to "stemmedCorpus"
		 ***********************************************/
		rm.rankStem();
		System.out.println("Finished Ranking Stem");
	}
	
	private String[] getUniToken(String content){
		return content.split("\\s+?");
	}
	
	private String[] getBiToken(String content){
		String[] tokens = content.split("\\s");
		String[] ret = new String[tokens.length-1];
		for(int i=0; i<tokens.length-1; i++){
			ret[i] = tokens[i] + " " + tokens[i+1];
		}
		return ret;
	}

	private String[] getTriToken(String content){	
		String[] tokens = content.split("\\s");
		String[] ret = new String[tokens.length-2];
		for(int i=0; i<tokens.length-2; i++){
			ret[i] = tokens[i] + " " + tokens[i+1] + " " + tokens[i+2];
		}
		return ret;
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

