package par5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
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

  val routine = system.scheduler.schedule(1 second, 2 seconds) {
    simpleActor ! "Heartbeat"
  }

  Thread.sleep(10 * 1000)
  system.terminate()
}
