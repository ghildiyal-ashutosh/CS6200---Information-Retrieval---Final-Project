import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
		System.out.println("avdl = " + avdl);
	}
	
	/**************BM25***************/
	public void rankBM25(){
		ReadQuery rq = new ReadQuery("originalQueriesTokens.txt");
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
			rankBM25PerQuery(query, "query"+i, scores.get(i-1), ranks[i-1]);
		}
		
	}
	public void rankBM25_Derivants(){
		ReadQuery rq = new ReadQuery("expandedQueriesTokensUsingDerivantsOriginalIncluded.txt");
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
			rankBM25_Derivants_PerQuery(query, "query"+i, scores.get(i-1), ranks[i-1]);
		}
		
	}
	public void rankBM25_Synonym(){
		ReadQuery rq = new ReadQuery("expandedQueriesTokensUsingSynonymOriginalIncluded.txt");
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
			rankBM25_Synonym_PerQuery(query, "query"+i, scores.get(i-1), ranks[i-1]);
		}
		
	}
	
	public void rankStopExtend(){
		ReadQuery rq = new ReadQuery("stoppedExpandedQueriesTokensUsingDerivantsOriginalIncluded.txt");
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
			rankStopExtendPerQuery(query, "query"+i, scores.get(i-1), ranks[i-1]);
		}
	}
	
	public void rankStop(){
		ReadQuery rq = new ReadQuery("stoppedQueriesTokens.txt");
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
			rankStopPerQuery(query, "query"+i, scores.get(i-1), ranks[i-1]);
		}
	}
	
	public void rankStem(){
		ReadQuery rq = new ReadQuery("cacm_stem.query.txt");
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
			rankStemPerQuery(query, "query"+i, scores.get(i-1), ranks[i-1]);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void rankStemPerQuery(Hashtable<String, Integer> query, String name, Hashtable<String, Double> scores, ArrayList<DocScore> rank1){
		ArrayList<DocScore> rank = new ArrayList<DocScore>();
		scores = new Hashtable<String, Double>();
		System.out.println(name + " Parsing: ");
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
		
		for(String file : corpus){
			scores.put(file, 0.0);
		}
		
		for(Entry<String, Integer> entry : query.entrySet()){
			String term = entry.getKey();
			int qf = entry.getValue();
			if(!inverted_indexes.containsKey(term)) continue;
			Pointers docsContainTerm = inverted_indexes.get(term);
			System.out.println("Number of docs contain - " + term + " - " + docsContainTerm.pointers.size());
			for(Pointer pointer : docsContainTerm.pointers){
				String doc = pointer.docID;
				int tf =  pointer.tf;
				int dl = fileSize.get(doc);
				double K = getKForDoc(dl);
				double score = getScorePerDoc(corpus.size(), docsContainTerm.pointers.size(), qf, tf, K);
				scores.replace(doc, score + scores.get(doc));
			}
		}
		for(Entry<String, Double> entry : scores.entrySet()){
			rank.add(new DocScore(entry.getKey(), entry.getValue()));
		}
		System.out.println("Size of rank is " + rank.size());
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
		
		File file = new File("results/Stem/"+ name + ".txt");
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
			pw.println(name.charAt(5) + " Q0 " + ds.docId + " " + i + " " + ds.score + " BM25");
			i++;
		}
		pw.close();
	}
	
	@SuppressWarnings("unchecked")
	private void rankStopPerQuery(Hashtable<String, Integer> query, String name, Hashtable<String, Double> scores, ArrayList<DocScore> rank1){
		ArrayList<DocScore> rank = new ArrayList<DocScore>();
		scores = new Hashtable<String, Double>();
		System.out.println(name + " Parsing: ");
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
		
		for(String file : corpus){
			scores.put(file, 0.0);
		}
		
		for(Entry<String, Integer> entry : query.entrySet()){
			String term = entry.getKey();
			int qf = entry.getValue();
			if(!inverted_indexes.containsKey(term)) continue;
			Pointers docsContainTerm = inverted_indexes.get(term);
			System.out.println("Number of docs contain - " + term + " - " + docsContainTerm.pointers.size());
			for(Pointer pointer : docsContainTerm.pointers){
				String doc = pointer.docID;
				int tf =  pointer.tf;
				int dl = fileSize.get(doc);
				double K = getKForDoc(dl);
				double score = getScorePerDoc(corpus.size(), docsContainTerm.pointers.size(), qf, tf, K);
				scores.replace(doc, score + scores.get(doc));
			}
		}
		for(Entry<String, Double> entry : scores.entrySet()){
			rank.add(new DocScore(entry.getKey(), entry.getValue()));
		}
		System.out.println("Size of rank is " + rank.size());
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
		
		File file = new File("results/Stop/"+ name + ".txt");
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
			pw.println(name.charAt(5) + " Q0 " + ds.docId + " " + i + " " + ds.score + " BM25");
			i++;
		}
		pw.close();
	}
	
	private void rankStopExtendPerQuery(Hashtable<String, Integer> query, String name, Hashtable<String, Double> scores, ArrayList<DocScore> rank1){
		ArrayList<DocScore> rank = new ArrayList<DocScore>();
		scores = new Hashtable<String, Double>();
		System.out.println(name + " Parsing: ");
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
		
		for(String file : corpus){
			scores.put(file, 0.0);
		}
		
		for(Entry<String, Integer> entry : query.entrySet()){
			String term = entry.getKey();
			int qf = entry.getValue();
			if(!inverted_indexes.containsKey(term)) continue;
			Pointers docsContainTerm = inverted_indexes.get(term);
			System.out.println("Number of docs contain - " + term + " - " + docsContainTerm.pointers.size());
			for(Pointer pointer : docsContainTerm.pointers){
				String doc = pointer.docID;
				int tf =  pointer.tf;
				int dl = fileSize.get(doc);
				double K = getKForDoc(dl);
				double score = getScorePerDoc(corpus.size(), docsContainTerm.pointers.size(), qf, tf, K);
				scores.replace(doc, score + scores.get(doc));
			}
		}
		for(Entry<String, Double> entry : scores.entrySet()){
			rank.add(new DocScore(entry.getKey(), entry.getValue()));
		}
		System.out.println("Size of rank is " + rank.size());
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
		
		File file = new File("results/StopExtend/"+ name + ".txt");
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
			pw.println(name.charAt(5) + " Q0 " + ds.docId + " " + i + " " + ds.score + " BM25");
			i++;
		}
		pw.close();
	}
	
	@SuppressWarnings("unchecked")
	private void rankBM25PerQuery(Hashtable<String, Integer> query, String name, Hashtable<String, Double> scores, ArrayList<DocScore> rank1){
		ArrayList<DocScore> rank = new ArrayList<DocScore>();
		scores = new Hashtable<String, Double>();
		System.out.println(name + " Parsing: ");
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
		
		for(String file : corpus){
			scores.put(file, 0.0);
		}
		
		for(Entry<String, Integer> entry : query.entrySet()){
			String term = entry.getKey();
			int qf = entry.getValue();
			if(!inverted_indexes.containsKey(term)) continue;
			Pointers docsContainTerm = inverted_indexes.get(term);
			System.out.println("Number of docs contain - " + term + " - " + docsContainTerm.pointers.size());
			for(Pointer pointer : docsContainTerm.pointers){
				String doc = pointer.docID;
				int tf =  pointer.tf;
				int dl = fileSize.get(doc);
				double K = getKForDoc(dl);
				double score = getScorePerDoc(corpus.size(), docsContainTerm.pointers.size(), qf, tf, K);
				scores.replace(doc, score + scores.get(doc));
			}
		}
		for(Entry<String, Double> entry : scores.entrySet()){
			rank.add(new DocScore(entry.getKey(), entry.getValue()));
		}
		System.out.println("Size of rank is " + rank.size());
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
		
		File file = new File("results/BM25_Origin/"+ name + ".txt");
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
			pw.println(name.charAt(5) + " Q0 " + ds.docId + " " + i + " " + ds.score + " BM25");
			i++;
		}
		pw.close();
	}
	
	@SuppressWarnings("unchecked")
	private void rankBM25_Derivants_PerQuery(Hashtable<String, Integer> query, String name, Hashtable<String, Double> scores, ArrayList<DocScore> rank1){
		ArrayList<DocScore> rank = new ArrayList<DocScore>();
		scores = new Hashtable<String, Double>();
		System.out.println(name + " Parsing: ");
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
		
		for(String file : corpus){
			scores.put(file, 0.0);
		}
		
		for(Entry<String, Integer> entry : query.entrySet()){
			String term = entry.getKey();
			int qf = entry.getValue();
			if(!inverted_indexes.containsKey(term)) continue;
			Pointers docsContainTerm = inverted_indexes.get(term);
			System.out.println("Number of docs contain - " + term + " - " + docsContainTerm.pointers.size());
			for(Pointer pointer : docsContainTerm.pointers){
				String doc = pointer.docID;
				int tf =  pointer.tf;
				int dl = fileSize.get(doc);
				double K = getKForDoc(dl);
				double score = getScorePerDoc(corpus.size(), docsContainTerm.pointers.size(), qf, tf, K);
				scores.replace(doc, score + scores.get(doc));
			}
		}
		for(Entry<String, Double> entry : scores.entrySet()){
			rank.add(new DocScore(entry.getKey(), entry.getValue()));
		}
		System.out.println("Size of rank is " + rank.size());
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
		
		File file = new File("results/BM25_Expanded_Derivants/"+ name + ".txt");
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
			pw.println(name.charAt(5) + " Q0 " + ds.docId + " " + i + " " + ds.score + " BM25");
			i++;
		}
		pw.close();
	}
	
	@SuppressWarnings("unchecked")
	private void rankBM25_Synonym_PerQuery(Hashtable<String, Integer> query, String name, Hashtable<String, Double> scores, ArrayList<DocScore> rank1){
		ArrayList<DocScore> rank = new ArrayList<DocScore>();
		scores = new Hashtable<String, Double>();
		System.out.println(name + " Parsing: ");
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
		
		for(String file : corpus){
			scores.put(file, 0.0);
		}
		
		for(Entry<String, Integer> entry : query.entrySet()){
			String term = entry.getKey();
			int qf = entry.getValue();
			if(!inverted_indexes.containsKey(term)) continue;
			Pointers docsContainTerm = inverted_indexes.get(term);
			System.out.println("Number of docs contain - " + term + " - " + docsContainTerm.pointers.size());
			for(Pointer pointer : docsContainTerm.pointers){
				String doc = pointer.docID;
				int tf =  pointer.tf;
				int dl = fileSize.get(doc);
				double K = getKForDoc(dl);
				double score = getScorePerDoc(corpus.size(), docsContainTerm.pointers.size(), qf, tf, K);
				scores.replace(doc, score + scores.get(doc));
			}
		}
		for(Entry<String, Double> entry : scores.entrySet()){
			rank.add(new DocScore(entry.getKey(), entry.getValue()));
		}
		System.out.println("Size of rank is " + rank.size());
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
		
		File file = new File("results/BM25_Expanded_Synonym/"+ name + ".txt");
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
			pw.println(name.charAt(5) + " Q0 " + ds.docId + " " + i + " " + ds.score + " BM25");
			i++;
		}
		pw.close();
	}
	
	/**************IFTDF***************/
	public void rankTFIDF(){
		ReadQuery rq = new ReadQuery("originalQueriesTokens.txt");
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
			pw.println(name.charAt(5) + " Q0 " + ds.docId + " " + i + " " + ds.score + " TFIDF");
			i++;
		}
		pw.close();
	}
	
	
	
	
	private double getKForDoc(int dl){
		return k1 * (1 - b + b * dl / avdl);
	}
	
	private double getScorePerDoc(int N, int n, int qf, int f, double K){
		double score = 0;
		score = (k1+1)*f/(K+f) * (k2+1)*qf/(k2+qf) * Math.log((0.5/0.5)/((n+0.5)/(N-n+0.5)));
		return score;
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
