import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


public class Perform {
	private Cache myCache;
	private int hit;
	private int miss;
	private int operationCount;
	private int historyFreqCount=0;
	private String lineNumbers="";
	private int emptyOrWrongAddress=0;

	public Perform() throws IOException{
		int bSize=takeTheBlockSize();
		int cSize=takeTheCacheSize(bSize);
		int assoc=getTheAssociativity(cSize/bSize);
		String fileName=getTheAddress();
		int freq=getHistoryResetFreq();
		String logName=getLogName();
		Cache newCache = new Cache(bSize, cSize, assoc, fileName, logName, freq);
		this.myCache=newCache;
		getTheAddressesToArrayList(myCache.getFilename());
	}

	/*
	 * Fills the blocks and counts hits and misses with the auxilary procedure, fillOneRow(index).
	 * Also writes on the log file.
	 */
	public void fillAllTheRows() throws IOException{
		try{
			String logName=myCache.getLogFile();
			FileWriter tempWr;
			tempWr=new FileWriter(new File(logName));
			tempWr.write("");
			tempWr.close();
			int size=myCache.getTxtAddresses().size();
			for(int i=0;i<size;i++){
				fillOneRow(i);
			}

			for(int i=0;i<myCache.getAddresses().length;i++){
				if(i % myCache.getAssociativity() == 0)
					System.out.println("\nSet" + i/myCache.getAssociativity());
				for(int j=0; j<myCache.getAddresses()[0].length;j++){
					if(j==0){
						System.out.println("\nBlock");
					}
					System.out.println(myCache.getAddresses()[i][j]);
				}
			}
			FileWriter w;
			w = new FileWriter(new File(logName), true);
			w.write("Operation count:" +operationCount+"\n"+
					"Hits: " +hit+"\n"+
					"Misses: "+miss+".\n"
							+ "Number of empty streams: "+emptyOrWrongAddress
							+ "\nLine numbers in the address text file you entered that has empty address: "+lineNumbers+".");

			w.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Hits:" +hit+ " Misses:" +miss);
	}


	/*
	 * There is an arraylist in the cache in which there are the addresses written in the text file.
	 * This procedure takes an index input and finds the address in the array list according to the index 
	 * argument.
	 * 
	 * Then it checks if that address is in the cache. If yes, increments the operation count and the hits.
	 * 
	 * If not, it is a miss. Then brings the address and some addresses increasing with the (mod numberOfSets)
	 * into the cache according to how many addresses a block can take.
	 */
	public void fillOneRow(int inputLoc) throws IOException{
		String logFile = myCache.getLogFile();
		int blockSize = myCache.getbSizeForArray();
		int[] temporaryArr = new int[blockSize];
		boolean test=false;
		FileWriter w;
		try {
			w = new FileWriter(new File(logFile), true);
			int temporaryAddr=myCache.getTxtAddresses().get(inputLoc);
			for(int check=0; check<blockSize;check++){
				temporaryArr[check]=temporaryAddr;
				/*
				 * hit case
				 */
				if(temporaryAddr==-33333){
					test=true;
					w.write("There is an empty address stream on the line "+(inputLoc+1)+" in the .txt file "
							+ " that user entered for addresses. Operation count"
							+ " does not increase\n");
					emptyOrWrongAddress++;
					lineNumbers+=inputLoc+1+", ";
					break;
				}else if(addressInTheCache(temporaryAddr)){
					hit++;
					test=true;
					historyFreqCount++;
					operationCount++;
					int loc=giveTheIndexIfHit(temporaryAddr);
					if(myCache.getHistoryBit()[loc]==0){
						myCache.getHistoryBit()[loc]=1;
						w.write(operationCount+"---"+temporaryAddr+" address is in the cache. Hit! "
								+ " History bit is set to 01 now.\n");
					}else if(myCache.getHistoryBit()[loc]==1 || myCache.getHistoryBit()[loc]==2 
							|| myCache.getHistoryBit()[loc]==3){
						myCache.getHistoryBit()[loc]=3;
						w.write(operationCount+"---"+temporaryAddr+" address is in the cache. Hit! "
								+ " History bit is set to 11 now.\n");
					}
					break;
					/*
					 * miss case also continued below with the if(test!=true) check
					 */
				}else{
					temporaryArr[check]=temporaryAddr;
					temporaryAddr+=myCache.getNumOfSets();
				}
			}

		/*
		 * PART I New address is found through random number.
		 * Random rd = new Random();
		 * int upperBound=myCache.getAssociativity()*(mod+1);
		 * int lowerBound=myCache.getAssociativity()*mod;
		 * int columnNoWithLRU=rd.nextInt(upperBound-lowerBound) + lowerBound;
		 */
		if(test!=true){
			miss++;
			operationCount++;
			w.write(operationCount+"---"+temporaryArr[0]+" address is not in the cache. Miss!"
					+ " History bit is set to 00 now.\n");
			historyFreqCount++;
			int mod = temporaryArr[0] % myCache.getNumOfSets();
			int columnNoIfThereAreFreeBlocks = giveTheLocation(mod);
			int columnNoWithLRU = giveTheLocationLRU(mod);
			for(int j=0; j<blockSize; j++){
				if(columnNoIfThereAreFreeBlocks!=-1){
					myCache.getAddresses()[columnNoIfThereAreFreeBlocks][j]=temporaryArr[j];
					myCache.getHistoryBit()[columnNoIfThereAreFreeBlocks]=0;
				}else{
					myCache.getAddresses()[columnNoWithLRU][j]=temporaryArr[j];
					myCache.getHistoryBit()[columnNoWithLRU]=0;
				}
			}
		}
		if(historyFreqCount==myCache.getHistoryResetFreq()) {
			setTheHistoryBitsToZero();
			w.write("History reset due to frequency count reach!\n");
			historyFreqCount=0;
		}
		w.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Sets the history bits of the cache to zero.
	 */

	public void setTheHistoryBitsToZero(){
		for(int i=0; i<myCache.getHistoryBit().length;i++){
			myCache.getHistoryBit()[i]=0;
		}
	}

	/*
	 * Gives out a new location for the missed address.
	 */

	public int giveTheLocationLRU(int mod){
		Random rd = new Random();
		int upperBound=myCache.getAssociativity()*(mod+1);
		int lowerBound=myCache.getAssociativity()*mod;
		int result=rd.nextInt(upperBound-lowerBound) + lowerBound;
		for(int i=lowerBound; i<upperBound; i++){
			if(myCache.getHistoryBit()[i]==0)
				return i;
		}

		for(int j=lowerBound; j<upperBound; j++){
			if(myCache.getHistoryBit()[j]==1)
				return j;
		}

		for(int k=lowerBound; k<upperBound; k++){
			if(myCache.getHistoryBit()[k]==2)
				return k;
		}

		for(int l=lowerBound; l<upperBound; l++){
			if(myCache.getHistoryBit()[l]==3)
				return l;
		}

		return result;
	}

	/*
	 * Gives a location for Part I. With a random access.
	 */

	public int giveTheLocation(int mod){
		int upperBound=myCache.getAssociativity()*(mod+1);
		int lowerBound=myCache.getAssociativity()*mod;
		for(int i=lowerBound; i<upperBound; i++){
			if(myCache.getValidBitForAddresses()[i]!=true){
				myCache.getValidBitForAddresses()[i]=true;
				return i;
			}
		}
		return -1;
	}
	/*
	 * Checks if an address is in the cache.
	 */

	public boolean addressInTheCache(int input){
		int[][] newArr=myCache.getAddresses();
		int columnSize = newArr.length;
		int blockSize = newArr[0].length;

		for(int i=0; i<columnSize; i++){
			for(int j=0; j<blockSize; j++){
				if (newArr[i][j]==input)
					return true;
			}
		}
		return false;
	}

	/*
	 * Gives the index of the "hit" address to manipulate the history bit.
	 */
	public int giveTheIndexIfHit(int input){
		int[][] newArr=myCache.getAddresses();
		int columnSize = newArr.length;
		int blockSize = newArr[0].length;

		for(int i=0; i<columnSize; i++){
			for(int j=0; j<blockSize; j++){
				if (newArr[i][j]==input)
					return i;
			}
		}
		return -1;
	}

	/*
	 * fills the array list with the addresses in the text
	 */
	public void getTheAddressesToArrayList(String inputFile) throws IOException{
		try {
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			for(String m = in.readLine(); m!=null; m = in.readLine()) {
				if(m.equals("")) 
					myCache.getTxtAddresses().add(-33333);
				else
				myCache.getTxtAddresses().add(Integer.parseInt(m));
			}
			in.close();
		} catch (IOException e) {
			System.out.println("File Read Error");
		}
	}

	/*
	 * reads the freq from the user
	 */
	public int getHistoryResetFreq(){

		Scanner reader = new Scanner(System.in);
		System.out.println("For no input, just press 'Enter' button."
				+ " \nEnter the history frequency of the cache:");
		String input3=reader.nextLine();
		int freq;
		if(input3.equals("")){
			freq=100;
		}else{
			freq=Integer.parseInt(input3);
		}
		return freq;
	}

	/*
	 * reads the log file name from the user.
	 */
	public String getLogName(){
		Scanner reader = new Scanner(System.in);
		System.out.println("For no input, just press 'Enter' button."
				+ " \nEnter the log file name for the cache without .txt extension and any spelling mistakes:");
		String addr=reader.nextLine();
		if(addr.equals(""))
			addr="log";
		addr=addr+".txt";
		return addr;
	}

	/*
	 * reads the address from the user.
	 */
	public String getTheAddress(){
		Scanner reader = new Scanner(System.in);
		System.out.println("For no input, just press 'Enter' button."
				+ " \nEnter the address file name for the cache without .txt extension and any spelling mistakes:");
		String addr=reader.nextLine();
		if(addr.equals(""))
			addr="addresses";
		addr=addr+".txt";
		return addr;
	}

	/*
	 * auxilary method for reading the associativity.
	 */
	public int getTheAssociativityHelper(){
		Scanner reader = new Scanner(System.in);
		System.out.println("For no input, just press 'Enter' button."
				+ " \nEnter the associativity of the cache:");
		String input3=reader.nextLine();
		int Assoc;
		if(input3.equals("")){
			Assoc=1;
		}else{
			Assoc=Integer.parseInt(input3);
		}
		return Assoc;
	}

	/*
	 * reads the associativity.
	 */
	public int getTheAssociativity(int numberOfBlocks){
		int result = getTheAssociativityHelper();
		while(numberOfBlocks % result != 0){
			System.out.println("You have to enter the associativity such that the number of blocks"
					+ " is divisible by the associativity! Number of blocks: "+numberOfBlocks+".");
			result=getTheAssociativityHelper();
		}

		return result;
	}

	/*
	 * auxilary method for reading the cache size
	 */
	public int takeTheCacheSizeHelper(){
		Scanner reader = new Scanner(System.in);
		System.out.println("For no input, just press 'Enter' button."
				+ " \nEnter the cache size in bytes:");
		String input2=reader.nextLine();
		int cSize;
		if(input2.equals("")){
			cSize=512;
		}else{
			cSize=Integer.parseInt(input2);
		}
		return cSize;
	}

	/*
	 * reads the cache size
	 */
	public int takeTheCacheSize(int bSize){
		int result = takeTheCacheSizeHelper();
		while(result % bSize !=0){
			System.out.println("You have to enter the cache size in multiples of block size!");
			result = takeTheCacheSizeHelper();
		}
		return result;
	}
	/*
	 * auxilary method for reading the block size in bytes
	 */
	public int takeTheBlockSizeHelper(){
		Scanner reader = new Scanner(System.in);
		System.out.println("For no input, just press 'Enter' button."
				+ " \nEnter the block size of the cache in bytes:");
		String input1=reader.nextLine();
		int bSize;
		if(input1.equals("")){
			bSize=4;
		}else{
			bSize=Integer.parseInt(input1);
		}
		return bSize;
	}

	/*
	 * reads the block size
	 */
	public  int takeTheBlockSize(){
		int result=takeTheBlockSizeHelper();
		while(result % 4 != 0){
			System.out.println("You have to enter the block size in multiples of 4!");
			result=takeTheBlockSizeHelper();
		}
		return result;
	}

	public Cache getMyCache() {
		return myCache;
	}

	public void setMyCache(Cache myCache) {
		this.myCache = myCache;
	}
}
