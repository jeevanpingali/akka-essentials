package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import part3testing.BasicSpec.{BlackHole, LabTestActor, SimpleActor}

import scala.concurrent.duration._
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("basicspec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "TA simple actor" should {
    "send back the same message" in {
      // testing scenario
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, test"
      echoActor ! message

      expectMsg(message) // akka.test.single-expect-default
    }

    "a black hole actor" should {
      "send back some message" in {
        val blackHoleActor = system.actorOf(Props[BlackHole])
        val message = "hello, test"
        blackHoleActor ! message

        expectNoMessage(1 second)
      }
    }
  }

  // message assertions
  "a lab test actor" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "turn a string into upper case" in {
      labTestActor ! "i love akka"
      val reply = expectMsgType[String]
      assert(reply == "I LOVE AKKA")
      assert(reply.split(" ").length == 3)
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf(20 second, "hi", "hello")
    }

  }

  "a lab test actor others" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "reply with favourite tech" in {
      labTestActor ! "favouriteTech"
      expectMsgAllOf(10 second, "Akka", "Scala")
    }
  }

  "a lab test actor others 2" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "reply with cool tech in a different way" in {
      labTestActor ! "favouriteTech"
      val messages = receiveN(2) // Seq[Any]
    }

    "reply with cool tech in a fancy way" in {
      labTestActor ! "favouriteTech"
      expectMsgPF() {
        case "Scala" => // only care partial function is defined
        case "Akka" =>
      }
    }
  }}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()
    override def receive: Receive = {
      case "greeting" => {
        if(random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      }
      case "favouriteTech" => {
        sender() ! "Scala"
        sender() ! "Akka"
      }
      case message: String => sender() ! message.toUpperCase
    }
  }
}
