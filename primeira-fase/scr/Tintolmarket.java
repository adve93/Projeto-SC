import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//import java.io.ObjectOutputStream.PutField;
//import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

//import javax.sound.sampled.FloatControl;

public class Tintolmarket {

    private Socket cSocket;
    private ObjectOutputStream outStream;
	private ObjectInputStream inStream;
    private String user;
    private String password;
    //private float saldo; Tirar comment quando se for usar

    public Tintolmarket(Socket cSocket, String user, String password){
        try {
            this.cSocket = cSocket;
            this.outStream = new ObjectOutputStream(cSocket.getOutputStream());
            this.inStream = new ObjectInputStream(cSocket.getInputStream());
            this.user = user;
            this.password = password;
        } catch (IOException e){
            System.out.println("An error as ocurred!");
            closeClient(cSocket, outStream, inStream);
        }
    }

    public void send() {
        try{

            outStream.writeObject(user);
            outStream.writeObject(password);
            outStream.flush();

            Scanner sc = new Scanner(System.in);
            while(cSocket.isConnected()){
                String command = sc.nextLine();
                outStream.writeObject(command);
                outStream.flush();
            }
            sc.close();

        } catch (IOException e){
            closeClient(cSocket, outStream, inStream);
        }
    }

    public void listen() {
        new Thread(new Runnable() {
            @ Override

            public void run() {
                try{

                    String messageFromServer;
                    while(cSocket.isConnected()) {
                    
                        try {

                            messageFromServer = (String)inStream.readObject();
                            System.out.println(messageFromServer);

                        } catch (ClassNotFoundException e1) {
                            closeClient(cSocket, outStream, inStream);
                            e1.printStackTrace();
                        }


                    }
                } catch (IOException e){
                    closeClient(cSocket, outStream, inStream);
                }
            }

        }).start();
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
<<<<<<< Updated upstream
        String ip = args[0];
        String user = args[1];
        String password = args[2];
        Socket cSocket = new Socket(ip, 12345);
        Tintolmarket tintol = new Tintolmarket(cSocket, user, password);
        System.out.println("Connecting...");
        tintol.printMenu();
        tintol.listen();
        tintol.send();
=======

        if(args.length == 3) {
            String ip = args[0];
            String user = args[1];
            String password;
            int port = 12345;
            password = args[2];
            if(ip.contains(":")){
                String[] ipPort = ip.split(":");
                ip = ipPort[0];
                port = Integer.valueOf(ipPort[1]);
            }
            System.out.println("ip: " + ip);
            System.out.println("porto: " + port);
            Socket cSocket = new Socket(ip, port);
            Tintolmarket tintol = new Tintolmarket(cSocket, user, password);
            System.out.println("Connecting...");
            tintol.printMenu();
            tintol.listen();
            tintol.send();
        } else {
            System.out.println("Introduced the wrong number of arguments! Be sure to read the readMe file on how to execute the client!");
        }
>>>>>>> Stashed changes
    }


    public void closeClient(Socket cSocket, ObjectOutputStream outStream, ObjectInputStream inStream ){
        try {

            if(inStream != null) {
                inStream.close();
            }
            if(outStream != null) {
                outStream.close();
            }
            if(cSocket != null) {
                cSocket.close();
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void printMenu(){
        System.out.println("Choose an action to perform: ");
        System.out.println("add; sell; view; buy; wallet; classify; talk; read");
        System.out.println("All actions can be called by using the first letter of the name.");
    }
}