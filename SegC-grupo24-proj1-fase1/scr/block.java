import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Object that represents a block of a blockchain
 */
public class block implements Serializable{

    private static final long serialVersionUID = 1L;

    //Attributes
    private int id;
    private List<transaction> data;
    private String hash;
    private String previousHash;
    private String signature;

    /**
     * Main constructor
     * @param id the id of the block
     * @param data the list of transactions(can be empty)
     * @param previousHash the hash of the previous block
     */
    public block(int id, List<transaction> data, String previousHash) {
        this.id = id;
        this.data = data;
        this.previousHash = previousHash;
        this.previousHash = previousHash;
    }

    
    /**
     * 
     * @return The if of the block
     */
    public int getId() {
        return this.id;
    }


    /**
     * 
     * @return the list of transactions
     */
    public  List<transaction> getListOfTransaction() {
        return this.data;
    }

    /**
     * 
     * @return the hash of the previous block
     */
    public String getPreviousHash() {
        return this.previousHash;
    }

    /**
     * 
     * @return the hash of this block, if it has one
     */
    public String getHash() {
        return this.hash;
    }

    /**
     * 
     * @param signature the signature of the server after the block is completed
     */
    public void setSignature(String signature) {
        this.signature = signature; 
        this.hash = calculateHash();
    }

    /**
     * 
     * @return the signature of the server after the block is completed
     */
    public String getSignature() {
        return this.signature;
    }


    /**
     * This func is used when initializing the server
     * @param hash the hash of this block
     */
    public void setHash(String hash){
        this.hash = hash;
    }

    /**
     * Add a new transaction 
     * @param transaction the new transaction
     */
    public void addTransaction(transaction transaction){
        this.data.add(transaction);
    }

    /**
     * 
     * @return calculate the hash of this block when needed
     */
    private String calculateHash() {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String blockData = id + data.toString() + previousHash + signature;
            byte[] hash = digest.digest(blockData.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte hashByte : hash) {
                sb.append(Integer.toHexString(0xff & hashByte));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Write to the blk file everytime a transaction is added
     */
    public void writeToDisk() {
        try {
            // create a file output stream for the block file
            FileOutputStream fos = new FileOutputStream("block" + id + ".blk");
    
            // create an object output stream for the file output stream
            ObjectOutputStream oos = new ObjectOutputStream(fos);
    
            // write the previous hash, id, number of transactions, and transactions to the file
            oos.writeObject(previousHash);
            oos.writeInt(id);
            oos.writeInt(data.size());
            for (transaction t : data) {
                oos.writeObject(t);
            }
    
            // write the server signature to the file if there is one
            if (signature != null) {
                oos.writeObject(signature);
            }
    
            // close the object output stream and file output stream
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the info from the blk file that is correspondent with this block
     * @return A string with the info of this block
     */
    public String readBlock(){
        StringBuilder sb = new StringBuilder();
        try {
            // create a file input stream for the block file
            FileInputStream fis = new FileInputStream("block" + id + ".blk");
    
            // create an object input stream for the file input stream
            ObjectInputStream ois = new ObjectInputStream(fis);
    
            // read in the previous hash, id, number of transactions, and transactions from the file
            String previousHash = (String) ois.readObject();
            int id = ois.readInt();
            int numTransactions = ois.readInt();
            List<transaction> transactions = new ArrayList<>();
            for (int i = 0; i < numTransactions; i++) {
                transaction t = (transaction) ois.readObject();
                transactions.add(t);
            }
    
            // read in the server signature from the file if there is one
            byte[] serverSignature = null;
            if (ois.available() > 0) {
                serverSignature = (byte[]) ois.readObject();
            }
    
            // print out the information for the block
            sb.append("Previous hash: " + previousHash + "\n");
            sb.append("ID: " + id + "\n");
            sb.append("Number of transactions: " + numTransactions + "\n");
            for (transaction t : transactions) {
                sb.append("Transaction: " + t.toString() + "\n");
            }
            if (serverSignature != null) {
                sb.append("Server signature: " + Arrays.toString(serverSignature) + "\n");
            }
    
            // close the object input stream and file input stream
            ois.close();
            fis.close();

            return sb.toString();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}