/**
 * @Shockley Xiang Li
 * 2012-6-19
 */
package influx.algo.cluster.evaluate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Shockley
 *
 */
public class LearnedOntologyPrepare {
	public static Logger logger = Logger.getLogger(LearnedOntologyPrepare.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	private Session session = null;
	private Transaction tx = null;
	private String sql = "";
	private SQLQuery query = null;
	
	/**
	 * the learned network
	 * key: hypernym (termid)
	 * value: direct chidren (termid)
	 */
	private Map<Integer, List<Integer>> group = new HashMap<Integer, List<Integer>>();
	/**
	 * the learned network, either
	 * key: hypernym (termid)
	 * value: all chidren (termid)
	 */
	private Map<Integer, List<Integer>> army = new HashMap<Integer, List<Integer>>();
	/**
	 * find the army from the root recursively
	 * @author Shockley Xiang Li
	 * @param current
	 * @return
	 */
	private List<Integer> getArmies(Integer current){
		List<Integer> children = group.get(current);
		List<Integer> currentArmy = new ArrayList<Integer>();;
		List<Integer> temp;
		if(children!=null && children.size()>0){
			currentArmy.addAll(children);
			for(Integer child : children){
				temp = getArmies(child);
				for(Integer c :temp){
					if(!currentArmy.contains(c)){
						currentArmy.add(c);
					}
				}
			}
		}
		army.put(current, currentArmy);
		return currentArmy;
	}
	public Map<Integer, List<Integer>> getArmy() {
		return army;
	}
	
	/**
	 * infer all the groups
	 *  (hypernym, children) from the original edge set
	 * @author Shockley Xiang Li
	 */
	private void getGroups(){
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select from_term,to_term from onto_network";
		query = session.createSQLQuery(sql);
		ScrollableResults pairs = query.scroll();
		while(pairs.next()){
			if(pairs.get(1)!=null && pairs.get(0)!=null){
				Integer from = (Integer) pairs.get(0);
				Integer to = (Integer) pairs.get(1);
				List<Integer> tos;
				if(group.containsKey(from)){
					tos = group.get(from);
					if(!tos.contains(to)){
						tos.add(to);
					}
				}else{
					tos = new ArrayList<Integer>();
					tos.add(to);
				}
				group.put(from, tos);
			}
		}
	}
	
	/**
	 * The army is flattened and all relations are stored in onto_relations table
	 * grandpa - grandson relations are included
	 * @author Shockley Xiang Li
	 */
	private void storeArmy(){
		List<Integer> keys = new ArrayList<Integer>();
		keys.addAll(army.keySet());
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "truncate table onto_relations";
		query = session.createSQLQuery(sql);
		query.executeUpdate();
		tx.commit();
		session.close();
		for(Integer k : keys){
			List<Integer> children = army.get(k);
			session = hs.getSession();
			tx = session.beginTransaction();
			for(Integer c: children){
				sql = "insert into onto_relations set from_term="+k+", to_term="+c;
				query = session.createSQLQuery(sql);
				query.executeUpdate();
			}
			tx.commit();
			session.close();
		}
	}
	
	/**
	 * find the root
	 * @author Shockley Xiang Li
	 * @return
	 */
	private Integer getRoot(){
		Integer temp = 63;
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "select from_term from onto_network where to_term =" + temp;
		query = session.createSQLQuery(sql);
		List<Integer> froms = query.list();
		while(froms!=null && froms.size()>0){
			if(froms.size()!=1)
				logger.error("!!");
			temp = froms.get(0);
			sql = "select from_term from onto_network where to_term ="+temp;
			query = session.createSQLQuery(sql);
			froms = query.list();
		}
		tx.commit();
		session.close();
		return temp;
	}
	
	/**
	 * this can be called
	 * to transfer the edge set into an army
	 * @author Shockley Xiang Li
	 */
	public void getArmyAndStore(){
		this.getGroups();
		this.getArmies(this.getRoot());
		this.storeArmy();
	}
	
	public static void main(String [] args){
		LearnedOntologyPrepare l = new LearnedOntologyPrepare();
		l.getArmyAndStore();
	}
}
