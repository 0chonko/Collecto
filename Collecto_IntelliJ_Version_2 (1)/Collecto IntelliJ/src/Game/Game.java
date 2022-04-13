// File: Board.java#
//
// Authors:  German Savchenko
//           Filip Ivanov
//
//

package Game;

import Board.Board;
import ClientServer.ClientHandler;

public class Game {
    private final ClientHandler[] clients = new ClientHandler[2];
    private final Board board;

    private int[][] ballsCollected = new int[2][6];

    public Game(ClientHandler c1, ClientHandler c2, Board board) {
        this.board = board;
        clients[0] = c1;
        clients[1] = c2;

        // Init the score array
        resetScore();
    }

    public Board getBoard() {
        return board;
    }

    public ClientHandler getClient(int index) {
        return clients[index];
    }

    public int clientIndex(ClientHandler c) {
        if (clients[0].equals(c)) {
            return 0;
        } else if (clients[1].equals(c)) {
            return 1;
        } else {
            return -1;
        }
    }

    public boolean hasClient(ClientHandler c) {
        return clients[0].equals(c) || clients[1].equals(c);
    }

    public void updateBalls (ClientHandler c, int[] balls) {
        int index = clientIndex(c);

        if (index != -1) {
            for (int i = 0; i < 6; i++) {
                ballsCollected[index][i] += balls[i];
            }
        }
    }

    public int getScore (ClientHandler c) {
        int index = clientIndex(c);

        int[] scored = ballsCollected[index];

        return board.scoreCounter(scored);
    }

    public void resetScore() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                ballsCollected[i][j] = 0;
            }
        }
    }
}
