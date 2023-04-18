/**
 * Projeto de Segurança e Confiabilidade - Fase 1 2023
 * @author Francisco Teixeira | FC56305
 * @author Afonso Soares | FC56314
 * @author Gonçalo Correia | FC56316
 */

//Imports for the project 
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * TintolmarketServer - Responsible for the Server side of the Tintol Market,
 * handles most of the operations between clients.
 * 
 */
public class TintolmarketServer {

    private HashMap<String,String> userList;
    private HashMap<String,Integer> userSaldo;
    private ArrayList<String> usernames;
    private ServerSocket sSocket;
    private BufferedWriter writer;
    private ArrayList<TintolmarketWine> wineList;
    private ArrayList<String> inbox;
    private ArrayList<ServerThread> users;

    /**
     * TintolmarketServer constructor
     * @param sSocket The server socket
     */
    public TintolmarketServer(ServerSocket sSocket) {
        this.sSocket = sSocket;
        this.userList = new HashMap<String,String>();
        this.userSaldo = new HashMap<String,Integer>();
        this.wineList = new ArrayList<>();
        this.inbox = new ArrayList<>();  
        this.users = new ArrayList<>();
        this.usernames = new ArrayList<>();
        this.writer = null;

    }

    public static void main(String[] args) {
        if(args.length == 4) {
            String passwordCifra = args[1];
            String keystorePath = args[2];
            String passKeystore = args[3];
            ServerSocket sSocket = null;
            int port = 12345;
            try {
                if(args.length == 1){port = Integer.valueOf(args[0]);} 
                System.out.println("Porto: " + port);

                sSocket = new ServerSocket(port);

            } catch (IOException e) {
                
                System.err.println(e.getMessage());
                System.exit(-1);
            }
            TintolmarketServer server = new TintolmarketServer(sSocket);
            server.loadData();
            try {
                server.writer = new BufferedWriter(new FileWriter("..//serverBase//users.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            } 
            server.startServer();
        } else {
            System.out.println("Introduced the wrong number of arguments! Be sure to read the readMe on how to execute.");
        }
	}

    /**
     * Starts the server and awaits new clients to connect to it
     */
    public void startServer() {
        try {

            while(!sSocket.isClosed()) {

                Socket newClientSocket = sSocket.accept();
                System.out.println("A new client is connecting. Awaiting authentication.");
                ServerThread newServerThread = new ServerThread(newClientSocket);
                newServerThread.start();

            }
            writer.close();
        } catch(IOException e) {
            try {
               
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println("entrou");
            e.printStackTrace();
        }

    }

    /**
     * Loads the data from a previous iteration of the program onto the respective objects
     */
    public void loadData(){
            try {
                BufferedReader reader = new BufferedReader(new FileReader("..//serverBase//users.txt"));
                String line;
                while((line = reader.readLine()) != null){
                    String[] data = line.split(" ");
                    usernames.add(data[0]);
                    userList.put(data[0], data[1]);
                }
                reader.close();

                reader = new BufferedReader(new FileReader("..//serverBase//usersSaldo.txt"));
                while((line = reader.readLine()) != null){
                    String[] data = line.split(" ");
                    userSaldo.put(data[0], Integer.valueOf(data[1]));
                }
                reader.close();

                reader = new BufferedReader(new FileReader("..//serverBase//wines.txt"));
                while((line = reader.readLine()) != null){
                    String[] data = line.split(" ");
                    TintolmarketWine wine = new TintolmarketWine(data[0], data[1]);
                    wine.setFinalClassification(Integer.valueOf(data[3]));
                    int wineSellers = Integer.valueOf(data[2]);
                    while(wineSellers > 0) {
                        line = reader.readLine();
                        data = line.split(" ");
                        wine.setQuantity(data[0], Integer.valueOf(data[2]));
                        wine.setValue(data[0], Integer.valueOf(data[1]));
                        wineSellers--;
                    }
                    wineList.add(wine);

                }
                reader.close();

                reader = new BufferedReader(new FileReader("..//serverBase//inbox.txt"));
                while((line = reader.readLine()) != null){
                    inbox.add(line);

                }
                reader.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
    }

    /**
     * Each ServerThread is responsible for a single client and all the operations related to that client, extends class Thread 
     */
    class ServerThread extends Thread {
        private Socket socket = null;
        private String username; //Identificador da thread.
        private ObjectOutputStream outStream; //outStream e inStream passados para variaveis globais de serverThread para podermos criar metodos como read e talk
        private ObjectInputStream inStream;
        private BufferedInputStream inBuff;
        private BufferedOutputStream outBuff;
        private int saldo;

        /**
         * ServerThread constructor
         * @param newClientSocket the client socket
         */
        ServerThread(Socket newClientSocket) {
			this.socket = newClientSocket;
            this.username = null;
            this.saldo = 200;
            try {

                this.outStream = new ObjectOutputStream(socket.getOutputStream());
                this.inStream = new ObjectInputStream(socket.getInputStream());
                this.inBuff = new BufferedInputStream(socket.getInputStream());
                this.outBuff = new BufferedOutputStream(socket.getOutputStream());

            } catch(IOException e) {
                e.printStackTrace();
            }

		}

        public void run() {
            try {

                String user = null;
				String passwd = null;

                try {
					user = (String)inStream.readObject();
					passwd = (String)inStream.readObject();
                    if(userSaldo.containsKey(user)) {
                        this.saldo = userSaldo.get(user);
                    }

				} catch (ClassNotFoundException e1) {
                    closeEverything(socket, inStream, outStream);
					e1.printStackTrace();

				}

                //Authentication Handling
                if(userList.size() != 0) {
                    
                    if(userList.containsKey(user)) {

                        if(userList.get(user).equals(passwd)) {                     
                            username = user;
                            System.out.println("User " + user + " logged in successful.");
                            
                            outStream.writeObject("Successful log in.");
                            outStream.flush();
                            writeUsers();
                            writeSaldo();

                        } else {

                            outStream.writeObject("You introduced the wrong password. Disconnecting!");
                            outStream.flush();
                            closeEverything(socket, inStream, outStream);
                            System.out.println("Client introduced wrong credentials, has been disconnected.");
                            return;

                        }

                    } else {

                        usernames.add(user);
                        userList.put(user, passwd);
                        userSaldo.put(user, this.saldo);
                        username = user;
                        System.out.println("New user " + user + " created.");
                        outStream.writeObject("Successful log in.");
                        outStream.flush();
                        users.add(this);
                        writeUsers();
                        writeSaldo();
                    }

                } else {
                    usernames.add(user);
                    userList.put(user, passwd);
                    userSaldo.put(user, this.saldo);
                    username = user;
                    System.out.println("New user " + user + " created.");
                    outStream.writeObject("Successful log in.");
                    outStream.flush();
                    users.add(this);
                    writeUsers();
                    writeSaldo();
                }
                
               
                //Listening for a message 
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
                                    System.out.println(username + " has been sent his messages.");
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("read failed due to wrong number of parameters. Be sure to use read.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }
                                break;

                            

                            default:
                                outStream.writeObject("Please introduce a valid command.");
                                outStream.flush();

                        }



                    } catch (ClassNotFoundException e1) {
                        closeEverything(socket, inStream, outStream);
                        e1.printStackTrace();
                    }

                }

            } catch(IOException e) {
                writeSaldo();
                System.out.println("Client " + username + " has disconnected form TintolMarket.");
            }

        }

        /**
         * Writes user related data onto a txt file 
         */
        public void writeUsers() {
            try {
                writer = new BufferedWriter(new FileWriter("..//serverBase//users.txt"));
                for(String tag : usernames) {
                    writer.write(tag + " " + userList.get(tag) + "\n");
                    writer.flush();
                }
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Writes inbox related data onto a txt file 
         */
        public void writeInbox() {
            try {
                writer = new BufferedWriter(new FileWriter("..//serverBase//inbox.txt"));
                for(String message : inbox) {
                    writer.write(message + "\n");
                    writer.flush();
                }
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Writes the users wallet onto a txt file
         */
        public void writeSaldo() {
            try {
                writer = new BufferedWriter(new FileWriter("..//serverBase//usersSaldo.txt"));
                for(String tag : usernames) {
                    writer.write(tag + " " + userSaldo.get(tag) +"\n");
                    writer.flush();
                }
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Writes current wines in the store and witch user is selling witch wine onto a txt file
         */
        public void writeWine() {
            try {
                writer = new BufferedWriter(new FileWriter("..//serverBase//wines.txt"));
                for(TintolmarketWine wine : wineList) {
                    writer.write(wine.getWinename() + " " + wine.getPath() + " " + wine.getListofSellers().size() + " " + wine.getClassification() + "\n");
                    writer.flush();
                    if(wine.getListofSellers().size() > 0) {
                        for(String seller: wine.getListofSellers()) {
                            writer.write(seller + " " + wine.getValueOfWineSoldBySeller(seller) + " " + wine.getQuantitySoldBySeller(seller) + "\n");
                            writer.flush();
                        }
                    }

                }
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Closes the ServerThread
         * @param socket the client socket
         * @param inStream the input stream
         * @param outStream the output stream
         */
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

        /**
         * Reads and sends to the user every message they had in their inbox
         * @param username
         */
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

            writeInbox();
            if(!hadMessage) {

                try {

                    outStream.writeObject("You had no messages in the server inbox.");
                    outStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        /**
         * "sender" adds a "message" to the inbox array specified for the "reciever"
         * @param reciever username of the user that will recieve the message
         * @param sender username of the user that will send the message
         * @param message message to be sent
         */
        public void talk(String reciever, String sender, String message) {
            for(ServerThread tr : users) {
                if(tr.username.equals(reciever)) {
                    System.out.println(username + " has sent a messages to " + reciever + ".");
                    inbox.add(reciever + ";" + sender + ": " + message);
                    writeInbox();

                    try {
                    outStream.writeObject("Message succesufuly delivered.");
                    } catch (IOException e) {
                    e.printStackTrace();
                    }
                    return;
                }
            } 
            System.out.println(username + " tried to send a messages to a user that does not exist.");

            try {
                outStream.writeObject("Introduced a user that does not exist.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /**
         * Adds a new wint to the store
         * @param wineName The name of the wine
         * @param ImgPath The name of the wine image
         */
        public void add(String wineName, String ImgPath){
            String path  = "..//serverBase//"+ImgPath;
            TintolmarketWine wine = new TintolmarketWine(wineName, path);

            if(!wineList.contains(wine)){
                try {
                    long fileSize = (long) inStream.readObject();
                    File f = new File(path);
                    FileOutputStream fout = new FileOutputStream(f);
                    OutputStream output = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while(fileSize > 0){
                        bytesRead = inBuff.read(buffer);
                        output.write(buffer, 0, bytesRead);
                        output.flush();
                        fileSize -= bytesRead;
                    }
                    output.close();
                    wineList.add(wine);
                    writeWine();
                    System.out.println(this.username + " has added a new wine to the list.");
                    outStream.writeObject("Added " + wineName + " to the wine list!");
                    outStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                
            } else {

                try {

                    System.out.println(this.username + " tried adding an existing wine to the list.");
                    outStream.writeObject("This wine already exists.");
                    outStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Puts a certain quantity to sale of the specified wine for the specified value
         * @param wine
         * @param value
         * @param quantity
         */
        public void sell(String wine, int value, int quantity){
            if(wineExists(wine)) {
                for(int i = 0; i <= wineList.size(); i++){
                    if((wineList.get(i)).getWinename().equals(wine)){

                        wineList.get(i).setQuantity(username, quantity);
                        wineList.get(i).setValue(username, value);
                        System.out.println(this.username + " has put " + wine + " on sale!");
                        try {
                            
                            outStream.writeObject("You have put " + quantity + " copies of " + wine + " on sale for " + value + "!");
                            outStream.flush();
                            writeWine();
        
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }

            } else {
                try {

                    System.out.println(this.username + " has tried  to sell a wine that does not exist.");
                    outStream.writeObject("You are trying to sell a wine that does not exist.");
                    outStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Views the information related to the specified wine
         * @param wine Name of the wine
         */
        public void view(String wine){
            try {

                if(wineExists(wine)) {
                    TintolmarketWine temp = wineList.get(getIndexOfWine(wine));
                    String path = temp.getPath();

                    outStream.writeObject("Wine name: " + wine);
                    outStream.flush();
                    outStream.writeObject("Wine image: " + path);
                    outStream.flush();
                    outStream.writeObject("start");
                    outStream.flush();
                    String[] img = path.split("//"); //(..//serverbase//nome)
                    outStream.writeObject(img[2]);
                    outStream.flush();

                    File f = new File(path);
                    FileInputStream fin = new FileInputStream(f);
                    InputStream input = new BufferedInputStream(fin);
                    outStream.writeObject(f.length());
                    outStream.flush();
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    
                    while((bytesRead = input.read(buffer)) != -1){                     
                        outBuff.write(buffer, 0, bytesRead);
                        outBuff.flush();
                    }
                    input.close();

                    int stars = temp.getClassification();
                    if(stars != 0) {
                        outStream.writeObject("Wine classification: " + temp.getClassification());
                        outStream.flush();
                    }

                    if(temp.getListofSellers().size() > 0){
                        for(int i = 0; i < temp.getListofSellers().size(); i++){

                            String seller = temp.getListofSellers().get(i);
                            outStream.writeObject("Seller " + i +": " +  seller);
                            outStream.flush();
                            outStream.writeObject("Quantity: " + temp.getQuantitySoldBySeller(seller));
                            outStream.flush();
                            outStream.writeObject("Value: " + temp.getValueOfWineSoldBySeller(seller));
                            outStream.flush();

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

        /**
         * Buys a specified quantity of a wine from a seller
         * @param wine The wine to be bought 
         * @param seller The seller from witch to buy
         * @param quantity The quantity of wine to buy
         */
        public void buy(String wine, String seller, int quantity){
            try{
                if(wineExists(wine)){
                    int index = getIndexOfWine(wine);
                    if(wineList.get(index).getQuantitySoldBySeller(seller) >= quantity){
                        int price = wineList.get(getIndexOfWine(wine)).getValueOfWineSoldBySeller(seller) * quantity;
                        if(this.saldo >= price){
                            System.out.println(this.username + " has bought " + wine + " from " + seller +  ".");
                            wineList.get(index).setQuantity(seller, -quantity);
                            this.saldo -= price;
                            userSaldo.put(this.username, this.saldo);
                            for(ServerThread t: users){
                                if(t.username.equals(seller)){                                
                                    t.saldo += price;
                                    userSaldo.put(t.username, t.saldo);
                                    writeSaldo();
                                    writeWine();
                                }
                            }
                            outStream.writeObject("You have bought " + wine + "from " + seller + "!");
                            outStream.flush();
                            outStream.writeObject("You have " + this.saldo + "euros remaining!");
                            outStream.flush();
                        } else {
                            System.out.println(this.username + "tried to buy a wine that is not sold by " + seller + ".");
                            outStream.writeObject("This wine is not sold by this specified seller!");
                            outStream.flush();
                        }
                    } else {
                        System.out.println(this.username + "tried to buy a wine that does not exist in the specified quantity.");
                        outStream.writeObject("This wine does not exist in this specified quantity!");
                        outStream.flush();
                    }
                } else {
                    System.out.println(this.username + "tried to buy a wine that does not exist.");
                    outStream.writeObject("This wine does not exist!");
                    outStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Classifys the wine from 1 to 5
         * @param wine The wine to classify
         * @param stars The number of stars to give
         */
        public void classify(String wine, int stars){
            int res = 0;
            if(stars < 0 || stars > 5) {
                try {
                    outStream.writeObject("You can only give from 0 to 5 stars.");
                    outStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if(wineExists(wine)){

                res = wineList.get(getIndexOfWine(wine)).giveClassification(stars, this.username);
                try {
                    if(res == 1) {
                        

                            outStream.writeObject(wine + " classified with " + stars + " stars.");
                            outStream.flush();
                            writeWine();
                  
                    } else {

                            outStream.writeObject(wine + " has already been classified before. You cannot classify the same wine twice.");
                            outStream.flush();
          
                    }
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


        /**
         * Check if a wine exists
         * @param wine The name of the wine
         * @return A boolean that says if the wine exists
         */
        private boolean wineExists(String wine){
            for(TintolmarketWine w: wineList){
                if(w.getWinename().equals(wine)){
                    return true;
                }
            }
            return false;
        }

        /**
         * Gets the index of the wine specified by its name in the List of TintolmarketWines
         * @param wine
         * @return
         */
        private int getIndexOfWine(String wine){
            for(int i = 0; i <= wineList.size(); i++){
                if((wineList.get(i)).getWinename().equals(wine)){
                    return i;
                }
            }
            return -1;
        }
    } //End of server thread

}