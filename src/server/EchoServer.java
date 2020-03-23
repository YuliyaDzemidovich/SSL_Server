package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EchoServer {
	static int numberOfClientsNow = 0;
	final static Logger log = LoggerFactory.getLogger(EchoServer.class);
	
	 private int port = 8089;
	    private boolean isServerDone = false;
	     
	    public static void main(String[] args){
	    	EchoServer server = new EchoServer();
	        server.run();
	    }
	     
	    EchoServer(){      
	    }
	     
	    EchoServer(int port){
	        this.port = port;
	    }
	     
	    // Create the and initialize the SSLContext
	    private SSLContext createSSLContext(){
	        try{
	            KeyStore keyStore = KeyStore.getInstance("JKS");
	            keyStore.load(new FileInputStream("mytestkeys.jks"),"keystorepassword".toCharArray());
	             
	            // Create key manager
	            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
	            keyManagerFactory.init(keyStore, "keystorepassword".toCharArray());
	            KeyManager[] km = keyManagerFactory.getKeyManagers();
	             
	            // Create trust manager
	            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
	            trustManagerFactory.init(keyStore);
	            TrustManager[] tm = trustManagerFactory.getTrustManagers();
	             
	            // Initialize SSLContext
	            SSLContext sslContext = SSLContext.getInstance("TLSv1");
	            sslContext.init(km,  tm, null);
	             
	            return sslContext;
	        } catch (Exception ex){
	            ex.printStackTrace();
	        }
	         
	        return null;
	    }
	     
	    // Start to run the server
	    public void run(){
	        SSLContext sslContext = this.createSSLContext();
	         
	        try{
	            // Create server socket factory
	            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
	             
	            // Create server socket
	            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port);
	             
	            log.info("SSL server started");
	            while(!isServerDone){
	                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
	                 
	                // Start the server thread
	                numberOfClientsNow++;
	                log.info("Server's number of clients increased to " + numberOfClientsNow);
	                new ServerThread(sslSocket).start();
	            }
	        } catch (Exception ex){
	            ex.printStackTrace();
	        }
	    }
	     
	    // Thread handling the socket from client
	    static class ServerThread extends Thread {
	        private SSLSocket sslSocket = null;
	         
	        ServerThread(SSLSocket sslSocket){
	            this.sslSocket = sslSocket;
	        }
	         
	        public void run(){
	            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
	             
	            try{
	                // Start handshake
	                sslSocket.startHandshake();
	                 
	                // Get session after the connection is established
	                SSLSession sslSession = sslSocket.getSession();
	                 
	                System.out.println("Server SSLSession :");
	                System.out.println("\tProtocol : " + sslSession.getProtocol());
	                System.out.println("\tCipher suite : " + sslSession.getCipherSuite());
	                 
	                // Start handling application content
	                InputStream inputStream = sslSocket.getInputStream();
	                OutputStream outputStream = sslSocket.getOutputStream();
	                 
	                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
	                 
	                String line = null;
	                ArrayList<String> params = new ArrayList<String>();
	                while((line = bufferedReader.readLine()) != null){
	                    System.out.println("Server received data: " + line);
	                    params.add(line);
	                     
	                    if(line.trim().isEmpty()){
	                        break;
	                    }
	                    
	                    if(params.size() >= 2) {
	                    	proceedFileExtensionSearch(params, printWriter);
	                    	break;
	                    }
	                }
	                 
	                // Write data
	                printWriter.print("HTTP/1.1 200\r\n");
	                printWriter.flush();
	                 
                	numberOfClientsNow--;
	                log.info("Server's number of clients downgraded to " + numberOfClientsNow);
	                sslSocket.close();
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }

			private void proceedFileExtensionSearch(ArrayList<String> params, PrintWriter printWriter) {
				File dir = new File(params.get(0));
				String fileExtension = params.get(1);
				FilenameFilter filter = new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(fileExtension);
					}
				};
				String [] filesFound = dir.list(filter);
				if (filesFound == null || filesFound.length == 0) {
					printWriter.println("Files with extension " + fileExtension + " in directory " + dir + " not found");
					return;
				} else {
					for (int i = 0; i < filesFound.length; i++) {
						printWriter.println(filesFound[i]);
					}
					return;
				}
			}
	    }
}
