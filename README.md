# Collecto

Collecto is a board game developed in Java as a class project. The goal of the game is to move tiles on a 7x7 grid to combine 3 or more of the same color and score points.

## Gameplay

- Two players take turns moving either a single row or column of tiles
- Movement wraps around the edges of the board
- Combining 3 or more tiles of the same color removes those tiles and awards the player points
- The first player unable to make a legal move loses

## Code Structure

The core code is organized into packages:

- `Board` - Defines the game board and logic
- `Game` - Manages a single game instance 
- `Player` - Abstract player class and AI/human implementations
- `ClientServer` - Network communication classes
- `Views` - User interface classes

Key classes include:

- `Board` - Main gameplay class storing the tile grid
- `Game` - Stores board state and player scores
- `HumanPlayer`/`ComputerPlayer` - Player implementations 
- `Server`/`Client` - Server/client socket classes

The game is playable either locally or over a network. The server manages game state while clients send/receive moves.

## Running the Game

To run a local single-player game:

```
java Controller
``` 

To start the server:

```
java Server
```

Then from another terminal run the client: 

```
java Client
```

Enjoy! The game is fully functional but some improvements could be made around AI strategies, UI polish, and bug fixes.
