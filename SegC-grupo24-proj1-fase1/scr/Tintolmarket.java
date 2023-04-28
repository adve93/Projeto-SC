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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
    private String dataBaseString;
    private KeyStore keystore;
    private KeyStore truststore;
    private String keystorePass;
    /**
     * Tintolmarket Constructor 
     * @param cSocket Client Socket
     * @param user Client Username
     * @param keyStore Client keystore
     * @param keystorePass Client keystore password
     * @param truststore Shared clients truststore
     */
    public Tintolmarket(SSLSocket cSocket, String user, KeyStore keyStore, String keystorePass, KeyStore truststore){
        try {
            this.cSocket = cSocket;
            this.outStream = new ObjectOutputStream(cSocket.getOutputStream());
            this.inStream = new ObjectInputStream(cSocket.getInputStream());
            this.outBuff = new BufferedOutputStream(cSocket.getOutputStream());
            this.inBuff = new BufferedInputStream(cSocket.getInputStream());
            this.user = user;
            this.truststore = truststore;
            this.keystore = keyStore;
            this.keystorePass = keystorePass;
            this.dataBaseString = "..//client"+user+"DataBase//";
            File temp = new File(dataBaseString);
            temp.mkdir();
        } catch (IOException e){
            System.out.println("An error as ocurred!");
            closeClient();
        }
    }

    /** 
     * @param args
     */
    public static void main(String[] args)  {

        try {

            //Processing arguments
            if(args.length == 5) {
                String ip = args[0];
                String trustStorePath = args[1]; 
                String trustPass = "grupo24";
                String keystorePath = args[2];
                String user = args[4];
                String passKeystore = args[3];
                int port = 12345;
                if(ip.contains(":")){
                    String[] ipPort = ip.split(":");
                    ip = ipPort[0];
                    port = Integer.valueOf(ipPort[1]);
                }
                System.out.println("ip: " + ip);
                System.out.println("porto: " + port);
                
                //Setting up keystore and truststore in the properties
                System.setProperty("javax.net.ssl.trustStore", trustStorePath);
                System.setProperty("javax.net.ssl.trustStorePassword", trustPass);
                System.setProperty("javax.net.ssl.keyStore", keystorePath);
                System.setProperty("javax.net.ssl.keyStorePassword", passKeystore);
                
                //Loading keystore
                KeyStore keystoreTemp;
                keystoreTemp = KeyStore.getInstance("JKS");
                keystoreTemp.load(new FileInputStream(keystorePath), passKeystore.toCharArray());   

                //Loading truststore
                KeyStore truststoreTemp;
                truststoreTemp = KeyStore.getInstance("JKS");
                truststoreTemp.load(new FileInputStream(trustStorePath), trustPass.toCharArray()); 
                
                //Connecting to the server
                System.out.println("Connecting...");
                SocketFactory sf = SSLSocketFactory.getDefault( );
                SSLSocket cSocket = (SSLSocket) sf.createSocket(ip , port);

                //Create Tintolmarket object
                Tintolmarket tintol = new Tintolmarket(cSocket, user, keystoreTemp, passKeystore, truststoreTemp);

                //Verify connection
                if(tintol.authenticateToServer()) {

                    //Create threads to send and listen for messages
                    tintol.printMenu();
                    tintol.listen();
                    tintol.send();
                }
                
            } else {
                System.out.println("Introduced the wrong number of arguments! Be sure to read the readMe on how to execute.");
            }

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch(NoSuchElementException e) {
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the processing of user authentication with the server.
     * When the user connects to the server, the server will send a nonce back to the user.
     * The user must then encrypt the nonce with it's private key and send back to the server
     * the original nonce, the encrypted nonce and their certificate.
     * After this the server will respond with authentication succesful or not.
     * 
     * @return if authentication succeded
     */
    public boolean authenticateToServer() {
        try {
            
            System.out.println("Authenticating...");
            
            //Loading private key
            PrivateKey privateKey = (PrivateKey) keystore.getKey("client" + user, keystorePass.toCharArray());

            //Loading certificate
            Certificate[] certificate = keystore.getCertificateChain("client" + user);
            
            //Send the username to the server
            outStream.writeObject(user);
            outStream.flush();

            //Wait for the server response with a nonce
            byte[] nonce = new byte[8];
            inBuff.read(nonce);
            
            //Encrypt the nonce
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptedNonce = cipher.doFinal(nonce);

            //Is registered?
            boolean isRegistered = (boolean)inStream.readObject();
            if(!isRegistered) {

                //Send original nonce, encrypted nonce size, encrypted nonce and certiificate to the server
                outBuff.write(nonce);
                outBuff.flush();
                outStream.writeObject(encryptedNonce.length);
                outStream.flush();
                outBuff.write(encryptedNonce);
                outBuff.flush();
                outStream.writeObject(certificate);
                outStream.flush();

                //Recieve servers response
                String serverResponse = (String)inStream.readObject();
                if(serverResponse.equals("Authentication failed, generated nonce and original nonce don't match, disconnecting!")) {
                    System.out.println(serverResponse);
                    closeClient();
                    return false;
                } else if(serverResponse.equals("Authentication failed, encrypted nonce doesn't match nonce, disconnecting client")) {
                    System.out.println(serverResponse);
                    closeClient();
                    return false;
                } else if(serverResponse.equals("Something has gone wrong, disconnecting client")) {
                    System.out.println("Something went wrong with the authetication process, verify that everysthing you are sending is correct, closing client.");
                    closeClient();
                    return false;
                } else {
                    System.out.println(serverResponse);
                    return true;
                }

            } else {

                //Send encrypted nonce size, encrypted nonce
                outStream.writeObject(encryptedNonce.length);
                outStream.flush();
                outBuff.write(encryptedNonce);
                outBuff.flush();

                //Recieve servers response
                String serverResponse = (String)inStream.readObject();
                if(serverResponse.equals("Authentication failed, generated nonce and original nonce don't match, disconnecting!")) {
                    System.out.println(serverResponse);
                    closeClient();
                    return false;
                } else if(serverResponse.equals("Authentication failed, encrypted nonce doesn't match nonce, disconnecting client")) {
                    System.out.println(serverResponse);
                    closeClient();
                    return false;
                } else if(serverResponse.equals("Something has gone wrong, disconnecting client")) {
                    System.out.println("Something went wrong with the authetication process, verify that everysthing you are sending is correct, closing client.");
                    closeClient();
                    return false;
                } else {
                    System.out.println(serverResponse);
                    return true;
                }

            }

            
            

        //Handling exceptions
        } catch (ClassNotFoundException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | 
                 InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | IOException e) 
        {
            System.out.println("Something has gone wrong, closing client");
            closeClient();
            e.printStackTrace();
            return false;
        }
        
    }

    /**
     * Sends messages to the server
     */
    public void send() {
        try{    

            Scanner sc = new Scanner(System.in);
            while(cSocket.isConnected()){
                String command = sc.nextLine();
                String[] temp = command.split(" ");

                // Send add command
                if(temp[0].equals("add") || temp[0].equals("a")){ 
                    
                    if(temp.length == 3) {

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
                            System.out.println("Verify if the path to the image is correct " + dataBaseString);
                        }

                    } else {

                        System.out.println("add failed due to wrong number of parameters. Be sure to use add 'wine' 'image'.");

                    }
                    

                // Send talk command
                } else if(temp[0].equals("talk") || temp[0].equals("t")) {

                    if(temp.length == 3) {

                        //Copy information
                        String commandTemp = String.copyValueOf(temp[0].toCharArray());
                        String recieverTemp = String.copyValueOf(temp[1].toCharArray());
                        String messageTemp = String.copyValueOf(temp[2].toCharArray());

                        // Get certificate for the given user
                        X509Certificate certificate = (X509Certificate) truststore.getCertificate("client" + recieverTemp);

                        // Get the public key of said user's certificate
                        PublicKey publicKey = certificate.getPublicKey();

                        // Change message from a string to a byte[]
                        byte[] byteMsg = messageTemp.getBytes("UTF-8");

                        // Encrypt message
                        Cipher cipher = Cipher.getInstance("RSA");
                        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                        byte[] byteEncryptedMsg = cipher.doFinal(byteMsg);

                        // Send the command to server
                        outStream.writeObject(commandTemp + " " + recieverTemp);
                        outStream.flush(); 

                        // Send the encrypted message size to server
                        outStream.writeObject(byteEncryptedMsg.length);
                        outStream.flush(); 

                        // Send the encrypted message to server
                        outBuff.write(byteEncryptedMsg);
                        outBuff.flush(); 

                    } else {

                        System.out.println("talk failed due to wrong number of parameters. Be sure to use talk 'user' 'message'.");

                    }

                // Send other commands
                }else {
                    outStream.writeObject(command);
                    outStream.flush();
                }
            }
            sc.close();

        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | 
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e){
                    closeClient();
        } catch (KeyStoreException e) {
            System.out.println("Tried to send a message to a user not registered in the server.");
        } catch (NullPointerException e) {
            System.out.println("Tried to send a message to a user not registered in the server.");
            send();
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
                            } else if(messageFromServer.equals("readFlag")) {

                                // Saves the message, with format UsersplitEncodedBytes locally
                                String msg = (String)inStream.readObject();
                                String[] splitMsg = msg.split("split");

                                // Decode the bytes
                                byte[] byteMsg = Base64.getDecoder().decode(splitMsg[1]);

                                // Loading private key
                                PrivateKey privateKey = (PrivateKey) keystore.getKey("client" + user, keystorePass.toCharArray());

                                // Decrypts the msg with private key
                                Cipher cipher = Cipher.getInstance("RSA");
                                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                                byte[] byteDecryptedMsg = cipher.doFinal(byteMsg);

                                // Change message back from byte[] to a string
                                String decryptedMsg = new String(byteDecryptedMsg, "UTF-8");

                                //Print out decrypted message and the sender's name
                                System.out.println(splitMsg[0] + ": " + decryptedMsg);


                            } else{
                                System.out.println(messageFromServer);
                            }
                            
                        } catch (ClassNotFoundException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException |
                                 NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e1) {
                            closeClient();
                            e1.printStackTrace();
                        }


                    }
                } catch (IOException e){
                    closeClient();
                }
            }

        }).start();
    }


    /**
     * Closes the client. Can be used when unexpected exceptions occur. 
     * 
     */
    public void closeClient(){
        try {

            if(this.inStream != null) {
                this.inStream.close();
            }
            if(this.outStream != null) {
                this.outStream.close();
            }
            if(this.outBuff != null) {
                this.outBuff.close();
            }
            if(this.cSocket != null) {
                this.cSocket.close();
            }
            
            if(this.inBuff != null) {
                this.inBuff.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the menu which contains the options for the client to use
     */
    public void printMenu(){
        System.out.println("Choose an action to perform: ");
        System.out.println("add; sell; view; buy; wallet; classify; talk; read; list");
        System.out.println("All actions can be called by using the first letter of the name.");
    }
}