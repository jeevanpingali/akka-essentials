package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsExercise extends App {

  // Distributed word counting
  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }
  class WordCounterMaster extends Actor {
    import WordCounterMaster._
    var lastCount = 0
    override def receive: Receive = {
      case Initialize(workerCount) =>
        println("workerCount: " + workerCount)
        println(s"${self.path} initializing with count: $workerCount")
        for(n <- 0 until workerCount) yield
          context.actorOf(Props[WordCounterWorker], s"worker$n")
        context.become(wordCountTaskExecution(workerCount, 0, 0, Map()))
    }

    def wordCountTaskExecution(workerCount: Int, workerIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case Initialize(workerCount) =>
        println("wrong place")
      case text =>
        val originalSender = sender()
        val index = if(workerCount == workerIndex) 0 else workerIndex
        val workerRef = context.actorSelection(s"/user/master/worker$index")
        workerRef ! WordCountTask(currentTaskId, text.toString)
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(wordCountTaskExecution(workerCount, index + 1, currentTaskId + 1, newRequestMap))
      case WordCountReply(currentTaskId, count) =>
        println(s"${self.path} words counted: $count")
        val originalSender = requestMap.get(currentTaskId)
//        originalSender ! count
        context.become(wordCountTaskExecution(workerCount, workerIndex, currentTaskId, requestMap - currentTaskId))
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case WordCountTask(currentTaskId, text) =>
        val wordCount = text.split(" ").length
        println(s"${self.path} words of $text counted: $wordCount")
        sender() ! WordCountReply(currentTaskId, wordCount)
    }
  }

  class TestActor extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        println("[test actor] got to go")
        val master = system.actorOf(Props[WordCounterMaster], "master2")
        println(s"[test actor] got master reference is: ${master.path}")
        master ! Initialize(3)
        Thread.sleep(1000 * 5)
        println("Initialization completed")

        val texts = List("I love Akka", "Scala is super done", "yes", "me too")
/*        texts.foreach(text => {
          println(s"[test actor] ")
          master ! text
          Thread.sleep(500)
        })*/
        master ! "Akka is cool"
      case count =>
        println(s"[test actor I received a reply: $count")
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
  /*val master = system.actorOf(Props[WordCounterMaster], "master")
  import WordCounterMaster._
  master ! Initialize(3)
  (0 to 10).foreach(_ => {
    master ! "akka is awesome for sure"
    Thread.sleep(5)
  })*/

  val testActor = system.actorOf(Props[TestActor], "testactor")
  Thread.sleep(5)
  testActor ! "go"

  Thread.sleep(5 * 1000)

  system.terminate()
}
