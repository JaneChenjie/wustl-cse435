package hw3;

import java.util.ArrayList;

import hw1.Field;

public interface Node {
	
	
	public int getDegree();
	public boolean isLeafNode();

	public Field getSearchKey();
	public boolean isFull();
	public ArrayList<Node> splitNode();
	public void setParentNode(InnerNode parentNode);
	public InnerNode getParentNode();
	public boolean isUnderflow();
	public Node getRightSibling();
	public Node getLeftSibling();
	public boolean hasSpare();
}
