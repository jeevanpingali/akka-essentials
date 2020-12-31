package part3testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class TestProbeSpec extends TestKit(ActorSystem("basicspec"))
with ImplicitSender
with WordSpecLike
with BeforeAndAfterAll {
  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbeSpec._
  "A master actor " should {
    Thread.sleep(3 * 1000)
    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
    }

    "send the work to slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
      val workloadString = "I love Akka"
      master ! Work(workloadString)

      slave.expectMsg(SlaveWork(workloadString, testActor))
      slave.reply(WorkCompleted(3, testActor))

      expectMsg(Report(3))
    }
  }
}

object TestProbeSpec {
  /**
   * Word counting actor hierarchy master-slave
   * send some worrk to master
   * master sends piece of work to slave
   * slave processes work and replies to master
   * master aggregates the results
   * master sends total work counts to original requester
   */

  case class Work(text: String)
  case class SlaveWork(text: String, originalRequester: ActorRef)
  case class WorkCompleted(count: Int, originalRequester: ActorRef)
  case class Register(slaveRef: ActorRef)
  case object RegistrationAck
  case class Report(totalCount: Int)

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegistrationAck
        context.become(online(slaveRef, 0))
      case _ => // ignore
    }

    def online(slaveRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalRequester) =>
        val newTotalWordCount = totalWordCount + count
        originalRequester ! Report(newTotalWordCount)
        context.become(online(slaveRef, newTotalWordCount))
    }
  }
}
