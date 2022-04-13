// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package ClientServer;

import Board.Board;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test to test the issues that may arise when using the server.
 *
 * Mainly disconnects or malformed input.
 *
 */

class ServerIssuesTest {
    ClientHandler c1;
    ClientHandler c2;

    private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    Server server = new Server();

    @BeforeAll
    static void setupStream() {
        System.setOut(new PrintStream(outContent));
    }

    @BeforeEach
    void setup() {
        c1 = new ClientHandler(null, null, null);
        c2 = new ClientHandler(null, null, null);

        outContent.reset();
    }

    /**
     * Tests what happens if an attempt to start the game is made
     * and one or more of the clients are not connected
     */
    @Test
    void gameStartTest() {
        // both players queued, ready to start game
        server.queue.add(c1);
        server.queue.add(c2);

        // The players are not in the client list, though
        // This only happens normally when either of them has dropped the connection

        server.checkQueue();

        assertTrue(outContent.toString().contains("Issues sending to client"));
    }

    @Test
    void movesTest() {
        server.clients.add(c1);
        server.clients.add(c2);

        server.queue.add(c1);
        server.queue.add(c2);

        Board board = new Board();
        resetBoardForTest(board);

        // Top row contains two tiles of the same color
        board.setTile(0, 0, 2);
        board.setTile(0, 2, 2);
        board.setTile(1, 0, 5);
        board.setTile(6, 3, 5);

        // Start the game with a given board
        server.gameStart(c1, c2, board);

        // Send a valid single move to the server
        server.singleMove(c1, board.singleMovesList().get(0));

        assertTrue(outContent.toString().contains("Found client / game!"));
        assertTrue(outContent.toString().contains("Found matching board!"));
        assertTrue(outContent.toString().contains("sent to the client"));

        // Send an invalid single move to the server
        server.singleMove(c1, 0);
        assertTrue(outContent.toString().contains("move invalid"));

        // Send an invalid double move
        server.doubleMove(c1, 0, 5);
        assertTrue(outContent.toString().contains("move invalid"));

        // Send a valid double move
        server.doubleMove(c1, board.doubleMovesList().get(0)[0],  board.doubleMovesList().get(0)[1]);
        assertTrue(outContent.toString().contains("sent to the client"));
    }

    @Test
    void gameOverSingleMove() {
        server.clients.add(c1);
        server.clients.add(c2);

        server.queue.add(c1);
        server.queue.add(c2);

        Board board = new Board();
        resetBoardForTest(board);

        // Top row contains two tiles of the same color
        board.setTile(0, 0, 2);
        board.setTile(0, 2, 2);

        // Start the game with a given board
        server.gameStart(c1, c2, board);

        server.singleMove(c1, board.singleMovesList().get(0));

        assertTrue(outContent.toString().contains("over"));
    }

    @Test
    void gameOverDoubleMove() {
        server.clients.add(c1);
        server.clients.add(c2);

        server.queue.add(c1);
        server.queue.add(c2);

        Board board = new Board();
        resetBoardForTest(board);

        // Top row contains two tiles of the same color
        board.setTile(1, 0, 5);
        board.setTile(6, 3, 5);

        // Start the game with a given board
        server.gameStart(c1, c2, board);

        server.doubleMove(c1, board.doubleMovesList().get(0)[0], board.doubleMovesList().get(0)[1]);

        assertTrue(outContent.toString().contains("over"));
    }

    @Test
    void singleMoveConnectionDroppedTest() {
        server.clients.add(c1);

        server.queue.add(c1);
        server.queue.add(c2);

        Board board = new Board();
        resetBoardForTest(board);

        // Top row contains two tiles of the same color
        board.setTile(0, 0, 2);
        board.setTile(0, 2, 2);
        board.setTile(1, 0, 5);
        board.setTile(6, 3, 5);

        // Start the game with a given board
        server.gameStart(c1, c2, board);

        server.singleMove(c1, board.singleMovesList().get(0));
        assertTrue(outContent.toString().contains("disconnected"));
    }

    @Test
    void doubleMoveConnectionDroppedTest() {
        server.clients.add(c1);

        server.queue.add(c1);
        server.queue.add(c2);

        Board board = new Board();
        resetBoardForTest(board);

        // Top row contains two tiles of the same color
        board.setTile(1, 0, 5);
        board.setTile(6, 3, 5);
        board.setTile(2, 1, 3);
        board.setTile(5, 6, 3);

        // Start the game with a given board
        server.gameStart(c1, c2, board);

        server.doubleMove(c1, board.doubleMovesList().get(0)[0], board.doubleMovesList().get(0)[1]);
        assertTrue(outContent.toString().contains("disconnected"));
    }

    /**
     * resets a given board for a test
     * @param board
     */
    void resetBoardForTest(Board board) {
        for (int y = 0; y < board.DIM; y++) {
            for (int x = 0; x < board.DIM; x++) {
                board.setTile(x, y, 0);
            }
        }
    }
}