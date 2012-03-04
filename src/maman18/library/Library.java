package maman18.library;

import java.util.Comparator;

import maman18.data.RBTree;


/**
 * @author Shlomi.v
 *
 * This class holds the library information
 * all complexity analysis refers to n as the total number of subscribers, m the total number of books
 *
 */
public class Library {

	/**
	 * an inner class representing each library subscriber
	 */
	static class Subscriber {
		public Subscriber(String name, int id) {
			this.name = name;
			this.id = id;
		}

		final String name;
		final int id;
		String books[] = new String[10]; // a maximum of 10 books
		int count;
	}

	// default comparators
	final Comparator<Integer> intOrd = new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			return o1.compareTo(o2);
		}
	};
	
	final Comparator<String> strOrd = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return o1.compareToIgnoreCase(o2);
		}
	};

	// the internal data structure is made out of two main RBTrees
	// subs:            SubId->Subscriber
	// whoHoldsTheBook: bookId->Subscriber
	//
	// and an array of 10 RBTrees. each node holds all the current users that have borrowed that many books.
	// mostBooks:       [0..10]->SubId->Subscriber
	RBTree<Integer, Subscriber> subs = RBTree.empty(intOrd);
	RBTree<String, Subscriber> whoHoldsTheBook = RBTree.empty(strOrd);
	RBTree<Integer, Subscriber> mostBooks[] = new RBTree[10];

	/**
	 * The constructor, builds 10 empty RBTrees
	 * Complexity : O(1)
	 */
	public Library() {
		for (int i = 0; i < mostBooks.length; i++) {
			mostBooks[i] = RBTree.empty(intOrd);
		}
	}

	/**
	 * @param name
	 * @param id
	 * 
	 * This method will add a subscriber by that given name and the given ID
	 * Complexity : O(lgn)
	 */
	void addSubscriber(String name, int id) {
		if (subs.containsKey(id)) {
			System.out.println("Subscriber " + name + " "+id +" already exists");
			return;
		}
		subs.put(id, new Subscriber(name, id));
		System.out.println("Added subscriber " + name + " " + id);
	}

	/**
	 * @param id
	 * 
	 * remove the subscriber identified by ID
	 * Complexity : O(lgn+lgm)
	 */
	void removeSubscriber(int id) {
		Subscriber s = subs.get(id); // O(lgn)
		int count = s.count;

		for (int i = 0; i < count; i++) {
			subReturnBook(id, s.books[0]); // O(lgm)
		}

		subs.remove(id); // O(lgn)

	}

	/**
	 * @param subId
	 * @param bookId
	 * 
	 * a subscriber identified by subId is borrowing the book identified by bookId
	 * Complexity : O(lgn+lgm)
	 */
	void subBorrowBook(int subId, String bookId) {
		Subscriber s = subs.get(subId); // O(lgn)
		if (s != null) {
			if (s.count == s.books.length) {
				System.out.println("Subscriber " + s.name + " " + s.id + " book limit reached");
				return;
			}
			Subscriber book = whoHoldsTheBook.get(bookId); // O(lgm)
			if (book != null) {
				System.out.println("another subscriber ("+book.id+") already took this book");
				return;
			}
			
			whoHoldsTheBook.put(bookId, s); // O(lgm)
			s.books[s.count] = bookId;
			if (s.count > 0) mostBooks[s.count -1].remove(subId); // O(lgm)
			s.count ++;
			mostBooks[s.count -1].put(subId, s); // O(lgm)
			System.out.println(s.name + " borrowed the book " + bookId);
			return;
		}
		System.out.println("user " + subId + " does not exists");
		return;
	}
	
	/**
	 * @param subId
	 * @param bookId
	 * 
	 * a subscriber identified by subId is returning a book identified by bookId
	 * Complexity : O(lgn+lgm)
	 */
	void subReturnBook(int subId, String bookId) {
		Subscriber sub = whoHoldsTheBook.get(bookId); // O(lgn)
		
		// check if the user exists
		if (subs.get(subId) == null) {                // O(lgm)
			System.out.println("user " + subId+" does not exist");
			return;
		}
		
		// check if anyone holds this book
		if (sub == null) {
			System.out.println("no one holds this book");
			return;	
		}
		
		// check that this user is actually holding this book
		if (sub.id != subId) {
			System.out.println("another user holds this book! (" + sub.id + ")");
			return;
		}
		
		// he does, so mark that the book is not lent to anyone
		whoHoldsTheBook.remove(bookId);				  // O(lgm)
		
		// now fix the mostBooks array
		// and remove this book from the subscriber
		for (int i = 0; i < sub.books.length; i++) {
			String book = sub.books[i];               
			if (book != null && book.equals(bookId)) {
				
				// we found the book in the subscriber's book list
				// so delete it by coping the last book over this book and reducing book count by one
				sub.books[i] = sub.books[sub.count - 1];
				
				// mark last book count as no book
				sub.books[sub.count - 1] = null;
				
				// fix mostBooks
				mostBooks[sub.count-- -1].remove(subId); // O(lgm)
				if (sub.count > 0) 
					mostBooks[sub.count - 1].put(subId, sub); // O(lgm)
				
				System.out.println(sub.name + " returned the book " + bookId);
				return;
			}
		}
		
		return;
	
	}
	
	/**
	 * @return the RedBlack Tree containing ALL the subscribers that currently hold the most books
	 * Complexity: O(1)
	 */
	public RBTree<Integer, Subscriber> mostBorrowed() {
		// find the largest RB-Tree that is not empty, it will contain all the users that are holding
		// that many books
		for (int i = 9; i >= 0; i--) {
			if (mostBooks[i].isNotEmpty()) {
				return mostBooks[i];
			}
		}
		
		return null;
	}

	/**
	 * @param bookId
	 * @return the Subscriber that is currently holding the book identified by bookId
	 * Complexity : O(lgm)
	 */
	public Subscriber whoHoldsTheBook(String bookId) {
		return whoHoldsTheBook.get(bookId);
	}


	/**
	 * @param subId
	 * @return the Subscriber by the subId
	 * Complexity : O(lgn)
	 */
	public Subscriber getSubscriber(int subId) { 
		return subs.get(subId);
	}	
}
