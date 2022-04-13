// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Strategies;

import Board.Board;

public interface Strategy {
    public String getName();
    public int determineSingleMove(Board board);
    public int[] determineDoubleMove(Board board);
}

