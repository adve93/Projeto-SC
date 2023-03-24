/**
 * Projeto de Segurança e Confiabilidade - Fase 1 2023
 * @author Francisco Teixeira | FC56305
 * @author Afonso Soares | FC56314
 * @author Gonçalo Correia | FC56316
 */

 /**
  * TintolmarketValues represents a tuple of values related to a wine that is being sold by a user, a value and a quantity
  */
public class TintolmarketValues{

    private int value;
    private int quantity;

    /**
     * TintolmarketValues main constructor
     * @param value the value of a wine
     * @param quantity the quantity of a wine
     */
    public TintolmarketValues(int value, int quantity){
        this.value = value;
        this.quantity = value;
    }

    /**
     * Get wine quantity
     * @return the quantity atribute
     */
    public int getQuantity(){
        return this.quantity;
    }

    /**
     * Get wine value
     * @return the value atribute
     */
    public int getValue(){
        return this.value;
    }

    /**
     * Set the wine quantity
     */
    public void setQuantity(int qnt){
        this.quantity += qnt;
    }

    /**
     * Set the wine value
     */
    public void setValue(int value){
        this.value = value;
    }
}