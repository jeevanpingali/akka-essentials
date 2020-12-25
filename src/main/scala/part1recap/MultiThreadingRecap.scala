package part1recap

object MultiThreadingRecap extends App {
  // creating threads on the JVM
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("I'm running in parallel")
  })

  val aThread2 = new Thread(() => println("I'm running in parallel"))

  aThread2.start()
  aThread2.join()

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))
  threadHello.start()
  threadGoodbye.start()
  threadGoodbye.join()
  threadHello.join()
}
