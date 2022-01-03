package mancala.player;

import java.io.Closeable;
import java.io.IOException;
import java.util.Scanner;
import mancala.Game;
import mancala.Move;
import mancala.Move.IllegalMoveException;
import mancala.Player;

public class ConsolePlayer extends Player implements Closeable {
  private final Scanner sc;

  public ConsolePlayer(final int playerNumber) {
    super(playerNumber);
    sc = new Scanner(System.in);
  }

  @Override
  public Move makeMove(final Game game) throws IllegalMoveException {
    System.out.print("Enter move [0-5]: ");
    return new Move(game.getBoard(), this, Integer.parseInt(sc.nextLine().trim()));
  }

  @Override
  public void inform(final Exception exception) {
    System.err.printf("Should not have happened: %s%n", exception.getMessage());
  }

  @Override
  public void close() throws IOException {
    sc.close();
  }
}
