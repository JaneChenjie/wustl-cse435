package hw3;


import java.util.ArrayList;


import hw1.Field;


public class BPlusTree {
	private Node rootNode;
	private int pInner;
	private int pLeaf;

    
    public BPlusTree(int pInner, int pLeaf) {
    	//your code here
    	this.pInner = pInner;
    	this.pLeaf = pLeaf;
    	this.rootNode = new LeafNode(pLeaf);
    
    }
    
    public LeafNode search(Field f) {
    	//your code here
    	Node curNode = rootNode;
    	while (!curNode.isLeafNode()) {
    		InnerNode curInnerNode = (InnerNode) curNode;
    		curNode = curInnerNode.findChildNodeByKey(f);
    	}
    	LeafNode curLeafNode = (LeafNode) curNode;
    	return  curLeafNode.containField(f) ? curLeafNode : null;
    }
    
    public void insert(Entry e) {
    	//your code here
    	rootNode = insert(rootNode, e);
    	if (rootNode.isFull()) {
    		rootNode = split(rootNode, null);
    	}
    	
    	
    	
    }
    private Node insert(Node curNode, Entry e) {
    	if (curNode.isLeafNode()) {
    		((LeafNode) curNode).addEntry(e);
    		return curNode;
    	}
    	
    	InnerNode curInnerNode = (InnerNode) curNode;
    	Node childNodeToInsert = curInnerNode.findChildNodeByKey(e.getField());
    	childNodeToInsert = insert(childNodeToInsert, e);
    
    	if (childNodeToInsert.isFull()) {
    		curInnerNode = split(childNodeToInsert, curInnerNode);
    	}
    	return curInnerNode;
    	
    }
    private InnerNode split(Node nodeToSplit, InnerNode parentNode) {
    	ArrayList<Node> resultAtferSplit = nodeToSplit.splitNode();
    	if (parentNode == null) {
    		parentNode = new InnerNode(pInner);
    		
    	} else {
    		parentNode.removeChild(nodeToSplit);
    	}
    	parentNode.addChild(resultAtferSplit.get(0));
		parentNode.addChild(resultAtferSplit.get(1));
    	return parentNode;
    }
    
    
    public void delete(Entry e) {
    	//your code here
    	LeafNode leafNodeToDelete = search(e.getField());
    	if (leafNodeToDelete == null) return;
    	
    	leafNodeToDelete.removeEntry(e);
    	rebalanceTree(leafNodeToDelete);
    	
    	
    }
    private void rebalanceTree(Node curNode) {
    	if (curNode == null) return;
    	if (!curNode.isUnderflow()) return;
    	
    	// the node is underflow
    	if (curNode == rootNode) {
    		if (curNode.isLeafNode()) return;
    		InnerNode curInnerNode = (InnerNode) curNode;
    		if (curInnerNode.getChildren().size() < 2) {
    			Node childNode = curInnerNode.getChildren().get(0);
    			childNode.setParentNode(null);
    			rootNode = childNode;
    			
    		}
    		return;
    	
    	}
    	Node leftSibling = curNode.getLeftSibling();
    	Node rightSibling = curNode.getRightSibling();
    	Node nodeToBurrow =  leftSibling != null && leftSibling.hasSpare() ? leftSibling : rightSibling;
    	Node nodeToMerge = leftSibling != null ? leftSibling : rightSibling;
    	InnerNode parentNode = curNode.getParentNode();
    	if (curNode.isLeafNode()) {
    		LeafNode curLeafNode = (LeafNode) curNode;
    		
    		if (nodeToBurrow != null && nodeToBurrow.hasSpare()) {
        		// burrow from sibling
    			LeafNode nodeToBurrowLeafNode = (LeafNode) nodeToBurrow;  			
    			Entry entryToBorrow = nodeToBurrowLeafNode == leftSibling ? 
    								nodeToBurrowLeafNode .getEntries().get(nodeToBurrowLeafNode.getEntries().size() - 1):
    								nodeToBurrowLeafNode.getEntries().get(0);
    						
    			nodeToBurrowLeafNode .removeEntry(entryToBorrow);
    			curLeafNode.addEntry(entryToBorrow);
    			parentNode.updateKey();
    		
    		
    		} else {
    			// merge a node
    			LeafNode nodeToMergeLeafNode = (LeafNode) nodeToMerge;
    			for (Entry entry : curLeafNode.getEntries()) {
    				nodeToMergeLeafNode.addEntry(entry);
    			}
    			parentNode.removeChild(curLeafNode);   			
    			
    		}
    	 
    	
    	} else {
    		// it is a inner node
    		InnerNode curInnerNode = (InnerNode) curNode;
    		if (nodeToBurrow != null && nodeToBurrow.hasSpare()) {
    			// burrow from a sibling
    			InnerNode nodeToBorrowInnderInnerNode = (InnerNode) nodeToBurrow;
    			
    			Node childNodeToBorrow = nodeToBorrowInnderInnerNode == leftSibling ?
    					nodeToBorrowInnderInnerNode.getChildren().get(nodeToBorrowInnderInnerNode.getChildren().size() - 1):
    					nodeToBorrowInnderInnerNode.getChildren().get(0);
    			
    			nodeToBorrowInnderInnerNode.removeChild(childNodeToBorrow);
    			curInnerNode.addChild(childNodeToBorrow);
    			parentNode.updateKey();
    		} else {
    			InnerNode nodeToMergeInnerNode = (InnerNode) nodeToMerge;
    			
    			for (Node childNode : curInnerNode.getChildren()) {
    				nodeToMergeInnerNode.addChild(childNode);
    			}
    			parentNode.removeChild(curInnerNode);
    			
    		}
    		
    	}
    	rebalanceTree(parentNode);
    	
		
		
	}

	
    
    public Node getRoot() {
    	//your code here
    	if (this.rootNode.isLeafNode() && ((LeafNode) rootNode).getEntries().size() == 0)
			return null;
    	return rootNode;
    }
    
    
	
}
