package fs2.sink.core

trait Transformer[A, B] {

  def transform: A => B

}

object Transformer {

  def function[A, B](fn: A => B): Transformer[A, B] = new Transformer[A, B] {
    override def transform: A => B = a => fn(a)
  }

}
