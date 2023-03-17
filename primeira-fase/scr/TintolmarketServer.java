import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//import java.io.PrintWriter;
//import java.io.UnsupportedEncodingException;
//import java.io.ObjectOutputStream.PutField;
import java.net.ServerSocket;
import java.net.Socket;
//import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Scanner;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;

public class TintolmarketServer {

    private HashMap<String,String> userList;
    private ServerSocket sSocket;
    //private File users;
    //private FileWriter writer;
    //private PrintWriter pw;
    private ArrayList<TintolmarketWine> wineList;
    private ArrayList<String> inbox;
    private ArrayList<ServerThread> users;

    public TintolmarketServer(ServerSocket sSocket) {
        this.sSocket = sSocket;
        this.userList = new HashMap<String,String>();
        this.wineList = new ArrayList<>();
        this.inbox = new ArrayList<>();  
        this.users = new ArrayList<>();
    }

    public static void main(String[] args) {
        ServerSocket sSocket = null;
        
        try {

			sSocket = new ServerSocket(12345);

		} catch (IOException e) {
            
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		TintolmarketServer server = new TintolmarketServer(sSocket);
		server.startServer();
	}

    public void startServer() {
        try {

            while(!sSocket.isClosed()) {

                Socket newClientSocket = sSocket.accept();
                System.out.println("A new client is connecting. Awaiting authentication.");
                ServerThread newServerThread = new ServerThread(newClientSocket);
                newServerThread.start();

            }

        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    class ServerThread extends Thread {
        private Socket socket = null;
        private String username; //Identificador da thread.
        private ObjectOutputStream outStream; //outStream e inStream passados para variaveis globais de serverThread para podermos criar metodos como read e talk
        private ObjectInputStream inStream;
        private int saldo;

        ServerThread(Socket newClientSocket) {
			this.socket = newClientSocket;
            this.username = null;
            this.saldo = 200;
            try {

                outStream = new ObjectOutputStream(socket.getOutputStream());
                inStream = new ObjectInputStream(socket.getInputStream());

            } catch(IOException e) {
                e.printStackTrace();
            }

		}

        public void run() {
            try {

                String user = null;
				String passwd = null;
                //Scanner sc = new Scanner(users);

                try {
					user = (String)inStream.readObject();
					passwd = (String)inStream.readObject();

				} catch (ClassNotFoundException e1) {
                    closeEverything(socket, inStream, outStream);
					e1.printStackTrace();

				}

                //Authentication Handling
                if(userList.size() != 0) {
                    
                    if(userList.containsKey(user)) {

                        if(userList.get(user).equals(passwd)) {

                            username = user;
                            System.out.println("User " + user + " logged in successful");
                            outStream.writeObject("Successful log in");
                            outStream.flush();

                        } else {

                            outStream.writeObject("You introduced the wrong password. Disconnecting!");
                            outStream.flush();
                            closeEverything(socket, inStream, outStream);
                            System.out.println("Client introduced wrong credentials, has been disconnected.");
                            return;

                        }

                    } else {

                        userList.put(user, passwd);
                        username = user;
                        System.out.println("New user " + user + " created");
                        outStream.writeObject("Successful log in");
                        outStream.flush();
                        users.add(this);
                    }

                } else {

                    userList.put(user, passwd);
                    username = user;
                    System.out.println("New user " + user + " created");
                    outStream.writeObject("Successful log in");
                    outStream.flush();
                    users.add(this);
                }
                
                //Listening for msg
                String messageFromClient;
                while(socket.isConnected()) {

                    try {

                        messageFromClient = (String)inStream.readObject();
                        String[] splitMessage = messageFromClient.split(" ", 0); //Partir a mensagem do utilizador. Em splitMessage[0] estara sempre o commando a executar

                        switch(splitMessage[0]) {
                            case "add":
                            case "a":
                                if(splitMessage.length == 3) {
                                    add(splitMessage[1], splitMessage[2]);
                                    System.out.println(username + " has added a new wine to the list");
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("add failed due to wrong number of parameters. Be sure to use add 'wine' 'image'.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }
                                break;          

                            case "sell":
                            case "s":
                                if(splitMessage.length == 4) {
                                    sell(splitMessage[1], Integer.valueOf(splitMessage[2]),  Integer.valueOf(splitMessage[3]));
                                    System.out.println(username + " has put " + splitMessage[1] + " on sale");
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("sell failed due to wrong number of parameters. Be sure to use sell 'wine' 'value' 'quantity'.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }
                                break;   

                            case "view":
                            case "v":
                                if(splitMessage.length == 2) {
                                    view(splitMessage[1]);
                                    System.out.println(username + " has requested to view wine " + splitMessage[1]);
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("view failed due to wrong number of parameters. Be sure to use view 'wine'.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }                              
                                break;

                            case "buy":
                            case "b":
                                if(splitMessage.length == 4) {
                                    buy(splitMessage[1], splitMessage[2], Integer.valueOf(splitMessage[3]));
                                    System.out.println(username + " has bought " + splitMessage[1] + " from " + splitMessage[2]);
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("buy failed due to wrong number of parameters. Be sure to use buy 'wine' 'seller' 'quantity'.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }
                                break;

                            case "wallet":
                            case "w":
                                if(splitMessage.length == 1) {
                                    outStream.writeObject(Integer.toString(this.saldo));
                                    outStream.flush();
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("wallet failed due to wrong number of parameters. Be sure to use wallet.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }  
                                break;

                            case "classify":
                            case "c":
                                if(splitMessage.length == 3) {
                                    classify(splitMessage[1], Integer.valueOf(splitMessage[2]));
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("classify failed due to wrong number of parameters. Be sure to use classify 'wine' 'stars'.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }
                                break;

                            case "talk":
                            case "t":
                                if(splitMessage.length == 3) {
                                    talk(splitMessage[1], this.username, splitMessage[2]);
                                    System.out.println(username + " has sent a messages to " + splitMessage[1]);
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("talk failed due to wrong number of parameters. Be sure to use talk 'user' 'message'.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }
                                break;

                            case "read":
                            case "r":
                                if(splitMessage.length == 1) {
                                    read(this.username);
                                    System.out.println(username + " has been sent his messages");
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("read failed due to wrong number of parameters. Be sure to use read.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }
                                break;

                            

                            default:
                                outStream.writeObject("Please introduce a valid command");
                                outStream.flush();

                        }



                    } catch (ClassNotFoundException e1) {
                        closeEverything(socket, inStream, outStream);
                        e1.printStackTrace();
                    }

                }

            } catch(IOException e) {
                e.printStackTrace();
            }

        }

        public void closeEverything(Socket socket, ObjectInputStream inStream, ObjectOutputStream outStream) {
            try {

                if(inStream != null) {
                    inStream.close();
                }
                if(outStream != null) {
                    outStream.close();
                }
                if(socket != null) {
                    socket.close();
                }

            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        public void read(String username) {
            boolean hadMessage = false;
            ArrayList<String> toDelete = new ArrayList<>();

            for(String message : inbox) {

                String[] toSend = message.split(";", 2);

                if(toSend[0].equals(this.username)) {
                    hadMessage = true;
                    toDelete.add(message);
                    try {

                        outStream.writeObject(toSend[1]);
                        outStream.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                                        
                
            }

            for(String delete : toDelete) {
                 inbox.remove(delete);
            }

            if(!hadMessage) {

                try {

                    outStream.writeObject("You had no messages in the server inbox");
                    outStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }


        public void talk(String reciever, String sender, String message) {
            inbox.add(reciever + ";" + sender + ": " + message);

            try {
                outStream.writeObject("Message succesufuly delivered");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void add(String wineName, String ImgPath){
            TintolmarketWine wine = new TintolmarketWine(wineName, ImgPath);

            if(!wineList.contains(wine)){

                try {

                    wineList.add(wine);
                    outStream.writeObject("Added " + wineName + " to the wine list!");
                    outStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            } else {

                try {

                    outStream.writeObject("This wine already exists");
                    outStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sell(String wine, int value, int quantity){
            for(int i = 0; i <= wineList.size(); i++){
                if((wineList.get(i)).getWinename().equals(wine)){

                    wineList.get(i).setQuantity(username, quantity);
                    wineList.get(i).setValue(username, value);
                    return;
                }
            }
            try {

                outStream.writeObject("This wine does not exist");
                outStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void view(String wine){
            try {

                if(wineExists(wine)) {

                    outStream.writeObject("Wine name: " + wine);
                    outStream.writeObject("Wine image: " + wineList.get(getIndexOfWine(wine)).getPath());
                    int stars = wineList.get(getIndexOfWine(wine)).getClassification();
                    if(stars != 0) {
                        outStream.writeObject("Wine classification: " + wineList.get(getIndexOfWine(wine)).getClassification());
                    }

                    if(wineList.get(getIndexOfWine(wine)).getListofSellers().size() > 0){
                        for(int i = 0; i < wineList.get(getIndexOfWine(wine)).getListofSellers().size(); i++){

                            String seller = wineList.get(getIndexOfWine(wine)).getListofSellers().get(i);
                            outStream.writeObject("Seller " + i +": " +  seller);
                            outStream.writeObject("Quantity: " + wineList.get(getIndexOfWine(wine)).getQuantitySoldBySeller(seller));
                            outStream.writeObject("Value: " + wineList.get(getIndexOfWine(wine)).getValueOfWineSoldBySeller(seller));

                        }
                    }
                } else {

                    outStream.writeObject("The wine you wanted to view doesn't exist");
                    outStream.flush();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void buy(String wine, String seller, int quantity){
            try{
                if(wineExists(wine)){
                    int index = getIndexOfWine(wine);
                    if(wineList.get(index).getQuantitySoldBySeller(seller) >= quantity){
                        int price = wineList.get(getIndexOfWine(wine)).getValueOfWineSoldBySeller(seller) * quantity;
                        if(this.saldo >= price){
                            wineList.get(index).setQuantity(seller, -quantity);
                            this.saldo -= price;
                            for(ServerThread t: users){
                                if(t.username.equals(seller)){
                                    t.saldo += price;
                                }
                            }
                        } else {
                            outStream.writeObject("This wine is not sold by this specified seller!");
                            outStream.flush();
                        }
                    } else {
                        outStream.writeObject("This wine does not exist in this specified quantity!");
                        outStream.flush();
                    }
                } else {
                    outStream.writeObject("This wine does not exist!");
                    outStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        
        public void classify(String wine, int stars){
            if(wineExists(wine)){

                wineList.get(getIndexOfWine(wine)).giveClassification(stars);
                try {

                    outStream.writeObject(wine + " classified with " + stars + " stars.");
                    outStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            } else {
                
                try {

                    outStream.writeObject("This wine does not exist!");
                    outStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        private boolean wineExists(String wine){
            for(TintolmarketWine w: wineList){
                if(w.getWinename().equals(wine)){
                    return true;
                }
            }
            return false;
        }


        private int getIndexOfWine(String wine){
            for(int i = 0; i <= wineList.size(); i++){
                if((wineList.get(i)).getWinename().equals(wine)){
                    return i;
                }
            }
            return -1;
        }
    } //FIM DE SERVER THREAD


    

    
}