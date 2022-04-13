// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Views;

import ClientServer.Client;
import Exceptions.AlreadyLoggedException;
import Exceptions.WrongFormatException;

import java.io.IOException;
import java.util.Scanner;

public class ClientTUI implements View{
    @Override
    public void displayMessage(String message) {
        // Display string in the console
        System.out.println(message);
    }

    @Override
    public String getStringInput() {
        // Get input from a scanner
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }

    @Override
    public int getIntInput() {
        Scanner sc = new Scanner(System.in);
        return sc.nextInt();
    }

    @Override
    public void listCommands() {
        System.out.println("List of commands available: ");
        System.out.println("1. HELP: Displays this help message.");
        System.out.println("2. LIST: Lists all players currently logged in the server.");
        System.out.println("2. HINT: Gives a possible single / double move.");
    }
}
