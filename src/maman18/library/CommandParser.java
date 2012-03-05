package maman18.library;

import maman18.data.Do;
import maman18.data.RBTree;
import maman18.library.Library.Subscriber;

/**
 * @author Shlomi
 * 
 * This class is responsible for parsing the user input, and manipulating the Library accordingly
 *
 */
public class CommandParser {
	private Library lib;

	/**
	 * @param lib
	 * 
	 * The constructor must get a library to work with.
	 */
	public CommandParser(Library lib) {
		this.lib = lib;
	}
	
	/**
	 * @param cmd
	 * 
	 * This method does the parsing, it calls the proper method on the given library object 
	 */
	public void doCommand(String cmd) {
		cmd = cmd.trim();
		
		if (cmd.equals("quit")) {
			System.out.println("Byebye!");
			System.exit(0);
		}
		
		// make sure we actually got a command
		if (cmd.length() > 0) {
			// split the command string into an array of strings, using whitespace as delimiters
			String[] cmds = cmd.split("\\s+");
			
			// check the very first character
			switch (cmd.charAt(0)) {
			case '+': { // if its a '+', this must be an add subscriber command
					// the name must appear as the second parameter
					String name = cmds[1]; 
	
					// and the id must appear as the third parameter
					int id = Integer.valueOf(cmds[2]);
					
					// and make the call
					lib.addSubscriber(name, id);
				}
				break;
			case '-': { // a '-' must mean remove subscriber
					int id = Integer.valueOf(cmds[2]);
					lib.removeSubscriber(id);
					System.out.println("removed user " + id);
				}
				break;
			case '?': { // a '?' means a beginning of a query, lets check which one:
					// the second parameter must identify the query
					String query = cmds[1];
					
					// and a letter must mean we got a query regarding a book - who olds the book
					if (Character.isLetter(query.charAt(0))) {
						
						// find out who holds this book, O(lgm)
						Subscriber who = lib.whoHoldsTheBook(query);
						
						// and print out a proper response
						System.out.println(who != null ? who.name + " has the book "+ query : "No subscriber is holding that book");
					} 
					// if it begins with a digit, then we got a query regarding a subscriber - what books that he hold?
					else if (Character.isDigit(query.charAt(0))) {

						// get the id of the subscriber 
						int id = Integer.valueOf(query);
						
						// get the current subscriber, O(lgn)
						Subscriber s = lib.getSubscriber(id);
						if (s != null) {
							System.out.println("User " + s.name + " " + s.id+ " has these books:");
							
							// we found him, print out all of his books, O(1)
							for (int i = 0; i < s.count; i++)
								System.out.println("\t" + s.books[i]);
						} else {
							System.out.println("No such subscriber exists " + id);
						}
	
					} 
					// it begins with a '!' - meaning we want to get a list of all the users that has the most books
					else if (query.trim().charAt(0) == '!') {
						// get the RBTree containing all these users, O(1)
						RBTree<Integer, Subscriber> most = lib.mostBorrowed();
						
						// if any are found
						if (most != null) {
							System.out.println("the following subscribers has the most ("+ most.firstEntry().count + ") books:");
							
							// traverse the result in-order, and print each Subscriber (O(n))
							most.foreach(new Do<Subscriber>() {
								@Override
								public void action(Subscriber s) {
									System.out.println("\t" + s.name);
								}
							});
						} else {
							System.out.println("no one has any books!");
						}
					}
				}
				break;
			default: // in any other case, lets check the last entered token 
				switch (cmd.charAt(cmd.length() - 1)) {
				case '+': { // a '+' is borrow book command
						String name = cmds[0];
						int id = Integer.parseInt(cmds[1]);
						String bookld = cmds[2];
						lib.subBorrowBook(id, bookld);
					}
					break;
				case '-': { // a '-' is return a borrowed book command
						String name = cmds[0];
						int id = Integer.parseInt(cmds[1]);
						String bookld = cmds[2];
						lib.subReturnBook(id, bookld);
					}
					break;
				default:
					System.out.println("command not recognized!");
				}
			}
		}
	}
}
