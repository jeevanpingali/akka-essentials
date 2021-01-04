package part4faulttollerance

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.concurrent.duration._

class SupervisionSpec extends TestKit(ActorSystem("SupervisonSpec"))
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import SupervisionSpec._

  "A supervisor" should {
    "resume it's child in case of minor fault" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      child ! "I love Akka"
      child ! Report
      expectMsg(3)

      child ! "Akka is awesome becasue I'm learning to think in a whole new way"
      child ! Report
      expectMsg(3)
    }

    "restart it's child in case of an empty sentence" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "I love Akka"
      child ! Report
      expectMsg(3)

      child ! ""
      child ! Report
      expectMsg(0)
    }

    "terminate the child in case of major error" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      watch(child)
      child ! "akka is nice"
      val terminatedMessage = expectMsgType[Terminated]
      assert(terminatedMessage.actor == child)
    }
  }
}

object SupervisionSpec {
  class Supervisor extends Actor with ActorLogging {
    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 second) {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => {
        log.error(s"Got IllegalArgumentException, stopping actor")
        Stop
      }
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }

    override def receive: Receive = {
      case props: Props =>
        val childRef = context.actorOf(props)
        sender ! childRef
    }
  }

  case object Report
  class FussyWordCounter extends Actor with ActorLogging {
    var words = 0
    override def receive: Receive = {
      case "" => throw new NullPointerException("sentence is empty")
      case sentence: String =>
        println(s"Got the sentence $sentence. ${Character.isUpperCase(sentence(0))}")
        if(sentence.length > 20) {
          log.error(s"Got a sentence with more than 20 length: $sentence, so throwing RuntimeException")
          throw new RuntimeException("sentence is too big")
        }
        else if(!Character.isUpperCase(sentence(0))) {
          log.error(s"Sentence $sentence doesn't start with uppercase, throwing error")
          new IllegalArgumentException("sentence must start with uppercase")
        }
        else words += sentence.split(" ").length
      case Report => sender() ! words
      case _ => throw new Exception("can only receive strings")
    }
  }
}
