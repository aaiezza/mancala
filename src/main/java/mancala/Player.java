package mancala;

import mancala.Move.IllegalMoveException;

@lombok.Data
public class Player {
  private final int playerNumber;

  public Player(final int playerNumber) {
    if (playerNumber != 1 && playerNumber != 2) {
      throw new IllegalArgumentException();
    }
    this.playerNumber = playerNumber;
  }

  public Move makeMove(final Game game) throws IllegalMoveException {
    // TODO
    return null;
  }

  public void inform(final Exception exception) {
    // TODO Auto-generated method stub
  }
}
