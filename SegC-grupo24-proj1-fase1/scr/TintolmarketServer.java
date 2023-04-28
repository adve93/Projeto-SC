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
import javax.crypto.spec.PBEKeySpec;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
    private SSLServerSocket sSocket;
    private BufferedWriter writer;
    private BufferedWriter writer2;
    private ArrayList<TintolmarketWine> wineList;
    private ArrayList<String> inbox;
    private ArrayList<SSLSimpleServer> users;
    private String passwordCipher;
    private blockchain blockchain;

    /**
     * TintolmarketServer constructor
     * @param sSocket The server socket
     */
    public TintolmarketServer(SSLServerSocket sSocket, String passwordCipher) {
        this.sSocket = sSocket;
        this.userList = new HashMap<String,String>();
        this.userSaldo = new HashMap<String,Integer>();
        this.wineList = new ArrayList<>();
        this.inbox = new ArrayList<>();  
        this.users = new ArrayList<>();
        this.usernames = new ArrayList<>();
        this.writer = null;
        this.writer2 = null;
        this.passwordCipher = passwordCipher;
        this.blockchain = new blockchain(5);

    }

    /** 
     * @param args
     */
    public static void main(String[] args) {
        try {

            //Processing arguments
            if(args.length == 4) {
                String passwordCifra = args[1];
                String keystorePath = args[2];
                String passKeystore = args[3];
                SSLServerSocket sSocket = null;
                int port = 12345;
                if(args.length == 1){port = Integer.valueOf(args[0]);} 
                System.out.println("Porto: " + port);

                //Setting up keystore in the properties
                System.setProperty("javax.net.ssl.keyStore", keystorePath);
                System.setProperty("javax.net.ssl.keyStorePassword", passKeystore);
                ServerSocketFactory ssf = SSLServerSocketFactory.getDefault( );
                sSocket = (SSLServerSocket) ssf.createServerSocket(port);

                //Create TintolmarketServer object and load any previous data
                TintolmarketServer server = new TintolmarketServer(sSocket, passwordCifra);
                server.loadData();
            
                server.startServer();
            } else {
                System.out.println("Introduced the wrong number of arguments! Be sure to read the readMe on how to execute.");
            }

        } catch (IOException e) {              
            System.err.println(e.getMessage());
            System.exit(-1);
        }
	}

    /**
     * Starts the server and awaits new clients to connect to it
     */
    public void startServer() {
        loadBLKS();
        blockchain.loadHashes();
        if(blockchain.isChainValid()) System.out.println("Chain is valid");
        else System.out.println("Chain is not valid");

        try {

            //Wait for a client to connect
            while(!sSocket.isClosed()) {

                new SSLSimpleServer(sSocket.accept()).start( );
                System.out.println("A new client is connecting. Awaiting authentication.");

            }
            writer.close();
        } catch(IOException e) {
            try {
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            closeSever();
            e.printStackTrace();
        }

    }

    /**
     * Loads the data from a previous iteration of the program onto the respective objects. In case of the users and usersSaldo, must first
     * decrypt the data.
     */
    public void loadData(){
            try {

                //Read users
                BufferedReader reader = new BufferedReader(new FileReader("..//serverBase//users.txt"));
                BufferedReader readerParams = new BufferedReader(new FileReader("..//serverBase//params.txt"));
                String line;
                while((line = reader.readLine()) != null){
                    
                    // Get params
                    String lineParams = readerParams.readLine();

                    // Decode bytes
                    byte[] byteMsg = Base64.getDecoder().decode(line);
                    byte[] byteParams = Base64.getDecoder().decode(lineParams);

                    // Initialize params
                    AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
                    p.init(byteParams);

                    // Get key
                    byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
                    PBEKeySpec keySpec = new PBEKeySpec(passwordCipher.toCharArray(), salt, 20); // pass, salt, iterations
                    SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
                    SecretKey key = kf.generateSecret(keySpec);

                    // Decrypt message bytes
                    Cipher d = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
                    d.init(Cipher.DECRYPT_MODE, key, p);
                    byte [] bytesDecMessage = d.doFinal(byteMsg);

                    // Change message back from byte[] to a string
                    String decryptedMsg = new String(bytesDecMessage, "UTF-8");

                    // Delete white spaces
                    String msgWithoutWhiteSpaces = decryptedMsg.replaceAll("\\s","");

                    // Load data from message
                    String[] data = msgWithoutWhiteSpaces.split(":");
                    usernames.add(data[0]);
                    userList.put(data[0], data[1]);
                }
                reader.close();
                readerParams.close();

                //Read usersSaldo
                reader = new BufferedReader(new FileReader("..//serverBase//usersSaldo.txt"));
                readerParams = new BufferedReader(new FileReader("..//serverBase//paramsSaldo.txt"));
                while((line = reader.readLine()) != null){

                    // Get params
                    String lineParams = readerParams.readLine();

                    // Decode bytes
                    byte[] byteMsg = Base64.getDecoder().decode(line);
                    byte[] byteParams = Base64.getDecoder().decode(lineParams);

                    // Initialize params
                    AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
                    p.init(byteParams);

                    // Get key
                    byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
                    PBEKeySpec keySpec = new PBEKeySpec(passwordCipher.toCharArray(), salt, 20); // pass, salt, iterations
                    SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
                    SecretKey key = kf.generateSecret(keySpec);

                    // Decrypt message bytes
                    Cipher d = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
                    d.init(Cipher.DECRYPT_MODE, key, p);
                    byte [] bytesDecMessage = d.doFinal(byteMsg);

                    // Change message back from byte[] to a string
                    String decryptedMsg = new String(bytesDecMessage, "UTF-8");

                    // Delete white spaces
                    String msgWithoutWhiteSpaces = decryptedMsg.replaceAll("\\s","");

                    // Load data from message
                    String[] data = msgWithoutWhiteSpaces.split(":");
                    userSaldo.put(data[0], Integer.valueOf(data[1]));

                }
                reader.close();

                //Read wines
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

                //Read inbox
                reader = new BufferedReader(new FileReader("..//serverBase//inbox.txt"));
                while((line = reader.readLine()) != null){
                    inbox.add(line);

                }
                reader.close();

            } catch ( IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
                      InvalidAlgorithmParameterException | IllegalBlockSizeException | InvalidKeySpecException e) {
                closeSever();
                e.printStackTrace();
            }  catch ( BadPaddingException e) {
                System.out.println("Introduced the wrong cipher-password. Be sure to use the same password as last time you executed the server.");
                closeSever();
            }
    }

    private void loadBLKS(){
        int i = 1;
        while (true) {
            File file = new File("block" + i + ".blk");
            if (!file.exists()) {
                break;
            }
            processBLK(i);
            i++;
        }
    }
    
    

    private void processBLK(int blkFile){
        
        try {
            FileInputStream fis = new FileInputStream("block" + blkFile + ".blk");
            ObjectInputStream ois = new ObjectInputStream(fis);

            String previousHash = (String) ois.readObject(); //hash
            int id = ois.readInt(); //id
            int numTransactions = ois.readInt(); //nTransaction
            List<transaction> transactions = new ArrayList<>(); //listOfTransactions
            for (int i = 0; i < numTransactions; i++) {
                transaction t = (transaction) ois.readObject();
                transactions.add(t);
            }
            blockchain.uploadBlock(previousHash, id, transactions);
            byte[] serverSignature = null;
            if (ois.available() > 0) {
                serverSignature = (byte[]) ois.readObject();
                blockchain.getLatestBlock().setSignature(new String(serverSignature));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        

    }



    /**
     * Closes the server. Can be used when unexpected exceptions occur.
     */
    private void closeSever() {
        try {
            if(writer != null) {
                writer.close();
            }

            if(writer2 != null) {
                writer.close();
            }

            if(sSocket != null) {
                sSocket.close();
            }
        } catch (IOException e) {
        e.printStackTrace();
        }
    }

    /**
     * Server Thread. Each SSLSimpleServer is responsible for a single client and all the operations related to that client, extends class Thread.
     */
    class SSLSimpleServer extends Thread {
        private Socket socket = null;
        private String username;
        private ObjectOutputStream outStream;
        private ObjectInputStream inStream;
        private BufferedInputStream inBuff;
        private BufferedOutputStream outBuff;
        private int saldo;

        /**
         * ServerThread constructor
         * @param newClientSocket the client socket
         */
        SSLSimpleServer(Socket newClientSocket) {
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

        // Runs the server thread.
        public void run() {
            try {

                // Call for user authentication immediatly after connection.
                authenticateUser();
                
                // Listening for a message 
                String messageFromClient;
                while(socket.isConnected()) {

                    try {

                        // Listens for the command message.
                        messageFromClient = (String)inStream.readObject();
                        String[] splitMessage = messageFromClient.split(" ", 0);

                        switch(splitMessage[0]) {

                            // Handles add command.
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

                            // Handles sell command.
                            case "sell":
                            case "s":
                                if(splitMessage.length == 4) {
                                    sell(splitMessage[1], Integer.valueOf(splitMessage[2]),  Integer.valueOf(splitMessage[3]));
                                    transaction tr = new transaction(splitMessage[1], Integer.valueOf(splitMessage[3]),Integer.valueOf(splitMessage[2]), this.username, transactionType.SELL);
                                    blockchain.addTransaction(tr);
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("sell failed due to wrong number of parameters. Be sure to use sell 'wine' 'value' 'quantity'.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }
                                break;   

                            // Handles view command.
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

                            // Handles buy command.
                            case "buy":
                            case "b":
                                if(splitMessage.length == 4) {
                                    buy(splitMessage[1], splitMessage[2], Integer.valueOf(splitMessage[3]));
                                    transaction tr = new transaction(splitMessage[1], Integer.valueOf(splitMessage[3]), wineList.get(getIndexOfWine(splitMessage[1])).getValueOfWineSoldBySeller(splitMessage[2]), this.username, transactionType.BUY);
                                    blockchain.addTransaction(tr);
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("buy failed due to wrong number of parameters. Be sure to use buy 'wine' 'seller' 'quantity'.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }
                                break;

                            // Handles wallet command.
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

                            // Handles classify command.
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

                            // Handles talk command.
                            case "talk":
                            case "t":
                                if(splitMessage.length == 2) {
                                    System.out.println(splitMessage[1]);
                                    talk(splitMessage[1], this.username);
                                } else {
                                    System.out.println(username + " introduced the wrong number of parameters when calling a function.");
                                    outStream.writeObject("talk failed due to wrong number of parameters. Be sure to use talk 'user' 'message'.");
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                }
                                break;

                            

                            // Handles read command.
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

                            case "list":
                            case "l":
                                    outStream.writeObject(voidReadBlockChain());
                                    outStream.writeObject("\n");
                                    outStream.flush();
                                break;  

                            // Handles cases where user introduced a non accepted command.
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
         * Handles the processing of user authentication with the server.
         * When the user sends his username to the server, the server will send a nonce back to the user.
         * The user will responde with the original nonce, the encrypted nonce and their certificate.
         * After this the server will verify the authenticity of the user by decrypting the nonce with the users private key.
         * Server sends back authentication confirmation.
         * 
         */
        public void authenticateUser() {
            try {

                // Recieving the username from the user.
                String user = (String)inStream.readObject();

                // Generating a nonce and sending it to the user.
                byte[] nonce = new byte[8];
                new SecureRandom().nextBytes(nonce);
                outBuff.write(nonce, 0, 8);
                outBuff.flush();

                // Verifying if the user is already registered.
                if(!usernames.contains(user)) {

                    // Send isRegistered flag.
                    outStream.writeObject(false);
                    outStream.flush();

                    // Receive original nonce and verify if it matches generated nonce.
                    byte[] originalNonce = new byte[8];
                    inBuff.read(originalNonce);
                    if(!Arrays.equals(originalNonce, nonce)){
                        outStream.writeObject("Authentication failed, generated nonce and original nonce don't match, disconnecting!");
                        outStream.flush();
                        System.out.println("Authentication failed, generated nonce and original nonce don't match, disconnecting client");
                        closeEverything(socket, inStream, outStream);
                        return;
                    };

                    // Receive encrypted nonce size and encrypted nonce.
                    int encryptedNonceLength = (int)inStream.readObject();
                    byte[] encryptedNonce =  new byte[encryptedNonceLength];
                    inBuff.read(encryptedNonce);

                    // Receive client certificate chain.
                    Certificate[] clientCer = (Certificate[])inStream.readObject();

                    // Get the public key from the certificate.
                    X509Certificate clientCert = (X509Certificate) clientCer[0];
                    PublicKey publicKey = clientCert.getPublicKey();

                    // Verify with the public key if the encrypted nonce is correct.
                    Cipher cipher = Cipher.getInstance("RSA");
                    cipher.init(Cipher.DECRYPT_MODE, publicKey);
                    byte[] decryptedNonce = cipher.doFinal(encryptedNonce);

                    // Send error message if decryption failed.
                    if (!Arrays.equals(decryptedNonce, nonce)) {
                        outStream.writeObject("Authentication failed, encrypted nonce doesn't match nonce, disconnecting!");
                        outStream.flush();
                        System.out.println("Authentication failed, encrypted nonce doesn't match nonce, disconnecting client");
                        closeEverything(socket, inStream, outStream);
                        return;
                    };
                    
                    // If every condition is met, register client.
                    this.username = user;
                    usernames.add(username);
                    userList.put(username, "..\\serverBase\\cert" + username + ".cer");
                    userSaldo.put(username, this.saldo);
                    System.out.println("New user " + username + " registered.");
                    users.add(this);
                    outStream.writeObject("Registered successfully!");
                    outStream.flush();
                    writeUsers();
                    writeSaldo();

                    // Save user certificate in server base.
                    FileOutputStream fout = new FileOutputStream("..\\serverBase\\cert" + username + ".cer");
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate cert = cf.generateCertificate(new ByteArrayInputStream(clientCer[0].getEncoded()));
                    fout.write(cert.getEncoded());
                    fout.close();
                    
                } else {

                    // Send isRegistered flag.
                    outStream.writeObject(true);
                    outStream.flush();

                    // Receive encrypted nonce size and encrypted nonce.
                    int encryptedNonceLength = (int)inStream.readObject();
                    byte[] encryptedNonce =  new byte[encryptedNonceLength];
                    inBuff.read(encryptedNonce);

                    // Get client certificate chain.
                    String certificatePath = userList.get(user);     
                    FileInputStream inputStream = new FileInputStream(certificatePath);
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");    
                    X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);
                    
                    // Get public key from certificate.
                    PublicKey publicKey = certificate.getPublicKey();

                    // Verify with the public key if the encrypted nonce is correct.
                    Cipher cipher = Cipher.getInstance("RSA");
                    cipher.init(Cipher.DECRYPT_MODE, publicKey);
                    byte[] decryptedNonce = cipher.doFinal(encryptedNonce);

                    // Send error message if decryption failed.
                    if (!Arrays.equals(decryptedNonce, nonce)) {
                        outStream.writeObject("Authentication failed, encrypted nonce doesn't match nonce, disconnecting!");
                        outStream.flush();
                        System.out.println("Authentication failed, encrypted nonce doesn't match nonce, disconnecting client");
                        closeEverything(socket, inStream, outStream);
                        return;
                    };

                    // If every condition is met, log in the client.
                    this.username = user;
                    users.add(this);
                    System.out.println("User " + username + " logged in successful."); 
                    outStream.writeObject("Authentication successful!");
                    outStream.flush();
                    writeUsers();
                    writeSaldo();


                }

            } catch (ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | IOException | 
                     InvalidKeyException | IllegalBlockSizeException | BadPaddingException | CertificateException e) 
            {
                System.out.println("Something has gone wrong, disconnecting client");
                e.printStackTrace();
            } 

        }


        /**
         * Writes user related data onto a txt file. Encrypts the data using AES 128 and the passwordCipher.
         * 
         */
        public void writeUsers() {
            try {

                // Create BufferedWriter objects for the users and params files.
                writer = new BufferedWriter(new FileWriter("..//serverBase//users.txt"));
                writer2 = new BufferedWriter(new FileWriter("..//serverBase//params.txt"));

                for(String tag : usernames) {

                    // Create string.
                    String toWrite = tag + ":" + userList.get(tag) + "\n";

                    // Generate key based on password.
                    byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
                    PBEKeySpec keySpec = new PBEKeySpec(passwordCipher.toCharArray(), salt, 20);
                    SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
                    SecretKey key = kf.generateSecret(keySpec);

                    // Change message to bytes.
                    byte[] byteMsg = toWrite.getBytes("UTF-8");

                    // Generate cipher and encrypt string.
                    Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
                    c.init(Cipher.ENCRYPT_MODE, key);
                    byte[] byteEncryptedMsg = c.doFinal(byteMsg);
                    byte[] params = c.getParameters().getEncoded();
                    
                    // Encode params bytes to string.
                    String encodedParams = Base64.getEncoder().encodeToString(params);

                    // Encode message bytes to string.
                    String encodedMsg = Base64.getEncoder().encodeToString(byteEncryptedMsg);

                    // Write.
                    writer.write(encodedMsg + "\n");
                    writer.flush();
                    writer2.write(encodedParams + "\n");
                    writer2.flush();

                }

                // Close both writers.
                writer.close();
                writer2.close();

            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                     IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e1) {
                e1.printStackTrace();
            }
        }  

        /**
        * Writes the users wallet onto a txt file. Encrypts the data using AES 128 and the passwordCipher.
        *
        */
        public void writeSaldo() {

            try {

                // Create BufferedWriter objects for the userSaldos and paramsSaldo files.
                writer = new BufferedWriter(new FileWriter("..//serverBase//usersSaldo.txt"));
                writer2 = new BufferedWriter(new FileWriter("..//serverBase//paramsSaldo.txt"));

                for(String tag : usernames) {

                    // Create string.
                    String toWrite = tag + ":" + userSaldo.get(tag) +"\n";

                    // Generate key based on password.
                    byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
                    PBEKeySpec keySpec = new PBEKeySpec(passwordCipher.toCharArray(), salt, 20);
                    SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
                    SecretKey key = kf.generateSecret(keySpec);

                    // Change message to bytes.
                    byte[] byteMsg = toWrite.getBytes("UTF-8");

                    // Generate cipher and encrypt string.
                    Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
                    c.init(Cipher.ENCRYPT_MODE, key);
                    byte[] byteEncryptedMsg = c.doFinal(byteMsg);
                    byte[] params = c.getParameters().getEncoded();
                    
                    // Encode params bytes to string.
                    String encodedParams = Base64.getEncoder().encodeToString(params);

                    // Encode message bytes to string.
                    String encodedMsg = Base64.getEncoder().encodeToString(byteEncryptedMsg);

                    // Write.
                    writer.write(encodedMsg + "\n");
                    writer.flush();
                    writer2.write(encodedParams + "\n");
                    writer2.flush();

                }

                // Close both writers.
                writer.close();
                writer2.close();

            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                     IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Writes inbox related data onto a txt file.
         */
        public void writeInbox() {
            try {

                // Create BufferedWriter objects for the inbox files.
                writer = new BufferedWriter(new FileWriter("..//serverBase//inbox.txt"));

                for(String message : inbox) {

                    // Write.
                    writer.write(message + "\n");
                    writer.flush();
                }

                // Close writer.
                writer.close();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Writes current wines in the store and which user is selling which wine onto a txt file.
         */
        public void writeWine() {
            try {

                // Create BufferedWriter objects for the inbox files.
                writer = new BufferedWriter(new FileWriter("..//serverBase//wines.txt"));

                for(TintolmarketWine wine : wineList) {

                    // Write.
                    writer.write(wine.getWinename() + " " + wine.getPath() + " " + wine.getListofSellers().size() + " " + wine.getClassification() + "\n");
                    writer.flush();
                    if(wine.getListofSellers().size() > 0) {
                        for(String seller: wine.getListofSellers()) {
                            writer.write(seller + " " + wine.getValueOfWineSoldBySeller(seller) + " " + wine.getQuantitySoldBySeller(seller) + "\n");
                            writer.flush();
                        }
                    }

                }

                // Close writer.
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

                String[] toSend = message.split("split", 2);

                if(toSend[0].equals(this.username)) {
                    hadMessage = true;
                    toDelete.add(message);
                    try {

                        outStream.writeObject("readFlag");
                        outStream.flush();

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
        public void talk(String reciever, String sender) {
            
            try {

                // Recieve encrypted message size and encrypted message 
                int encryptedMessageLength = (int)inStream.readObject();
                byte[] byteEncryptedMessage =  new byte[encryptedMessageLength];
                inBuff.read(byteEncryptedMessage);

                // Encode the bytes
                String encodedBytes = Base64.getEncoder().encodeToString(byteEncryptedMessage);

                for(SSLSimpleServer tr : users) {
                    if(tr.username.contains(reciever)) {
                        System.out.println(username + " has sent a messages to " + reciever + ".");
                        inbox.add(reciever + "split" + sender + "split" + encodedBytes);
                        writeInbox();
                        outStream.writeObject("Message added to inbox.");
                        outStream.flush();
                        return;
                    }
                } 
                System.out.println(username + " tried to send a messages to a user that does not exist.");

         
                outStream.writeObject("Introduced a user that does not exist.");
            } catch (ClassNotFoundException | IOException e) {
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
                    String[] img = path.split("//");
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
                            for(SSLSimpleServer t: users){
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

        private String voidReadBlockChain(){
            StringBuilder sb = new StringBuilder();
            List<block> temp = blockchain.getListOBlocks();
            for(block block: temp){
                sb.append(block.readBlock() + "\n");
            }
            return sb.toString();
        }

    } //End of server thread

}