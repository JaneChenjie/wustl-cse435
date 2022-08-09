package hw2;
//Chenjie Xiong
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;
import net.sf.jsqlparser.expression.CaseExpression;

/**
 * A class to perform various aggregations, by accepting one tuple at a time
 * 
 * @author Doug Shook
 *
 */
public class Aggregator {
	private AggregateOperator op;
	private boolean groupby;
	private TupleDesc td;
	private ArrayList<Tuple> tuples;// if we do not have group by, store tuple here
	private HashMap<Field, ArrayList<Tuple>> groups;// if we have group by, store tuple here

	public Aggregator(AggregateOperator o, boolean groupBy, TupleDesc td) {
		// your code here
		this.op = o;
		this.groupby = groupBy;
		this.td = td;
		tuples = new ArrayList<>();

		groups = new HashMap<>();

	}

	/**
	 * Merges the given tuple into the current aggregation
	 * 
	 * @param t the tuple to be aggregated
	 */
	public void merge(Tuple t) {
		// your code here
		switch (this.op) {
		case MAX:
			if (this.groupby) {
				addTupleToHashMap(t);
			} else {
				mergeMax(t);
			}
			break;

		case MIN:
			if (this.groupby) {
				addTupleToHashMap(t);
			} else {
				mergeMin(t);
			}
			break;

		case AVG:
			if (this.groupby) {
				addTupleToHashMap(t);
			} else {
				tuples.add(t);
			}
			break;

		case COUNT:
			if (this.groupby) {
				addTupleToHashMap(t);
			} else {
				tuples.add(t);
			}
			break;

		case SUM:
			if (this.groupby) {
				addTupleToHashMap(t);
			} else {
				tuples.add(t);
			}
			break;

		default:
			throw new IllegalArgumentException("Unexpected value: " + this.op);
		}
	}

	private void addTupleToHashMap(Tuple t) {
		// used by group by in order to group
		// the first column will be the column containing the groups,
		// and the second column will contain the data to be aggregated.

		Field key = t.getField(0);
		if (groups.containsKey(key)) {
			groups.get(key).add(t);
		} else {
			groups.put(key, new ArrayList<>());
			groups.get(key).add(t);

		}

	}

	private void mergeMax(Tuple t) {
		// no group by
		if (tuples.isEmpty()) {
			tuples.add(t);
		} else {
			Tuple oldMaxTuple = tuples.get(0);

			if (t.getField(0).compare(RelationalOperator.GT, oldMaxTuple.getField(0))) {
				tuples.set(0, t);

			}
		}
	}

	private void mergeMin(Tuple t) {
		// no group by
		if (tuples.isEmpty()) {
			tuples.add(t);
		} else {
			Tuple oldTupleMinTuple = tuples.get(0);
			if (t.getField(0).compare(RelationalOperator.LT, oldTupleMinTuple.getField(0))) {
				tuples.set(0, t);

			}
		}

	}

	/**
	 * Returns the result of the aggregation
	 * 
	 * @return a list containing the tuples after aggregation
	 */
	public ArrayList<Tuple> getResults() {
		// your code here
		switch (this.op) {
		case MAX:
			if (this.groupby) {
				return maxWithGroupBy();
			} else {

			}
			break;

		case MIN:
			if (this.groupby) {
				return minWithGroupBy();

			} else {

			}
			break;

		case AVG:
			if (this.groupby) {
				return avgWithGroupBy();

			} else {
				return avgWithoutGroupBy();

			}

		case COUNT:
			if (this.groupby) {
				return countWithGroupBy();

			} else {
				return countWithoutGroupBy();

			}

		case SUM:
			if (this.groupby) {
				return sumWithGroupBy();

			} else {
				return sumWithoutGroupBy();

			}
			

		default:
			throw new IllegalArgumentException("Unexpected value: " + this.op);
		}

		return this.tuples;
	}

	private ArrayList<Tuple> minWithGroupBy() {
		ArrayList<Tuple> resultArrayList = new ArrayList<>();
		if (!groups.isEmpty()) {
			for (Map.Entry<Field, ArrayList<Tuple>> eachGroupEntry : groups.entrySet()) {
				ArrayList<Tuple> eachGroupList = eachGroupEntry.getValue();

				Tuple currentMinTuple = eachGroupList.get(0);
				for (Tuple tupleInThisGroupTuple : eachGroupList) {
					// find the max field in the group
					if (tupleInThisGroupTuple.getField(1).compare(RelationalOperator.LT, currentMinTuple.getField(1))) {
						currentMinTuple = tupleInThisGroupTuple;
					}
				}
				resultArrayList.add(currentMinTuple);
			}

		}
		return resultArrayList;
	}

	private ArrayList<Tuple> avgWithoutGroupBy() {
		ArrayList<Tuple> resultArrayList = new ArrayList<>();
		if (td.getType(0) == Type.STRING)
			return null;// we can not avg string
		int totalNum = tuples.size();
		int sum = 0;
		for (Tuple tuple : tuples) {
			sum += tuple.getField(0).hashCode();
		}
		int avg = sum / totalNum;
		// create the result tuple
		Type[] tupesTypes = new Type[] { Type.INT };
		String[] nameStrings = new String[] { "AVG" };
		TupleDesc newTupleDesc = new TupleDesc(tupesTypes, nameStrings);
		Tuple newTuple = new Tuple(newTupleDesc);
		newTuple.setField(0, new IntField(avg));
		resultArrayList.add(newTuple);

		return resultArrayList;
	}

	private ArrayList<Tuple> avgWithGroupBy() {
		if (td.getType(1) == Type.STRING)
			return null; // can not compute string's avg
		ArrayList<Tuple> resutlArrayList = new ArrayList<>();
		// create new describe
		Type[] tupesTypes = new Type[] { Type.INT, Type.INT };
		String[] nameStrings = new String[] { td.getFieldName(0), "AVG" };
		TupleDesc newTupleDesc = new TupleDesc(tupesTypes, nameStrings);

		for (Map.Entry<Field, ArrayList<Tuple>> eachEntry : groups.entrySet()) {
			ArrayList<Tuple> eachGrouList = eachEntry.getValue();

			Field keyField = eachEntry.getKey();

			int numTupleInGroup = eachGrouList.size();
			int sum = 0;
			for (Tuple tupleInThisGroup : eachGrouList) {
				sum += tupleInThisGroup.getField(1).hashCode();
			}
			// create new tuple for this group
			int avgForThisGroup = sum / numTupleInGroup;
			Tuple newTuple = new Tuple(newTupleDesc);
			newTuple.setField(0, keyField);
			newTuple.setField(1, new IntField(avgForThisGroup));
			resutlArrayList.add(newTuple);

		}
		return resutlArrayList;
	}

	private ArrayList<Tuple> maxWithGroupBy() {
		ArrayList<Tuple> resultArrayList = new ArrayList<>();

		for (Map.Entry<Field, ArrayList<Tuple>> eachGroupEntry : groups.entrySet()) {
			ArrayList<Tuple> eachGroupList = eachGroupEntry.getValue();

			Tuple currentMaxTuple = eachGroupList.get(0);
			for (Tuple tupleInThisGroupTuple : eachGroupList) {
				// find the max field in the group
				if (tupleInThisGroupTuple.getField(1).compare(RelationalOperator.GT, currentMaxTuple.getField(1))) {
					currentMaxTuple = tupleInThisGroupTuple;
				}
			}
			resultArrayList.add(currentMaxTuple);
		}

		return resultArrayList;
	}

	private ArrayList<Tuple> countWithGroupBy() {
		// create new describe
		Type[] tupesTypes = new Type[] { this.td.getType(0), Type.INT };
		String[] nameStrings = new String[] { td.getFieldName(0), "COUNT" };
		TupleDesc newTupleDesc = new TupleDesc(tupesTypes, nameStrings);

		ArrayList<Tuple> resultArrayList = new ArrayList<>();

		for (Map.Entry<Field, ArrayList<Tuple>> eachGroupEntry : groups.entrySet()) {
			ArrayList<Tuple> eachGroupList = eachGroupEntry.getValue();
			Field keyField = eachGroupEntry.getKey();
			int count = eachGroupList.size();
			Tuple newTuple = new Tuple(newTupleDesc);
			newTuple.setField(0, keyField);
			newTuple.setField(1, new IntField(count));
			resultArrayList.add(newTuple);

		}
		return resultArrayList;

	}

	private ArrayList<Tuple> countWithoutGroupBy() {
		// create new describe
		Type[] tupesTypes = new Type[] { Type.INT };
		String[] nameStrings = new String[] { "COUNT" };
		TupleDesc newTupleDesc = new TupleDesc(tupesTypes, nameStrings);

		ArrayList<Tuple> resultArrayList = new ArrayList<>();
		Tuple newTuple = new Tuple(newTupleDesc);
		newTuple.setField(0, new IntField(tuples.size()));
		resultArrayList.add(newTuple);
		return resultArrayList;
	}

	private ArrayList<Tuple> sumWithoutGroupBy() {
		ArrayList<Tuple> resultArrayList = new ArrayList<>();
		Type[] tupesTypes = new Type[] { Type.INT };
		String[] nameStrings = new String[] { "SUM" };
		TupleDesc newTupleDesc = new TupleDesc(tupesTypes, nameStrings);
		if (td.getType(0) == Type.STRING)
			return null;// we can not sum string
		int sum = 0;
		for (Tuple tuple : tuples) {
			sum += tuple.getField(0).hashCode();
		}
		Tuple newTuple = new Tuple(newTupleDesc);
		newTuple.setField(0, new IntField(sum));
		resultArrayList.add(newTuple);
		return resultArrayList;
	}

	private ArrayList<Tuple> sumWithGroupBy() {
		ArrayList<Tuple> resultArrayList = new ArrayList<>();
		// create new describe
		Type[] tupesTypes = new Type[] { this.td.getType(0), Type.INT };
		String[] nameStrings = new String[] { td.getFieldName(0), "SUM" };
		TupleDesc newTupleDesc = new TupleDesc(tupesTypes, nameStrings);
		if (td.getType(1) == Type.STRING)
			return null;// we can not sum string
		for (Map.Entry<Field, ArrayList<Tuple>> eachGroupEntry : groups.entrySet()) {
			ArrayList<Tuple> eachGroupList = eachGroupEntry.getValue();
			Field keyField = eachGroupEntry.getKey();
			int sumGroup = 0;
			for (Tuple tupleInThisGroupTuple : eachGroupList) {
				sumGroup += tupleInThisGroupTuple.getField(1).hashCode();
			}
			Tuple newTuple = new Tuple(newTupleDesc);
			newTuple.setField(0, keyField);
			newTuple.setField(1, new IntField(sumGroup));
			resultArrayList.add(newTuple);
			
		}

		return resultArrayList;
	}

}
