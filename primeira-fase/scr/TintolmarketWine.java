import java.util.ArrayList;

public class TintolmarketWine {
    
    private String wineName;
    private String seller; //String com o username do vendedor
    private String path; //Caminho para a imagem associada ao vinho
    private int quantity; //Numero de vinhos em stock
    private float value; //Preço do vinho
    private ArrayList<Integer> classifications;

    public TintolmarketWine(String wineName, String seller, String path){
        this.wineName = wineName;
        this.seller = seller;
        this.path = path;
        this.quantity = 0;
        this.value = 0;
        this.classifications = new ArrayList<>();
    }

    public void setQuantity(int quant) {
        this.quantity = quant;
    } 

    public void setValue(float value) {
        this.value = value;
    } 

    public void giveClassification(int classification) {
        classifications.add(classification);
    } 

    public String getWinename(){
        return this.wineName;
    }

    public String getSeller() {
        return this.seller;
    }

    public String getPath() {
        return this.path;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public float getValue() {
        return this.value;
    }

    public int getClassification() {
        int sum = 0;
        for(Integer num : this.classifications) {
            sum += num;
        }
        return sum / (this.classifications.size());
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
