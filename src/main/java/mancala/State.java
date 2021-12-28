package mancala;

import java.util.Optional;
import mancala.Move.IllegalMoveException;
import mancala.Well.HomeBase;

public interface State {
  public Game progressGame(final Game game);

  public Player getCurrentPlayer();

  public default String getToStringPrefix() {
    return String.format(
        "Player %d: %s", getCurrentPlayer().getPlayerNumber(), this.getClass().getSimpleName());
  }

  @lombok.Value
  public static class AwaitingPlayerChoice implements State {
    private final Player currentPlayer;

    public Game progressGame(final Game game) {
      Move move = null;

      while (move == null) {
        try {
          move = currentPlayer.makeMove(game);
        } catch (final IllegalMoveException exception) {
          currentPlayer.inform(exception);
          move = null;
        }
      }

      return new Game(
          game.getPlayers(),
          game.getBoard(),
          new MakingMove(
              currentPlayer,
              move,
              game.getBoard().getWellFor(currentPlayer, move.getWellIndex()).getNumberOfBeads(),
              game.getBoard().getNextBoardWellIndexFor(currentPlayer, move.getWellIndex())));
    }

    public String toString() {
      return String.format("%s%n", getToStringPrefix());
    }
  }

  @lombok.Value
  public static class MakingMove implements State {
    private final Player currentPlayer;
    private final Move move;
    private final int beadsLeftToMove;
    private final int toBoardWellIndex;

    public Game progressGame(final Game game) {
      final Board boardAfterLastBead =
          game.getBoard().moveBeadFor(currentPlayer, move.getWellIndex(), toBoardWellIndex);

      if (beadsLeftToMove > 1) {
        return new Game(
            game.getPlayers(),
            boardAfterLastBead,
            new MakingMove(currentPlayer, move, beadsLeftToMove - 1, (toBoardWellIndex + 1) % 14));
      } else {

        // Check for NoMoreMovesLeft
        if (boardAfterLastBead.allPlayerWellsEmpty()) {
          return new Game(
              game.getPlayers(),
              boardAfterLastBead,
              new NoMoreMovesLeft(game.getOtherPlayer(currentPlayer)));
        }

        // Check for ClearBoardOfStrayBeadsOnOneSide
        if (boardAfterLastBead.allPlayerWellsEmpty(currentPlayer)) {
          return new Game(
              game.getPlayers(),
              boardAfterLastBead,
              new ClearBoardOfStrayBeadsOnOneSide(game.getOtherPlayer(currentPlayer)));
        } else if (boardAfterLastBead.allPlayerWellsEmpty(game.getOtherPlayer(currentPlayer))) {
          return new Game(
              game.getPlayers(),
              boardAfterLastBead,
              new ClearBoardOfStrayBeadsOnOneSide(currentPlayer));
        }

        // Check for Ending in home base, go again
        if (currentPlayer.getPlayerNumber() == 1 && toBoardWellIndex == 6
            || currentPlayer.getPlayerNumber() == 2 && toBoardWellIndex == 13) {
          return new Game(
              game.getPlayers(), boardAfterLastBead, new AwaitingPlayerChoice(currentPlayer));
        }

        // Check for CommittingCapture
        if (game.getBoard().wellBelongsTo(currentPlayer, toBoardWellIndex)
            && boardAfterLastBead.getWells().get(toBoardWellIndex).getNumberOfBeads() == 1
            && !game.getBoard().getOppositeWell(toBoardWellIndex).isEmpty()) {
          return new Game(
              game.getPlayers(),
              boardAfterLastBead,
              new CommittingCapture(currentPlayer, toBoardWellIndex));
        }

        // Default: AwaitingPlayerChoice
        return new Game(
            game.getPlayers(),
            boardAfterLastBead,
            new AwaitingPlayerChoice(game.getOtherPlayer(currentPlayer)));
      }
    }

    public String toString() {
      return String.format("%s | Well %2d%n", getToStringPrefix(), move.getWellIndex());
    }
  }

  @lombok.Value
  public static class CommittingCapture implements State {
    private final Player currentPlayer;
    private final int lastBoardWellIndexFromMove;

    public Game progressGame(final Game game) {
      final Board boardAfterCapture =
          game.getBoard().captureBeadsForPlayer(currentPlayer, lastBoardWellIndexFromMove);

      // Check for NoMoreMovesLeft
      if (boardAfterCapture.allPlayerWellsEmpty()) {
        return new Game(
            game.getPlayers(),
            boardAfterCapture,
            new NoMoreMovesLeft(game.getOtherPlayer(currentPlayer)));
      }

      // Check for ClearBoardOfStrayBeadsOnOneSide
      if (boardAfterCapture.allPlayerWellsEmpty(currentPlayer)) {
        return new Game(
            game.getPlayers(),
            boardAfterCapture,
            new ClearBoardOfStrayBeadsOnOneSide(game.getOtherPlayer(currentPlayer)));
      } else if (boardAfterCapture.allPlayerWellsEmpty(game.getOtherPlayer(currentPlayer))) {
        return new Game(
            game.getPlayers(),
            boardAfterCapture,
            new ClearBoardOfStrayBeadsOnOneSide(currentPlayer));
      }

      // Default: AwaitingPlayerChoice
      return new Game(
          game.getPlayers(),
          boardAfterCapture,
          new AwaitingPlayerChoice(game.getOtherPlayer(currentPlayer)));
    }

    public String toString() {
      return String.format(
          "%s | Board Well %2d%n", getToStringPrefix(), lastBoardWellIndexFromMove);
    }
  }

  @lombok.Value
  public static class ClearBoardOfStrayBeadsOnOneSide implements State {
    private final Player currentPlayer;

    public Game progressGame(final Game game) {
      return new Game(
          game.getPlayers(),
          game.getBoard().claimAllPlayerWellsFor(currentPlayer),
          new NoMoreMovesLeft(currentPlayer));
    }

    public String toString() {
      return String.format("%s%n", getToStringPrefix());
    }
  }

  @lombok.Value
  public static class NoMoreMovesLeft implements State {
    private final Player currentPlayer;

    public Game progressGame(final Game game) {
      final HomeBase playerHomeBase = (HomeBase) game.getBoard().getWellFor(currentPlayer, 6);
      final HomeBase otherPlayerHomeBase =
          (HomeBase) game.getBoard().getWellFor(game.getOtherPlayer(currentPlayer), 6);

      if (playerHomeBase.getNumberOfBeads() > otherPlayerHomeBase.getNumberOfBeads()) {
        return new Game.FinishedGame(game, Optional.of(currentPlayer));
      } else if (playerHomeBase.getNumberOfBeads() < otherPlayerHomeBase.getNumberOfBeads()) {
        return new Game.FinishedGame(game, Optional.of(game.getOtherPlayer(currentPlayer)));
      }

      return new Game.FinishedGame(game, Optional.empty());
    }

    public String toString() {
      return String.format("%s%n", getToStringPrefix());
    }
  }
}
