package part2actors

import akka.actor.{Actor, ActorSystem, Props}
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

  system.terminate()

}
