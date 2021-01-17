package par5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}

import scala.concurrent.duration._

object TimersSchedulers extends App {
  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("SchedulersAndTimersDemo")

/*
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  system.log.info("Scheduling reminder for simple actor")
*/

  import system.dispatcher

 /* system.scheduler.scheduleOnce(2 second) {
    simpleActor ! "Reminder"
  }

  val routine:Cancellable  = system.scheduler.schedule(1 second, 2 seconds) {
    simpleActor ! "Heartbeat"
  }

  system.scheduler.scheduleOnce(5 seconds) {
    system.log.info("Cancelling heartbeat")
    routine.cancel()
  }
*/
  class SelfClosingActor extends Actor with ActorLogging {
    var schedule = createTimeoutWindow()

    def createTimeoutWindow(): Cancellable = {
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "timeout"
      }
    }

    override def receive: Receive = {
      case "timeout" =>
        log.info("Stopping myself")
        context.stop(self)
      case message =>
        log.info(s"Receives $message, staying alive")
        schedule.cancel()
        schedule = createTimeoutWindow()
    }
  }
/*
  val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosingACtor")

  system.scheduler.scheduleOnce(250 milliseconds) {
    selfClosingActor ! "ping"
  }


  system.scheduler.scheduleOnce(2 seconds) {
    system.log.info("sending pong to the self closing window")
    selfClosingActor ! "pong"
  }*/

  case object TimerKey
  case object Start
  case object Reminder
  case object Stop
  class TimerBasedHeartBeatActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(TimerKey, Start, 500 milliseconds)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startPeriodicTimer(TimerKey, Reminder, 1 second)
      case Reminder =>
        log.info("I am alive")
      case Stop =>
        log.warning("Stopping")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  val timerBasedHeartBeatActor = system.actorOf(Props[TimerBasedHeartBeatActor], "timerActor")
  system.scheduler.scheduleOnce(5 seconds) {
    timerBasedHeartBeatActor ! Stop
  }

  system.scheduler.scheduleOnce(15 seconds) {
    system.log.info("terminating actor system")
    system.terminate()
  }
}
