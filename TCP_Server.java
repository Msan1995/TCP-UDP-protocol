package finaltcpserver;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;



public class TCP_Server extends Thread {
	
 static int i=0;
   static String link="";
  
	public static void main(String[] args) throws Exception {
		
	///////////new one 	 
		// TODO Auto-generated method stub
		ServerSocket server= new ServerSocket(3302);
        Socket s =server.accept();
      System.out.println("connect1");
      OutputStream output = s.getOutputStream();
      PrintWriter writer = new PrintWriter(output, true);
      InputStream input = s.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(input));

	  
      
      
      
      
   ///////////   
		  int op=0;
		  int port =3007;
		  int port2 =3009;
		  int port3 =3022;
	      
	       while(op!=5)
	       {
	       System.out.println("Please choose the following options/n 1)cd\n 2)ls \n 3)put \n 4)get\n5)exit");
	       String s1=reader.readLine();
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
	       server.close();
	       s.close();
	}
	
	
	
	public static  void cd() throws IOException
	{
		ServerSocket smain= new ServerSocket(3007);
		
        Socket s1main =smain.accept();
        System.out.println("connected for cd");
        OutputStream output = s1main.getOutputStream();
      PrintWriter writer = new PrintWriter(output, true);
      InputStream input = s1main.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(input));
      String s2=reader.readLine();
      System.out.println(s1main);
      File dir = new File(s2);
      String g="";
      if(dir.isDirectory()==true)
      {
          System.setProperty("user.dir", dir.getAbsolutePath());
          link=dir.toString();
          System.out.println("connected to cd");
      }
      else 
      {
          s2=s2+ " is not a directory.";
          System.out.println(s2);
      } 
        
        s1main.close();
        smain.close();
        writer.close();
	}
	public static  void ls() throws IOException
	{
		 ServerSocket server1= new ServerSocket(3009);
	        Socket s21 =server1.accept();
	      System.out.println("connected to ls");
	      OutputStream output1 = s21.getOutputStream();
	      PrintWriter writer1 = new PrintWriter(output1, true);
	      

	      InputStream input1 = s21.getInputStream();

	      BufferedReader reader1 = new BufferedReader(new InputStreamReader(input1));
	      String s12=reader1.readLine();
	      System.out.println("ls from"+s12);
	      
	      File file = new File(s12);
		   String[] names = file.list();
	   String send =new String();
		   for(String name : names)
		   {
		       
		           System.out.println(name);
		           send=send+"@"+name;
		       
		   } 
	   writer1.println(send);
	   
	   server1.close();
	   s21.close();
	}
	
	public static  void put() throws Exception
	{
		ServerSocket server3 = new ServerSocket(3022);
        Socket s3 = server3.accept();
        ObjectOutputStream oos = new ObjectOutputStream(s3.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(s3.getInputStream());
        FileOutputStream fos = null;
    //    InputStream input = s3.getInputStream();

      //  BufferedReader reader = new BufferedReader(new InputStreamReader(input));
       // String s1=reader.readLine();
        //System.out.println(s1);
        
        
      
        byte [] buffer = new byte[100];
        Object o = ois.readObject();
        i++;
        System.out.println("Connected to client for put");
        try{
        //	System.out.println(link+s1);
        fos = new FileOutputStream(link+"/"+"1.docx");
        }catch (Exception e) {
			System.out.println("Wrong path declared");
		}
        
      
        
        Integer bytesRead = 0;
        
        do {
            o = ois.readObject();
            if (!(o instanceof Integer)) {
                throwException("Something is wrong");
            }
            
            
            bytesRead = (Integer) o;
 
            o = ois.readObject(); 
            
            if (!(o instanceof byte[])) {
                throwException("Something is wrong");
            }
            buffer = (byte[])o;
            fos.write(buffer, 0, bytesRead);
        } while (bytesRead == 100);
            server3.close();
            s3.close();fos.close();oos.close();

	}
	
//	
	
public static  void get() throws IOException
	{
	FileInputStream fis = null;
    BufferedInputStream bis = null;
    OutputStream os = null;
    ServerSocket servsock = null;
    Socket sock = null;
    try {
      servsock = new ServerSocket(4000);
       {
        System.out.println("Waiting...");
        try {
          sock = servsock.accept();
          System.out.println("Accepted connection : " + sock);
          InputStream input = sock.getInputStream();

          BufferedReader reader = new BufferedReader(new InputStreamReader(input));
          String y=reader.readLine();
          System.out.println("path from client"+y);
          // send file
          File myFile = new File (link+"/"+y);
          byte [] mybytearray  = new byte [(int)myFile.length()];
          fis = new FileInputStream(myFile);
          bis = new BufferedInputStream(fis);
          bis.read(mybytearray,0,mybytearray.length);
          os = sock.getOutputStream();
          System.out.println("Sending " + link + "(" + mybytearray.length + " bytes)");
          os.write(mybytearray,0,mybytearray.length);
          os.flush();
          System.out.println("Done.");
        }
        finally {
          if (bis != null) bis.close();
          if (os != null) os.close();
          if (sock!=null) sock.close();
        }
      }
    }
    finally {
      if (servsock != null) servsock.close();
    }
	}
	
	private static void throwException(String message) throws Exception {
		// TODO Auto-generated method stub
		throw new Exception(message);
	
	}
}	


