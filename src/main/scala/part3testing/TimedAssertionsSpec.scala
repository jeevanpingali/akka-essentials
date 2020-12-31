package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class TimedAssertionsSpec extends TestKit(ActorSystem("TimedAssertionsSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {
  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TimedAssertionsSpec._
  "A worker actor" should {
    val workerActor = system.actorOf(Props[WorkerActor])

    "reply with the meaning of life in a timely manner" in {
      within(500 millis, 1 second) {
        workerActor ! "work"
        expectMsg(WorkResult(5))
      }
    }
  }
}

object TimedAssertionsSpec {
  case class WorkResult(result: Int)
  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        // long or hard computation
        Thread.sleep(500)
        sender() ! WorkResult(5)

      case "workSequence" =>
        val r = new Random()
        (1 to 10).foreach(_ => {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        })
    }
  }
}
