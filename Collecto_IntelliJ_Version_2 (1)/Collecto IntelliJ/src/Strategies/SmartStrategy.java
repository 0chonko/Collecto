// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Strategies;

import Board.Board;

import java.util.Arrays;

public class SmartStrategy implements Strategy {

    @Override
    public String getName() {
        //TODO Auto-generated method stub
        return "Smart";
    }

    @Override
    public int determineSingleMove(Board board) {
        //check if gives biggest amount of adjacent balls

        int counter = 0;
        int previousMax = 0;
        if (!board.gameOver() && board.singleMovePossible()) {
            for (int i : board.singleMovesList()) {
                Board copiedBoard = board.deepCopy();
                copiedBoard.moveLine(i);
                if (copiedBoard.adjacentBalls().size() > previousMax) {
                    previousMax = copiedBoard.adjacentBalls().size();
                    counter = i;
                }
            }
            return counter;
        }
        return -1;
    }

    public int[] determineDoubleMove(Board board) {
        //check if gives biggest amount of adjacent balls
        int counter[] = new int[2];
        int previousMax = 0;
        if (!board.gameOver() && !board.singleMovePossible()) {
            for (Integer[] i : board.doubleMovesList()) {
                Board copiedBoard = board.deepCopy();
                copiedBoard.moveLine(i[0]);
                copiedBoard.moveLine(i[1]);
                if (copiedBoard.adjacentBalls().size() >= previousMax) {
                    previousMax = copiedBoard.adjacentBalls().size();
                    counter[0] = i[0];
                    counter[1] = i[1];

                }
            }
            return counter;
        }
        return new int[]{-1, -1};
    }

}

