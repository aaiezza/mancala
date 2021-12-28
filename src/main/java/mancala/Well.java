package mancala;

public interface Well {
  public int getNumberOfBeads();

  public default boolean isEmpty() {
    return getNumberOfBeads() <= 0;
  }

  public Well increment();

  public Well decrement();

  @lombok.Value
  public static class HomeBase implements Well {
    private final int numberOfBeads;

    @Override
    public Well increment() {
      return new HomeBase(numberOfBeads + 1);
    }

    @Override
    public Well decrement() {
      throw new UnsupportedOperationException();
    }
  }

  @lombok.Value
  public static class PlayerWell implements Well {
    private final int numberOfBeads;

    @Override
    public Well increment() {
      return new PlayerWell(numberOfBeads + 1);
    }

    @Override
    public Well decrement() {
      return new PlayerWell(numberOfBeads - 1);
    }
  }
}
