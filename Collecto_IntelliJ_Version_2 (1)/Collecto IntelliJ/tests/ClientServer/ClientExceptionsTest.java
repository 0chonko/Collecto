// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package ClientServer;

import Exceptions.AlreadyLoggedException;
import Exceptions.WrongFormatException;
import Protocol.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Client unit test to test if custom exceptions are thrown properly
 */
class ClientExceptionsTest {
    Client client;
    private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void clientSetup() {
        outContent.reset();

        client = new Client();

        client.out = new BufferedWriter(new OutputStreamWriter(System.out));

        ByteArrayInputStream stream = new ByteArrayInputStream(("bad response" + System.lineSeparator()).getBytes());

        client.in = new BufferedReader(new InputStreamReader(stream));

        System.setOut(new PrintStream(outContent));
    }

    @Test
    void wrongFormatExceptionHello() {
        try {
            client.hello();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WrongFormatException e) {
            System.out.println("malformed");
        }

        assertTrue(outContent.toString().contains("malformed"));
    }

    @Test
    void wrongFormatExceptionLogin() {
        try {
            client.login("Mah boi");
        } catch (IOException | AlreadyLoggedException e) {
            e.printStackTrace();
        } catch (WrongFormatException e) {
            System.out.println("malformed");
        }
        assertTrue(outContent.toString().contains("malformed"));
    }

    @Test
    void wrongFormatExceptionList() {
        try {
            client.list();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WrongFormatException e) {
            System.out.println("malformed");
        }
        assertTrue(outContent.toString().contains("malformed"));
    }

    @Test
    void alreadyLoggedInException() {
        ByteArrayInputStream stream = new ByteArrayInputStream((Messages.ALREADYLOGGEDIN
                + System.lineSeparator()).getBytes());

        client.in = new BufferedReader(new InputStreamReader(stream));

        System.setOut(new PrintStream(outContent));

        try {
            client.login("Filip");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AlreadyLoggedException e) {
            System.out.println("logged in already");
        } catch (WrongFormatException e) {
            e.printStackTrace();
        }

        assertTrue(outContent.toString().contains("logged in already"));
    }
}