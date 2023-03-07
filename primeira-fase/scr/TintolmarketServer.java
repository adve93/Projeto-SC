import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.ObjectOutputStream.PutField;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

public class TintolmarketServer {

    private HashMap<String,String> userList;
    private ServerSocket sSocket;
    //private File users;
    //private FileWriter writer;
    //private PrintWriter pw;
    private ObjectOutputStream outStream;
	private ObjectInputStream inStream;

    public TintolmarketServer(ServerSocket sSocket) {
        this.sSocket = sSocket;
        this.userList = new HashMap<String,String>();
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

        ServerThread(Socket newClientSocket) {
			this.socket = newClientSocket;
		}

        public void run() {
            try {

                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
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

                System.out.println(user);
                System.out.println(passwd);

                //Authentication Handling
                if(userList.size() != 0) {
                    
                    if(userList.containsKey(user)) {

                        if(userList.get(user) == passwd) {

                            System.out.println("User" + user + "logged in successful");
                            outStream.writeObject("Successful log in");
                            outStream.flush();

                        } else {

                            outStream.writeObject("You introduced the wrong password. Disconnecting!");
                            outStream.flush();
                            closeEverything(socket, inStream, outStream);
                            return;

                        }

                    } else {

                        userList.put(user, passwd);
                        System.out.println("New user " + user + " created");
                        outStream.writeObject("Successful log in");
                        outStream.flush();

                    }

                } else {

                    userList.put(user, passwd);
                    System.out.println("New user " + user + " created");
                    outStream.writeObject("Successful log in");
                    outStream.flush();
                    
                }
                
                //Listening for msg
                String messageFromClient;
                while(socket.isConnected()) {

                    try {

                        messageFromClient = (String)inStream.readObject();
                        System.out.println(messageFromClient);

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
    }

}