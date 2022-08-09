package hw4;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hw1.Database;
import hw1.HeapPage;
import hw1.Tuple;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool which check that the transaction has the appropriate
 * locks to read/write the page.
 */
public class BufferPool {
    /** Bytes per page, including header. */
    public static final int PAGE_SIZE = 4096;

    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;
    private int numPages;
    
    private HashMap<String, HeapPage> cache = new HashMap<>(); // all the heap page in the buffer pool, key is tableId+pageId
    
    private HashMap<Integer, ArrayList<String>> transIdToPages = new HashMap<>(); // all the page related to the transaction
    
    private HashMap<String, ArrayList<Integer>> readLocks = new HashMap<>(); // String is tableId+pageId(heapPage), integers is related transaction
    
    private HashMap<String, Integer> writeLocks = new HashMap<>(); // String is tableId+pageId(heapPage), integers is related transaction 
    

    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        // your code here
    	this.numPages = numPages;
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param tableId the ID of the table with the requested page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public HeapPage getPage(int tid, int tableId, int pid, Permissions perm)
        throws Exception {
        // your code here
    	String keyForHeapPage = tableId + "+" + pid;
    	if (writeLocks.containsKey(keyForHeapPage) && writeLocks.get(keyForHeapPage) != tid) {
    		transactionComplete(tid, false);
    		return null;
    	}
    	if (!cache.containsKey(keyForHeapPage) && cache.size() == numPages) {
    		evictPage();
    	}
    	if (perm == Permissions.READ_ONLY) {
    		ArrayList<Integer> transIds = readLocks.getOrDefault(keyForHeapPage, new ArrayList<>());
    		if (!transIds.contains(tid))  transIds.add(tid);
    		readLocks.put(keyForHeapPage, transIds);
    		
    	} else {
    		writeLocks.put(keyForHeapPage, tid);
    		
    	}
    	ArrayList<String> allPageForTid = transIdToPages.getOrDefault(keyForHeapPage, new ArrayList<>());
    	if (!allPageForTid.contains(allPageForTid)) allPageForTid.add(keyForHeapPage);
    	HeapPage page = Database.getCatalog().getDbFile(tableId).readPage(pid);
    	cache.put(keyForHeapPage, page);
    	transIdToPages.put(tid, allPageForTid);
    	
    	// add to the 
        return page;
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param tableID the ID of the table containing the page to unlock
     * @param pid the ID of the page to unlock
     */
    public  void releasePage(int tid, int tableId, int pid) {
        // your code here
    	String keyForHeapPage = tableId + "+" + pid;
    	if (writeLocks.containsKey(keyForHeapPage) && writeLocks.get(keyForHeapPage) == tid) {
    		writeLocks.remove(keyForHeapPage);
    		
    	}
    	if (readLocks.containsKey(keyForHeapPage)) {
    		ArrayList<Integer> transIds = readLocks.get(keyForHeapPage);
    		transIds.remove(tid);
    		if (transIds.size() == 0) readLocks.remove(keyForHeapPage); 
    	}
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public   boolean holdsLock(int tid, int tableId, int pid) {
        // your code here
    	String key = tableId + "+" + pid;
    	if (readLocks.containsKey(key)) return readLocks.get(key).contains(tid);  
        return writeLocks.containsKey(key) && writeLocks.get(key) == tid;
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction. If the transaction wishes to commit, write
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public  void transactionComplete(int tid, boolean commit)
        throws IOException {
        // your code here
    	if (!transIdToPages.containsKey(tid)) throw new IOException("no such transaction");
    	for (String keyForHeapPage : transIdToPages.get(tid)) {
    		
    		String[] keySplit = keyForHeapPage.split("\\+", 2);
    		int tableId = Integer.parseInt(keySplit[0]);
    		
    		int pid = Integer.parseInt(keySplit[1]);
    		
    		releasePage(tid, tableId, pid);
    		if (cache.get(keyForHeapPage).isDirty() && cache.get(keyForHeapPage).getTransId() == tid) {
    			if (commit) {
        			flushPage(tableId, pid);
        		} else {
        			cache.remove(keyForHeapPage);
        			
        		}
    			
    		}
    		
    		
    	}
    	
    }

    /**
     * Add a tuple to the specified table behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to. May block if the lock cannot 
     * be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public  void insertTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
    	String keyToHeapPage = tableId + "+" +t.getPid();
    	
    	if (!writeLocks.containsKey(keyToHeapPage)) {
    		throw new Exception("can not write, do not have lock");
    	} 
    	HeapPage page = cache.get(keyToHeapPage);
		page.setDirty(true, tid);
		page.addTuple(t);
		cache.put(keyToHeapPage, page);
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from. May block if
     * the lock cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty.
     *
     * @param tid the transaction adding the tuple.
     * @param tableId the ID of the table that contains the tuple to be deleted
     * @param t the tuple to add
     */
    public  void deleteTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
    	String keyForHeapPage = tableId + "+" + t.getPid();
    	
    	if (!writeLocks.containsKey(keyForHeapPage)) {
    		throw new Exception("can not write, do not have lock");
    	} 
    	HeapPage page = cache.get(keyForHeapPage);
		page.setDirty(true, tid);
		page.deleteTuple(t);
		cache.put(keyForHeapPage, page);
    }

    private synchronized  void flushPage(int tableId, int pid) throws IOException {
        // your code here
    	String keyForHeapPage = tableId + "+" + pid;
    	
    	if (!cache.containsKey(keyForHeapPage)) throw new IOException("No such heap page exists");
    	HeapPage page = cache.get(keyForHeapPage);
    	page.setDirty(false, -1);
    	Database.getCatalog().getDbFile(tableId).writePage(page);
    
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized  void evictPage() throws Exception {
        // your code here
    	for (Map.Entry<String, HeapPage> e : cache.entrySet()) {
    		String keyForHeapPage = e.getKey();
    		HeapPage page = e.getValue();
    		if (!page.isDirty()) {
    			for (ArrayList<String> pages : transIdToPages.values()) {
    				pages.remove(keyForHeapPage);
    				
    				
    			}
    			readLocks.remove(keyForHeapPage);
    			writeLocks.remove(keyForHeapPage);
    			return;
    		}
    		
    		
    	}
    	throw new Exception("No such page exist");
    }
    public static void main(String args[]) {
    	String string = "1903076468+0";
    	String[] splitStrings = string.split("\\+");
    	System.out.print(splitStrings[0]);
    	System.out.print(splitStrings[1]);
    	
    }

}
