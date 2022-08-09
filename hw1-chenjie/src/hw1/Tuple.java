package hw1;

import java.sql.Types;
import java.util.HashMap;

/**
 * This class represents a tuple that will contain a single row's worth of information
 * from a table. It also includes information about where it is stored
 * @author Sam Madden modified by Doug Shook
 *
 */
public class Tuple {
	
	/**
	 * Creates a new tuple with the given description
	 * @param t the schema for this tuple
	 */
	private TupleDesc tupleDesc;
	private int pageId;
	private int slotId;
	private Field[] fields;
	
	public Tuple(TupleDesc t) {
		//your code here
		tupleDesc = t;
		fields = new Field[tupleDesc.numFields()];
	}
	
	public TupleDesc getDesc() {
		//your code here
		return tupleDesc;
	}
	
	/**
	 * retrieves the page id where this tuple is stored
	 * @return the page id of this tuple
	 */
	public int getPid() {
		//your code here
		return pageId;
	}

	public void setPid(int pid) {
		//your code here
		pageId = pid;
	}

	/**
	 * retrieves the tuple (slot) id of this tuple
	 * @return the slot where this tuple is stored
	 */
	public int getId() {
		//your code here
		return slotId;
	}

	public void setId(int id) {
		//your code here
		slotId = id;
	}
	
	public void setDesc(TupleDesc td) {
		//your code here;
		tupleDesc = td;
	}
	
	/**
	 * Stores the given data at the i-th field
	 * @param i the field number to store the data
	 * @param v the data
	 */
	public void setField(int i, Field v) {
		//your code here
		fields[i] = v;
	}
	
	public Field getField(int i) {
		//your code here
		return fields[i];
	}
	
	/**
	 * Creates a string representation of this tuple that displays its contents.
	 * You should convert the binary data into a readable format (i.e. display the ints in base-10 and convert
	 * the String columns to readable text).
	 */
	public String toString() {
		//your code here
		StringBuilder sBuilder = new StringBuilder("");
		String prefixString = "";
		for (int i = 0; i < fields.length; i++) {
			sBuilder.append(prefixString);
			prefixString = ",";
			if (tupleDesc.getType(i) == Type.INT) {
				IntField realField = (IntField) fields[i];
				sBuilder.append(realField.getValue());
			} else {
				StringField realField = (StringField) fields[i];
				sBuilder.append(realField.getValue());
			}
		}
		return sBuilder.toString();
	}
}
	