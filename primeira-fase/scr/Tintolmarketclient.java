public class Tintolmarketclient {

    private String username;
    private String pass;
    private int saldo;

    public Tintolmarketclient(String user, String pass, int saldo){

        this.username = user;
        this.pass = pass;
        this.saldo = saldo;

    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.pass;
    }

    public int getSaldo() {
        return this.saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    @Override
    public boolean equals(Object client){

        if(client == this){
            return true;
        }

        if(!(client instanceof Tintolmarketclient)){
            return false;
        }

        Tintolmarketclient c = (Tintolmarketclient) client;

        return c.getUsername().equals(getUsername());
    }

}
