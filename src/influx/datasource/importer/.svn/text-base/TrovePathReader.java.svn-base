/**
 * @Shockley Xiang Li
 * 2012-4-5
 */
package influx.datasource.importer;

import influx.datasource.DataSourceFactory;
import influx.datasource.HibernateService;
import influx.datasource.dao.Term;
import influx.datasource.dao.Relation;
import influx.datasource.dao.Source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Parse the trove-path txt file provided by Sourceforge Archive Dataset,
 * basically, 3 types of parsing facility are handled in this class:<br>
 * <br>
 * 1. parse relations and store it to database <br>
 * 2. parse concepts and store it to database <br>
 * 3. parse towards a graphviz readable file
 * 
 * @author Shockley
 * 
 */
public class TrovePathReader {
	public static Logger logger = Logger.getLogger("TroveTxtReader");
	public static String SFtroveTxt = "D:\\work\\experiment\\ontology\\TKDE\\sf\\sf-trove.txt";
	// public static String dmozOnto =
	// "D:\\work\\experiment\\ontology\\DMOZ\\software.ontology";
	public static HibernateService hs = DataSourceFactory
			.getHibernateInstance();
	/**
	 * store all trove path that starts with 'Topic' locally into Linked Lists
	 */
	private List<LinkedList<String>> trovePaths = new ArrayList<LinkedList<String>>();

	/**
	 * parse and store all trove path that starts with 'Topic' locally into
	 * Linked Lists
	 * 
	 * @author Shockley Xiang Li
	 */
	private void parseSFtrove() {
		try {
			File input = new File(SFtroveTxt);
			BufferedReader r = new BufferedReader(new FileReader(input));
			// read line after line
			for (String line = r.readLine(); line != null; line = r.readLine()) {
				if (!line.startsWith("Topic")) {
					continue;
				}
				// rest of the line after some previous troves are parsed and
				// stored
				String rest = line;
				int restLength = rest.length();
				// begin index of the next trove
				int nextBeginning = 0;
				LinkedList<String> trovePath = new LinkedList<String>();
				// break when no ' :: ' found
				for (int i = rest.indexOf(" :: "); i >= 0; i = rest
						.indexOf(" :: ")) {
					String trove = rest.substring(0, i);
					trovePath.addLast(trove);
					nextBeginning = rest.indexOf(" :: ") + 4;
					if (nextBeginning >= restLength) {
						logger
								.info("Impossible during parsing: The line is ended up with ' :: ', break parsing");
						break;
					}
					rest = rest.substring(nextBeginning, restLength);
					restLength = rest.length();
				}
				// the last ';' shall be eliminated
				trovePath.addLast(rest.substring(0, restLength - 1));
				trovePaths.add(trovePath);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * parse and store all trove path that starts with 'Topic' locally into
	 * Linked Lists
	 * 
	 * @author Shockley Xiang Li
	 */
	private void parseDMOZtrove() {
		Session session;
		Transaction tx;
		String sql = "select Topic from dmoz_computers_software";
		session = hs.getSession();
		tx = session.beginTransaction();
		List<String> paths = session.createSQLQuery(sql).list();
		// read line after line
		for (String line : paths) {
			if (!line.startsWith("Top/Computers/Software")) {
				logger.info("error: not software subtree");
				continue;
			}
			if (line.charAt(line.length() - 2) == '/') {
				continue;
			}
			// rest of the line after some previous troves are parsed and stored
			String rest = line;
			int restLength = rest.length();
			// begin index of the next trove
			int nextBeginning = 0;
			LinkedList<String> catPath = new LinkedList<String>();
			// break when no ' / ' found
			for (int i = rest.indexOf("/"); i >= 0; i = rest.indexOf("/")) {
				String cat = rest.substring(0, i);
				catPath.addLast(cat);
				nextBeginning = rest.indexOf("/") + 1;
				if (nextBeginning >= restLength) {
					logger
							.info("Impossible during parsing: The line is ended up with '/', break parsing");
					break;
				}
				rest = rest.substring(nextBeginning, restLength);
				restLength = rest.length();
			}
			catPath.addLast(rest.substring(0, restLength));
			trovePaths.add(catPath);
		}
		tx.commit();
		session.disconnect();
	}

	/**
	 * After parsing, import all concepts and broader relations into database
	 * e.g. if the path is gradpa::father::son then the relations include
	 * gradpa::father, gradpa::son, father::son
	 * 
	 * @author Shockley Xiang Li
	 */
	public void storeSFrelations() {
		parseSFtrove();
		Session session;
		Transaction tx;
		String hql;
		String sql;

		// 1. delete existed concepts and relations
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "truncate table onto_concepts";
		session.createSQLQuery(sql).executeUpdate();
		sql = "truncate table onto_relations";
		session.createSQLQuery(sql).executeUpdate();
		tx.commit();
		session.close();

		// 2. store all the concepts first!!!
		session = hs.getSession();
		tx = session.beginTransaction();
		for (LinkedList<String> path : trovePaths) {
			String last = path.getLast();
			hql = "from Concept c where name = '" + last + "'";
			List<Source> concepts = session.createQuery(hql).list();
			if (concepts.size() < 0) {
				logger.info("Database inconsistency!: Concept : break!");
				return;
			} else if (concepts.size() == 0) {
				Term c = new Term();
				c.setName(last);
				session.save(c);
			}
		}
		tx.commit();
		session.disconnect();

		// 3. import all boader relations
		session = hs.getSession();
		tx = session.beginTransaction();
		// get source
		hql = "from Source s where s.description = 'Truth'";
		List<Source> sources = session.createQuery(hql).list();
		if (sources.size() != 1) {
			logger.info("Database inconsistency!: sources : break!");
			return;
		}
		Source source = sources.get(0);

		for (LinkedList<String> path : trovePaths) {
			Term broaderConcept, narrowerConcept;

			// only the relations concerning the last element needs are new
			String son = path.getLast();
			hql = "from Concept c where c.name = '" + son + "'";
			List<Term> sons = session.createQuery(hql).list();
			if (sons.size() != 1) {
				logger.info("Database inconsistency!: concepts: break!");
				logger.info("query is " + son + " not 1!");
				logger
						.info("the resultlist size is " + sons.size()
								+ " not 1!");
				return;
			}
			narrowerConcept = sons.get(0);
			for (int i = 0; i < path.size() - 1; i++) {
				Relation relation = new Relation();
				String father = path.get(i);
				hql = "from Concept c where c.name = '" + father + "'";
				List<Term> fathers = session.createQuery(hql).list();
				if (fathers.size() != 1) {
					logger.info("Database inconsistency!: concepts: break!");
					return;
				} else
					broaderConcept = fathers.get(0);

				relation.setFrom(broaderConcept);
				relation.setTo(narrowerConcept);
				relation.setBroader(true);
				relation.setSource(source);
				session.save(relation);
			}
		}
	}

	/**
	 * After parsing, import all concepts and broader relations into database
	 * e.g. if the path is gradpa::father::son then the relations include
	 * gradpa::father, gradpa::son, father::son
	 * 
	 * @author Shockley Xiang Li
	 */
	public void storeDMOZrelations() {
		parseDMOZtrove();
		List<String> allTerms = new ArrayList<String>();
		allTerms.add("Computers");
		allTerms.add("Top");
		
		Session session;
		Transaction tx;
		String sql;

		// 1. delete existed terms and relations
		session = hs.getSession();
		tx = session.beginTransaction();
		sql = "truncate table dmoz_relation";
		session.createSQLQuery(sql).executeUpdate();
		sql = "truncate table dmoz_term";
		session.createSQLQuery(sql).executeUpdate();
		tx.commit();
		session.close();
		// 2. import all boader relations
		for (LinkedList<String> path : trovePaths) {
			session = hs.getSession();
			tx = session.beginTransaction();
			// only the relations concerning the last element needs are new
			String to = path.getLast();
			//check whether its an 'new' cat
			if(!allTerms.contains(to)){
				allTerms.add(to);
			}
			for (int i = 0; i < path.size() - 1; i++) {
				String from = path.get(i);
				
				sql = "insert into dmoz_relation (`from`,`to`) values (\"" + from
						+ "\", \"" + to + "\")";
				session.createSQLQuery(sql).executeUpdate();
			}
			tx.commit();
			session.close();
		}
		
		//3. insert all terms
		session = hs.getSession();
		tx = session.beginTransaction();
		for(String term : allTerms){
			sql = "insert into dmoz_term (name) values (\"" + term+ "\")";
			session.createSQLQuery(sql).executeUpdate();
		}
		tx.commit();
		session.close();
	}

	/**
	 * After parsing, import all cats and edges into database e.g. if the path
	 * is gradpa::father::son then the relations include gradpa::father,
	 * father::son, duplications are eliminated
	 * 
	 * @author Shockley Xiang Li
	 */
	public void parseAndPrintDMOZOntology(String filePath) {
		this.parseDMOZtrove();
		File file = new File(filePath);
		List<String> tos = new ArrayList<String>();
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			pw.println("digraph vis{");
			pw.println("rankdir=LR;");
			for (LinkedList<String> path : trovePaths) {
				String to = path.pollLast();
				String from = path.pollLast();
				// /*each has only one father, right?
				// if not, split each occurrence as a different cat
				int dupName = 0;
				for (String t : tos) {
					if (t.equals(to)) {
						dupName++;
					}
				}
				tos.add(to);
				if (dupName > 0)
					to += "_" + dupName;
				//
				// */
				String line = "\"" + from + "\" -> \"" + to + "\";";
				pw.println(line);
			}
			pw.println("}");
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @author Shockley Xiang Li
	 */
	public void storeSFtrove() {
		try {
			List<String> terms = new ArrayList<String>();
			File input = new File(SFtroveTxt);
			BufferedReader r = new BufferedReader(new FileReader(input));
			String line = r.readLine();
			// read line after line
			while (line != null) {
				int i = line.lastIndexOf(" :: ");
				if (i < 0) {
					System.out.println(" :: is not found in this line");
					line = r.readLine();
					continue;
				}
				int lastTrove = i + 4;
				String to = line.substring(lastTrove, line.length() - 1);
				if (!terms.contains(to))
					terms.add(to);
				line = line.substring(0, i);
				int j = line.lastIndexOf(" :: ");
				if (j < 0) {
					j = -4;
				}
				lastTrove = j + 4;
				String from = line.substring(lastTrove, line.length());
				if (!terms.contains(from))
					terms.add(from);
				line = r.readLine();
			}

			HibernateService hs = DataSourceFactory.getHibernateInstance();
			Session session = hs.getSession();
			Transaction tx = session.beginTransaction();
			for (String name : terms) {
				Term term = new Term();
				term.setName(name);
				session.save(term);
			}
			tx.commit();
			session.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Convert the original trove path file to a graphviz-readable file For the
	 * requirement graphviz, the output file contains all egde information (two
	 * nodes of each egde)
	 * 
	 * @author Shockley Xiang Li
	 * @param outPath
	 */
	public void sfTrove2Dot(String outPath) {
		File input = new File(SFtroveTxt);
		File output = new File(outPath);
		try {

			PrintWriter pw = new PrintWriter(new FileWriter(output));
			pw.println("digraph vis{");
			pw.println("rankdir=LR;");
			BufferedReader r = new BufferedReader(new FileReader(input));
			// String line = r.readLine();
			for (String line = r.readLine(); line != null; line = r.readLine()) {
				// currently, topic only
				if (!line.startsWith("Topic"))
					continue;
				int i = line.lastIndexOf(" :: ");
				if (i < 0) {
					System.out.println(" :: is not found in this line");
					line = r.readLine();
					continue;
				}
				int lastTrove = i + 4;
				String to = line.substring(lastTrove, line.length() - 1);

				line = line.substring(0, i);
				int j = line.lastIndexOf(" :: ");
				if (j < 0) {
					j = -4;
				}
				lastTrove = j + 4;
				String from = line.substring(lastTrove, line.length());
				int l = from.length();
				for (int c = 0; c < l; c++) {
					char x = from.charAt(c);
					if (x < 'a' || x > 'z') {
						if (x < 'A' || x > 'Z') {
							from = from.replace(x, '_');
						}
					}
				}
				l = to.length();
				for (int c = 0; c < l; c++) {
					char x = to.charAt(c);
					if (x < 'a' || x > 'z') {
						if (x < 'A' || x > 'Z') {
							to = to.replace(x, '_');
						}
					}
				}
				pw.println(from + "->" + to + ";");
			}
			r.close();
			pw.println("}");
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String dmozOnto = "D:\\work\\experiment\\ontology\\DMOZ\\software.ontology";
		TrovePathReader reader = new TrovePathReader();
		reader.storeDMOZrelations();
		// reader.printDMOZOntology(dmozOnto);
		// reader.sfTrove2Dot("D:\\work\\experiment\\ontology\\TKDE\\sf\\dot.sf-trove");
		// reader.importRelationsIntoDB();
	}
}
