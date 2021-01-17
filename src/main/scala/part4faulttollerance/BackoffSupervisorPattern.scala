package part4faulttollerance

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{Backoff, BackoffSupervisor}

import scala.concurrent.duration._
import java.io.File
import scala.io.Source

object BackoffSupervisorPattern extends App {

  case object ReadFile

  class FileBasedPersistenceActor extends Actor with ActorLogging {
    var daatSource: Source = null

    override def preStart(): Unit =
      log.info("Persistent actor starting")

    override def postStop(): Unit =
      log.info("Persistent actor stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.warning("Persistent actor restarting")

    override def receive: Receive = {
      case ReadFile =>
        if (daatSource == null) {
          daatSource = Source.fromFile(new File("src/main/resources/testfiles/important_data.txt"))
        }
        log.info("I've just read some important data: " + daatSource.getLines().toList)
    }
  }

  val system = ActorSystem("BackoffSupervisorSystem")
  //  val simpleActor = system.actorOf(Props[FileBasedPersistenceActor], "simpleActor")
  //  simpleActor ! ReadFile

  val simpleSupervisorProps = BackoffSupervisor.props(
    Backoff.onFailure(
      Props[FileBasedPersistenceActor], "simpleBackoffActor", 3 seconds, 30 seconds, 0.2
    )
  )

  //  val simpleBackoffSupervisor = system.actorOf(simpleSupervisorProps, "simpleSupervisor")
  //
  //  simpleBackoffSupervisor ! ReadFile

  val stopSupervisorProps = BackoffSupervisor.props(
    Backoff.onStop(
      Props[FileBasedPersistenceActor],
      "stopBackoffSupervisor",
      3 seconds,
      30 seconds,
      0.2
    ).withSupervisorStrategy(
      OneForOneStrategy() {
        case _ => Stop
      }
    )
  )

  //  val stopSupervisor = system.actorOf(stopSupervisorProps, "stopSupervisor")
  //  stopSupervisor ! ReadFile

  class EagerFBPActor extends FileBasedPersistenceActor {
    override def preStart(): Unit = {
      daatSource = Source.fromFile(new File("src/main/resources/testfiles/important_data.txt"))
    }
  }

  val repeatedSupervisorProps = BackoffSupervisor.props(
    Backoff.onStop(
      Props[EagerFBPActor],
      "eagerActor",
      1 second,
      33 seconds,
      0.1
    )
  )

  val repeatedSupervisor = system.actorOf(repeatedSupervisorProps, "repeatedSupervisor")

  //Thread.sleep(1000 * 5)
  //  system.terminate()
}
