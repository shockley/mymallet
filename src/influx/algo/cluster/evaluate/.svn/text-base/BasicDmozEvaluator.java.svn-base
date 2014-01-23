/**
 * @Shockley Xiang Li
 * 2012-6-19
 */
package influx.algo.cluster.evaluate;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;

import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Shockley
 *
 */
public class BasicDmozEvaluator {
	public static Logger logger = Logger.getLogger(BasicDmozEvaluator.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	private Session session = null;
	private Transaction tx = null;
	private String sql = "";
	private SQLQuery query = null;
	/**
	 * calc the precision given an onto_network, a lot of joint query!
	 * @author Shockley Xiang Li
	 * @return
	 */
	public double getPrecision(){
		int correctRelation = 0;
		int recoPair = 0;
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select `from_term`, `to_term` from onto_network";
		query = session.createSQLQuery(sql);
		ScrollableResults pairs = query.scroll();
		logger.info(query.list().size());
		while(pairs.next()){
			Integer fromTermId = (Integer) pairs.get(0);
			Integer toTermId = (Integer) pairs.get(1);
			sql = "select `dmoz_name` from onto_terms where id = "+fromTermId;
			String dmozFrom = (String) session.createSQLQuery(sql).list().get(0);
			if(dmozFrom==null || dmozFrom.equals("")){
				//this term is not covered by dmoz
				continue;
			}
			
			sql = "select `dmoz_name` from onto_terms where id = "+toTermId;
			String dmozTo = (String) session.createSQLQuery(sql).list().get(0);
			if(dmozTo==null || dmozTo.equals("")){
				//this term is not covered by dmoz
				continue;
			}
			recoPair ++;
			sql = "select count(*) from dmoz_relation where `from` = \""+dmozFrom+"\" && `to` = \""+dmozTo+"\"";
			query = session.createSQLQuery(sql);
			ScrollableResults count = query.scroll();
			count.first();
			BigInteger c = (BigInteger) count.get(0);
			if(c.intValue() > 0)
				correctRelation++;
		}
		logger.info(recoPair);
		logger.info(correctRelation);
		return 0.0;
	}
}
