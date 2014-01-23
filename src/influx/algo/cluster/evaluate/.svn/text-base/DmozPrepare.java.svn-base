/**
 * @Shockley Xiang Li
 * 2012-6-19
 */
package influx.algo.cluster.evaluate;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Shockley
 *
 */
public class DmozPrepare {
	public static Logger logger = Logger.getLogger(DmozPrepare.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	private Session session = null;
	private Transaction tx = null;
	private String sql = "";
	private SQLQuery query = null;
	/**
	 * update the table onto_terms, to fill the dmoz names
	 * @author Shockley Xiang Li
	 * @return
	 */
	public void fillDmozName(){
		List<String> tags = new ArrayList<String> ();
		List<String> dmozNames = new ArrayList<String> ();
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select p.description, p.dmoz_name from ohloh_popular_tag p";
		query = session.createSQLQuery(sql);
		ScrollableResults pairs = query.scroll();
		while(pairs.next()){
			if(pairs.get(1)!=null && pairs.get(0)!=null && !((String) pairs.get(1)).equals("")){
				String tag = (String) pairs.get(0);
				String dmoz = (String) pairs.get(1);
				dmozNames.add(dmoz);
				tags.add(tag);
			}
		}
		for(int i=0;i<tags.size();i++){
			sql = "update onto_terms set dmoz_name = \""+dmozNames.get(i)+"\" where name = \""+tags.get(i)+"\"";
			query = session.createSQLQuery(sql);
			query.executeUpdate();
		}
		tx.commit();
		session.close();
	}
}
