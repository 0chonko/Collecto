// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Strategies;

import Board.Board;

import java.util.List;

public class NaiveStrategy implements Strategy {

    @Override
    public String getName() {
        return "Naive";
    }

    @Override
    public int determineSingleMove(Board board) {
        if (!board.gameOver() && board.singleMovePossible()) {
            int rand = (int) (Math.random() * board.singleMovesList().size());
            return board.singleMovesList().get(rand);
        }
        return -1;
    }

    public int[] determineDoubleMove(Board board) {
        /*int[] moves = new int[2];

        if (!board.gameOver() && !board.singleMovePossible()) {
            int rand = (int) (Math.random() * board.doubleMovesList().size());
            moves[0] = board.doubleMovesList().get(rand)[0];
            moves[1] = board.doubleMovesList().get(rand)[1];
            return moves;
        }

        return new int[]{-1, -1};*/

        if (board.doubleMovePossible()) {
            List<Integer[]> moves = board.doubleMovesList();

            int index = (int) (Math.random() * moves.size());

            Integer[] options = moves.get(index);

            return new int[] {options[0], options[1]};
        }

        return new int[] {-1, -1};
    }
}

