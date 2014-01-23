/**
 * @Shockley Xiang Li
 * 2012-5-10
 */
package influx.datasource.util;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;
import influx.datasource.dao.LdaAttribute;
import influx.ontology.TkdeLdaOntology;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Shockley
 *
 */
public class TempDatabaseUtil {
	public static Logger logger = Logger.getLogger(TempDatabaseUtil.class);
	public HibernateService hs = DataSourceFactory.getHibernateInstance();
	public void test(){
		Session s = hs.getSession();
		Transaction tx = s.beginTransaction();
		String hql = "from LdaAttribute l";
		List<LdaAttribute> atts = s.createQuery(hql).list();
		for(LdaAttribute a : atts){
			if(a==null || a.getAttributeName()==null)
				continue;
			String old = a.getAttributeName();
			if(old.contains("concept")){
				String nEw = old.replace("concept", "term");
				a.setAttributeName(nEw);
				s.saveOrUpdate(a);
			}
		}
		tx.commit();
		s.clear();
	}
}
