/**
 * @Shockley Xiang Li
 * 2012-4-5
 */
package influx.datasource.importer;

import java.util.Iterator;
import java.util.List;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;
import influx.datasource.dao.Forge;
import influx.datasource.dao.Term;
import influx.datasource.dao.OhlohPopularTag;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Read the popular ohloh tags from ohloh_popular_tag table, store them into onto_concept table
 * @author Shockley
 *
 */
public class OhlohTagReader {
	public static Logger logger = Logger.getLogger("OhlohTagReader");
	public static HibernateService hs = DataSourceFactory.getHibernateInstance();
	public static long ohlohforgeId = 5;
	
	public void truncateConcept(){
		Session session = hs.getSession();
		Transaction tx = session.beginTransaction();
		String sql = "truncate table onto_concepts";
		session.createSQLQuery(sql).executeUpdate();
		tx.commit();
		session.close();
	}
	public void readTags(){
		Session session = hs.getSession();
		Transaction tx = session.beginTransaction();
		String hql = "from OhlohPopularTag pt";
		Iterator<OhlohPopularTag> popularTags = session.createQuery(hql).iterate();
		if(popularTags==null){
			logger.error("database error!");
			return;
		}
		while(popularTags.hasNext()){
			OhlohPopularTag tag = popularTags.next();
			String name = tag.getDescription();
			hql = "from Concept c where c.name ='"+name+"'";
			List<Term> concepts = session.createQuery(hql).list();
			if(concepts.size()==0){
				Term term = new Term();
				term.setName(name);
				//term.setForge((Forge) session.load(Forge.class, ohlohforgeId));
				session.save(term);
			}
		}
		tx.commit();
		session.close();
	}
	
	/**
	 * whether the tag is wanted
	 * @author Shockley Xiang Li
	 * @param tag
	 * @return
	 */
	public boolean isWanted(String s){
		//TODO add some prefiltering policies
		return true;
	}
	
	public static void main(String [] args){
		OhlohTagReader reader = new OhlohTagReader();
		reader.truncateConcept();
		reader.readTags();
	}
}
