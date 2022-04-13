// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package ClientServer;

import Protocol.Messages;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;

public class ClientHandler implements Runnable{
    private String name;
    private final Socket socket;
    private final Server server;
    public BufferedReader in;
    public PrintWriter out;

    public ClientHandler(Socket socket, Server server, String name) {
        this.socket = socket;
        this.server = server;
        this.name = name;

        if (socket != null) { // for the tests
            try {
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream()), true);
            } catch (IOException e) {
                System.out.println("Error connecting to the server!");
                //e.printStackTrace();
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void run() {

        String message;

        try {
            message = in.readLine();

            while (message != null) {
                System.out.println("> [" + name + "] Incoming: " + message);
                handleCommand(message);
                out.flush();
                message = in.readLine();
            }
        } catch (IOException e) {
            shutDown();
        }
    }

    public void handleCommand(String message) {
        String[] split = message.split("~", 0);

        switch (split[0]) {
            case Messages.HELLO:
                if (split.length == 2) {
                    out.println(server.hello());
                } else {
                    out.println(Messages.ERROR + Messages.DELIMITER + "Invalid command");
                }
                break;
            case Messages.LOGIN:
                if (split.length == 2) {
                    String argument = split[1];
                    out.println(server.login(this, argument));
                } else {
                    out.println(Messages.ERROR + Messages.DELIMITER + "Invalid command");
                }
                break;
            case Messages.QUEUE:
                if (split.length == 1) {
                    server.queue(this);
                    server.checkQueue();
                } else {
                    out.println(Messages.ERROR + Messages.DELIMITER + "Invalid command");
                }
                break;

            case Messages.LIST:
                if (split.length == 1) {
                    out.println(server.list());
                    server.checkQueue();
                } else {
                    out.println(Messages.ERROR + Messages.DELIMITER + "Invalid command");
                }
                break;

            case Messages.MOVE:
                if (split.length >= 2) {
                    int command1 = 0;
                    int command2 = 0;

                    try {
                        // Parse the first move
                        command1 = Integer.parseInt(split[1]);

                        // If second move present parse that one as well
                        if (split.length == 3) {
                            command2 = Integer.parseInt(split[2]);
                        }

                        if (split.length < 3) { // if single move
                            server.singleMove(this, command1);
                        } else {
                            server.doubleMove(this, command1, command2);
                        }
                    } catch (NumberFormatException e) {
                        out.println(Messages.ERROR + Messages.DELIMITER + "Invalid command");
                        break;
                    }
                } else {
                    out.println(Messages.ERROR + Messages.DELIMITER + "Invalid command");
                }
                break;
            default:
                out.println(Messages.ERROR + Messages.DELIMITER + "Invalid command");
                break;
        }
    }

    public void shutDown() {
        try {
            System.out.println("Disconnecting due to issues with server...");
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error in disconnecting!");
        }

        server.removeClient(this);
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
