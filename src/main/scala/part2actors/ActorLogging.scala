package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingObject extends App {
  class SimpleActorWithExplicitLogger extends Actor {
    val logger = Logging(context.system, this)
    override def receive: Receive = {
      /**
       * 1 - DEBUG
       * 2 - INFO
       * 3 - WARNING/WARN
       * 4 - ERROR
       */
      case message => // LOG IT
        logger.info(message.toString)
    }
  }

  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a, b) => log.info("Two parameters: {} and {}", a, b)
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplicitLogger], "loggingActor")
  val actor2 = system.actorOf(Props[ActorWithLogging], "actorWithLogging")

  actor ! "Logging a simple message"
  actor2 ! "A simple message to log"
  actor2 ! (42, 65)

  system.terminate()
}
