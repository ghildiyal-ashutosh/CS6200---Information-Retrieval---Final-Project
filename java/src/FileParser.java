import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class FileParser {
	private static String PATH = "files/"; 
	private ArrayList<File> files;
	
	public FileParser(){
		
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
	
	public void parseHtml() throws IOException {
		for(File file : files){
			String body = Jsoup.parse(file, "ISO-8859-1").select("body").html();
			String content = Jsoup.parse(body).select("p").text();
			PrintWriter pw = new PrintWriter(file);
			pw.println(content);
			pw.close();
		}
	}
	
	public void parseFileName() throws IOException {
		for(File file : files) {
			String name = file.getName();
			if(name.equals(".DS_Store")) continue;
			name = name.replace("_", "");
			File newName = new File(PATH+name);
			file.renameTo(newName);
			System.out.println("Renamed - File name: " + name);
		}
	}
	
	public void tokenize() {
		for(File file : files){
			try {
				tokenizeFile(file);
				System.out.println("Tokenized File: " + file.getName());
			} catch (IOException e) {
				System.out.println("ERROR Opening file " + file.getName());
				e.printStackTrace();
			}
		}
	}
	
	private void tokenizeFile(File file) throws IOException{
		String content = readFile(PATH+file.getName(), StandardCharsets.UTF_8);
		//to lower case
		content = content.toLowerCase();
		//remove [12]
		content = content.replaceAll("\\[(\\d+?)\\]", " ");
		//remove punctuation except , . % 
		content = content.replaceAll("[^0-9a-zA-Z\\s\\-\\%\\,\\.]", "");
		//remove , . % which are not digit use
		for(int i=0; i<content.length(); i++){
			if((content.charAt(i) == ',' || content.charAt(i) == '.') && i+1<content.length() && i>0 && (content.charAt(i+1)>'9'||content.charAt(i+1)<'0' || content.charAt(i-1)>'9'||content.charAt(i-1)<'0')){
				//System.out.println("Matched at index: " + i);
				content = content.substring(0, i) + content.substring(i+1);
				i--;
			}
			if(content.charAt(i) == '%' && i>0 && (content.charAt(i-1)>'9'||content.charAt(i-1)<'0')){
				content = content.substring(0, i) + content.substring(i+1);
				i--;
			}
		}
		//remove consecutive white spaces
		content = content.replaceAll(" +", " ");
		//print out into the file
		PrintWriter pw = new PrintWriter(file);
		pw.println(content);
		pw.close();
	}
	
	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
