package par5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}

import scala.concurrent.duration._

object TimersSchedulers extends App {
  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("SchedulersAndTimersDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  system.log.info("Scheduling reminder for simple actor")

  import system.dispatcher

  system.scheduler.scheduleOnce(2 second) {
    simpleActor ! "Reminder"
  }

  val routine:Cancellable  = system.scheduler.schedule(1 second, 2 seconds) {
    simpleActor ! "Heartbeat"
  }

  system.scheduler.scheduleOnce(5 seconds) {
    system.log.info("Cancelling heartbeat")
    routine.cancel()
  }

  system.scheduler.scheduleOnce(15 seconds) {
    system.log.info("terminating actor system")
    system.terminate()
  }
}
