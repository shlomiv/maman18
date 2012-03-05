package maman18.data;

import java.util.Comparator;

/**
 * @author Shlomi.v
 *
 * @param <K> - the type of the Key
 * @param <V> - the type of the Values
 * 
 * This is an implementation of RB-Tree. 
 * To build this class I used various reading materials:
 *  (*) The courses text book, 
 *  (*) the implementation of java.util.TreeMap
 *  (*) Wikipedia and other online sources 
 * 
 */
public class RBTree<K, V> {

	enum Color {
		RED, BLACK
	};

	final protected Comparator<K> order;

	/**
	 * @param order
	 * I didnt want to force the default ordering of keys, 
	 * so the constructor has to get the ordering of keys as the user wants
	 * 
	 * notice this method is private. in order to construct a new RBTree, see empty below
	 */
	private RBTree(Comparator<K> order) {
		this.order = order;
	}
	
	/**
	 * @param order
	 * @return a new empty RBTree, with the given ordering
	 */
	static public <K, V> RBTree<K, V> empty(Comparator<K> order) {
		return new RBTree<K, V>(order);
	}

	/**
	 * @author Shlomi.v
	 * the internal representation of a node in the tree,
	 * including helper methods to navigate between the nodes.
	 */
	private class Node {
		K key;
		V value;
		Node parent;
		Node left;
		Node right;
		Color color;
		@Override
	    public String toString() {
	    	return "("+key+","+value+"," +color+")";
	    }
		Node(K key, V value, Node parent, Node left, Node right, Color color) {
			this.key = key;
			this.value = value;
			this.left = left;
			this.right = right;
			this.parent = parent;
			this.color = color;
			if (left != null)
				left.parent = this;
			if (right != null)
				right.parent = this;
		}

		/* helper node navigation methods */
		boolean isLeftChild() {
			return parent.left == this;
		}

		boolean isRightChild() {
			return parent.right == this;
		}

		Node uncle() {
			return parent.brother();
		}

		Node grandparent() {
			return parent.parent;
		}

		Node brother() {
			return isLeftChild() ? parent.right : parent.left ;
		}

		/**
		 * @return the node that has the largest value
		 * a recursive method that only goes right
		 * Complexity: O(lgn)
		 */
		private Node maximumNode() {
			if (right == null)
				return this;
			return right.maximumNode();
		}

		/**
		 * @param key
		 * @param value
		 * @return the newly inserted node
		 * this is a recursive implementation of tree insert
		 */
		private Node insert(K key, V value) {
			int ord = order.compare(key, this.key);
			if (ord == 0) {
				this.value = value;
				return this;
			}

			if (ord < 0) {
				if (left != null) return left.insert(key, value);
				else {
					left = new Node(key, value, this, null, null, Color.RED);
					return left;
				}
			} else {
				if (right != null) return right.insert(key, value);
				else {
					right = new Node(key, value, this, null, null, Color.RED);
					return right;
				}
			}
		}

		/**
		 * @return the maximum depth of the tree rooted in this node
		 * this is a helper method for testing..
		 */
		private int maxDepth() {
			if (left == null && right == null) return 1;
			if (left == null && right != null) return 1+right.maxDepth();
			if (left != null && right == null) return 1+left.maxDepth();
			return 1+Math.max(left.maxDepth(), right.maxDepth());
		}
		
		/**
		 * @return the number of elements in the tree rooted in this node
		 */
		private int getSize() {
			return 1 + ((left == null) ? 0 : left.getSize()) + ((right == null) ? 0 : right.getSize());
		}
		
		/**
		 * @param action - the action that will get executed during tree traversal
		 * perform an in-order scan of this tree, running action over each item
		 */
		public void foreach(Do<V> action) {
			if (left != null) left.foreach(action);
			action.action(value);
			if (right != null) right.foreach(action);
		}
	}

	/**
	 * @param key
	 * @param value
	 * this method adds a new key/value pair to this RBTree
	 */
	public void put(K key, V value) {
		// if we have an empty tree, so simply insert a root node and make sure its black
		if (root == null) {
			root = new Node(key, value, null, null, null, Color.BLACK);
			return;
		}

		// this tree is not empty, so insert a new node to the root tree, and fix it to satisfy all 5 red-black properties
		fixRedBlackInsert(root.insert(key, value));
	}

	/**
	 * @param node
	 * @return the color of the node
	 * ***NOTE*** we are using nulls instead of Nil[T], so we define a null node to have the color Black
	 */
	Color getColor(final Node node) {
		// return the color of the node, a null node is black (instead of Nil[T])
		return node == null ? Color.BLACK : node.color;
	}

	// the RBTree's root
	Node root = null;

	/**
	 * @param key 
	 * @return the found node
	 * an iterative find node method. each loop goes one level down the tree.
	 * Complexity: O(lgn)
	 */
	public Node findNode(final K key) {
		Node p = root;
		while (p != null) {
			int theOrder = order.compare(key, p.key);
			if (theOrder == 0)
				return p;
			p = theOrder < 0 ? p.left : p.right;
		}
		return p;
	}

	/**
	 * @param key
	 * @return the value associated with the given key
	 * Complexity: O(lgn)
	 */
	public V get(final K key) {
		final Node n = findNode(key);
		return n == null ? null : n.value;
	}

	
	/**
	 * @param n - the node to rotate
	 */
	private void rotateLeft(final Node n) {
		final Node r = n.right;
		replaceNode(n, r);
		n.right = r.left;
		if (r.left != null)
			r.left.parent = n;
		r.left = n;
		n.parent = r;
	}

	/**
	 * @param n - the node to rotate
	 */
	private void rotateRight(final Node n) {
		final Node l = n.left;
		replaceNode(n, l);
		n.left = l.right;
		if (l.right != null)
			l.right.parent = n;
		l.right = n;
		n.parent = l;
	}
	
	/**
	 * @param oldn - the node to override
	 * @param newn - the node that will override
	 * a helper method for rotating nodes
	 */
	private void replaceNode(final Node oldn, final Node newn) {
		if (oldn.parent == null) 
			root = newn;
		else 
			if (oldn.isLeftChild()) oldn.parent.left = newn;
			else oldn.parent.right = newn;
		
		if (newn != null) newn.parent = oldn.parent;
	}


	
	/**
	 * @param z
	 * fix insert in a red black tree
	 */
	private void fixRedBlackInsert(final Node z) {
		// if we reached the tree's head, make sure its black
		if (z.parent == null)
			z.color = Color.BLACK; 
		else {
			// otherwise, fix all cases..
			if (getColor(z.parent) == Color.BLACK)
				return;
			else {
				// case 1
				if (getColor(z.uncle()) == Color.RED) {
					z.parent.color = Color.BLACK;
					z.uncle().color = Color.BLACK;
					z.grandparent().color = Color.RED;
					fixRedBlackInsert(z.grandparent());
				} else {
					// case 2
					if (z.isRightChild() && z.parent.isLeftChild()) {
						rotateLeft(z.parent);
						finalInsertFix(z.left);
						return;
					} else if (z.isLeftChild() && z.parent.isRightChild()) {
						rotateRight(z.parent);
						finalInsertFix(z.right);
						return;
					}
					finalInsertFix(z);
				}
			}
		}
	}

	
	/**
	 * @param z
	 * this method is a helper to the fixup method..
	 */
	private void finalInsertFix(Node z) {
		// case 3
		z.parent.color = Color.BLACK;
		z.grandparent().color = Color.RED;
		if (z.isLeftChild() && z.parent.isLeftChild()) {
			rotateRight(z.grandparent());
		} else {
			rotateLeft(z.grandparent());
		}
	}

	/**
	 * @param key
	 * remove an item by its key.
	 * since in this implementation, node is an internal class that does not get exposed outside
	 * we dont remove the node by a reference to an existing node, rather we have to first 
	 * find the node in a tree to get the reference and then remove it.
	 * 
	 * Complexity: O(lgn)
	 */
	public void remove(K key) {
		Node n = findNode(key);
		if (n == null)
			return; // Key not found, do nothing
		if (n.left != null && n.right != null) {
			// Copy key/value from predecessor and then delete it instead
			Node pred = n.left.maximumNode();
			n.key = pred.key;
			n.value = pred.value;
			n = pred;
		}

		Node child = (n.right == null) ? n.left : n.right;
		if (getColor(n) == Color.BLACK) {
			n.color = getColor(child);
			fixRedBlackDelete(n);
		}
		replaceNode(n, child);

		if (getColor(root) == Color.RED) {
			root.color = Color.BLACK;
		}

	}

	/**
	 * @param z 
	 * this method runs up the tree, fixing colors and rotating nodes to maintain all 5
	 * RBTree attributes
	 * Complexity: O(lgn)
	 */
	private void fixRedBlackDelete(final Node z) {
		if (z.parent == null)
			return;
		else {
			// case 1, z is red
			if (getColor(z.brother()) == Color.RED) {
				z.parent.color = Color.RED;
				z.brother().color = Color.BLACK;
				if (z.isLeftChild())
					rotateLeft(z.parent);
				else
					rotateRight(z.parent);
			}
			// case 2, z's brother is black, and both its sons are black
			if (getColor(z.brother()) == Color.BLACK
					&& getColor(z.brother().left) == Color.BLACK
					&& getColor(z.brother().right) == Color.BLACK) {
				
				z.brother().color = Color.RED;
				if (getColor(z.parent) == Color.BLACK) fixRedBlackDelete(z.parent);
				else z.parent.color = Color.BLACK; 
			} 
			else {
				// case 3, z's brother is black, left child is red and right child is black
				if (z.isLeftChild()
					&& getColor(z.brother()) == Color.BLACK
					&& getColor(z.brother().left) == Color.RED
					&& getColor(z.brother().right) == Color.BLACK) {
					
					z.brother().color = Color.RED;
					z.brother().left.color = Color.BLACK;
					rotateRight(z.brother());
				} else if (z.isRightChild()
						&& getColor(z.brother()) == Color.BLACK
						&& getColor(z.brother().right) == Color.RED
						&& getColor(z.brother().left) == Color.BLACK) {
					
					z.brother().color = Color.RED;
					z.brother().right.color = Color.BLACK;
					rotateLeft(z.brother());
				}
				
				// case 4, fix the colors of z's brother and z's parent
				z.brother().color = getColor(z.parent);
				z.parent.color = Color.BLACK;
	
				if (z.isLeftChild()) {
					z.brother().right.color = Color.BLACK;
					rotateLeft(z.parent);
				} else {
					z.brother().left.color = Color.BLACK;
					rotateRight(z.parent);
				}
			}
		}

	}

	/**
	 * @return the maximum depth in the tree
	 * used to test that the tree.. 
	 */
	public int maxDepth() {
		if (root == null) return 0;
		return root.maxDepth();
	}
	
	/**
	 * @param key
	 * @return true iff the key exists
	 */
	public boolean containsKey(K key) {
		return get(key) != null;
	}

	/**
	 * @return true iff this RBTree is empty
	 */
	public boolean isEmpty() {
		return root == null;
	}
	
	/**
	 * @return true iff this RBTree is NOT empty
	 */
	public boolean isNotEmpty() {
		return !isEmpty();
	}

	/**
	 * @return the value stored in the root element
	 */
	public V firstEntry() {
		return root.value;
	}
	
	/**
	 * @param action - the action to perform over each item in the tree
	 * I was too lazy to write a linked list or a stack :)
	 * so instead i used a function that will get execute during the tree traversal.. 
	 * the traversal is done in-order, so the action gets on the keys in a sorted order
	 * Complexity: O(n)   
	 */
	public void foreach(Do<V> action) {
		root.foreach(action);
	}

	/**
	 * @return the number of nodes in this tree
	 * Complexity: O(n)
	 */
	public int getSize() {
		return root.getSize();
	}
}
