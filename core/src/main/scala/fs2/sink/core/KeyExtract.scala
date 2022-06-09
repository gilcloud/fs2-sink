package fs2.sink.core

trait KeyExtract[A, K] {
  def extract: A => K
}

object KeyExtract {

  def function[A, K](fn: A => K): KeyExtract[A, K] = new KeyExtract[A, K] {
    override val extract: A => K = fn
  }
}
