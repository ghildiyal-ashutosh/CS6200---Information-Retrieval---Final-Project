import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;


public class ReadQuery {

	private String path;
	
	private static int count = 64;
	//private static int count = 7;
	
	public ReadQuery(String path){
		this.path = path;
	}
	public String[] Read() throws IOException{
		String content = readFile(path, StandardCharsets.UTF_8);
		String[] queries = content.split("\n");
		for(int i=0; i<count; i++){
			queries[i] = queries[i].substring(2);
			if(queries[i].charAt(0) == ' '){
				queries[i] = queries[i].substring(1);
			}
		}
		return queries;
		
	}
	
	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
