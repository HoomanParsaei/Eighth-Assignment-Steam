package Server;

import Shared.Response;
import netscape.javascript.JSObject;
import org.cef.handler.CefClientHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMain {
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler>clients = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        ServerMain server = new ServerMain(2345);
        server.start();
    }
    public void start(){
        System.out.println("Server started.");
        while(true){
            try{
                Socket socket=serverSocket.accept();
                System.out.println("New client connected: " + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.start();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public ServerMain(int portNumber) throws IOException {
        this.serverSocket = new ServerSocket(portNumber);
    }

    private class ClientHandler extends Thread{
        private Socket socket;
        private BufferedReader read;
        private PrintWriter out;
        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }
        public void run(){
            String inputLine;
            try{
                inputLine=read.readLine();
                while(inputLine!=null){
                    System.out.println("Received message from " + socket.getRemoteSocketAddress() + ": " + inputLine);
//                    broadcast(inputLine);
                    JSONObject json=new JSONObject(inputLine);
                    switch (json.getString("Command")){
                        case "Login":
                            out.println(Response.Login(json));
                            break;
                        case "SignUp":
                            out.println(Response.SignUp(json));
                            break;
                        case "List of available games":

                    }
                    inputLine=read.readLine();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                    clients.remove(this);
                    System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        public void broadcast(String message){
            for(ClientHandler client : clients){
                client.out.println(message);
            }
        }
    }
}