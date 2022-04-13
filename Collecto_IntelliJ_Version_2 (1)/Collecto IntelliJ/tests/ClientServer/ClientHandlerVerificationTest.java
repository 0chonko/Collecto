// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package ClientServer;

import Protocol.Messages;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerVerificationTest {
    private static ClientHandler clientHandler;

    private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeAll
    static void handlerSetup() {
        clientHandler = new ClientHandler(null, null, "John Doe");
        clientHandler.out = new PrintWriter(new OutputStreamWriter(outContent), true);
        clientHandler.in = new BufferedReader(
                new InputStreamReader(System.in));

        new Thread(clientHandler).start();
    }

    @BeforeEach
    void resetOutContent() {
        outContent.reset();
    }

    @Test
    void invalidCommand() {
        // Send unrecognizable command
        clientHandler.handleCommand("Gibberish~Lmao");

        assertEquals(Messages.ERROR + Messages.DELIMITER + "Invalid command" + System.lineSeparator(),
                outContent.toString());
    }

    @Test
    void invalidHello() {
        // Send hello and more than one argument
        clientHandler.handleCommand(Messages.HELLO + Messages.DELIMITER + "Bad" + Messages.DELIMITER + "Hello");

        assertEquals(Messages.ERROR + Messages.DELIMITER + "Invalid command" + System.lineSeparator(),
                outContent.toString());
    }

    @Test
    void invalidQueue() {
        // Send queue with more than one argument
        clientHandler.handleCommand(Messages.HELLO + Messages.DELIMITER + "Bad" + Messages.DELIMITER + "Queue");

        assertEquals(Messages.ERROR + Messages.DELIMITER + "Invalid command" + System.lineSeparator(),
                outContent.toString());
    }

    @Test
    void invalidList() {
        // Send queue with more than one argument
        clientHandler.handleCommand(Messages.LIST + Messages.DELIMITER + "Bad" + Messages.DELIMITER + "Queue");

        assertEquals(Messages.ERROR + Messages.DELIMITER + "Invalid command" + System.lineSeparator(),
                outContent.toString());
    }

    @Test
    void invalidLogin() {
        // Send login and more than one argument
        clientHandler.handleCommand(Messages.LOGIN + Messages.DELIMITER + "Bad" + Messages.DELIMITER + "Login");

        assertEquals(Messages.ERROR + Messages.DELIMITER + "Invalid command" + System.lineSeparator(),
                outContent.toString());
    }

    @Test
    void invalidMove() {
        clientHandler.handleCommand(Messages.MOVE);

        assertEquals(Messages.ERROR + Messages.DELIMITER + "Invalid command" + System.lineSeparator(),
                outContent.toString());
        outContent.reset();

        clientHandler.handleCommand(Messages.MOVE + Messages.DELIMITER + "Bad");

        assertEquals(Messages.ERROR + Messages.DELIMITER + "Invalid command" + System.lineSeparator(),
                outContent.toString());
        outContent.reset();

        clientHandler.handleCommand(Messages.MOVE + Messages.DELIMITER + "Bad" + Messages.DELIMITER + "Move");

        assertEquals(Messages.ERROR + Messages.DELIMITER + "Invalid command" + System.lineSeparator(),
                outContent.toString());
        outContent.reset();
    }
}