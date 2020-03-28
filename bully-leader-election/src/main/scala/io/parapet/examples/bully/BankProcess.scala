package io.parapet.examples.bully

import io.parapet.core.Event.Marshall
import io.parapet.core.processes.BullyLeaderElection._
import io.parapet.core.{Event, Process, ProcessRef}
import io.parapet.examples.bully.BankProcess._

import scala.util.matching.Regex

class BankProcess[F[_]](initialBalance: Int, override val ref: ProcessRef, bully: ProcessRef) extends Process[F] {

  import dsl._

  private var balance = initialBalance

  override def handle: Receive = {
    case Operation(GetBalance) => eval(println("bank received GetBalance")) ++ withSender(s => Rep(Ok, Balance(balance)) ~> s)
    case Operation(Deposit(delta)) =>
      eval(println(s"bank received Deposit($delta)")) ++ flow {
        val newBalance = balance + delta
        eval(balance = newBalance) ++ withSender(s => Rep(Ok, Balance(newBalance)) ~> s)
      }
    case Operation(Withdraw(delta)) =>
      eval(println(s"bank received Withdraw($delta)")) ++
        flow {
          val newBalance = balance - delta
          if (newBalance < 0) {
            withSender(s => Rep(Error, "insufficient balance".getBytes()) ~> s)
          } else {
            eval(balance = newBalance) ++ withSender(s => Rep(Ok, Balance(newBalance)) ~> s)
          }
        }
  }
}

object BankProcess {

  // API
  trait Operation extends Event with Marshall

  case object GetBalance extends Operation {
    override def marshall: Array[Byte] = "GET_BALANCE".getBytes
  }

  case class Deposit(value: Int) extends Operation {
    override def marshall: Array[Byte] = s"DEPOSIT|$value".getBytes
  }

  case class Withdraw(value: Int) extends Operation {
    override def marshall: Array[Byte] = s"WITHDRAW|$value".getBytes
  }

  object Operation {
    val getBalancePattern: Regex = "\\bGET_BALANCE\\b".r
    val depositPattern: Regex = "DEPOSIT\\|(\\d*)".r
    val withdrawPattern: Regex = "WITHDRAW\\|(\\d*)".r

    def unapply(event: Event): Option[Operation] = {
      event match {
        case Req(bytes) =>
          val data = new String(bytes)
          data match {
            case getBalancePattern() => Some(GetBalance)
            case depositPattern(delta) => Some(Deposit(delta.toInt))
            case withdrawPattern(delta) => Some(Withdraw(delta.toInt))
            case _ => None
          }
        case _ => None
      }
    }
  }

  // Events
  case class Balance(value: Int) extends Event with Marshall {
    override def marshall: Array[Byte] = value.toString.getBytes()
  }

}
