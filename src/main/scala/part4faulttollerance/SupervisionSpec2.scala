package part4faulttollerance

import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class SupervisionSpec2 extends TestKit(ActorSystem("SupervisonSpec"))
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import SupervisionSpec._

  "A supervisor" should {
  }
}