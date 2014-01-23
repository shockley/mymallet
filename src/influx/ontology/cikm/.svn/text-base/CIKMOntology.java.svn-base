/**
 * @Shockley Xiang Li
 * 2012-6-21
 */
package influx.ontology.cikm;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Shockley
 *
 */
public class CIKMOntology {
	public static Logger logger = Logger.getLogger(CIKMOntology.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	private Session session;
	private Transaction tx;
	private String sql;
	private Query query;
	private List<String> popularTags;
	/**
	 * the corresponding term ids
	 */
	private List<Integer> tagTermIds;
	
	//parameters
	public static double min_sup = 0.00001;
	public static double min_conf = 0.015;
	public static double nameda = 0.95;
	public static int max_iteration = 1000;
	public static double diff_threshold = 0.001;
	public static File gVizFile = new File("D:\\work\\experiment\\ontology\\PAPER\\CIKM\\ohloh.gviz");
	
	

	private HashMap<String,List<Integer>> tag2ProjIDs;
	private List<TreeNode> ontologies = new ArrayList<TreeNode>();
	/**
	 * indexed by the tag index of the list popularTags
	 */
	
	private double [][] confidence;
	private double [][] support;
	private double [] generalScore;
	private int [] ranks; // the rank starts with 0
	
	
	/**
	 * @author Shockley Xiang Li
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void readPopularTags(){
		List<String> tags = new ArrayList<String>();
		List<Integer> termIDs = new ArrayList<Integer>();
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select id,name from onto_terms";
		query = session.createSQLQuery(sql);
		ScrollableResults results = query.scroll();
		while(results.next()){
			Integer id = (Integer) results.get(0);
			String s = (String) results.get(1);
			if(s!=null && id !=null){
				tags.add(s);
				termIDs.add(id);
			}
		}
		popularTags = tags;
		tagTermIds = termIDs;
		logger.info(tags.size() + " tags read!");
	}
	
	/**
	 * @author Shockley Xiang Li
	 * @param tag
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> readProjectListByTag(String tag){
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select proj_id from ohloh_tagged where description = '"+tag+"'";
		query = session.createSQLQuery(sql);
		List<Integer> temp = query.list();
		List<Integer> toReturn = new ArrayList<Integer>();
		if(temp!=null && temp.size()>0){
			for(Integer s:temp){
				if(s!=null){
					toReturn.add(s);
				}
			}
		}
		tx.commit();
		session.close();
		return toReturn;
	}
	
	public void readAllNeeded(){
		tag2ProjIDs = new HashMap<String,List<Integer>>();
		readPopularTags();
		for(String tag : popularTags){
			List<Integer> pIDs = readProjectListByTag(tag);
			if(tag2ProjIDs.containsKey(tag))
				logger.error("why duplicate tag exists?");
			tag2ProjIDs.put(tag, pIDs);
		}
		logger.info("all read!");
	}
	
	/**
	 * the confidence between tag a and b
	 * @author Shockley Xiang Li
	 * @param ta
	 * @param tb
	 * @return
	 */
	private double confidence(String ta, String tb){
		List<Integer> A = this.tag2ProjIDs.get(ta);
		return (double)support(ta,tb) / A.size();
	}
	
	private double support(String ta, String tb){
		List<Integer> A = this.tag2ProjIDs.get(ta); 
		List<Integer> B = this.tag2ProjIDs.get(tb);
		if(A==null||B==null)
			logger.error("the tag is wrong");
		int intersect = CollectionUtils.intersection(A, B).size();
		return (double)intersect;
	}
	
	private void calcConfidenMatrix(){
		int l = popularTags.size();
		double [][] confidences = new double[l][l];
		for(int i=0;i<l;i++){
			for(int j=0;j<l;j++){
				confidences[i][j] = support[i][j]/support[i][i];
			}
		}
		confidence = confidences;
		logger.info("confidences calculated!");
	} 
	
	private void calcSupportMatrix(){
		int l = popularTags.size();
		double [][] supports = new double[l][l];
		for(int i=0;i<l;i++){
			for(int j=0;j<l;j++){
				supports[i][j] = support(popularTags.get(i), popularTags.get(j));
			}
		}
		support = supports;
		logger.info("supports calculated!");
	} 
	
	public double[][] getConfidence() {
		return confidence;
	}

	/**
	 * rank according to general score,
	 * the ranks are kept in ranks
	 * @author Shockley Xiang Li
	 */
	private void rankTags(){
		int size = popularTags.size();
		double [] temp = new double [size];
		for(int i=0; i<size; i++){
			temp[i] = generalScore[i];
		}
		
		//rank the general score
		double max;
		ranks = new int [temp.length]; //the first is ranked as 0
		int index;
		for(int j=0;j<temp.length;j++){
			index = -1;
			max = -1.0;
			for(int i=0;i<temp.length;i++){
				if(temp[i]>max){
					max = temp[i];
					index = i;
				}
			}
			temp[index] = -1.0;
			ranks[index] = j;
		}
		
		logger.info("tags ranked");
	}
	
	public void buildOntology(){
		readAllNeeded();
		calcSupportMatrix();
		calcConfidenMatrix();
		calcGeneralScore();
		rankTags();
		ontologyConstruction();
		
	}
	
	
	private void calcGeneralScore() {
		CIKMRandomWalk rw = new CIKMRandomWalk();
		generalScore = rw.generalScore(confidence, nameda, max_iteration, diff_threshold);
		/*for(int i =0; i<popularTags.size(); i++){
			logger.info(popularTags.get(i)+" "+generalScore[i]);
		}*/
		logger.info("general score calced");
	}

	
	/**
	 * @author Tao Wang
	 * @param support
	 * @param confidence
	 * @param min_sup
	 * @param min_conf
	 * @param node_ranks : the int array in which the index is the rank and the value is the tag id
	 */
	private void ontologyConstruction(){
		ArrayList<TreeNode> roots = new ArrayList<TreeNode>();
		for(int node_rank:ranks){
			TreeNode node = new TreeNode();
			node.setID(node_rank);
			
			double max_conf = 0;
			int node_id = node.getID();
			int parent_position = -1;
			for(int i=0;i<roots.size();i++){
				int root_id = roots.get(i).getID();
				if(confidence[node_id][root_id]>min_conf && support[node_id][root_id]>min_sup){
					if(confidence[node_id][root_id]>max_conf){
						int flag=1;
						ArrayList<Integer> ancestors = roots.get(i).getAncestors();
						if(ancestors!=null){
							for(int ancestor: ancestors){
								if(support[node_id][ancestor]<min_sup || confidence[node_id][ancestor]<min_conf){
									flag=0;
								}
							}
						}
						
						if(flag==1){
//							node.setParent(roots.get(i));
//							roots.get(i).getSons().add(node);
							parent_position = i;
							max_conf = confidence[node_id][root_id];
						}
					}
				}
			}
			if(parent_position ==-1){
				roots.add(node);
				//System.out.println("Add root to the roots"+ node.getID());
			}else{
				roots.add(node);
				node.setParent(roots.get(parent_position));
				roots.get(parent_position).setSons(node);
			}
		}
		ontologies = roots;
		logger.info("ontology constructed!");
	}

	/**
	 * print the ontology
	 * @author Shockley Xiang Li
	 */
	private void storeOntology(){
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "truncate table onto_network";
		query = session.createSQLQuery(sql);
		query.executeUpdate();
		tx.commit();
		session.close();
		for(TreeNode root:ontologies){
			ArrayList<TreeNode> sons = root.getSons();
			session = hs.getSession();
			tx = session.beginTransaction();
			for(TreeNode son:sons){
				int from = tagTermIds.get(root.getID());
				int to = tagTermIds.get(son.getID());
				sql = "insert into onto_network set from_term = "+from+", to_term = "+to;
				query = session.createSQLQuery(sql);
				query.executeUpdate();
			}
			tx.commit();
			session.close();
		}
		logger.info("ontology stored to onto_network table");
	}
	
	/**
	 * print the ontology
	 * @author Shockley Xiang Li
	 */
	private void printToGraphViz(){
		PrintWriter pw;
		try {
			pw = new PrintWriter(gVizFile);
			pw.println("digraph vis{");
			pw.println("rankdir=LR;");
			for(TreeNode root:ontologies){
				ArrayList<TreeNode> sons = root.getSons();
				for(TreeNode son:sons){
					pw.println("\""+popularTags.get(root.getID())
							+"\"->\""+popularTags.get(son.getID())+"\";");
				}
			}
			pw.println("}");
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("ontology printed to file");
	}
	
	public static void main(String [] args){
		CIKMOntology onto = new CIKMOntology();
		onto.buildOntology();
		onto.printToGraphViz();
		onto.storeOntology();
	}


	public int [] getRanks() {
		return ranks;
	}
	public double[][] getSupport() {
		return support;
	}
	public List<String> getPopularTags() {
		return popularTags;
	}
}
