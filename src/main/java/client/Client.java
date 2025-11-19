import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {

        try (Socket socket = new Socket("localhost", 9092);
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream();
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Connected to server");
            byte [] buffer = new byte[1024];
            int read = in.read(buffer);
            String welcomeMsg = new String(buffer, 0, read).trim();
            System.out.println( welcomeMsg);

            while (true) {
                System.out.print("Enter message: ");
                String message = sc.nextLine().trim();
                out.write((message + "\r\n").getBytes());
                out.flush();

                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Client exiting...");
                    break;
                }

                // Peek the first byte to determine response type
                int firstByte = in.read();
                if (firstByte == -1) {
                    System.out.println("Server disconnected");
                    break;
                }
                

                switch (firstByte) {
                    case '+':
                        System.out.println("Server: " + readLine(in));
                        break;
                    case '-':
                        System.out.println("Server Error: " + readLine(in));
                        break;
                    case ':':
                        System.out.println("Server Integer: " + readLine(in));
                        break;
                    case '$':
                        System.out.println("Server Bulk: " + readBulkString(in));
                        break;
                    default:
                        System.out.println("Unknown response from server");
                        break;
                }
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\r') {
                if (in.read() == '\n') break;
            } else {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }

    private static String readBulkString(InputStream in) throws IOException {
        // Read the length first
        String lenStr = readLine(in);
        int len = Integer.parseInt(lenStr);
        if (len == -1) return null; // key does not exist

        byte[] data = new byte[len];
        int totalRead = 0;
        while (totalRead < len) {
            int read = in.read(data, totalRead, len - totalRead);
            if (read == -1) throw new IOException("Unexpected end of stream");
            totalRead += read;
        }

        // Consume trailing \r\n
        in.read();
        in.read();

        return new String(data);
    }
}
