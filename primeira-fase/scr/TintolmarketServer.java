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
import java.io.File;
import java.io.FileNotFoundException;

public class TintolmarketServer {

    private HashMap<String,String> userList;
    private ServerSocket sSocket;
    private PrintWriter users;
    private ObjectOutputStream outStream;
	private ObjectInputStream inStream;

    public TintolmarketServer(ServerSocket sSocket) {
        this.sSocket = sSocket;
        this.userList = new HashMap<String,String>();

        try {
            this.users = new PrintWriter("users.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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

                try {
                    /* 
                    outStream.writeChars("Please introduce your username and password!");
                    outStream.writeChars("\n");
                    outStream.flush();
                    */
					user = (String)inStream.readObject();
					passwd = (String)inStream.readObject();
                    System.out.println("User: "  +  user);
                    System.out.println("pass: " + passwd);

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
                            outStream.writeChars("Successful log in");
                            outStream.flush();

                        } else {

                            outStream.writeChars("You introduced the wrong password. Disconnecting!");
                            outStream.flush();
                            closeEverything(socket, inStream, outStream);
                            return;

                        }

                    } else {

                        userList.put(user, passwd);
                        System.out.println("New user" + user + "created");
                        outStream.writeChars("Successful log in");
                        outStream.flush();

                    }

                } else {

                    userList.put(user, passwd);
                    System.out.println("New user" + user + "created");
                    outStream.writeChars("Successful log in");
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