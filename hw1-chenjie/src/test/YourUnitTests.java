package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.IntField;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

public class YourUnitTests {
	
	private HeapFile hf;
	private TupleDesc td;
	private Catalog c;
	private HeapPage hp;

	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		td = c.getTupleDesc(tableId);
		hf = c.getDbFile(tableId);
		hp = hf.readPage(0);
	}
	
	@Test
	public void testGetSize() {
		Type[] types = new Type[4];
    	types[0] = Type.INT;
    	types[1] = Type.STRING;
    	types[2] = Type.INT;
    	types[3] = Type.STRING;
    	String[] fields = new String[4];
    	fields[0] = "0";
    	fields[1] = "1";
    	fields[2] = "the2";
    	fields[3] = "3";
    	TupleDesc tupleDesc = new TupleDesc(types, fields);
    	assertTrue("getsize can not work properly", tupleDesc.getSize() == 266);
	}
	@Test
	public void testHPAdd() throws Exception{
		
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		hf.addTuple(t);
	
		Iterator<Tuple> alltupleIterator = hf.getAllTuples().iterator();
		boolean sameTupleInHF = false;
		while (alltupleIterator.hasNext()) {
			Tuple curTuple = alltupleIterator.next();
			
			if (curTuple.getPid() == t.getPid() && curTuple.getId() == t.getId()) {
				sameTupleInHF = true;
				break;
			}
			
		}
		assertTrue("do not add successfully", sameTupleInHF);
		

	}
	

}
