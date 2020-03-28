package io.parapet.examples.bully

import cats.effect.Concurrent
import io.parapet.core.Dsl.DslF
import io.parapet.core.Event.Start
import io.parapet.core.processes.BullyLeaderElection._
import io.parapet.core.{Channel, Process, ProcessRef}
import io.parapet.examples.bully.BankProcess._

import scala.concurrent.duration._
import scala.util.Random

class SimulationProcess[F[_] : Concurrent](bully: ProcessRef) extends Process[F] {

  import dsl._

  private val initialDelay = 5.second // wait for peers
  private val transactionDelay = 3.second
  private val depositPercent = 0.1
  private val withdrawPercent = 0.1

  private val chan = Channel[F] //  for REQ-REP dialog with Bank

  def step: DslF[F, Unit] = flow {
    eval(println("send GetBalance")) ++
      chan.send(Req(GetBalance), bully, {
        case scala.util.Success(Rep(Ok, data)) =>
          val balance = new String(data).toInt
          eval(println(s"current balance = $balance")) ++
            flow {
              val op = randomOperation(balance)
              chan.send(Req(op), bully, {
                case scala.util.Success(Rep(Ok, data)) => eval(println(s"bank accepted $op. new balance = ${new String(data)}"))
                case scala.util.Success(Rep(Error, data)) => eval(println(s"bank failed to execute $op. reason: ${new String(data)}"))
                case scala.util.Failure(ex) => eval(println(s"unexpected error: $ex"))
              })
            }
        case scala.util.Success(Rep(Error, data)) => eval(println(s"bank failed to provide information about balance. reason: ${new String(data)}"))
        case scala.util.Failure(ex) => eval(println(s"unexpected error: $ex"))
      }) ++ delay(transactionDelay, step)
  }

  override def handle: Receive = {
    case Start => register(ref, chan) ++ delay(initialDelay, step)
  }

  def randomOperation(currentBalance: Int): Operation = {
    if (Math.abs(new Random().nextInt()) % 2 == 0) {
      Deposit((currentBalance * depositPercent).toInt)
    } else {
      Withdraw((currentBalance * withdrawPercent).toInt)
    }
  }

}