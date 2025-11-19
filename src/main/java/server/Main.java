import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;


public class Main {

    private static ConcurrentHashMap<String, String> dataStore = new java.util.concurrent.ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        System.err.println("Server starting...");
        

        

        int port = 9092;
        

        try (ServerSocket serverSocket = new ServerSocket(port)) {
          while(true){
            System.out.println("Waiting for client...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");
            ClientHandler handler = new ClientHandler(clientSocket,dataStore);
            handler.start();
            
          }

          

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }


}
