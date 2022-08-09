package hw1;
import java.util.*;
//Chenjie Xiong
/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc {

	private Type[] types;
	private String[] fields;
	
    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr array specifying the number of and types of fields in
     *        this TupleDesc. It must contain at least one entry.
     * @param fieldAr array specifying the names of the fields. Note that names may be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
    	//your code here
    	this.types = typeAr;
    	this.fields = fieldAr;
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        //your code here
    	return types.length;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public String getFieldName(int i) throws IllegalArgumentException {
        //your code here
    	if (i < 0 || i >= numFields()) {
    		throw new IllegalArgumentException("Index your provided is out of range!");
    	}
    	return fields[i];
    }

    /**
     * Find the index of the field with a given name.
     *
     * @param name name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException if no field with a matching name is found.
     */
    public int nameToId(String name) throws IllegalArgumentException{
        //your code here
    	int index = -1;
    	for (int i = 0; i < fields.length; i++) {
    		if(fields[i].equals(name)) {
    			index = i;
    			break;
    		}
    	}
    	if (index == -1) {
    		throw new IllegalArgumentException("No field with a matching name is found!");
    	}
    	return index;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i The index of the field to get the type of. It must be a valid index.
     * @return the type of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public Type getType(int i) throws NoSuchElementException {
        //your code here
    	if(i < 0 || i >= numFields()) {
    		throw new NoSuchElementException("No field with a matching name is found!");
    	}
    	return this.types[i];
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     * Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
    	//your code here
    	int sizes = 0;
    	for (Type type :  types) {
    		if (type == Type.INT) {
    			sizes += 4;
    		} else {
    			sizes += 129;
    		}
    	}
    	return sizes;
    }

    /**
     * Compares the specified object with this TupleDesc for equality.
     * Two TupleDescs are considered equal if they are the same size and if the
     * n-th type in this TupleDesc is equal to the n-th type in td.
     *
     * @param o the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
    	//your code here
    	if (this == o) return true;
    	if (o == null || this.getClass() != o.getClass()) {
    		return false;
    	}
    	TupleDesc otherDesc = (TupleDesc) o;
    	
    	
    	return Arrays.equals(types, otherDesc.types);
    }
    

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * @return String describing this descriptor.
     */
    public String toString() {
        //your code here
    	StringBuilder sBuilder = new StringBuilder("");
    	String prefixString = "";
    	for (int i = 0; i < types.length; i++) {
    		sBuilder.append(prefixString);
    		prefixString = ",";
    		sBuilder.append(types[i]);
    		sBuilder.append("(");
    		sBuilder.append(fields[i]);
    		sBuilder.append(")");
    	
    		
    	}
    	return sBuilder.toString();
    }
    public static void main(String args[]) {
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
    	System.out.println(tupleDesc.toString());
    	
    
		
	}
}
