import java.util.ArrayList;

public class TintolmarketWine {
    
    private String wineName;
    private ArrayList<String> sellers; //String com o username do vendedor
    private String path; //Caminho para a imagem associada ao vinho
    private ArrayList<Integer> quantitys; //Numero de vinhos em stock
    private ArrayList<Integer> values; //Preço do vinho
    private ArrayList<Integer> classifications;

    //Listas de Sellers, quantitys e values têm de obrigatoriamente ter sempre o mesmo tamanho


    public TintolmarketWine(String wineName, String path){
        this.wineName = wineName;
        this.sellers = new ArrayList<>();
        this.path = path;
        this.quantitys = new ArrayList<>();
        this.values = new ArrayList<>();
        this.classifications = new ArrayList<>();
    }

    public void setQuantity(String user, int quant) {
        if(!sellers.contains(user)){
            addSeller(user);
        }

        this.quantitys.add(sellers.indexOf(user), quant);

    } 

    public void setValue(String user, int value) {
        if(!sellers.contains(user)){
            addSeller(user);
        }

        this.values.add(sellers.indexOf(user), value);
    } 

    public void giveClassification(int classification) {
        classifications.add(classification);
    } 

    public String getWinename(){
        return this.wineName;
    }

    public ArrayList<String> getListofSellers() {
        return this.sellers;
    }

    public String getPath() {
        return this.path;
    }

    public int getQuantitySoldBySeller(String user) {
        return this.quantitys.get(sellers.indexOf(user));
    }

    public float getValueOfWineSoldBySeller(String user) {
        return this.values.get(sellers.indexOf(user));
    }

    public int getClassification() {
        int sum = 0;
        for(Integer num : this.classifications) {
            sum += num;
        }
        return sum / (this.classifications.size());
    }


    private void addSeller(String user){
        sellers.add(user);
        quantitys.add(-1);
        values.add(-1);
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
