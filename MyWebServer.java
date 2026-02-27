import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;

public class MyWebServer{
    public static void main(String[] args) throws Exception{
        if args.length < 2){
            System.err.println("Usage: java MyWebServer.java <port> <rootPath>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String rootPath = args[1];

        if(rootPath.startsWith("~")){
            rootPath = System.getProperty("user.home") + rootPath.substring(1);
        }
        if(rootPath.endsWith("/")){
            rootPath = rootPath.substring(0,rootPath.length() -1);
        }
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("MyWebServer started on port " + port + " serving from: " + rootPath);

        while(true){
            Socket clientSocket = serverSocket.accept();
            final String finalRootPath = rootPath;
            new Thread(() -> handleConnection(clientSocket, finalRootPath)).start();
        }
    }

    static void handleConnection(Socket socket, String rootPath){
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())){
            String requestLine;

            While(true){
                requestLine = in.readLine();
                if(requestLine == null){
                    break;
                }
                if (requestLine.trim().isEmpty()){
                    continue;
                }
                HTTPRequest request = new HTTPRequest(requestLine, in, rootPath);
                HTTPRequest response = new HTTPRequest(request);
                response.send(out);
            }

        }
        catch (Exception e){
            System.err.println("Connection closed: " + e.getMessage());
        }
    }





}



