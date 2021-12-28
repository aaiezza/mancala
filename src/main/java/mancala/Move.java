package mancala;

@lombok.Value
public class Move {
  private final int wellIndex;

  public Move(final Board board, final Player player, final int wellIndex)
      throws IllegalMoveException {
    if (wellIndex < 0 || wellIndex > 5) {
      throw new IllegalMoveException(wellIndex);
    }
    if (board.getWellFor(player, wellIndex).isEmpty()) {
      throw new IllegalMoveException(wellIndex);
    }
    this.wellIndex = wellIndex;
  }

  @SuppressWarnings("serial")
  @lombok.Value
  @lombok.EqualsAndHashCode(callSuper = true)
  public static class IllegalMoveException extends Exception {
    public IllegalMoveException(final int wellIndex) {
      super(String.format("Move [%d] is illegal", wellIndex));
    }
  }
}
