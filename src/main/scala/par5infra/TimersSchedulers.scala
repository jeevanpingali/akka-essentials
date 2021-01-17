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
  system.scheduler.scheduleOnce(1 second) {
    simpleActor ! "Reminder"
  }(system.dispatcher)

  Thread.sleep(5 * 1000)
  system.terminate()
}
