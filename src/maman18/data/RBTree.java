package maman18.data;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class RBTree<K, V> {

	enum Color {
		RED, BLACK
	};

	final protected Comparator<K> order;

	private RBTree(Comparator<K> order) {
		this.order = order;
	}

	static public <K, V> RBTree<K, V> empty(Comparator<K> order) {
		return new RBTree<K, V>(order);
	}

	class Node {
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

		private Node maximumNode() {
			if (right == null)
				return this;
			return right.maximumNode();
		}

		public Node insert(K key, V value) {
			int ord = order.compare(key, this.key);
			if (ord == 0) {
				this.value = value;
				return this;
			}

			if (ord < 0) {
				if (left != null)
					return left.insert(key, value);
				else {
					left = new Node(key, value, this, null, null, Color.RED);
					return left;
				}
			} else {
				if (right != null)
					return right.insert(key, value);
				else {
					right = new Node(key, value, this, null, null, Color.RED);
					return right;
				}
			}
		}

		public int maxDepth() {
			if (left == null && right == null) return 1;
			if (left == null && right != null) return 1+right.maxDepth();
			if (left != null && right == null) return 1+left.maxDepth();
			return 1+Math.max(left.maxDepth(), right.maxDepth());
		}
		
		public List<V> values(LinkedList<V> vals) {
			if (left != null) left.values(vals);
			vals.add(value);
			if (right != null) right.values(vals);
			return vals;
		}
	}

	public void put(K key, V value) {
		if (root == null) {
			root = new Node(key, value, null, null, null, Color.BLACK);
			return;
		}

		fixRedBlackInsert(root.insert(key, value));
	}

	Color getColor(Node node) {
		return node == null ? Color.BLACK : node.color;
	}

	Node root = null;

	public Node findNode(K key) {
		Node p = root;
		while (p != null) {
			int theOrder = order.compare(key, p.key);
			if (theOrder == 0)
				return p;
			p = theOrder < 0 ? p.left : p.right;
		}
		return p;
	}

	public V get(K key) {
		Node n = findNode(key);
		return n == null ? null : n.value;
	}

	private void replaceNode(Node oldn, Node newn) {
		if (oldn.parent == null) {
			root = newn;
		} else {
			if (oldn.isLeftChild())
				oldn.parent.left = newn;
			else
				oldn.parent.right = newn;
		}
		if (newn != null) {
			newn.parent = oldn.parent;
		}
	}

	private void rotateLeft(Node n) {
		Node r = n.right;
		replaceNode(n, r);
		n.right = r.left;
		if (r.left != null)
			r.left.parent = n;
		r.left = n;
		n.parent = r;
	}

	private void rotateRight(Node n) {
		Node l = n.left;
		replaceNode(n, l);
		n.left = l.right;
		if (l.right != null)
			l.right.parent = n;
		l.right = n;
		n.parent = l;
	}

	private void fixRedBlackInsert(final Node n) {
		if (n.parent == null)
			n.color = Color.BLACK; // this is the head of the tree, make black
		else {
			if (getColor(n.parent) == Color.BLACK)
				return;
			else {
				if (getColor(n.uncle()) == Color.RED) {
					n.parent.color = Color.BLACK;
					n.uncle().color = Color.BLACK;
					n.grandparent().color = Color.RED;
					fixRedBlackInsert(n.grandparent());
				} else {
					if (n.isRightChild() && n.parent.isLeftChild()) {
						rotateLeft(n.parent);
						finalInsertFix(n.left);
						return;
					} else if (n.isLeftChild() && n.parent.isRightChild()) {
						rotateRight(n.parent);
						finalInsertFix(n.right);
						return;
					}
					finalInsertFix(n);
				}
			}
		}
	}

	private void finalInsertFix(Node n) {
		n.parent.color = Color.BLACK;
		n.grandparent().color = Color.RED;
		if (n.isLeftChild() && n.parent.isLeftChild()) {
			rotateRight(n.grandparent());
		} else {
			rotateLeft(n.grandparent());
		}
	}

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

	private void fixRedBlackDelete(final Node n) {
		if (n.parent == null)
			return;
		else {
			if (getColor(n.brother()) == Color.RED) {
				n.parent.color = Color.RED;
				n.brother().color = Color.BLACK;
				if (n.isLeftChild())
					rotateLeft(n.parent);
				else
					rotateRight(n.parent);
			}

			if (getColor(n.parent) == Color.BLACK
					&& getColor(n.brother()) == Color.BLACK
					&& getColor(n.brother().left) == Color.BLACK
					&& getColor(n.brother().right) == Color.BLACK) {
				n.brother().color = Color.RED;
				fixRedBlackDelete(n.parent);
			} else if (getColor(n.parent) == Color.RED
					&& getColor(n.brother()) == Color.BLACK
					&& getColor(n.brother().left) == Color.BLACK
					&& getColor(n.brother().right) == Color.BLACK) {
				n.brother().color = Color.RED;
				n.parent.color = Color.BLACK;
			} else {
				if (n == n.parent.left
					&& getColor(n.brother()) == Color.BLACK
					&& getColor(n.brother().left) == Color.RED
					&& getColor(n.brother().right) == Color.BLACK) {
					n.brother().color = Color.RED;
					n.brother().left.color = Color.BLACK;
					rotateRight(n.brother());
				} else if (n == n.parent.right
						&& getColor(n.brother()) == Color.BLACK
						&& getColor(n.brother().right) == Color.RED
						&& getColor(n.brother().left) == Color.BLACK) {
					n.brother().color = Color.RED;
					n.brother().right.color = Color.BLACK;
					rotateLeft(n.brother());
				}
				n.brother().color = getColor(n.parent);
				n.parent.color = Color.BLACK;
	
				if (n == n.parent.left) {
					n.brother().right.color = Color.BLACK;
					rotateLeft(n.parent);
				} else {
					n.brother().left.color = Color.BLACK;
					rotateRight(n.parent);
				}
			}
		}

	}

	public int maxDepth() {
		if (root == null) return 0;
		return root.maxDepth();
	}
	
	public static void main(String[] args) {
		final Comparator<Integer> order = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		};

		RBTree<Integer, String> tree = RBTree.empty(order);
		tree.put(4, "ASS");
		tree.put(2, "BIATCHh");
		tree.put(6, "KRKRK");
		tree.put(5, "BIATCH");
		tree.put(14, "ASS");
		tree.put(22, "BIATCHh");
		tree.put(26, "KRKRK");
		tree.put(15, "BIATCH");
		tree.put(42, "ASS");
		tree.put(21, "BIATCHh");
		tree.put(64, "KRKRK");
		tree.put(51, "BIATCH");
		tree.put(124, "ASS");
		tree.put(122, "BIATCHh");
		tree.put(126, "KRKRK");
		tree.put(115, "BIATCH");
		System.out.println(tree.maxDepth());

		System.out.println(tree.get(2));
		
		tree.remove(4);
		tree.remove(2);
		System.out.println(tree.maxDepth());
		tree.remove(6);
		tree.remove(5);
		tree.remove(14);
		tree.remove(22);
		tree.remove(26);
		System.out.println(tree.maxDepth());
		tree.remove(15);
		tree.remove(42);
		tree.remove(21);
		tree.remove(64);
		tree.remove(51);
		tree.remove(124);
		tree.remove(122);
		System.out.println(tree.maxDepth());
		tree.remove(126);
		tree.remove(115);
		
		
		System.out.println(tree.maxDepth());


	}

	public boolean containsKey(K key) {
		return get(key) != null;
	}

	public boolean isEmpty() {
		return root == null;
	}
	
	public boolean isNotEmpty() {
		return !isEmpty();
	}

	public V firstEntry() {
		return root.value;
	}
	
	public List<V> values() {
		LinkedList<V> vals = new LinkedList<V>();
		return root.values(vals);
	}
}
