package influx.ontology.cikm;

import java.util.HashMap;

import org.apache.log4j.Logger;

public class CIKMRandomWalk {
	public static Logger logger = Logger.getLogger(CIKMRandomWalk.class);
	/**
	 * @param confidence
	 *            [][] confidence[][] is the weighted nodes matrix, for which
	 *            each element confidence[i][j] means the weighted connection
	 *            between node i and node j
	 * @param nameda
	 *            nameda is the tranport probabilistic for jumping from
	 * @param trans_prob
	 *            [][] trans_prob[][] is the transport probabilistic matrix, Its
	 *            elements trans_prob[i][j] means the probabilistic from node i
	 *            to node j
	 * @param max_iteration
	 *            max_iteration means the number of iteration times for the
	 *            random walk
	 * @param diff_threshold
	 *            diff_threshold is the terminate condition, either the
	 *            iteration time reaches the parameter iteration of the
	 *            difference between two iterations less than the predefined
	 *            diff_threshold, the algorithm should terminate.
	 */
	public double[] generalScore(double[][] confidence, double nameda,
			int max_iteration, double diff_threshold) {
		int length = confidence.length;
		System.out.println("the length of confidence is " + length);

		double general_score[] = new double[length];
		for (int i = 0; i < length; i++) {
			general_score[i] = 10;
		}

		double[][] trans_prob = new double[length][length];
		double s = (double) 1 / (double) length;
		// calculate the transport probabilistic based on the weighted adjacent
		// matrix
		for (int i = 0; i < length; i++) {
			double confidence_sum = 0;
			for (int j = 0; j < length; j++) {
				if(i!=j)
					confidence_sum += confidence[i][j];
			}
			for (int k = 0; k < length; k++) {
				trans_prob[i][k] = confidence[i][k] / confidence_sum;
				//System.out.println("trans_prob is " + trans_prob[i][k]);
			}
		}

		// calculate the generality score based on the random walk
		int iteration_num = 0;
		double later_difference = 0;
		double pre_difference = 0;
		double difference = 1.0;
		while (iteration_num < max_iteration && difference > diff_threshold) {
			if(iteration_num  % (max_iteration/10)==0){
				//logger.info("" + iteration_num + " iterations have passed!");
			}
			double[] temp_general_score = general_score.clone();
			for (int i = 0; i < length; i++) {
				general_score[i] = 0;
				for (int j = 0; j < length; j++) {
					if (j != i) {
						general_score[i] += (double) (temp_general_score[j] * trans_prob[j][i]);
					}
				}
				general_score[i] = (nameda * general_score[i] + (double) (1 - nameda)* s);
				later_difference += (double) Math.pow(Math.abs((general_score[i] - temp_general_score[i])),2);
				pre_difference += (double) Math.pow(temp_general_score[i], 2);
				//System.out.println(i + "   score: " + temp_general_score[i]
				//		+ "  " + general_score[i]);
			}
			difference = Math.sqrt(later_difference / pre_difference);
			iteration_num++;
		}
		System.out.println("Iteration time is " + iteration_num
				+ " difference is : " + difference);
		return general_score;
	}
	
	  public static void main(String[] args){ 
		  CIKMRandomWalk rw = new CIKMRandomWalk();
		double confidence[][] ={{0, 0.24,0.67,0,0.32,0,0},{0,0,0.48,0.33,0,0,0},{0.34,0,0,0,0.27,0.21,0}, 
				{ 0,0,0,0,0,0.65,0},{0,0,0.85,0,0,0,0},{0,0,0.24,0,0,0,0},{0,0,0,0,0,0.79,0}}; 
//	  double  confidence[][]={{0,0.1,0.2,0.8},{0,0,0.7,0.1},{0,0,0,0.3},{0.1,0,0.2,0}};
	 
		  double nameda = 0.95; int max_iteration = 10000; 
		  double dif_threshold =  0.001;
	  
	  double general_score[] =
		  rw.generalScore(confidence,nameda,max_iteration,dif_threshold); 
	  for(int
	  i=0;i<general_score.length;i++)
	  { 
		  System.out.println(general_score[i]);
	  }
//	   System.out.println(Math.pow(1.1, 100)); }
	  }
}
