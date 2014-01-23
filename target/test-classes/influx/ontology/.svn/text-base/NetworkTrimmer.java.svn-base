/**
 * @Shockley Xiang Li
 * 2012-5-10
 */
package influx.ontology;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;
import influx.datasource.dao.Forge;
import influx.datasource.dao.Network;
import influx.datasource.dao.Term;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * this is part of an aborted idea for ontology learning:
 *    1. first we learn what tags are related (semantically similar), link them to output
 *    a network
 *    2. trim the network according to subsumption threshold.
 * @author Shockley
 * @deprecated
 */
public class NetworkTrimmer {
	public static Logger logger = Logger.getLogger(NetworkTrimmer.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	public static long ohlohforgeId = 5;

	public Session session;
	public Transaction tx;
	public String ql;
	private List<Network> originNetwork = null;
	private List<Network> trimmedNetwork = null;
	private Map<Term, List<Long>> term2Projects = null;
	
	public void readTerm2ProjectsFromDB(){
		term2Projects = new HashMap<Term, List<Long>>();
		session = hs.getSession();
		tx = session.beginTransaction();
		ql = "from Term t where t.forge = :f";
		Query q = session.createQuery(ql);
		q.setParameter("f", session.load(Forge.class, ohlohforgeId));
		List<Term> terms = q.list();
		if(terms == null){
			logger.error("ABORT : db error!");
			return;
		}
		tx.commit();
		session.close();
		logger.info("term loaded");
		
		for(Term term : terms){
			String tag = term.getName();
			session = hs.getSession();
			tx = session.beginTransaction();
			ql = "select t.proj_id from ohloh_tagged t where t.description = '"+tag+"'";
			List<Long> projects = session.createSQLQuery(ql).list();
			if(projects==null || projects.contains(null)){
				logger.error("ABORT : db error!");
				return;
			}
			term2Projects.put(term, projects);
			tx.commit();
			session.close();
		}
	}
	
	/**
	 * Read undirected edges of network one by one
	 * check subsumption relation along both direction of that edge
	 * @author Shockley Xiang Li
	 */
	public void readAndTrimNetwork(){
		trimmedNetwork = new ArrayList<Network>();
		session = hs.getSession();
		tx = session.beginTransaction();
		//ql = "select n.from,n.to from onto_network n";
		ql = "from Network n";
		List<Network> nets = session.createQuery(ql).list();
		if(nets == null){
			logger.error("ABORT : db error!");
			return;
		}
		originNetwork = new ArrayList<Network>(); 
		int i = 0;
		for(Network n : nets){
			if(n!=null){
				if(i%100==0){
					logger.info(" "+i+" old nets has been checked");
				}
				boolean ifsubsume = TagSubsumption.ifSubsume(n.getFrom(), n.getTo(), term2Projects);
				if(ifsubsume){
					trimmedNetwork.add(n);
				}
				ifsubsume = TagSubsumption.ifSubsume(n.getTo(), n.getFrom(), term2Projects);
				if(ifsubsume){
					Network nreverse = new Network();
					nreverse.setFrom(n.getTo());
					nreverse.setTo(n.getFrom());
					trimmedNetwork.add(nreverse);
				}
				i++;
			}
		}
		tx.commit();
		session.close();
	}
	
	/**
	 * @author Shockley Xiang Li
	 * @param file
	 */
	public void printTrimmedToFile(File file){
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			pw.println("digraph vis{");
			pw.println("rankdir=LR;");
			//pw.print("the rank of Ontology is "+rank + "\r");
			for(Network net : trimmedNetwork){
				pw.println("\""+net.getFrom().getName()+"\" -> \""+net.getTo().getName()+"\";");
			}
			pw.println("}");
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args){
		String trimmedNetworkFile = "D:\\work\\experiment\\ontology\\TKDE\\ohloh\\trimmed.net";
		File networkFile = new File(trimmedNetworkFile);
		NetworkTrimmer trimmer = new NetworkTrimmer();
		trimmer.readTerm2ProjectsFromDB();
		logger.info("term loaded!");
		trimmer.readAndTrimNetwork();
		logger.info("db loaded!");
		//trimmer.trimNetwork();
		trimmer.printTrimmedToFile(networkFile);
	}
}
