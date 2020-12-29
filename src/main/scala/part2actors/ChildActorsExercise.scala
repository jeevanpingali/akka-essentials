package part2actors

import akka.actor.Actor

object ChildActorsExercise extends App {

  // Distributed word counting
  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String)
    case class WordCountReply(count: Int)
  }
  class WordCounterMaster extends Actor {
    import WordCounterMaster._
    override def receive: Receive = ???
  }

  class WordCounterWorker extends Actor {
    override def receive: Receive = ???
  }

  /**
   * Create WordCounterMaster
   * send Initialize(10) to WordCounterMaster
   * Send "akka is awesome", WordCounterMaster will send WordCountTask("") to it's child
   * Child replies WordCountReply(3) to the master
   * MAster replies 3 to sender
   *
   * round robin logic used to send tasks to child actors
   */
}
