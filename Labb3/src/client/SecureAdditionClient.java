package client;

// A client-side class that uses a secure TCP/IP socket

import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;
import java.util.StringTokenizer;



public class SecureAdditionClient {
	private InetAddress host;
	private int port;
	// This is not a reserved port number 
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "./src/client/LIUkeystore.ks";
	static final String TRUSTSTORE = "./src/client/LIUtruststore.ks";
	static final String KEYSTOREPASS = "123456";
	static final String TRUSTSTOREPASS = "abcdef";
  
	
	// Constructor @param host Internet address of the host where the server is located
	// @param port Port number on the host where the server is listening
	public SecureAdditionClient( InetAddress host, int port ) {
		this.host = host;
		this.port = port;
	}
	
  // The method used to start a client object
	public void run() {
		try {
			
			//Load Keystores
			KeyStore ks = KeyStore.getInstance( "JCEKS" );
			ks.load( new FileInputStream( KEYSTORE ), KEYSTOREPASS.toCharArray() );
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), TRUSTSTOREPASS.toCharArray() );
			
			//Setup Key and Trust managers
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, KEYSTOREPASS.toCharArray() );
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			
			//Setup SSL
			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			SSLSocketFactory sslFact = sslContext.getSocketFactory();      	
			SSLSocket client =  (SSLSocket)sslFact.createSocket(host, port);
			client.setEnabledCipherSuites( client.getSupportedCipherSuites() );
			System.out.println("\n>>>> SSL/TLS handshake completed");

			//Setup Transmissions
			BufferedReader socketIn;
			socketIn = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
			PrintWriter socketOut = new PrintWriter( client.getOutputStream(), true );
			
			
			System.out.println("Hello! What do you want to do?");
		    System.out.println("1. Delete file?");
		    System.out.println("2. Download file?");
		    System.out.println("3. Upload file?");
		    
		    String userInput = new BufferedReader(new InputStreamReader(System.in)).readLine();
		    int chosen = Integer.parseInt(userInput);

			socketOut.println(chosen);		
			
			String upFilename = "Text.txt";
			String downFilename = "Text2.txt";
			
			socketOut.flush();
			
			switch(chosen) {
			//Deleting
			case 1:
				System.out.println("Deleting File");
				socketOut.flush();
				socketOut.println(upFilename);            
				break;
			//Download
			case 2:
				System.out.println("Downloading File");
				socketOut.println(downFilename);
				sendFile(downFilename, socketIn);
				break;
			//Upload
			case 3:
				System.out.println("Uploading File");
				socketOut.println(upFilename);
				String result = readFile(upFilename);
				socketOut.println(result);
				break;
			}
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	public static String readFile(String fileName) throws IOException {
		
		String sCurrent = "";
		String result = "";
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		
		try {
			//StringBuilder = Mutable sequence of characters
			StringBuilder stringBuild = new StringBuilder();

			//Read file while more to read
			while((sCurrent=br.readLine()) != null) {
				stringBuild.append(sCurrent);
				stringBuild.append(System.lineSeparator());
			}
			result = stringBuild.toString();
			System.out.println(result);
			
		}finally{
			//close the bufferedreader
           br.close();
        }
		
		//return the result
		return result;
	}
	
	public static void sendFile(String fileName, BufferedReader socketIn) throws IOException{
			
		String sCurrent = null;
		String result = null;
		
		try {
			StringBuilder stringBuild = new StringBuilder();
			
			//Read incoming data
			while((sCurrent = socketIn.readLine()) != null) { 
				stringBuild.append(sCurrent);
				stringBuild.append(System.lineSeparator());
			}
			//Loaded data
			result = stringBuild.toString();
			
			//Creates a new file for the loaded data
			File file = null;
			file = new File("download/"+fileName);
			
			if(!file.exists()) {
				file.createNewFile();
			}
			
			
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			BufferedWriter buffWriter = new BufferedWriter(fileWriter);
			buffWriter.write(result);
			buffWriter.close();
			
		}catch( NumberFormatException nfe ){
			System.out.println( "Sorry, your list"
					  + "contains an"
					  + "invalid nuber" );
		}
	}
				
	
	// The test method for the class @param args Optional port number and host name
	public static void main( String[] args ) {
		try {
			InetAddress host = InetAddress.getLocalHost();
			int port = DEFAULT_PORT;
			if ( args.length > 0 ) {
				port = Integer.parseInt( args[0] );
			}
			if ( args.length > 1 ) {
				host = InetAddress.getByName( args[1] );
			}
			SecureAdditionClient addClient = new SecureAdditionClient( host, port );
			addClient.run();
		}
		catch ( UnknownHostException uhx ) {
			System.out.println( uhx );
			uhx.printStackTrace();
		}
	}
}
