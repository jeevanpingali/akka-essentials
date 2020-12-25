package part1recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultiThreadingRecap extends App {
  // creating threads on the JVM
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("I'm running in parallel")
  })

  val aThread2 = new Thread(() => println("I'm running in parallel"))

  aThread2.start()
  aThread2.join()

  val threadHello = new Thread(() => (1 to 10000).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 10000).foreach(_ => println("goodbye")))
  threadHello.start()
  threadGoodbye.start()
  threadGoodbye.join()
  threadHello.join()

  // inter-thread communication on the JVM
  // wait - notify mechanism
  // Scala futures
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    42
  }

  // callbacks
  future.onComplete {
    case Success(42) => println("I found the meaning of life")
    case Failure(_) => println("Something happened with the meaning of life!")
  }

  val aProcessedFuture = future.map(_ + 1)
  val aFlatFuture = future.flatMap { value =>
    Future(value + 2)
  }

  val filteredFuture = future.filter(_ % 2 == 0) // NoSuchElementException

  // for comprehensions
  val aNonsenseFuture = for {
    meaningOfLife <- future
      filteredMeaning <- filteredFuture
  } yield meaningOfLife + filteredMeaning

  // andThen, recover/recoverWith


}
