package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class LeafNode implements Node {
	private ArrayList<Entry> leafNodes;
	private int degree;
	private InnerNode parentNode;
	
	public LeafNode(int degree) {
		//your code here
		leafNodes = new ArrayList<>();
		this.degree = degree;
	}
	
	public ArrayList<Entry> getEntries() {
		//your code here
		return leafNodes;
	}

	public int getDegree() {
		//your code here
		return degree;
	}
	
	public boolean isLeafNode() {
		return true;
	}
	
	public boolean isFull() {
		return leafNodes.size() > degree;
	}
	public boolean containField(Field field) {
		for (Entry entry : leafNodes) {
			if (field.compare(RelationalOperator.EQ, entry.getField())) {
				return true;
			}
		}
		return false;
	}

	
	public void addEntry(Entry newEntry) {
		for (int i = 0; i < leafNodes.size(); i++) {
		
			if (leafNodes.get(i).getField().compare(RelationalOperator.EQ, newEntry.getField())) {
				return;
			}
			if (leafNodes.get(i).getField().compare(RelationalOperator.GT, newEntry.getField())) {
				leafNodes.add(i, newEntry);
				return;
			}
		}
		leafNodes.add(newEntry);
		
		
	}
	public void removeEntry(Entry e) {
		for (Entry en : leafNodes) {
			if (en.getField().compare(RelationalOperator.EQ, e.getField())) {
				leafNodes.remove(en);
				return;
			}
		}
	}
	
	public boolean isUnderflow() {
		return getEntries().size() < (int)Math.ceil(degree/2.0);
	}
	
	public boolean hasSpare() {
		return getEntries().size() - 1 >= (int)Math.ceil(degree/2.0);
	}
	public Field getSearchKey() {
		return this.leafNodes.get(leafNodes.size() - 1).getField();
	}
	public ArrayList<Node> splitNode() {
		ArrayList<Node> resultAfterSplit = new ArrayList<>();
		
		int mid = (int) Math.ceil(leafNodes.size() / 2.0);
		LeafNode leftLeafNode = new LeafNode(this.degree);
		LeafNode rightLeafNode = new LeafNode(this.degree);
		
		leftLeafNode.leafNodes = new ArrayList<>(leafNodes.subList(0, mid));
		rightLeafNode.leafNodes = new ArrayList<>((leafNodes.subList(mid, leafNodes.size())));
		
		resultAfterSplit.add(leftLeafNode);
		resultAfterSplit.add(rightLeafNode);
		
		return resultAfterSplit;
	}

	@Override
	public void setParentNode(InnerNode parentNode) {
		this.parentNode = parentNode;
		
	}

	@Override
	public InnerNode getParentNode() {
		
		return parentNode;
	}
	public Node getLeftSibling() {
		if (this.parentNode == null) return null;
		int indexOfCurNode = parentNode.getChildren().indexOf(this);
		if (indexOfCurNode <= 0) return null;
		
		return  parentNode.getChildren().get( indexOfCurNode - 1);
	}
	public Node getRightSibling() {
		if (this.parentNode == null) return null;
		int indexOfCurrentNode = parentNode.getChildren().indexOf(this);
		if (indexOfCurrentNode >= parentNode.getChildren().size() - 1) return null;
		
		return parentNode.getChildren().get(indexOfCurrentNode + 1);
	}
public String toString() {
		
		return getEntries().toString();
	}

	

}