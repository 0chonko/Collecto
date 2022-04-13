// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//


package ClientServer;

import Board.Board;
import Exceptions.AlreadyLoggedException;
import Exceptions.WrongFormatException;
import Player.Player;
import Player.ComputerPlayer;
import Player.HumanPlayer;
import Protocol.Messages;
import Strategies.SmartStrategy;
import Views.ClientTUI;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.net.InetAddress;

public class Client {
    public Socket serverSock;
    public BufferedReader in;
    public BufferedWriter out;

    private final String CLIENT_NAME = "Generic client #9000";

    private String name;

    private static Player player;

    boolean myTurn;

    boolean playing = true;

    ClientTUI view = new ClientTUI();

    Board board;

    public void connect() {
        resetConnection();

        while (serverSock == null) {
            String host;
            int port;

            // Get the IP of the server
            Scanner sc = new Scanner(System.in);
            view.displayMessage("Please enter the IP you want to connect to: ");
            if (sc.hasNextLine()) {
                host = sc.nextLine();
            } else { // default to address 127.0.0.1 so the test doesn't hang
                host = "127.0.0.1";
            }

            // Get the port of the server
            view.displayMessage("Please enter the port you want to connect to: ");

            if (sc.hasNextInt()) {
                port = sc.nextInt();
            } else { // default to port 8888 so the test doesn't hang
                port = 8888;
            }

            try {
                //Setup the connection to the socket
                InetAddress address = InetAddress.getByName(host);
                view.displayMessage("Attempting to connect to " + address + ":"
                        + port + "...");
                serverSock = new Socket(address, port);

                // Setup the input and output streams for sending messages to the server
                in = new BufferedReader(new InputStreamReader(
                        serverSock.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(
                        serverSock.getOutputStream()));
            } catch (IOException | IllegalArgumentException e) {
                view.displayMessage("Could not connect to "
                        + host + " at port " + port + ". Try again.");
            }
        }
    }

    public void resetConnection() {
        serverSock = null;
        in = null;
        out = null;
    }

    public void closeConnection() {
        view.displayMessage("Disconnecting...");
        try {
            in.close();
            out.close();
            serverSock.close();
        } catch (IOException e) {
            view.displayMessage("Error disconnecting from server.");
        }

        view.displayMessage("Disconnected from server.");
    }

    public synchronized void sendMessage(String message) throws IOException {
        if (out != null) {
            out.write(message);
            out.newLine();
            out.flush();
        } else {
            throw new IOException();
        }
    }

    public String getMessage() {
        if (in != null) {
            try {
                // Read and return answer from Server

                return in.readLine();
            } catch (IOException e) {
                shutDown();
                return null;
            }
        } else {
            shutDown();
            return null;
        }
    }

    public void hello() throws IOException, WrongFormatException {
        sendMessage(Messages.HELLO + Messages.DELIMITER + CLIENT_NAME);

        String response = getMessage();

        if (response != null) {
            String[] split = response.split(Messages.DELIMITER, 0);

            if (split[0].equals(Messages.HELLO)) {
                view.displayMessage("Connected successfully!");
            } else {
                throw new WrongFormatException("The response from the server was malformed!");
            }
        } else {
            throw new WrongFormatException("Received no response from the server!");
        }
    }

    public void login(String userName) throws IOException, AlreadyLoggedException, WrongFormatException {
        sendMessage(Messages.LOGIN + Messages.DELIMITER + userName);

        String response = getMessage();

        // If user with such a name already exists
        if (response.equals(Messages.ALREADYLOGGEDIN)) {
            throw new AlreadyLoggedException("User with such a user name already exists!");
        }

        // Otherwise attempt to login
        if (response.equals(Messages.LOGIN)) {
            view.displayMessage("Logged in successfully!");
        } else {
            throw new WrongFormatException("The response from the server was malformed!");
        }
    }

    public void queue() throws IOException, WrongFormatException {
        sendMessage(Messages.QUEUE);
        view.displayMessage("Queued successfully!");

        /*
        String response = getMessage();

        if (response.equals(Messages.QUEUE)) {
            System.out.println("Queued successfully, waiting for game...");
        } else {
            throw new WrongFormatException("The response from the server was malformed!");
        }
         */
    }

    public void list() throws IOException, WrongFormatException {
        sendMessage(Messages.LIST);

        String response = getMessage();

        if (response != null) {
            String[] split = response.split(Messages.DELIMITER, 0);

            if (split[0].equals(Messages.LIST)) {
                if (split.length > 2) {
                    for (int i = 1; i < split.length; i++) {
                        view.displayMessage("Player " + i + ": " + split[i]);
                    }
                } else {
                    throw new WrongFormatException("The response from the server was malformed!");
                }
            } else {
                throw new WrongFormatException("The response from the server was malformed!");
            }
        } else {
            throw new WrongFormatException("Received no response from the server!");
        }
    }

    public int singleHint() {
        if (board.singleMovePossible()) { // if a single move is possible, show one randomly
            List<Integer> moves = board.singleMovesList();

            int index = (int) (Math.random() * moves.size());

            return moves.get(index);
        }

        return -1;
    }

    public Integer[] doubleHint() {
        if (board.doubleMovePossible()) {
            List<Integer[]> moves = board.doubleMovesList();

            int index = (int) (Math.random() * moves.size());

            return moves.get(index);
        }

        return null;
    }

    public Board getBoard() {
        return board;
    }

    public void determinePlayerType() {
        boolean askPlayer = true;
        boolean askDifficulty = true;

        while (askPlayer) {
            view.displayMessage("Would you like to be a human or an AI?");

            String answer = view.getStringInput();

            if (answer.equalsIgnoreCase("AI")) {
                while (askDifficulty) {
                    view.displayMessage("Would you like to be a naive or a smart AI?");

                    answer = view.getStringInput();

                    if (answer.equalsIgnoreCase("naive")) {
                        player = new ComputerPlayer(name, view, this);

                        view.displayMessage("About to be queued as a " + answer.toLowerCase() + " AI. " +
                                "Greetings future overlord!");

                        askDifficulty = false;
                    } else if (answer.equalsIgnoreCase("smart")) {
                        player = new ComputerPlayer(name, view, this, new SmartStrategy());

                        view.displayMessage("About to be queued as a " + answer.toLowerCase() + " AI. " +
                                "Greetings future overlord!");

                        askDifficulty = false;
                    } else {
                        view.displayMessage(answer.toLowerCase() + " is not a valid intelligence level.");
                    }
                }

                askPlayer = false;
            } else if (answer.equalsIgnoreCase("human")) {
                view.displayMessage("About to be queued as a human. Greetings human!");
                player = new HumanPlayer("Human", view, this);

                askPlayer = false;
            } else {
                view.displayMessage("I'm sorry but you cannot play as " + answer.toLowerCase() + ".");
            }
        }
    }

    public void gameStart() {
        String gameStart = null;

        while (gameStart == null) {
            gameStart = getMessage();

            String[] split = gameStart.split(Messages.DELIMITER, 0);

            if (split[0].equals(Messages.NEWGAME)) {
                view.displayMessage("Starting game...");

                //System.out.println("Starting game...");

                // Make a board instance for this client
                board = new Board();

                for (int y = 0; y < 7; y++) {
                    for (int x = 0; x < 7; x++) {
                        board.fields[x][y] = Integer.parseInt(split[7 * y + x + 1]);
                    }
                }

                // Determine if we go first
                myTurn = split[50].equals(name);

                // Print initial board
                if (!myTurn) {
                    view.displayMessage(board.toString());
                }
            } else {
                gameStart = null;
            }
        }
    }

    public void play() {
        while (playing) {
            if (myTurn) {
                playMyTurn();
            } else {
                playOtherTurn();
            }
        }
    }

    public void playMyTurn() {
        // Print the board out to the view
        view.displayMessage(board.toString());

        if (board.singleMovePossible()) { // if a single move is possible
            // Determine the move we should make
            int move = player.determineSingleMove(board);

            // Send the move to the server
            try {
                sendMessage(Messages.MOVE + Messages.DELIMITER + move);
            } catch (IOException e) {
                //view.displayMessage("Connection dropped!");
                shutDown();
            }

            // Wait for the response from the server
            String message = getMessage();

            if (message == null) {
                shutDown();
            } else {
                String[] split = message.split(Messages.DELIMITER, 0);

                if (split[0].equals(Messages.MOVE)) { // valid response from server
                    // make the move
                    player.makeSingleMove(board, move);
                    myTurn = false;
                } else if (split[0].equals(Messages.GAMEOVER)) { // check if the move has resulted in a game over
                    handleGameOver(split);
                } else {
                    view.displayMessage("Move was invalid!");
                }
            }
        } else { // if only a double move is possible
            int[] moves = player.determineDoubleMove(board);

            // Send the move to the server
            try {
                sendMessage(Messages.MOVE + Messages.DELIMITER + moves[0] + Messages.DELIMITER + moves[1]);
            } catch (IOException e) {
                shutDown();
            }

            // Wait for the response from the server
            String message = getMessage();
            String[] split = message.split(Messages.DELIMITER, 0);

            if (split[0].equals(Messages.MOVE)) { // valid response from server
                // make the move
                player.makeDoubleMove(board, moves[0], moves[1]);
                myTurn = false;
            } else if (split[0].equals(Messages.GAMEOVER)) {
                handleGameOver(split);
            } else {
                view.displayMessage("Move was invalid!");
            }
        }
    }

    public void playOtherTurn() {
        // Wait to receive the move message
        String move = getMessage();

        if (move == null) {
            shutDown();
        } else {
            String[] split = move.split(Messages.DELIMITER);

            if (split[0].equals(Messages.MOVE)) { // if the message was a move
                if (split.length < 3) { // Single move
                    if (board.singleMoveValid(Integer.parseInt(split[1]))) {
                        player.makeSingleMove(board, Integer.parseInt(split[1]));
                    }
                } else { // Double move
                    if (board.doubleMoveValid(Integer.parseInt(split[1]), Integer.parseInt(split[2]))) {
                        player.makeDoubleMove(board, Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                    }
                }

                myTurn = true;
            } else if (split[0].equals(Messages.GAMEOVER)) { // if the game is over
                handleGameOver(split);
            }
        }
    }

    public void handleGameOver(String[] split) {
        //TODO: add prompts for second time playing
        switch (split[1]) {
            case Messages.DRAW -> {
                view.displayMessage("The match resulted in a draw!");
                view.displayMessage("Would you like to play again? (yes / no)");
                String answer = view.getStringInput();
                if (!answer.equalsIgnoreCase("yes")
                        && !answer.equalsIgnoreCase("y")) {
                    playing = false;
                } else {
                    queueTUI();
                    gameStart();
                }
            }
            case Messages.VICTORY -> {
                view.displayMessage("The winner of this match was " + split[2] + "!");
                view.displayMessage("Would you like to play again? (yes / no)");
                answer = view.getStringInput();
                if (!answer.equalsIgnoreCase("yes")
                        && !answer.equalsIgnoreCase("y")) {
                    playing = false;
                } else {
                    queueTUI();
                    gameStart();
                }
            }
            case Messages.DISCONNECT -> {
                view.displayMessage("Your opponent disconnected. This counts as a win for you!");
                view.displayMessage("Would you like to queue again? (yes / no)");
                answer = view.getStringInput();
                if (!answer.equalsIgnoreCase("yes")
                        && !answer.equalsIgnoreCase("y")) {
                    playing = false;
                } else {
                    queueTUI();
                    gameStart();
                }
            }
            default -> view.displayMessage("Oops! Something went wrong. Disconnecting...");
        }
    }

    public void shutDown() {
        view.displayMessage("Lost connection to the server!");
        closeConnection();
        System.exit(0);
    }

    /**
     * Method that handles connection / reconnection via the TUI.
     * This is called in the main method.
     *
     * @ensures client has said hello or has gracefully terminated
     */
    public void helloTUI() {
        boolean hello = true;
        while (hello) {
            try {
                hello();
                hello = false;
            } catch (IOException | WrongFormatException e) {
                view.displayMessage("Something went wrong connecting to the server!");

                view.displayMessage("Would you like to reconnect? (yes / no)");
                String answer = view.getStringInput();
                if (!answer.equalsIgnoreCase("yes")
                        && !answer.equalsIgnoreCase("y")) {
                    shutDown();
                }
            }
        }
    }

    /**
     * Method that handles logging in via the TUI.
     * This is called in the main method.
     *
     * @ensures client has logged in or has gracefully terminated
     */
    public void loginTUI() {
        boolean enterUserName = true;
        boolean login = true;

        while (login) {
            // Enter your username if necessary
            if (enterUserName) {
                view.displayMessage("Please enter your username: ");
                name = view.getStringInput();
                enterUserName = false;
            }

            try {
                login(name);
                login = false;
            } catch (IOException e) {
                view.displayMessage("Something went wrong trying to login!");

                view.displayMessage("Would you like to try logging in again? (yes / no)");
                String answer = view.getStringInput();
                if (!answer.equalsIgnoreCase("yes")
                        && !answer.equalsIgnoreCase("y")) {
                    shutDown();
                }
            } catch (AlreadyLoggedException | WrongFormatException e) {
                enterUserName = true;
            }
        }
    }

    /**
     * Method that handles queuing via the TUI.
     * This is called in the main method.
     *
     * @ensures client has queued or has gracefully terminated
     */
    public void queueTUI() {
        boolean queue = true;
        while (queue) {
            try {
                queue();
                queue = false;
            } catch (IOException | WrongFormatException e) {
                view.displayMessage("Something went wrong trying to queue!");

                view.displayMessage("Would you like to try to queue again? (yes / no)");
                String answer = view.getStringInput();
                if (!answer.equalsIgnoreCase("yes")
                        && !answer.equalsIgnoreCase("y")) {
                    shutDown();
                }
            }
        }
    }

    /**
     * Main method
     * Connects the client, logs them in, queues them and handles playing games.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Connect the client to the server
        Client client = new Client();
        client.connect();

        // Say hello via the TUI
        client.helloTUI();

        // Login
        client.loginTUI();

        // Ask if AI or Human player
        client.determinePlayerType();

        // Queue
        client.queueTUI();

        // Wait for the server to start a game
        client.gameStart();

        // Play the game
        client.play();
    }
}
