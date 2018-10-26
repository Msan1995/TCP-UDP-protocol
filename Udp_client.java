import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Udp_client {

	static String link="";
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		
		int o=0;
		 while(o!=5)
	       {
	       System.out.println("Please choose the following options/n 1)cd/n 2)ls /n 3)put /n 4)get");
	       
		   
	        //o=Integer.parseInt(q.readLine());
	     //   o=y.nextInt();      
	       o=Integer.parseInt(sc.nextLine());
//	        String s1=reader.readLine();
//	        System.out.println(s1);   
	       switch(o)
	       {
	       case 1:
	    	   int port=1234;
	    	   cd(port);
	    	  
		       
		       break;
	       case 2:
	    	  
	    	   int port1=3009;
	    	   ls(port1);
	    	   
	           break;
	       case 3:
	    	   
	    	   int port2=3022;
	    	  put(port2);
	    	  
	    	   break;
	    
	
	       case 4:
	    	   int port3=3023;
	    	   get(port3);
	    	  
	    	   break;
	    	   
	       }
	}
	}
	
	public static void cd(int port) throws IOException
	{
		 DatagramSocket ds = new DatagramSocket(); 
	     
	       InetAddress ip = InetAddress.getLocalHost(); 
	        byte buf[] = null; 
	  
	        Scanner sc= new Scanner(System.in);
	        System.out.println("Please enter the path");
	            String s1=sc.nextLine();
	            link=s1;
	            buf = s1.getBytes(); 
	  
	            
	            DatagramPacket DpSend = 
	                  new DatagramPacket(buf, buf.length, ip, port); 
	      
	            
	            ds.send(DpSend);
	            DatagramSocket ds1 = new DatagramSocket(1240); 
	            byte[] receive = new byte[65535]; 
	      
	            DatagramPacket DpReceive = null; 
	            
	                DpReceive = new DatagramPacket(receive, receive.length); 
	      
	                
	                ds1.receive(DpReceive); 
	     
	               String v= data(receive).toString(); 
	               System.out.println(v);
	               ds.close();
	               ds1.close();
	}
	
	public static void ls(int port) throws IOException
	{
		DatagramSocket ds = new DatagramSocket(); 
        byte[] ipAddr = new byte[]{(byte) 192,(byte)168,(byte) 1, (byte)3};
       InetAddress addr = InetAddress.getLocalHost();
        byte buf[] = null; 
            buf = link.getBytes(); 
            DatagramPacket DpSend = 
                  new DatagramPacket(buf, buf.length, addr, 1600); 
            ds.send(DpSend); 
            DatagramSocket ds1 = new DatagramSocket(1240); 
            byte[] receive = new byte[65535]; 
      
            DatagramPacket DpReceive = null; 
            
      
                // Step 2 : create a DatgramPacket to receive the data. 
                DpReceive = new DatagramPacket(receive, receive.length); 
      
                // Step 3 : revieve the data in byte buffer. 
                ds1.receive(DpReceive); 
     
               String v= data(receive).toString(); 
               String[] s2=v.split("##");
               for(String s3 :s2)
               {
            	   System.out.println(s3);
               }
            
            ds1.close();
            ds.close();
            
	}
	public static void put(int port) throws IOException
	{
		final String hostName = "127.0.0.1";
         port = 3005;
        String fileName = "";
        Scanner sc= new Scanner(System.in);
        System.out.println("Enter the file name");
        fileName=sc.nextLine();
        String qw=link+"\\"+fileName;
        System.out.println("Enter the file format like : .pdf or .txt");
        String fileName1 = sc.nextLine();
        byte buf[] = null;
        buf = fileName1.getBytes();
        DatagramSocket ds = new DatagramSocket();
        DatagramPacket DpSend = 
                new DatagramPacket(buf, buf.length,InetAddress.getByName(hostName), 3007);
        ds.send(DpSend);
        createAndSend(hostName, port, qw);
    }
	
	 public static void createAndSend(String hostName, int port, String qw) throws IOException {
	        System.out.println("Sending the file");

	        // Create the socket, set the address and create the file to be sent
	        DatagramSocket socket = new DatagramSocket();
	        InetAddress address = InetAddress.getByName(hostName);
	        File file = new File(qw); 

	        // Create a byte array to store the filestream
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

	            // Send the message
	            DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
	            socket.send(sendPacket);
	            System.out.println("Sent: Sequence number = " + sequenceNumber + ", Flag = " + lastMessageFlag);

	            // Sleep for 20 milliseconds to avoid sending too quickly
	            try {
	                Thread.sleep(20);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }

	        socket.close();
	        System.out.println("File " + qw + " has been sent");
	    }
	 
	 public static void get(int port) throws IOException
	 {
		 System.out.println("Ready to receive the file!");

		 Scanner sc= new Scanner(System.in);
		 //System.out.println("Please enter the path to be saved");
	        // Get the address, port and name of file to send over UDP
	        port = 3005;
	         String fileName =  "E:/Masters/sem1";
	        
	        DatagramSocket ds = new DatagramSocket(3007); 
	        byte[] receive = new byte[65535]; 
	  
	        DatagramPacket DpReceive = null; 
	        
	  
	            // Step 2 : create a DatgramPacket to receive the data. 
	            DpReceive = new DatagramPacket(receive, receive.length); 
	  
	            // Step 3 : revieve the data in byte buffer. 
	            ds.receive(DpReceive); 
	 
	           String v= data(receive).toString(); 
	           System.out.println(v);
	           ds.close();
	       String s2=fileName+v;
	        receiveAndCreate(port,s2);
	    }

	    public static void receiveAndCreate(int port, String fileName) throws IOException {
	        // Create the socket, set the address and create the file to be sent
	        DatagramSocket socket = new DatagramSocket(port);
	        InetAddress address;
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
	            socket.receive(receivedPacket);
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
	                socket.close();
	                lastMessage = false;
	                break;
	            }
	        }
	        
	        socket.close();
	        System.out.println("File " + fileName + " has been received.");
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
