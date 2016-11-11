import actors.BenchmarkActor.{GetResults, TestAction, TestBenchmark}
import actors.{AuctionMain, BenchmarkActor}
import akka.actor.{ActorSystem, Inbox, Props}
import messages.InitAuctionApp

/*
# TEST RESULTS
## leveldb - native:off
SIZE     |
-------------------
100      | 1920
1000     | 726
10000    | 281

## leveldb - native:on
SIZE     |
-------------------
100      | 2402
1000     | 858
10000    | 257

## inmemory
SIZE     |
-------------------
100      | 2954
1000     | 1775
10000    | 271

*/

object BenchmarkApp extends App {
  println("Starting benchmark app")
  val system = ActorSystem("benchmark")
  val benchmark = system.actorOf(Props[BenchmarkActor], name="benchmark")
  val inbox = Inbox.create(system)
  var x = 0
  for (x <- Range(0, 100)) {
    inbox.send(benchmark, TestAction())
  }
  inbox.send(benchmark, GetResults)

  for (x <- Range(0, 1000)) {
    inbox.send(benchmark, TestAction())
  }
  inbox.send(benchmark, GetResults)

  for (x <- Range(0, 10000)) {
    inbox.send(benchmark, TestAction())
  }
  inbox.send(benchmark, GetResults)
}
