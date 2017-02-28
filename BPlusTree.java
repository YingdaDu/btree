import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

/**
 * BPlusTree Class Assumptions: 
 * 1. No duplicate keys inserted 
 * 2. Order D: D <= number of keys in a node <= 2*D 
 * 3. All keys are non-negative
 * TODO: Rename to BPlusTree
 */

public class BPlusTree<K extends Comparable<K>, T> {

	public Node<K,T> root;
	public static final int D = 2;

	/**
	 * TODO Search the value for a specific key
	 * @param key
	 * @return value
	 */
	public T search(K key) {
		if(key == null) {return null;}
		else{return treeSearch(root, key);}
	}

	/**Helper Method: given a starting node and key, search the whole tree to 
	   get the value of this key. If not found, return null */
	public T treeSearch (Node<K,T> n, K key) {
		// corner case that nothing is in the tree
		if(n == null || n.keys.size()==0){return null;}
		
		// normal cases
		LeafNode<K,T> leafNode = getLeafNode(n, key); // find the LeafNode of this key
		int keySize = leafNode.keys.size();
		for (int i=0; i < keySize; i++) {
			if (key.compareTo(leafNode.keys.get(i)) == 0) 
				return leafNode.values.get(i);
		}
		return null;
	}


	/** Helper Method: Given a starting node and a key, return the 
	    LeafNode that the key initially should belong to. */
	public LeafNode<K,T> getLeafNode(Node<K,T> n, K key) {
		// if reaches a leafNode, return the leafNode
		if (n.isLeafNode) return (LeafNode<K,T>)n;
		// if reaches a indexNode, descent to a deeper indexNode or a leafNode 
		if (!n.isLeafNode) {
			IndexNode<K,T> indexNd = (IndexNode<K,T>)n; // cast Node n to LeafNode leaf
			int keySize = indexNd.keys.size();

			// iterate over keys in the node, find which child node should be pointed to
			for (int i=0; i< keySize; i++) {
				// find where the key should be pointed
				if (key.compareTo(indexNd.keys.get(i)) < 0) { 
					return getLeafNode(indexNd.children.get(i), key);
				} 
			}
			// if not found, means the key is larger any key in the node
			// use the right most pointer to find the next descendant node to search
			return getLeafNode(indexNd.children.get(keySize), key);
		}
		return null;
	}



	/**
	 * TODO Insert a key/value pair into the BPlusTree
	 * @param key
	 * @param value
	 */
	public void insert(K key, T value) {
		//if (root == null) 
			//return;
		insertTree(root, key, value, null);
	}

	/** Helper Method: Insert a key with a value into the BPlusTree, use recursion */
	public SimpleEntry<K, Node<K,T>> insertTree
	(Node<K,T> n,K key,T value, SimpleEntry<K, Node<K,T>> entry) {
		
		// beginning case, root is null
		// create a new leaf and set it as root
		if(n==null){
			LeafNode<K,T> new_root = new LeafNode<K,T>(key, value);
			root = new_root;
			return null;
		}
		
		// if n is leaf; insert and decide whether to return entry or null
		if (n.isLeafNode) {
			LeafNode<K,T> ln= (LeafNode<K,T>) n;
			ln.insertSorted(key, value);
			
			// if leaf is overflow
			if(ln.isOverflowed()) {
				entry= splitLeafNode(ln);
			    LeafNode<K,T> right= (LeafNode<K,T>) entry.getValue();
			    right.previousLeaf= ln;
			    right.nextLeaf = ln.nextLeaf;
			    ln.nextLeaf= right;
			    
			    // if the leaf node is the root
				// create a new root as an IndexNode and add the old and new leaf
			    // nodes into this children
				if(root == ln){
					IndexNode<K,T> new_root = new IndexNode<K,T>(entry.getKey(), ln, right);
					root = (Node<K,T>) new_root;
				}
				// return entry to outer function
				return entry;
			} else {
			
			// leaf not overflowed
			return null;
			}
		}
		
		// if n is an Index Node; recursively call inner insertTree and 
		// then act according to the entry returned by inner
		if(!n.isLeafNode){
			IndexNode<K,T> in= (IndexNode<K,T>) n;
			Node<K,T> childNode= getChild(in, key);
			entry = insertTree(childNode, key, value, entry);
			// if entry returned from inner is null, there nothing to do
			if(entry == null) {return null;}
			
			// else, current index node need to insert the returned entry
			in.insertSorted(entry,getIndex(in, entry.getKey()));
			// check if n is overflowed after insertion
			if(in.isOverflowed()) {
				// if in is the root
				if(in == root) {
					entry= splitIndexNode(in);
					IndexNode<K,T> newRoot= new IndexNode<K,T>(entry.getKey(), root, entry.getValue());        
					root= (Node<K,T>) newRoot; // set root to new root
					return null;
				}
				// if in isn't root, just return the new entry
				entry= splitIndexNode(in);
				return entry;
			}
		}
		return null;
	}

	/** Helper Method: Traverse the index tree, get the child node in the descent level 
	 *  where the current IndexNode should point to */
	public Node<K,T> getChild(IndexNode<K,T> in, K key) {
		Node<K,T> child= new Node<K,T>();
		for (int i= 0; i< in.keys.size(); i++) {
			if (key.compareTo((K)in.keys.get(i))< 0) {
				child= (Node<K,T>) in.children.get(i);
				return child;
			}
		}
		child= (Node<K,T>) in.children.get(in.keys.size());
		return child;
	}

	/** Helper Method: Get the index on a IndexNode to insert entry */	
	public int getIndex(IndexNode<K,T> n, K indexKey) {
		for (int i= 0; i< n.keys.size(); i++ ) {
			if (indexKey.compareTo((K) n.keys.get(i))< 0) {
				return i;
			}
		}
		return n.keys.size();
	}


	/**
	 * TODO Split a leaf node and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * @param leaf, any other relevant data
	 * @return the key/node pair as an Entry
	 */
	public SimpleEntry<K, Node<K,T>> splitLeafNode(LeafNode<K,T> leaf) {
		LeafNode<K,T> rightNode= new LeafNode<K,T>(new ArrayList<K>(), new ArrayList<T>());
		
		// removing by iterating a copy of original ArrayList 
		for(K k:new ArrayList<K>(leaf.keys)){
			int i = leaf.keys.indexOf(k);
			if(i>=D) rightNode.keys.add(leaf.keys.remove(i));
		}
		
		for(T v:new ArrayList<T>(leaf.values)){
			int i= leaf.values.indexOf(v);
			if(i>=D) rightNode.values.add(leaf.values.remove(i));
		}
		
		K slitingKey= (K) rightNode.keys.get(0);
		SimpleEntry<K,Node<K,T>> newEntry= new SimpleEntry<K,Node<K,T>>(slitingKey, (Node<K,T>) rightNode);  
		return newEntry;
	}


	/**
	 * TODO split an indexNode and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * @param index, any other relevant data
	 * @return new key/node pair as an Entry
	 */
	public SimpleEntry<K, Node<K,T>> splitIndexNode(IndexNode<K,T> index) {
		IndexNode<K,T> rightNode= new IndexNode<K,T>(new ArrayList<K>(),new ArrayList<Node<K,T>>());
		
		// reserve the splitKey, which is the (D+1)th key in index
		K splitKey = index.keys.get(D);
		index.keys.remove(D);
		
		// removing by iterating a copy of original ArrayList 
		for(K k:new ArrayList<K>(index.keys)){
			int i = index.keys.indexOf(k);
			if(i>=D) rightNode.keys.add(index.keys.remove(i));
		}

		for(Node<K,T> c:new ArrayList<Node<K,T>>(index.children)){
			int i= index.children.indexOf(c);
			if(i>D) rightNode.children.add(index.children.remove(i));
		}
		
		// make the entry that is going to be returned		
		SimpleEntry<K,Node<K,T>> newEntry= new SimpleEntry<K,Node<K,T>>(splitKey,(Node<K,T>)rightNode);
		return newEntry;
	}

	/**
	 * TODO Delete a key/value pair from this B+Tree
	 * @param key
	 */
	public void delete(K key) {
		Integer splitKeyIndex = new Integer(-1);
		deleteTree(null, root, key, splitKeyIndex);
	}
	
	/**
	 * Actual delete procedure 
	 */
	public Integer deleteTree(IndexNode<K,T> parent, Node<K,T> n, K key, Integer splitKeyIndex){
		// if the node is leaf
		if(n.isLeafNode){
			LeafNode<K,T> leaf = (LeafNode<K,T>) n;
			
			// use helper function to move key and value
			// if no removal happens, just return
			boolean removal_happens = removeKeyLeaf(leaf, key);
			if(!removal_happens) return -1;
			
			// if leaf is underflow
			if(leaf.isUnderflowed()){
				// if leaf itself is the root, nothing to worry unless
				// leaf is empty, in this case set root to null
				if(leaf == root){
					if(leaf.keys.size() == 0) {root = null;}
					return -1;
				}
				
				// if leaf isn't the root, then need to find siblings
				// helper function to find two siblings, left and right
				ArrayList<Node<K,T>> sibs = findSiblings(parent, (Node<K,T>)leaf);
				
				// use helper function to handle underflow
				// if in a redistribution, parent's key need to be changed
				// handleLeafNodeUnderflow should handle it
				splitKeyIndex = handleLeafNodeUnderflow
						(((LeafNode<K,T>)sibs.get(0)), ((LeafNode<K,T>)sibs.get(1)), parent);
				return splitKeyIndex;
			}
			// if leaf isn't underflow
			else {
				return -1;
			}
		} else if(!n.isLeafNode){
			// if the node is an IndexNode{
			IndexNode<K,T> in = (IndexNode<K,T>) n;
			Node<K,T> child = getChild(in, key);
			splitKeyIndex = deleteTree(in, child, key, splitKeyIndex);
			
			// if splitKeyIndex is -1, no need to do anything at this IndexNode
			if(splitKeyIndex == -1){
				return -1;
			} else { // if splitKeyIndex isn't -1, need to change this IndexNode
				removeKeyIndex(in, splitKeyIndex); // remove key and child given key
				// if IndexNode is underflow
				if(in.isUnderflowed()){
					// in is the root
					if(in == root){ 
						// if the root is empty, set new root as first child
						if(in.keys.size() == 0) {
							root = in.children.get(0);
							return -1;}
						// else do nothing
						else {return -1;}
					} 
					else { // in isn't a root
						ArrayList<Node<K,T>> sibs = findSiblings(parent, (Node<K,T>)in);

						// handleIndexNodeUnderflow should also handle situation
						// that the root becomes empty
						splitKeyIndex = handleIndexNodeUnderflow
								(((IndexNode<K,T>)sibs.get(0)), ((IndexNode<K,T>)sibs.get(1)), parent);
						return splitKeyIndex;
					}
				} 
				else {
					// IndexNode isn't underflow, return -1
					return -1;
				}
			}
		}
		// this case only happens if root is null and tries to delete from root
		return -1;
	}
	
	/**
	 * given a key and a leaf, remove the key and value from leaf if exist
	 * return a boolean to tell if something is removed
	 */
	public boolean removeKeyLeaf(LeafNode<K,T> leaf, K key){
		boolean flag = false;
		for(int i=0; i<leaf.keys.size(); i++){
			if(key.compareTo(leaf.keys.get(i)) ==0 ){
				leaf.keys.remove(i);
				leaf.values.remove(i);
				flag = true;
			}
		}
		return flag;
	}
	
	/**
	 * given a IndexNode and splitKeyIndex, remove the given key and child 
	 */
	public void removeKeyIndex(IndexNode<K,T> in, int splitKeyIndex){
		in.keys.remove(splitKeyIndex); // remove key
		in.children.remove(splitKeyIndex+1); // remove index
	}
	
	/**
	 * given a parent IndexNode and current node, find the target sibling
	 * return an ArrayList of nodes, 1st is the left node and 2nd is the right
	 */
	public ArrayList<Node<K,T>> findSiblings(IndexNode<K,T> parent, Node<K,T> n){
		ArrayList<Node<K,T>> siblings = new ArrayList<Node<K,T>>(); // initialize
		int i = parent.children.indexOf(n); // get the index of n in parent
		if(i > 0){ // n has a left sibling
			siblings.add((Node<K,T>) parent.children.get(i-1));
			siblings.add((Node<K,T>) n);
		} else{ // n doesn't have left, but has a right sibling
			siblings.add((Node<K,T>) n);
			siblings.add((Node<K,T>) parent.children.get(i+1));
		}
		return siblings;
	}
	
	
	/**
	 * TODO Handle LeafNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitKeyIndex position in parent if merged so that parent can
	 *         delete the useless key and child later on. -1 otherwise
	 */
	public Integer handleLeafNodeUnderflow
	(LeafNode<K,T> left, LeafNode<K,T> right,IndexNode<K,T> parent) {
		Integer splitKeyIndex = -1; // default value for splitKeyRemoval
		
		// We know left or right is less than D, if the other is equal to D
		// then we need to merge 
		// otherwise, the other is more than D; can redistribute
		if(left.keys.size() == D || right.keys.size() == D){
			splitKeyIndex = mergeLeaf(left, right, parent);
		} else{redistributeLeaf(left, right, parent);}
		
		return splitKeyIndex;
	}
	
	/**
	 * given an target node and nonTarget node, redistribute key/values from
	 * target node to nonTarget node
	 */
	public void redistributeLeaf
	(LeafNode<K,T> left, LeafNode<K,T> right, IndexNode<K,T> parent){
		// first collect all (key, value) pairs sorted by key
		ArrayList<SimpleEntry<K, T>> pairs = new ArrayList<SimpleEntry<K, T>>();
		// record the index of the right leaf in parent
		int rightIndex = parent.children.indexOf(right);
		
		// move (key value) pairs into pairs from both left and right leaves
		// use the helper function moveIntoPairs
		moveIntoPairs(pairs, left);
		moveIntoPairs(pairs, right);
		
		// clear left and right nodes, both keys and values
		left.keys.clear();
		left.values.clear();
		right.keys.clear();
		right.values.clear();
		
		
		// refill pairs: put n/2 into left and rest in right.
		// Java's floor divide will ensure left keys are less or equal to right
		for(int i= 0; i<pairs.size(); i++){
			if(i<(pairs.size()/2)){ // should be in the left node
				left.keys.add(pairs.get(i).getKey());
				left.values.add(pairs.get(i).getValue());
			} else{
				right.keys.add(pairs.get(i).getKey());
				right.values.add(pairs.get(i).getValue());
			}
		}
		
		// update the parent key that separate the left and right leaf
		// use the first key in right leaf to replace the previous key
		parent.keys.set(rightIndex-1, right.keys.get(0));
	}
	
	/**
	 * given left and right leaf nodes, copy right leaf into left leaf
	 * return a signal that the parent node of left and right need to change
	 */
	public Integer mergeLeaf
		(LeafNode<K,T> left, LeafNode<K,T> right, IndexNode<K,T> parent){
		// get the splitKeyIndex
		Integer splitKeyIndex = parent.children.indexOf(left);
		
		// initiate an ArrayList to store (key, values) pairs in right leaf
		ArrayList<SimpleEntry<K, T>> pairs = new ArrayList<SimpleEntry<K, T>>();
		
		// move (key, value) pairs into pairs and clear right leaf
		moveIntoPairs(pairs, right);
		
		// add pairs into left leaf
		for(int i=0; i<pairs.size(); i++){
			left.keys.add(pairs.get(i).getKey());
			left.values.add(pairs.get(i).getValue());
		}
		
		// change the left leaf's nextLeaf pointer and
		// the right leaf's right neighbour's previous leaf 
		left.nextLeaf = right.nextLeaf;
		if(right.nextLeaf != null){
			right.nextLeaf.previousLeaf = left;
		}
		
		// return splitKeyIndex (the index of left)
		return splitKeyIndex;
	}
	
	/**
	 * given a SimpleEntry pair and a LeafNode, add (key,value) into pair
	 * 
	 */
	public void moveIntoPairs
	(ArrayList<SimpleEntry<K, T>> pairs, LeafNode<K,T> leaf){
		for(int i= 0; i<leaf.keys.size(); i++){
			SimpleEntry<K, T> newPair = 
					new SimpleEntry<K, T>(leaf.keys.get(i), leaf.values.get(i));
			pairs.add(newPair);
		}
	}

	/**
	 * TODO Handle IndexNode Underflow (merge or redistribution)
	 * NEED to consider reset the root if root is empty
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitKeyIndex position in parent if merged so that parent can
	 *         delete the splitKeyIndex later on. -1 otherwise
	 */
	public Integer handleIndexNodeUnderflow
	(IndexNode<K,T> left, IndexNode<K,T> right, IndexNode<K,T> parent) {
		Integer splitKeyIndex = -1; // default value for splitKeyIndex

		// if either node's keys are just D, need to merge
		// else, redistribute
		if(left.keys.size() == D || right.keys.size() == D){
			splitKeyIndex = mergeIndex(left, right, parent);
		} else{redistributeIndex(left, right, parent);}

		return splitKeyIndex;
	}
	
	/**
	 * given left and right IndexNode, redistribute keys and children
	 */
	public void redistributeIndex
	(IndexNode<K,T> left, IndexNode<K,T> right, IndexNode<K,T> parent){
		// redistribution are done in two steps: 
		// 1. change the keys and 2. change the children in left and right
		
		// Step 1. change the keys
		// collect all keys could be involved in redistribution,sorted
		
		// initialize an ArrayList to store keys
		ArrayList<K> keys = new ArrayList<K>();
		// find the index of splitKeyIndex
		int splitKeyIndex = parent.children.indexOf(right)-1; 
		
		// move keys in left child, the splitting key in parent 
		// and keys in right child into keys; implemented by copy-and-delete-original
		for(K k:left.keys){keys.add(k);}
		keys.add(parent.keys.get(splitKeyIndex));
		for(K k:right.keys){keys.add(k);}
		left.keys.clear();
		right.keys.clear();
		
		// refill keys into the left, parent and right
		// assume keys.size() = n, the rule is 
		// the index for key in parent is(n-1)/2;
		// keys < (n-1)/2 go in left and > (n-1)/2 goes in right
		for(int i=0; i< keys.size(); i++){
			if(i<((keys.size()-1))/2){left.keys.add(keys.get(i));}
			else if(i == ((keys.size()-1)/2)){parent.keys.set(splitKeyIndex, keys.get(i));}
			else {right.keys.add(keys.get(i));}
		}
		
		// Step 2. change the children in left and right
		// collect all children in left and right, sorted
		
		// initialize an ArryList to store those children
		ArrayList<Node<K,T>> children = new ArrayList<Node<K,T>>();
		// add all children into "children" and delete original
		for(Node<K,T> c : left.children){children.add(c);}
		left.children.clear();
		for(Node<K,T> c : right.children){children.add(c);}
		right.children.clear();
		
		// refill children in left and right leaves
		// we can use the keys.size() to distribute children, the rule is
		// children whose index <= (n+1)/2 should be in left; others in the right
		for(int i=0; i<children.size(); i++){
			if(i<(keys.size()+1)/2){left.children.add(children.get(i));}
			else {right.children.add(children.get(i));}
		}
	}
	
	/**
	 * given left and right IndexNode, merge right into left
	 * return a signal to parent to remind parent to delete
	 */
	public Integer mergeIndex
	(IndexNode<K,T> left, IndexNode<K,T> right, IndexNode<K,T> parent){
		Integer splitKeyIndex = parent.children.indexOf(left);
		
		// first append key in the parent into left
		left.keys.add(parent.keys.get(splitKeyIndex));
		
		// then append keys and children in the right into left as well
		for(K k : right.keys){left.keys.add(k);}
		for(Node<K,T> n : right.children){left.children.add(n);}
		
		// finally return the splitKeyIndex so that the parent knows
		// it need to remove the key at index of splitKeyIndex and the following child 
		return splitKeyIndex;
	}
}
