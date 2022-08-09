package hw2;
//Chenjie Xiong
import java.util.ArrayList;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * This class provides methods to perform relational algebra operations. It will be used
 * to implement SQL queries.
 * @author Doug Shook
 *
 */
public class Relation {

	private ArrayList<Tuple> tuples;
	private TupleDesc td;
	
	public Relation(ArrayList<Tuple> l, TupleDesc td) {
		//your code here
		tuples = l;
		this.td = td;
	}
	
	/**
	 * This method performs a select operation on a relation
	 * @param field number (refer to TupleDesc) of the field to be compared, left side of comparison
	 * @param op the comparison operator
	 * @param operand a constant to be compared against the given column
	 * @return
	 */
	public Relation select(int field, RelationalOperator op, Field operand) {
		//your code here
		ArrayList<Tuple> resultTuples = new ArrayList<>();
		for (Tuple tuple : tuples) {
			if (tuple.getField(field).compare(op, operand)) {
				resultTuples.add(tuple);
			}
		}
		this.tuples = resultTuples;
		 //should we create new relation or just this.tuples = resultTuples
		return this;
	}
	
	/**
	 * This method performs a rename operation on a relation
	 * @param fields the field numbers (refer to TupleDesc) of the fields to be renamed
	 * @param names a list of new names. The order of these names is the same as the order of field numbers in the field list
	 * @return
	 */
	public Relation rename(ArrayList<Integer> fields, ArrayList<String> names) throws Exception {
		//your code here
		
		int fieldNum = this.td.numFields();
		String[] newFieldNameStrings = new String[fieldNum];
		Type[] tdTypes = new Type[fieldNum];
		// copy the string and type
		for (int i = 0; i < fieldNum; i++) {
			newFieldNameStrings[i] = td.getFieldName(i);
			tdTypes[i] = td.getType(i);
		}
		for (String s : newFieldNameStrings) {
			if (names.contains(s)) {
				//System.out.println(true);
				throw new Exception();
			}
			
		}
		
		// change to the new name 
		
		for (int i = 0; i < fields.size(); i++) {
			int FieldIdToChange = fields.get(i);
			if (names.get(i) == null || names.get(i).length() == 0) continue;
			
			newFieldNameStrings[FieldIdToChange] = names.get(i);
		}
		TupleDesc newTupleDesc = new TupleDesc(tdTypes, newFieldNameStrings);
		this.td = newTupleDesc;
		
		for (Tuple tuple : tuples) {
			tuple.setDesc(newTupleDesc);
			
		}
		
		
		
		return this;
	}
	
	/**
	 * This method performs a project operation on a relation
	 * @param fields a list of field numbers (refer to TupleDesc) that should be in the result
	 * @return
	 */
	public Relation project(ArrayList<Integer> fields) {
		//your code here
		
		// create new tuple describe
		int newFieldNum = fields.size();
		String[] newFieldNameStrings = new String[newFieldNum];
		Type[] tdNewTypes = new Type[newFieldNum];
		for (int i = 0; i < newFieldNum; i++) {
			int fieldNumToKeep = fields.get(i);
			newFieldNameStrings[i] = td.getFieldName(fieldNumToKeep);
			tdNewTypes[i] = td.getType(fieldNumToKeep);
		}
		TupleDesc newTupleDesc = new TupleDesc(tdNewTypes, newFieldNameStrings);
		
		// create new tuple list
		if (fields.size() == 0) return new Relation(new ArrayList<>(), newTupleDesc);
		
		
		// handle the edge case
		
		for (Tuple oldTuple : tuples) {
			oldTuple.changeFieldToProject(fields);
			oldTuple.setDesc(newTupleDesc);
		}
		this.td = newTupleDesc;
		
		
		
		
		return this;
	}
	
	/**
	 * This method performs a join between this relation and a second relation.
	 * The resulting relation will contain all of the columns from both of the given relations,
	 * joined using the equality operator (=)
	 * @param other the relation to be joined
	 * @param field1 the field number (refer to TupleDesc) from this relation to be used in the join condition
	 * @param field2 the field number (refer to TupleDesc) from other to be used in the join condition
	 * @return
	 */
	public Relation join(Relation other, int field1, int field2) {
		//your code here
		// create new tuple describe
		int numFieldAfterJoin = this.td.numFields() + other.getDesc().numFields();
		String[] newFieldNameStrings = new String[numFieldAfterJoin];
		Type[] tdNewTypes = new Type[numFieldAfterJoin];
		
		for (int i = 0; i < this.td.numFields(); i++) {
			newFieldNameStrings[i] = this.td.getFieldName(i);
			tdNewTypes[i] = this.td.getType(i);
		}
		for (int i = td.numFields(); i < numFieldAfterJoin; i++) {
			int j = i - td.numFields();
			newFieldNameStrings[i] = other.getDesc().getFieldName(j);
			tdNewTypes[i] = other.getDesc().getType(j);
		}
		TupleDesc newTupleDesc = new TupleDesc(tdNewTypes, newFieldNameStrings);
		
		// create new tuple
		ArrayList<Tuple> resultTuples = new ArrayList<>();
		for (Tuple thisTuple : tuples) {
			for (Tuple otherTuple : other.getTuples()) {
				if (thisTuple.getField(field1).compare(RelationalOperator.EQ, otherTuple.getField(field2))) {
					Tuple newTuple = new Tuple(newTupleDesc);
					for (int i = 0; i < thisTuple.getDesc().numFields(); i++) {
						newTuple.setField(i, thisTuple.getField(i));
					}
					for (int i = 0; i < otherTuple.getDesc().numFields(); i++) {
						newTuple.setField(i + thisTuple.getDesc().numFields(), otherTuple.getField(i));
					}
					resultTuples.add(newTuple);
				}
			}
		}
		this.td = newTupleDesc;
		this.tuples = resultTuples;
		
		
		return this;
	}
	
	/**
	 * Performs an aggregation operation on a relation. See the lab write up for details.
	 * @param op the aggregation operation to be performed
	 * @param groupBy whether or not a grouping should be performed
	 * @return
	 */
	public Relation aggregate(AggregateOperator op, boolean groupBy) {
		//your code here
		Aggregator aggregator = new Aggregator(op, groupBy, td);
		for (Tuple tuple : tuples) {
			aggregator.merge(tuple);
			
		}
		ArrayList<Tuple> resultArrayList = aggregator.getResults();
		return new Relation(resultArrayList, resultArrayList.get(0).getDesc());
	}
	
	public TupleDesc getDesc() {
		//your code here
		return this.td;
	}
	
	public ArrayList<Tuple> getTuples() {
		//your code here
		return this.tuples;
	}
	
	/**
	 * Returns a string representation of this relation. The string representation should
	 * first contain the TupleDesc, followed by each of the tuples in this relation
	 */
	public String toString() {
		//your code here
		StringBuilder builder = new StringBuilder("");
		builder.append(this.td.toString());
		builder.append("\n");
		for (Tuple tuple : tuples) {
			builder.append(tuple.toString());
			builder.append("\n");
		}
		return builder.toString();
	}
	public static void main(String args[]) {
    	Type[] types = new Type[2];
    	types[0] = Type.INT;
    	
    	types[1] = Type.INT;
    	
    	
    	String[] fields = new String[2];
    	fields[0] = "first";
    	fields[1] = "second";
    	
    	
    	
    	TupleDesc tupleDesc = new TupleDesc(types, fields);
    	System.out.println(tupleDesc.toString());
    	
    	Tuple tuple1 = new Tuple(tupleDesc);
    	Tuple tuple = new Tuple(tupleDesc);
    	Tuple tuple2 = new Tuple(tupleDesc);
    	Tuple tuple3 = new Tuple(tupleDesc);
    	ArrayList<Tuple> tuples = new ArrayList<>();
    	
    	tuples.add(tuple);
    	tuples.add(tuple1);
    	tuples.add(tuple2);
    	tuples.add(tuple3);
    	
    	tuple.setField(0, new IntField(0));
    	tuple.setField(1, new IntField(2));
    
    	
    	tuple1.setField(0, new IntField(1));
    	tuple1.setField(1, new IntField(2));
    	
    	tuple2.setField(0, new IntField(1));
    	tuple2.setField(1, new IntField(8));
    	
    	tuple3.setField(0, new IntField(1));
    	tuple3.setField(1, new IntField(4));
    	
    	
    	Relation relation = new Relation(tuples, tupleDesc);
    	
    	System.out.println(tuple);
    	System.out.println(tuple1);
    	System.out.println(tuple2);
    	System.out.println(tuple3);
//    	ArrayList<Integer> fieldsArrayList = new ArrayList<>();
//   	   fieldsArrayList.add(0);
//    	fieldsArrayList.add(2);
//    	ArrayList<String> nameArrayList = new ArrayList<>();
//    	nameArrayList.add("firstChange");
//    	nameArrayList.add("secondChange");
    	System.out.println("call method");
    	System.out.println(relation.aggregate(AggregateOperator.SUM, true));
    	
    	
    	
    
		
	}
}
