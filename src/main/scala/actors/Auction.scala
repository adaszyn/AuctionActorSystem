package actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable}
import messages._
import misc.Timers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class Auction(name: String) extends Actor {
  override def receive = init(0.0)
  private var cancellable: Cancellable = _
  var currentValue: Double = 0.0
  var winningBuyer: ActorRef = _
  def init(currentValue:Double): Receive =  {
    case GetCurrentValue => sender() ! this.currentValue
    case StartAuction => {
      println("[Starting auction]")
      context.parent ! AuctionStarted
      context.become(created())
      println("Becoming +++CREATED+++")
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
    }
  }
  def created(): Receive =  {
    case GetCurrentValue => sender() ! this.currentValue
    case bid: Bid => {
      println("Auction " + self.toString() + " <<CREATED>> " + " + " + bid.value.toString)
      println("Becoming +++ACTIVATED+++")
      currentValue = bid.value
      winningBuyer = sender()
      context become activated()
      cancellable.cancel()
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
    }
    case TimerTick => {
      println("Becoming IGNORED")
      context.become(ignored(currentValue))
      cancellable.cancel()
      context.parent ! AuctionIgnored
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
    }
  }

  def activated(): Receive =  {
    case GetCurrentValue => sender() ! this.currentValue
    case bid: Bid => {
      println("Auction " + self.toString() + " <<ACTIVATED>> " + " + " + bid.value.toString)
      if (bid.value > currentValue) {
        currentValue = bid.value
        winningBuyer ! LostBid(bid.value)
        winningBuyer = sender()
        context become activated()
      }
      else
        context become activated()
    }
    case TimerTick => {
      // BECOMING SOLD
      cancellable.cancel()
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
      context.become(sold())
    }
  }

  def sold(): Receive  =  {
    case GetCurrentValue => sender() ! this.currentValue
    case TimerTick => {
      context.become(stopped())
      println("Auction " + self.toString() + " <<STOPPED>> ")
      winningBuyer ! AuctionSold(currentValue, this.name)
    }
  }

  def ignored(currentValue: Double):Receive = {
    case GetCurrentValue => sender() ! this.currentValue
    case RelistAuction => {

      context become created()
      cancellable.cancel()
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
    }
    case TimerTick => {
      println("- - - - - - -- - - -  -")
      println("Auction " + this.name + " ignored")
      println("- - - - - - -- - - -  -")
      context become stopped()
      cancellable.cancel()
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
    }
  }
  def stopped():Receive = {
    case GetCurrentValue => sender() ! this.currentValue
    case TimerTick => {
      println("Definitely stopping application")
    }
    case bid: Bid => {
      sender ! DeleteAuction
      context.stop(self)
    }
  }
}