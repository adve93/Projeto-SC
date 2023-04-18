/**
 * Projeto de Segurança e Confiabilidade - Fase 1 2023
 * @author Francisco Teixeira | FC56305
 * @author Afonso Soares | FC56314
 * @author Gonçalo Correia | FC56316
 */

//Imports for the project 
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
import java.security.KeyStore;
import java.util.Scanner;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Tintolmarket - Responsible for the Client side of the Tintol Market,
 * sends and recives messages from the server
 */
public class Tintolmarket {

    private SSLSocket cSocket;
    private ObjectOutputStream outStream;
	private ObjectInputStream inStream;
    private BufferedOutputStream outBuff;
    private BufferedInputStream inBuff;
    private String user;
    private String password;
    private String dataBaseString;
    private KeyStore truststore; //Um bocado unsure de o que é o truststore ainda, mas acho que é um ficheiro onde guardamos pares CHAVE PRIVADA-CHAVE PUBLICA
                                               //Vai ser preciso então ler o ficheiro no inicio do cliente e adicionar aqui a sua informação.
    private KeyStore keystore; //Mesma coisa que trustScore?
    /**
     * Tintolmarket Constructor 
     * @param cSocket Client Socket
     * @param user Client Username
     * @param password Client Password
     */
    public Tintolmarket(SSLSocket cSocket, String user, String password){
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
            closeClient(cSocket, outStream, inStream, outBuff, inBuff);
        }
    }

    /**
     * Sends messages to the server
     */
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
            closeClient(cSocket, outStream, inStream, outBuff, inBuff);
        }
    }

    /**
     * Listens to messages from the server
     */
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
                            closeClient(cSocket, outStream, inStream, outBuff, inBuff);
                            e1.printStackTrace();
                        }


                    }
                } catch (IOException e){
                    closeClient(cSocket, outStream, inStream, outBuff, inBuff);
                }
            }

        }).start();
    }

    /** 
     * @param args
     * @throws UnknownHostException
     * @throws IOException
     */
    public static void main(String[] args) throws UnknownHostException, IOException {
        if(args.length == 5) {
            String ip = args[0];
            String trustStorePath = args[1]; //é necessario ir agora a este path e ler o ficheiro
            String keystorePath = args[2]; //Mesma coisa que truststore???
            String user = args[4];
            String password = args[3];
            int port = 12345;
            if(ip.contains(":")){
                String[] ipPort = ip.split(":");
                ip = ipPort[0];
                port = Integer.valueOf(ipPort[1]);
            }
            System.out.println("ip: " + ip);
            System.out.println("porto: " + port);
            SocketFactory sf = SSLSocketFactory.getDefault( );
            SSLSocket cSocket = (SSLSocket) sf.createSocket(args[0], Integer.parseInt(args[1]));
            Tintolmarket tintol = new Tintolmarket(cSocket, user, password);
            System.out.println("Connecting...");
            tintol.printMenu();
            tintol.listen();
            tintol.send();
        } else {
            System.out.println("Introduced the wrong number of arguments! Be sure to read the readMe on how to execute.");
        }
    }

    /**
     * Closes the client 
     * @param cSocket Client socket
     * @param outStream output stream
     * @param inStream input stream
     * @param outBuff output buffer
     * @param inBuff input bufer
     */
    public void closeClient(Socket cSocket, ObjectOutputStream outStream, ObjectInputStream inStream, BufferedOutputStream outBuff, BufferedInputStream inBuff){
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
            
            if(inBuff != null) {
                inBuff.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the menu wich contains the options for the client to use
     */
    public void printMenu(){
        System.out.println("Choose an action to perform: ");
        System.out.println("add; sell; view; buy; wallet; classify; talk; read");
        System.out.println("All actions can be called by using the first letter of the name.");
    }
}