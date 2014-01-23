/**
 * @Shockley Xiang Li
 * 2012-5-10
 */
package influx.ontology;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;
import influx.datasource.dao.Term;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Currently, for ohloh only, 
 * to get subsumption relations between a pair of tags
 * @author Shockley
 *
 */
public class TagSubsumption {
	/**
	 * thetaS : support threshold
	 * thetaC : confidence threshold
	 */
	public static double thetaS = 30;
	public static double thetaC = 0.3;
	public static long ohlohForgeId = 5;
	
	
	public static Logger logger = Logger.getLogger(TagSubsumption.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	public Session session;
	public Transaction tx;
	public String ql;
	
	/**
	 * Currently, for ohloh only, 
	 * to get subsumption relations between a pair of tags
	 * @param totalProject : # of projects involved
	 * @param A
	 * @param B
	 */
	public TagSubsumption(){
	}
	
	/**
	 * confidence(A,B) = (A && B)/A
	 * @author Shockley Xiang Li
	 * @param <T>
	 * @param A
	 * @param B
	 * @return
	 */
	public static <T> double confidence(Collection<T> A, Collection<T> B){
		int intersect = CollectionUtils.intersection(A, B).size();
		return (double)intersect / A.size();
	}
	
	/**
	 * support(A,B) = (A && B)
	 * @author Shockley Xiang Li
	 * @param <T>
	 * @param A
	 * @param B
	 * @return
	 */
	public static <T> double support(Collection<T> A, Collection<T> B){
		int intersect = CollectionUtils.intersection(A, B).size();
		return (double)intersect;
	}
	
	/**
	 * if A subsumes B
	 * @author Shockley Xiang Li
	 * @param A
	 * @param B
	 * @return
	 */
	public static boolean ifSubsume(Term A, Term B, Map<Term,List<Long>>  term2Projects){
		List<Long> projectsA = term2Projects.get(A);
		List<Long> projectsB = term2Projects.get(B);
		//if(support(projectsB, projectsA) > thetaS){
			if(confidence(projectsB, projectsA) > thetaC)
				return true;
		//}
		return false;
	}
}
