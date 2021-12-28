package mancala;

import static io.vavr.Predicates.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import mancala.Game.FinishedGame;
import one.util.streamex.StreamEx;
import org.assertj.core.api.Condition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GameTest {
  @MethodSource
  @ParameterizedTest(name = "[{index}] {0}")
  void shouldWorkAsExpected(
      final String description,
      final IntFunction<Player> playerOneFactory,
      final IntFunction<Player> playerTwoFactory,
      final Consumer<FinishedGame> gameAssertions) {
    final Player playerOne = playerOneFactory.apply(1);
    final Player playerTwo = playerTwoFactory.apply(2);

    Game game = new Game(playerOne, playerTwo);

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

    assertThat(finishedGame).satisfies(gameAssertions);
  }

  static Stream<Arguments> shouldWorkAsExpected() {
    return StreamEx.of(
        arguments(
            "Bad v Bad | Player 1 Wins",
            playerFactory(BadPlayer::new),
            playerFactory(BadPlayer::new),
            finishedGameAssertions(game -> assertThat(game).satisfies(playerWon(1)))),
        arguments(
            "Better v Bad | Player 1 Wins",
            playerFactory(BetterPlayer::new),
            playerFactory(BadPlayer::new),
            finishedGameAssertions(game -> assertThat(game).satisfies(playerWon(1)))),
        arguments(
            "Bad v Better | Player 2 Wins",
            playerFactory(BadPlayer::new),
            playerFactory(BetterPlayer::new),
            finishedGameAssertions(game -> assertThat(game).satisfies(playerWon(2)))),
        arguments(
            "Better v Better | Player 2 Wins",
            playerFactory(BetterPlayer::new),
            playerFactory(BetterPlayer::new),
            finishedGameAssertions(game -> assertThat(game).satisfies(playerWon(2)))),
        arguments(
            "Pirate v Better | Player 1 Wins",
            playerFactory(PiratePlayer::new),
            playerFactory(BetterPlayer::new),
            finishedGameAssertions(game -> assertThat(game).satisfies(playerWon(1)))),
        arguments(
            "Bad v Pirate | Player 2 Wins",
            playerFactory(BadPlayer::new),
            playerFactory(PiratePlayer::new),
            finishedGameAssertions(game -> assertThat(game).satisfies(playerWon(2)))),
        arguments(
            "Pirate v Pirate | Player 1 Wins",
            playerFactory(PiratePlayer::new),
            playerFactory(PiratePlayer::new),
            finishedGameAssertions(game -> assertThat(game).satisfies(playerWon(1)))));
  }

  private static Consumer<FinishedGame> finishedGameAssertions(final Consumer<FinishedGame> value) {
    return value;
  }

  private static IntFunction<Player> playerFactory(final IntFunction<Player> value) {
    return value;
  }

  private static Condition<FinishedGame> playerWon(final Integer playerNumber) {
    return new Condition<>(
        game ->
            game.getWinningPlayer()
                .map(Player::getPlayerNumber)
                .map(playerNumber::equals)
                .orElse(false),
        "Player %d won",
        playerNumber);
  }
}
