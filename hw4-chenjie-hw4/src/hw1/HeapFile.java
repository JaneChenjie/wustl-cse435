package hw1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A heap file stores a collection of tuples. It is also responsible for
 * managing pages. It needs to be able to manage page creation as well as
 * correctly manipulating pages when tuples are added or deleted.
 * 
 * @author Sam Madden modified by Doug Shook
 *
 */
public class HeapFile {

	public static final int PAGE_SIZE = 4096;
	private File file;
	private TupleDesc tDesc;
	private int id;

	/**
	 * Creates a new heap file in the given location that can accept tuples of the
	 * given type
	 * 
	 * @param f     location of the heap file
	 * @param types type of tuples contained in the file
	 */
	public HeapFile(File f, TupleDesc type) {
		// your code here
		this.file = f;
		this.tDesc = type;
		id = file.hashCode();
	}

	public File getFile() {
		// your code here
		return this.file;
	}

	public TupleDesc getTupleDesc() {
		// your code here
		return this.tDesc;
	}

	/**
	 * Creates a HeapPage object representing the page at the given page number.
	 * Because it will be necessary to arbitrarily move around the file, a
	 * RandomAccessFile object should be used here.
	 * 
	 * @param id the page number to be retrieved
	 * @return a HeapPage at the given page number
	 */
	public HeapPage readPage(int id) {
		// your code here
		long startPoint = HeapFile.PAGE_SIZE * id;
		byte[] date = new byte[HeapFile.PAGE_SIZE];
		HeapPage newPage = null;
		int tableId = this.id;
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(startPoint);
			raf.read(date);
			raf.close();
			newPage = new HeapPage(id, date, tableId);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return newPage;
	}

	/**
	 * Returns a unique id number for this heap file. Consider using the hash of the
	 * File itself.
	 * 
	 * @return
	 */
	public int getId() {
		// your code here
		return this.id;
	}

	/**
	 * Writes the given HeapPage to disk. Because of the need to seek through the
	 * file, a RandomAccessFile object should be used in this method.
	 * 
	 * @param p the page to write to disk
	 */
	public void writePage(HeapPage p) {
		// your code here
		long start = PAGE_SIZE * p.getId();
		byte[] dateToWrite = p.getPageData();

		try {
			RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
			raf.seek(start);
			raf.write(dateToWrite);
			raf.close();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/**
	 * Adds a tuple. This method must first find a page with an open slot, creating
	 * a new page if all others are full. It then passes the tuple to this page to
	 * be stored. It then writes the page to disk (see writePage)
	 * 
	 * @param t The tuple to be stored
	 * @return The HeapPage that contains the tuple
	 */
	public HeapPage addTuple(Tuple t) {
		// your code here
		HeapPage page = null;
		int i = 0;
		for (; i < getNumPages(); i++) {
			HeapPage currentPage = readPage(i);
			for (int j = 0; j < currentPage.getNumSlots(); j++) {
				if (!currentPage.slotOccupied(j)) {
					try {
						currentPage.addTuple(t);
						page = currentPage;
						this.writePage(page);
						return page;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if (i == getNumPages()) {
			// create new heap page
			try {
				page = new HeapPage(getNumPages(), new byte[PAGE_SIZE], this.id);
				page.addTuple(t);
				this.writePage(page);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		return page;
	}

	/**
	 * This method will examine the tuple to find out where it is stored, then
	 * delete it from the proper HeapPage. It then writes the modified page to disk.
	 * 
	 * @param t the Tuple to be deleted
	 */
	public void deleteTuple(Tuple t) {
		// your code here
		HeapPage pageToModify = this.readPage(t.getPid());
		pageToModify.deleteTuple(t);
		this.writePage(pageToModify);
	}

	/**
	 * Returns an ArrayList containing all of the tuples in this HeapFile. It must
	 * access each HeapPage to do this (see iterator() in HeapPage)
	 * 
	 * @return
	 */
	public ArrayList<Tuple> getAllTuples() {
		// your code here
		ArrayList<Tuple> allTuples = new ArrayList<>();
		for (int i = 0; i < getNumPages(); i++) {
			Iterator<Tuple> it = this.readPage(i).iterator();
			while (it.hasNext()) {

				allTuples.add(it.next());

			}
		}
		return allTuples;
	}

	/**
	 * Computes and returns the total number of pages contained in this HeapFile
	 * 
	 * @return the number of pages
	 */
	public int getNumPages() {
		// your code here

		return (int) Math.ceil((file.length() / PAGE_SIZE));
	}

	public static void main(String args[]) {

		try {
			byte[] b1 = { 1, 2, 3 };
			byte[] b2 = new byte[8];

			// create a new RandomAccessFile with filename test
			RandomAccessFile raf = new RandomAccessFile("testfiles/mytext.txt", "rw");

			// write something in the file

			// set the file pointer at 0 position
			raf.seek(0);

			// read 2 bytes, starting from 1
			System.out.println("" + raf.read(b2));

			// set the file pointer at 0 position

			// read 3 bytes, starting from 4rth

			System.out.println(Arrays.toString(b2));
			// System.out.println(Arrays.toString(b1));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}
}
