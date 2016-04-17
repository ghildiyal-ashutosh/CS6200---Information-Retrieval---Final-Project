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
	static private String PATH = "files/";
	private ArrayList<File> files;
	
	private Hashtable<String, Integer> FileSize; 
	private Hashtable<String, Pointers> unigram;
	private Hashtable<String, Pointers> bigram;
	private Hashtable<String, Pointers> trigram;
	
	public Summarizer(){
		//initialize variables
		FileSize = new Hashtable<String, Integer>();
		unigram = new Hashtable<String, Pointers>();
		bigram = new Hashtable<String, Pointers>();
		trigram = new Hashtable<String, Pointers>();
		
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
			//String[] biTokens = getBiToken(content);
			//String[] triTokens = getTriToken(content);
			
			summarize(uniTokens, 1, file);
			//summarize(biTokens, 2, file);
			//summarize(triTokens, 3, file);
			
			//store the file size;
			FileSize.put(file.getName(), uniTokens.length);
			
			//get unigram first
			
		}
		
		Comparator<WordTF> wtfComparator = new WTFComparator();
		PriorityQueue<WordTF> unigram1 = new PriorityQueue<WordTF>(1,wtfComparator);
		PriorityQueue<WordTF> bigram1 = new PriorityQueue<WordTF>(1,wtfComparator);
		PriorityQueue<WordTF> trigram1 = new PriorityQueue<WordTF>(1,wtfComparator);
		
		Comparator<WordDF> wdfComparator = new WDFComparator();
		PriorityQueue<WordDF> unigram2 = new PriorityQueue<WordDF>(1,wdfComparator);
		PriorityQueue<WordDF> bigram2 = new PriorityQueue<WordDF>(1,wdfComparator);
		PriorityQueue<WordDF> trigram2 = new PriorityQueue<WordDF>(1,wdfComparator);
		
		//check unigram
		for(Entry<String, Pointers> entry : unigram.entrySet()){
			unigram1.add(new WordTF(entry.getKey(), entry.getValue().total));
			unigram2.add(new WordDF(entry.getKey(), entry.getValue()));
		}
		
		File file = new File("results/unigram1.txt");
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		while(!unigram1.isEmpty()){
			WordTF wtf = unigram1.poll();
			pw.println(wtf.word + " " + wtf.tf);
		}
		pw.close();
		
		file = new File("results/unigram2.txt");
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		while(!unigram2.isEmpty()){
			WordDF wdf = unigram2.poll();
			pw.print(wdf.word + "\t");
			for(Pointer p : wdf.pointers.pointers)
				pw.print(p.docID + ",");
			pw.print("\t" + wdf.pointers.pointers.size());
			pw.print("\n");
		}
		pw.close();
		
		//check bigram
		for(Entry<String, Pointers> entry : bigram.entrySet()){
			bigram1.add(new WordTF(entry.getKey(), entry.getValue().total));
			bigram2.add(new WordDF(entry.getKey(), entry.getValue()));
		}
		
		file = new File("results/bigram1.txt");
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		while(!bigram1.isEmpty()){
			WordTF wtf = bigram1.poll();
			pw.println(wtf.word + " " + wtf.tf);
		}
		pw.close();
		
		file = new File("results/bigram2.txt");
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		while(!bigram2.isEmpty()){
			WordDF wdf = bigram2.poll();
			pw.print(wdf.word + "\t");
			for(Pointer p : wdf.pointers.pointers)
				pw.print(p.docID + ",");
			pw.print("\t" + wdf.pointers.pointers.size());
			pw.print("\n");
		}
		pw.close();
		
		//check trigram
		for(Entry<String, Pointers> entry : trigram.entrySet()){
			trigram1.add(new WordTF(entry.getKey(), entry.getValue().total));
			trigram2.add(new WordDF(entry.getKey(), entry.getValue()));
		}
		
		file = new File("results/trigram1.txt");
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		while(!trigram1.isEmpty()){
			WordTF wtf = trigram1.poll();
			pw.println(wtf.word + " " + wtf.tf);
		}
		pw.close();
		
		file = new File("results/trigram2.txt");
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		while(!trigram2.isEmpty()){
			WordDF wdf = trigram2.poll();
			pw.print(wdf.word + "\t");
			for(Pointer p : wdf.pointers.pointers)
				pw.print(p.docID + ",");
			pw.print("\t" + wdf.pointers.pointers.size());
			pw.print("\n");
		}
		pw.close();
	}
	
	public void rank(){
		BM25 bm25 = new BM25(files, FileSize, unigram);
		bm25.rank();
	}
	
	private String[] getUniToken(String content){
		return content.split("\\s");
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
			case 2:
				current = this.bigram;
				break;
			case 3:
				current = this.trigram;
				break;
			default:
				return;
				
		}
		Hashtable<String, Integer> wordCount = new Hashtable<String, Integer>();
		for(String token : tokens){
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

