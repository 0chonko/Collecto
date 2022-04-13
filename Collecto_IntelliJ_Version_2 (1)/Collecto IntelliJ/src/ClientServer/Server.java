// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package ClientServer;

import Board.Board;
import Game.Game;
import Protocol.Messages;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.IOException;
import java.net.InetAddress;

public class Server implements Runnable {
    private ServerSocket serverSock;
    public List<ClientHandler> clients;
    public List<ClientHandler> queue;
    public int port;

    private int clientNum = 0;

    private final String SERVER_NAME = "Fil and German's Collecto Server";

    private Map<ClientHandler, ClientHandler> C2CMap; // Client to Client map for a pair of players
    private Map<Set<ClientHandler>, Board> P2BMap; // Client pair to board map for a player to a board

    private List<Game> games;

    public Server() {
        clients = new ArrayList<>();
        games = new ArrayList<>();
        queue = new ArrayList<>();
        C2CMap = new HashMap<>();
        P2BMap = new HashMap<>();
    }

    @Override
    public void run() {
        boolean openNewSocket = true;

        while (openNewSocket) {
            try {
                setup();

                while (true) {
                    // Open the server socket to accept incoming client connections
                    Socket sock = serverSock.accept();

                    // Create a client handler for the connected client
                    clientNum++;
                    System.out.println("Client " + clientNum + " connecting...");
                    ClientHandler handler = new ClientHandler(sock, this, "John Doe");
                    new Thread(handler).start();
                    clients.add(handler);
                }
            } catch (IOException e) {
                System.out.println("A server IO error occurred: "
                        + e.getMessage());

                openNewSocket = false;
            }
        }
    }

    private void setup() {
        serverSock = null;
        Scanner sc = new Scanner(System.in);

        // Try to start a server at the port that was input
        while (serverSock == null) {
            System.out.println("Please enter a server port for your server: ");

            String input = "placeholder";

            if (port == 0) {
                input = sc.nextLine();
            }

            try {
                if (port == 0) {
                    port = Integer.parseInt(input);
                }
            } catch (NumberFormatException e) {
                System.out.println("This is not a valid port!");
                continue;
            }

            if (port <= 1024) {
                System.out.println("This is not a valid port!");
                port = 0;
                continue;
            }

            try {
                System.out.println("Trying to open server at port " + port + "...");
                serverSock = new ServerSocket(port, 0,
                        InetAddress.getByName("127.0.0.1"));
                System.out.println("Server started at port " + port + "!");
            } catch (IOException e) { // If not possible because port is busy, ask for new port
                System.out.println("Port " + port + " busy! Please enter another port: ");
                port = 0;
            }
        }
    }

    public void removeClient(ClientHandler client) {
        this.clients.remove(client);
    }

    public String hello() {
        return Messages.HELLO + Messages.DELIMITER + SERVER_NAME;
    }

    public String login(ClientHandler client, String name) {
        // Check to see if name is contained in the current client list
        if (!clients.isEmpty()) {
            for (ClientHandler c : clients) {
                if (c.getName().equals(name)) {
                    return Messages.ALREADYLOGGEDIN;
                }
            }
        }

        client.setName(name);
        return Messages.LOGIN;
    }

    public void queue(ClientHandler client) {
        if (!queue.isEmpty() && queue.contains(client)) { // client already in queue
            queue.remove(client);
        } else {
            queue.add(client);
        }
    }

    public void checkQueue() {
        if (queue.size() >= 2) {
            ClientHandler c1 = queue.get(0);
            ClientHandler c2 = queue.get(1);

            // Remove clients from the queue
            queue.remove(c1);
            queue.remove(c2);

            gameStart(c1, c2, null);
        }
    }

    public void clearQueue() {
        queue.removeAll(queue);
    }

    public void clearClients() {
        clients.removeAll(clients);
    }

    public String list() {
        String list = Messages.LIST;

        if (!clients.isEmpty()) {
            for (ClientHandler c : clients) {
                list += "~" + c.getName();
            }

            return list;
        }

        return list;
    }

    void gameStart(ClientHandler c1, ClientHandler c2, Board board) {
        // Initialize a new board for the 2 clients
        if (board == null) {
            board = new Board();
            board.initBoard();
        }

        // Initialize a new game object with those two clients
        Game game = new Game(c1, c2, board);
        games.add(game);

        // Send new game to the client handlers
        int[] initBoard = board.rowBoard;

        String message = Messages.NEWGAME;
        for (int i = 0; i < initBoard.length; i++) {
            message += Messages.DELIMITER + initBoard[i];
        }

        System.out.println(board.toString());

        if (clients.contains(c1)) {
            c1.sendMessage(message + Messages.DELIMITER + c1.getName() + Messages.DELIMITER + c2.getName());
        } else {
            System.out.println("Issues sending to client " + c1.getName());
        }

        if (clients.contains(c2)) {
            c2.sendMessage(message + Messages.DELIMITER + c1.getName() + Messages.DELIMITER + c2.getName());
        } else {
            System.out.println("Issues sending to client " + c2.getName());
        }
    }

    public void singleMove(ClientHandler c1, int command) {
        ClientHandler c2 = null;
        Game game = null;
        Board b = null;

        // Find the matching client / game
        for (Game g : games) {
            if (g.hasClient(c1)) {
                int index = g.clientIndex(c1);

                if (index == 0) {
                    c2 = g.getClient(1);
                } else if (index == 1) {
                    c2 = g.getClient(0);
                }

                game = g;

                System.out.println("Found client / game!");
            }
        }

        // Get the board
        if (game == null) {
            System.out.println("Unable to find the board!");
        } else {
            b = game.getBoard();
            System.out.println("Found matching board!");
        }

        // Update the server board if the move is valid
        if (b.singleMoveValid(command)) {
            b.moveLine(command);
            game.updateBalls(c1, b.removeBalls());

            // Check if the game is over
            if (b.gameOver()) {
                //TODO: move to own method
                int s1 = game.getScore(c1);
                int s2 = game.getScore(c2);

                if (s1 == s2) {
                    c1.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.DRAW);
                    c2.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.DRAW);
                } else {
                    ClientHandler victor;

                    if (s1 > s2) {
                        victor = c1;
                    } else {
                        victor = c2;
                    }

                    c1.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.VICTORY
                            + Messages.DELIMITER + victor.getName());
                    c2.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.VICTORY
                            + Messages.DELIMITER + victor.getName());
                }

                games.remove(game);

                System.out.println("Game is over!");
            } else {
                if (clients.contains(c1)) {
                    c1.sendMessage(Messages.MOVE + Messages.DELIMITER + command);
                } else if (clients.contains(c2)) {
                    c2.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.DISCONNECT);
                    games.remove(game);
                    System.out.println("Client disconnected!");
                }

                if (clients.contains(c2)) {
                    c2.sendMessage(Messages.MOVE + Messages.DELIMITER + command);
                } else if (clients.contains(c1)) {
                    c1.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.DISCONNECT);
                    games.remove(game);
                    System.out.println("Client disconnected!");
                }

                System.out.println("Move: " + command + " sent to the client " + c1.getName());
                System.out.println("Move: " + command + " sent to the client " + c2.getName());
            }
        } else {
            System.out.println("Unable to send move! (move invalid)");
        }
    }

    public void doubleMove(ClientHandler c1, int command1, int command2) {
        ClientHandler c2 = null;
        Game game = null;
        Board b = null;

        // Find the matching client / game
        for (Game g : games) {
            if (g.hasClient(c1)) {
                int index = g.clientIndex(c1);

                if (index == 0) {
                    c2 = g.getClient(1);
                } else if (index == 1) {
                    c2 = g.getClient(0);
                }

                game = g;

                System.out.println("Found client / game!");
            }
        }

        // Get the board
        if (game == null) {
            System.out.println("Unable to find the board!");
        } else {
            b = game.getBoard();
            System.out.println("Found matching board!");
        }

        // Update the server board if the move is valid
        if (b.doubleMoveValid(command1, command2)) {
            //TODO: move to own method
            b.moveLine(command1);
            b.moveLine(command2);

            game.updateBalls(c1, b.removeBalls());

            // Check if the game is over
            if (b.gameOver()) {
                int s1 = game.getScore(c1);
                int s2 = game.getScore(c2);

                if (s1 == s2) {
                    c1.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.DRAW);
                    c2.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.DRAW);
                } else {
                    ClientHandler victor;

                    if (s1 > s2) {
                        victor = c1;
                    } else {
                        victor = c2;
                    }

                    c1.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.VICTORY
                            + Messages.DELIMITER + victor.getName());
                    c2.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.VICTORY
                            + Messages.DELIMITER + victor.getName());
                }

                games.remove(game);

                System.out.println("Game over!");
            } else {
                if (clients.contains(c1)) {
                    c1.sendMessage(Messages.MOVE + Messages.DELIMITER + command1 + Messages.DELIMITER + command2);
                    System.out.println("Move: " + command1 + ", " + command2 + " sent to the client " + c1.getName());
                } else if (clients.contains(c2)) {
                    c2.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.DISCONNECT);
                    System.out.println("Client disconnected!");
                    games.remove(game);
                }

                if (clients.contains(c2)) {
                    c2.sendMessage(Messages.MOVE + Messages.DELIMITER + command1 + Messages.DELIMITER + command2);
                    System.out.println("Move: " + command1 + ", " + command2 + " sent to the client " + c2.getName());
                } else if (clients.contains(c1)) {
                    c1.sendMessage(Messages.GAMEOVER + Messages.DELIMITER + Messages.DISCONNECT);
                    System.out.println("Client disconnected!");
                    games.remove(game);
                }
            }
        } else {
            System.out.println("Unable to send move! (move invalid)");
        }
    }

    public static void main(String[] args) {
        // Start a new server
        Server server = new Server();
        new Thread(server).start();
    }
}
