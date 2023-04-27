import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.ArrayList;

public class block {

    private int id;
    private ArrayList<transaction> data;
    private long timestamp;
    private String hash;
    private String previousHash;
    private String signature;

    public block(int id, ArrayList<transaction> data, Long timeStamp, String previousHash) {
        this.id = id;
        this.data = data;
        this.previousHash = previousHash;
        this.hash = calculateHash();
        this.previousHash = previousHash;
        this.timestamp = timestamp;
    }

    public int getId() {
        return this.id;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public  ArrayList<transaction> getListOfTransaction() {
        return this.data;
    }

    public String getPreviousHash() {
        return this.previousHash;
    }

    public String getHash() {
        return this.hash;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return this.signature;
    }


    private String calculateHash() {
        String dataToHash = id + previousHash + Long.toString(timestamp);
        for(transaction transaction :  data){
            dataToHash += transaction.getData();
        }
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToHash.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();
            for(int i = 0; i < hash.length; i++){
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append("0");
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public boolean isValid(){
        String newDataHash = calculateHash();
        if(!newDataHash.equals(hash)){
            return false;
        }
        return true;
    }

    public boolean isSignedBy(KeyPair keyPair) throws Exception{
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(keyPair.getPublic());
        signature.update(hash.getBytes());
        return signature.verify(this.signature.getBytes());
    }
}