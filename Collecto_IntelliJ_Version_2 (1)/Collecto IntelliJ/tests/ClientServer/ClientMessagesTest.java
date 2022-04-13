// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package ClientServer;

import Exceptions.AlreadyLoggedException;
import Exceptions.WrongFormatException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Client messages integration test:
 *
 * IMPORTANT: run the server at port 8888 to test this!
 *
 * tests the ability to send and receive messages to / from the server
 */
class ClientMessagesTest {
    public static Server server;
    public static Client client, client2;
    public static final InputStream sysInBackup = System.in;
    private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpConnection() {
        server.clearClients();
        server.clearQueue();

        client = new Client();
        client2 = new Client();

        // Connect to the server
        ByteArrayInputStream in = new ByteArrayInputStream(("127.0.0.1" + "\n" + "8888").getBytes());
        System.setIn(in);
        client.connect();
        System.setIn(sysInBackup);

        in = new ByteArrayInputStream(("127.0.0.1" + "\n" + "8888").getBytes());
        System.setIn(in);
        client2.connect();
        System.setIn(sysInBackup);
    }

    @BeforeAll
    static public void setUpStream() {
        // Start a server at port 8888
        server = new Server();
        server.port = 8888;
        new Thread(server).start();

        // Set the out stream so we can read it
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void helloTest() {
        // Send the hello message
        try {
            client.hello();
        } catch (IOException | WrongFormatException e) {
            e.printStackTrace();
        }

        assertTrue(outContent.toString().contains("Connected successfully!"));

        outContent.reset();
    }

    @Test
    void loginTest() {
        // Send the hello message
        try {
            client.hello();
            client2.hello();
        } catch (IOException | WrongFormatException e) {
            e.printStackTrace();
        }

        // Login once
        try {
            client.login("Filip");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AlreadyLoggedException | WrongFormatException e) {
            e.printStackTrace();
        }

        assertTrue(outContent.toString().contains("Logged in successfully!"));

        outContent.reset();

        // Login with another user
        try {
            client2.login("German");
        } catch (IOException | WrongFormatException e) {
            e.printStackTrace();
        } catch (AlreadyLoggedException e) {
            System.out.println("User with such a user name already exists!");
        }

        assertTrue(outContent.toString().contains("Logged in successfully!"));
        outContent.reset();
    }

    @Test
    void queueTest() {
        // Send the hello message
        try {
            client.hello();
        } catch (IOException | WrongFormatException e) {
            e.printStackTrace();
        }

        // Queue
        try {
            client.queue();
        } catch (IOException | WrongFormatException e) {
            e.printStackTrace();
        }

        assertTrue(outContent.toString().contains("Queued successfully!"));
        outContent.reset();
    }

    @Test
    void listTest() {
        // Send the hello message
        try {
            client.hello();
            client2.hello();
        } catch (IOException | WrongFormatException e) {
            e.printStackTrace();
        }

        // Login once
        try {
            client.login("Filip");
            client2.login("German");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AlreadyLoggedException e) {
            e.printStackTrace();
        } catch (WrongFormatException e) {
            e.printStackTrace();
        }

        try {
            client.list();
        } catch (IOException | WrongFormatException e) {
            e.printStackTrace();
        }

        assertTrue(outContent.toString().contains("Filip"));
        assertTrue(outContent.toString().contains("German"));

        outContent.reset();
    }

    @Test
    void startGameTest() {
        // Send the hello message
        try {
            client.hello();
            client2.hello();
        } catch (IOException | WrongFormatException e) {
            e.printStackTrace();
        }

        // Login
        try {
            client.login("Filip");
            client2.login("German");
        } catch (AlreadyLoggedException e) {
            e.printStackTrace();
        } catch (WrongFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Queue
        try {
            client.queue();
            client2.queue();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WrongFormatException e) {
            e.printStackTrace();
        }

        String content = outContent.toString();

        content = outContent.toString();
    }
}