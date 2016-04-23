import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;


public class RM {
	static private double k1 = 1.2;
	static private double b = 0.75;
	static private double k2 = 100.0;
	
	private double avdl;
	
	private ArrayList<File> files;
	private Hashtable<String, Integer> fileSize;
	private Hashtable<String, Pointers> inverted_indexes;
	
	private Hashtable<String, Integer> relevantDocs;
	
	private ArrayList<Hashtable<String, Double>> scores;
	
	@SuppressWarnings("unchecked")
	private static ArrayList<DocScore>[] ranks = (ArrayList<DocScore>[])new ArrayList<?>[64];
	
	public RM(ArrayList<File> files, 
			Hashtable<String, Integer> fileSize, 
			Hashtable<String, Pointers> inverted_indexes){
		this.files = files;
		this.fileSize = fileSize;
		this.inverted_indexes = inverted_indexes;
		
		double totalDl = 0.0;
		scores = new ArrayList<Hashtable<String, Double>>();
		//initialize score hashtable
		for(int i=0; i<64; i++)
			scores.add(new Hashtable<String, Double>());
		
		//get average document length
		for(Entry<String, Integer> entry : fileSize.entrySet())
			totalDl += entry.getValue();
		this.avdl = totalDl/fileSize.size();
		
		//read relevance info
		try {
			readRelvanceInfo();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("avdl = " + avdl);
	}
	
	/**************BM25***************/
	public void rankBM25(){
		ReadQuery rq = new ReadQuery("quries/originalQueriesTokens.txt", 64);
		ArrayList<Hashtable<String, Integer>> content;
		try {
			content = rq.Read();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int i=0;
		for(Hashtable<String, Integer> query : content){
			i++;
			rankPerQuery(query, "query"+i, "BM25_Origin", "BM25");
		}
		
	}
	public void rankBM25_Derivants(){
		ReadQuery rq = new ReadQuery("quries/expandedQueriesTokensUsingDerivantsOriginalIncluded.txt", 64);
		ArrayList<Hashtable<String, Integer>> content;
		try {
			content = rq.Read();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int i=0;
		for(Hashtable<String, Integer> query : content){
			i++;
			rankPerQuery(query, "query"+i, "BM25_Expanded_Derivants", "BM25");
		}
		
	}
	public void rankBM25_Synonym(){
		ReadQuery rq = new ReadQuery("quries/expandedQueriesTokensUsingSynonymOriginalIncluded.txt", 64);
		ArrayList<Hashtable<String, Integer>> content;
		try {
			content = rq.Read();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int i=0;
		for(Hashtable<String, Integer> query : content){
			i++;
			rankPerQuery(query, "query"+i, "BM25_Expanded_Synonym", "BM25");
		}
		
	}
	
	public void rankStopExtend(){
		ReadQuery rq = new ReadQuery("quries/stoppedExpandedQueriesTokensUsingDerivantsOriginalIncluded.txt", 64);
		ArrayList<Hashtable<String, Integer>> content;
		try {
			content = rq.Read();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int i=0;
		for(Hashtable<String, Integer> query : content){
			i++;
			rankPerQuery(query, "query"+i, "StopExtend", "BM25");
		}
	}
	
	public void rankStop(){
		ReadQuery rq = new ReadQuery("quries/stoppedQueriesTokens.txt", 64);
		ArrayList<Hashtable<String, Integer>> content;
		try {
			content = rq.Read();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int i=0;
		for(Hashtable<String, Integer> query : content){
			i++;
			rankPerQuery(query, "query"+i, "Stop", "BM25");
		}
	}
	
	public void rankStem(){
		ReadQuery rq = new ReadQuery("quries/cacm_stem.query.txt", 7);
		ArrayList<Hashtable<String, Integer>> content;
		try {
			content = rq.Read();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int i=0;
		for(Hashtable<String, Integer> query : content){
			i++;
			rankPerQuery(query, "query"+i, "Stem", "BM25");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void rankPerQuery(Hashtable<String, Integer> query, String name, String destFolder, String searchEngine){
		
		ArrayList<DocScore> rank = new ArrayList<DocScore>();
		Hashtable<String, Double> scores = new Hashtable<String, Double>();
		//relevance info
		int R = 0, NR = 0;
		
		System.out.println(name + " Parsing: ");
		HashSet<String> corpus = new HashSet<String>();
		System.out.println("\tCorpus size is : " + corpus.size());
		for(Entry<String, Integer> entry : query.entrySet()){
			String term = entry.getKey();
			if(!inverted_indexes.containsKey(term)) continue;
			Pointers docsContainTerm = inverted_indexes.get(term);
			for(Pointer pointer : docsContainTerm.pointers){
				if(!corpus.contains(pointer.docID))
					corpus.add(pointer.docID);
			}
		}
		
		for(String file : corpus){
			scores.put(file, 0.0);
			if(checkRelevance(file, Integer.parseInt(name.substring(5)))) R++;
			else NR ++;
		}
		
		for(Entry<String, Integer> entry : query.entrySet()){
			String term = entry.getKey();
			int qf = entry.getValue();
			if(!inverted_indexes.containsKey(term)) continue;
			Pointers docsContainTerm = inverted_indexes.get(term);
			System.out.println("\tNumber of docs contain - " + term + " - " + docsContainTerm.pointers.size());
			
			//get relevance information here
			int r = 0, nr = 0;
			for(Pointer pointer : docsContainTerm.pointers){
				if(checkRelevance(pointer.docID, Integer.parseInt(name.substring(5)))) r++;
				else nr ++;
			}
			
			for(Pointer pointer : docsContainTerm.pointers){
				String doc = pointer.docID;
				int tf =  pointer.tf;
				int dl = fileSize.get(doc);
				double K = getKForDoc(dl);
				double score = getScorePerDoc(corpus.size(), docsContainTerm.pointers.size(), qf, tf, K, R, r, NR, nr);
				scores.replace(doc, score + scores.get(doc));
			}
		}
		for(Entry<String, Double> entry : scores.entrySet()){
			rank.add(new DocScore(entry.getKey(), entry.getValue()));
		}
		System.out.println("\tSize of rank is " + rank.size());
		Collections.sort(rank,new Comparator(){
			@Override
			public int compare(Object o1, Object o2) {
				DocScore d1 = (DocScore) o1;
				DocScore d2 = (DocScore) o2;
				int ret = 0;
				if(d1.score - d2.score > 0) ret = -1;
				if(d1.score - d2.score < 0) ret = 1;
				return ret;
			}
		}); 
		
		File file = new File("../results/" + destFolder + "/"+ name + ".txt");
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		int i=1;
		for(DocScore ds : rank){
			if(i > 100) break;
			pw.println(name + " Q0 " + ds.docId + " " + i + " " + ds.score + " " + searchEngine);
			i++;
		}
		pw.close();
	}
	
	/**************IFTDF***************/
	public void rankTFIDF(){
		ReadQuery rq = new ReadQuery("quries/originalQueriesTokens.txt", 64);
		ArrayList<Hashtable<String, Integer>> content;
		try {
			content = rq.Read();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int i=0;
		for(Hashtable<String, Integer> query : content){
			i++;
			rankTFIDFPerQuery(query, "query"+i);
			System.out.println("TFIDF: query" + i + " processed!");
		}
	}

	//TFIDF
	@SuppressWarnings("unchecked")
	private void rankTFIDFPerQuery(Hashtable<String, Integer> query, String name){
		Hashtable<String, Double> dividen = new Hashtable<String, Double>();
		Hashtable<String, Double> tfidf_scores = new Hashtable<String, Double>();
		
		HashSet<String> corpus = new HashSet<String>();
		for(Entry<String, Integer> entry : query.entrySet()){
			String term = entry.getKey();
			if(!inverted_indexes.containsKey(term)) continue;
			Pointers docsContainTerm = inverted_indexes.get(term);
			for(Pointer pointer : docsContainTerm.pointers){
				if(!corpus.contains(pointer.docID))
					corpus.add(pointer.docID);
			}
		}
		System.out.println("Size of corpus is - " + corpus.size());
		for(String file : corpus){
			tfidf_scores.put(file, 0.0);
		}
		
		for(Entry<String, Integer> entry : query.entrySet()){
			if(!inverted_indexes.containsKey(entry.getKey())) continue;
			Pointers pointers = inverted_indexes.get(entry.getKey());
			int df = pointers.pointers.size();
			for(Pointer pointer : pointers.pointers){
				String fileName = pointer.docID;
				double tf = (double)pointer.tf / (double)fileSize.get(fileName);
				double idf = Math.log((double)corpus.size() / (double)df);
				tfidf_scores.replace(fileName, tfidf_scores.get(fileName) + tf*idf);
			}
		}
		
		
		//put into rank arraylist to sort
		ArrayList<DocScore> rank = new ArrayList<DocScore>();
		for(Entry<String, Double> entry : tfidf_scores.entrySet()){
			rank.add(new DocScore(entry.getKey(), entry.getValue()));
			//System.out.println(entry.getKey() + ": \t" + entry.getValue());
		}
		
		Collections.sort(rank,new Comparator(){
			@Override
			public int compare(Object o1, Object o2) {
				DocScore d1 = (DocScore) o1;
				DocScore d2 = (DocScore) o2;
				int ret = 0;
				if(d1.score - d2.score >= 0) ret = -1;
				if(d1.score - d2.score < 0) ret = 1;
				return ret;
			}
		}); 
		
		File file = new File("results/TFIDF_Origin/"+ name + ".txt");
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		int i=1;
		for(DocScore ds : rank){
			if(i > 100) break;
			pw.println(name + " Q0 " + ds.docId + " " + i + " " + ds.score + " TFIDF");
			i++;
		}
		pw.close();
	}
	
	private double getKForDoc(int dl){
		return k1 * (1 - b + b * dl / avdl);
	}
	
	private double getScorePerDoc(int N, int n, int qf, int f, double K, int R, int r, int NR, int nr){
		double score = 0;
		double relevance = Math.log(((r+0.5)/(R-r+0.5))/((nr - r + 0.5)/(N-nr-R+r+0.5)));
		score = (k1+1)*f/(K+f) * (k2+1)*qf/(k2+qf) * Math.log((0.5/0.5)/((n+0.5)/(N-n+0.5)));
		return score*relevance;
	}
	
	private boolean checkRelevance(String fileName, int queryID){
		String name = fileName.replace(".txt", "");
		if(this.relevantDocs.containsKey(name) && this.relevantDocs.get(name) == queryID)
			return true;
		return false;
	}
	
	
	private void readRelvanceInfo() throws IOException{
		this.relevantDocs = new Hashtable<String, Integer>();
		
		// Open the file
		FileInputStream fstream = new FileInputStream("../files/cacm.rel");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
		  String[] tokens = strLine.split(" ");
		  relevantDocs.put(tokens[2], Integer.parseInt(tokens[0]));
		}

		//Close the input stream
		br.close();
	}
	
}

class DocScore{
	public String docId;
	public double score;
	
	public DocScore(String docId, double score){
		this.docId = docId;
		this.score = score;
	}
}
