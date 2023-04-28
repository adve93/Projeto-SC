import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents the blockchain itself and manages the blocks
 */
public class blockchain {
    
    //Attributes
    private int blockSize;
    private List<block> blockchain;

    /**
     * Main constructor
     * @param blockSize the max number of transactions a block can have
     */
    public blockchain(int blockSize){
        this.blockSize = blockSize;
        this.blockchain = new ArrayList<>();
    }


    /**
     * 
     * @return the list of blocks 
     */
    public List<block> getListOBlocks(){
        return this.blockchain;
    }


    /**
     * Uploads a block that was extracted from a blk file
     * @param previousHash the hash of the previous block
     * @param id the id of the block
     * @param tr the list of transactions
     */
    public void uploadBlock(String previousHash, int id, List<transaction> tr){
        blockchain.add(new block(id, tr, previousHash));
    }

    /**
     * Adds a new transaction to the block chain
     * @param transaction the transaction to be added
     */
    public void addTransaction(transaction transaction) {
        if (blockchain.isEmpty() || blockchain.get(blockchain.size() - 1).getListOfTransaction().size() == blockSize) {
            // Create a new block if there are no blocks or the current block is full
            String previousHash = blockchain.isEmpty() ? "00000000000000000000000000000000" : blockchain.get(blockchain.size() - 1).getHash();
            block newBlock = new block(blockchain.size() + 1, new ArrayList<>(), previousHash);
            blockchain.add(newBlock);
        }
        // Add the transaction to the latest block
        blockchain.get(blockchain.size() - 1).addTransaction(transaction);
        if (blockchain.get(blockchain.size() - 1).getListOfTransaction().size() == blockSize) {
            // Sign the latest block if it is full
            String signature = "server_signature"; // Replace with actual server signature
            blockchain.get(blockchain.size() - 1).setSignature(signature);
        }
        blockchain.get(blockchain.size() - 1).writeToDisk();
    }

    /**
     * 
     * @return The last block on the blockchain
     */
    public block getLatestBlock() {
        return blockchain.get(blockchain.size() - 1);
    }

    /**
     * Checks if the chain is valid after loading it from the blk files
     * @return a boolean saying if the chain is valid
     */
    public boolean isChainValid() {
        for (int i = 0; i < blockchain.size() - 1; i++) {
            if(i != blockchain.size() - 1){
                block currentBlock = blockchain.get(i);
                block previousBlock = blockchain.get(i - 1);
    
                if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                    return false;
                }
            } 
        }
        return true;
    }

    /**
     * Load the hashes from each block from their blks, this is used to verify if the blockchain is valid
     */
    public void loadHashes(){
        for (int i = 0; i < blockchain.size() - 1; i++) {
            if(i != blockchain.size() - 1){
                block currentBlock = blockchain.get(i);
                block nextBlock = blockchain.get(i+1);

                currentBlock.setHash(nextBlock.getPreviousHash());

            } 
        }
    }



    @Override
    public String toString() {
        return "Blockchain{" +
                "maxTransactionsPerBlock=" + blockSize +
                ", blocks=" + blockchain +
                '}';
    }
}
