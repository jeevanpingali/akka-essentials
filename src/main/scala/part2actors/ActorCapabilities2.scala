package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilities2.BankAccount.{Deposit, Statement, Withdraw}
import part2actors.ActorCapabilities2.Person.LiveTheLife

object ActorCapabilities2 extends App {
  /**
   * 1. create counter actor
   * - respond to increment and decrement and print messages
   * 2. create a bank account as an actor
   * - receive messages to deposit and to withdraw an amount and extra message  of statement
   * - reply with a success or failure of these operations
   * - interact with other kind of actor which will
   */

  val system = ActorSystem("ActorCapabilitiesDemo2")


  // DOMAIN of the Counter
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }
  class Counter extends Actor {
    import Counter._
    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[counter] My current count is: $count")
    }
  }

  import Counter._
  val counter = system.actorOf(Props[Counter], "myCounter")
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print

  object BankAccount {
    case class Deposit(amount: Double)
    case class Withdraw(amount: Double)
    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)
  }

  class BankAccount extends Actor {
    import BankAccount._
    var funds: Double = 0.0d

    override def receive: Receive = {
      case Deposit(amount) => {
        if (amount > 0) {
          funds += amount
          // send success message
          println(s"Amount $amount successfully deposited, now the funds are $funds")
          sender() ! TransactionSuccess(s"Amount $amount successfully deposited, now the funds are $funds")
        } else {
          // send failure message
          sender() ! TransactionFailure("Invalid deposit amount")
        }
      }
      case Withdraw(amount) => {
        if (funds >= amount & amount > 0) {
          funds -= amount
          // send success message
          println(s"Amount $amount successfully withdrawn, now the funds are $funds")
          sender() ! TransactionSuccess(s"Amount $amount successfully withdrawn, now the funds are $funds")
        } else {
          // send failure message
          sender() ! TransactionFailure("Invalid withdraw amount")
        }
      }
      case Statement => sender() ! s"Your balance is $funds"
    }
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }
  class Person extends Actor {
    import Person._

    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statement
      case message => println(message.toString)
    }
  }

  import BankAccount._
  val account = system.actorOf(Props[BankAccount], "myBank")
  val person = system.actorOf(Props[Person], "jeevan")
  person ! LiveTheLife(account)

  system.terminate()
}
