package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehaviorExercise.Counter.{Decrement, Increment, Print}

object ChangingActorBehaviorExercise extends App {
  object Counter {
    case class Increment(currentCount: Int)
    case class Decrement(currentCount: Int)
    case object Print
  }
  class Counter extends Actor {
    import Counter._

    override def receive: Receive = requestReceived(0)

    def requestReceived(currentCount: Int): Receive = {
      case Increment => context.become(requestReceived(currentCount + 1), false)
      case Decrement => context.unbecome
      case Print => println(s"Current count: $currentCount")
    }
  }

  val system = ActorSystem("ChangingActorBehaviorExercise1")
  val counter = system.actorOf(Props[Counter], "counter")

  import Counter._

  (1 to 15).foreach(_ => counter ! Increment)
  counter ! Print
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print

  /**
   * Exercise 2
   */

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])
  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(c) => context.become(voted(c))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {
    override def receive: Receive = {
      awaitingCommand
    }

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) => {
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
      }
    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) =>
        // Citizen hasn't voted yet
        sender() ! VoteStatusRequest // this might end up in an infinite loop
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if(newStillWaiting.isEmpty) {
          println(s"[aggregator] poll stats: $newStats")
        } else {
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }
    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])


  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Ronald")
  daniel ! Vote("Ronald")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

  /**
   * Print the status of votes, a map of candidates and the number of votes received
   */



  system.terminate()

}
