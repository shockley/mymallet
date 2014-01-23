/**
 * @author: Shockley
 * @date: 2012-3-22
 */
package influx.datasource.importer;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;
import influx.datasource.dao.Term;
import influx.datasource.dao.OhlohTag;
import influx.datasource.dao.Project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Shockley
 *
 */
public class OhlohDatabaseLoader {
	public static Logger logger = Logger.getLogger("OhlohDatabaseLoader");
	public static HibernateService hs = DataSourceFactory.getHibernateInstance();
	private Map<Long, String> tag2Summaries = new HashMap<Long, String>();
	private Map<String, String> project2Summary = new HashMap<String, String>();
	
	public static long ohlohforgeId = 5;
	public static int minOhlohProj = 551053;
	public static int maxOhlohProj = 968396;
	
	/**
	 * Configurations
	 */
	private int begin = minOhlohProj;
	private int end = minOhlohProj+10000;
	public static String dir = "D:\\work\\experiment\\ontology\\TKDE\\ohloh\\all\\";
	public static String comdir = "D:\\work\\experiment\\ontology\\TKDE\\ohloh\\all.combined\\";
	
	/**
	 * get project summaries, and combined summaries, each in a different dir
	 * @author Shockley Xiang Li
	 * @param start start project id, 1-based
	 * @param end end project id
	 */
	public void getOhlohProjectSummary(){
		//slice it into ten loops
		int nullp = 0;
		int namelessp = 0;
		int nosump = 0;
		int nullt = 0;
		int nodesct = 0;
		for(int i=begin; i<=end; i+=5){
			//5 is good for running speed
			Session session = hs.getSession();
			Transaction tx = session.beginTransaction();
			String hql = "from Project p where p.forge.id = "+ohlohforgeId+" and p.id >="
			+i+"  and p.id <="+ (end> i+4 ? i+4:end);
			Query q = session.createQuery(hql);
			Iterator<Project> pIterator = q.iterate();
			while(pIterator.hasNext()){
				Project p = pIterator.next();
				if(p==null){
					nullp ++;
					continue;
				}
				String name = p.getName();
				if(name==null){
					namelessp ++;
					continue;
				}
				if(p.getOhlohSummary()==null || p.getOhlohSummary().size()<=0 ||
						p.getOhlohSummary().get(0)==null){
					nosump ++;
					continue;
				}
				String summary = p.getOhlohSummary().get(0).getDescription();
				if(summary==null || summary.equals("")){
					nosump ++;
					continue;
				}
				
				if(project2Summary.containsKey(name))
					logger.info("duplicate in project2Summary found for key= "+ name+", and overwriting has done");
				project2Summary.put(name, summary);
				
				List<OhlohTag> tags = p.getOhlohTag();
				for(OhlohTag t : tags){
					if(t==null){
						nullt ++;
						continue;
					}
					String tagString = t.getDescription();
					if(tagString==null || tagString.equals("")){
						nodesct ++;
						continue;
					}
					hql = "from Concept c where c.name = '"+tagString+"'";
					List<Term> concepts = session.createQuery(hql).list();
					Term concept = null;
					if(concepts.size()==1){
						concept = concepts.get(0);
					}else if(concepts.size()==0){
						continue;
					}else{
						logger.info("concept table inconsistency! return");
						return;
					}
					String combinedSummary= tag2Summaries.get(concept.getId());
					if(combinedSummary!=null){
						combinedSummary += "\n\r"+summary;
						tag2Summaries.put(concept.getId(), combinedSummary);
					}else{
						combinedSummary = summary;
						tag2Summaries.put(concept.getId(), combinedSummary);
					}
				}
				if(p.getId()%100==0 || p.getId()%1000==999 || p.getId()%1000==998 || p.getId()%1000==1 || p.getId()%1000==2)
				{
					logger.info("summary "+p.getId());
					//begin a new tx
					//tx.commit();
					//tx = session.beginTransaction();
				}
			}
			tx.commit();
			session.close();
		}
		logger.info("p==null : "+nullp);	
		logger.info("p nameless : "+namelessp);
		logger.info("p no summary : "+nosump);
		logger.info("t==null : "+nullt);	
		logger.info("t no description : "+nodesct);
		logger.info("collected project count = "+project2Summary.size());
		logger.info("encountered topic count = "+tag2Summaries.size());
	}
	
	public void storeToTextFile(){
		File dfile = new File(dir);
		dfile.mkdirs();
		
		dfile = new File(comdir);
		dfile.mkdirs();
		
		for(String pname :project2Summary.keySet()){
			if(pname==null){
				logger.info("Surprised : p2s contains null key! skipped this time");
				continue;
			}
			pname = pname.replace('/', '_').replace('\\', '_');
			
			File pfile = new File(dir + pname +".txt");
			String summary = project2Summary.get(pname);
			if(summary==null){
				logger.info("Surprised ! : p2s contains null value! make it an empty string this time");
				summary = "";
			}
			if(!pfile.exists() || !pfile.isFile()){	
				try {
					pfile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			PrintWriter out;
			try {
				out = new PrintWriter(pfile);
				out.write(summary);
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(Long t :tag2Summaries.keySet()){
			File pfile = new File(comdir + t +".txt");
			if(!pfile.exists() || !pfile.isFile()){	
				try {
					pfile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			PrintWriter out;
			try {
				out = new PrintWriter(pfile);
				out.write(tag2Summaries.get(t));
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String [] args){
		OhlohDatabaseLoader loader = new OhlohDatabaseLoader();
		loader.getOhlohProjectSummary();
		//loader.getSourceForgeProjectSummary(DatabaseLoader.root);
		loader.storeToTextFile();
		//getCombinedSummary(3000,4000);
	}
}
