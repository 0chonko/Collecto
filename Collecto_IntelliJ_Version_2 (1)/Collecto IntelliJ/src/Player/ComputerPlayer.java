// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Player;

import Board.Board;
import ClientServer.Client;
import Strategies.NaiveStrategy;
import Strategies.Strategy;
import Views.View;

import java.util.List;
import java.util.Scanner;

public class ComputerPlayer extends Player{
    private String name;

    private View view;

    private Client client;

    public ComputerPlayer (String name, View view, Client client, Strategy strategy) {
        super(strategy.getName() + " " + name);

        this.client = client;

        this.view = view;

        this.strategy = strategy;
    }

    public ComputerPlayer (String name, View view, Client client) {
        super("Naive " + name);

        this.client = client;

        this.view = view;

        this.strategy = new NaiveStrategy();
    }

    @Override
    public int determineSingleMove(Board board) {
        return strategy.determineSingleMove(board);
    }

    @Override
    public int[] determineDoubleMove(Board board) {
        return strategy.determineDoubleMove(board);
    }

    @Override
    public void makeDoubleMove(Board board, int move1, int move2) {
        // Apply move to the board
        board.moveLine(move1);
        board.moveLine(move2);

        // Remove adjacent balls
        updateScore(board);
    }

    @Override
    public void makeSingleMove(Board board, int move) {
        // Apply move to the board
        board.moveLine(move);

        // Remove adjacent balls
        updateScore(board);
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
}
