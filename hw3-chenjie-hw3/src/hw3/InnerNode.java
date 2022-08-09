package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class InnerNode implements Node {
	private ArrayList<Field> keys;
	private ArrayList<Node> childrenNodes;
	private int numOfKey;
	private InnerNode parentNode;
	
	public InnerNode(int degree) {
		//your code here
		keys = new ArrayList<>();
		childrenNodes = new ArrayList<>();
		numOfKey = degree - 1;
		
	}
	
	public ArrayList<Field> getKeys() {
		//your code here
		return keys;
	}
	
	public ArrayList<Node> getChildren() {
		//your code here
		return childrenNodes;
	}

	public int getDegree() {
		//your code here
		return numOfKey;
	}
	public boolean isFull() {
		return keys.size() > numOfKey;
	}
	
	public boolean hasSpare() {
		return keys.size() - 1 >= (int)Math.ceil(numOfKey/2.0);
	}
	
	public boolean isLeafNode() {
		return false;
	}
	public Node findChildNodeByKey(Field key) {
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).compare(RelationalOperator.GTE, key)) {
				return childrenNodes.get(i);
			}
		}
		return childrenNodes.get(childrenNodes.size() - 1);
	}

	
	public void addChild(Node newNode) {
		Field newNodeField = newNode.getSearchKey();
		newNode.setParentNode(this);
		for (int i = 0; i < childrenNodes.size(); i++) {
			if (childrenNodes.get(i).getSearchKey().compare(RelationalOperator.GT,newNodeField)) {
				childrenNodes.add(i, newNode);
				
				updateKey();
				
				
				return;
			}
		
		}
		childrenNodes.add(newNode);
		updateKey();
	
		
	}
	public void updateKey() {
		ArrayList<Field> updatedKeyArrayList = new ArrayList<>();
		for (int i = 0; i < childrenNodes.size() - 1; i++) {
			updatedKeyArrayList.add(childrenNodes.get(i).getSearchKey());
		}
	this.keys = updatedKeyArrayList;
		
	}
	public boolean isUnderflow() {
		return keys.size() < (int)Math.ceil(numOfKey/2.0);
	}
	public Field getSearchKey() {
		
		return childrenNodes.get(childrenNodes.size() - 1).getSearchKey();
	}
	public void removeChild(Node node) {
		childrenNodes.remove(node);
		node.setParentNode(null);
		updateKey();
	}
	public ArrayList<Node> splitNode() {
		ArrayList<Node> resultAfterSplit = new ArrayList<>();
		
		InnerNode leftInnerNode = new InnerNode(numOfKey + 1);
		InnerNode rightInnerNode = new InnerNode(numOfKey + 1);
		
		int mid = (int) Math.ceil(childrenNodes.size() / 2.0);
		leftInnerNode.childrenNodes = new ArrayList<>(childrenNodes.subList(0, mid));
		leftInnerNode.updateKey();
		
		rightInnerNode.childrenNodes = new ArrayList<>(childrenNodes.subList(mid, childrenNodes.size()));
		rightInnerNode.updateKey();
		
		resultAfterSplit.add(leftInnerNode);
		resultAfterSplit.add(rightInnerNode);
		for (Node node : rightInnerNode.childrenNodes) {
			node.setParentNode(rightInnerNode);
		
		}
		for (Node node : leftInnerNode.childrenNodes) {
			node.setParentNode(leftInnerNode);
		}
		
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
		
		return keys.toString();
	}

}