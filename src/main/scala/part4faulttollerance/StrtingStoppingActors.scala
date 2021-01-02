package part4faulttollerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import part4faulttollerance.StrtingStoppingActors.Parent.{StartChild, StopChild, Stop}

object StrtingStoppingActors extends App {
  val system = ActorSystem("StoppingActorsDemo")

  object Parent {

    case class StartChild(name: String)

    case class StopChild(name: String)

    case object Stop // parent to stop itself
  }

  class Parent extends Actor with ActorLogging {

    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"Starting child with the name: $name")
        context.become(withChildren(children + (name -> context.actorOf(Props[Child], name))))
      case StopChild(name) =>
        log.info(s"Stopping child $name")
        val childOption = children.get(name)
        childOption.foreach(childRef => context.stop(childRef))
      case Stop =>
        log.info("Stopping myself")
        context.stop(self)
      case message => log.info(message.toString)
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val parent = system.actorOf(Props[Parent], "parent")
  parent ! StartChild("child1")
  val child = system.actorSelection("/user/parent/child1")
  child ! "Hi kid!"

  parent ! StopChild("child1")
/*
  (1 to 50).foreach(num =>
    child ! "are you still there?"
  )
*/

  parent ! StartChild("child2")
  val child2 = system.actorSelection("/user/parent/child2")
  child2 ! "Hi second child"

  parent ! Stop
  (0 to 10).foreach(num =>
    parent ! "parent, are you still there"
  )
  (0 to 100).foreach(num =>
    child2 ! s"[${num}] second child, are you still there"
  )
  system.terminate()
}
