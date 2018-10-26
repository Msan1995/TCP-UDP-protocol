package finaltcpclient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;





public class TCP_Client {
static String path="";
public final static int FILE_SIZE = 6022386;
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub

		
		int o=0;
		
		
			
		Socket s=new Socket("127.0.0.1",3302);
	    System.out.println("connected");
	    OutputStream output = s.getOutputStream();
	    PrintWriter writer = new PrintWriter(output, true);
	    Scanner s5= new Scanner(System.in);
	    
	   
		
	     //  BufferedReader q=new BufferedReader(new InputStreamReader(System.in));
	      Scanner y = new Scanner(System.in);
	    
	       while(o!=5)
	       {
	       System.out.println("Please choose the following options/n 1)cd/n 2)ls /n 3)put /n 4)get");
	       
		    String getstring=s5.nextLine();
		    writer.println(getstring);
	        //o=Integer.parseInt(q.readLine());
	     //   o=y.nextInt();      
	       o=Integer.parseInt(getstring);
//	        String s1=reader.readLine();
//	        System.out.println(s1);   
	       switch(o)
	       {
	       case 1:
	    	   int port=3007;
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
	       
	       y.close();
	       s.close();
	       s5.close();
	       
	}
	
	  public static  void cd(int port) throws UnknownHostException, IOException
	   	
      {
		  Socket s1 = new Socket(InetAddress.getLocalHost(),port);
   	   
   	   System.out.println("connected to cd");
	       OutputStream output = s1.getOutputStream();
	       PrintWriter writer = new PrintWriter(output, true);
	       BufferedReader bf=new BufferedReader(new InputStreamReader(System.in));
   	   System.out.println("Please enter the path");
	       path=bf.readLine();
	       writer.println(path);

	     
	     
	      s1.close();
      }
	  public static  void ls(int port) throws UnknownHostException, IOException
	  {
		  Socket s2 = new Socket(InetAddress.getLocalHost(),port);
   	   OutputStream output1 = s2.getOutputStream();
          PrintWriter writer1 = new PrintWriter(output1, true);
          writer1.println(path);

          InputStream input1 = s2.getInputStream();

          BufferedReader reader1 = new BufferedReader(new InputStreamReader(input1));
          String s12=reader1.readLine();
          System.out.println(s12);
          String[] s21=s12.split("@");
          for(String s3 :s21)
          {
       	   System.out.println(s3);
          }
          s2.close();
	  }
	  public static  void put(int port) throws UnknownHostException, IOException
	  {
		  BufferedReader br = null;
          try {
              
              System.out.println("Connected to server");
              Scanner scanner = new Scanner(System.in);
              String file_name = scanner.nextLine();
              
              File file = new File(file_name);
              Socket s3 = new Socket(InetAddress.getLocalHost(), port);
              ObjectInputStream ois = new ObjectInputStream(s3.getInputStream());
              ObjectOutputStream oos = new ObjectOutputStream(s3.getOutputStream());
              OutputStream output = s3.getOutputStream();
              PrintWriter writer = new PrintWriter(output, true);
              
              int i=file_name.lastIndexOf("/");
              
              String kk=file_name.substring(i+1, file_name.length());
              System.out.println("extracted file name "+ kk);
              
              
              
              writer.println(kk);
             
       
              oos.writeObject(file.getName());
       
              FileInputStream fis = new FileInputStream(file);
              byte [] buffer = new byte[100];
              Integer bytesRead = 0;
       
              while ((bytesRead = fis.read(buffer)) > 0) {
                  oos.writeObject(bytesRead);
                  oos.writeObject(Arrays.copyOf(buffer, buffer.length));
              }
               
              s3.close();
              fis.close();ois.close();
              
	  }
          
          
          finally {
			
		}
	}
	  public static  void get(int port) throws IOException
      {
		  int bytesRead;
		    int current = 0;
		    FileOutputStream fos = null;
		    BufferedOutputStream bos = null;
		    Socket sock = null;
		    try {
		    	Scanner s= new Scanner(System.in);
		    	System.out.println("enter file name you want to download");
		    	String y=s.nextLine();
		    	
		      sock = new Socket(InetAddress.getLocalHost(), 4000);
		      
		      OutputStream output = sock.getOutputStream();
		      PrintWriter writer = new PrintWriter(output, true);
		      writer.println(y);
		      
		      
		      System.out.println("Connecting...");
		      BufferedReader q1=new BufferedReader(new InputStreamReader(System.in));
		      System.out.println("Enter the path to be saved");
		      String x=q1.readLine();
		      // receive file
		      byte [] mybytearray  = new byte [FILE_SIZE];
		      InputStream is = sock.getInputStream();
		      fos = new FileOutputStream(x);
		      bos = new BufferedOutputStream(fos);
		      bytesRead = is.read(mybytearray,0,mybytearray.length);
		      current = bytesRead;

		      do {
		         bytesRead =
		            is.read(mybytearray, current, (mybytearray.length-current));
		         if(bytesRead >= 0) current += bytesRead;
		      } while(bytesRead > -1);

		      bos.write(mybytearray, 0 , current);
		      bos.flush();
		      System.out.println("File " + x
		          + " downloaded (" + current + " bytes read)");
		    
		    }
		    finally {
		      if (fos != null) fos.close();
		      if (bos != null) bos.close();
		      if (sock != null) sock.close();
		     
		    }
      }
}


