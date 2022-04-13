// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package ClientServer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Client connections unit test:
 *
 * tests the various connection functionalities
 */

class ClientConnectionsTest {
    public static final InputStream sysInBackup = System.in;
    public static Server server;
    private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeAll
    static public void setUpStream() {
        server = new Server();
        server.port = 8888;
        new Thread(server).start();

        System.setOut(new PrintStream(outContent));
    }

    /**
     * Connection test:
     *
     * test the connection of one client to the server at port 8888, address: 127.0.0.1
     *
     * @ensures after connection the client's server socket, input and output streams are not null
     */
    @Test
    void connectionTest() {
        Client client = new Client();

        // Mock the IP and port console input
        ByteArrayInputStream in = new ByteArrayInputStream(("127.0.0.1" + "\n" + "8888").getBytes());
        System.setIn(in);
        client.connect();
        System.setIn(sysInBackup);

        assertNotNull(client.serverSock);
        assertNotNull(client.out);
        assertNotNull(client.in);
    }

    /**
     * Wrong Port / IP test:
     *
     * tests if the behavior of the IP or the port being wrong leads to an error in the connection
     *
     * @ensures if a wrong IP / port is input, the client asks for the IP / port again
     */
    @Test
    void connectionWrongPortIPTest() {
        Client client = new Client();

        // Mock the input (WRONG: IP)
        ByteArrayInputStream in = new ByteArrayInputStream(("196.2.3.1" + "\n" + "8888").getBytes());
        System.setIn(in);
        client.connect();
        System.setIn(sysInBackup);

        assertTrue(outContent.toString().contains("196.2.3.1"));

        outContent.reset();

        // Mock the input (WRONG: port)
        in = new ByteArrayInputStream(("127.0.0.1" + "\n" + "8080").getBytes());
        System.setIn(in);
        client.connect();
        System.setIn(sysInBackup);

        assertTrue(outContent.toString().contains("8080"));

        outContent.reset();
    }

    /**
     * Reset connection test:
     *
     * tests if the connection can be properly reset
     *
     * @ensures after resetting the connection the client's socket, in and out stream are null
     */
    @Test
    void resetConnectionTest() {
        Client client = new Client();

        // Set some mock values
        client.serverSock = new java.net.Socket();
        client.in = new BufferedReader(new InputStreamReader(System.in));
        client.out = new BufferedWriter(new OutputStreamWriter(System.out));

        // Reset connection
        client.resetConnection();

        assertNull(client.serverSock);
        assertNull(client.in);
        assertNull(client.out);
    }

    /**
     * Close connection test:
     *
     * tests if the connection can be properly closed
     *
     * @ensures connection is closed after calling method
     */
    @Test
    void closeConnectionTest() {
        // Connect
        Client client = new Client();

        ByteArrayInputStream in = new ByteArrayInputStream(("127.0.0.1" + "\n" + "8888").getBytes());
        System.setIn(in);
        client.connect();
        System.setIn(sysInBackup);

        // Close the connection
        client.closeConnection();

        assertTrue(outContent.toString().contains("Disconnected from server."));

        outContent.reset();
    }
}