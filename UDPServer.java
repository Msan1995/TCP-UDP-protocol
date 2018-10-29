
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;

//Server Control Port = 6000
//Server Data Control Port = 7000
//Client Control Port = 8000
//Client Data port = 9000


public class UDPServer {
	static String link="";
	static int serverContorlPortNumber;
	static int serverDataPortNumber;
	static int clientDataPortNumber;
	
public static void main(String[] args) throws IOException {

		   //Server server = new Server();
		   getUserInput();
		   String cmd;
		   DatagramSocket controlSocket = new DatagramSocket(serverContorlPortNumber);
		   DatagramSocket dataSocket = new DatagramSocket(serverDataPortNumber);
		 
		   messageProcess(controlSocket,dataSocket,clientDataPortNumber);
		   
		   controlSocket.close();
		   dataSocket.close();
	       
}
	
public static void getUserInput() throws IOException {
	    Scanner b = new Scanner(System.in);
	    
	    //System.out.println("Enter Server Control port");
	    //serverContorlPortNumber = b.nextInt();
	    serverContorlPortNumber = 13000;
	    if(serverContorlPortNumber < 0 || serverContorlPortNumber > 655535 ) {
	    	System.out.println("Invalid port Number, Terminating");
	    	System.exit(0);
	    }
	    
	    //System.out.println("Enter Server Data port");
	    //serverDataPortNumber = b.nextInt();BVP
            serverDataPortNumber=14000;
	    if(serverDataPortNumber < 0 || serverDataPortNumber > 655535 ) {
	    	System.out.println("Invalid port Number, Terminating");
	    	System.exit(0);
	    }
	    
	    //System.out.println("Enter Client Data port");
	    //clientDataPortNumber = b.nextInt();
	    clientDataPortNumber = 16000;
	    if(clientDataPortNumber < 0 || clientDataPortNumber > 655535 ) {
	    	System.out.println("Invalid port Number, Terminating");
	    	System.exit(0);
	    }
	    b.close();
}            

	
public static  void messageProcess(DatagramSocket controlSocket,DatagramSocket dataSocket,int clientDataPortNumber ) throws IOException{
		//DatagramSocket ds = new DatagramSocket(1234); 
        byte[] messageLengthByte = new byte[65535]; 
        byte[] message = new byte[65535]; 
            
     //Receive Message Length
     while(true) {
    	 System.out.println("Waiting For Client Message or Command");	   
        DatagramPacket DpReceive = new DatagramPacket(messageLengthByte, messageLengthByte.length); 
        controlSocket.receive(DpReceive);
        ByteArrayInputStream messageLengthbin = new ByteArrayInputStream(DpReceive.getData());
        DataInputStream messageLengthDataIn = new DataInputStream(messageLengthbin);
        int messageLength = messageLengthDataIn.readInt();
        System.out.println("RCV:MsgLEngth: "+messageLength);
        messageLengthbin.close();
        
        // set the timeout in milliseconds for socket to wait for Message
        controlSocket.setSoTimeout(500);   
        
        while(true){   
            try {
            	//Receive Message
            	DatagramPacket messageDpReceive = new DatagramPacket(message, message.length); 
                controlSocket.receive(messageDpReceive);
                ByteArrayInputStream messageBin = new ByteArrayInputStream(messageDpReceive.getData());
                DataInputStream messageDataIn = new DataInputStream(messageBin);
                String receivedMessage = messageDataIn.readUTF();
                int clientPort = DpReceive.getPort();
            	InetAddress clientAddress = DpReceive.getAddress();
                System.out.println("RCV:Msg: "+receivedMessage);
                messageBin.close();
            	                              
                /*Get ACK into byte array*/
                ByteArrayOutputStream messageACKByteOut = new ByteArrayOutputStream();
                DataOutputStream messageACKDataOut = new DataOutputStream(messageACKByteOut);
                messageACKDataOut.writeUTF("ACK");
                byte messageACKBuffer[] = messageACKByteOut.toByteArray(); 
               
                // Send ACK to the Client
                DatagramPacket DpSendACK =  new DatagramPacket(messageACKBuffer, messageACKBuffer.length,
                		                                                clientAddress,clientPort); 
                controlSocket.send(DpSendACK);
                messageACKByteOut.close(); 
               
               
                // Process The Message
            	System.out.println(receivedMessage);
                String[] commandArray=receivedMessage.split(" ");
                
                if(messageLength == receivedMessage.length()) {
                	if(commandArray[0].equals("cd")) {
                	  cdProcess(controlSocket,commandArray[1],clientPort,clientAddress);
                	  controlSocket.setSoTimeout(0);
                	  break;
                	}
                	else if(commandArray[0].equals("ls")) {
                  	  lsProcess(controlSocket,commandArray[1],clientPort,clientAddress);
                  	  controlSocket.setSoTimeout(0); 
                  	  break;
                  	}
                	else if(commandArray[0].equals("put")) {
                    	  putProcess(dataSocket,commandArray[1],clientPort,clientAddress);
                    	  controlSocket.setSoTimeout(0);
                    	  break;
                    	}
                	else if(commandArray[0].equals("get")) {
                		if(commandArray[1].equals(null)) {
                			System.out.println("Should be this Format get <srcFile> <dstFile>");
                			break;
                		}
                  	    getProcess(dataSocket,commandArray[1],clientDataPortNumber,clientAddress);
                  	    controlSocket.setSoTimeout(0);
                  	  break;
                  	}
                        else{
                            System.out.println("Invalid Command Received from the client.");
                            controlSocket.setSoTimeout(0);
                            break;
                        }
                }
                break;
            }
            catch (SocketTimeoutException e) {
                // timeout exception.
                System.out.println("Did Not Receive valid data from Client. Terminating." + e);
                controlSocket.close();
                System.exit(0);
            }
        }
        continue;
     }
}
public static void getProcess(DatagramSocket dataSocket,String fileName,
		                        int dataPort,  InetAddress clientIPAddress) throws IOException{
	String completeFileName = link+"/"+fileName;
	sendFileContent(dataSocket,clientIPAddress, dataPort, completeFileName);
}

public static void sendFileContent(DatagramSocket dataSocket,InetAddress address,int dataPort, String qw) throws IOException {
    System.out.println("Sending the file"+ qw);
    File file = new File(qw); 

    InputStream inFromFile = new FileInputStream(file);
    byte[] fileByteArray = new byte[(int)file.length()];
    inFromFile.read(fileByteArray);

    // Create a flag to indicate the last message and a 16-bit sequence number
    int sequenceNumber = 0;
    boolean lastMessageFlag = false;

    // For each message we will create
    for (int i=0; i < fileByteArray.length; i = i+1021 ) {

        // Increment sequence number
        sequenceNumber += 1;

        // Create new message. Set first and second bytes of the message to sequence number
        byte[] message = new byte[1024];
        message[0] = (byte)(sequenceNumber >> 8);
        message[1] = (byte)(sequenceNumber);

        // Set flag to 1 if packet is last packet and store it in third byte of header
        if ((i+1021) >= fileByteArray.length) {
            lastMessageFlag = true;
            message[2] = (byte)(1);
        } else { // If not last packet, store flag as 0
            lastMessageFlag = false;
            message[2] = (byte)(0);
        }

        // Copy the bytes for the message to the message array
        if (lastMessageFlag == false) {
            for (int j=0; j <= 1020; j++) {
                message[j+3] = fileByteArray[i+j];
            }
        } else if (lastMessageFlag == true) { // If it is the last message
            for (int j=0;  j < (fileByteArray.length - i)  ;j++) {
                message[j+3] = fileByteArray[i+j];                      
            }
        }
     
       
        /*Get File Chunk into byte array*/
        ByteArrayOutputStream fileChunkByteOut = new ByteArrayOutputStream();
        DataOutputStream fileChunkDataOut = new DataOutputStream(fileChunkByteOut);
        fileChunkDataOut.write(message);
        byte fileChunkBuffer[] = fileChunkByteOut.toByteArray(); 
       
        // Send File Chunk to the Server
        DatagramPacket DpSendFileChunk =  new DatagramPacket(fileChunkBuffer, fileChunkBuffer.length,address,dataPort); 
        dataSocket.send(DpSendFileChunk);
        fileChunkByteOut.close(); 
        
        System.out.println("Sent: Sequence number = " + sequenceNumber + ", Flag = " + lastMessageFlag);

        // Sleep for 30 milliseconds to avoid sending too quickly
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    dataSocket.close();
    inFromFile.close();
    System.out.println("File " + qw + " has been sent");
}

public static void cdProcess(DatagramSocket controlSocket, String fileName,
		                     int clientPortNumber,  InetAddress clientIPAddress) throws IOException{
    File dir = new File(fileName);
    String n=null;
    byte buf[] = null;
    
    if(dir.isDirectory()==true){
        System.setProperty("user.dir", dir.getAbsolutePath());
        link=fileName;
       n= new String("changed");
       System.out.println("Current Directory is changed to "+n );
    }
    else{
        System.out.println(fileName + " is not a directory.");
        n= new String("can not be changed");
     }
     // buf = n.getBytes();
     //DatagramPacket DpSend = new DatagramPacket(buf, buf.length, clientIPAddress, clientPortNumber);  
     //controlSocket.send(DpSend);
}

public static void lsProcess(DatagramSocket controlSocket, String fileName,
        int clientPortNumber,  InetAddress clientIPAddress) throws IOException{

	File file = new File(fileName);
	String[] names = file.list();
    String send =new String();
    
    for(String name : names){
	    System.out.println(name);
	    send=send+"!!!"+name;   
	}     
	DatagramSocket ds1 = new DatagramSocket(); 
    byte buf[] = null; 
    buf = send.getBytes();
    DatagramPacket DpSend = new DatagramPacket(buf, buf.length, clientIPAddress, clientPortNumber); 
    ds1.send(DpSend);
    ds1.close();
}	

public static void ls() throws IOException{
	
		DatagramSocket ds = new DatagramSocket(1600); 
        byte[] receive = new byte[65535]; 
  
        DatagramPacket DpReceive = null; 
        
  
            // Step 2 : create a DatgramPacket to receive the data. 
            DpReceive = new DatagramPacket(receive, receive.length); 
  
            // Step 3 : revieve the data in byte buffer. 
            ds.receive(DpReceive); 
 
           String v= data(receive).toString(); 
           
           File file = new File(v);
       	   String[] names = file.list();
           String send =new String();
       	   for(String name : names){ 	       
       	       System.out.println(name);
       	       send=send+"!!!"+name;   
       	   } 
           
       	DatagramSocket ds1 = new DatagramSocket(); 
        
        InetAddress ip = InetAddress.getLocalHost(); 
        byte buf[] = null; 
        buf = send.getBytes();
        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 1240); 
        ds1.send(DpSend);
        ds1.close();
        ds.close();
	}

public static void putProcess(DatagramSocket dataSocket, String absoluteFileName,
                               int clientPortNumber,InetAddress clientIPAddress) throws IOException{
	   System.out.println("Ready to receive the file!");

       // Get the address, port and name of file to send over UDP
       //final int port = 3005;
	   
	  int i= absoluteFileName.lastIndexOf("/");
	   //int i= absoluteFileName.lastIndexOf("\\");
	   System.out.println("Received Absolute FileName:"+absoluteFileName);
	   String fileName = absoluteFileName.substring(i+1, absoluteFileName.length());
	   System.out.println("Received FileName:"+fileName);  
	    
	   String filePath = link;
    
       String completeFileName= link+"/"+fileName; 
       System.out.println("Path Where File Will be Created: "+completeFileName);
          
       receiveAndCreateFile(dataSocket,completeFileName,clientIPAddress);
 }


public static void receiveAndCreateFile(DatagramSocket dataSocket, String fileName,InetAddress clientIPAddress) throws IOException {
    
    File file = new File(fileName);
    
    FileOutputStream outToFile = new FileOutputStream(file);

    // Create a flag to indicate the last message
    boolean lastMessageFlag = false;
    boolean lastMessage = false;

    // Store sequence number
    int sequenceNumber = 0;

    // For each message we will receive
    while (!lastMessage) {
        // Create byte array for full message and another for file data without header
        byte[] message = new byte[1024];
        byte[] filebyteArray = new byte[1021];

        // Receive packet and retrieve message
        DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
        System.out.println("Waiting to Receive File Content");
        dataSocket.receive(receivedPacket);
        System.out.println("Received File Content");
        message = receivedPacket.getData();

        // Retrieve the sequence number
        sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);	

        // Retrieve the last message flag
        if ((message[2] & 0xff) == 1) {
            lastMessageFlag = true;
        } else {
            lastMessageFlag = false;
        }

        // Retrieve data from message
        for (int i=3; i < 1024 ; i++) {
            filebyteArray[i-3] = message[i];
        }

        // Write the message to the file and print received message
        outToFile.write(filebyteArray);
        System.out.println("Received: Sequence number = " + sequenceNumber + ", Flag = " + lastMessageFlag);

        // Check for last message
        if (lastMessageFlag) {
            outToFile.close();
           // dataSocket.close();
            lastMessage = false;
            break;
        }
    }
  
    //dataSocket.close();
    System.out.println("File " + fileName + " has been received.");
}
BufferedReader executeCommand(String cmd) throws IOException {
	 ProcessBuilder builder = new ProcessBuilder(
	            "cmd.exe", "/c", "cmd");
	        builder.redirectErrorStream(true);
	        Process p = builder.start();
	        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        return r;
}
	public static StringBuilder data(byte[] a) 
    { 
        if (a == null) 
            return null; 
        StringBuilder ret = new StringBuilder(); 
        int i = 0; 
        while (a[i] != 0 ) 
        { 
        	if(a[i]>=0)
        	{
        	
            ret.append((char) a[i]); 
           
        	}
        	 i++; 
        } 
        return ret; 
    } 
}
