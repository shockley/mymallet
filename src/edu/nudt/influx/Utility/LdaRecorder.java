/* Copyright (C) 2005 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

package edu.nudt.influx.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;

/**
 * built upon cc.mallet.topics.LDA, changed by Shockley, so this supports basic
 * incremental learning (with no additional vocabulary) add the function
 */

// Think about support for incrementally adding more documents...
// (I think this means we might want to use FeatureSequence directly).
// We will also need to support a growing vocabulary!

public class LdaRecorder implements Serializable {

	/**
	 * 
	 */
	private List<Long> termDocIds;
	/**
	 * doc-topic distribution, i.e. theta(d) indexed by <doc,topic>, the value
	 * is the probability p=0~1
	 */
	private double[][] oldDocTopicDistrib;
	/**
	 * doc-topic distribution of new docs, i.e. theta(d) indexed by <doc,topic>, the value
	 * is the probability p=0~1
	 */
	private double[][] newDocTopicDistrib;
	/**
	 * topic word distribution, indexed by <topic,word>, the value is the
	 * probability p=0~1
	 */
	private double[][] topicWordDistrib;

	

	/**
	 * to record , unlike this.printDocumentTopics (), this function keeps the
	 * order of topic index
	 * 
	 * @author Shockley Xiang Li
	 */
	public void recordOldDocumentTopics() {
		int numOldDoc = oldlist.size();
		double[][] tempDTdistrib = new double[numOldDoc][numTopics];
		int docLen;
		double [] topicDist;
		for (int di = 0; di < numOldDoc; di++) {
			// each doc
			topicDist = null;
			topicDist = new double[numTopics];
			docLen = topicAssignment[di].length;
			for (int ti = 0; ti < numTopics; ti++)
				//topicDist[ti] = (((float) docTopicCounts[di][ti]) / docLen);
				topicDist[ti] = (alpha + docTopicCounts[di][ti])/ (tAlpha + docLen);
			tempDTdistrib[di] = topicDist;
		}
		oldDocTopicDistrib = tempDTdistrib;
	}

	/**
	 * to record , unlike this.printDocumentTopics (), this function keeps the
	 * order of topic index
	 * 
	 * @author Shockley Xiang Li
	 */
	public void recordNewDocumentTopics() {
		int numNewDoc = newlist.size();
		int numOldDoc = oldlist.size();
		double[][] tempDTdistrib = new double[numNewDoc][numTopics];
		int docLen;
		double[] topicDist;
		for (int di = numOldDoc; di < numNewDoc+numOldDoc; di++) {
			// each doc
			topicDist = null;
			topicDist = new double[numTopics];
			docLen = topicAssignment[di].length;
			for (int ti = 0; ti < numTopics; ti++)
				//topicDist[ti] = (((float) docTopicCounts[di][ti]) / docLen);
				topicDist[ti] = (alpha + docTopicCounts[di][ti])/ (tAlpha + docLen);
			tempDTdistrib[di-numOldDoc] = topicDist;
		}
		newDocTopicDistrib = tempDTdistrib;
	}
	
	/**
	 * This distribution doesn't sort the word
	 * @author Shockley Xiang Li
	 */
	public void recordTopicWordDitrib() {
		double [][] tempTopicWordProb = new double [numTopics][];

		for (int ti = 0; ti < numTopics; ti++) {
			for (int wi = 0; wi < numTypes; wi++){
				tempTopicWordProb[ti][wi] =(((double) typeTopicCounts[wi][ti]) + beta)/ (vBeta + tokensPerTopic[ti]);
			}
		}
		topicWordDistrib = tempTopicWordProb;
	}
	
	
	/**
	 * Number of topics to be fit
	 */
	int numTopics; // 

	/**
	 * Dirichlet(alpha,alpha,...) is the distribution over topics
	 */
	double alpha; // 
	/**
	 * Prior on per-topic multinomial distribution over words
	 */
	double beta; // 
	double tAlpha;
	double vBeta;
	/**
	 * the data field of the instances is expected to hold
	 */
	InstanceList oldlist; // 
							// a
	// FeatureSequence

	/**
	 * addtional list
	 */
	InstanceList newlist; // 
	/**
	 * topic assignments of each token indexed by <document index,
	 * token/sequence index>
	 */
	int[][] topicAssignment;
	int numTypes;
	int numTokens;
	/**
	 * indexed by (document index, topic index)
	 */
	int[][] docTopicCounts; // 
	/**
	 * indexed by (feature index, topic index)
	 */
	int[][] typeTopicCounts; // 
	/**
	 * indexed by (topic index)
	 */
	int[] tokensPerTopic; // 

	public LdaRecorder(int numberOfTopics) {
		this(numberOfTopics, 50.0, 0.01);
	}

	public LdaRecorder(int numberOfTopics, double alphaSum, double beta) {
		this.numTopics = numberOfTopics;
		this.alpha = alphaSum / numTopics;
		this.beta = beta;
	}

	public void estimate(InstanceList documents, int numIterations,
			int showTopicsInterval, int outputModelInterval,
			String outputModelFilename, Randoms r) {
		oldlist = documents.shallowClone();
		numTypes = oldlist.getDataAlphabet().size();
		int numDocs = oldlist.size();
		topicAssignment = new int[numDocs][];
		docTopicCounts = new int[numDocs][numTopics];
		typeTopicCounts = new int[numTypes][numTopics];
		tokensPerTopic = new int[numTopics];
		tAlpha = alpha * numTopics;
		vBeta = beta * numTypes;

		long startTime = System.currentTimeMillis();

		// Initialize with random assignments of tokens to topics
		// and finish allocating this.topics and this.tokens
		int topic, seqLen;
		FeatureSequence fs;
		for (int di = 0; di < numDocs; di++) {
			try {
				fs = (FeatureSequence) oldlist.get(di).getData();
			} catch (ClassCastException e) {
				System.err
						.println("LDA and other topic models expect FeatureSequence data, not FeatureVector data.  "
								+ "With text2vectors, you can obtain such data with --keep-sequence or --keep-bisequence.");
				throw e;
			}
			seqLen = fs.getLength();
			numTokens += seqLen;
			topicAssignment[di] = new int[seqLen];
			// Randomly assign tokens to topics
			for (int si = 0; si < seqLen; si++) {
				topic = r.nextInt(numTopics);
				topicAssignment[di][si] = topic;
				docTopicCounts[di][topic]++;
				typeTopicCounts[fs.getIndexAtPosition(si)][topic]++;
				tokensPerTopic[topic]++;
			}
		}

		this.estimate(0, numDocs, numIterations, showTopicsInterval,
				outputModelInterval, outputModelFilename, r);
		// 124.5 seconds
		// 144.8 seconds after using FeatureSequence instead of tokens[][] array
		// 121.6 seconds after putting "final" on
		// FeatureSequence.getIndexAtPosition()
		// 106.3 seconds after avoiding array lookup in inner loop with a
		// temporary variable

	}

	/**
	 * add new documents to the old model and Gibbs-samples them,
	 * So they will be annotated with topic-proportions,
	 *  during which the new doc names are recorded
	 * @author Shockley Xiang Li
	 * @param additionalDocuments
	 * @param numIterations
	 * @param showTopicsInterval
	 * @param outputModelInterval
	 * @param outputModelFilename
	 * @param r
	 */
	public void addDocuments(InstanceList additionalDocuments,
			int numIterations, int showTopicsInterval, int outputModelInterval,
			String outputModelFilename, Randoms r) {
		newlist = additionalDocuments;
		if (oldlist == null)
			throw new IllegalStateException(
					"Must already have some documents first.");

		numTypes = oldlist.getDataAlphabet().size();
		int numNewDocs = newlist.size();
		int numOldDocs = topicAssignment.length;
		int numDocs = numOldDocs + numNewDocs;
		termDocIds = new ArrayList<Long>();
		
		// Expand various arrays to make space for the new data.
		int[][] newTopics = new int[numDocs][];
		for (int i = 0; i < topicAssignment.length; i++)
			newTopics[i] = topicAssignment[i];
		topicAssignment = newTopics; // The rest of this array will be
		// initialized below.
		int[][] newDocTopicCounts = new int[numDocs][numTopics];
		for (int i = 0; i < docTopicCounts.length; i++) {
			newDocTopicCounts[i] = docTopicCounts[i];
		}
		docTopicCounts = newDocTopicCounts; // The rest of this array will be
		// initialized below.
		int[][] newTypeTopicCounts = new int[numTypes][numTopics];
		for (int i = 0; i < typeTopicCounts.length; i++) {
			for (int j = 0; j < numTopics; j++) {
				newTypeTopicCounts[i][j] = typeTopicCounts[i][j]; // This array
				// further
				// populated
				// below
			}
		}
		typeTopicCounts = newTypeTopicCounts;

		FeatureSequence fs;
		for (int di = numOldDocs; di < numDocs; di++) {
			try {
				Instance newDoc = newlist.get(di - numOldDocs);
				fs = (FeatureSequence) newDoc.getData();
				String absoFilePath = newDoc.getSource().toString();
				int start = absoFilePath.lastIndexOf('\\')+1;
				int end = absoFilePath.lastIndexOf(".txt");
				String fileName = absoFilePath.substring(start,end);
				termDocIds.add(Long.parseLong(fileName));
			} catch (ClassCastException e) {
				System.err
						.println("LDA and other topic models expect FeatureSequence data, not FeatureVector data.  "
								+ "With text2vectors, you can obtain such data with --keep-sequence or --keep-bisequence.");
				throw e;
			}
			int seqLen = fs.getLength();
			numTokens += seqLen;
			topicAssignment[di] = new int[seqLen];
			// Randomly assign tokens to topics
			for (int si = 0; si < seqLen; si++) {
				int topic = r.nextInt(numTopics);
				topicAssignment[di][si] = topic;
				docTopicCounts[di][topic]++;
				Object type = fs.getObjectAtPosition(si);
				typeTopicCounts[oldlist.getDataAlphabet().lookupIndex(type)][topic]++;
				tokensPerTopic[topic]++;
			}
		}
		incrementalEstimate(numIterations, showTopicsInterval,
				outputModelInterval, outputModelFilename, r);
	}

	/**
	 * @author Shockley Xiang Li
	 * @param numOldDocs
	 * @param numNewDocs
	 * @param numIterations
	 * @param showTopicsInterval
	 * @param outputModelInterval
	 * @param outputModelFilename
	 * @param r
	 */
	public void incrementalEstimate(int numIterations, int showTopicsInterval,
			int outputModelInterval, String outputModelFilename, Randoms r) {
		long startTime = System.currentTimeMillis();
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
			double[] topicWeights = new double[numTopics];
			// Loop over every word in the corpus
			for (int di = oldlist.size(); di < oldlist.size() + newlist.size(); di++) {
				sampleTopicsForOneDoc((FeatureSequence) newlist.get(
						di - oldlist.size()).getData(), topicAssignment[di],
						docTopicCounts[di], topicWeights, r);
			}
		}

		long seconds = Math
				.round((System.currentTimeMillis() - startTime) / 1000.0);
		long minutes = seconds / 60;
		seconds %= 60;
		long hours = minutes / 60;
		minutes %= 60;
		long days = hours / 24;
		hours %= 24;
		System.out.print("\nTotal time: ");
		if (days != 0) {
			System.out.print(days);
			System.out.print(" days ");
		}
		if (hours != 0) {
			System.out.print(hours);
			System.out.print(" hours ");
		}
		if (minutes != 0) {
			System.out.print(minutes);
			System.out.print(" minutes ");
		}
		System.out.print(seconds);
		System.out.println(" seconds");
	}

	/*
	 * Perform several rounds of Gibbs sampling on the documents in the given
	 * range.
	 */
	public void estimate(int docIndexStart, int docIndexLength,
			int numIterations, int showTopicsInterval, int outputModelInterval,
			String outputModelFilename, Randoms r) {
		long startTime = System.currentTimeMillis();
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
			sampleTopicsForDocs(docIndexStart, docIndexLength, r);
		}

		long seconds = Math
				.round((System.currentTimeMillis() - startTime) / 1000.0);
		long minutes = seconds / 60;
		seconds %= 60;
		long hours = minutes / 60;
		minutes %= 60;
		long days = hours / 24;
		hours %= 24;
		System.out.print("\nTotal time: ");
		if (days != 0) {
			System.out.print(days);
			System.out.print(" days ");
		}
		if (hours != 0) {
			System.out.print(hours);
			System.out.print(" hours ");
		}
		if (minutes != 0) {
			System.out.print(minutes);
			System.out.print(" minutes ");
		}
		System.out.print(seconds);
		System.out.println(" seconds");
	}

	/* One iteration of Gibbs sampling, across all documents. */
	public void sampleTopicsForAllDocs(Randoms r) {
		double[] topicWeights = new double[numTopics];
		// Loop over every word in the corpus
		for (int di = 0; di < topicAssignment.length; di++) {
			sampleTopicsForOneDoc((FeatureSequence) oldlist.get(di).getData(),
					topicAssignment[di], docTopicCounts[di], topicWeights, r);
		}
	}

	/* One iteration of Gibbs sampling, across all documents. */
	public void sampleTopicsForDocs(int start, int length, Randoms r) {
		assert (start + length <= docTopicCounts.length);
		double[] topicWeights = new double[numTopics];
		// Loop over every word in the corpus
		for (int di = start; di < start + length; di++) {
			sampleTopicsForOneDoc((FeatureSequence) oldlist.get(di).getData(),
					topicAssignment[di], docTopicCounts[di], topicWeights, r);
		}
	}

	/*
	 * public double[] assignTopics (int[] testTokens, Random r) { int[]
	 * testTopics = new int[testTokens.length]; int[] testTopicCounts = new
	 * int[numTopics]; int numTokens = MatrixOps.sum(testTokens); double[]
	 * topicWeights = new double[numTopics]; // Randomly assign topics to the
	 * words and // incorporate this document in the global counts int topic;
	 * for (int si = 0; si < testTokens.length; si++) { topic = r.nextInt
	 * (numTopics); testTopics[si] = topic; // analogous to this.topics
	 * testTopicCounts[topic]++; // analogous to this.docTopicCounts
	 * typeTopicCounts[testTokens[si]][topic]++; tokensPerTopic[topic]++; } //
	 * Repeatedly sample topic assignments for the words in this document for
	 * (int iterations = 0; iterations < numTokens*2; iterations++)
	 * sampleTopicsForOneDoc (testTokens, testTopics, testTopicCounts,
	 * topicWeights, r); // Remove this document from the global counts // and
	 * also fill topicWeights with an unnormalized distribution over topics for
	 * whole doc Arrays.fill (topicWeights, 0.0); for (int si = 0; si <
	 * testTokens.length; si++) { topic = testTopics[si];
	 * typeTopicCounts[testTokens[si]][topic]--; tokensPerTopic[topic]--;
	 * topicWeights[topic]++; } // Normalize the distribution over topics for
	 * whole doc for (int ti = 0; ti < numTopics; ti++) topicWeights[ti] /=
	 * testTokens.length; return topicWeights; }
	 */

	/**
	 * changed by Shockley, so this supports basic incremental learning (with no
	 * additional vocabulary)
	 * 
	 * @author Shockley Xiang Li
	 * @param oneDocTokens
	 * @param oneDocTopics
	 * @param oneDocTopicCounts
	 * @param topicWeights
	 * @param r
	 */
	private void sampleTopicsForOneDoc(FeatureSequence oneDocTokens,
			int[] oneDocTopics, // indexed by seq position
			int[] oneDocTopicCounts, // indexed by topic index
			double[] topicWeights, Randoms r) {
		int[] currentTypeTopicCounts;
		int type, oldTopic, newTopic;
		double topicWeightsSum;
		int docLen = oneDocTokens.getLength();
		double tw;
		// Iterate over the positions (words) in the document
		for (int si = 0; si < docLen; si++) {
			/**
			 * changed by Shockley, so this supports basic incremental learning
			 * (with no additional vocabulary)
			 * **/
			Object typeObject = oneDocTokens.getObjectAtPosition(si);
			type = oldlist.getDataAlphabet().lookupIndex(typeObject);
			oldTopic = oneDocTopics[si];
			// Remove this token from all counts
			oneDocTopicCounts[oldTopic]--;
			typeTopicCounts[type][oldTopic]--;
			tokensPerTopic[oldTopic]--;
			// Build a distribution over topics for this token
			Arrays.fill(topicWeights, 0.0);
			topicWeightsSum = 0;
			currentTypeTopicCounts = typeTopicCounts[type];
			for (int ti = 0; ti < numTopics; ti++) {
				tw = ((currentTypeTopicCounts[ti] + beta) / (tokensPerTopic[ti] + vBeta))
						* ((oneDocTopicCounts[ti] + alpha)); // (/docLen-1+tAlpha);
				// is constant
				// across all
				// topics
				topicWeightsSum += tw;
				topicWeights[ti] = tw;
			}
			// Sample a topic assignment from this distribution
			newTopic = r.nextDiscrete(topicWeights, topicWeightsSum);

			// Put that new topic into the counts
			oneDocTopics[si] = newTopic;
			oneDocTopicCounts[newTopic]++;
			typeTopicCounts[type][newTopic]++;
			tokensPerTopic[newTopic]++;
		}
	}

	public int[][] getDocTopicCounts() {
		return docTopicCounts;
	}

	public int[][] getTypeTopicCounts() {
		return typeTopicCounts;
	}

	public int[] getTokensPerTopic() {
		return tokensPerTopic;
	}

	public void printTopWords(int numWords, boolean useNewLines) {
		class WordProb implements Comparable {
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
				wp[wi] = new WordProb(wi, ((double) typeTopicCounts[wi][ti])
						/ tokensPerTopic[ti]);
			Arrays.sort(wp);
			if (useNewLines) {
				System.out.println("\nTopic " + ti);
				for (int i = 0; i < numWords; i++)
					System.out.println(oldlist.getDataAlphabet().lookupObject(
							wp[i].wi).toString()
							+ " " + wp[i].p);
			} else {
				System.out.print("Topic " + ti + ": ");
				for (int i = 0; i < numWords; i++)
					System.out.print(oldlist.getDataAlphabet().lookupObject(
							wp[i].wi).toString()
							+ " ");
				System.out.println();
			}
		}
	}

	public void printOldDocumentTopics(File f) throws IOException {
		printOldDocumentTopics(new PrintWriter(new FileWriter(f)));
	}
	
	public void printOldDocumentTopics(File f, double threshold, int max) throws IOException {
		printOldDocumentTopics(new PrintWriter(new FileWriter(f)), threshold, max);
	}

	public void printOldDocumentTopics(PrintWriter pw) {
		printOldDocumentTopics(pw, 0.0, -1);
	}

	/**
	 * this prints the topics in the order of their proportion
	 * 
	 * @author Shockley Xiang Li
	 * @param pw
	 * @param threshold
	 * @param max
	 */
	public void printOldDocumentTopics(PrintWriter pw, double threshold, int max) {
		pw.println("#doc source topic proportion ...");
		int docLen;
		int oldDocNum = oldlist.size();
		double topicDist[] = new double[oldDocNum];
		for (int di = 0; di < oldDocNum; di++) {
			pw.print(di);
			pw.print(' ');
			if (oldlist.get(di).getSource() != null) {
				pw.print(oldlist.get(di).getSource().toString());
			} else {
				pw.print("null-source");
			}
			pw.print(' ');
			docLen = topicAssignment[di].length;
			for (int ti = 0; ti < numTopics; ti++)
				topicDist[ti] = (((float) docTopicCounts[di][ti]) / docLen);
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
				pw.print(maxindex + " " + topicDist[maxindex] + " ");
				topicDist[maxindex] = 0;
			}
			pw.println(' ');
		}
	}

	/**
	 * this prints the topics in the order of their proportion
	 * 
	 * @author Shockley Xiang Li
	 * @param pw
	 * @param threshold
	 * @param max
	 */
	public void printNewDocumentTopics(File f, double threshold, int max) {
		try {
			PrintWriter pw;
			pw = new PrintWriter(new FileWriter(f));
			pw.println("#doc source topic proportion ...");
			int docLen;
			double topicDist[] = new double[topicAssignment.length];
			for (int di = oldlist.size(); di < newlist.size() + oldlist.size(); di++) {
				pw.print(di);
				pw.print(' ');
				if (newlist.get(di - oldlist.size()).getSource() != null) {
					pw.print(newlist.get(di - oldlist.size()).getSource()
							.toString());
				} else {
					pw.print("null-source");
				}
				pw.print(' ');
				docLen = topicAssignment[di].length;
				for (int ti = 0; ti < numTopics; ti++)
					topicDist[ti] = (((float) docTopicCounts[di][ti]) / docLen);
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
					pw.print(maxindex + " " + topicDist[maxindex] + " ");
					topicDist[maxindex] = 0;
				}
				pw.println(' ');
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printState(File f) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(f));
		printState(writer);
		writer.close();
	}

	public void printState(PrintWriter pw) {
		Alphabet a = oldlist.getDataAlphabet();
		pw.println("#doc pos typeindex type topic");
		for (int di = 0; di < topicAssignment.length; di++) {
			FeatureSequence fs = (FeatureSequence) oldlist.get(di).getData();
			for (int si = 0; si < topicAssignment[di].length; si++) {
				int type = fs.getIndexAtPosition(si);
				pw.print(di);
				pw.print(' ');
				pw.print(si);
				pw.print(' ');
				pw.print(type);
				pw.print(' ');
				pw.print(a.lookupObject(type));
				pw.print(' ');
				pw.print(topicAssignment[di][si]);
				pw.println();
			}
		}
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
	private static final int CURRENT_SERIAL_VERSION = 0;
	private static final int NULL_INTEGER = -1;

	
	/**
	 * write object to db
	 * @author Shockley Xiang Li
	 * @throws IOException
	 */
	/*
	public void storeWhatOntologyLearningNeeded() throws IOException {
		HibernateService hs = DataSourceFactory.getHibernateInstance();
		Session session = hs.getSession();
		Transaction tx = session.beginTransaction();
		String sql = "truncate table onto_ldarecorder";
		SQLQuery query = session.createSQLQuery(sql);
		query.executeUpdate();
		tx.commit();
		
		
		session = hs.getSession();
		tx = session.beginTransaction();
		LdaAttribute att;
		
        att = new LdaAttribute();
		att.setAttributeName("numTopics");
		att.setDescription("number of Topics");
		att.setValue(Integer.toString(numTopics));
		session.save(att);
		
		att = new LdaAttribute();
		att.setAttributeName("numTypes");
		att.setDescription("number of Types");
		att.setValue(Integer.toString(numTypes));
		session.save(att);
		
		att = new LdaAttribute();
		att.setAttributeName("alpha");
		att.setDescription("alpha");
		att.setValue(Double.toString(alpha));
		session.save(att);
		
		att = new LdaAttribute();
		att.setAttributeName("beta");
		att.setDescription("beta");
		att.setValue(Double.toString(beta));
		session.save(att);
		
		att = new LdaAttribute();
		att.setAttributeName("talpha");
		att.setDescription("t times alpha");
		att.setValue(Double.toString(tAlpha));
		session.save(att);
		
		att = new LdaAttribute();
		att.setAttributeName("vBeta");
		att.setDescription("V times Beta");
		att.setValue(Double.toString(vBeta));
		session.save(att);
		
		att = new LdaAttribute();
		att.setAttributeName("oldDocNum");
		att.setDescription("number of old documents");
		att.setValue(Integer.toString(oldDocTopicDistrib.length));
		session.save(att);
		
		att = new LdaAttribute();
		att.setAttributeName("newDocNum");
		att.setDescription("number of new(term) documents");
		att.setValue(Integer.toString(newDocTopicDistrib.length));
		session.save(att);
		
		for (int i = 0; i < termDocIds.size(); i++){
			att = new LdaAttribute();
			att.setAttributeName("termDocIds");
			att.setIndex2(i);
			att.setDescription("termIds of new(term) documents");
			att.setValue(Long.toString(termDocIds.get(i)));
			session.save(att);
		}
		
		for (int di = 0; di < newDocTopicDistrib.length; di++){
			for (int ti = 0; ti < newDocTopicDistrib[di].length; ti++){
				att = new LdaAttribute();
				att.setAttributeName("newDocTopicDistrib");
				att.setDescription("doc-topic proportion of new documents, p[doc][topic]");
				att.setIndex1(di);
				att.setIndex2(ti);
				att.setValue(Double.toString(newDocTopicDistrib[di][ti]));
				session.save(att);
			}
		}
		tx.commit();
		session.close();
	}
	
*/
	public InstanceList getInstanceList() {
		return oldlist;
	}

	// Recommended to use mallet/bin/vectors2topics instead.
	public static void main(String[] args) throws IOException {
		String oldDocsLocation = "D:\\work\\experiment\\ontology\\TKDE\\ohloh\\all.mallet";
		String newDocsLocation = "D:\\work\\experiment\\ontology\\TKDE\\ohloh\\all.combined.mallet";
		String oldDocTopicLocation = "D:\\work\\experiment\\ontology\\TKDE\\ohloh\\old.doc.topic";
		String newDocTopicLocation = "D:\\work\\experiment\\ontology\\TKDE\\ohloh\\new.doc.topic";
		
		File oldDocs = new File(oldDocsLocation);
		File newDocs = new File(newDocsLocation);
		File oldDocTopicDist = new File(oldDocTopicLocation);
		File newDocTopicDist = new File(newDocTopicLocation);
		
		InstanceList olddocs = InstanceList.load(oldDocs);
		InstanceList newdocs = InstanceList.load(newDocs);
		int numIterations = 2000;
		int numTopWords = 20;
		int numTopics = 60;
		System.out.println("Data loaded.");
		LdaRecorder lda = new LdaRecorder(numTopics);
		lda.estimate(olddocs, numIterations, 50, 0, null, new Randoms());
		lda.addDocuments(newdocs, numIterations, 50, 0, null, new Randoms());
		lda.printTopWords(numTopWords, true);
		
		lda.recordOldDocumentTopics();
		lda.recordNewDocumentTopics();
		lda.printOldDocumentTopics(oldDocTopicDist,0.02,5);
		lda.printNewDocumentTopics(newDocTopicDist,0.02,5);
		
//		lda.storeWhatOntologyLearningNeeded();
	}

	public double[][] getNewDocTopicDistrib() {
		return newDocTopicDistrib;
	}

	public List<Long> getTermDocIds() {
		return termDocIds;
	}
	
	public double[][] getOldDocTopicDistrib() {
		return oldDocTopicDistrib;
	}

	public double[][] getTopicWordDistrib() {
		return topicWordDistrib;
	}
}
