package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.IntField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw2.Query;
import hw2.Relation;
// Chenjie Xiong
public class YourHW2Tests {

	private HeapFile testhf;
	private TupleDesc testtd;
	private HeapFile ahf;
	private TupleDesc atd;
	private Catalog c;

	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File("testfiles/A.dat.bak").toPath(), new File("testfiles/A.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		testtd = c.getTupleDesc(tableId);
		testhf = c.getDbFile(tableId);
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/A.txt");
		
		tableId = c.getTableId("A");
		atd = c.getTupleDesc(tableId);
		ahf = c.getDbFile(tableId);
		System.out.println(new Relation(ahf.getAllTuples(), atd));
		System.out.println(new Relation(testhf.getAllTuples(), atd));
	}
	
	@Test
	public void testContentWhereQ() {
		Query q = new Query("SELECT a1, a2 FROM A WHERE a1 = 530");
		Relation r = q.execute();
		
		
		ArrayList<Tuple> tuples = r.getTuples();
		for (Tuple tuple : tuples) {
			assertTrue("the column on a1 is 530", tuple.getField(0).hashCode() == 530);
		
		}
	}
	@Test
	public void testGroupByAndAggregateQ() {
		//test the order of group by and projection
		Query q = new Query("SELECT SUM(a2) FROM A GROUP BY a1");
		Relation r = q.execute();
		System.out.println("text");
		System.out.println(r);
		
		assertTrue("Tuple size should be 4  after aggregating", r.getDesc().getSize() == 4);
		assertTrue("Should be 4 groups from this query",r.getTuples().size() == 4);
		
		ArrayList<Integer> sums = new ArrayList<Integer>();
		
		for(Tuple t : r.getTuples()) {
		
			sums.add(((IntField)t.getField(0)).getValue());
		}
		
		
		
		assertTrue("Missing sum", sums.contains(2));
		assertTrue("Missing sum", sums.contains(20));
		assertTrue("Missing sum", sums.contains(6));
		assertTrue("Missing sum", sums.contains(8));
	}

}
