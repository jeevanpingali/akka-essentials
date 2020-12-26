package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

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

  case class IncrementCount(number: Int)
  case class DecrementCount(number: Int)
  case class PrintCounter()

  class MyCounter extends Actor {
    var count = 0
    override def receive: Receive = {
      case IncrementCount(number) => count += number
      case DecrementCount(number) => count -= number
      case PrintCounter => println(s"Current count is: $count")
    }
  }

  class MyBank extends Actor {
    var funds: Double = 0.0d

    override def receive: Receive = {
      case Deposit(amount) => {
        if (amount > 0) {
          funds += amount
          // send success message
          println(s"Amount $amount deposited, now the funds are $funds")
        } else {
          // send failure message
        }
      }
      case Withdraw(amount) => {
        if (funds >= amount & amount > 0) {
          funds -= amount
          // send success message
          println(s"Amount $amount withdrawn, now the funds are $funds")
        } else {
          // send failure message
        }
      }
    }
  }


  case class Deposit(amount: Double)

  case class Withdraw(amount: Double)

  val counter = system.actorOf(Props[MyCounter], "myCounter")
  counter ! IncrementCount(25)
  counter ! DecrementCount(20)
  counter ! PrintCounter

  val bank = system.actorOf(Props[MyBank], "myBank")
  bank ! Deposit(1000.50)
  bank ! Withdraw(1000.50)

  //  Thread.sleep(1000 * 5)

  system.terminate()
}
