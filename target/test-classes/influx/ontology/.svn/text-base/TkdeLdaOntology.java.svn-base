/**
 * @Shockley Xiang Li
 * 2012-3-23
 */
package influx.ontology;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;
import influx.datasource.dao.Network;
import influx.datasource.dao.Term;
import influx.datasource.dao.LdaAttribute;

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

import cc.mallet.types.IDSorter;
import cc.mallet.util.Maths;


/**
 * Algorithm 1LSHL(root)
Require:V, Ms, I, THs, THd, THn, and Mc.
Ensure:A topic hierarchy with broader relation.
1: Initialise V, Ms, I, THs, THd, THn, and Mc;
2: Add current root into V;
3: Select most similarMc nodes of root from Ms;
4: Add all similar nodes into Vtemp;
5: Remove nodes in Vtemp against Definition 2;
6: Assert broader relation between root and nodes in Vtemp;
7: Move nodes in Vtemp to V;
8: Remove current root node from V;
9: while(i < I and V is not empty) do
10:   LSHL(1st node in V);
11:   Increment i by 1;
12: end while
 * @author Shockley
 *
 */
public class TkdeLdaOntology {
	/**
	 * THs : if one is a hypernym of another, the two have to be similar enough, used with similarity measure
	 * THn : takle data noisiness when comparing kl divergencies of the two
	 * THd : the same propose as THs, used with divergence measure
	 */
	
	public static double THn = 0.2; //0.3~0.5
	public static double THd = 0.35; //0.25~0.45
	public static int Mc = 15; //22-44
	public static int I = 10000; // infinite
	public static double THs = 0.3; //0.5~0.75
	
	private String algorithm;
	private double [][] docTopicDistrib;
	private List<Long> termIds;
	private double [][] Ms;
	/**
	 * KL[a][b] = KLDiver (KL[a]||KL[b])
	 */
	private double [][] KL;
	
	/**
	 */
	private int root;
	private List<String> termNames;
	private Relations [][] ontology;
	
	public static enum Relations {UNKNOWN, RELATED, BROADER};
	
	public static Logger logger = Logger.getLogger(TkdeLdaOntology.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	public TkdeLdaOntology() {
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
		
		session = hs.getSession();
		tx = session.beginTransaction();
		termIds = new ArrayList<Long>();
		for(int i=0;i<numTerms;i++){
			hql = "from LdaAttribute l where l.attributeName = 'termDocIds'" +
						" and l.index2 = " +i;
			q = session.createQuery(hql);
			attributes = q.list();
			if(attributes.size()!=1)
				logger.error("database is not in consistency!");
			att = attributes.get(0);
			Long termId = Long.parseLong(att.getValue());
			termIds.add(termId);
		}
		tx.commit();
		session.close();
		
		termNames  =  new ArrayList<String>();
		session = hs.getSession();
		tx = session.beginTransaction();
		for(Long cid : termIds){
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
		}
		tx.commit();
		session.close();
	}
	
	/**
	 *Ms,KL
	 * @author Shockley Xiang Li
	 * @param DTdistrib
	 * @param algo: default is "js"
	 */
	public void computeMsKL(String algo){
		int docNum = docTopicDistrib.length;
		Ms = new double [docNum][docNum];
		KL = new double [docNum][docNum];
		if(algo==null||!algo.equals("cosine")){
			for(int i=0;i<docNum;i++){
				double [] rowMs = new double [docNum];
				double [] rowKL = new double [docNum];
				for(int j=0;j<docNum;j++){
					rowMs[j] = Maths.jensenShannonDivergence(docTopicDistrib[i], docTopicDistrib[j]);
					rowKL[j] = Maths.klDivergence(docTopicDistrib[i], docTopicDistrib[j]);
				}
				Ms[i] = rowMs;
				KL[i] = rowKL;
			}
		}else{
			//cosine
			for(int i=0;i<docNum;i++){
				double [] rowMs = new double [docNum];
				double [] rowKL = new double [docNum];
				for(int j=0;j<docNum;j++){
					rowMs[j] = Maths.cosineSimilarity(docTopicDistrib[i], docTopicDistrib[j]);
					rowKL[j] = Maths.klDivergence(docTopicDistrib[i], docTopicDistrib[j]);
				}
				Ms[i] = rowMs;
				KL[i] = rowKL;
			}
		}
	}
	
	/**
	 * @author Shockley Xiang Li
	 */
	public void printMsToFile(File file){
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			pw.println("Ms : ("+algorithm+")");
			int rank = Ms.length;
			for(int i=0;i<rank;i++){
				for(int j=0;j<rank;j++){
					pw.println(termIds.get(i)+","+termIds.get(j)+":"+Ms[i][j]);
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
		int rank = Ms.length;
		logger.info("the rank of ms is "+rank);
		for(int i=0;i<rank;i++){
			for(int j=0;j<rank;j++){
				logger.info(Ms[i][j]+" ");
			}
			logger.info("\r");
		}
	}
	
	public void printOntologyToScreen(){
		
	}
	
	public void printOntologyToFile(File file){
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			int rank = ontology.length;
			pw.println("digraph vis{");
			pw.println("rankdir=LR;");
			//pw.print("the rank of Ontology is "+rank + "\r");
			
			for(int i=0;i<rank;i++){
				for(int j=0;j<rank;j++){
					if(ontology[i][j]==Relations.BROADER)
						pw.println(termNames.get(i)+"->"+termNames.get(j)+";");
								//+" [ label = \"B\"];");
					//if(ontology[i][j]==Relations.RELATED)
					//	pw.println(terms.get(i)+"->"+terms.get(j));
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
	 * @deprecated use LSHL() instead
	 * @author Shockley Xiang Li
	 */
	private void RLSHLonce(int current, List<Integer> V, List<Integer> Vh, List<Integer> parentFound, int i){
		List<Integer> Vtemp = findMcSimilarTerms(current);
		List<Integer> VtempNotBroader = new ArrayList<Integer>();
		for(Integer c : Vtemp){
			if(!broaderConditionHolds(current, c)){
				VtempNotBroader.add(c);
			}else if(parentFound.contains(c) || Vh.contains(c)){
				//assure that each has only one broader node
				//and there is no loop: what has been a father before, can't be a child
				VtempNotBroader.add(c);
			}else{
				//write down those broader relations
				ontology[current][c] = Relations.BROADER;
				//keep notes of the already-find-my-parent orphans
				parentFound.add(c);
			}
		}
		
		Vtemp.removeAll(VtempNotBroader);
		V.addAll(Vtemp);
		//Vh.addAll(Vtemp);
		V.remove((Integer) current);
		VtempNotBroader = null;
		Vtemp = null;
		
		//gurantee that the top element has never been handled
		while(V.size()>=1 && Vh.contains(V.get(0))){
			V.remove(0);
		}
		if(V.isEmpty()){
			logger.info("at last i = "+ i);
			return;
		}
		while(!(i<I)){
			i+=1;
			Vh.add(V.get(0));
			RLSHLonce(V.get(0), V, Vh, parentFound, i);
		}
	}
	
	/**
	 * @deprecated use LSHL() instead
	 * @author Shockley Xiang Li
	 */
	private void RLSHL(){
		int current = root;
		int i = 1;
		List<Integer> V = new ArrayList<Integer>();
		List<Integer> Vh = new ArrayList<Integer>();
		List<Integer> parentFound = new ArrayList<Integer>();
		RLSHLonce(current, V, Vh, parentFound, i);
	}
	
	/**
	 * lshl
	 * @author Shockley Xiang Li
	 */
	private void LSHL(){
		int current = root;
		List<Integer> V = new ArrayList<Integer>();
		List<Integer> Vh = new ArrayList<Integer>();
		List<Integer> haveParent = new ArrayList<Integer>();
		List<Integer> Vtemp;
		List<Integer> VtempNotBroader;
		int i =0;
		V.add(current);
		Vh.add(current);
		while(i<I){
			i += 1;
			Vtemp = findMcSimilarTerms(current);
			VtempNotBroader = new ArrayList<Integer>();
			for(Integer c : Vtemp){
				if(!broaderConditionHolds(current, c)){
					VtempNotBroader.add(c);
				}else if(haveParent.contains(c) || Vh.contains(c)){
					//assure that each has only one broader node
					//and there is no loop: what has been a father before, can't be a child
					VtempNotBroader.add(c);
				}else{
					//write down those broader relations
					ontology[current][c] = Relations.BROADER;
					//keep notes of the already-find-my-parent orphans
					haveParent.add(c);
				}
			}
			//remove those do not satisfy the broader condition
			Vtemp.removeAll(VtempNotBroader);
			//move those satisfy to V
			V.addAll(Vtemp);
			//remove current from v
			V.remove((Integer) current);
			//release memory
			VtempNotBroader = null;
			Vtemp = null;
			//gurantee that the top element has never been handled
			//other wise skip-and-delete
			while(V.size()>=1 && Vh.contains(V.get(0))){
				V.remove(0);
			}
			//halt when v is empty
			if(V.isEmpty()){
				logger.info("at last i = "+ i);
				return;
			}
			current = V.get(0);
			//every current-root is written down in Vh
			Vh.add(current);
		}
	}
	
	
	/**
	 * GSHL
	 * @author Shockley Xiang Li
	 * @param current
	 */
	private void GSHL(){
		int current = root;
		int i = 1;
		List<Integer> V = new ArrayList<Integer>();
		List<Integer> Vh = new ArrayList<Integer>();
		List<Integer> haveParent = new ArrayList<Integer>();
		V.add(current);
		Vh.add(current);
		while(i<I){
			List<Integer> Vtemp = this.findMcSimilarTerms(current);
			List<Integer> siblings = findSiblings(current);
			for(Integer ni : Vtemp){
				if(!broaderConditionHolds(current, ni)){
					continue;
				}else if(haveParent.contains(ni)){
					//assure that each has only one broader node
					continue;
				}else{
					boolean isBroader = true;
					//no sibling then only related is marked
					if(siblings==null || siblings.size()==0){
						ontology[current][ni] = Relations.RELATED;
						isBroader = false;
					}else{
						for(Integer s : siblings){
							//if it's more similar(less diverged) to a sibiling
							if(Ms[current][ni] >= Ms[s][ni]){
								ontology[current][ni] = Relations.RELATED;
								isBroader = false;
								break;
							}
						}
						if(isBroader){
							ontology[current][ni] = Relations.BROADER;
							haveParent.add(ni);
						}
					}
					V.add(ni);
					i++;
				}
			}
			//remove current from v
			V.remove((Integer) current);
			//gurantee that the top element has never been handled
			//other wise skip-and-delete
			while(V.size()>=1 && Vh.contains(V.get(0))){
				V.remove(0);
			}
			//halt when v is empty
			if(V.isEmpty()){
				logger.info("at last i = "+ i);
				break;
			}
			current = V.get(0);
			//every current-root is written down in Vh
			Vh.add(current);
		}
	}
	
	/**
	 * the assumption shall be kept that each has only one parent 
	 * @author Shockley Xiang Li
	 * @param current
	 * @return
	 */
	public List<Integer> findSiblings(int current){
		List<Integer> parents = new ArrayList<Integer>();
		List<Integer> siblings = new ArrayList<Integer>();
		Integer parent;
		for(int i = 0; i<ontology.length;i++){
			if(ontology[i][current] == Relations.BROADER)
				parents.add((Integer) i);
		}
		//some has no sibling
		if(parents.size()!=1)
			return siblings;
		//the parent is found
		parent = parents.get(0);
		//find the children of this parent
		for(int i = 0; i<ontology.length;i++){
			if(ontology[parent][i] == Relations.BROADER)
				siblings.add((Integer) i);
		}
		return siblings;
	}
	
	/**
	 * @author Shockley Xiang Li
	 * @param rootTerm
	 * @param global : true= GSHL, 
	 */
	public void init(int rootTerm, String algo){
		algorithm = algo;
		int docNum = docTopicDistrib.length;
		ontology = new Relations [docNum][docNum];
		root = rootTerm;
		computeMsKL(algorithm);
		//broaderCount(root);
	}
	
	
	/**
	 * Output all the semanticly-similar edges, according to Ms
	 * Filtered with THs/THd
	 * Called after init()
	 * This algorithm is not in the original TKDE paper
	 * @author Shockley Xiang Li
	 */
	public void printNetworkToFile(File file){
		List<String> topicStrings  =  new ArrayList<String> ();
		Session session = hs.getSession();
		Transaction tx = session.beginTransaction();
		String hql;
		for(Long cid : termIds){
			hql = "from Term c where c.id = "+cid;
			List<Term> terms = session.createQuery(hql).list();
			if(terms.size()!=1 || terms.get(0) == null){
				logger.info("terms.size()!=1 return");
				return;
			}
			String topic = terms.get(0).getName();
			//acording to DOT language given in the 
			topic = "\""+topic+"\"";
			topicStrings.add(topic);
		}
		tx.commit();
		session.close();
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			pw.println("graph vis{");
			pw.println("rankdir=LR;");
			//pw.print("the rank of Ontology is "+rank + "\r");
			int docNum = Ms.length;
			for(int i=0;i<docNum;i++){
				for(int j=i+1;j<docNum;j++){
					if(Ms[i][j]<THd){
						pw.println(topicStrings.get(i)+"--"+topicStrings.get(j)+";");
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
	 * the possible relations between term a and b
	 * @author Shockley Xiang Li
	 * @param a
	 * @param b
	 * @return the relation of term a & b, a is (similar with)/(broader than) b
	**/
	public boolean broaderConditionHolds(int a, int b){
		if(algorithm.equals("js")){
			if(Ms[a][b] < THd && KL[a][b]-KL[b][a]<THn){
				return true;
			}
		}else if(algorithm.equals("cosine")){
			if(Ms[a][b] > THs && KL[a][b]-KL[b][a]<THn){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @author Shockley Xiang Li
	 * @param c : the current term
	 * @return
	 */
	public List<Integer> findMcSimilarTerms(int c){
		List<Integer> toReturn = new ArrayList<Integer>();
		double [] theRow = Ms[c];
		IDSorter [] sorters = new IDSorter[theRow.length];
		for(int i =0; i < sorters.length; i++){
			sorters[i] = new IDSorter(i, theRow[i]);
		}
		sorters = selectionSort(sorters);
		if(algorithm.equals("js")){
			//most similar means the least diverged
			for(int i=sorters.length-1; i>=sorters.length-Mc; i--){
				int s = sorters[i].getID();
				if(s!=c)
					toReturn.add(s);
			}
		}else if(algorithm.equals("cosine")){
			//most similar
			for(int i=0; i<Mc; i++){
				int s = sorters[i].getID();
				if(s!=c)
					toReturn.add(s);
			}
		}
		return toReturn;
	}

	/**
	 * @author Shockley Xiang Li
	 * @param vec
	 * @return
	 */
	public static IDSorter [] selectionSort(IDSorter [] vec){
		IDSorter [] toReturn = null;
		for(int i =0; i < vec.length; i++){
			for(int j=i+1; j < vec.length; j++){
				if(vec[j].getWeight()>vec[i].getWeight()){
					//swap(vec, i, j);
					IDSorter tmp = vec[i];
					vec[i] = vec[j];
					vec[j] = tmp;
				}
			}
		}
		toReturn = vec;
		return toReturn;
	}
	
	/** 
	 * store the ontology into influx.ontology.sql
	 * @author Shockley Xiang Li
	 **/
	public void storeOntologyToDB(){
		Session session = hs.getSession();
		Transaction tx = session.beginTransaction();
		String sql = "truncate table onto_network";
		SQLQuery query = session.createSQLQuery(sql);
		query.executeUpdate();
		tx.commit();
		
		int termNum = ontology.length;
		for(int i=0;i<termNum;i++){
			for(int j=0;j<termNum;j++){
				if(ontology[i][j]!=null&&ontology[i][j].equals(Relations.BROADER)){
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
	 * @author Shockley Xiang Li
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String ontology = "D:\\work\\experiment\\ontology\\TKDE\\ohloh\\ohloh.ontology";
		File ontologyFile = new File(ontology);
		TkdeLdaOntology onto = new TkdeLdaOntology();
		onto.loadDataFromDb();
		logger.info("data loaded");
		int root = -1;
		List<Long> ids= onto.getTermIds();
		for(Long id : ids){
			//295 stands is the id of 'Communications'
			//368 stands for "topics" (wikipedia software doc)
			//271 is the 'software' in ohloh
			if(id == 271){
				root = ids.indexOf(id);
				logger.info("root = " + root);
				break;
			}
		}
		onto.init(root, "cosine");
		//onto.printNetworkToFile(networkFile);
		onto.LSHL();
		onto.printOntologyToFile(ontologyFile);
		onto.storeOntologyToDB();
	}

	public List<Long> getTermIds() {
		return termIds;
	}
}
