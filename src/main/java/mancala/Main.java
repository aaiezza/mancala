package mancala;

import static io.vavr.Predicates.not;

import java.util.concurrent.atomic.AtomicInteger;
import mancala.Game.FinishedGame;
import mancala.player.ConsolePlayer;
import mancala.player.PiratePlayer;
import one.util.streamex.StreamEx;

public class Main {
  public static void main(final String[] args) {
    final ConsolePlayer playerOne = new ConsolePlayer(1);
    final PiratePlayer playerTwo = new PiratePlayer(2);

    final Game game = new Game(playerOne, playerTwo);

    final AtomicInteger i = new AtomicInteger();

    final FinishedGame finishedGame =
        StreamEx.iterate(
                game,
                g -> {
                  System.out.printf("◊› %-3d | %s%n", i.incrementAndGet(), g.getState());
                  System.out.println(g.getBoard());
                  return g.progress();
                })
            .dropWhile(not(FinishedGame.class::isInstance))
            .limit(1)
            .select(FinishedGame.class)
            .findFirst()
            .get();

    System.out.println(
        finishedGame
            .getWinningPlayer()
            .map(winner -> String.format("Outcome: Winner is Player %d", winner.getPlayerNumber()))
            .orElse("Outcome: Draw!"));
  }
}
