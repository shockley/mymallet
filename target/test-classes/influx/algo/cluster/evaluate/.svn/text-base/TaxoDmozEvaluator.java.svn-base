/**
 * @Shockley Xiang Li
 * 2012-5-30
 */
package influx.algo.cluster.evaluate;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * To compare HAC or TKDE experments' result (ontology sql)
 * with the ground truth
 * @author Shockley
 *
 */
public class TaxoDmozEvaluator {
	public static Logger logger = Logger.getLogger(TaxoDmozEvaluator.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	private Session session = null;
	private Transaction tx = null;
	private String sql = "";
	private SQLQuery query = null;
	
	
	
	/**
	 * get the super concepts for a given concept string, in DMOZ-software subtree
	 * @author Shockley Xiang Li
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<String> getSupersInDMOZ(String c){
		List<String> supers = new ArrayList<String>();
		String current = c;
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select `from` from dmoz_relation where `to` = '" +current+"'";
		query = session.createSQLQuery(sql);
		List<String> fathers = query.list();
		for(String s : fathers){
			if(!supers.contains(s))
				supers.add(s);
		}
		tx.commit();
		session.close();
		return supers;
	}
	
	/**
	 * get the super concepts for a given concept string, in their onto_term ids
	 * @author Shockley Xiang Li
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getSupersInResult(int c){
		List<Integer> supers = new ArrayList<Integer>();
		Integer current = c;
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select from_term from onto_relations where to_term = '" +current+"'";
		query = session.createSQLQuery(sql);
		List<Integer> fathers = query.list();
		for(Integer s : fathers){
			if(!supers.contains(s))
				supers.add(s);
		}
		tx.commit();
		session.close();
		return supers;
	}
	
	/**
	 * get the super concepts for a given concept string, in termId
	 * @author Shockley Xiang Li
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getSubsInResult(int c){
		List<Integer> subs = new ArrayList<Integer>();
		int current = c;
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select to_term from onto_relations where from_term = '" +current+"'";
		query = session.createSQLQuery(sql);
		List<Integer> children = query.list();
		for(Integer s : children){
			if(!subs.contains(s))
				subs.add(s);
		}
		tx.commit();
		session.close();
		return subs;
	}
	
	@SuppressWarnings("unchecked")
	private Integer getTermIdByName(String tag){
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select `id` from onto_terms where `name` = '" +tag+"'";
		query = session.createSQLQuery(sql);
		List<Integer> ids = query.list();
		if(ids==null || ids.size()!=1)
			logger.error("INCORRECT tag "+tag);
		return ids.get(0);
	}
	
	@SuppressWarnings("unchecked")
	private String getDmozName(String tag){
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select `dmoz_name` from onto_terms where `name` = '" +tag+"'";
		query = session.createSQLQuery(sql);
		List<String> ids = query.list();
		if(ids==null || ids.size()!=1)
			logger.error("INCORRECT tag name "+tag);
		return ids.get(0);
	}
	
	/**
	 * 
	 * @author Shockley Xiang Li
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String getTermInDmozName(Integer id){
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select `dmoz_name` from onto_terms where `id` = " +id;
		query = session.createSQLQuery(sql);
		List<String> names = query.list();
		if(names==null || names.size()!=1)
			logger.error("!!");
		return names.get(0);
	}
	
	/**
	 * @author Shockley Xiang Li
	 * @param id
	 * @return the ohloh tag with the given id
	 */
	@SuppressWarnings("unchecked")
	private String getTermNameById(Integer id){
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select `name` from onto_terms where `id` = " +id;
		query = session.createSQLQuery(sql);
		List<String> names = query.list();
		if(names==null || names.size()!=1)
			logger.error("!!");
		return names.get(0);
	}
	
	/**
	 * get the sub-concepts for a given concept string, in DMOZ-software subtree
	 * recursive manner
	 * @author Shockley Xiang Li
	 * @param c
	 * @return the sub-concepts in dmoz form
	 */
	@SuppressWarnings("unchecked")
	private List<String> getSubsInDMOZ(String c){
		List<String> subs = new ArrayList<String>();
		String current = c;
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select `to` from dmoz_relation where `from` = '" +current+"'";
		query = session.createSQLQuery(sql);
		List<String> children = query.list();
		for(String s : children){
			if(!subs.contains(s))
				subs.add(s);
		}
		tx.commit();
		session.close();
		return subs;
	}
	
	/**
	 * return the 170 common tags, return with dmoz form if difference exists
	 * @author Shockley Xiang Li
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<String> getCommonTermsInDmoz(){
		List<String> commons = new ArrayList<String>();
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select `dmoz_name` from onto_terms where `dmoz_name` is not null ";
		query = session.createSQLQuery(sql);
		List<String> children = query.list();
		for(String s : children){
			if(!commons.contains(s))
				commons.add(s);
		}
		tx.commit();
		session.close();
		return commons;
	}
	
	/**
	 * return the 170 common tags, return with ohloh form if difference exists
	 * @author Shockley Xiang Li
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<String> getCommonTermsInOhloh(){
		List<String> commons = new ArrayList<String>();
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select `name` from onto_terms where `dmoz_name` is not null ";
		query = session.createSQLQuery(sql);
		List<String> children = query.list();
		for(String s : children){
			if(!commons.contains(s))
				commons.add(s);
		}
		tx.commit();
		session.close();
		return commons;
	}
	
	/**
	 * return the 178 common tags' termid
	 * @author Shockley Xiang Li
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getCommonTermIds(){
		List<Integer> commons = new ArrayList<Integer>();
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select `id` from onto_terms where `dmoz_name` is not null ";
		query = session.createSQLQuery(sql);
		List<Integer> children = query.list();
		for(Integer s : children){
			if(!commons.contains(s))
				commons.add(s);
		}
		tx.commit();
		session.close();
		return commons;
	}

	
	/**
	 * @author Shockley Xiang Li
	 * @param c the tag string in ohloh form
	 * @param dmoz
	 * @return csc all in ohloh tag form
	 */
	@SuppressWarnings("unchecked")
	private List<String> csc(String c, boolean dmoz){
		List<String> csc;
		if(dmoz){
			String tag = this.getDmozName(c);
			List<String> subs = this.getSubsInDMOZ(tag);
			List<String> supers = this.getSupersInDMOZ(tag);
			List<String> common = this.getCommonTermsInDmoz();
			List<String> dmozSc = ListUtils.union(subs, supers);
			List<String> dmozCsc = ListUtils.intersection(dmozSc, common);
			csc = this.getNamesByDmozNames(dmozCsc);
		}else{
			Integer cid = this.getTermIdByName(c);
			List<Integer> subIds = this.getSubsInResult(cid);
			List<Integer> superIds = this.getSupersInResult(cid);
			List<Integer> commonIds = this.getCommonTermIds();
			List<Integer> scIds = ListUtils.union(subIds, superIds);
			List<Integer> cscIds = ListUtils.intersection(scIds, commonIds);
			csc = new ArrayList<String>();
			for(Integer id : cscIds){
				String s = this.getTermNameById(id);
				if(!csc.contains(s))
					csc.add(s);
			}
		}
		return csc;
	}
	
	/**
	 * Get all ohloh tag names covered by some dmoz concepts,
	 * one dmoz concept may refer to several ohloh tag
	 * @author Shockley Xiang Li
	 * @param dmoznames : set of dmoz concepts
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<String> getNamesByDmozNames(List<String> dmoznames){
		List<String> names  = new ArrayList<String>();
		if(dmoznames == null)
			return names;
		for(String dname : dmoznames){
			session = hs.getSession();
			tx = session.beginTransaction();
			sql = "select name from onto_terms where dmoz_name= '"+dname+"'";
			query = session.createSQLQuery(sql);
			List<String> terms = query.list();
			if(terms==null || terms.size()<1)
				logger.info("wrong dmoz name "+dname+" is given in the id list!");
			for(String t :terms){
				if(!names.contains(t))
					names.add(t);
			}
		}
		return names;
	}
	
	/**
	 * get ohloh tag names by the termIds
	 * @author Shockley Xiang Li
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<String> getTermNamesByIDs(List<Integer> ids){
		List<String> names  = new ArrayList<String>();
		if(ids == null)
			return names;
		for(Integer id : ids){
			session = hs.getSession();
			tx = session.beginTransaction();
			sql = "select name from onto_terms where id="+id;
			query = session.createSQLQuery(sql);
			List<String> terms = query.list();
			if(terms==null || terms.size()!=1)
				logger.info("wrong id "+id+" is given in the id list!");
			names.add(terms.get(0));
		}
		return names;
	}
	
	
	
	public double tp(String c, boolean dmoz){
		List<String> csc = csc(c, dmoz);
		List<String> rcsc = csc(c, !dmoz);
		double fenzi = ListUtils.intersection(csc, rcsc).size();
		double fenmu = csc.size();
		if(fenmu==0.0){
			return 0.0;
		}
		return fenzi/fenmu;
	}
	
	public double TP(){
		List<String> common = this.getCommonTermsInOhloh();
		double fenzi = 0.0;
		double fenmu = common.size();
		for(String c : common){
			fenzi += tp(c,false);
		}
		return fenzi/fenmu;
	}
	
	public double TR(){
		List<String> common = this.getCommonTermsInOhloh();
		double fenzi = 0.0;
		double fenmu = common.size();
		for(String c : common){
			fenzi += tp(c,true);
		}
		return fenzi/fenmu;
	}
	
	public static void main(String [] args){
		TaxoDmozEvaluator e = new TaxoDmozEvaluator();
		//int size = e.group.get(62).size();
		//e.getGroups();
		//e.getArmies(e.getRoot());
		//e.storeArmy();
		double tr = e.TR();
		double tp = e.TP();
		logger.info("tp "+tp);
		logger.info("tr "+tr);
	}
}
