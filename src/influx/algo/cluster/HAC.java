/**
 * @Shockley Xiang Li
 * 2012-5-25
 */
package influx.algo.cluster;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;
import influx.ontology.TermNetworkBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * A java implementation of Hierarchical Agglomerative Clustering for Building tag ontology
 * This function is also prototype-based, i.e., 
 * Input : Divergence Score (0~1) Between each element pair
 * Output : A tag tree in its adjancent-matrix representation
 * @author Shockley
 *
 */
public class HAC {
	public static Logger logger = Logger.getLogger(HAC.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	public enum Medoid {CENTER, ENTROPY, TKDE};
	public enum Divergence {JS, KL, COSINE};
	public enum Promote {DEMOCRACY, NOBLE_ONLY};
	/**
	 * In this class, a tag is identified by its index in the divergence matrix
	 * so we can treat that index as its 'tag-id'!!!
	 * @param divergence : Divergence matrix for tags/terms
	 * @param entropy : entropy vector for tags/terms, each entropy is for one tag-topic dist
	 * @param centerMedoid : if centerness is used to decide the medoid, if false, entropy is used
	 * @param democracy : when merging two clusters, the chance of becoming a new medoid is not
	 * only limited to the two old medoids, but being public
	 */
	/**
	 * @param builder
	 * @param medoidtype
	 * @param promoteType
	 * @param JS
	 */
	public HAC(TermNetworkBuilder builder, Medoid medoidtype, Promote promoteType, Divergence divType) {
		super();
		this.builder = builder;
		this.medoidType = medoidtype;
		this.promoteType = promoteType;
		if(divType.equals(Divergence.JS))
			divergence = builder.getJS();
		else if(divType.equals(Divergence.KL))
			divergence = builder.getKL();
		else if(divType.equals(Divergence.COSINE))
			divergence = builder.getCOSINE();
	}
	
	/**
	 * The auxiliary variable,
	 * everything we need is in it,
	 * so this builder shall be initialzed beforehand
	 */
	private TermNetworkBuilder builder;
	
	/**
	 * Divergence matrix for tags/terms
	 */
	private double [][] divergence;
	
	/**
	 * if centerness is used to decide the medoid, if false, entropy is used
	 */
	private Medoid medoidType;
	
	/**
	 * when merging two clusters, the chance of becoming a new medoid is not
	 * only limited to the two old medoids, but being public
	 */
	private Promote promoteType;
	
	/**
	 * the in-process set of clusters, each element is a pair of <medoid,cluster> 
	 */
	private HashMap<Integer,int []> clusters;
	
	/**
	 * The output data:
	 * adjancent matrix for the DAG (here not necessarily a tree) of tags/terms
	 * an item [i][j] is true iffi in the DAG there is an edge i->j 
	 */
	private boolean [][] ontology;
	
	/**
	 * how many merges have been performed
	 */
	private int mergeCount;
	
	/**
	 * initialize each tag as a singleton cluster;
	 * init the DAG as a edgeless graph
	 * @author Shockley Xiang Li
	 */
	private void init(){
		int numOfTags = divergence.length;
		clusters = new HashMap<Integer, int []>();
		for(int i =0; i<numOfTags; i++){
			int [] singleton = {i};
			clusters.put(i, singleton);
		}
		ontology = new boolean [numOfTags][numOfTags];
		for(int i = 0; i<numOfTags; i++){
			for(int j = 0; j<numOfTags; j++){
				ontology[i][j] = false;
			}
		}
	}
	
	/**
	 * the 
	 * @author Shockley Xiang Li
	 */
	public void hierCluster(){
		mergeCount = 1;
		init();
		boolean hasMultiCluster = pickAndMerge();
		while(hasMultiCluster){
			mergeCount ++;
			hasMultiCluster = pickAndMerge();
		}
		logger.info("Totally "+ mergeCount +" merges has been operated!");
	}
	
	/**
	 * pick two/several clusters and merge them,
	 * distance between two clusters is the divergence of the two medoids,
	 * if only one cluster left, a merge won't be preformed
	 * called by the main function hierCluster()
	 * @author Shockley Xiang Li
	 */
	private boolean pickAndMerge(){
		Set<Integer> keysets = clusters.keySet();
		List<Integer> medoidIDs = new ArrayList<Integer>();
		medoidIDs.addAll(keysets);
		if(medoidIDs.size() == 1){
			return false;
		}
		double minDist = 10000.0;
		Integer toMergeAID = -1 , toMergeBID = -1;
		for(int i=0;i<medoidIDs.size();i++){
			for(int j=i+1;j<medoidIDs.size();j++){
				double dist = divergence[medoidIDs.get(i)][medoidIDs.get(j)];
				if(dist < minDist){
					minDist = dist;
					toMergeAID = medoidIDs.get(i);
					toMergeBID = medoidIDs.get(j);
				}
			}
		}
		if(toMergeAID==null || toMergeAID<0 || toMergeAID >= divergence.length){
			logger.error("problem detected!");
		}if(toMergeBID==null || toMergeBID<0 || toMergeBID >= divergence.length){
			logger.error("problem detected!");
		}
		mergeTwo(toMergeAID, toMergeBID);
		return true;
	}
	
	/**
	 * merge 2 clusters whose medoids are a & b,
	 * the ontology is also changed accordingly:
	 *  the newMedoid gets promoted as the new father,
	 * @author Shockley Xiang Li
	 * @param aID : medoid a's 'tag-id'
	 * @param bID : medoid b's 'tag-id
	 */
	private void mergeTwo(Integer aID, Integer bID){
		int [] clusterAIDs = clusters.get(aID);
		int [] clusterBIDs = clusters.get(bID);
		if(clusterAIDs==null || clusterBIDs==null){
			logger.error("problem detected!");
		}
		int len = clusterAIDs.length + clusterBIDs.length;
		int [] mergedIDs = new int [len];
		for(int i=0; i<clusterAIDs.length; i++){
			mergedIDs[i] = clusterAIDs[i];
		}
		for(int i=clusterAIDs.length; i<len; i++){
			mergedIDs[i] = clusterBIDs[i-clusterAIDs.length];
		}
		Integer newMedoidID = null;
		switch(medoidType){
		case CENTER:
			newMedoidID = getMedoidIdByCenter(mergedIDs, aID, bID);
			break;
		case ENTROPY:
			newMedoidID = getMedoidIdByEntropy(mergedIDs, aID, bID);
			break;
		case TKDE:
			newMedoidID = getTkdeMedoid(aID, bID);
			break;
		default:
			logger.error("unexpected medoid type!");
		}
		clusters.remove(aID);
		clusters.remove(bID);
		clusters.put(newMedoidID, mergedIDs);
		promote(newMedoidID, aID, bID);
	}
	
	
	/**
	 * Find a prototype/medoid as the hypernym tag,
	 * the medoid is the tag with smallest entropy
	 * @author Shockley Xiang Li
	 * @param cluster : all indeces of tags in the cluster
	 * @param medoidAID
	 * @param medoidBID
	 * @return index of medoid
	 */
	public int getMedoidIdByEntropy(int [] clusterIDs, Integer medoidAID, Integer medoidBID){
		double [] entropy = builder.getEntropy();
		double minEntropy = 1000.0;
		int medoidID = -1;
		for(int i=0; i<clusterIDs.length;i++){
			if(promoteType.equals(Promote.NOBLE_ONLY)){
				//only oldMedoidA, oldMedoidB get a chance
				if(clusterIDs[i]!=medoidAID && clusterIDs[i]!=medoidBID)
					continue;
			}
			if(entropy[clusterIDs[i]]<minEntropy){
				medoidID = clusterIDs[i];
				minEntropy = entropy[medoidID];
			}
		}
		if(medoidID==-1){
			logger.error("Unexpected : no medoid for this cluster!");
		}
		return medoidID;
	}
	
	/**
	 * Find a prototype/medoid as the hypernym tag,
	 * the medoid is the tag which resides nearest to the center
	 * notice, if it's not democracy, only the two old memoids can get a chance of being
	 * the medoid
	 * @author Shockley Xiang Li
	 * @param clusterIDs : all indeces of tags in the cluster
	 * @param medoidAID
	 * @param medoidBID
	 * @return index of medoid
	 */
	public int getMedoidIdByCenter(int [] clusterIDs, Integer medoidAID, Integer medoidBID){
		
		int minDist = 10000;
		int newMedoidID = -1;
		for(int i=0; i<clusterIDs.length;i++){
			int x = 0;
			if(promoteType.equals(Promote.NOBLE_ONLY)){
				//only oldMedoidA, oldMedoidB get a chance
				if(clusterIDs[i]!=medoidAID && clusterIDs[i]!=medoidBID)
					continue;
			}
			//formerly int j=i+1, wrong?
			for(int j=0; j<clusterIDs.length;j++)
				x += divergence[clusterIDs[i]][clusterIDs[j]];
			if(x < minDist){
				minDist = x;
				newMedoidID = clusterIDs[i];
			}
		}
		if(newMedoidID==-1){
			logger.error("Unexpected : no medoid for this cluster!");
		}
		return newMedoidID;
	}
	
	/**
	 * Using a tkde dkl manner to find a broader medoid between the two
	 * @author Shockley Xiang Li
	 * @param cluster : all indeces of tags in the cluster
	 * @param medoidAID
	 * @param medoidBID
	 * @return index of medoid
	 */
	public int getTkdeMedoid(Integer medoidAID, Integer medoidBID){
		double [][] KL = builder.getKL();
		int medoidID = -1;
		if(KL[medoidAID][medoidBID] < KL[medoidBID][medoidAID]){
			medoidID = medoidAID;
		}else{
			medoidID = medoidBID;
		}
		if(medoidID==-1){
			logger.error("Unexpected : no medoid for this cluster!");
		}
		return medoidID;
	}
	
	/**
	 * promote newLeader and rebuild the ontology,
	 * it is possible that newLeader == a or b
	 * @author Shockley Xiang Li
	 * @param mID : the new medoid
	 * @param aID : a,b are the two old medoids
	 * @param bID : a,b are the two old medoids
	 */
	private void promote(Integer mID, Integer aID, Integer bID){
		if(aID==bID)
			logger.error("this is it!");
		if(mID.equals(aID)){
			ontology[aID][bID] = true;
		}else if(mID.equals(bID)){
			ontology[bID][aID] = true;
		}else{
			int thenFatherOfM = -1;
			
			//find m's then father
			for(int i=0; i<divergence.length; i++){
				if(ontology[i][mID]==true){
					thenFatherOfM = i;
					//Assert only one father exists
					//detach the father
					ontology[thenFatherOfM][mID] = false;
					break;
				}
			}
			for(int i=0; i<divergence[mID].length; i++){
				if(ontology[mID][i] == true){
					//detach all m's former children
					ontology[mID][i] = false;
					//attach them to m's then father
					
					ontology[thenFatherOfM][i] = true;
					if(ontology[i][thenFatherOfM])
						logger.error("this is it!");
				}
			}
			
			if(ontology[aID][mID] || ontology[bID][mID])
				logger.error("this is it!");
			ontology[mID][aID] = true;
			ontology[mID][bID] = true;
		}
	}
	
	public void setDivergence(double [][] divergence) {
		this.divergence = divergence;
	}
	public double [][] getDivergence() {
		return divergence;
	}
	public void setOntology(boolean [][] ontology) {
		this.ontology = ontology;
	}
	public boolean [][] getOntology() {
		return ontology;
	}
	
	public static void main(String [] args){
		String network = "D:\\work\\experiment\\stackoverflow\\network";
		File networkFile = new File(network);
		TermNetworkBuilder onto = new TermNetworkBuilder();
		onto.loadDataFromDb();
		onto.computeJsKlEntropy();
		//use centerness, not democracy, JS
		HAC hac = new HAC(onto, Medoid.CENTER, Promote.DEMOCRACY, Divergence.JS);
		hac.hierCluster();
		onto.printOntologyToFile(hac, networkFile);
		//onto.setNetwork(hac.ontology);
		//onto.storeNetworkToDB();
	}
}
