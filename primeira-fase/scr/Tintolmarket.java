import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Tintolmarket {

    private Socket cSocket;
    private ObjectOutputStream outStream;
	private ObjectInputStream inStream;
    private BufferedOutputStream out;
    private String user;
    private String password;

    public Tintolmarket(Socket cSocket, String user, String password){
        try {
            this.cSocket = cSocket;
            this.outStream = new ObjectOutputStream(cSocket.getOutputStream());
            this.inStream = new ObjectInputStream(cSocket.getInputStream());
            this.out = new BufferedOutputStream(cSocket.getOutputStream());
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
                String[] temp = command.split(" ");
                if(temp[0].equals("add") || temp[0].equals("a")){
                    outStream.writeObject(command);
                    outStream.flush();  
                    File f = new File(temp[2]);
                    FileInputStream fin = new FileInputStream(f);
                    InputStream input = new BufferedInputStream(fin);
                    outStream.writeObject(f.length());
                    outStream.flush();
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while((bytesRead = input.read(buffer)) != -1){                     
                        out.write(buffer, 0, bytesRead);
                    }
                    input.close();
                    out.flush();
                } else {
                    outStream.writeObject(command);
                    outStream.flush();
                }
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
                    closeClient(cSocket, outStream, inStream, out);
                }
            }

        }).start();
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        String ip = args[0];
        String user = args[1];
        String password;
        if(args.length == 3){
            password = args[2];
        } else {
            System.out.println("Password: ");
            Scanner scanner = new Scanner(System.in);
            password = scanner.nextLine();
            scanner.close();
        }
        Socket cSocket = new Socket(ip, 12345);
        Tintolmarket tintol = new Tintolmarket(cSocket, user, password);
        System.out.println("Connecting...");
        tintol.printMenu();
        tintol.listen();
        tintol.send();
    }


    public void closeClient(Socket cSocket, ObjectOutputStream outStream, ObjectInputStream inStream, BufferedOutputStream out ){
        try {

            if(inStream != null) {
                inStream.close();
            }
            if(outStream != null) {
                outStream.close();
            }
            if(out != null) {
                out.close();
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