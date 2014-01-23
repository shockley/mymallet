/**
 * @Shockley Xiang Li
 * 2012-3-23
 */
package influx.ontology;

import java.io.File;
import java.util.Arrays;

import cc.mallet.topics.LDA;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
import cc.mallet.util.Randoms;

/**
 * @author Shockley
 * 
 */
public class ParallelLdaRecorder {
	
	/**
	 * doc-topic distribution, i.e. theta(d)
	 * indexed by <doc,topic>, the value is the probability p=0~1
	 */
	private double[][] docTopicDistrib;
	/**
	 * topic word distribution,
	 * indexed by <topic,word>, the value is the probability p=0~1
	 */
	private double[][] topicWordDistrib;
	/**
	 * indexed by <doc,token>, the value is the topic index k=1:K
	 */
	private int[][] tokenTopicAssign;
	
	
	/**
	 *  @param topicModel  the topic model we are using
	 */
	public void recordDocumentTopics (ParallelTopicModel topicModel)	{
		int corpusSize = topicModel.getData().size();
		int topicNum = topicModel.getNumTopics();
		
		double[][] tempDTdistrib = new double[corpusSize][topicNum];
		//TODO:
		//double[][] tempTWdistrib;
		
		int docLen;
		int[] topicCounts = new int[ topicNum ];

		//in this function, topics will never be sorted
		IDSorter[] topics = new IDSorter[ topicNum ];
		for (int topic = 0; topic < topicNum; topic++) {
			// Initialize the sorters with dummy values
			topics[topic] = new IDSorter(topic, topic);
		}

		

		for (int doc = 0; doc < corpusSize; doc++) {
			//topic distrib of this doc
			double [] topicDistrib = new double [topicNum]; 
			
			LabelSequence topicSequence = (LabelSequence) topicModel.getData().get(doc).topicSequence;
			int[] currentDocTopics = topicSequence.getFeatures();

			docLen = currentDocTopics.length;

			// Count up the tokens
			for (int token=0; token < docLen; token++) {
				topicCounts[ currentDocTopics[token] ]++;
			}

			// And normalize
			for (int topic = 0; topic < topicNum; topic++) {
				topics[topic].set(topic, (float) topicCounts[topic] / docLen);
			}
			
			//Arrays.sort(sortedTopics);

			for (int i = 0; i < topicNum; i++) {
				double topicvalue= topics[i].getWeight();
				topicDistrib[i] = topicvalue;
			}
			
			tempDTdistrib[doc] = topicDistrib;
			Arrays.fill(topicCounts, 0);
		}
		docTopicDistrib = tempDTdistrib;
	}

	
	/**
	 * getters:
	 */
	public double[][] getTopicWordDistrib() {
		return topicWordDistrib;
	}

	public int[][] getTokenTopicAssign() {
		return tokenTopicAssign;
	}

	public double[][] getDocTopicDistrib() {
		return docTopicDistrib;
	}
	
	
	
	/**
	 * 
	 * @author Shockley Xiang Li
	 * @param args:
	 * 	args[0]  training file, default = 200
	 *  args[1]  iteration num
	 *  args[2]  topwords to print out
	 * @throws java.io.IOException
	 */
	public void ldaAndRecord (String[] args) throws java.io.IOException
	{
		try {
			
			InstanceList training = InstanceList.load (new File(args[0]));
			
			int numTopics = args.length > 1 ? Integer.parseInt(args[1]) : 200;
			
			ParallelTopicModel lda = new ParallelTopicModel (numTopics, 50.0, 0.01);
			lda.printLogLikelihood = true;
			lda.setTopicDisplay(50, 7);
			lda.addInstances(training);
			
			lda.setNumThreads(Integer.parseInt(args[2]));
			lda.estimate();
			ParallelTopicModel.logger.info("printing state");
			lda.printState(new File("state.gz"));
			ParallelTopicModel.logger.info("finished printing");
			this.recordDocumentTopics(lda);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
