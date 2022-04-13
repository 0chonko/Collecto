// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
// This class defines the game's board functionalities and GUI

package Board;

import Constants.ColorCodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {

    public static final int DIM = 7;
    public static final int[] TILES = {1, 2, 3, 4, 5, 6};
    /**
     * The DIM by DIM fields of the Tic Tac Toe board. The possible colors
     * are represented by TILES (6 colors)
     *
     * @invariant there are always DIM*DIM fields
     * @invariant rowBoard is always DIM*DIM long
     * @invariant all fields are either 1, 2, 3, 4, 5, or 6.
     */

    public int[] rowBoard = new int[DIM * DIM];
    public int[][] fields = new int[DIM][DIM];


    /**
     * This method randomly generates the tiles of the board, checks for their validity (no adjacent tiles of same
     * color allowed), checks if board has initial moves, and sets the 2D board array.
     *
     * @ensures all fields are full except the middle one
     * @ensures a possible initial move (single or double)
     * @ensures no adjacent balls
     * @ensures the 2D array is generated in a cartesian format x y
     */
    public void initBoard() {
        //checks the amount of tiles per color
        int[] checker = new int[6];
        //generate random and fill it into board array
        //if the randomly generated number is not valid, the program will generate a new one
        for (int i = 0; i < DIM * DIM; i++) {
            if (i != Math.floorDiv(DIM * DIM, 2)) { //do not set the middle slot
                if (i == 0) {   //first element of the board
                    int n = getRandomIntegerBetweenRange(TILES[0], TILES.length);
                    this.rowBoard[i] = n;
                    checker[n - 1] += 1;
                } else if (i < 7) {     //first row
                    int n = getRandomIntegerBetweenRange(TILES[0], TILES.length);

                    while (n == this.rowBoard[i - 1]) {
                        n = getRandomIntegerBetweenRange(TILES[0], TILES.length);
                    }
                    this.rowBoard[i] = n;
                    checker[n - 1] += 1;

                } else if (i != 0 && i % 7 == 0) {      //first element of each row doesn't need to check the previous element
                    int n = getRandomIntegerBetweenRange(TILES[0], TILES.length);

                    while (n == this.rowBoard[i - DIM]) {
                        n = getRandomIntegerBetweenRange(TILES[0], TILES.length);
                    }
                    this.rowBoard[i] = n;
                    checker[n - 1] += 1;

                } else {       //all the remaining tiles of the board
                    int n = getRandomIntegerBetweenRange(TILES[0], TILES.length);
                    while (n == this.rowBoard[i - 1] || n == this.rowBoard[i - DIM]) {
                        n = getRandomIntegerBetweenRange(TILES[0], TILES.length);
                    }
                    this.rowBoard[i] = n;
                    checker[n - 1] += 1;
                }
                //the board is now full: check whether it is valid by counting the amount of tiles of each color
                //and the possible moves
            }
            for (int j : checker) {
                if (j > 8) {
                    checker = new int[6];
                    rowBoard = new int[DIM * DIM];
                    i = -1;
                }
            }
        }
        //set the middle slot of the board to empty
        this.rowBoard[Math.floorDiv(DIM * DIM, 2)] = 0;

        //generate 2D board array
        for (int y = 0; y < DIM; y++) {
            for (int x = 0; x < DIM; x++) {
                this.fields[x][y] = this.rowBoard[7 * y + x];
            }
        }
        //check if the board has initial moves, otherwise re-initiate the board
        if (!singleMovePossible() && !doubleMovePossible()) {
            System.out.println("board re-initiated");
            synchronized (this) {
                initBoard();
            }
        }
    }


    /**
     * This method makes use of the raw board array to generate a two dimensional board array for an easier cartesian access
     * to the slots on the board
     *
     * @return the two dimensional array of board tiles
     */

    public int[][] getTiles() {
        return this.fields;
    }


    /**
     * This method generates a random primitive integer in range (min, max)
     *
     * @return randomly generated integer
     * @requires min and max to be not null and integer
     * @ensures the range of the randomly generated number is in range (min, max) included
     */

    public int getRandomIntegerBetweenRange(int min, int max) {
        int x = (int) (Math.random() * ((max - min) + 1)) + min;
        assert x > 0;
        return x;
    }

    /**
     * This method checks which are the lines that can be moved and returns the list
     * of the possible commands in the moment of the execution of the method.
     *
     * @return list of currently available commands
     * @ensures array list of all currently possible moves, even if not resulting in adjacent balls
     */
    public List<Integer> possibleMovements() {
        List<Integer> possibleMoves = new ArrayList<Integer>();

        for (int i = 0; i < DIM * 4; i++) { //dim*4 = amount of commands
            if (rowFreeSpace(commandIndex(i)) || columnFreeSpace(commandIndex(i))) {
                possibleMoves.add(i);
            }
        }
        return possibleMoves;
    }

    /**
     * This method checks whether there is at least one valid single move possible in the current state of the game.
     *
     * @return True if there is at least one valid single move possible
     * @ensures True if single move possible
     */

    public boolean singleMovePossible() {
        return singleMovesList().size() != 0;
    }

    /**
     * This method checks whether there is at least one valid double move possible in the current state of the game.
     *
     * @return True if there is at least one valid double move possible
     * @ensures True if double move is possible
     */

    public boolean doubleMovePossible() {
        return doubleMovesList().size() != 0;
    }

    /**
     * This method checks whether the move in the argument of the method is a valid single move.
     *
     * @param move represents the command or line to move and direction
     * @return True if the single move leads to at least one pair of adjacent balls of the same color
     * @ensures True if the single move results in adjacent balls
     * @requires move to be in range 0-27
     */

    public boolean singleMoveValid(int move) {
        //if single move possible and results in adjacent, then true
        //try the move on a copied version of the board
        //check for all the possible commands (rows/cols with free spaces) and add them to the list
        if (possibleMovements().contains(move)) {
            Board b = deepCopy();
            b.moveLine(move);
            return b.adjacentBallsPresent();
        }
        return false;
    }

    /**
     * This method checks whether the two moves in the argument of the method are valid
     * for a double move.
     *
     * @param move1 represents the first command or line to move and direction
     * @param move2 represents the second command or line to move and direction
     * @return True if there are no single moves available, and if the first move gives
     * the possibility to the second move to lead to at least one pair of adjacent balls
     * of the same color.
     * @requires move to be in range 0-27
     * @requires move1 not equal to move2
     * @requires move1 not equal to the opposite of move2
     * @ensures True if the combination of move1 and move2 is valid for
     * @ensures True if valid and if single move not possible
     */

    public boolean doubleMoveValid(int move1, int move2) {
        //if single valid move not possible
        //if second move (combined with the first) is valid then return true
        //second move might become available only after move1's execution
        //try the moves on a copied version of the board

        if (possibleMovements().contains(move1)     //check if the first move can be moved
                && singleMovesList().size() == 0) { //check
            Board b = deepCopy();
            b.moveLine(move1);                      //execute the move
            //check if the copy of the board now has the valid move
            return b.singleMoveValid(move2);

        }
        return false;
    }

    /**
     * This method checks for all the possible valid single moves on the board and adds
     * the corresponding commands to a list of Integers
     *
     * @return the list of all possible valid single moves
     * @ensures List to contain all the possible single moves of the current state
     * @ensures all moves are in range 0-27
     */

    public List<Integer> singleMovesList() {
        List<Integer> singleMovesList = new ArrayList<Integer>();
        //all valid single moves
        for (int i : possibleMovements()) {
            if (singleMoveValid(i)) {
                singleMovesList.add(i);
            }
        }
        return singleMovesList;
    }

    /**
     * This method checks for all the possible valid double moves on the board and adds
     * the corresponding commands to a list of Integer arrays.
     *
     * @return the list of all possible valid double moves
     * @ensures List to contain all the possible double moves of the current state
     * @ensures all moves are in range 0-27
     */

    public List<Integer[]> doubleMovesList() {
        //check for all the second moves or just for the ones available
        //all valid double moves
        List<Integer[]> doubleMovesList = new ArrayList<>();
        //all valid single moves
        //could check only the available ones
        for (int i : possibleMovements()) {
            for (int j = 0; j < DIM * 4; j++) {
                if (doubleMoveValid(i, j)) {
                    //expensivest, but the easiest to execute. Taken in consideration that there
                    //won't be lots of doubleMoves during gameplay
                    // (32 bytes allocation instead of 8 bytes per array)
                    doubleMovesList.add(storeIntCouple(i, j));
                }
            }
        }
        return doubleMovesList;
    }

    /**
     * This method checks whether the column of index in the parameter has at least one
     * free space.
     *
     * @param index of the column
     * @return True if the column has at least one free space
     * @requires index to be in range 0 - 6
     * @ensures True if column of index has at least one empty space
     */

    public boolean columnFreeSpace(int index) {
        for (int y = 0; y < DIM; y++) {
            if (getTiles()[index][y] == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks whether the row of index in the parameter has at least one
     * free space.
     *
     * @param index of the row
     * @return True if the row has at least one free space
     * @requires index to be in range 0 - 6
     * @ensures True if row of index has at least one empty space
     */

    public boolean rowFreeSpace(int index) {
        for (int x = 0; x < DIM; x++) {
            if (getTiles()[x][index] == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method removes the adjacent balls on the board and counts the colors of each
     * removed ball.
     *
     * @return array of integers representing the amount of balls per color.
     * @requires adjacent balls to be present on the board
     * @ensures all adjacent balls are removed
     * @ensures color value of each removed ball is stored and returned
     */

    public int[] removeBalls() {
        //check for all adjacent balls
        int[] colorChecker = new int[TILES.length];
        if (adjacentBallsPresent()) {
            List<Integer[]> copy = new ArrayList<>(adjacentBalls().size());

            for (Integer[] ints : adjacentBalls()) { //copy the array of current adjacent
                copy.add(ints.clone());
            }

            for (Integer[] integers : copy) {
                int x = integers[0];
                int y = integers[1];
                colorChecker[getTileColor(x, y) - 1]++;     //store the color of the removed ball for score
                setTile(x, y, 0);
            }

            return colorChecker;
        } else {
            return null;
        }
    }

    /**
     * This method moves the lines up, down, left, or right.
     * The method iterates from left in case of moving left or up.
     * The method iterates from right in case of moving right or down.
     *
     * @param command between 0-27
     * @requires command to be in range 0-27
     * @requires command to be in the list of possible single/double moves lists
     * @ensures all the tiles of the row/column targeted by the command are moved
     * in the respective direction
     * @ensures there are no empty spaces between the tiles after the move
     * @ensures there are no duplicates and if the tile has been moved and the space
     * was not replaced by another tile, the space is set to zero
     */

    public void moveLine(int command) {

        int lastChecked = 0;
        int index = commandIndex(command);
        if (commandDirection(command) == 0 || commandDirection(command) == 2) {
            //iterate from the left
            for (int i = 0; i < DIM; i++) {
                if (commandRowColumn(command) && rowFreeSpace(index)) {    //if row
                    if (getRow(index)[i] != 0) {    //if not empty
                        if (i != lastChecked) {     //check if needs to be moved
                            setTileRow(index, lastChecked, getTileColor(i, index));
                            setTileRow(index, i, 0);
                        }
                        lastChecked++;
                    }
                } else if (!commandRowColumn(command) && columnFreeSpace(index)) {                           //if column
                    if (getColumn(index)[i] != 0) {  //if not empty
                        if (i != lastChecked) {      //check if needs to be moved
                            setTileColumn(index, lastChecked, getTileColor(index, i)); //move the tile
                            setTileColumn(index, i, 0); //delete the duplicate
                        }
                        lastChecked++;
                    }
                }
            }
        } else if (commandDirection(command) == 1 || commandDirection(command) == 3) {
            //iterate from right
            lastChecked = DIM - 1;
            for (int i = DIM - 1; i >= 0; i--) {
                //if row
                if (commandRowColumn(command) && rowFreeSpace(index)) {    //if row
                    if (getRow(index)[i] != 0) {    //if not empty
                        if (i != lastChecked) {     //check if needs to be moved
                            setTileRow(index, lastChecked, getTileColor(i, index)); //move the tile
                            setTileRow(index, i, 0);    //delete the duplicate
                        }
                        lastChecked--;
                    }
                } else if (!commandRowColumn(command) && columnFreeSpace(index)) {                            //if column
                    if (getColumn(index)[i] != 0) { //if not empty
                        if (i != lastChecked) {     //check if needs to be moved
                            setTileColumn(index, lastChecked, getTileColor(index, i));  //move the tile
                            setTileColumn(index, i, 0);  //delete the duplicate
                        }
                        lastChecked--;
                    }
                }
            }
        }

    }

    /**
     * This method creates a copy of the board with it's current state.
     *
     * @return new Board copy of the board
     * @ensures the result is a new object, so not this object
     * @ensures the values of all fields of the copy match the ones of this Board
     */

    public Board deepCopy() {
        Board newBoard = new Board();

        for (int y = 0; y < DIM; y++) {
            for (int x = 0; x < DIM; x++) {
                newBoard.setTile(x, y, getTiles()[x][y]);
            }
        }
        return newBoard;
    }

    /**
     * This method checks whether there are any adjacent balls present on the board.
     *
     * @return True if there's at least one pair of adjacent balls on the board.
     * @ensures True if there's at least one pair of adjacent balls on the board.
     */

    public boolean adjacentBallsPresent() {
        return adjacentBalls().size() != 0;
    }


    /**
     * This method checks whether the move is valid
     *
     * @param move between 0-27
     * @return False is the move not in range
     * @ensures True if the command is in the range 0-27
     */

    public boolean isMove(int move) {
        return (move >= 0 && move < DIM * 4);
    }


    /**
     * This method calculates the store of the incoming array of tiles
     *
     * @param tiles containes the array of scored tiles
     * @return one point for every 3 tiles of the same color.
     * @requires command to be in range 0-27
     * @ensures a point to be added for every 3 balls of the same color in tiles
     */

    public int scoreCounter(int[] tiles) {
        int count = 0;

        for (int i = 0; i < tiles.length; i++) {
            System.out.println(count);
            count += Math.floorDiv(tiles[i], 3);
        }
        return count;
    }

    /**
     * This method sorts the possible commands based on the direction, which are:
     * left, right, up, down.
     *
     * @param command between 0-27
     * @return the direction index between 0 and 3 where:
     * <p>
     * 0 stands for left
     * 1 stands for right
     * 2 stands for up
     * 3 stands for down
     * @requires command to be in range 0-27
     * @ensures a different direction index for every 7 commands
     * @ensures left to return 1, right to return 2, up to return 3, down to return 4
     */

    public int commandDirection(int command) {
        if (command >= 0 && command < DIM) {
            //left
            return 0;
        } else if (command >= DIM && command < 2 * DIM) {
            //right
            return 1;
        } else if (command >= 2 * DIM && command < 3 * DIM) {
            //up
            return 2;
        } else if (command >= 3 * DIM && command < 4 * DIM) {
            //down
            return 3;
        }
        return -1;
    }

    /**
     * This method checks the command is moving a row or a column
     *
     * @param command between 0-27
     * @return True if it is a row, false if a column.
     * @requires command to be in range 0-27
     * @ensures True if the command indicates a row movement
     */

    public boolean commandRowColumn(int command) {
        switch (commandDirection(command)) {
            case 0:
            case 1:
                return true;
            default:
                return false;
        }
    }

    /**
     * This method returns the row/col index which should be moved by the command
     *
     * @param command between 0-27
     * @return an integer between 0 and 6 which represents the row/column index
     * @requires command to be in range 0-27
     * @ensures command to be mapped in a range of 0-6 of the possible row/col indexes
     * @ensures all the commands representing a specific row to return the same index value
     */

    public int commandIndex(int command) {
        //row
        if (commandRowColumn(command)) {
            //return indexes of commands  0-6 and 7-14
            if (command - DIM < 0) {
                return command;
            } else {
                return command - DIM;
            }
        } else {
            //return indexes of commands  14-20 and 21-27
            if (command - (3 * DIM) < 0) {
                return command - (2 * DIM);
            } else {
                return command - (3 * DIM);
            }
        }
    }

    /**
     * This method returns the tiles in the row of index in the parameter.
     *
     * @param index between 0-6
     * @return array of tiles in the row.
     * @ensures every value of row index to be returned in an array
     * @requires command to be in range 0-6
     */

    public int[] getRow(int index) {
        int[] row = new int[DIM];

        for (int i = 0; i < DIM; i++) {
            row[i] = getTiles()[i][index];
        }
        return row;
    }

    /**
     * This method returns the tiles in the column of index in the parameter.
     *
     * @param index between 0-6
     * @return array of tiles in the column.
     * @ensures every value of column index to be returned in an array
     * @requires command to be in range 0-6
     */

    public int[] getColumn(int index) {
        int[] column = new int[DIM];
        for (int i = 0; i < DIM; i++) {
            column[i] = getTiles()[index][i];
        }
        return column;
    }

    /**
     * This method sets a tile to a new tile in a specific column of index colIndex.
     *
     * @param colIndex  represents the index of the column
     * @param tileIndex represents the index of the tile in the column colIndex
     * @param newTile   represents the new tile color
     * @requires colIndex, tileIndex, and newTile to be in range 0-6
     * @ensures that the specified tile in a column is set to newTile
     */

    public void setTileColumn(int colIndex, int tileIndex, int newTile) {
        this.fields[colIndex][tileIndex] = newTile;
    }

    /**
     * This method sets a tile to a new tile in a specific row of index rowIndex.
     *
     * @param rowIndex  represents the index of the row
     * @param tileIndex represents the index of the tile in the row rowIndex
     * @param newTile   represents the new tile color
     * @requires rowIndex, tileIndex, and newTile to be in range 0-6
     * @ensures that the specified tile in a row is set to newTile
     */

    public void setTileRow(int rowIndex, int tileIndex, int newTile) {
        this.fields[tileIndex][rowIndex] = newTile;
    }

    /**
     * This method sets a tile to a new tile by using regular cartesian coordinate x y.
     *
     * @param colIndex represents the index of the column
     * @param rowIndex represents the index of the tile in the column
     * @param newTile  represents the new tile color
     * @requires colIndex, rowIndex, and newTile to be in range 0-6
     * @ensures that the specified tile in a coordinate (colIndex, rowIndex) is set to newTile
     */

    public void setTile(int rowIndex, int colIndex, int newTile) {
        this.fields[rowIndex][colIndex] = newTile;
    }

    /**
     * Checks the color of a specific coordinate on the board and returns the color stored in that board
     *
     * @param colIndex represents the index of the column
     * @param rowIndex represents the index of the tile in the column
     * @return int value of the tile color
     * @requires colIndex and rowIndex to be in range 0-6
     * @ensures that the specified tile in a coordinate (colIndex, rowIndex) returns the
     * value of the tile.
     */

    public int getTileColor(int rowIndex, int colIndex) {
        return this.fields[rowIndex][colIndex];
    }

    /**
     * This method checks whether the game has reached its' end.
     *
     * @return True if there are no possible moves remaining.
     * @ensures True if no single or double moves are possible
     */

    public boolean gameOver() {
        return !singleMovePossible() && !doubleMovePossible();
    }

    /**
     * This method checks for all the adjacent balls currently present on the board.
     * The coordinates of each adjacent ball on the board is returned in
     * an array of Integers.
     *
     * @return the coordinates of all the adjacent balls on the board in
     * a two dimensional array list.
     * @ensures All the adjacent balls of the same color are added to the array (even if
     * there are multiple pairs of different colors).
     * @ensures that the comparisons never check the indexes that are not in the board limits
     * @ensures that empty spaces are not considered as adjacent
     */
    public List<Integer[]> adjacentBalls() {
        //count adjacent balls
        List<Integer[]> adjacentBallsCords = new ArrayList<>();
        for (int y = 0; y < DIM; y++) {
            for (int x = 0; x < DIM; x++) {
                if (getTileColor(x, y) != 0) {
                    switch (x) {
                        case 0:
                            if (y == 0) {
                                //check whether x+1 or y+1
                                if (getTileColor(x, y) == getTileColor(x + 1, y)
                                        || getTileColor(x, y) == getTileColor(x, y + 1)) {
                                    //if found any adjacency, increase
                                    // the count of the specific color index by 1
                                    adjacentBallsCords.add(storeIntCouple(x, y));
                                }

                            } else if (y == DIM - 1) {
                                //check whether x+1 or y-1
                                if (getTileColor(x, y) == getTileColor(x + 1, y)
                                        || getTileColor(x, y) == getTileColor(x, y - 1)) {
                                    adjacentBallsCords.add(storeIntCouple(x, y));
                                }

                            } else {
                                //check whether x+1 and y-1 and y+1
                                if (getTileColor(x, y) == getTileColor(x + 1, y)
                                        || getTileColor(x, y) == getTileColor(x, y - 1)
                                        || getTileColor(x, y) == getTileColor(x, y + 1)) {
                                    adjacentBallsCords.add(storeIntCouple(x, y));
                                }

                            }
                            break;
                        case DIM - 1:
                            if (y == 0) {
                                //check whether x-1 or y+1
                                if (getTileColor(x, y) == getTileColor(x - 1, y)
                                        || getTileColor(x, y) == getTileColor(x, y + 1)) {
                                    adjacentBallsCords.add(storeIntCouple(x, y));
                                }

                            } else if (y == DIM - 1) {
                                //check whether x-1 or y-1
                                if (getTileColor(x, y) == getTileColor(x - 1, y)
                                        || getTileColor(x, y) == getTileColor(x, y - 1)) {
                                    adjacentBallsCords.add(storeIntCouple(x, y));
                                }


                            } else {
                                //check whether x-1 and y-1 and y+1
                                if (getTileColor(x, y) == getTileColor(x - 1, y)
                                        || getTileColor(x, y) == getTileColor(x, y - 1)
                                        || getTileColor(x, y) == getTileColor(x, y + 1)) {
                                    adjacentBallsCords.add(storeIntCouple(x, y));
                                }


                            }
                            break;
                        default:
                            //check x-1 , x+1 , y-1 , y+1
                            if (y == 0) {
                                if (getTileColor(x, y) == getTileColor(x + 1, y)
                                        || getTileColor(x, y) == getTileColor(x, y + 1)
                                        || getTileColor(x, y) == getTileColor(x - 1, y)) {
                                    adjacentBallsCords.add(storeIntCouple(x, y));
                                }
                            } else if (y == DIM - 1) {
                                if (getTileColor(x, y) == getTileColor(x + 1, y)
                                        || getTileColor(x, y) == getTileColor(x, y - 1)
                                        || getTileColor(x, y) == getTileColor(x - 1, y)) {
                                    adjacentBallsCords.add(storeIntCouple(x, y));
                                }
                            } else {
                                if (getTileColor(x, y) == getTileColor(x + 1, y)
                                        || getTileColor(x, y) == getTileColor(x, y - 1)
                                        || getTileColor(x, y) == getTileColor(x, y + 1)
                                        || getTileColor(x, y) == getTileColor(x - 1, y)) {
                                    adjacentBallsCords.add(storeIntCouple(x, y));
                                }
                            }

                    }
                }
            }
        }
        return adjacentBallsCords;
    }

    /**
     * This method stores two ints in an array of Integer type.
     *
     * @param x represents the first value to add
     * @param y represents the second value to add
     * @return an array of type Integer
     * @requires x and y to be a primitive int
     * @ensures that x and y are stored in an Integer array
     * @ensures the Integer array to be returned
     */

    private Integer[] storeIntCouple(int x, int y) {
        Integer[] storeTile = new Integer[2];
        storeTile[0] = x;
        storeTile[1] = y;
        return storeTile;
    }

    /**
     * This method returns the visualization of the board and the
     * respective commands helpers to a string
     *
     * @return String visualization of the board
     * @ensures the board visualization to be flipped correctly
     * @ensures the delimiters to properly delimit the board
     */

    @Override
    public String toString() {
        //TODO: could go to the top in a final static
        String newLine = "\n";
        String delim = "+---+---+---+---+---+---+---+";
        String spacing = "     ";
        String leftDelim = " > |";
        String rightDelim = " < ";
        String upCommands = " 21  22  23  24  25  26  27 ";
        String lowCOmmands = " 14  15  16  17  18  19  20 ";
        String upDelim = "  v   v   v   v   v   v   v   ";
        String downDelim = "  ^   ^   ^   ^   ^   ^   ^   ";

        int[][] twoD = new int[DIM][DIM];
        String b = "";

        b += spacing + upCommands + newLine + spacing + upDelim + newLine;
        for (int x = 0; x < DIM; x++) {
            b += spacing + delim + "\n";

            for (int y = 0; y < DIM; y++) {
                twoD[x][y] = this.fields[y][x];
                if (y == 0) {
                    b += String.format("%2s", (y * DIM + x + 7)) + leftDelim;
                }

                if (twoD[x][y] != 0) {
                    b += (" " + ColorCodes.colors[twoD[x][y]] + "⚈" + ColorCodes.reset + " |");
                } else {
                    b += (" " + " " + " |");
                }

                if (y == DIM - 1) {
                    b += rightDelim + String.format("%1s", x);
                }
            }

            b += "\n";
        }
        b += spacing + delim + newLine;
        b += spacing + downDelim + newLine;
        b += spacing + lowCOmmands;
        return b;
    }
}
