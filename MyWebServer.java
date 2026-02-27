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



