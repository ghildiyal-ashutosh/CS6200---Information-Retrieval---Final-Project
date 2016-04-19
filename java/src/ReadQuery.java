import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;


public class ReadQuery {

	private String path;
	
	//private static int count = 64;
	private static int count = 7;
	
	public ReadQuery(String path){
		this.path = path;
	}
	public ArrayList<Hashtable<String, Integer>> Read() throws IOException{
		String content = readFile(path, StandardCharsets.UTF_8);
		String[] queries = content.split("\n");
		ArrayList<Hashtable<String, Integer>> ret = new ArrayList<Hashtable<String, Integer>>();
		for(int i=0; i<count; i++){
			ret.add(new Hashtable<String, Integer>());
			queries[i] = queries[i].substring(2);
			if(queries[i].charAt(0) == ' '){
				queries[i] = queries[i].substring(1);
			}
		}
		int i = 0;
		for(String query : queries){
			String[] terms = query.split(" ");
			for(String term : terms){
				if(ret.get(i).contains(term)){
					ret.get(i).replace(term, ret.get(i).get(term)+1);
				}
				else{
					ret.get(i).put(term, 1);
				}
			}
			i++;
		}
		return ret;
	}
	
	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
