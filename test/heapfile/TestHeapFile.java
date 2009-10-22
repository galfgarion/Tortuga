package test.heapfile;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import neustore.heapfile.*;
import neustore.base.DBBuffer;
import neustore.base.DBIndex;
import neustore.base.KeyData;
import neustore.base.LRUBuffer;
import neustore.base.IntKey;
import neustore.base.StringData;

/**
 * A class that tests the HeapFile.
 * We could define our own Key and Data. But here we use the pre-defined
 * {@link IntKey} and {@link StringData}.
 * 
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class TestHeapFile {
	final int maxStringLength = 10;

	HeapFile TestCreate( DBBuffer buffer, String filename ) throws IOException {
		File file = new File(filename);
		file.delete();
		IntKey sampleKey = new IntKey(0);
		StringData sampleData = new StringData(maxStringLength, ""); 
		HeapFile hp = new HeapFile(buffer, filename, DBIndex.CREATE, sampleKey, sampleData);
		return hp;
	}
	
	void TestInsert( HeapFile hp ) throws IOException{
		hp.insert( new IntKey(10), new StringData(maxStringLength, "John"));
		hp.insert( new IntKey(30), new StringData(maxStringLength, "Adams"));
		hp.insert( new IntKey(20), new StringData(maxStringLength, "Bill"));
		hp.insert( new IntKey(40), new StringData(maxStringLength, "George"));
		hp.insert( new IntKey(60), new StringData(maxStringLength, "Tom"));
		hp.insert( new IntKey(70), new StringData(maxStringLength, "Jack"));		
	}

	void TestSearch( HeapFile hp ) throws IOException {
		StringData sd = (StringData)hp.search( new IntKey(60) );
		if ( sd != null ) {
			System.out.println("Good, key 60 corresponds to " + sd.string );
		}
		else {
			System.out.println("Strange, key=60 was not found.");
		}
		sd = (StringData)hp.search( new IntKey(35) );
		if ( sd != null ) {
			System.out.println("Strange, key=35 was not inserted but found.");
		}
		else {
			System.out.println("Good, key=35 was not inserted and not found.");
		}
	}
	
	void TestScan( HeapFile hp ) throws IOException {
		Enumeration<KeyData> e = hp.StartEnumeration();
		while ( e.hasMoreElements() ) {
			KeyData keyData = e.nextElement();
			System.out.println("key=" + ((IntKey)keyData.key).key + ", data=" + ((StringData)keyData.data).string);
		}
	}
	
	public static void main(String[]args) throws IOException {
		System.out.println( "***** Creating databases *****" );		
		String filename1 = "TESTHEAPFILE1";
		LRUBuffer buffer = new LRUBuffer( 2, 50 );
		TestHeapFile test = new TestHeapFile();		
		HeapFile hp1 = test.TestCreate( buffer, filename1 );
		
		System.out.println( "***** Inserting keys *****");
		test.TestInsert(hp1);
		
		System.out.println( "***** Searching keys *****" );
		test.TestSearch(hp1);

		System.out.println( "***** Deleting key=60 from hp1 *****" );
		hp1.delete( new IntKey(60) );

		System.out.println( "***** Scanning *****" );
		test.TestScan(hp1);

		System.out.println( "***** Searching keys 60 *****" );
		StringData sd = (StringData)hp1.search( new IntKey(60) );
		if ( sd != null ) {
			System.out.println("Strange, key=60 was deleted but still found." );
		}
		else {
			System.out.println("Good, key=60 was deleted and thus not found.");
		}
		
		System.out.println( "***** Index information *****" );
		hp1.printInformation( );
		
		hp1.close();
	}
}