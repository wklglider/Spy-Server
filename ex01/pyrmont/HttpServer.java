package ex01.pyrmont;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import javax.swing.JOptionPane;

public class HttpServer extends Thread {
	
	Socket sock;

	public HttpServer() {
		
	}
	
	public HttpServer(Socket ss) {
		sock = ss;
	}
	
	public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";

	// the shutdown command received
	public static boolean shutdown = false;
	public static String ssn = "";
	public static String serverAddress = JOptionPane.showInputDialog("Enter IP Address of a machine that is\nrunning the date service on port 9090:");
	// shutdown command
	public static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

	public static void main(String[] args) {
		HttpServer server = new HttpServer();
		server.await();
	}

	public void await() {
		ServerSocket serverSocket = null;
		int port = 8080;
		try {
			serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// Loop waiting for a request
		while (!shutdown) {
			try {
				Socket ss = serverSocket.accept();
				// Multiple threads
				new HttpServer(ss).start();
			} catch (IOException e) {
				System.out.println(e);
				continue;
			}
		}
	}
	
	@Override
	public void run() {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = sock.getInputStream();
			output = sock.getOutputStream();

			// create Request object and parse
			Request request = new Request(input);
			request.parse();
			
			// create Response object
			Response response = new Response(output);
			response.setRequest(request);
			response.sendStaticResource();

			// Close the socket
			sock.close();
			
			// Request from spyServer
			Socket s = new Socket(HttpServer.serverAddress, 9090);
	        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
	        String answer = br.readLine();
	        HttpServer.ssn = "<html><head><title>*spy</title></head><body>Secret: " + answer + "<br></body></html>";
	        
			// check if the previous URI is a shutdown command
			HttpServer.shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
	        s.close();
	        br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
