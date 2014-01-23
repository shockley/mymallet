package edu.nudt.influx.lda;
/*
 * @author WangTao
 * 澶氭爣绛綥-LDA绠楁硶瀹炵幇
 * 鍦ㄨ绠楁祴璇曢泦涓瘝鐨勪富棰樻鐜囨椂锛岃�铏戣缁冮泦涓�numIterations-burnIn)涔嬮棿鐨勮瘝-涓婚鐨勮祴鍊肩患鍚堝潎鍊兼儏鍐�
 * 鍚屾椂锛屽湪鑰冭檻涓婚鐢熸垚璇嶇殑鏃跺�鐢卞涓富棰樼‘瀹氳瘝鑰屼笉浠呬粎鏄竴涓富棰樼‘瀹氫竴涓瘝
 * 姝ょ被涓昏鍒╃敤LDA杩涜璁粌锛屽緱鍒皐ord-label鐨勫垎甯冿紝骞跺皢鍏舵墦鍗板嚭鏉�
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;
import edu.nudt.influx.Utility.HashProcessor;


public class MultiLabeledLDA implements Serializable {

	int numTopics; // Number of topics to be fit
	public double alpha; // Dirichlet(alpha,alpha,...) is the distribution over
							// topics
	public double beta; // Prior on per-topic multinomial distribution over
						// words
	public int burnIn;//number of iteration after which the algorithm will be stable and sampling can begin;

	double tAlpha;
	double vBeta;
	InstanceList ilist; // the data field of the instances is expected to hold a
						// FeatureSequence
	InstanceList tag_ilist;
	int numLabels;//number of distinctive labels of all documents
		
	int[][][] topics; // indexed by <document index, sequence index>
	
	int numTypes;// number of distinguish words over all documents
	
	int numTokens;// number of words over all documents including duplicate
					// words
	
	int differentTokens;
	public HashProcessor hp = new HashProcessor();
	
	HashMap<Integer,Integer>[] docTopicCounts; // indexed by <document index, topic index>
	HashMap<Integer,Integer>[] docTopicCountsSamples;
	
	HashMap<Integer,Integer>[] typeTopicCounts; // indexed by <feature index, topic index>
	HashMap<Integer,Double>[] typeTopicCountsSamples;
	HashMap<Integer,Double>[] typeTopicCountsAverage;
	
	int[] tokensPerTopic; // indexed by <topic index>
	int[] tokensPerTopicSamples;
	
	int numIterations;
	
	int[][] docTopicValue;//indexed by<document index,label index>
						  //this is used to store the label->topic mapping docTopicValue[i][j]=k means 
						  //that the j's label of document i is mapping to topic k;
	int[][] originalDocTopicValue;
	
	HashMap<Integer,Integer> type_hash = new HashMap<Integer,Integer>(); 
	String dtts = "F:\\Experiment\\LDA\\FmResult\\result\\12_22\\fm_Token_Topic_Assignment_3_2_1_";
	String dttp = "F:\\Experiment\\LDA\\FmResult\\result\\12_22\\fm_Token_Topic_posibility_3_2_1_";
	
	public MultiLabeledLDA(double alpha, double beta) {
		this.alpha = alpha;
		this.beta = beta;
	}

	public void estimate(InstanceList documents, InstanceList tag_documents,int numIterations,
			int showTopicsInterval, int outputModelInterval,
			String outputModelFilename, Randoms r,int burnIn) {
		ilist = documents.shallowClone();
		numTypes = ilist.getDataAlphabet().size();// get the distinctive words
													// in all documents
		tag_ilist = tag_documents.shallowClone();
		numLabels = tag_ilist.getDataAlphabet().size();// this number should be the topic number k
		this.numTopics = numLabels;
		this.tAlpha =alpha*numTopics;
		this.burnIn = burnIn;
		this.numIterations = numIterations;
		int numDocs = ilist.size();// get number of documents
		topics = new int[numDocs][][];
		
		docTopicCounts = new HashMap[numDocs];
		docTopicCountsSamples = new HashMap[numDocs];	
		hp.initial(docTopicCounts, numDocs);
		hp.initial(docTopicCountsSamples, numDocs);
		
		
		typeTopicCounts = new HashMap[numTypes];
		typeTopicCountsSamples = new HashMap[numTypes];
		hp.initial(typeTopicCounts, numTypes);
		hp.initial(typeTopicCountsSamples, numTypes);
		
		tokensPerTopic = new int[numTopics];
		tokensPerTopicSamples = new int[numTopics];
		vBeta = beta * numTypes;
		
		docTopicValue = new int[numDocs][];//record every document's topic id锛宻uch as docTopicValue[i][j] 
										   //means the j's label's corresponding topic id of document i 
		
		// Initialize with random assignments of tokens to topics and finish allocating this.topics and this.tokens
		int seqLen;
		FeatureSequence fs;
		FeatureSequence fs_label;
		for (int di = 0; di < numDocs; di++) {
			try {
				fs = (FeatureSequence) ilist.get(di).getData();
				fs_label=(FeatureSequence)tag_ilist.get(di).getData();
			} catch (ClassCastException e) {
				System.err
						.println("LDA and other topic models expect FeatureSequence data, not FeatureVector data.  "
								+ "With text2vectors, you can obtain such data with --keep-sequence or --keep-bisequence.");
				throw e;
			}
			docTopicValue[di] = new int[fs_label.size()];
			
			for(int li=0;li<fs_label.size();li++){
				docTopicValue[di][li]=fs_label.getIndexAtPosition(li);
			}	
			seqLen = fs.getLength();
			numTokens += seqLen;
			topics[di] = new int[seqLen][2];
			
			// Randomly assign tokens to topics
			for (int si = 0; si < seqLen; si++) {
				int topic0,topic1;
				int position0,position1;
				int length = docTopicValue[di].length;
				position0 = r.nextInt(length);
				position1 = r.nextInt(length);
				if(length>3){
					while(position0==position1){
						position1 = r.nextInt(docTopicValue[di].length);
					}
				}
				topic0 = docTopicValue[di][position0];
				topic1 = docTopicValue[di][position1];
				topics[di][si][0] = topic0;// randomly assign topic to every words in document di;
				topics[di][si][1] = topic1;
				hp.addInteger(docTopicCounts[di], topic0);
				hp.addInteger(docTopicCounts[di], topic1);
				hp.addInteger(typeTopicCounts[fs.getIndexAtPosition(si)], topic0);
				hp.addInteger(typeTopicCounts[fs.getIndexAtPosition(si)], topic1);
				tokensPerTopic[topic0]++;
				tokensPerTopic[topic1]++;
			}
		}

		this.estimate(0, numDocs, numIterations, showTopicsInterval,
				outputModelInterval, outputModelFilename,r,burnIn);

	}

	/*
	 * Perform several rounds of Gibbs sampling on the documents in the given
	 * range.
	 */
	public void estimate(int docIndexStart, int docIndexLength,
			int numIterations, int showTopicsInterval, int outputModelInterval,
			String outputModelFilename, Randoms r,int burnIn) {
		for (int iterations = 0; iterations < numIterations; iterations++) {
			if (iterations % 10 == 0)
				System.out.print(iterations);
			else
				System.out.print(".");
			System.out.flush();
			if (showTopicsInterval != 0 && iterations % showTopicsInterval == 0
					&& iterations > 0) {
				System.out.println();
				printTopWords(5, false);
			}
			if (outputModelInterval != 0
					&& iterations % outputModelInterval == 0 && iterations > 0) {
				this.write(new File(outputModelFilename + '.' + iterations));
			}
			sampleTopicsForDocs(docIndexStart, docIndexLength, r,burnIn,iterations);
		}

	}
	
	
	/* One iteration of Gibbs sampling, across all documents. */
	public void sampleTopicsForDocs(int start, int length, Randoms r,int burnIn,int currentIteration) {
		assert (start + length <= docTopicCounts.length);
		// Loop over every word in the corpus
		for (int di = start; di < start + length; di++) {
			sampleTopicsForOneDoc((FeatureSequence) ilist.get(di).getData(),docTopicValue[di],
					topics[di], docTopicCounts[di],docTopicCountsSamples[di], r,burnIn,currentIteration);
		}
	}
		
	private void sampleTopicsForOneDoc(FeatureSequence oneDocTokens,int[] oneDocTopicValues,
			int[][] oneDocTopics, // indexed by seq position
			HashMap<Integer,Integer> oneDocTopicCounts, // indexed by topic index
			HashMap<Integer,Integer> oneDocTopicCountsSamples,
			Randoms r,int burnIn,int currentIteration) {
		HashMap<Integer,Integer> currentTypeTopicCounts = new HashMap<Integer,Integer>();
		int type, oldTopic0,oldTopic1,newTopic0,newTopic1;
		double topicWeightsSum;
		int docLen = oneDocTokens.getLength();
		int docTopicLen = oneDocTopicValues.length;
		double[] topicWeights = new double[docTopicLen];
		double tw;
		// Iterate over the positions (words) in the document
		for (int si = 0; si < docLen; si++) {
			type = oneDocTokens.getIndexAtPosition(si);
			oldTopic0 = oneDocTopics[si][0];
			oldTopic1 = oneDocTopics[si][1];
			// Remove this token from all counts
			tokensPerTopic[oldTopic0]--;
			tokensPerTopic[oldTopic1]--;
			hp.reduceInteger(oneDocTopicCounts, oldTopic0);
			hp.reduceInteger(oneDocTopicCounts, oldTopic1);
			hp.reduceInteger(typeTopicCounts[type], oldTopic0);
			hp.reduceInteger(typeTopicCounts[type], oldTopic1);
			// Build a distribution over topics for this token			
			Arrays.fill(topicWeights, 0.0);
			topicWeightsSum = 0.0;
			currentTypeTopicCounts = typeTopicCounts[type];
			for (int li = 0; li < docTopicLen; li++) {
				int ti = oneDocTopicValues[li];
				tw = ((hp.getCountInteger(currentTypeTopicCounts, ti) + beta) / (tokensPerTopic[ti] + vBeta))
						* ((hp.getCountInteger(oneDocTopicCounts,ti) + alpha)); // (/docLen-1+tAlpha) is constant across all
				topicWeightsSum += tw;
				topicWeights[li] = tw;
			}
			// Sample a topic assignment from this distribution
			int position0,position1;
			position0 = r.nextDiscrete(topicWeights, topicWeightsSum);
			position1 = r.nextDiscrete(topicWeights, topicWeightsSum);
			if(docTopicLen>3){
				while(position1==position0){
					position1 = r.nextDiscrete(topicWeights, topicWeightsSum);
				}
			}
			newTopic0 = oneDocTopicValues[position0];
			newTopic1 = oneDocTopicValues[position1];
			// Put that new topic into the counts
			oneDocTopics[si][0] = newTopic0;
			oneDocTopics[si][1] = newTopic1;
			hp.addInteger(oneDocTopicCounts, newTopic0);
			hp.addInteger(oneDocTopicCounts, newTopic1);
			hp.addInteger(typeTopicCounts[type], newTopic0);
			hp.addInteger(typeTopicCounts[type], newTopic1);
			tokensPerTopic[newTopic0]++;
			tokensPerTopic[newTopic1]++;
			if(currentIteration>burnIn-1)
				hp.addInteger(oneDocTopicCountsSamples,newTopic0);
				hp.addInteger(oneDocTopicCountsSamples,newTopic1);
				hp.addDouble(typeTopicCountsSamples[type], newTopic0);
				hp.addDouble(typeTopicCountsSamples[type], newTopic1);
				tokensPerTopicSamples[newTopic0]++;
				tokensPerTopicSamples[newTopic1]++;
		}
	}

	public Alphabet megdict(InstanceList ilist1, InstanceList ilist2) {// Alphabet
																		// datadic=null;
																		// boolean
																		// flag=false;
		Alphabet datadic1 = ilist1.getAlphabet();
		System.out.print(datadic1.size() + "\n");
		Alphabet datadic2 = ilist2.getAlphabet();
		System.out.print(datadic2.size() + "\n");
		for (int i = 0; i < datadic2.size(); i++) {
			datadic1.lookupIndex(datadic2.lookupObject(i).toString());
			// System.out.print(datadic1.size());
		}

		System.out.print(datadic1.size() + "\n");
		return datadic1;
	}

	public HashMap<Integer,Integer>[] getDocTopicCounts() {
		return docTopicCounts;
	}

	public HashMap<Integer,Integer>[] getTypeTopicCounts() {
		return typeTopicCounts;
	}

	public int[] getTokensPerTopic() {
		return tokensPerTopic;
	}
	
	public int[][][] getTopics(){
		return this.topics;
	}
	
	public int[][] getDocTopicValue(){
		return this.docTopicValue;
	}
    
	public int getDifferentTokens(){
		return this.differentTokens;
	}
	
	public int getTestTokens(){
		return this.numTokens;
	}
	public void getProjectTopics(InstanceList testIlist,int max_num_topic){
		FeatureSequence fs = (FeatureSequence)testIlist.get(0).getData();
		double[] totalTopic = new double[numLabels];
		int docAllLength = fs.getLength();
		for(int i=0;i<docAllLength;i++){
			String token = fs.getObjectAtPosition(i).toString();
			if(ilist.getDataAlphabet().contains(token)){				
				int oldTypeId = ilist.getDataAlphabet().lookupIndex(token);
				HashMap<Integer,Double> test = typeTopicCountsAverage[oldTypeId];
				Iterator it = test.keySet().iterator();
				while(it.hasNext()){
					int key = (Integer)it.next();
					double count = typeTopicCountsAverage[oldTypeId].get(key);
//					docTopicWeight[i][key]=count;
					totalTopic[key]+=count;
				}
			}
		}
		double[] totalTopicTemp = totalTopic.clone();
		Arrays.sort(totalTopicTemp);
		
		for(int i=0;i<max_num_topic; i++){
			double topic_count =totalTopicTemp[numLabels-1-i];
			for(int j=0;j<numLabels;j++){
				if(totalTopic[j]==topic_count){
					System.out.println(tag_ilist.getDataAlphabet().lookupObject(j)+"  "+topic_count);
					break;
				}
			}
		}				
	}
	public void printTypeTopicCountsAverage(String file){
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(new File(file)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		pw.print("\t\t");
		for(int t=0;t<numLabels;t++){
	    	pw.print("\t"+tag_ilist.getDataAlphabet().lookupObject(t).toString());
	    }
	    	
	    for(int i=0;i<numTypes;i++){
	    	String type = ilist.getDataAlphabet().lookupObject(i).toString();
	    	pw.print("\n"+type);
	    	Object[] keys = typeTopicCountsAverage[i].keySet().toArray();
	    	
	    	Arrays.sort(keys);	    	
	    	int k=0;
	    	for(int j=0;j<numLabels;j++){
	    		double mm;
	    		if(k<keys.length){
	    		    if((Integer)keys[k]==j){
	    			   k++;
	    			   mm=typeTopicCountsAverage[i].get(j);
	    			   
	    		    }else{
	    		    	mm=0;
	    		    }
	    		}else{
	    			mm=0;
	    		}
//	    		System.out.println("mm: "+mm);
	    		pw.print("\t\t"+mm);
	    	}
	    	pw.flush();
	    }	
	}
	
	public void printTypeTopicCountsAverage(String file,InstanceList test_ilist){
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(new File(file)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		pw.print("\t\t");
		for(int t=0;t<numLabels;t++){
	    	pw.print("\t"+tag_ilist.getDataAlphabet().lookupObject(t).toString());
	    }
	    int docLength = test_ilist.getDataAlphabet().size();
	    FeatureSequence fs_test = (FeatureSequence)test_ilist.get(0).getData();
	    for(int i=0;i<docLength;i++){
	    	String token = fs_test.getObjectAtPosition(i).toString();
	    	System.out.println("Token: "+token);
	    	if(ilist.getDataAlphabet().contains(token)){
	    		System.out.println("Contained");
	    		pw.print("\n"+token);
	    		int oldTypeID = ilist.getDataAlphabet().lookupIndex(token);
	    		Object[] keys = typeTopicCountsAverage[oldTypeID].keySet().toArray();
	    		Arrays.sort(keys);
	    		int k=0;
	    		for(int j=0;j<numLabels;j++){
	    			double mm=0.0;
	    			if(k<keys.length){
	    				if((Integer)keys[k]==j){
	    					k++;
	    					mm=typeTopicCountsAverage[oldTypeID].get(j);
	    				}
	    			}
//	    			System.out.println("mm: "+mm);
	    			pw.print("\t\t\t"+mm);
	    		}
	    		pw.flush();
	    	}else{
	    		pw.print("\n"+token);
	    		for(int j=0;j<numLabels;j++){
	    			pw.print("\t\t\t"+0.0);
	    		}
	    	}
	    }
	}
	
	public void getTypeTopicCountsAverage(){
		typeTopicCountsAverage = new HashMap[numTypes];
		hp.initial(typeTopicCountsAverage, numTypes);
	    for(int i=0; i<numTypes;i++){
	    	getTypeTopicCountsAverageForOne(i);
	    }
//	    for(int i=0;i<numTypes;i++){
//	    	printAverage(i);
//	    }
	}
    
	private void getTypeTopicCountsAverageForOne(int typeID){
		System.out.println(typeID +" typeID is processed!");
		Iterator direct_topic_iterator = typeTopicCountsSamples[typeID].keySet().iterator();
		typeTopicCountsAverage[typeID] = (HashMap<Integer, Double>)typeTopicCountsSamples[typeID].clone();
		while(direct_topic_iterator.hasNext()){
			int direct_topic_id = (Integer)(direct_topic_iterator.next());
			double topic_count = typeTopicCountsSamples[typeID].get(direct_topic_id);
			Iterator indirect_topic_iterator = typeTopicCountsSamples[direct_topic_id].keySet().iterator();
			double outweight_direct_topic = 0;
			while(indirect_topic_iterator.hasNext()){
				int indirect_topic_id = (Integer) indirect_topic_iterator.next();
				outweight_direct_topic += typeTopicCountsSamples[direct_topic_id].get(indirect_topic_id);
			}
			while(indirect_topic_iterator.hasNext()){
				int indirect_topic_id = (Integer)indirect_topic_iterator.next();
				double inweight = typeTopicCountsSamples[direct_topic_id].get(indirect_topic_id);
				double indirect_weight =topic_count*(inweight/outweight_direct_topic);
				double direct_weight = 0;
				if(typeTopicCountsAverage[typeID].containsKey(indirect_topic_id)){
					direct_weight =typeTopicCountsAverage[typeID].get(indirect_topic_id);
				}
				typeTopicCountsAverage[typeID].put(indirect_topic_id, direct_weight+indirect_weight);
			}
		}
		Iterator average_it = typeTopicCountsAverage[typeID].keySet().iterator();
		double total_average_count = 0;
		while(average_it.hasNext()){
			int average_key = (Integer)average_it.next();
			total_average_count+=(double)typeTopicCountsAverage[typeID].get(average_key);
		}
		Iterator average_it2 = typeTopicCountsAverage[typeID].keySet().iterator();
		while(average_it2.hasNext()){
			int average_key2 = (Integer)average_it2.next();
			double average_topic_count = typeTopicCountsAverage[typeID].get(average_key2);
			typeTopicCountsAverage[typeID].put(average_key2, average_topic_count/total_average_count);
		}
	}
	public void printAverage(int typeID){
		Iterator it = typeTopicCountsAverage[typeID].keySet().iterator();
		while(it.hasNext()){
			int key = (Integer)it.next();
			double count = typeTopicCountsAverage[typeID].get(key);
			System.out.println(typeID+"  "+key+"  "+count);
		}
	}
	public void printLabelWordDistribution(String file){
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(new File(file)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int typeID=0;typeID<numTypes;typeID++){
			String typeWord = ilist.getDataAlphabet().lookupObject(typeID).toString();
			Iterator<Integer> it = typeTopicCountsSamples[typeID].keySet().iterator();
		    while(it.hasNext()){
		    	int labelID = (Integer)it.next();
		    	double count = typeTopicCountsSamples[typeID].get(labelID);
		    	String labelWord = tag_ilist.getDataAlphabet().lookupObject(labelID).toString();
		    	if(count>100){
		    		pw.print(typeWord+"--->"+labelWord+" : "+count);
		    		pw.println();
		    	}
		    }
			
		}
	}
	
	//print the tokens that assigned with one topic to generate the topic text based on typeTopicCountSamples
	public void printOneTopicText(int burnIn,int iterationTime, int topic_id, String target_file_dir) throws IOException{
		String topic_name = tag_ilist.getDataAlphabet().lookupObject(topic_id).toString();
		String file = target_file_dir+topic_name;
		PrintWriter pw = new PrintWriter(new FileWriter(new File(file)));
		for(int i=0;i<typeTopicCountsSamples.length;i++){
			String word = ilist.getDataAlphabet().lookupObject(i).toString();
			HashMap<Integer,Double> word_topic_hash = typeTopicCountsSamples[i];
			if(word_topic_hash.containsKey(topic_id)){
				double double_count = word_topic_hash.get(topic_id)/(iterationTime-burnIn);
				int count = Integer.valueOf(new BigDecimal(double_count).setScale(0, BigDecimal.ROUND_HALF_UP).toString());
				for(int j=0;j<count;j++){
					pw.print(word+" ");
				}
			}
		}
		pw.flush();
		pw.close();
	}
	//print all the topics' text based on typeTopicCountsSample;
	public void printTopicText(int burnIn, int iterationTime, String target_file_dir){
		for(int i=0;i<tag_ilist.getDataAlphabet().size();i++){
			try {
				printOneTopicText(burnIn, iterationTime, i, target_file_dir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
	
	public void printTopWords(int numWords, boolean useNewLines) {
		class WordProb implements Comparable<Object> {
			int wi;
			double p;

			public WordProb(int wi, double p) {
				this.wi = wi;
				this.p = p;
			}

			public final int compareTo(Object o2) {
				if (p > ((WordProb) o2).p)
					return -1;
				else if (p == ((WordProb) o2).p)
					return 0;
				else
					return 1;
			}
		}

		WordProb[] wp = new WordProb[numTypes];
		for (int ti = 0; ti < numTopics; ti++) {
			for (int wi = 0; wi < numTypes; wi++)
				wp[wi] = new WordProb(wi,
						(double) (hp.getCountInteger(typeTopicCounts[wi],ti) + beta)
								/ (tokensPerTopic[ti] + vBeta));
			Arrays.sort(wp);
			if (useNewLines) {
				System.out.println("\nTopic " + tag_ilist.getDataAlphabet().lookupObject(ti).toString() );
				for (int i = 0; i < numWords; i++)
					System.out.println(ilist.getDataAlphabet()
							.lookupObject(wp[i].wi).toString()
							+ "\t" + wp[i].p);
				// System.out.println
				// (ilist.getDataAlphabet().lookupObject(wp[i].wi).toString());
			} else {
				System.out.print("Topic " + tag_ilist.getDataAlphabet().lookupObject(ti).toString() + ": ");
				for (int i = 0; i < numWords; i++)
					System.out.print(ilist.getDataAlphabet()
							.lookupObject(wp[i].wi).toString()
							+ "\t");
				System.out.println();
			}
		}
	}

	public void printToFile(int numWords, boolean useNewLines, File file) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		printToFile(numWords, useNewLines, pw);
		pw.close();
	}

	public void printToFile(int numWords, boolean useNewLines, PrintWriter out) {
		class WordProb implements Comparable<Object> {
			int wi;
			double p;

			public WordProb(int wi, double p) {
				this.wi = wi;
				this.p = p;
			}

			public final int compareTo(Object o2) {
				if (p > ((WordProb) o2).p)
					return -1;
				else if (p == ((WordProb) o2).p)
					return 0;
				else
					return 1;
			}
		}

		WordProb[] wp = new WordProb[numTypes];
		for (int ti = 0; ti < numTopics; ti++) {
			for (int wi = 0; wi < numTypes; wi++)
				wp[wi] = new WordProb(wi,(hp.getCountDouble(typeTopicCountsSamples[wi],ti))
						/ (tokensPerTopicSamples[ti]));
			
			Arrays.sort(wp);
			if (useNewLines) {
				out.println("\nTopic " + tag_ilist.getDataAlphabet().lookupObject(ti).toString() );
				for (int i = 0; i < numWords; i++)
					out.println(ilist.getDataAlphabet().lookupObject(wp[i].wi)
							.toString()
							+ " " + wp[i].p);
			} else {
				out.print("Topic " + tag_ilist.getDataAlphabet().lookupObject(ti).toString()  + ": ");
				for (int i = 0; i < numWords; i++)
					out.print(ilist.getDataAlphabet().lookupObject(wp[i].wi)
							.toString()
							+ " ");
				out.println();
			}
		}
	}

//	public void SprintToDatabase(int numWords, boolean useNewLines, File file) {
//		PrintWriter out = null;
//		Connection conn = null;
//		Statement stmt = null;
//		String word = null;
//		try {
//			out = new PrintWriter(new FileWriter(file));
//			DataBase.openDB();
//			conn = DataBase.getDBConection();
//			stmt = DataBase.getDBStatement(conn);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		} catch (SQLException e1) {
//			e1.printStackTrace();
//		}
//		class WordProb implements Comparable<Object> {
//			int wi;
//			double p;
//
//			public WordProb(int wi, double p) {
//				this.wi = wi;
//				this.p = p;
//			}
//
//			public final int compareTo(Object o2) {
//				if (p > ((WordProb) o2).p)
//					return -1;
//				else if (p == ((WordProb) o2).p)
//					return 0;
//				else
//					return 1;
//			}
//		}
//
//		WordProb[] wp = new WordProb[numTypes];
//		for (int ti = 0; ti < numTopics; ti++) {
//			for (int wi = 0; wi < numTypes; wi++)
//				wp[wi] = new WordProb(wi,
//						(double) (hp.getCountInteger(typeTopicCounts[wi],ti) + beta)
//								/ (tokensPerTopic[ti] + vBeta));
//			Arrays.sort(wp);
//			try {
//				if (useNewLines) {
//					out.println("\nTopic " + ti);
//					for (int i = 0; i < numWords; i++) {
//						String sem = ilist.getDataAlphabet()
//								.lookupObject(wp[i].wi).toString();
//						String sql = "select * from dict_f where first_sem='"
//								+ sem + "' order by count desc";
//						ResultSet rs = DataBase.DBExecuteQuery(stmt, sql);
//						if (rs.next()) {
//							word = rs.getString("w_c");
//						} else {
//							word = sem;
//						}
//						out.println(word + "\t" + wp[i].p);
//					}
//				} else {
//					out.print("Topic " + ti + ": ");
//					for (int i = 0; i < numWords; i++) {
//						String sem = ilist.getDataAlphabet()
//								.lookupObject(wp[i].wi).toString();
//						String sql = "select * from dict_f where first_sem='"
//								+ sem + "' order by count desc";
//						ResultSet rs = DataBase.DBExecuteQuery(stmt, sql);
//						if (rs.next()) {
//							word = rs.getString("w_c");
//						} else {
//							word = sem;
//						}
//						out.println(word + "\t" + wp[i].p);
//					}
//					out.println();
//				}
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//		}
//		out.close();
//		try {
//			DataBase.closeDBStatement(stmt);
//			DataBase.closeDBConnection(conn);
//			DataBase.closeDB();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}

	public void SprintToFile(int numWords, boolean useNewLines, File file) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		SprintToFile(numWords, useNewLines, pw);
		pw.close();
	}

	public void SprintToFile(int numWords, boolean useNewLines, PrintWriter out) {
		class WordProb implements Comparable<Object> {
			int wi;
			double p;

			public WordProb(int wi, double p) {
				this.wi = wi;
				this.p = p;
			}

			public final int compareTo(Object o2) {
				if (p > ((WordProb) o2).p)
					return -1;
				else if (p == ((WordProb) o2).p)
					return 0;
				else
					return 1;
			}
		}

		WordProb[] wp = new WordProb[numTypes];
		for (int ti = 0; ti < numTopics; ti++) {
			for (int wi = 0; wi < numTypes; wi++)
				wp[wi] = new WordProb(wi,
						(double) (hp.getCountInteger(typeTopicCounts[wi],ti) + beta)
								/ (tokensPerTopic[ti] + vBeta));
			Arrays.sort(wp);
			if (useNewLines) {
				out.println("\nTopic " + ti);
				for (int i = 0; i < numWords; i++)
					out.println(ilist.getDataAlphabet().lookupObject(wp[i].wi)
							.toString()
							+ "\t" + wp[i].p);
			} else {
				out.print("Topic " + ti + ": ");
				for (int i = 0; i < numWords; i++)
					out.print(ilist.getDataAlphabet().lookupObject(wp[i].wi)
							.toString()
							+ "\t");
				out.println();
			}
		}
	}

	public void printDocumentTopics(File f) throws IOException {
		printDocumentTopics(new PrintWriter(new FileWriter(f)));
	}

	public void printDocumentTopics(PrintWriter pw) {
		printDocumentTopics(pw, 0.0, -1);
		pw.close();
	}

	public void printDocumentTopics(PrintWriter pw, double threshold, int max) {
		pw.println("#doc source topic proportion ...");
		int docLen;
		double topicDist[] = new double[numTopics];
		for (int di = 0; di < topics.length; di++) {
			pw.print(di);
			pw.print(' ');
			if (ilist.get(di).getSource() != null) {
				pw.print(ilist.get(di).getSource().toString());
			} else {
				pw.print("null-source");
			}
			pw.print(' ');
			docLen = topics[di].length;
			for (int ti = 0; ti < numTopics; ti++)
				topicDist[ti] = (((double) hp.getCountInteger(docTopicCountsSamples[di],ti)) / (docLen*(numIterations-burnIn)));
			if (max < 0)
				max = numTopics;
			for (int tp = 0; tp < max; tp++) {
				double maxvalue = 0;
				int maxindex = -1;
				for (int ti = 0; ti < numTopics; ti++)
					if (topicDist[ti] > maxvalue) {
						maxvalue = topicDist[ti];
						maxindex = ti;
					}
				if (maxindex == -1 || topicDist[maxindex] < threshold)
					break;
				pw.print(tag_ilist.getDataAlphabet().lookupObject(maxindex).toString() + " " + topicDist[maxindex] + " ");
				topicDist[maxindex] = 0;
			}
			pw.println(' ');
		}
	}

	public void SprintDocumentTopics(File f) throws IOException {
		SprintDocumentTopics(new PrintWriter(new FileWriter(f)));
	}

	public void SprintDocumentTopics(PrintWriter pw) {
		SprintDocumentTopics(pw, 0.0, -1);
		pw.close();
	}

	public void SprintDocumentTopics(PrintWriter pw, double threshold, int max) {
		pw.println("#doc source topic proportion ...");
		int docLen;
		double topicDist[] = new double[numTopics];
		for (int di = 0; di < topics.length; di++) {
			pw.print(di);
			pw.print('\t');
			if (ilist.get(di).getSource() != null) {
				pw.print(ilist.get(di).getSource().toString());
			} else {
				pw.print("null-source");
			}
			pw.print('\t');
			docLen = topics[di].length;
			for (int ti = 0; ti < numTopics; ti++)
				topicDist[ti] = ((double) (hp.getCountInteger(docTopicCountsSamples[di],ti)/(numIterations-burnIn) + alpha) / (docLen + tAlpha));
			// 鎺掑簭
			if (max < 0)
				max = numTopics;
			for (int tp = 0; tp < max; tp++) {
				double maxvalue = 0;
				int maxindex = -1;
				for (int ti = 0; ti < numTopics; ti++)
					if (topicDist[ti] > maxvalue) {
						maxvalue = topicDist[ti];
						maxindex = ti;
					}
				if (maxindex == -1 || topicDist[maxindex] < threshold)
					break;
				pw.print(maxindex + "\t" + topicDist[maxindex] + "\t");
				topicDist[maxindex] = 0;
			}
			pw.println('\t');
		}
	}
	public void printTopicDocument(File pf) throws FileNotFoundException {
		FileOutputStream ou = new FileOutputStream(pf);
		PrintStream p = new PrintStream(ou);
		double[][] topicdoc = new double[numTopics][topics.length];
		double[] avg = new double[numTopics];
		for (int ti = 0; ti < numTopics; ti++) {
			avg[ti] = 0;
			for (int di = 0; di < topics.length; di++) {
				topicdoc[ti][di] = ((double) (hp.getCountInteger(docTopicCounts[di],ti) + alpha) / (topics[di].length + tAlpha));
				avg[ti] = avg[ti] + topicdoc[ti][di];
			}
			avg[ti] = avg[ti] / (topics.length);
		}

		for (int i = 0; i < numTopics; i++)
			p.println(avg[i]);
		p.close();

	}

	public void printTopicDocuments(File pf) throws FileNotFoundException {
		class WordProb implements Comparable<Object> {
			int wi;
			double p;

			public WordProb(int wi, double p) {
				this.wi = wi;
				this.p = p;
			}

			public final int compareTo(Object o2) {
				if (p > ((WordProb) o2).p)
					return -1;
				else if (p == ((WordProb) o2).p)
					return 0;
				else
					return 1;
			}
		}

		int doc = (topics.length > 10) ? 10 : topics.length;
		WordProb[][] wp = new WordProb[numTopics][topics.length];
		FileOutputStream ou = new FileOutputStream(pf);
		PrintStream p = new PrintStream(ou);
		for (int ti = 0; ti < numTopics; ti++) {
			for (int di = 0; di < topics.length; di++) {
				wp[ti][di] = new WordProb(
						di,
						((double) (hp.getCountInteger(docTopicCounts[di],ti) + alpha) / (topics[di].length + tAlpha)));
			}
			Arrays.sort(wp[ti]);
		}
		for (int ti = 0; ti < numTopics; ti++) {
			p.println("Topic " + ti);
			for (int di = 0; di < doc; di++) {
				p.println(ilist.get(wp[ti][di].wi).getSource().toString()
						+ "\t" + wp[ti][di].p);
			}
		}

		p.close();

	}

	public void printState(File f) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(f));
		printState(writer);
		writer.close();
	}

	public void printState(PrintWriter pw) {
		Alphabet a = ilist.getDataAlphabet();
		pw.println("#doc pos typeindex type topic");
		for (int di = 0; di < topics.length; di++) {
			FeatureSequence fs = (FeatureSequence) ilist.get(di).getData();
			for (int si = 0; si < topics[di].length; si++) {
				int type = fs.getIndexAtPosition(si);
				pw.print(di);
				pw.print(' ');
				pw.print(si);
				pw.print(' ');
				pw.print(type);
				pw.print(' ');
				pw.print(a.lookupObject(type));
				pw.print(' ');
				pw.print(topics[di][si]);
				pw.println();
			}
		}
	}
/**
 * print one specific document's tokens'topic assignment to see the process
 * @throws IOException 
 */
	public void printDocTokenTopicState(String dtts,int di) throws IOException{
		FileWriter fw = new FileWriter(new File(dtts),true);
		BufferedWriter bw = new BufferedWriter(fw);
		FeatureSequence fs = (FeatureSequence)ilist.get(di).getData();
		bw.newLine();
		for(int si=0;si<topics[di].length;si++){		
			String token = fs.getObjectAtPosition(si).toString();
			int ti0 = topics[di][si][0];
			int ti1 = topics[di][si][1];
			String topic0 = tag_ilist.getDataAlphabet().lookupObject(ti0).toString();
			String topic1 = tag_ilist.getDataAlphabet().lookupObject(ti1).toString();
			bw.append(token+"---<"+topic0+", "+topic1+">"+"\t");
			bw.flush();
		}
		fw.close();
		bw.close();
	}
	
	public void write(File f) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(f));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			System.err.println("Exception writing file " + f + ": " + e);
		}
	}

	// Serialization

	private static final long serialVersionUID = 1;

	public void PrintString(String out, File pf) {
		FileOutputStream ou = null;
		try {
			ou = new FileOutputStream(pf);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		PrintStream p = new PrintStream(ou);
		p.print(out);

	}

	public InstanceList getInstanceList() {
		return ilist;
	}

	public static void main(String[] args) throws IOException {
		InstanceList ilist = InstanceList.load(new File(
				"F:\\Experiment\\LDA\\OhlohExperimentFiles\\mallet\\files1.mallet"));
		InstanceList tag_ilist = InstanceList.load(new File(
				"F:\\Experiment\\LDA\\OhlohExperimentFiles\\mallet\\tag1.mallet"));
		int numTrainIterations = 10;
		int pr = 5;
		int numTopWords =20;
//		String target_file_dir="F:\\Experiment\\MLClassification4\\transformed_files\\feature_processed_files\\sf_testing_tag_text_files\\";
		System.out.println("Data loaded.");
	    MultiLabeledLDA lda = new MultiLabeledLDA(2,0.5);
		lda.estimate(ilist, tag_ilist, numTrainIterations, pr, 0, null, new Randoms(),8); 
	    lda.printTopWords(numTopWords, true);
		lda.printToFile(30, true, new File(
				"F:\\Experiment\\LDA\\OhlohExperimentFiles\\labelworddistribution\\ohloh3.topic"));
		lda.printDocumentTopics(new File(
				"F:\\Experiment\\LDA\\OhlohExperimentFiles\\labelworddistribution\\ohloh3.lda"));
//		lda.printTopicText(burnIn,numTrainIterations,target_file_dir);

	}
}
