import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Scanner;

import javax.sound.sampled.FloatControl;

public class Tintolmarket {

    private Socket cSocket;
    private ObjectOutputStream outStream;
	private ObjectInputStream inStream;
    private String user;
    private String password;
    private float saldo;

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

    public void userHandler(){
        try{
            outStream.writeChars(user);
            outStream.writeChars(password);

            Scanner sc = new Scanner(System.in);
            while(cSocket.isConnected()){
                System.out.println("comadno");
                String command = sc.nextLine();
                outStream.writeChars(command);
            }
            sc.close();
        } catch (IOException e){
            closeClient(cSocket, outStream, inStream);
        }

    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        String ip = args[0];
        String user = args[1];
        String password = args[2];
        Socket cSocket = new Socket(ip, 12345);
        Tintolmarket tintol = new Tintolmarket(cSocket, user, password);
        System.out.println("Connecting...");
        tintol.userHandler();
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
}