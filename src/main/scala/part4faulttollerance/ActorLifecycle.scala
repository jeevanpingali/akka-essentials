package part4faulttollerance

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object ActorLifecycle extends App {

  object StartChild

  class LifecycleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case StartChild =>
        context.actorOf(Props[LifecycleActor], "child")
    }

    override def preStart(): Unit = log.info("I am starting")

    override def postStop(): Unit = log.info("I have stopped")
  }

  val system = ActorSystem("LifecycleDemo")
  /*val parent = system.actorOf(Props[LifecycleActor], "parent")
  parent ! StartChild
  parent ! PoisonPill*/

  case object Fail
  object CheckChild
  object Check
  class Child extends Actor with ActorLogging {
    override def preStart(): Unit = log.info("supervised child started")

    override def postStop(): Unit = log.info("supervised child stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"supervised actor restarting because of ${reason.getMessage}")

    override def postRestart(reason: Throwable): Unit =
      log.info(s"supervised actor restarted")

    override def receive: Receive = {
      case Fail => log.warning("child will fail now")
        throw new RuntimeException("I failed")
      case Check =>
        log.info("alive and kicking")
    }
  }

  case object FailChild
  class Parent extends Actor {
    private val child = context.actorOf(Props[Child], "supervisedChild")

    override def receive: Receive = {
      case FailChild =>
        child ! Fail
      case CheckChild => child ! Check
    }
  }

  val supervisor = system.actorOf(Props[Parent], "supervisor")
  supervisor ! FailChild
  supervisor ! CheckChild

  // supervison strategy

  Thread.sleep(10 * 1000)
  system.terminate()
}
