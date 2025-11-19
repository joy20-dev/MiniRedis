import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
public class ClientHandler extends Thread{

    private Socket clientSocket;
    private ConcurrentHashMap<String, String> dataStore;

    public ClientHandler(Socket socket, ConcurrentHashMap<String, String> store) {
        this.clientSocket = socket;
        this.dataStore = store;
    }

    public void run(){
        try  {
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();
            String clientInfo = clientSocket.getRemoteSocketAddress().toString();

            String welcomeMsg = "Welcome to the server!\n" +
                                "Use the following commands:\n" +
                                "SET <key> <value>\n" +
                                "GET <key>\n" +
                                "DEL <key>\n" +
                                "EXIT\n";
            out.write(("$"+ welcomeMsg.length()+"\r\n"+welcomeMsg+"\r\n").getBytes());
            out.flush();




            boolean running = true;

            while (running) {
                byte[] buffer = new byte[1024];
                int read = in.read(buffer);

                if (read == -1) {
                    System.out.println("Client disconnected.");
                    break;
                }

                String message = new String(buffer, 0, read).trim();
                System.out.println("Received: " + message + " from " + clientInfo);

                
                
                String [] parts = message.split(" ");
                String command = parts[0].toLowerCase();
                String key = parts.length > 1 ? parts[1] : null;
                String value = parts.length > 2 ? parts[2] : null;

                switch(command){
                    case "set":
                        if(key !=null && value !=null){
                            dataStore.put(key,value);
                            out.write("+OK\r\n".getBytes());
                        }
                        else{
                            out.write("-ERR wrong number of arguments for 'set' command\r\n".getBytes());
                        }
                        out.flush();
                        break;
                    case "get":
                        if(key !=null){
                            if(dataStore.containsKey(key)){
                                String val = dataStore.get(key);
                                out.write(("$"+ val.length() + "\r\n" + val + "\r\n").getBytes());
                                
                            }
                            else{
                                out.write("$-1\r\n".getBytes());
                                
                            }
                        }
                        else{
                            out.write("-ERR wrong number of arguments for 'get' command\r\n".getBytes());
                        }
                        
                        out.flush();
                        break;
                    case "del":
                        if(key != null){
                            int removed = dataStore.containsKey(key) ? 1 : 0;
                            dataStore.remove(key);
                            out.write((":" + removed + "\r\n").getBytes()); // Integer reply
                        } else {
                            out.write("-ERR wrong number of arguments for 'del' command\r\n".getBytes());
                        }
                        out.flush();
                        break;
                    case "ping":
                        out.write("+PONG\r\n".getBytes());
                        out.flush();
                        break;
                    case "exit":
                        out.write("+Goodbye!\r\n".getBytes());
                        out.flush();
                        running = false;
                        break;
                    default:
                        out.write("-ERR unknown command\r\n".getBytes());
                        out.flush();
                }
            }

            clientSocket.close();
            System.out.println("client handler shutting down.");

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }

    }


}