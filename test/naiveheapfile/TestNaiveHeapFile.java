package test.naiveheapfile;

import java.io.File;
import java.io.IOException;

import neustore.base.DBIndex;
import neustore.base.LRUBuffer;

/**
 * A class that tests the naive heap file.
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class TestNaiveHeapFile {
	public static void main(String[]args) throws IOException {
		System.out.println( "***** Deleting file if exists *****" );
		String filename = "TESTNAIVEHEAPFILE";
		File file = new File(filename);
		file.delete();

		System.out.println( "***** Creating database *****" );
		LRUBuffer buffer = null;
		buffer = new LRUBuffer( 5, 20 );
		NaiveHeapFile hp = new NaiveHeapFile(buffer, filename, DBIndex.CREATE);
		
		System.out.println( "***** Inserting keys *****");
		for ( int i=1; i<=10; i++ ) {
			hp.insert( i*5 );
		}
		
		System.out.println( "***** Searching keys *****" );
		if ( hp.search( 10 ) ) {
			System.out.println("Good, an inserted key=10 is found.");
		}
		else {
			System.out.println("Strange, key=10 was not found.");
		}
		if ( hp.search(101) ) {
			System.out.println("Strange, key=101 was not inserted but found.");
		}
		else {
			System.out.println("Good, key=101 was not inserted and not found.");
		}
		
		System.out.println( "***** Reopening file *****" );
		hp.close();
		hp = new NaiveHeapFile( buffer, filename, DBIndex.OPEN );

		System.out.println( "***** Searching keys *****" );
		if ( hp.search( 10 ) ) {
			System.out.println("Good, an inserted key=10 is found.");
		}
		else {
			System.out.println("Strange, key=10 was not found.");
		}
		if ( hp.search(101) ) {
			System.out.println("Strange, key=101 was not inserted but found.");
		}
		else {
			System.out.println("Good, key=101 was not inserted and not found.");
		}
		
		hp.close();
	}
}