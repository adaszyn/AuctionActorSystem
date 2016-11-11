package actors

import java.util.concurrent.TimeUnit

import actors.Auction.{BidAuctionEvent, _}
import actors.AuctionSearch._
import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable}
import messages._
import akka.persistence._
import akka.util.Timeout
import misc.Timers

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class AuctionState(var currentValue: Double = 0.0, var winningBuyerId: String = null) {
  def updated(event: AuctionEvent, oldState: AuctionState): AuctionState = event match {
    case bae: BidAuctionEvent => {
      if (oldState.currentValue < bae.bidValue) {
        println("RETURNING NEW STATE", AuctionState(bae.bidValue, bae.actorId))
        AuctionState(bae.bidValue, bae.actorId)
      }
      else oldState
    }
    case _ => oldState
  }
}

object Auction {
  class AuctionEvent { }
  case class StartAuction() extends AuctionEvent
  case class AuctionStarted() extends AuctionEvent
  case class AuctionIgnored() extends AuctionEvent
  case class GetCurrentValue() extends AuctionEvent
  case class BidAuctionEvent(bidValue: Double, actorId: String) extends AuctionEvent { }

  case class ChangeStateCreatedEvent() extends AuctionEvent
  case class ChangeStateActivatedEvent() extends AuctionEvent
  case class ChangeStateSoldEvent() extends AuctionEvent
  case class ChangeStateIgnoredEvent() extends AuctionEvent
  case class ChangeStateStoppedEvent() extends AuctionEvent

}

class Auction(name: String) extends PersistentActor {
  import actors.Auction._
  override def receiveCommand = init()
  private var cancellable: Cancellable = _
  var state = AuctionState(0.0, null)

  def updateState(event: AuctionEvent) = {
    state = state.updated(event, state)
    cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
      self ! TimerTick
    }
    event match {
      case ChangeStateCreatedEvent() => context become created()
      case ChangeStateActivatedEvent() => context become activated()
      case ChangeStateSoldEvent() => context become sold()
      case ChangeStateIgnoredEvent() => context become ignored()
      case ChangeStateStoppedEvent() => context become stopped()
      case _ => Nil
    }
  }
  def sendMessageToAuctionSearch(message: Any): Unit = {
    implicit val timeout = Timeout(1 seconds)
    val future = context.actorSelection("/user/AuctionSearch").resolveOne()
    val auctionSearchActor = Await.result(future, timeout.duration)
    auctionSearchActor ! message
  }

  def init(): Receive =  {
    case StartAuction => {
      context.parent ! AuctionStarted
      persist(ChangeStateCreatedEvent()) {
        event => updateState(event)
      }
      context.become(created())
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
    }
  }
  def created(): Receive =  {
    case bid: Bid => {
      println("TRYING TO BID WITH: " + bid.value + " | current value: " + state.currentValue + "| CREATED")
      persist(BidAuctionEvent(bid.value, bid.buyerId)) {
        event => updateState(event)
      }
      persist(ChangeStateActivatedEvent()) {
        event => updateState(event)
      }
      context become activated()
      cancellable.cancel()
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
    }
    case TimerTick => {
      persist(ChangeStateIgnoredEvent()) {
        event => updateState(event)
      }
      println("Becoming IGNORED")
      context.become(ignored())
      cancellable.cancel()
      context.parent ! AuctionIgnored
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
    }
  }

  def activated(): Receive =  {
    case bid: Bid => {
      println("TRYING TO BID WITH: " + bid.value + " | current value: " + state.currentValue+ "| ACTIVATED")
      if (bid.value > state.currentValue) {
        persist(BidAuctionEvent(bid.value, bid.buyerId)) {

          event => {println(event); updateState(event)}
        }
        sendMessageToAuctionSearch(SendMessageToBuyer(state.winningBuyerId, LostBid(bid.value)))
        context become activated()
      }
      else
        context become activated()
    }
    case TimerTick => {
      // BECOMING SOLD
      persist(ChangeStateSoldEvent()) {
        event => updateState(event)
      }
      cancellable.cancel()
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
      context.become(sold())
    }
  }

  def sold(): Receive  =  {
    case TimerTick => {
      persist(ChangeStateStoppedEvent()) {
        event => updateState(event)
      }
      context.become(stopped())
      println("Auction " + self.toString() + " <<STOPPED>> ")
      sendMessageToAuctionSearch(SendMessageToBuyer(state.winningBuyerId, AuctionSold(state.currentValue, this.name)))
    }
  }

  def ignored():Receive = {
    case RelistAuction => {

      context become created()
      cancellable.cancel()
      cancellable = context.system.scheduler.scheduleOnce(Duration(Timers.TIMER, TimeUnit.SECONDS)) {
        self ! TimerTick
      }
    }
    case TimerTick => {
      persist(ChangeStateStoppedEvent()) {
        event => updateState(event)
      }
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
    case TimerTick => {
      println("Definitely stopping application")
    }
    case bid: Bid => {
      sender ! DeleteAuction
      context.stop(self)
    }
  }

  override def unhandled(message: Any): Unit = message match {
    case GetCurrentValue => sender() ! state.currentValue
    case _ => super.unhandled(message)
  }

  override def receiveRecover: Receive = {
    case evt: BidAuctionEvent => {
      println("receiveRecover", evt.actorId, evt.bidValue)
      updateState(evt)
    }
    case evt: ChangeStateCreatedEvent => updateState(evt)
    case evt: ChangeStateActivatedEvent => updateState(evt)
    case evt: ChangeStateSoldEvent => updateState(evt)
    case evt: ChangeStateIgnoredEvent => updateState(evt)
    case evt: ChangeStateStoppedEvent => updateState(evt)
    case SnapshotOffer(_, snapshot: AuctionState) => {
      println("\t SNAPSHOT OFFER")
      state = snapshot
    }
  }

  override def persistenceId: String = "persistent-auction-actor"
}