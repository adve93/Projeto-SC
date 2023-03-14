import java.util.ArrayList;
import java.util.HashMap;

public class TintolmarketWine {
    
    private String wineName;
    private String path; //Caminho para a imagem associada ao vinho
    private ArrayList<Integer> classifications;
    private HashMap<String,TintolmarketValues> sellersList;


    //Listas de Sellers, quantitys e values têm de obrigatoriamente ter sempre o mesmo tamanho


    public TintolmarketWine(String wineName, String path){
        this.wineName = wineName;
        this.path = path;
        this.classifications = new ArrayList<>();
        this.sellersList = new HashMap<String,TintolmarketValues>();
    }

    public void setQuantity(String selller, int quant) {
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

    public void giveClassification(int classification) {
        classifications.add(classification);
    } 

    public String getWinename(){
        return this.wineName;
    }

    public HashMap<String,TintolmarketValues> getListofSellers() {
        return this.sellersList;
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
        int sum = 0;
        for(Integer num : this.classifications) {
            sum += num;
        }
        if (this.classifications.size() > 0){
            return sum / (this.classifications.size());
        }
        return 0;
        
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
