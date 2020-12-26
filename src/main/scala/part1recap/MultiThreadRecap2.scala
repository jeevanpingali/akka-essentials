package part1recap

import scala.concurrent.Future

object MultiThreadRecap2 extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val futures = (0 to 9)
    .map(i => 100000 * i until 100000 * (i + 1)) // 0 - 99999, 100000 - 199999, 200000 - 299999 etc
    .map(range => Future {
      if(range.contains(546735)) throw new RuntimeException("Invlid number")
      range.sum
    })

  val sumFuture = Future.reduceLeft(futures)(_ + _) // Future with the sum of all numbers
  sumFuture.onComplete(println)

  Thread.sleep(1000 * 10)
}
