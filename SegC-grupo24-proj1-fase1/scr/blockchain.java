import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;

public class blockchain {
    
    private ArrayList<block> blockchain = new ArrayList<>();
    private int difficulty = 5;
    private int sizeOfBlock = 5; //5 transactions for eachblock
    private KeyPair keyPair;


    public blockchain() throws Exception{
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        this.keyPair = keyPairGenerator.generateKeyPair();
    }

    public void addBlock(ArrayList<transaction> transactions) throws Exception {

        block previousBlock = getPreviousBlock();

        int id = blockchain.size();
        long timeStamp = System.currentTimeMillis();
        String previousHashString = previousBlock != null ? previousBlock.getHash() : "0000000000000000000000000000000000000000000000000000000000000000";

        block block = new block(id, transactions, timeStamp, previousHashString);

        block = mineBlock(block);

        blockchain.add(block);

    }

    public boolean isValid() throws Exception{
        for (int i = 0; i < blockchain.size(); i++) {
            block  block = blockchain.get(i);

            // Validate the block's signature
            if (!block.isSignedBy(keyPair)) {
                return false;
            }

            // Check the block's hash
            if (!block.isValid()) {
                return false;
            }

            // Check the chain of hashes
            if (i > 0) {
                block previousBlock = blockchain.get(i - 1);
                if (!block.getPreviousHash().equals(previousBlock.getHash())) {
                    return false;
                }
            }
        }

        return true;
    }


    private block mineBlock(block block) {
        String target = new String(new char[difficulty]).replace('\0', '0');

        // Keep trying to find a nonce until the target is met
        int nonce = 0;
        while (!block.getHash().substring(0, difficulty).equals(target)) {
            nonce++;
            block.setSignature(null);
            block.setHash(null);
            block.setId(block.getId() + 1);
            block.setTimestamp(System.currentTimeMillis());
            block.setPreviousHash(getPreviousBlock().getHash());
            block.setHash(block.calculateHash() + nonce);
        }

        // Sign the block with the server's private key
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(block.getHash().getBytes());
        byte[] signatureBytes = signature.sign();
        String signatureString = new String(signatureBytes);
        block.setSignature(signatureString);

        return block;
    }

    private block getPreviousBlock() {
        return null;
    }

}
