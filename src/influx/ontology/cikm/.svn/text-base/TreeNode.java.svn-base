package influx.ontology.cikm;

import java.util.ArrayList;

public class TreeNode {
	private int ID;
	private TreeNode parent;
	private ArrayList<TreeNode> sons = new ArrayList<TreeNode>();

	public TreeNode() {
		this.ID = -1;
		this.parent = null;
		this.sons = new ArrayList<TreeNode>();
	}

	public int getID() {
		return this.ID;
	}

	public TreeNode getParent() {
		return this.parent;
	}

	public ArrayList<TreeNode> getSons() {
		return this.sons;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public void setSons(TreeNode son) {
		this.sons.add(son);
	}

	public ArrayList<Integer> getAncestors() {
		ArrayList<Integer> ancestors = new ArrayList<Integer>();
		TreeNode parent = this.parent;
		if(parent ==null){
			return null;
		}else{
			while (parent != null) {
				ancestors.add(parent.ID);
				parent = parent.getParent();
			}
			return ancestors;
		}
		
	}

}
