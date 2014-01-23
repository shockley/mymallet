/**
 * @Shockley Xiang Li
 * 2012-6-19
 */
package influx.algo.cluster.evaluate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Shockley
 *
 */
public class HumanEvaluator {
	public static Logger logger = Logger.getLogger(HumanEvaluator.class);
	public static String basedir = "D:/work/experiment/ontology/paper/human/";
	public static File ahc_kl = new File(basedir+"AHC.EXP3.judged");
	public static File ahc_js = new File(basedir+"AHC.EXP4.judged");
	public static File ahc_cos = new File(basedir+"AHC.EXP11.judged");
	public static File tkde = new File(basedir+"TKDE.EXP1.judged");
	public static File cikm_js = new File(basedir+"CIKM.judged");
	public static File cikm_cos = new File(basedir+"CIKM.judged");
	
	public double countPrecision(){
		double p = 0.0;
		int T = 0;
		int F = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(cikm_cos));
			String ln =reader.readLine();
			int lnnumber = 1;
			while(ln!=null){
				if(ln.endsWith("T"))
					T++;
				else if(ln.endsWith("F"))
					F++;
				else if(ln.endsWith("\";"))
					logger.info("unjudged! at "+ lnnumber);
				ln = reader.readLine();	
				lnnumber++;
			}
			p = ((double) T)/(T+F);
			logger.info("T = " +T);
			logger.info("F = " +F);
			logger.info("p = " +p);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;
	}
	
	public static void main(String [] args){
		HumanEvaluator eva = new HumanEvaluator();
		eva.countPrecision();
	}
}
