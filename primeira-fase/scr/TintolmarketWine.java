import java.util.ArrayList;
import java.util.HashMap;

public class TintolmarketWine {
    
    private String wineName;
    private String path; //Caminho para a imagem associada ao vinho
    private int finalClassification;
    private ArrayList<Integer> classifications;
    private HashMap<String,TintolmarketValues> sellersList;
    private ArrayList<String> hasClassified;


    //Listas de Sellers, quantitys e values têm de obrigatoriamente ter sempre o mesmo tamanho


    public TintolmarketWine(String wineName, String path){
        this.wineName = wineName;
        this.path = path;
        this.finalClassification = 0;
        this.hasClassified = new ArrayList<>();
        this.classifications = new ArrayList<>();
        this.sellersList = new HashMap<String,TintolmarketValues>();
    }

    public void setQuantity(String seller, int quant) {
        if(!sellersList.containsKey(seller)){
            addSeller(seller);
        }
        this.sellersList.get(seller).setQuantity(quant);
    } 

    public void setValue(String seller, int value) {
        if(!sellersList.containsKey(seller)){
            addSeller(seller);
        }
        this.sellersList.get(seller).setValue(value);
    } 

    public int giveClassification(int classification, String username) { //Return 1 when wine has been sucessfully classified or -1 when the user already classified the wine previously.
        if(hasClassified.contains(username)) {
            return -1; 
        } else {
            classifications.add(classification);
            hasClassified.add(username);
            return 1;
        }         
    } 

    public String getWinename(){
        return this.wineName;
    }

    public ArrayList<String> getListofSellers() {
        return new ArrayList<String>(sellersList.keySet());
    }

    public String getPath() {
        return this.path;
    }

    public int getQuantitySoldBySeller(String seller) {
        return this.sellersList.get(seller).getQuantity();
    }

    public int getValueOfWineSoldBySeller(String seller) {
        return this.sellersList.get(seller).getValue();
    }

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

    public void setFinalClassification(int num) {
        this.finalClassification = num;
    }


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
