package server;
// An example class that uses the secure server socket class

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;


public class SecureAdditionServer {
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "./src/server/LIUkeystore.ks";
	static final String TRUSTSTORE = "./src/server/LIUtruststore.ks";
	static final String KEYSTOREPASS = "123456";
	static final String TRUSTSTOREPASS = "abcdef";
	
	PrintWriter out;
	BufferedReader in;
	
	/** Constructor
	 * @param port The port where the server
	 *    will listen for requests
	 */
	SecureAdditionServer( int port ) {
		this.port = port;
	}
	
	/** The method that does the work for the class */
	public void run() {
		try {
			
			//Load Keystores
			KeyStore ks = KeyStore.getInstance( "JCEKS" );
			ks.load( new FileInputStream( KEYSTORE ), KEYSTOREPASS.toCharArray() );
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), TRUSTSTOREPASS.toCharArray() );
			
			//Setup key and trust managers
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, KEYSTOREPASS.toCharArray() );
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			
			//Setup a SSL server
			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket( port );
			sss.setNeedClientAuth(true); //Importante!
			sss.setEnabledCipherSuites( sss.getSupportedCipherSuites() );
			System.out.println("\n>>>> SecureAdditionServer: active ");
			
			//Prepare the incoming connections
			SSLSocket incoming = (SSLSocket)sss.accept();
			in = new BufferedReader( new InputStreamReader( incoming.getInputStream() ) );
			out = new PrintWriter( incoming.getOutputStream(), true);			
				
			String file_name;
			int chosen = Integer.parseInt(in.readLine());
			
			switch(chosen) {
			case 1: 
				file_name = in.readLine();
				deleteFile(file_name);
				in.close();
				break;
			case 2:
				file_name = in.readLine();
				download(file_name, out);
				in.close();
				out.flush();
				out.close();
				break;
			case 3:
				file_name = in.readLine();
				uploadFile(file_name, in);		
				in.close();		
				break;
			default:
				System.out.println("BAD");
			
			}
		}catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}	
	}
	
	//delete a file
	private void deleteFile(String fileName) {
			File tempFile = new File(fileName);
			tempFile.delete();
			out.println("File was deleted");	
	}
	
	 public void download(String filename, PrintWriter out) throws IOException{
	       
		 	BufferedReader buffRead = null;
		 	String sCurr = "";
		 	String result = "";

	        try
	        {
	        	//Read the file 
	        	buffRead = new BufferedReader(new FileReader(filename));
	        	StringBuilder stringBuild = new StringBuilder();
	        	
	        	//While more info to read
	        	while((sCurr = buffRead.readLine()) != null) {
	        		stringBuild.append(sCurr);
	        		stringBuild.append(System.lineSeparator());
	        	}  
	        	//Save result 
	        	result = stringBuild.toString();
	        }
	        catch( NumberFormatException nfe ){
				out.println( "Contains invalid nmbr" );
			}
	        //Send result
	        out.println(result);
	    }

	
	public void uploadFile(String fileName, BufferedReader in) throws IOException {
		
		String sCurrent = "";
		String result = "";
	
		try {
			
			
			StringBuilder stringBuild = new StringBuilder();
			
			//Read incoming 
			while((sCurrent = in.readLine()) != null) { 
				stringBuild.append(sCurrent);
				stringBuild.append(System.lineSeparator());
				out.close(); //need this here, because reasons 
			}
			
			//Create new string from result
			result = stringBuild.toString();
			
			//Create new file
			String FileName = "upload/"+fileName;
			System.out.println(result);
		    System.out.println(FileName);
		   		    PrintWriter writer;
		    
		    writer = new PrintWriter(FileName, "UTF-8");
            writer.print(result);
            
            //Close printwriter
            writer.flush();
            writer.close();
		
		}catch( NumberFormatException nfe ){
			System.out.println( "Contains invalid nmbr" );
		}
		 
	}

	
	/** The test method for the class
	 * @param args[0] Optional port number in place of
	 *        the default
	 */
	public static void main( String[] args ) {
		int port = DEFAULT_PORT;
		if (args.length > 0 ) {
			port = Integer.parseInt( args[0] );
		}
		SecureAdditionServer addServe = new SecureAdditionServer( port );
		addServe.run();
	}
}

