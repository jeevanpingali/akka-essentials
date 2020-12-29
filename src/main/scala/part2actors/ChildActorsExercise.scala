package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsExercise extends App {

  // Distributed word counting
  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String)
    case class WordCountReply(count: Int)
  }
  class WordCounterMaster extends Actor {
    import WordCounterMaster._
    var lastCount = 0
    override def receive: Receive = {
      case Initialize(count) =>
        println(s"${self.path} initializing with count: $count")
        (0 until count).foreach(n => {
          context.actorOf(Props[WordCounterWorker], "worker" + n)
        })
      case WordCountTask(text) => {
        if(lastCount == 10) {
          lastCount = 0
        }
        val workerRef = context.actorSelection("/user/master/worker" + lastCount)
        lastCount += 1
        workerRef ! text
      }
      case WordCountReply(count) =>
        println(s"${self.path} words counted: $count")
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case text => {
        val wordCount = text.toString.split(" ").length
        println(s"${self.path} words of $text counted: $wordCount")
        sender() ! WordCountReply(wordCount)
      }
    }
  }

  /**
   * Create WordCounterMaster
   * send Initialize(10) to WordCounterMaster
   * Send "akka is awesome", WordCounterMaster will send WordCountTask("") to it's child
   * Child replies WordCountReply(3) to the master
   * Master replies 3 to sender
   *
   * round robin logic used to send tasks to child actors
   */

  val system = ActorSystem("ChildActorsExercise")
  val master = system.actorOf(Props[WordCounterMaster], "master")
  import WordCounterMaster._
  master ! Initialize(10)
  Thread.sleep(1000)
  (0 to 15).foreach(_ => {
    master ! WordCountTask("akka is awesome for sure")
    Thread.sleep(500)
  })

  system.terminate()
}
