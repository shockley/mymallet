package influx.datasource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

public class HibernateService {
	public HibernateService() {
		sessionFactory = configuration.configure().buildSessionFactory();
	}
	private Logger logger = Logger.getLogger(HibernateService.class);
	private SessionFactory sessionFactory;
	// if trying to do the class-table mapping using the hibernate.cfg.xml
	// we need AnnotationConfiguration()
	private Configuration configuration = new AnnotationConfiguration();

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public Session getSession() {
		return sessionFactory.openSession();
	}

	public void hello() {
		System.out.println("s");
	}

	public <T> boolean addTuples(List<T> tuples) throws HibernateException {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		for (T tuple : tuples) {
			session.save(tuple);
		}
		tx.commit();
		session.close();
		return true;
	}

	/**
	 * Add a tuple, which shall be previously absent in db
	 * 
	 * @param <T>
	 * @param tuple
	 * @return
	 * @throws Exception
	 */
	public <T> boolean addTuple(T tuple) throws Exception {
		
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.save(tuple);
		tx.commit();
		session.close();
		
		return true;
	}

	/**
	 * update a tuple, which shall be previously existed in db
	 * 
	 * @param <T>
	 * @param tuple
	 * @return
	 * @throws Exception
	 */
	public <T> boolean updateTuple(T tuple) throws Exception {

		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.update(tuple);
		tx.commit();
		session.close();
		
		return true;
	}

	public <T> boolean deleteTuple(T tuple) throws Exception {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.delete(tuple);
		tx.commit();
		session.close();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.influx.core.internal.service.db.IHDBService#deleteTuples(java.util
	 * .List)
	 */
	public <T> boolean deleteTuples(List<T> tuples) throws Exception {
		
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		for (T tuple : tuples) {
			session.delete(tuple);
		}
		tx.commit();
		session.close();
		return true;

	}

	public int executeUpdate(String hql, Map<String, Object> params) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			Query query = session.createQuery(hql);
			if (params != null) {
				for (String param : params.keySet()) {
					query.setParameter(param, params.get(param));
				}
			}
			tx.commit();
			session.close();
			return query.executeUpdate();

		} catch (QueryException e) {
			throw e;
		} catch (HibernateException e) {
			return -1;
		} catch (ClassCastException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * @param clazz: the class name of the DAO
	 * @param attr: the name of the attr in the DAO class
	 * @param value
	 * @return
	 * @throws Exception
	 * @example generalFindBy("Project","realname","xyz");
	 */
	public List<?> generalFindBy(String clazz, String attr, String value) throws Exception{
		
		String hql = "from " + clazz + " obj where obj." + attr + " = \'" + value + "\'";
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery(hql);
		List<?> results = q.list();
		tx.commit();
		session.close();
		return results;
	}
	
//	public List<?> findByRealName(String value) throws Exception{
//		String table = "projects";
//		String column = "proj_real_name";
//		List<?> results = generalFindBy(table,column,value);
//		return results;
//	}
	/**
	 * start : index of the start result limit : max results number
	 * 
	 * @param hql
	 * @param params
	 * @param collectionParams
	 * @param lockForUpdate
	 * @param start
	 * @param limit
	 * @return
	 * @throws QueryException
	 */
	public List<?> doHQL(String hql, Map<String, Object> params,
			Map<String, Collection> collectionParams, boolean lockForUpdate,
			int start, int limit) throws QueryException {
		try {
			List<?> results = null;
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			Query query = session.createQuery(hql);
			if (params != null) {
				for (String param : params.keySet()) {
					query.setParameter(param, params.get(param));
				}
			}
			if (collectionParams != null) {
				for (String param : collectionParams.keySet()) {
					query.setParameterList(param, collectionParams.get(param));
				}
			}
			if (lockForUpdate) {
				query.setLockMode("foo", LockMode.PESSIMISTIC_WRITE);
			}
			if (start >= 0 && limit >= 0) {
				query.setFirstResult(start);
				query.setMaxResults(limit);
			}
			results = query.list();
			tx.commit();
			session.close();
			return results;
		} catch (QueryException e) {
			throw e;
		} catch (HibernateException e) {
			return Collections.emptyList();
		} catch (ClassCastException e) {
			QueryException ebis = new QueryException(
					"Invalid HQL query parameter type: " + e.getMessage(), e);

			throw ebis;
		}
	}

	
	/**
	 * the hql only accept one ? and one string value
	 * @param hql
	 * @param value
	 * @param lockForUpdate
	 * @return
	 * @throws Exception
	 */
	public List<?> doHQL(String hql, String value, boolean lockForUpdate) throws Exception {
			List<?> results = null;
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			Query query = session.createQuery(hql).setString(0,value);
			if (lockForUpdate) {
				query.setLockMode("foo", LockMode.PESSIMISTIC_WRITE);
			}
			results = query.list();
			tx.commit();
			session.close();
			return results;
	}
	
	/**
	 * @param <T>
	 * @param daoClass
	 * @param id
	 * @param useLock
	 *            , if true, use upgrade lock
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findObjectById(Class<T> daoClass, long id, boolean useLock)
			throws Exception {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		T result = (T) (useLock ? session
				.get(daoClass, id, LockOptions.UPGRADE) : session.get(daoClass,
				id));
		tx.commit();
		session.close();
		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.influx.core.internal.service.db.IHDBService#shutdown()
	 */
	public void shutdown() {
		getSessionFactory().close();
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		HibernateService hdbsc = new HibernateService();
		Map<String, Object> params = new HashMap<String, Object>();
		Long id = (long) 55;
		params.put("id", id);
		String query = "select p.languages from Project p where p.id =:id";
		List<String> languages = (List<String>) hdbsc.doHQL(query, params,
				null, false, -1, -1);
		for (String language : languages) {
			System.out.println(language);
		}
	}
}
