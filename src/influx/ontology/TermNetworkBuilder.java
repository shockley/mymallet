/**
 * @Shockley Xiang Li
 * 2012-3-23
 */
package influx.ontology;

import influx.algo.cluster.HAC;
import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;
import influx.datasource.dao.LdaAttribute;
import influx.datasource.dao.Network;
import influx.datasource.dao.Term;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import cc.mallet.util.Maths;


/**
 * end while
 * @author Shockley
 *
 */
public class TermNetworkBuilder {
	/**
	 * THn : takcle data noisiness when comparing kl divergencies of the two
	 * THd : to judge whether 2 vectors are diverged enough
	 */
	public static double THs = 0.5; //0.5~0.75
	public static double THn = 0.2; //0.3~0.5
	public static double THd = 0.1; //0.25~0.45
	public static int Mc = 15; //22-44
	
	private double [][] docTopicDistrib;
	private List<Long> termIds;
	private double [][] JS;
	private double [][] COSINE;
	

	public double[][] getCOSINE() {
		return COSINE;
	}


	private boolean [][] network;
	
	

	/**
	 * KL[a][b] = KLDiver (KL[a]||KL[b])
	 */
	private double [][] KL;
	
	private double [] entropy;
	
	public double[][] getKL() {
		return KL;
	}

	public double [] getEntropy( ) {
		return entropy;
	}
	

	private List<String> termNames;
	
	public static Logger logger = Logger.getLogger(TermNetworkBuilder.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	public TermNetworkBuilder() {
		super();
	}
	
	public void loadDataFromDb(){
		Session session = hs.getSession();
		Transaction tx = session.beginTransaction();
		String hql = "from LdaAttribute l where l.attributeName = 'newDocNum'";
		Query q = session.createQuery(hql);
		List<LdaAttribute> attributes = q.list();
		if(attributes.size()!=1)
			logger.error("database is not in consistency!");
		LdaAttribute att = attributes.get(0);
		int numTerms = Integer.parseInt(att.getValue());
		
		hql = "from LdaAttribute l where l.attributeName = 'numTopics'";
		q = session.createQuery(hql);
		attributes = q.list();
		if(attributes.size()!=1)
			logger.error("database is not in consistency!");
		att = attributes.get(0);
		int numTopics = Integer.parseInt(att.getValue());
		tx.commit();
		session.close();
		
		docTopicDistrib = new double [numTerms][numTopics];
		for(int i=0;i<numTerms;i++){
			session = hs.getSession();
			tx = session.beginTransaction();
			for(int j=0;j<numTopics;j++){
				hql = "from LdaAttribute l where l.attributeName = 'newDocTopicDistrib'" +
						" and l.index1 = " +i +" and l.index2 = "+j;
				q = session.createQuery(hql);
				attributes = q.list();
				if(attributes.size()!=1)
					logger.error("database is not in consistency!");
				att = attributes.get(0);
				docTopicDistrib[i][j] = Double.parseDouble(att.getValue());
			}
			tx.commit();
			session.close();
		}
		
		
		termIds = new ArrayList<Long>();
		for(int i=0;i<numTerms;i++){
			session = hs.getSession();
			tx = session.beginTransaction();
			hql = "from LdaAttribute l where l.attributeName = 'termDocIds'" +
						" and l.index2 = " +i;
			q = session.createQuery(hql);
			attributes = q.list();
			if(attributes.size()!=1)
				logger.error("database is not in consistency!");
			att = attributes.get(0);
			Long termId = Long.parseLong(att.getValue());
			termIds.add(termId);
			tx.commit();
			session.close();
		}
		
		
		termNames  =  new ArrayList<String>();
		
		for(Long cid : termIds){
			session = hs.getSession();
			tx = session.beginTransaction();
			hql = "from Term c where c.id = "+cid;
			List<Term> terms = session.createQuery(hql).list();
			if(terms.size()!=1 || terms.get(0) == null){
				logger.info("terms.size()!=1 return");
				return;
			}
			String term = terms.get(0).getName();
			//acording to DOT language given in the 
			term = "\""+term+"\"";
			termNames.add(term);
			tx.commit();
			session.close();
		}
		
		logger.info("Data Loaded!");
	}
	
	
	/**
	 *JS,KL
	 * @author Shockley Xiang Li
	 */
	public void computeJsKlEntropy(){
		int docNum = docTopicDistrib.length;
		JS = new double [docNum][docNum];
		KL = new double [docNum][docNum];
		COSINE = new double [docNum][docNum];
		entropy = new double [docNum];
		for(int i=0;i<docNum;i++){
				double [] rowJS = new double [docNum];
				double [] rowKL = new double [docNum];
				double [] rowCOS = new double [docNum];
				for(int j=0;j<docNum;j++){
					rowJS[j] = Maths.jensenShannonDivergence(docTopicDistrib[i], docTopicDistrib[j]);
					rowKL[j] = Maths.klDivergence(docTopicDistrib[i], docTopicDistrib[j]);
					rowCOS[j] = 1.0 - Maths.cosineSimilarity(docTopicDistrib[i], docTopicDistrib[j]);
				}
				JS[i] = rowJS;
				KL[i] = rowKL;
				COSINE[i] = rowCOS;
				entropy[i] = Maths.getEntropy(docTopicDistrib[i]);
		}
		logger.info("js kl entropy calced!");
	}
	
	/**
	 * calculate the whole network, even though it's symmetrical
	 * but later we only store the upper triangle
	 * @author Shockley Xiang Li
	 * @param useJS : false -- use KL divergence
	 * true -- use JS divergence
	 */
	public void computeNetwork(boolean useJS){
		int termNum = JS.length;
		this.network = new boolean[termNum][termNum];
		if(useJS){
			for(int i=0;i<termNum;i++){
				for(int j=0;j<termNum;j++){
					network[i][j] = (JS[i][j]<THd);
				}
			}
		}else{
			for(int i=0;i<termNum;i++){
				for(int j=0;j<termNum;j++){
					network[i][j] = (KL[i][j]<THd);
				}
			}
		}
	}
	
	/**
	 * @author Shockley Xiang Li
	 */
	public void printJSToFile(File file){
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			pw.println("JS : ");
			int rank = JS.length;
			for(int i=0;i<rank;i++){
				for(int j=0;j<rank;j++){
					pw.println(termIds.get(i)+","+termIds.get(j)+":"+JS[i][j]);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @author Shockley Xiang Li
	 */
	public void printDocTopicToFile(File file){
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			int docNum = docTopicDistrib.length;
			for(int i = 0 ;i< docTopicDistrib.length;i++){
				for(int j = i+1 ;j< docTopicDistrib.length;j++){
					if(docTopicDistrib[i].length != docTopicDistrib[j].length){
						logger.info("Incorrect document-topic distribution: topic number is not consistent!");
						return;
					}
				}
			}
			int topicNum = docTopicDistrib[0].length;
			logger.info("term num is "+docNum);
			for(int i=0;i<docNum;i++){
				for(int j=0;j<topicNum;j++){
					pw.println(termIds.get(i)+", topic "+j+", "+docTopicDistrib[i][j]);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @author Shockley Xiang Li
	 */
	public void printDTdistToScreen(){
		int rank = JS.length;
		logger.info("the rank of JS is "+rank);
		for(int i=0;i<rank;i++){
			for(int j=0;j<rank;j++){
				logger.info(JS[i][j]+" ");
			}
			logger.info("\r");
		}
	}
	
	/**
	 * Output all the semanticly-similar edges, according to JS
	 * only upper triangle of the matrix
	 * Filtered with THs/THd
	 * Called after init()
	 * This algorithm is not in the original TKDE paper
	 * @author Shockley Xiang Li
	 */
	public void printNetworkToFile(File file){
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			pw.println("graph vis{");
			pw.println("rankdir=LR;");
			//pw.print("the rank of Ontology is "+rank + "\r");
			int termNum = network.length;
			for(int i=0;i<termNum;i++){
				for(int j=i+1;j<termNum;j++){
					if(network[i][j]){
						pw.println(termNames.get(i)+"--"+termNames.get(j)+";");
					}
				}
			}
			pw.println("}");
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * To print the ontology learned by a HAC learner,
	 * the function is placed here because this class 
	 * knows the termNames (after loading from db)
	 * @author Shockley Xiang Li
	 * @param file
	 * @param hac
	 */
	public void printOntologyToFile(HAC hac, File file){
		PrintWriter pw;
		boolean [][] ontology = hac.getOntology();
		try {
			pw = new PrintWriter(file);
			pw.println("digraph vis{");
			pw.println("rankdir=LR;");
			//pw.print("the rank of Ontology is "+rank + "\r");
			int termNum = ontology.length;
			for(int i=0;i<termNum;i++){
				for(int j=0;j<termNum;j++){
					if(ontology[i][j]){
						pw.println(termNames.get(i)+"->"+termNames.get(j)+";");
					}
				}
			}
			pw.println("}");
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @author Shockley Xiang Li
	 */
	public void storeNetworkToDB(){
		Session session = hs.getSession();
		Transaction tx = session.beginTransaction();
		String sql = "truncate table onto_network";
		SQLQuery query = session.createSQLQuery(sql);
		query.executeUpdate();
		tx.commit();
		
		int termNum = JS.length;
		for(int i=0;i<termNum;i++){
			for(int j=0;j<termNum;j++){
				if(network[i][j]){
					session = hs.getSession();
					tx = session.beginTransaction();
					
					Long I = termIds.get(i);
					Long J = termIds.get(j);
					Network n = new Network();
					n.setFrom((Term) session.load(Term.class, I));
					n.setTo((Term) session.load(Term.class, J));
					session.save(n);
					
					tx.commit();
					session.close();
				}
			}
		}
		logger.info("network stored in db!");
	}
	
	/**
	 * the possible relations between term a and b
	 * @author Shockley Xiang Li
	 * @param a
	 * @param b
	 * @return the relation of term a & b, a is (similar with)/(broader than) b
	**/
	public boolean broaderConditionHolds(int a, int b){
		if(JS[a][b] < THd && KL[a][b]-KL[b][a]<THn){
			return true;
		}else{
			return false;
		}
		
	}
	
	public List<Long> getTermIds() {
		return termIds;
	}

	public void setTerms(List<String> terms) {
		this.termNames = terms;
	}

	public List<String> getTerms() {
		return termNames;
	}
	
	public double[][] getJS() {
		return JS;
	}

	public void setJS(double[][] jS) {
		JS = jS;
	}
	public boolean[][] getNetwork() {
		return network;
	}

	public void setNetwork(boolean[][] network) {
		this.network = network;
	}
	
	/**
	 * @author Shockley Xiang Li
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String network = "D:\\work\\experiment\\ontology\\TKDE\\ohloh\\ohloh.network";
		File networkFile = new File(network);
		TermNetworkBuilder onto = new TermNetworkBuilder();
		onto.loadDataFromDb();
		onto.computeJsKlEntropy();
		onto.computeNetwork(true);
		onto.printNetworkToFile(networkFile);
		onto.storeNetworkToDB();
	}
}
