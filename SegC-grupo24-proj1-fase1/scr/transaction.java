import java.io.Serializable;

/**
 * Object that represents a transaction to be stored in a block of a blockchain
 */
public class transaction implements Serializable{
    
    //Attributes 
    private String wineString;

    private int unites;

    private int wineValue;

    private String ownerId;

    private transactionType transactionType;

    /**
     * Main constructor
     * 
     * @param wineString the name of the wine it was interacted with
     * @param unites the number of units interacted with
     * @param wineValue the value of a single unit of the wine it was interacted with
     * @param ownerId the id of the user that made de sell or buy operation
     * @param transactionType the type of operation realized
     */
    public transaction(String wineString, int unites, int wineValue, String ownerId, transactionType transactionType){
        this.wineString = wineString;
        this.unites = unites;
        this.wineValue = wineValue;
        this.ownerId = ownerId;
        this.transactionType = transactionType;
    }

    /**
     * 
     * @return The name of the wine
     */
    public String getWineString(){
        return this.wineString;
    }

    /**
     * 
     * @return The units interacted with
     */
    public int getUnites(){
        return this.unites;
    }

    /**
     * 
     * @return The value of a single bottle
     */
    public int getWineValue(){
        return this.wineValue;
    }

    /**
     * 
     * @return The id of the user that made the operation
     */
    public String getOwnerId(){
        return this.ownerId;
    }

    /**
     * 
     * @return The transaction type
     */
    public transactionType getTransactionType(){
        return this.transactionType;
    }


    @Override
    public String toString(){
        return "Transaction of type: " + transactionType + " --> " + " Name of Wine: " + wineString + " / Unites Manipulated: " + unites + " / Value of each Unit: " +
        wineValue + " / ID of user that perform de transaction: " + ownerId;
        
    }

 

}
