package maman18;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import maman18.library.CommandParser;
import maman18.library.Library;

/**
 * @author Shlomi.v
 *
 * this is the entry point to our application.
 *
 */
public class LibraryApp {
	public static void main(String[] args) {
		// initialize the parser and a new library
		CommandParser parser = new CommandParser(new Library());
		
		// prepare a command reader
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Welcome to the library, please enter a command\nEnter quit to exit the application");
		
		// Endlessly run..
		try {
			while (true) {
				System.out.print("> ");
				parser.doCommand(br.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
