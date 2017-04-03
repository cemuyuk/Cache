import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;


public class Cache {

	private int blockSize;
	private int cacheSize;
	private int associativity;
	private int numberOfBlocks;
	private String filename;
	private int[][] addresses;
	private boolean[] validBitAddresses;//boolean to know if a block is empty or not
	private int numberOfSets;
	private int bSizeForArray;
	private ArrayList<Integer> txtAddresses=new ArrayList<Integer>();
	private String logFile;
	private int historyResetFreq;
	private int[] historyBit;//history bit is a different array that is parallel to the blocks of the cache
	//we follow the track of the history bits from this array.

	/*
	 * Constructor that takes block size, cache size, associativity, file name, log file name and reset frequency as arguments.
	 */
	public Cache(int blockSize, int cacheSize, int associativity, String filename, String logFile, int historyResetFreq){
		this.blockSize=blockSize;
		this.cacheSize=cacheSize;
		this.associativity=associativity;
		this.filename=filename;	
		int bSizeForArray=this.blockSize/4; //block size in bytes is 4 times more than the number of addresses in a block
		int cSizeForArray=this.cacheSize/this.blockSize;
		this.bSizeForArray=bSizeForArray;
		this.logFile=logFile;
		this.historyResetFreq=historyResetFreq;

		addresses=new int[cSizeForArray][bSizeForArray];

		validBitAddresses = new boolean[cSizeForArray];
		historyBit = new int[cSizeForArray];
		for(int i=0;i<cSizeForArray;i++){
			validBitAddresses[i]=false;
		}

		numberOfBlocks=this.cacheSize/this.blockSize;
		numberOfSets=cacheSize/(associativity*blockSize);

	}

	public int getNumOfSets(){
		return numberOfSets;
	}

	public int getBlockSize() {
		return blockSize;
	}


	public int getHistoryResetFreq() {
		return historyResetFreq;
	}


	public void setHistoryResetFreq(int historyResetFreq) {
		this.historyResetFreq = historyResetFreq;
	}


	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public int getAssociativity() {
		return associativity;
	}


	public int getbSizeForArray() {
		return bSizeForArray;
	}


	public boolean[] getValidBitForAddresses() {
		return validBitAddresses;
	}



	public String getLogFile() {
		return logFile;
	}


	public void setLogFile(String textFile) {
		this.logFile = textFile;
	}


	public void setValidBitForAddresses(boolean[] boolForAddress) {
		this.validBitAddresses = boolForAddress;
	}


	public void setbSizeForArray(int bSizeForArray) {
		this.bSizeForArray = bSizeForArray;
	}


	public void setAssociativity(int associativity) {
		this.associativity = associativity;
	}

	public int getNumberOfBlocks() {
		return numberOfBlocks;
	}


	public ArrayList<Integer> getTxtAddresses() {
		return txtAddresses;
	}


	public void setTxtAddresses(ArrayList<Integer> addressesInArrayList) {
		this.txtAddresses = addressesInArrayList;
	}


	public void setNumberOfBlocks(int numberOfBlocks) {
		this.numberOfBlocks = numberOfBlocks;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int[][] getAddresses() {
		return addresses;
	}

	public void setAddresses(int[][] addresses) {
		this.addresses = addresses;
	}


	public int[] getHistoryBit() {
		return historyBit;
	}


	public void setHistoryBit(int[] historyBit) {
		this.historyBit = historyBit;
	}




}
