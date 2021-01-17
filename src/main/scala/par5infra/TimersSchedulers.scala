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

  implicit val executionContext = system.dispatcher

  system.scheduler.scheduleOnce(2 second) {
    simpleActor ! "Reminder"
  }

  Thread.sleep(5 * 1000)
  system.terminate()
}
