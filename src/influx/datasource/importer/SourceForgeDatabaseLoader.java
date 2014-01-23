/**
 * @author: Shockley
 * @date: 2012-3-22
 */
package influx.datasource.importer;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;
import influx.datasource.dao.Term;
import influx.datasource.dao.Project;
import influx.datasource.dao.SfTopic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
public class SourceForgeDatabaseLoader {
	public static Logger logger = Logger.getLogger("SourceForgeDatabaseLoader");
	public static HibernateService hs = DataSourceFactory.getHibernateInstance();
	private Map<Long, String> topic2Summaries = new HashMap<Long, String>();
	private Map<String, String> project2Summary = new HashMap<String, String>();
	
	/**
	 * Configurations
	 */
	public static int start =1;
	public static int end = 10000;
	public static String root = "Communications";
	public static String dir = "D:\\work\\experiment\\ontology\\TKDE\\sf\\sf.all\\";
	public static String comdir = "D:\\work\\experiment\\ontology\\TKDE\\sf\\sf.all.combined\\";
	
	/**
	 * get summaries of specific projects, <br>
	 * confined by not only the id range, but also the subdomain rooted with rootConcept <br>
	 * besides, all topics a project has shall fall in the subdomain <br>
	 * otherwise the project is out of concern!
	 * @author Shockley Xiang Li
	 * @param start
	 * @param end
	 * @param rootConcept
	 */
	public void getSourceForgeProjectSummary(String rootConcept){
		//TODO 
		List<String> candidateTopics = new ArrayList<String>();
		Session session;
		Transaction tx;
		String sql, hql;
		
		//1.get candidate topics(topics of the subdomain)
		session = hs.getSession();
		tx = session.beginTransaction();
		hql = "from Concept c where c.name = '"+rootConcept+"'";
		List<Term> roots = session.createQuery(hql).list();
		if(roots.size()!=1){
			logger.info("concept table inconsistency! return");
			return;
		}
		Term root = roots.get(0);
		//the root itself shall be added!
		candidateTopics.add(root.getName());
		hql = "select r.to from Relation r where r.from.name = '"+rootConcept+"'";
		List<Term> children = session.createQuery(hql).list();
		if(children==null || children.size()<1){
			logger.info("concept table inconsistency! return");
			return;
		}
		for(Term c : children){
			String topic = c.getName();
			if(topic==null){
				logger.info("null name is found in concept! return");
				return;
			}
			candidateTopics.add(topic);
		}
		tx.commit();
		session.close();
		
		//2.get topic2Summaries and project2Summaries
		//and refine it afterwards: remove those not within the domain
		//slice it into ten loops
		int nullp = 0;
		int namelessp = 0;
		int nosump = 0;
		int nullt = 0;
		int nodesct = 0;
		int pnum = 0;
		for(int i=start; i<=end; i+=1000){
			
			session = hs.getSession();
			tx = session.beginTransaction();
			hql = "from Project p where p.forge.id = 2 and p.id >="+i+"  and p.id <="+ (end> i+999 ? i+999:end);
			Query q = session.createQuery(hql);
			Iterator<Project> pIterator = q.iterate();
			projectloop: while(pIterator.hasNext()){
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
				if(p.getSfSummary()==null || p.getSfSummary().size()<=0 ||
						p.getSfSummary().get(0)==null){
					nosump ++;
					continue;
				}
				String summary = p.getSfSummary().get(0).getDescription();
				if(summary==null || summary.equals("")){
					nosump ++;
					continue;
				}

				List<SfTopic> topics = p.getSfTopic();
				List<String> topicStrings = new ArrayList<String>();
				//whether a project is within the subdomain
				boolean isTarget = true;
				topicloop: for(SfTopic t : topics){
					if(t==null){
						nullt ++;
						continue topicloop;
					}
					String topicString = t.getDescription();
					if(topicString==null || topicString.equals("")){
						nodesct ++;
						continue topicloop;
					}
					//cache all topics, in case the project is within our want-list
					topicStrings.add(topicString);
					//so long as one of the topics are beyond the subdomain, the project is skipped
					if(isTarget && !candidateTopics.contains(topicString)){
						isTarget = false;
						break topicloop;
					}
				}
				//if there is no topic, the project is also skipped
				if(topicStrings.size()<=0)
					isTarget = false;
				
				//if it is a target to consider, put the <project,summary> in the map
				//else skip this project
				if(isTarget){
					if(project2Summary.containsKey(name))
						logger.info("duplicate project found! even within subdomain");
					else{
						project2Summary.put(name, summary);
					}
					//save all its topic strings to hashmap
					for(String t : topicStrings){
						hql = "from Concept c where c.name = '"+t+"'";
						List<Term> concepts = session.createQuery(hql).list();
						if(concepts.size()!=1){
							logger.info("concept table inconsistency! return");
							return;
						}
						Term concept = concepts.get(0);
						String combinedSummary= topic2Summaries.get(concept.getId());
						if(combinedSummary!=null){
							combinedSummary += "\n\r"+summary;
							topic2Summaries.put(concept.getId(), combinedSummary);
						}else{
							combinedSummary = summary;
							topic2Summaries.put(concept.getId(), combinedSummary);
						}
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
		logger.info("encountered topic count = "+topic2Summaries.size());
	}
	
	/**
	 * get project summaries, and combined summaries, each in a different dir
	 * @author Shockley Xiang Li
	 * @param start start project id, 1-based
	 * @param end end project id
	 */
	public void getSourceForgeProjectSummary(){
		//slice it into ten loops
		int nullp = 0;
		int namelessp = 0;
		int nosump = 0;
		int nullt = 0;
		int nodesct = 0;
		for(int i=start; i<=end; i+=1000){
			
			Session session = hs.getSession();
			Transaction tx = session.beginTransaction();
			String hql = "from Project p where p.forge.id = 2 and p.id >="+i+"  and p.id <="+ (end> i+999 ? i+999:end);
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
				if(p.getSfSummary()==null || p.getSfSummary().size()<=0 ||
						p.getSfSummary().get(0)==null){
					nosump ++;
					continue;
				}
				String summary = p.getSfSummary().get(0).getDescription();
				if(summary==null || summary.equals("")){
					nosump ++;
					continue;
				}
				
				if(project2Summary.containsKey(name))
					logger.info("duplicate in project2Summary found for key= "+ name+", and overwriting has done");
				project2Summary.put(name, summary);
				
				List<SfTopic> topics = p.getSfTopic();
				for(SfTopic t : topics){
					if(t==null){
						nullt ++;
						continue;
					}
					String topicString = t.getDescription();
					if(topicString==null || topicString.equals("")){
						nodesct ++;
						continue;
					}
					hql = "from Concept c where c.name = '"+topicString+"'";
					List<Term> concepts = session.createQuery(hql).list();
					if(concepts.size()!=1){
						logger.info("concept table inconsistency! return");
						return;
					}
					Term concept = concepts.get(0);
					String combinedSummary= topic2Summaries.get(concept.getId());
					if(combinedSummary!=null){
						combinedSummary += "\n\r"+summary;
						topic2Summaries.put(concept.getId(), combinedSummary);
					}else{
						combinedSummary = summary;
						topic2Summaries.put(concept.getId(), combinedSummary);
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
		logger.info("encountered topic count = "+topic2Summaries.size());
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
		for(Long t :topic2Summaries.keySet()){
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
				out.write(topic2Summaries.get(t));
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String [] args){
		SourceForgeDatabaseLoader loader = new SourceForgeDatabaseLoader();
		loader.getSourceForgeProjectSummary();
		//loader.getSourceForgeProjectSummary(DatabaseLoader.root);
		loader.storeToTextFile();
		//getCombinedSummary(3000,4000);
	}
}
