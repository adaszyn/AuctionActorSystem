package actors

import actors.BenchmarkActor.{GetResults, TestAction, TestEvent}
import akka.persistence.PersistentActor

object BenchmarkActor {
  case class TestAction()
  case class TestEvent()
  case class TestBenchmark()
  case class GetResults()
}

class BenchmarkActor extends PersistentActor {
  override def receive: Receive = receiveCommand
  var testCounter = 0
  var totalTime: Long = 0
  override def receiveRecover: Receive = {
    case evt: TestEvent => updateState(evt)
  }

  var counter = 0.0
  def updateState(testEvent: TestEvent): Unit = {
    counter = counter + 1
  }
  override def receiveCommand: Receive = {
    case TestAction() => {
      val t1 = System.nanoTime()
      persist(TestEvent()) {
        testCounter = testCounter + 1
        val t2 = System.nanoTime()
        totalTime = totalTime + (t2 - t1)
        event => updateState(event)
      }
    }
    case GetResults => {
      println("~ ~ ~ ~  ~ ~ ~ ~ ~ ~ ~")
      println(totalTime / testCounter)
      println("~ ~ ~ ~  ~ ~ ~ ~ ~ ~ ~")
      sender ! totalTime / testCounter
    }
  }

  override def persistenceId: String = "benchmark-actor"
}

