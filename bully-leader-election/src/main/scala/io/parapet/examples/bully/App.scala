package io.parapet.examples.bully

import cats.effect.IO
import io.parapet.core.ProcessRef
import io.parapet.core.processes.BullyLeaderElection.{Config => BullyConfig}
import io.parapet.core.processes.{BullyLeaderElection, PeerProcess}
import io.parapet.p2p.{Config => P2PConfig}
import io.parapet.{CatsApp, core}

object App extends CatsApp {

  val peerCount = 3 // total number of processes
  val multicastIp = "230.0.0.0"
  val multicastPort = 4447
  val protocolVer = 1

  override def processes: IO[Seq[core.Process[IO]]] = IO {
    val bankRef = ProcessRef.jdkUUIDRef
    val peer = PeerProcess[IO](P2PConfig.builder()
      .multicastIp(multicastIp)
      .protocolVer(protocolVer)
      .multicastPort(multicastPort).build())

    val bully = new BullyLeaderElection[IO](
      clientProcess = bankRef,
      peerProcess = peer.ref,
      config = BullyConfig(quorumSize = 1)//peerCount / 2 + 1
    )

    val bank = new BankProcess[IO](100, bankRef, bully.ref)
    val simulation = new SimulationProcess[IO](bully.ref)

    Seq(peer, bully, bank, simulation)
  }
}
