// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Player;

import Board.Board;
import Strategies.Strategy;

public abstract class Player {
    protected Strategy strategy;
    public int[] scored = new int[6];

    private String name;

    Player(String name) {
        this.name = name;
    }

    public abstract int determineSingleMove(Board board);

    public abstract int[] determineDoubleMove(Board board);

    public abstract void makeSingleMove(Board board, int move);

    public abstract void makeDoubleMove(Board board, int move1, int move2);

    public void updateScore(Board board) {
        int[] scr;
        scr = board.removeBalls();
        for (int i = 0; i < this.scored.length; i++) {
            this.scored[i] += scr[i];
        }
    }

    public int getScore(Board board) {
        return board.scoreCounter(this.scored);
    }

    public void resetScore() {
        this.scored = new int[6];
    }
}