package influx.datasource;
import org.hibernate.cfg.*;

/**
 * implements both plain db service and hibernate service as singletons
 * @author Shockley
 *
 */
@SuppressWarnings("unused")
public class DataSourceFactory {
	private static SimpleDBService sdbservice;
	private static HibernateService hservice;
	/**
	 * get singleton service
	 * @return
	 */
	public static SimpleDBService getSimpleDBInstance(){		
		if(sdbservice==null){
			sdbservice = new SimpleDBService();
		}
		return sdbservice;
	}
	/**
	 * get singleton service
	 * @return
	 */
	public static HibernateService getHibernateInstance(){
		if(hservice==null){
			hservice = new HibernateService();
		}
		return hservice;		
	}
}
