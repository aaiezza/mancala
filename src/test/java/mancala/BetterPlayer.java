package mancala;

import static io.vavr.Predicates.not;
import static org.assertj.core.api.Assertions.fail;

import mancala.Move.IllegalMoveException;
import mancala.Well.PlayerWell;
import one.util.streamex.EntryStream;

public class BetterPlayer extends Player {
  public BetterPlayer(final int playerNumber) {
    super(playerNumber);
  }

  @Override
  public Move makeMove(final Game game) throws IllegalMoveException {
    final int i =
        EntryStream.of(game.getBoard().getPlayerWells(this))
            .selectValues(PlayerWell.class)
            .filterValues(not(PlayerWell::isEmpty))
            .mapValues(PlayerWell::getNumberOfBeads)
            .filterKeyValue((index, beads) -> beads == (6 - index))
            .keys()
            .findAny()
            .orElse(
                EntryStream.of(game.getBoard().getPlayerWells(this))
                    .selectValues(PlayerWell.class)
                    .filterValues(not(PlayerWell::isEmpty))
                    .sortedByInt(well -> well.getValue().getNumberOfBeads())
                    .keys()
                    .findAny()
                    .orElse(-1));

    return new Move(game.getBoard(), this, i);
  }

  @Override
  public void inform(final Exception exception) {
    fail("Should not have happened: %s", exception.getMessage());
  }
}
