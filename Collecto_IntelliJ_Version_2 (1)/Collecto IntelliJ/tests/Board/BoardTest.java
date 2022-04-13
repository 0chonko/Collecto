// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Board;

import Board.Board;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Test.*;

class BoardTest {

    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
        board.initBoard();
    }


    @Test
    void checkBoardValidity() {
        assertEquals(0, board.getTiles()[3][3]);
        assertFalse(!board.singleMovePossible() && !board.doubleMovePossible());
        assertEquals(4, board.possibleMovements().size());
    }

    @Test
    void testWinner() {
        while (board.singleMovePossible() || board.doubleMovePossible()) {
            if (board.singleMovePossible()) {
                board.moveLine(board.singleMovesList().get(0));
                board.removeBalls();
            } else if (board.doubleMovePossible() && !board.singleMovePossible()) {
                int[] moves = new int[2];
                moves[0] = board.doubleMovesList().get(0)[0];
                moves[1] = board.doubleMovesList().get(0)[1];
                board.toString();

                board.moveLine(moves[0]);
                board.moveLine(moves[1]);
                //System.out.println("hey");
                if (board.adjacentBallsPresent()) {
                    board.removeBalls();
                }
            }
        }
        assertTrue(board.gameOver());
    }

    @Test
    public void testDeepCopy() {
        board.setTile(3, 4, 6);
        board.toString();
        Board deepCopyBoard = board.deepCopy();
        deepCopyBoard.setTile(3, 4, 5);

        assertEquals(6, board.getTileColor(3, 4));
        assertEquals(5, deepCopyBoard.getTileColor(3, 4));
        assertTrue(deepCopyBoard != board);
    }

    @Test
    void testMovesLists() {
        //TODO: simulate a board with n single moves and compare if the actual board's moves matches
        Board copyBoard = board.deepCopy();
        resetBoardForTest(copyBoard);
        assertTrue(copyBoard.gameOver());
        copyBoard.setTileRow(1, 2, 6);
        copyBoard.setTileRow(1, 5, 6);
        assertEquals(2, copyBoard.singleMovesList().size());

        copyBoard.setTileRow(1, 4, 3);
        copyBoard.setTileRow(0, 0, 3);
        assertTrue(copyBoard.doubleMovesList().size() >= 1);
    }

    @Test
    void testSingleMoveValidity() {
        //TODO: do wrong move and assert false, predifined board
        assertTrue(board.singleMovePossible());
        board.moveLine(board.singleMovesList().get(0));
        assertTrue(board.adjacentBallsPresent());
        board.removeBalls();
        if (board.singleMovePossible()) {
            board.moveLine(board.singleMovesList().get(0));
        }
        assertTrue(board.adjacentBallsPresent());

        resetBoardForTest(board);
        board.setTile(0, 0, 6);
        board.setTile(1, 5, 6);
        assertFalse(board.singleMoveValid(7));
        assertTrue(board.singleMoveValid(15));
        board.setTile(0, 6, 6);
        assertTrue(board.singleMoveValid(14));
        assertTrue(board.singleMoveValid(21));

    }

    @Test
    public void testAdjacentBallsDetection() {
        board.moveLine(board.singleMovesList().get(0));
        assertTrue(board.adjacentBallsPresent());
        assertTrue(board.adjacentBalls().size() >= 2);
        board.setTileRow(0, 0, 1);
        board.setTileRow(0, 1, 1);
        assertEquals(0, board.adjacentBalls().get(0)[0]);
        assertEquals(0, board.adjacentBalls().get(0)[1]);
        assertEquals(1, board.adjacentBalls().get(1)[0]);
        assertEquals(0, board.adjacentBalls().get(1)[1]);
    }

    @Test
    void testDoubleMoveValidity() {
        while (board.singleMovePossible()) {
            board.moveLine(board.singleMovesList().get(0));
            board.removeBalls();
            board.toString();
            System.out.println(" ");
            System.out.println(" ");
        }
        assertFalse(board.singleMovePossible());
        assertTrue(board.doubleMoveValid(board.doubleMovesList().get(0)[0],
                board.doubleMovesList().get(0)[1]));

        resetBoardForTest(board);
        board.setTile(0, 0, 6);
        board.setTile(6, 6, 6);
        assertTrue(board.doubleMovesList().size() >= 1);
        assertTrue(board.doubleMoveValid(21, 13));
        assertTrue(board.doubleMoveValid(7, 27));
        assertFalse(board.doubleMoveValid(5, 2));
    }

    @Test
    void testLinesMovement() {
        board.moveLine(3);
        assertTrue(board.getRow(3)[board.DIM - 1] == 0);
        assertTrue(board.getRow(3)[(board.DIM - 1) / 2] != 0);
        board.moveLine(20);
        assertEquals(board.getRow(board.DIM - 1)[board.DIM - 1], 0);
        assertNotEquals(board.getRow(3)[board.DIM - 1], 0);

        resetBoardForTest(board);
        board.setTile(0, 0, 6);
        board.moveLine(21);
        assertEquals(6, board.getTileColor(0, 6));
    }

    @Test
    void commandSorting() {
        assertEquals(6, board.commandIndex(6));
        assertEquals(5, board.commandIndex(12));
        assertEquals(4, board.commandIndex(18));
        assertEquals(4, board.commandIndex(25));
        assertEquals(0, board.commandIndex(7));
        assertEquals(0, board.commandIndex(0));
        assertEquals(0, board.commandIndex(21));
        assertEquals(0, board.commandIndex(14));
    }

    @Test
    public void testScoring() {
        int[] tiles = new int[6];
        tiles[4] = 5;
        assertEquals(1, board.scoreCounter(tiles));
        tiles[4]++;
        tiles[0] = 4;
        assertEquals(3, board.scoreCounter(tiles));
    }

    @Test
    void testBallRemoval() {
        int[] scored = new int[6];
        assertFalse(board.adjacentBallsPresent());
        board.moveLine(board.singleMovesList().get(0));
        assertTrue(board.adjacentBallsPresent());
        System.arraycopy(board.removeBalls(), 0, scored, 0, scored.length);
        assertFalse(board.adjacentBallsPresent());

        resetBoardForTest(board);
        board.setTile(0, 0, 5);
        board.setTile(0, 1, 5);
        assertEquals(5, board.getTileColor(0, 0));
        assertEquals(5, board.getTileColor(0, 1));
        board.removeBalls();
        assertEquals(0, board.getTileColor(0, 0));
        assertEquals(0, board.getTileColor(0, 1));

    }

    @Test
    void testSetTile() {
        board.setTileRow(3, 2, 0);
        board.setTileColumn(4, 2, 0);

        assertEquals(0, board.getTiles()[2][3]);
        assertEquals(0, board.getTiles()[4][2]);

        board.setTileRow(3, 2, 6);
        board.setTileColumn(4, 2, 5);

        assertEquals(6, board.getTiles()[2][3]);
        assertEquals(5, board.getTiles()[4][2]);
    }

    @Test
    public void testFreeRowSpace() {
        resetBoardForTest(board);
        for (int i = 0; i < board.DIM; i++) {
            board.setTile(i, 0, 6);
        }
        assertFalse(board.rowFreeSpace(0));
        board.setTile(3, 0, 0);
        assertTrue(board.rowFreeSpace(0));
    }

    @Test
    public void testFreeColumnSpace() {
        resetBoardForTest(board);
        for (int i = 0; i < board.DIM; i++) {
            board.setTile(3, i, 6);
        }
        assertFalse(board.columnFreeSpace(3));
        board.setTile(3, 5, 0);
        assertTrue(board.columnFreeSpace(3));
    }


    @Test
    public void testCommandValidity() {
        assertFalse(board.isMove(54));
        assertFalse(board.isMove(28));
        assertFalse(board.isMove(-1));
        assertTrue(board.isMove(3));
    }

    @Test
    public void testGetRowOfIndex() {
        resetBoardForTest(board);
        for (int i = 0; i < board.DIM; i++) {
            board.setTile(i, 3, 2);
        }

        int[] n = board.getRow(3);
        assertEquals(2, n[0]);
        assertEquals(2, n[1]);
        assertEquals(2, n[2]);
        assertEquals(2, n[3]);
        assertEquals(2, n[4]);
        assertEquals(2, n[5]);

    }

    @Test
    public void testGetColumnOfIndex() {
        resetBoardForTest(board);
        for (int i = 0; i < board.DIM; i++) {
            board.setTile(2, i, 6);
        }

        int[] n = board.getColumn(2);
        assertEquals(6, n[0]);
        assertEquals(6, n[1]);
        assertEquals(6, n[2]);
        assertEquals(6, n[3]);
        assertEquals(6, n[4]);
        assertEquals(6, n[5]);
    }


    void resetBoardForTest(Board board) {
        for (int y = 0; y < board.DIM; y++) {
            for (int x = 0; x < board.DIM; x++) {
                board.setTile(x, y, 0);
            }
        }
    }
}