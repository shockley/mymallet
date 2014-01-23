package influx.ontology.cikm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class HashMapUtils {

	public List<Map.Entry<String, Integer>> hashMapSortInteger(
			HashMap<String, Integer> hash, final boolean byValue) {
		List<Map.Entry<String, Integer>> infor = new ArrayList<Map.Entry<String, Integer>>(
				hash.entrySet());
		Collections.sort(infor, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				if (byValue) {
					return (o1.getValue().compareTo(o2.getValue()));
				} else {
					return (o1.getKey().toString().compareTo(o2.getKey()
							.toString()));
				}
			}
		});
		return infor;
	}

	public List<Map.Entry<Integer, Double>> hashMapSortDouble(
			HashMap<Integer, Double> hash, final boolean byValue) {
		List<Map.Entry<Integer, Double>> infor = new ArrayList<Map.Entry<Integer, Double>>(
				hash.entrySet());
		Collections.sort(infor, new Comparator<Map.Entry<Integer, Double>>() {
			public int compare(Map.Entry<Integer, Double> o1,
					Map.Entry<Integer, Double> o2) {
				if (byValue) {
					return (o2.getValue().compareTo(o1.getValue()));
				} else {
					return (o2.getKey().toString().compareTo(o1.getKey()
							.toString()));
				}
			}
		});
		return infor;
	}

	public String[] keysSortedByIntegerValue(HashMap<String, Integer> hash,
			final boolean byValue) {
		List<?> key_values = hashMapSortInteger(hash, byValue);
		int length = key_values.size();
		String[] keys = new String[length];
		for (int i = 0; i < length; i++) {
			String key_value = key_values.get(i).toString();
			keys[i] = key_value.substring(0, key_value.indexOf("="));
		}
		return keys;
	}

	public int[] keysSortedByDoubleValue(HashMap<Integer, Double> hash,
			final boolean byValue) {
		List<?> key_values = hashMapSortDouble(hash, byValue);
		int length = key_values.size();
		int[] keys = new int[length];
		for (int i = 0; i < length; i++) {
			String key_value = key_values.get(i).toString();
			keys[i] = Integer.parseInt((key_value.substring(0,
					key_value.indexOf("="))));
		}
		return keys;
	}

	/**
	 *  Given a key in a hashmap reduce the value
	 * @author Tao Wang
	 * @param hash
	 * @param key
	 * @param reduce_num : the gap to be reduced
	 */
	public void hashMapReduce(HashMap hash, String key, int reduce_num) {
		int original_value = 0;
		if (hash.containsKey(key)) {
			original_value = (Integer) hash.get(key);
		}
		hash.put(key, original_value - reduce_num);
	}

	public void projHashMapAttach(HashMap<String, ArrayList<String>> hash,
			Integer key, String obj) {
		ArrayList<String> original_list = new ArrayList<String>();
		String target = Integer.toString(key);
		if (hash.containsKey(target)) {
			original_list = hash.get(target);
		}
		original_list.add(obj);
		hash.put(String.valueOf(key), original_list);
	}

	public void tagHashMapAttach(HashMap<String, ArrayList<String>> hash,
			String key, Integer proj_id) {
		ArrayList<String> original_list = new ArrayList<String>();
		if (hash.containsKey(key)) {
			original_list = hash.get(key);
		}
		original_list.add(String.valueOf(proj_id));
		hash.put(key, original_list);
	}

	public void hashMapRemove(HashMap<String, ArrayList<String>> hash,
			String key, String target) {
		ArrayList<String> original_list = new ArrayList<String>();
		if (hash.containsKey(key)) {
			original_list = hash.get(key);
			original_list.remove(target);
			hash.put(key, original_list);
		}

	}

	public static void main(String[] args) {

		HashMapUtils hmu = new HashMapUtils();
		//
		// HashMap<Object, ArrayList> hash = new HashMap<Object, ArrayList>();
		// ArrayList<String> al = new ArrayList<String>();
		// al.add("aaa");
		// hash.put("a", al);
		// // hmu.projHashMapAttach(hash,"a","bbb");
		// System.out.println(hash.get("a").get(1).toString());
		HashMap hash = new HashMap<String, Integer>();
		hash.put("ad", 4000);
		hash.put("cd", 1123);
		hash.put("ba", 55);
		hash.put("da", 2);
		// String tt[] =
		Object tt[] = hmu.keysSortedByIntegerValue(hash, true);
		for (int i = 0; i < tt.length; i++) {
			System.out.println(Integer.parseInt(tt[i].toString()));
		}
		// hash.remove("ab");
		// Iterator it = hash.keySet().iterator();
		// while(it.hasNext()){
		// String key = it.next().toString();
		// System.out.println(key+" "+hash.get(key));
		// }
		for (int i = 0; i < tt.length; i++) {
			System.out.println(tt[i]);
		}
	}

}
