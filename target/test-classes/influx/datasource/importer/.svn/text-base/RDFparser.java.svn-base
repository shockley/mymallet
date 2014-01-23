/**
 * @Shockley Xiang Li
 * 2012-5-28
 */
package influx.datasource.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.sf.josser.Josser;

/**
 * @author Shockley
 *
 */
public class RDFparser {
	public static String BASE_DIR = "D:\\work\\dmoz\\";
	private File rawRdf = new File(BASE_DIR+"structure.rdf.u8");
	private File part1 = new File(BASE_DIR+"structure.rdf.u8.PT1");
	private Josser josser = new Josser();
	
	public void parse(){
		
	}
	
	public void split(){
		try {
			BufferedReader r = new BufferedReader(new FileReader(rawRdf));
			PrintWriter w = new PrintWriter(new FileWriter(part1));
			for(int i=0; i<1000; i++){
				String line = r.readLine();
				w.println(line);
			}
			r.close();
			w.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args){
		RDFparser s = new RDFparser();
		s.split();
	}
}
