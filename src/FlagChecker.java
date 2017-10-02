public class FlagChecker {
  static boolean containsFlag(Flag[] flags, Flag f) {
    for (Flag s : flags) {
      if (s == f) return true;
    }
    return false;
  }
}
