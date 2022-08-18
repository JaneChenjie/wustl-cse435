# wustl-cse435
It is the coding homework of WUSTL's CSE435 couse, instructed by Doug Shrook. Some part of coding is also provided by him.

hw1:https://www.cse.wustl.edu/~dshook/cse530/hw/hw1.html
hw2:https://www.cse.wustl.edu/~dshook/cse530/hw/hw2.html 
hw3:https://www.cse.wustl.edu/~dshook/cse530/hw/hw3.html 
hw4:https://www.cse.wustl.edu/~dshook/cse530/hw/hw4.html 

## Example

The following example will load a table from a file, then display all of the tuples contained in that table. In other words, it is equivallent to SELECT * FROM test;. The .txt file contains the schema and the catalog looks for the data in a file called test.dat.
```
Catalog c = Database.getCatalog();
			c.loadSchema("testfiles/test.txt");
			
			int tableId = c.getTableId("test");
			td = c.getTupleDesc(tableId);
			System.out.println(td);
			
			hf = c.getDbFile(tableId);
			ArrayList tups = hf.getAllTuples();
			Iterator it = tups.iterator();
			
			while(it.hasNext()) {
				System.out.println(it.next());
			}

```


