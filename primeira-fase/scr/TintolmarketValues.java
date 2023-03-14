public class TintolmarketValues{

    private int value;
    private int quantity;

    public TintolmarketValues(int value, int quantity){
        this.value = value;
        this.quantity = value;
    }

    public int getQuantity(){
        return this.quantity;
    }

    public int getValue(){
        return this.value;
    }

    public void setQuantity(int qnt){
        this.quantity += qnt;
    }

    public void setValue(int value){
        this.value = value;
    }
}