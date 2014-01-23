package edu.nudt.influx.lda;
/*
 * @author LiangZhen
 * @author WangTao
 * 实现RecSys09 LDA for tag recommendation论文的算法；
 * 主要思想是以每个项目的tag集合作为项目的描述文档，然后将训练集和测试集放在一起进行训练，最后用训练得到的Topic
 * 来表示每个项目，将doc-topic的概率乘以对应topic-tag的概率，就可以得到针对每个文档的tag排名，将排在前面的x个
 * tag来作为推荐的tag
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;

public class LDAForRecSys09 implements Serializable {

	int numTopics; // Number of topics to be fit
	public double alpha; // Dirichlet(alpha,alpha,...) is the distribution over
							// topics
	public double beta; // Prior on per-topic multinomial distribution over
						// words
	double tAlpha;
	double vBeta;
	InstanceList ilist; // the data field of the instances is expected to hold a
						// FeatureSequence
	int[][] topics; // indexed by <document index, sequence index>
	public int numTypes;// number of distinguish words over all documents
	int numTokens;// number of words over all documents including duplicate
					// words
	int[][] docTopicCounts; // indexed by <document index, topic index>
	int[][] typeTopicCounts; // indexed by <feature index, topic index>
	int[] tokensPerTopic; // indexed by <topic index>

	public LDAForRecSys09(int numberOfTopics) {
		this(numberOfTopics, 50.0, 0.01);
	}

	public LDAForRecSys09(int numberOfTopics, double alphaSum, double beta) {
		this.numTopics = numberOfTopics;
		this.alpha = alphaSum / numTopics;
		this.beta = beta;
	}

	public void estimate(InstanceList documents, int numIterations,
			int showTopicsInterval, int outputModelInterval,
			String outputModelFilename, Randoms r) {
		ilist = documents.shallowClone();
		numTypes = ilist.getDataAlphabet().size();// get the distinctive words
													// in all documents
		int numDocs = ilist.size();// get number of documents
		topics = new int[numDocs][];
		docTopicCounts = new int[numDocs][numTopics];
		typeTopicCounts = new int[numTypes][numTopics];
		tokensPerTopic = new int[numTopics];
		tAlpha = alpha * numTopics;
		vBeta = beta * numTypes;

		// Initialize with random assignments of tokens to topics
		// and finish allocating this.topics and this.tokens
		int topic, seqLen;
		FeatureSequence fs;
		for (int di = 0; di < numDocs; di++) {
			try {
				fs = (FeatureSequence) ilist.get(di).getData();
			} catch (ClassCastException e) {
				System.err
						.println("LDA and other topic models expect FeatureSequence data, not FeatureVector data.  "
								+ "With text2vectors, you can obtain such data with --keep-sequence or --keep-bisequence.");
				throw e;
			}
			seqLen = fs.getLength();
			numTokens += seqLen;
			topics[di] = new int[seqLen];
			// Randomly assign tokens to topics
			for (int si = 0; si < seqLen; si++) {
				topic = r.nextInt(numTopics);
				topics[di][si] = topic;// randomly assign topic to every words
										// in document di
				docTopicCounts[di][topic]++;
				typeTopicCounts[fs.getIndexAtPosition(si)][topic]++;
				tokensPerTopic[topic]++;
			}
		}

		this.estimate(0, numDocs, numIterations, showTopicsInterval,
				outputModelInterval, outputModelFilename, r);

	}

	public void addDocuments(InstanceList additionalDocuments,
			int numIterations, int showTopicsInterval, int outputModelInterval,
			String outputModelFilename, Randoms r, double w) {
		if (ilist == null)
			throw new IllegalStateException(
					"Must already have some documents first.");

		ilist = additionalDocuments;
		numTypes = additionalDocuments.getDataAlphabet().size();
		vBeta = beta * numTypes;
		int numToken = numTokens;
		numTokens = 0;

		int numDocs = additionalDocuments.size();
		int[][] newTopics = new int[numDocs][];
		topics = newTopics;
		int[][] newDocTopicCounts = new int[numDocs][numTopics];
		docTopicCounts = newDocTopicCounts;
		int[][] newTypeTopicCounts = new int[numTypes][numTopics];
		FeatureSequence fs1;
		for (int di = 0; di < topics.length; di++) {
			fs1 = (FeatureSequence) additionalDocuments.get(di).getData();
			int seqLen = fs1.getLength();
			numTokens += seqLen;
		}
		/*
		 * if(numToken<(numTokens*w)) { w=(numTokens*w)/numToken; } else
		 * if(numToken>(numTokens/w)) { w=numTokens/(numToken*w); }
		 */
		w = (numTokens * w) / numToken;
		int sum = 0;
		for (int i = 0; i < numTopics; i++) {
			for (int j = 0; j < typeTopicCounts.length; j++) {
				newTypeTopicCounts[j][i] = (int) Math
						.round(typeTopicCounts[j][i] * w);
				sum = sum + newTypeTopicCounts[j][i];
			}
			tokensPerTopic[i] = sum;
			sum = 0;
		}
		typeTopicCounts = newTypeTopicCounts;
		numTokens = 0;
		FeatureSequence fs;
		for (int di = 0; di < topics.length; di++) {
			fs = (FeatureSequence) additionalDocuments.get(di).getData();
			int seqLen = fs.getLength();
			numTokens += seqLen;
			topics[di] = new int[seqLen];
			// Randomly assign tokens to topics
			for (int si = 0; si < seqLen; si++) {
				int topic = r.nextInt(numTopics);
				topics[di][si] = topic;
				docTopicCounts[di][topic]++;
				typeTopicCounts[fs.getIndexAtPosition(si)][topic]++;
				tokensPerTopic[topic]++;
			}
		}
		this.estimate(0, numDocs, numIterations, showTopicsInterval,
				outputModelInterval, outputModelFilename, r);
	}

	/*
	 * Perform several rounds of Gibbs sampling on the documents in the given
	 * range.
	 */
	public void estimate(int docIndexStart, int docIndexLength,
			int numIterations, int showTopicsInterval, int outputModelInterval,
			String outputModelFilename, Randoms r) {
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

	}

	/* One iteration of Gibbs sampling, across all documents. */
	public void sampleTopicsForDocs(int start, int length, Randoms r) {
		assert (start + length <= docTopicCounts.length);
		double[] topicWeights = new double[numTopics];
		// Loop over every word in the corpus
		for (int di = start; di < start + length; di++) {
			sampleTopicsForOneDoc((FeatureSequence) ilist.get(di).getData(),
					topics[di], docTopicCounts[di], topicWeights, r);
		}
	}

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
			type = oneDocTokens.getIndexAtPosition(si);
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
				// System.out.println(si+"---"+ ti+"---"+tw);
			}
			// Sample a topic assignment from this distribution
			newTopic = r.nextDiscrete(topicWeights, topicWeightsSum);
			// System.out.println(si+"---"+ newTopic);
			// Put that new topic into the counts
			oneDocTopics[si] = newTopic;
			oneDocTopicCounts[newTopic]++;
			typeTopicCounts[type][newTopic]++;
			tokensPerTopic[newTopic]++;
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

	public int[][] getDocTopicCounts() {
		return docTopicCounts;
	}

	public int[][] getTypeTopicCounts() {
		return typeTopicCounts;
	}

	public int[] getTokensPerTopic() {
		return tokensPerTopic;
	}
	
	public int[][] getTopics(){
		return this.topics;
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
						(double) (typeTopicCounts[wi][ti] + beta)
								/ (tokensPerTopic[ti] + vBeta));
			Arrays.sort(wp);
			if (useNewLines) {
				System.out.println("\nTopic " + ti);
				for (int i = 0; i < numWords; i++)
					System.out.println(ilist.getDataAlphabet()
							.lookupObject(wp[i].wi).toString()
							+ "\t" + wp[i].p);
				// System.out.println
				// (ilist.getDataAlphabet().lookupObject(wp[i].wi).toString());
			} else {
				System.out.print("Topic " + ti + ": ");
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
			// TODO Auto-generated catch block
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
				wp[wi] = new WordProb(wi, ((double) typeTopicCounts[wi][ti])
						/ tokensPerTopic[ti]);
			Arrays.sort(wp);
			if (useNewLines) {
				out.println("\nTopic " + ti);
				for (int i = 0; i < numWords; i++)
					out.println(ilist.getDataAlphabet().lookupObject(wp[i].wi)
							.toString()
							+ " " + wp[i].p);
			} else {
				out.print("Topic " + ti + ": ");
				for (int i = 0; i < numWords; i++)
					out.print(ilist.getDataAlphabet().lookupObject(wp[i].wi)
							.toString()
							+ " ");
				out.println();
			}
		}
	}

	public void SprintToFile(int numWords, boolean useNewLines, File file) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
						(double) (typeTopicCounts[wi][ti] + beta)
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
				topicDist[ti] = ((double) (docTopicCounts[di][ti] + alpha) / (docLen + tAlpha));
			// 排序
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
				topicdoc[ti][di] = ((double) (docTopicCounts[di][ti] + alpha) / (topics[di].length + tAlpha));
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
						((double) (docTopicCounts[di][ti] + alpha) / (topics[di].length + tAlpha)));
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

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(CURRENT_SERIAL_VERSION);
		out.writeObject(ilist);
		out.writeInt(numTopics);
		out.writeDouble(alpha);
		out.writeDouble(beta);
		out.writeDouble(tAlpha);
		out.writeDouble(vBeta);
		for (int di = 0; di < topics.length; di++)
			for (int si = 0; si < topics[di].length; si++)
				out.writeInt(topics[di][si]);
		for (int di = 0; di < topics.length; di++)
			for (int ti = 0; ti < numTopics; ti++)
				out.writeInt(docTopicCounts[di][ti]);
		for (int fi = 0; fi < numTypes; fi++)
			for (int ti = 0; ti < numTopics; ti++)
				out.writeInt(typeTopicCounts[fi][ti]);
		for (int ti = 0; ti < numTopics; ti++)
			out.writeInt(tokensPerTopic[ti]);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		ilist = (InstanceList) in.readObject();
		numTopics = in.readInt();
		alpha = in.readDouble();
		beta = in.readDouble();
		tAlpha = in.readDouble();
		vBeta = in.readDouble();
		int numDocs = ilist.size();
		topics = new int[numDocs][];
		for (int di = 0; di < ilist.size(); di++) {
			int docLen = ((FeatureSequence) ilist.get(di).getData())
					.getLength();
			topics[di] = new int[docLen];
			for (int si = 0; si < docLen; si++)
				topics[di][si] = in.readInt();
		}
		docTopicCounts = new int[numDocs][numTopics];
		for (int di = 0; di < ilist.size(); di++)
			for (int ti = 0; ti < numTopics; ti++)
				docTopicCounts[di][ti] = in.readInt();
		int numTypes = ilist.getDataAlphabet().size();
		typeTopicCounts = new int[numTypes][numTopics];
		for (int fi = 0; fi < numTypes; fi++)
			for (int ti = 0; ti < numTopics; ti++)
				typeTopicCounts[fi][ti] = in.readInt();
		tokensPerTopic = new int[numTopics];
		for (int ti = 0; ti < numTopics; ti++)
			tokensPerTopic[ti] = in.readInt();
	}

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
	
	public void printSampleTopics(LDAForRecSys09 lda,String sampleTopicLogFile){
		int[][] topics=lda.getTopics();
		int doc_len = topics.length;
		try {
			FileWriter fw = new FileWriter(sampleTopicLogFile,true);
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i=0;i<doc_len;i++){
				int[] tokens = topics[i];
				int token_len = tokens.length;
				for(int j=0;j<token_len-2;j++){
					bw.write(ilist.get(i).getDataAlphabet().lookupObject(j)+": "+topics[i][j]+"\t\t");
					if(j%5==0&&j!=0){
						bw.newLine();
					}
				}
				bw.newLine();
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public class WordProb implements Comparable<Object> {
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
	
	//获得主题-词的概率分布
	public WordProb[][] getTopicWordDistr(){
		WordProb[][] wp = new WordProb[numTopics][numTypes];
		for (int ti = 0; ti < numTopics; ti++) {
			for (int wi = 0; wi < numTypes; wi++)
				wp[ti][wi] = new WordProb(wi, ((double) typeTopicCounts[wi][ti]) / tokensPerTopic[ti]);
			Arrays.sort(wp[ti]);
		}
		return wp;
	}
	
	//获得文档主题分布
	public double[][] getDocTopicDistr(){
		int num_docs = topics.length;
		double[][] doc_topic_distr = new double[num_docs][numTopics];
		
		for (int di = 0; di < num_docs; di++) {//对每个文档进行计算
			int doc_len = topics[di].length;
			for(int ti=0; ti<numTopics; ti++)
				doc_topic_distr[di][ti] = (((float)docTopicCounts[di][ti])/doc_len);
		}
		return doc_topic_distr;
	}
	
    /*
     * 将对每个项目推荐的tag打印出来(按照<文档-主题概率>*<主题-词概率>=最终每个文档对应tag的排序，以此排序作为最终推荐的顺序)
     */
//	public void printRecommendations(int num_recommendation, double threshold,File file){
//		PrintWriter pw = null;
//		try {
//			pw = new PrintWriter(new FileWriter(file));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		printRecommendations(pw, num_recommendation, threshold);
//		pw.close();
//	}
//	
	public String getFileName(String file_path){
		String file_name;
		int start_position = file_path.lastIndexOf("\\");
		int end_position = file_path.lastIndexOf(".");
		file_name =(String)file_path.subSequence(start_position+1, end_position+4);	
		return file_name;
	}
	
	public void printRecommendations(String file_dir, int num_recommendation,double threshold){
		PrintWriter pw = null;
//		pw.println("#doc source topic proportion ...");
		double[][] doc_topic_distr =  getDocTopicDistr();
		WordProb[][] topic_word_distr = getTopicWordDistr();
		
		for(int di=0; di<topics.length; di++){	
			int total_doc_tag = numTopics*num_recommendation;
			WordProb[] total_doc_tag_distr = new WordProb[total_doc_tag];
			HashMap<Integer, Integer> word_position_hash = new HashMap<Integer, Integer>();
			int position = 0;
			for(int ti=0; ti<numTopics;ti++)
				for(int wi=0;wi<num_recommendation;wi++){
					int word_id = topic_word_distr[ti][wi].wi;
					double word_prob = topic_word_distr[ti][wi].p;
					if(word_position_hash.containsKey(word_id)){
						int position_temp = (int) word_position_hash.get(word_id);
						double word_prob_temp = word_prob*doc_topic_distr[di][ti];
						word_prob_temp += total_doc_tag_distr[position_temp].p;
						total_doc_tag_distr[position_temp] = new WordProb(word_id, word_prob_temp);
					}else{
						total_doc_tag_distr[position] = new WordProb(word_id, word_prob*doc_topic_distr[di][ti]);
						word_position_hash.put(word_id,position);
						position++;
					}
				}
			if(position<total_doc_tag){
				for(int i = position;i<total_doc_tag;i++){
					total_doc_tag_distr[i]=new WordProb(0,0);
				}
			}
			Arrays.sort(total_doc_tag_distr);
			
			String init_file_path = ilist.get(di).getSource().toString();
			String file_name = getFileName(init_file_path);
			String target_file_path = file_dir.concat(file_name);
			File target_file = new File(target_file_path);
//			System.out.println(target_file_path);
			
			try {
				pw = new PrintWriter(new FileWriter(target_file));
			} catch (IOException e) {
				e.printStackTrace();
			}
			pw.print(di);
			pw.print(' ');
			if (ilist.get(di).getSource() != null) {
				pw.print(ilist.get(di).getSource().toString());
			} else {
				pw.print("null-source");
			}
			pw.println();
            
			for(int tag_id=0;tag_id<num_recommendation;tag_id++){
				String tag = ilist.getDataAlphabet().lookupObject(total_doc_tag_distr[tag_id].wi).toString();
				double tag_prob = total_doc_tag_distr[tag_id].p;
//				System.out.println("THIAHIFSIHFIHISHFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF      "+tag_prob);
				pw.println(tag + "  "+tag_prob+"    ");
			}
//			pw.println();
			pw.flush();
		}
//		pw.flush();
		pw.close();
	}
	
	
	public static void main(String[] args) throws IOException {
		InstanceList ilist = InstanceList.load(new File(
				"F:\\Experiment\\LDA\\recSys\\mallet\\mallet2\\recSys.mallet"));
		int numIterations = 1000;
		int numTopWords = 30;
		int num_recommendation = 30;
		double threshold = 0.001;
		System.out.println("Data loaded.");
		int pr = 200;

		LDAForRecSys09 lda = new LDAForRecSys09(100);
		lda.estimate(ilist, numIterations, pr, 0, null, new Randoms());
		lda.printTopWords(numTopWords, true);
		lda.printToFile(numTopWords, true, new File(
				"F:\\Experiment\\LDA\\recSys\\result\\recSys2\\topicLda\\tag1000.topic"));
		lda.printDocumentTopics(new File(
				"F:\\Experiment\\LDA\\recSys\\result\\recSys2\\topicLda\\tag1000.lda"));
	
		String target_dir = "F:\\Experiment\\LDA\\recSys\\result\\recSys2\\recommendation\\1000\\";
		lda.printRecommendations(target_dir, num_recommendation, threshold);
	}
}

