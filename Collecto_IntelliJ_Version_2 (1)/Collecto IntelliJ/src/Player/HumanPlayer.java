// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Player;

import Board.Board;
import ClientServer.Client;
import Constants.ClientCommands;
import Exceptions.WrongFormatException;
import Views.View;

import java.io.IOException;

public class HumanPlayer extends Player {
    private final View view;

    private final Client client;

    public HumanPlayer(String name, View view, Client client) {
        super(name);
        this.view = view;
        this.client = client;
    }

    @Override
    public void makeDoubleMove(Board board, int move1, int move2) {
        // Apply move to the board
        board.moveLine(move1);
        board.moveLine(move2);

        // Remove adjacent balls and add to the score
        board.removeBalls();
    }

    @Override
    public void makeSingleMove(Board board, int move) {
        // Apply move to the board
        board.moveLine(move);

        // Remove adjacent balls and add to the score
        board.removeBalls();
    }

    @Override
    public int[] determineDoubleMove(Board board) {
        view.displayMessage("Only a double move is possible!");

        while(true) {
            // Get the first move
            int move1;
            int move2;

            view.displayMessage("Please enter your first move's index or a command " +
                    "(type help for the list of commands): ");
            String input = view.getStringInput();


            // Determine if command or index of move
            try {
                move1 = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                handleCommand(input);
                continue;
            }

            view.displayMessage("Please enter your second move's index or a command " +
                    "(type help for the list of commands): ");
            input = view.getStringInput();

            // Determine if command or index of move
            try {
                move2 = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                handleCommand(input);
                continue;
            }

            // If two moves have been input
            if (board.doubleMoveValid(move1, move2)) {
                return new int[]{move1, move2};
            }
        }
    }

    @Override
    public int determineSingleMove(Board board) {
        view.displayMessage("A single move is possible!");

        while (true) {
                int move = -1;

                view.displayMessage("Please enter your move's index or a command (type help for the list of commands):");

                String input = view.getStringInput();

                // Determine if command or index of move
                try {
                    move = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    handleCommand(input);
                }

                // Check if input is a valid move
                if (board.singleMoveValid(move)) {
                    return move;
                }
        }
    }

    public void handleCommand(String command) {
        if (ClientCommands.contains(command)) { // if it is in the list of commands
            switch (command.toLowerCase()) {
                case ClientCommands.HELP:
                    view.listCommands();
                    break;

                case ClientCommands.LIST:
                    //TODO: handle the exceptions properly
                    try {
                        client.list();
                    } catch (IOException | WrongFormatException e) {
                        e.printStackTrace();
                    }

                    break;

                case ClientCommands.HINT:
                    if (client.getBoard().singleMovePossible()) { // if single move possible
                        int move = client.singleHint();

                        System.out.println("A single move is possible with index: " + move);
                    } else {
                        Integer[] moves = client.doubleHint();

                        System.out.println("A double move is possible with indices: " + moves[0] + moves[1]);
                    }
                    break;

                default:
                    break;
            }
        } else {
            System.out.println("This is not an available command. Try again!");
        }
    }
}
