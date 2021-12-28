package mancala;

import static io.vavr.Predicates.is;
import static io.vavr.Predicates.not;

import java.util.Optional;
import one.util.streamex.StreamEx;

@lombok.Data
public class Game {
  private final Player[] players;
  private final Board board;
  private final State state;

  public Game(final Player player1, final Player player2, final Board board, final State state) {
    this(new Player[] {player1, player2}, board, state);
  }

  public Game(final Player player1, final Player player2) {
    this(
        new Player[] {player1, player2},
        Board.createStartingBoard(),
        new State.AwaitingPlayerChoice(player1));
  }

  Game(final Player[] players, final Board board, final State state) {
    this.players = players;
    this.board = board;
    this.state = state;
  }

  public Player getOtherPlayer(final Player player) {
    return StreamEx.of(players).findFirst(not(is(player))).orElseThrow();
  }

  public Game progress() {
    return state.progressGame(this);
  }

  @lombok.Value
  @lombok.EqualsAndHashCode(callSuper = true)
  public static class FinishedGame extends Game {
    private final Optional<Player> winningPlayer;

    public FinishedGame(final Game game, final Optional<Player> winningPlayer) {
      super(game.getPlayers(), game.getBoard(), game.getState());
      this.winningPlayer = winningPlayer;
    }

    public boolean isDraw() {
      return winningPlayer.isEmpty();
    }
  }
}
