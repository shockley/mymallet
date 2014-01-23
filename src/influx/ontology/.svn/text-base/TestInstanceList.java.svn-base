/**
 * @Shockley Xiang Li
 * 2012-3-26
 */
package influx.ontology;

import java.io.File;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

/**
 * This proves that each list has its own vocabulary
 * @author Shockley
 */
public class TestInstanceList {
	public static void main(String [] args){
		InstanceList newList= InstanceList.load(
				new File("D:\\work\\experiment\\ontology\\TKDE\\sf\\sf3000.mallet") );
		InstanceList oldList= InstanceList.load(
				new File("D:\\work\\experiment\\ontology\\TKDE\\sf\\sf3000combined.mallet") );
		for (Instance inst : newList){
			String absoFilePath = inst.getSource().toString();
			int start = absoFilePath.lastIndexOf('\\')+1;
			int end = absoFilePath.lastIndexOf(".txt");
			String fileName = absoFilePath.substring(start,end);
			System.out.println(" "+fileName);
			FeatureSequence fs = (FeatureSequence)inst.getData();
			for(int i =0; i < fs.getLength();i++){
				Object o = fs.getObjectAtPosition(i);
				int index = oldList.getDataAlphabet().lookupIndex(o);
				System.out.print(index+" ");
			}
			break;
		}
	}
}
