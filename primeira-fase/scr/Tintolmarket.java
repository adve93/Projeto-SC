import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Tintolmarket {

    private Socket cSocket;
    private ObjectOutputStream outStream;
	private ObjectInputStream inStream;
    private BufferedOutputStream outBuff;
    private BufferedInputStream inBuff;
    private String user;
    private String password;
    private String dataBaseString;

    public Tintolmarket(Socket cSocket, String user, String password){
        try {
            this.cSocket = cSocket;
            this.outStream = new ObjectOutputStream(cSocket.getOutputStream());
            this.inStream = new ObjectInputStream(cSocket.getInputStream());
            this.outBuff = new BufferedOutputStream(cSocket.getOutputStream());
            this.inBuff = new BufferedInputStream(cSocket.getInputStream());
            this.user = user;
            this.password = password;
            this.dataBaseString = "..//client"+user+"DataBase//";
            File temp = new File(dataBaseString);
            temp.mkdir();
        } catch (IOException e){
            System.out.println("An error as ocurred!");
            closeClient(cSocket, outStream, inStream, outBuff);
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
                    File f = new File(dataBaseString+temp[2]);
                    if(f.exists() && !f.isDirectory()) { 
                        outStream.writeObject(command);
                        outStream.flush(); 
                        FileInputStream fin = new FileInputStream(f);
                        InputStream input = new BufferedInputStream(fin);
                        outStream.writeObject(f.length());
                        outStream.flush();
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        while((bytesRead = input.read(buffer)) != -1){                     
                            outBuff.write(buffer, 0, bytesRead);
                        }
                        input.close();
                        outBuff.flush();
                    } else {
                        System.out.println("Certifiquese que a imagem que desenha mandar se encontra em " + dataBaseString);
                    }
                } else {
                    outStream.writeObject(command);
                    outStream.flush();
                }
            }
            sc.close();

        } catch (IOException e){
            closeClient(cSocket, outStream, inStream, outBuff);
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
                            if(messageFromServer.contains("start")){
                                File f = new File(dataBaseString+(String)inStream.readObject());
                                long fileSize = (long) inStream.readObject();
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
                            } else{
                                System.out.println(messageFromServer);
                            }
                            
                        } catch (ClassNotFoundException e1) {
                            closeClient(cSocket, outStream, inStream, outBuff);
                            e1.printStackTrace();
                        }


                    }
                } catch (IOException e){
                    closeClient(cSocket, outStream, inStream, outBuff);
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


    public void closeClient(Socket cSocket, ObjectOutputStream outStream, ObjectInputStream inStream, BufferedOutputStream outBuff ){
        try {

            if(inStream != null) {
                inStream.close();
            }
            if(outStream != null) {
                outStream.close();
            }
            if(outBuff != null) {
                outBuff.close();
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