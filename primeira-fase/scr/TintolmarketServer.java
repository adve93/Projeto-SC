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

    public TintolmarketServer(ServerSocket sSocket) {
        this.sSocket = sSocket;
        this.userList = new HashMap<String,String>();
        this.wineList = new ArrayList<>();
        this.inbox = new ArrayList<>();
        //this.users = new File("out.txt");
        /* 
        try {
            this.writer = new FileWriter(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.pw = new PrintWriter(writer);
        try {
            users.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }  
        */    
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

        ServerThread(Socket newClientSocket) {
			this.socket = newClientSocket;
            this.username = null;
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

                    }

                } else {

                    userList.put(user, passwd);
                    username = user;
                    System.out.println("New user " + user + " created");
                    outStream.writeObject("Successful log in");
                    outStream.flush();
                    
                }
                
                //Listening for msg
                String messageFromClient;
                while(socket.isConnected()) {

                    try {

                        messageFromClient = (String)inStream.readObject();
                        String[] splitMessage = messageFromClient.split(" ", 0); //Partir a mensagem do utilizador. Em splitMessage[0] estara sempre o commando a executar

                        switch(splitMessage[0]) {

                            case "read":
                                read(this.username);
                                System.out.println(username + " has been sent his messages");
                                break;

                            case "talk":
                                talk(splitMessage[1], this.username, splitMessage[2]);
                                System.out.println(username + " has sent a messages to " + splitMessage[1]);
                                break;

                            case "r":
                                read(this.username);
                                System.out.println(username + " has been sent his messages");
                                break;

                            case "t":
                                talk(splitMessage[1], this.username, splitMessage[2]);
                                System.out.println(username + " has sent a messages to " + splitMessage[1]);
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

        public void add(String wineName, String user, String ImgPath){
            TintolmarketWine wine = new TintolmarketWine(wineName, user, ImgPath);
            if(!wineList.contains(wine)){
                wineList.add(wine);
            } else {
                try {
                    outStream.writeObject("This wine already exists");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sell(String wine, float value, int quantity){
            for(int i = 0; i <= wineList.size(); i++){
                if((wineList.get(i)).getWinename().equals(wine)){
                    wineList.get(i).setQuantity(quantity);
                    wineList.get(i).setValue(value);
                    return;
                }
            }
            try {
                outStream.writeObject("This wine does not exist");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } //FIM DE SERVER THREAD


    

    
}