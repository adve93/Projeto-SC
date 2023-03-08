public class TintolmarketWine {
    
    private String seller; //String com o username do vendedor
    private String path; //Caminho para a imagem associada ao vinho
    private int quantity; //Numero de vinhos em stock
    private float value; //Preço do vinho
    private int classification; //Classificação média do vinho

    public TintolmarketWine(String seller, String path){
        this.seller = seller;
        this.path = path;
        this.classification = -1; //classificaçao começa a -1 quando ninguem classificou o vinho ainda
    }

    public void setQuantity(int quant) {
        this.quantity = quant;
    } 

    public void setValue(float value) {
        this.value = value;
    } 

    public void setClassification(int classification) {
        if(this.classification == -1) {
            this.classification = classification; //Caso não exista nenhuma classificação, coloca o valor diretamente em classifcation
        } else {
            this.classification = (this.classification + classification)/2;//Caso já exista uma classificação, faz a média da classificação atual com a nova classificação adicionada
        }
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
        return this.classification;
    }

}
