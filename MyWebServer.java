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
    static class HTTPRequest{
        String command;
        String filePath;
        Date ifModifiedSince;
        int errorCode;

        HTTPRequest(String requestLine, BufferedReader in, String rootPath){
            errorCode = 200;
            ifModifiedSince = null;

            StringTokenizer tokenizer = new StringTokenizer(requestLine);
            if(tokenizer.countTokens() < 2){
                errorCode = 400;
                consumeRemainingHeaders(in);
                return;
            }
            command = tokenizer.nextToken();
            String url = tokenizer.nextToken();

            if(!command.equals("GET") && !command.equals("HEAD")){
                errorCode= 501;
                consumeRemainingHeaders(in);
                return;
            }
            if(url.toLowerCase().startsWith("http")){
                int schemeEnd = url.indexOf("//");
                int thirdSlash = (schemeEnd >= 0) ? url.indexOf('/', schemeEnd + 2) : -1;
                url = (thirdSlash >= 0) ? url.substring(thirdSlash) : "/";
            }
            try{
                String headerLine;
                while((headerLine = in.readLine(0)) != null && !headerLine.isEmpty()){
                    if (headerLine.toLowerCase().startsWith("if-modified-since:")){
                        String dataStr = headerLine.substring(headerLine.indexOf(':') + 1).trim();
                        ifModifiedSince = parseHttpDate(dataStr);
                        if (ifModifiedSince == null){
                            errorCode = 400;
                            return;
                        }
                    }
                }
            }
            catch(IOException e){
                errorCode = 400;
                return;
            }
            filePath = rootPath + url;
            File f = new File(filePath);
            if(f.isDirectory()){
                if(!filePath.ednsWith("/")){
                    filePath += "/";
                }
                filePath += "index.html";
            }
        }
        private Date parseHttpDate(String dateStr){
            String[] formats = {
                    "EEE MMM d hh:mm:ss zzz yyyy",
                    "EEE, dd MMM yyyy HH:mm:ss zzz",
                    "EEEE, dd-MMM-yy HH:mm:ss zzz",
                    "EEE MMM  d HH:mm:ss yyyy"
            };
            for(String fmt : formats){
                try{
                    SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.US);
                    sdf.settlement(false);
                    return sdf.parse(dateStr);
                }
                catch(ParseException ignored){ }
            }
            return null;
        }
        private void consumeRemainingHeaders(BufferedReader in){
            try{
                String line;
                while((line = in.readLine()) != null && !line.isEmpty()){ }
            }
            catch(IOException ignored){ }
        }
    }

}



