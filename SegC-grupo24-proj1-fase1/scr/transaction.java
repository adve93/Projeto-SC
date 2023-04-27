public class transaction {
    
    private String wineString;

    private int unites;

    private int wineValue;

    private String ownerId;

    private String signatureString;

    private transaction transactionType;

    public transaction(String wineString, int unites, int wineValue, String ownerId, transaction transactionType){
        this.wineString = wineString;
        this.unites = unites;
        this.wineValue = wineValue;
        this.ownerId = ownerId;
        this.transactionType = transactionType;
    }


    public String getWineString(){
        return this.wineString;
    }

    public int getUnites(){
        return this.unites;
    }

    public int getWineValue(){
        return this.wineValue;
    }

    public String getOwnerId(){
        return this.ownerId;
    }

    public String getSignatureString(){
        return this.signatureString;
    }

    public transaction getTransactionType(){
        return this.transactionType;
    }

    public void setSignature(String signatureString){
        this.signatureString = signatureString;
    }


    public String getData() {
        return wineString + "," + unites + "," + wineValue + "," + ownerId;
    }
}
