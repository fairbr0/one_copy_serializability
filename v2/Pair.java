import java.io.Serializable;

public class Pair<T1, T2> implements Serializable {
  T1 t1;
  T2 t2;

  public Pair(T1 t1, T2 t2) {
    this.t1 = t1;
    this.t2 = t2;
  }
}
