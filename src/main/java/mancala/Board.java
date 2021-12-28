package mancala;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Optional;
import mancala.Well.HomeBase;
import mancala.Well.PlayerWell;
import one.util.streamex.EntryStream;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

@lombok.Value
public class Board {
  private final List<Well> wells;

  public Board(final List<Well> wells) {
    if (wells.size() != 14) {
      throw new IllegalArgumentException();
    }

    this.wells = unmodifiableList(wells);
  }

  public int getNextBoardWellIndexFor(final Player player, final int playerWellIndex) {
    return EntryStream.of(wells)
        .mapKeyValuePartial(
            (index, well) -> {
              if (getWellFor(player, playerWellIndex) == well) {
                return Optional.of((index + 1) % 14);
              }
              return Optional.empty();
            })
        .findFirst()
        .orElseThrow();
  }

  public Well getWellFor(final Player player, final int playerWellIndex) {
    return getPlayerWells(player).get(playerWellIndex);
  }

  public int getBoardWellIndexFor(final Player player, final int playerWellIndex) {
    return EntryStream.of(wells)
        .mapKeyValuePartial(
            (index, well) -> {
              if (player.getPlayerNumber() == 1 && index <= 6) {
                return Optional.of(index);
              } else if (player.getPlayerNumber() == 2 && index > 6) {
                return Optional.of(index);
              } else {
                return Optional.empty();
              }
            })
        .findFirst()
        .orElseThrow();
  }

  public Well getOppositeWell(final int boardWellIndex) {
    if (boardWellIndex == 6) {
      return wells.get(13);
    } else if (boardWellIndex == 13) {
      return wells.get(6);
    } else {
      return wells.get(12 - boardWellIndex);
    }
  }

  public Well getOppositePlayerWell(final Player player, final int playerWellIndex) {
    return getOppositeWell(getBoardWellIndexFor(player, playerWellIndex));
  }

  public Board moveBeadFor(
      final Player player, final int playerWellIndex, final int toBoardWellIndex) {
    final Well moveWell = getWellFor(player, playerWellIndex);
    return EntryStream.of(wells)
        .mapKeyValue(
            (index, well) -> {
              if (well == moveWell && index != toBoardWellIndex) {
                return well.decrement();
              }
              if (well != moveWell && index == toBoardWellIndex) {
                return well.increment();
              }
              return well;
            })
        .toListAndThen(Board::new);
  }

  public List<Well> getPlayerWells(final Player player) {
    return EntryStream.of(wells)
        .mapKeyValuePartial(
            (index, well) -> {
              if (player.getPlayerNumber() == 1 && index <= 6) {
                return Optional.of(well);
              } else if (player.getPlayerNumber() == 2 && index > 6) {
                return Optional.of(well);
              } else {
                return Optional.empty();
              }
            })
        .toImmutableList();
  }

  public boolean wellBelongsTo(final Player player, final int boardWellIndex) {
    return (player.getPlayerNumber() == 1 && boardWellIndex <= 6)
        || (player.getPlayerNumber() == 2 && boardWellIndex > 6);
  }

  public boolean allPlayerWellsEmpty(final Player player) {
    return StreamEx.of(getPlayerWells(player)).select(PlayerWell.class).allMatch(Well::isEmpty);
  }

  public boolean allPlayerWellsEmpty() {
    return EntryStream.of(wells)
        .filterKeys(index -> index != 6 && index != 13)
        .values()
        .allMatch(Well::isEmpty);
  }

  public Board captureBeadsForPlayer(final Player player, final int lastBoardWellIndexFromMove) {
    final int totalCapturedBeads =
        getOppositeWell(lastBoardWellIndexFromMove).getNumberOfBeads()
            + wells.get(lastBoardWellIndexFromMove).getNumberOfBeads();
    final int playerHomeBaseIndex = player.getPlayerNumber() == 1 ? 6 : 13;

    return EntryStream.of(wells)
        .mapKeyValue(
            (index, well) -> {
              if (well instanceof PlayerWell) {
                if (well == getOppositeWell(lastBoardWellIndexFromMove)
                    || well == wells.get(lastBoardWellIndexFromMove)) {
                  return new PlayerWell(0);
                }
              } else if (well instanceof HomeBase) {
                if (index == playerHomeBaseIndex) {
                  return new HomeBase(well.getNumberOfBeads() + totalCapturedBeads);
                }
              }
              return well;
            })
        .toListAndThen(Board::new);
  }

  public Board claimAllPlayerWellsFor(final Player player) {
    final int totalCapturedBeads =
        StreamEx.of(wells)
            .mapToInt(
                well ->
                    getPlayerWells(player).contains(well) && well instanceof PlayerWell
                        ? well.getNumberOfBeads()
                        : 0)
            .sum();

    return StreamEx.of(wells)
        .map(
            well -> {
              if (getPlayerWells(player).contains(well)) {
                if (well instanceof PlayerWell) {
                  return new PlayerWell(0);
                } else if (well instanceof HomeBase) {
                  return new HomeBase(well.getNumberOfBeads() + totalCapturedBeads);
                }
              }
              return well;
            })
        .toListAndThen(Board::new);
  }

  @Override
  public String toString() {
    final StringBuilder out = new StringBuilder("   ");

    IntStreamEx.rangeClosed(7, 12)
        .reverseSorted()
        .forEach(i -> out.append(String.format(" %02d", wells.get(i).getNumberOfBeads())));
    out.append("\n");

    out.append(String.format(" %02d ", wells.get(13).getNumberOfBeads()));

    IntStreamEx.rangeClosed(0, 5).forEach(i -> out.append("   "));

    out.append(String.format("%02d", wells.get(6).getNumberOfBeads()));
    out.append("\n   ");

    IntStreamEx.rangeClosed(0, 5)
        .forEach(i -> out.append(String.format(" %02d", wells.get(i).getNumberOfBeads())));
    out.append("\n");

    return out.toString();
    // return toStringWithBraille();
  }

  public String toStringWithBraille() {
    final StringBuilder out = new StringBuilder("     ");

    IntStreamEx.rangeClosed(7, 12)
        .reverseSorted()
        .forEach(
            i -> out.append(String.format(" (%2s)", mapBeads(wells.get(i).getNumberOfBeads()))));
    out.append("\n");

    out.append(String.format(" (%2s)", mapBeads(wells.get(13).getNumberOfBeads())));

    IntStreamEx.rangeClosed(0, 5).forEach(i -> out.append("     "));

    out.append(String.format(" (%2s)", mapBeads(wells.get(6).getNumberOfBeads())));
    out.append("\n     ");

    IntStreamEx.rangeClosed(0, 5)
        .forEach(
            i -> out.append(String.format(" (%2s)", mapBeads(wells.get(i).getNumberOfBeads()))));
    out.append("\n");

    return out.toString();
  }

  private static String mapBeads(final int numberOfBeads) {
    switch (numberOfBeads) {
      case 0:
        return "  ";
      case 1:
        return "• ";
      case 2:
        return " :";
      case 3:
        return "⠕ ";
      case 4:
        return " ⠭";
      case 5:
        return "⠽ ";
      case 6:
        return "⠼⠑";
      case 7:
        return "⠼⠋";
      case 8:
        return "⠭⠭";
      case 9:
        return "⠟⠵";
      default:
        return String.format("%02d", numberOfBeads);
    }
  }

  public static Board createStartingBoard() {
    return IntStreamEx.range(14)
        .mapToObj(i -> i == 6 || i == 13 ? new HomeBase(0) : new PlayerWell(4))
        .toListAndThen(Board::new);
  }
}
