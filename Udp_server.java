import java.io.BufferedReader;
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
import java.util.Scanner;

public class Udp_server {
	static String link="";
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		
		
		
		
		int op=0;
		   BufferedReader q=new BufferedReader(new InputStreamReader(System.in));
		   
	       while(op!=5)
	       {
	       System.out.println("Please choose the following options/n 1)cd\n 2)ls \n 3)put \n 4)get\n5)exit");
	       String s1=q.readLine();
	       op=Integer.parseInt(s1);
	       
	      
	       switch(op)
	       {
	       case 1:
	       {
	       cd();
	       
	       
	       
	       break;
	       }
	       case 2:
	       {
	    	   ls();
	      
	       break;
	       }
	       case 3:
	       {
  
              put();
              
              
              break;
           } 
     
	       case 4:
	       {
	    	   get();
	    	  
	    	   break;
	    	   }
	       }
	       }
	       
	}
	
	public static  void cd() throws IOException
	{
		DatagramSocket ds = new DatagramSocket(1234); 
        byte[] receive = new byte[65535]; 
  
        DatagramPacket DpReceive = null; 
        
            DpReceive = new DatagramPacket(receive, receive.length); 
  
            ds.receive(DpReceive); 
 
           String v= data(receive).toString(); 
           System.out.println(v);
           File dir = new File(v);
           String n;
           if(dir.isDirectory()==true)
           {
               System.setProperty("user.dir", dir.getAbsolutePath());
               link=v;
              n= new String("changed");
           }
           else 
           {
               System.out.println(v + " is not a directory.");
              
               n= new String("cant be changed");
            }
           
       	DatagramSocket ds1 = new DatagramSocket(); 
        
        InetAddress ip = InetAddress.getLocalHost(); 
        byte buf[] = null; 
        buf = n.getBytes();
        DatagramPacket DpSend = 
                new DatagramPacket(buf, buf.length, ip, 1240); 
        
        ds1.send(DpSend); 
        ds1.close();
        ds.close();
        
	}
	
	public static void ls() throws IOException
	{
		DatagramSocket ds = new DatagramSocket(1600); 
        byte[] receive = new byte[65535]; 
  
        DatagramPacket DpReceive = null; 
        
  
            // Step 2 : create a DatgramPacket to receive the data. 
            DpReceive = new DatagramPacket(receive, receive.length); 
  
            // Step 3 : revieve the data in byte buffer. 
            ds.receive(DpReceive); 
 
           String v= data(receive).toString(); 
           //////
           File file = new File(v);
       	   String[] names = file.list();
           String send =new String();
       	   for(String name : names)
       	   {
       	       
       	           System.out.println(name);
       	           send=send+"##"+name;
       	       
       	   } 
           
       	DatagramSocket ds1 = new DatagramSocket(); 
        
        InetAddress ip = InetAddress.getLocalHost(); 
        byte buf[] = null; 
        buf = send.getBytes();
        DatagramPacket DpSend = 
                new DatagramPacket(buf, buf.length, ip, 1240); 
        ds1.send(DpSend);
        ds1.close();
        ds.close();
	}
	
	 public static void put() throws IOException
	    {
		 System.out.println("Ready to receive the file!");

	        // Get the address, port and name of file to send over UDP
	        final int port = 3005;
	         String fileName =  link;
	        
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

	 
	 public static void get() throws IOException
	 {
		 final String hostName = "127.0.0.1";
         final int port = 3005;
         String fileName = "";
         Scanner sc= new Scanner(System.in);
         
         fileName="E:/Masters/sem1/Software engineering/1.pdf";

        
         
         
        
         
         
         
         System.out.println("Enter the file format like : .pdf or .txt");
         String fileName1 = sc.nextLine();
         byte buf[] = null;
         buf = fileName1.getBytes();
         DatagramSocket ds = new DatagramSocket();
         DatagramPacket DpSend = 
                 new DatagramPacket(buf, buf.length,InetAddress.getByName(hostName), 3007);
         ds.send(DpSend);
         createAndSend(hostName, port, fileName);
         
         
         
         
         
         
         
     }
         
     public static void createAndSend(String hostName, int port, String fileName) throws IOException {
         System.out.println("Sending the file");

         // Create the socket, set the address and create the file to be sent
         DatagramSocket socket = new DatagramSocket();
         InetAddress address = InetAddress.getByName(hostName);
         File file = new File(fileName); 

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
         System.out.println("File " + fileName + " has been sent");
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
