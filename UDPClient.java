
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

// Server Control Port = 6000
//Server Data Control Port = 7000
//Client Control Port = 8000
//Client Data port = 9000

public class UDPClient {
	
	static String ipAddress;
	static int serverContorlPortNumber;
	static int serverDataPortNumber;
	static int clientControlPortNumber;
	static int clientDataPortNumber;
	static String link="";
	
	public static void main(String[] args) throws IOException {
		String cmd;
		//Client client =new Client();
		getUserInput();
		
		Scanner sc = new Scanner(System.in);
	    DatagramSocket clientSocket = new DatagramSocket(); 
	    DatagramSocket controlSocket = new DatagramSocket(clientControlPortNumber);
		DatagramSocket dataSocket = new DatagramSocket(clientDataPortNumber);
		   
        InetAddress ip = InetAddress.getByName(ipAddress); 
        int commandPort =serverContorlPortNumber;
        int dataPort =serverDataPortNumber;
       	
	    
	    while(true) {
	    	System.out.println("Enter Command:");
            cmd = sc.nextLine();
            sendCommand(controlSocket,dataSocket,ip,commandPort,dataPort,cmd);
            if(cmd.equals("exit")) {
            	   break;
            }       
	    }
	    sc.close();
		clientSocket.close();
		dataSocket.close();
}
	
public static void getUserInput() throws IOException {
		   
	    Scanner b = new Scanner(System.in);
	    System.out.println("Enter Server IP address");
    	    ipAddress = b.nextLine();
    	    //ipAddress = "127.0.0.1";
    	
        System.out.println("Enter Server Control port <as 13000>");
        serverContorlPortNumber = b.nextInt();
        //serverContorlPortNumber = 13000;
        if(serverContorlPortNumber < 0 || serverContorlPortNumber > 655535 ) {
        	System.out.println("Invalid port Number, Terminating");
        	System.exit(0);
        }
        
        //System.out.println("Enter Server Data port");
        //serverDataPortNumber = b.nextInt();
        serverDataPortNumber = 14000;
        if(serverDataPortNumber < 0 || serverDataPortNumber > 655535 ) {
        	System.out.println("Invalid port Number, Terminating");
        	System.exit(0);
        }
         
        //System.out.println("Enter Client Control port");
        //clientControlPortNumber = b.nextInt();
        clientControlPortNumber = 15000;
        if(clientControlPortNumber < 0 || clientControlPortNumber > 655535 ) {
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
}

public static void sendCommand(DatagramSocket controlSocket,DatagramSocket dataSocket,
		                        InetAddress ip, int commandPort,
		                        int dataPort,String command) throws IOException{
	String commandInput = command;
	String[] commandArray=command.split(" ");
	int resendCount=2;
	byte[] receive = new byte[65535];
	byte[] ack = new byte[6];
	
	//*Get the Command Length in Byte Array Format*/
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(byteOut);
    dataOut.writeInt(commandInput.length());
    byte messageLengthBuffer[] = byteOut.toByteArray();
   
    /*Get Command into byte array*/
    ByteArrayOutputStream messageByteOut = new ByteArrayOutputStream();
    DataOutputStream messageDataOut = new DataOutputStream(messageByteOut);
    messageDataOut.writeUTF(commandInput);
    byte messageBuffer[] = messageByteOut.toByteArray(); 
    //link=commandInput;  
    
    /*Send Message Length*/
    DatagramPacket DpSendMessageLength =  new DatagramPacket(messageLengthBuffer, messageLengthBuffer.length,ip ,commandPort); 
    controlSocket.send(DpSendMessageLength);
    dataOut.close(); // or dataOut.flush()
   
    /*Send Message*/
    DatagramPacket DpSendMessage =  new DatagramPacket(messageBuffer, messageBuffer.length,ip ,commandPort); 
    controlSocket.send(DpSendMessage);
    messageDataOut.close();
    
    DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 
    
  
    // set the timeout in milliseconds for socket to wait for ACK
    controlSocket.setSoTimeout(1000); 
   
    while(true){   
        try {
        	//Receive ACK
        	DatagramPacket ackDpReceive = new DatagramPacket(ack, ack.length); 
            controlSocket.receive(ackDpReceive);
            ByteArrayInputStream ackBin = new ByteArrayInputStream(ackDpReceive.getData());
            DataInputStream ackDataIn = new DataInputStream(ackBin);
            String ackMessage = ackDataIn.readUTF();
            System.out.println("RCV: "+ackMessage);
            ackBin.close();
        	
            if(ackMessage.equals("ACK")) {
        		System.out.println("ACK Received from the Server");
        		if(commandArray[0].equals("ls")){
        		   processLS(controlSocket);
        		   break;
        		}
        		else if(commandArray[0].equals("cd")){
         		   break;
         		}
        		else if(commandArray[0].equals("put")){ 			
        			processPut(dataSocket,commandArray[1],ip,dataPort);
          		   break;
          		}
        		else if(commandArray[0].equals("get")){ // get Src Dst
        			   processGet(dataSocket,commandArray[1]);
          		       break;
          		}
        	}
        	else {
        		System.out.println(ackMessage);
        		break;
        	}
            
        }
        catch (SocketTimeoutException e) {
            // timeout exception.
            System.out.println("Timeout reached!!! " + e);
            if(resendCount>0) {
            	
            	/*Re-send Send Message Length*/
    	        controlSocket.send(DpSendMessageLength);
    	        /*Re-send Message*/
    	        controlSocket.send(DpSendMessage);
    	        
    	        resendCount--;
    	        
    	        // set the timeout in milliseconds for socket to wait for ACK
    	        controlSocket.setSoTimeout(1000); 
    	        
    	        //Continue to wait for ACK
    	        continue;      	
            }
            else {
            System.out.println("Failed to send data to server. Terminating.");	
            controlSocket.close();
            System.exit(0);
            break;
            }
        }
    }
    //controlSocket.close();
}
public static void processGet(DatagramSocket dataSocket, String fileName) throws IOException{
          System.out.println("Ready to receive the file!"+ fileName);
          receiveAndCreateFile(dataSocket,fileName);
}


public static void receiveAndCreateFile(DatagramSocket dataSocket, String fileName) throws IOException {
    
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
            //socket.close();
            //dataSocket.close();
            lastMessage = false;
            break;
        }
    }
    
    //socket.close();
    //dataSocket.close();
    System.out.println("File " + fileName + " has been received.");
}

public static void processPut(DatagramSocket dataSocket,String absoluteFileName,
		                      InetAddress address, int dataPort  ) throws IOException{
	
	sendFileContent(dataSocket,address,dataPort,absoluteFileName);
}

public static void sendFileContent(DatagramSocket dataSocket,InetAddress address,int dataPort, String qw) throws IOException {
    File file = new File(qw); 

    InputStream inFromFile = new FileInputStream(file);
    byte[] fileByteArray = new byte[(int)file.length()];
    inFromFile.read(fileByteArray);

     System.out.println("Sending the file of Length: "+file.length()+"Byte Array LEngth"+fileByteArray.length);
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

    //dataSocket.close();
    inFromFile.close();
    System.out.println("File " + qw + " has been sent");
}


public static void processLS(DatagramSocket controlSocket) throws IOException {
	byte[] receive = new byte[65535];
    DatagramPacket DpReceive =  new DatagramPacket(receive, receive.length); 
    controlSocket.receive(DpReceive); 
    String v= data(receive).toString(); 
    String[] s2=v.split("!!!");
    for(String s3 :s2){
 	   System.out.println(s3);
    }
}

public static void ls(DatagramSocket controlSocket,InetAddress ip,int commandPort,String commandInput) throws IOException
	{
		DatagramSocket ds = new DatagramSocket(); 
        byte[] ipAddr = new byte[]{(byte) 192,(byte)168,(byte) 1, (byte)3};
       InetAddress addr = InetAddress.getLocalHost();
       if (commandInput.equals(null)|| commandInput.equals("") || commandInput.isEmpty())
       {
    	   commandInput=link;
       }
        byte buf[] = null; 
            buf = commandInput.getBytes(); 
            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, addr, commandPort); 
            controlSocket.send(DpSend); 
            DatagramSocket ds1 = new DatagramSocket(1240); 
            byte[] receive = new byte[65535]; 
      
            DatagramPacket DpReceive = null; 
            
      
                // Step 2 : create a DatgramPacket to receive the data. 
                DpReceive = new DatagramPacket(receive, receive.length); 
      
                // Step 3 : revieve the data in byte buffer. 
                ds1.receive(DpReceive); 
     
               String v= data(receive).toString(); 
               String[] s2=v.split("!!!");
               for(String s3 :s2)
               {
            	   System.out.println(s3);
               }
            
            ds1.close();
            ds.close();
            
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
