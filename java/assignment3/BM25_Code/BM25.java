import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map.Entry;


public class BM25 {
	static private double k1 = 1.2;
	static private double b = 0.75;
	static private double k2 = 100.0;
	
	private double avdl;
	
	private String[] query1;
	private String[] query2;
	private String[] query3;
	private String[] query4;
	
	static private String PATH = "files/";
	private ArrayList<File> files;
	private Hashtable<String, Integer> fileSize;
	private Hashtable<String, Pointers> inverted_indexes;
	
	private Hashtable<String, Double> scores1;
	private Hashtable<String, Double> scores2;
	private Hashtable<String, Double> scores3;
	private Hashtable<String, Double> scores4;
	
	private ArrayList<DocScore> Rank1;
	private ArrayList<DocScore> Rank2;
	private ArrayList<DocScore> Rank3;
	private ArrayList<DocScore> Rank4;
	
	public BM25(ArrayList<File> files, 
			Hashtable<String, Integer> fileSize, 
			Hashtable<String, Pointers> inverted_indexes){
		this.files = files;
		this.fileSize = fileSize;
		this.inverted_indexes = inverted_indexes;
		
		query1 = new String[3];
		query1[0] = "global";
		query1[1] = "warming";
		query1[2] = "potential";
		
		query2 = new String[4];
		query2[0] = "green";
		query2[1] = "power";
		query2[2] = "renewable";
		query2[3] = "energy";
		
		query3 = new String[3];
		query3[0] = "solar";
		query3[1] = "energy";
		query3[2] = "california";
		
		query4 = new String[5];
		query4[0] = "light";
		query4[1] = "bulb";
		query4[2] = "bulbs";
		query4[3] = "alternative";
		query4[4] = "alternatives";
		
		double totalDl = 0.0;
		
		//initialize score hashtable
		scores1 = new Hashtable<String, Double>();
		scores2 = new Hashtable<String, Double>();
		scores3 = new Hashtable<String, Double>();
		scores4 = new Hashtable<String, Double>();
		for(Entry<String, Integer> entry : fileSize.entrySet()){
			scores1.put(entry.getKey(), 0.0);
			scores2.put(entry.getKey(), 0.0);
			scores3.put(entry.getKey(), 0.0);
			scores4.put(entry.getKey(), 0.0);
			totalDl += entry.getValue();
		}
		//get average document length
		this.avdl = totalDl/fileSize.size();
	}
	
	//ranking function
	public void rank(){
		rankPerQuery(query1, "query1", scores1, Rank1);
		rankPerQuery(query2, "query2", scores2, Rank2);
		rankPerQuery(query3, "query3", scores3, Rank3);
		rankPerQuery(query4, "query4", scores4, Rank4);
		
		/*String[] toyQuery = new String[2];
		toyQuery[0] = "i";
		toyQuery[1] = "my";
		rankPerQuery(toyQuery, "toyQuery", scores1, Rank1);*/
		//print
		
	}
	
	@SuppressWarnings("unchecked")
	private void rankPerQuery(String[] query, String name, Hashtable<String, Double> scores, ArrayList<DocScore> rank){
		rank = new ArrayList<DocScore>();
		System.out.println(name + " Parsing: ");
		for(String term : query){
			if(!inverted_indexes.containsKey(term)) continue;
			Pointers docsContainTerm = inverted_indexes.get(term);
			System.out.println("Number of docs contain - " + term + " - " + docsContainTerm.pointers.size());
			for(Pointer pointer : docsContainTerm.pointers){
				String doc = pointer.docID;
				int tf =  pointer.tf;
				int dl = fileSize.get(doc);
				double K = getKForDoc(dl);
				double score = getScorePerDoc(files.size(), docsContainTerm.pointers.size(), 1, tf, K);
				scores.replace(doc, score + scores.get(doc));
			}
		}
		for(Entry<String, Double> entry : scores.entrySet()){
			if(entry.getValue() != 0.0)
				rank.add(new DocScore(entry.getKey(), entry.getValue()));
		}
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
		
		File file = new File("assignment3/"+ name + "-BM25.txt");
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		int i=1;
		for(DocScore ds : rank){
			pw.println(name.charAt(5) + " Q0 " + ds.docId + " " + i + " " + ds.score + " WX");
			i++;
		}
		pw.close();
		/*
		System.out.println("**** ****");
		System.out.println(name + ": Top 100 ranking");
		for(int i=0; i<100; i++){
			System.out.println("Doc: " + rank.get(i).docId + "\t Score: " + rank.get(i).score);
		}
		//query_id	Q0	doc_id	rank	BM25_score	system_name
		System.out.println("----------\n");*/
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
