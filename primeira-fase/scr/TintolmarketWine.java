/**
 * Projeto de Segurança e Confiabilidade - Fase 1 2023
 * @author Francisco Teixeira | FC56305
 * @author Afonso Soares | FC56314
 * @author Gonçalo Correia | FC56316
 */

//Imports for the project 
import java.util.ArrayList;
import java.util.HashMap;

/**
 * TintolmarketWine represents a wine that is available in the Tintol Market 
 */
public class TintolmarketWine {
    
    private String wineName;
    private String path;
    private int finalClassification;
    private ArrayList<Integer> classifications;
    private HashMap<String,TintolmarketValues> sellersList;
    private ArrayList<String> hasClassified;

    /**
     * TintolmarketWine constructor
     * @param wineName the name of the wine
     * @param path the path to the wine image
     */
    public TintolmarketWine(String wineName, String path){
        this.wineName = wineName;
        this.path = path;
        this.finalClassification = 0;
        this.hasClassified = new ArrayList<>();
        this.classifications = new ArrayList<>();
        this.sellersList = new HashMap<String,TintolmarketValues>();
    }

    /**
     * Set the wine quantity sold by a certain seller
     * @param seller The wine seller
     * @param quant The quantity
     */
    public void setQuantity(String seller, int quant) {
        if(!sellersList.containsKey(seller)){
            addSeller(seller);
        }
        this.sellersList.get(seller).setQuantity(quant);
    } 

    /**
     * Set the value of a wine being sold by a seller
     * @param seller The wine seller
     * @param value The value of the wine being sold
     */
    public void setValue(String seller, int value) {
        if(!sellersList.containsKey(seller)){
            addSeller(seller);
        }
        this.sellersList.get(seller).setValue(value);
    } 

    /**
     * Gives a classification of this wine by a user
     * @param classification The classification (1-5)
     * @param username The user giving the classification
     * @return Returns 1 if the user never classified the wine or -1 if they already classified this wine
     */
    public int giveClassification(int classification, String username) { //Return 1 when wine has been sucessfully classified or -1 when the user already classified the wine previously.
        if(hasClassified.contains(username)) {
            return -1; 
        } else {
            classifications.add(classification);
            hasClassified.add(username);
            return 1;
        }         
    } 

    /**
     * Gets the wine name
     * @return The name of the wine
     */
    public String getWinename(){
        return this.wineName;
    }

    /**
     * Gets the list of sellers selling this wine
     * @return list of sellers selling this wine
     */
    public ArrayList<String> getListofSellers() {
        return new ArrayList<String>(sellersList.keySet());
    }

    /**
     * Gets the path to the wine image
     * @return path to the wine image
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Gets quantity of this wine sold by a seller
     * @param seller the wine seller
     * @return the quantity sold by the specified seller
     */
    public int getQuantitySoldBySeller(String seller) {
        return this.sellersList.get(seller).getQuantity();
    }

    /**
     * Gets the value of a wine sold by a seller
     * @param seller the wine seller
     * @return the value of the wine sold by a specified seller
     */
    public int getValueOfWineSoldBySeller(String seller) {
        return this.sellersList.get(seller).getValue();
    }

    /**
     * Gets the wine classification
     * @return wine classification
     */
    public int getClassification() {
        for(Integer num : this.classifications) {
            finalClassification += num;
        }
        if (this.classifications.size() > 0){
            finalClassification = finalClassification / (this.classifications.size());
            return finalClassification;
        }
        return 0;
        
    }

    /**
     * Sets the final value for classification
     * @param num value to set finalClassification as
     */
    public void setFinalClassification(int num) {
        this.finalClassification = num;
    }

    /**
     * Adds a new seller of this type of wine
     * @param user The user that is selling this wine 
    */
    private void addSeller(String user){
        sellersList.put(user, new TintolmarketValues(0,0));
    }

    //Metodo equals do object wine, retirado de "geeksforgeeks.org/overriding-equals-method-in-java/"
    @Override
    public boolean equals(Object wine){

        if(wine == this){
            return true;
        }

        if(!(wine instanceof TintolmarketWine)){
            return false;
        }

        TintolmarketWine c = (TintolmarketWine) wine;

        return c.getWinename().equals(getWinename());
    }

}
