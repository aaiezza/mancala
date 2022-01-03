package mancala.player;

import static io.vavr.Predicates.not;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import mancala.Game;
import mancala.Move;
import mancala.Move.IllegalMoveException;
import mancala.Player;
import mancala.Well.PlayerWell;
import one.util.streamex.EntryStream;

public class PiratePlayer extends Player {
  public PiratePlayer(final int playerNumber) {
    super(playerNumber);
  }

  @Override
  public Move makeMove(final Game game) throws IllegalMoveException {
    final int i =
        findCaptureMove(game)
            .or(findExtraTurnMove(game))
            .or(usePenultimateWellIfFull(game))
            .or(findLowestNumberOfBeadsMove(game))
            .orElse(-1);

    return new Move(game.getBoard(), this, i);
  }

  private Optional<Integer> findCaptureMove(final Game game) {
    return getChoices(game)
        .mapValues(PlayerWell::getNumberOfBeads)
        .filterKeyValue(
            (index, beads) -> {
              if (beads > 14) {
                return false;
              } else if (beads == 14) {
                return true;
              }
              final int lastBoardWellIndex = (index + beads + getPlayerNumber() == 1 ? 0 : 7) % 14;
              if (game.getBoard().wellBelongsTo(this, lastBoardWellIndex)
                  && (index == lastBoardWellIndex
                      || game.getBoard().getWells().get(lastBoardWellIndex).isEmpty())
                  && !game.getBoard().getOppositeWell(lastBoardWellIndex).isEmpty()) {
                return true;
              }
              return false;
            })
        .keys()
        .findFirst();
  }

  private Supplier<Optional<Integer>> findExtraTurnMove(final Game game) {
    return () ->
        getChoices(game)
            .mapValues(PlayerWell::getNumberOfBeads)
            .filterKeyValue((index, beads) -> beads == (6 - index))
            .keys()
            .findAny();
  }

  private Supplier<Optional<Integer>> usePenultimateWellIfFull(final Game game) {
    return () -> game.getBoard().getWellFor(this, 4).isEmpty() ? Optional.empty() : Optional.of(4);
  }

  private Supplier<Optional<Integer>> findLowestNumberOfBeadsMove(final Game game) {
    return () -> {
      final OptionalInt min =
          getChoices(game).values().mapToInt(PlayerWell::getNumberOfBeads).min();
      if (min.isPresent()) {
        return getChoices(game)
            .filterValues(well -> well.getNumberOfBeads() == min.getAsInt())
            .keys()
            .reverseSorted()
            .findFirst();
      } else {
        return Optional.empty();
      }
    };
  }

  private EntryStream<Integer, PlayerWell> getChoices(final Game game) {
    return EntryStream.of(game.getBoard().getPlayerWells(this))
        .selectValues(PlayerWell.class)
        .filterValues(not(PlayerWell::isEmpty));
  }

  @Override
  public void inform(final Exception exception) {
    System.err.printf("Should not have happened: %s%n", exception.getMessage());
  }
}
