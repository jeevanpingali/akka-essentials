package part4faulttollerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, Terminated}
import part4faulttollerance.StrtingStoppingActors.Parent.{StartChild, Stop, StopChild}

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

  /**
   * Method 1: context.stop
   */
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

/*
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
*/

  /**
   * Method 2: PoisonPill
   */

/*
  val looseActror = system.actorOf(Props[Child])
  looseActror ! "Hello loose actor"
  looseActror ! PoisonPill
  looseActror ! "Are you still there"
*/

  /**
   * Method 3: Kill
   */

/*
  val abruptlyTerminatedActor = system.actorOf(Props[Child])
  abruptlyTerminatedActor ! "You are about to be terminated"
  abruptlyTerminatedActor ! Kill
  abruptlyTerminatedActor ! "you have been terminated"

*/
  /**
   * Death Watcher
   */

  class Watcher extends Actor with ActorLogging {
    import Parent._
    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Child], name)
        log.info(s"Started and watching child $name")
        context.watch(child)
      case Terminated(actorRef) =>
        log.info(s"The reference $actorRef I'm watching has been stopped")
    }
  }

  val watcher = system.actorOf(Props[Watcher], "watcher")
  watcher ! StartChild("watchedChild")
  val watchedChild = system.actorSelection("/user/watcher/watchedChild")
  watchedChild ! "Hi watched child"
  Thread.sleep(500)
  watchedChild ! PoisonPill

  Thread.sleep(5 * 1000)
  system.terminate()
}
