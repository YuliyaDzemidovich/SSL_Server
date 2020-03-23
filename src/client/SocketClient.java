package client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SocketClient {
	private String host = "127.0.0.1";
	private int port = 8089;

	public static void main(String[] args) {
		SocketClient client = new SocketClient();
		client.start();
	}

	SocketClient() {
	}

	SocketClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	// Create the and initialize the SSLContext
	private SSLContext createSSLContext() {
		try {
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("mytestkeys.jks"), "keystorepassword".toCharArray());

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
			sslContext.init(km, tm, null);

			return sslContext;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	// Start to run the client
	public void start() {
		SSLContext sslContext = this.createSSLContext();

		try {
			// Create socket factory
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			// Create socket
			SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(this.host, this.port);

			System.out.println("SSL client started");

			sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

			try {
				// Start handshake
				sslSocket.startHandshake();

				// Get session after the connection is established
				SSLSession sslSession = sslSocket.getSession();

				System.out.println("SSLSession :");
				System.out.println("\tProtocol : " + sslSession.getProtocol());
				System.out.println("\tCipher suite : " + sslSession.getCipherSuite());

				// Start handling application content
				InputStream inputStream = sslSocket.getInputStream();
				OutputStream outputStream = sslSocket.getOutputStream();

				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));

				// Write data to server
				printWriter.println("C:\\Windows\\");
				printWriter.println(".log");
				printWriter.flush();

				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					System.out.println("Server response: " + line);

//                    Thread.sleep(10000);
					if (line.trim().equals("HTTP/1.1 200\r\n")) {
						break;
					}
				}
				sslSocket.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}