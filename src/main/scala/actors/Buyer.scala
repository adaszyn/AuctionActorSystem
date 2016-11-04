package actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.util.Timeout
import messages._

import scala.util.{Failure, Random, Success}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import akka.actor._
import akka.pattern.ask
import misc.Timers
import scala.concurrent.ExecutionContext.Implicits.global

class Buyer() extends Actor {
  override def receive: Receive = {
    case bidAuction:BidAuction => {
      bidAuction.auctionRef ! Bid(Random.nextDouble() * 100)
    }
    case bidAuction:BidAuctionLoop => {
      implicit val timeout = Timeout(5 seconds)
      val future = context.actorSelection("/user/AuctionSearch").resolveOne()
      val auctionSearchActor = Await.result(future, timeout.duration)
      val futureAuction = auctionSearchActor ? FindAuction(bidAuction.name)
      val auctionRef = Await.result(futureAuction, timeout.duration).asInstanceOf[ActorRef]
      println(auctionRef)
      auctionRef ! Bid(Random.nextDouble() * 100)
      context.system.scheduler.scheduleOnce(Duration(Timers.BUYER_LOOP_TIMER, TimeUnit.SECONDS)) {
        self ! new BidAuctionLoop(bidAuction.name)
      }
    }
    case bidAuction:BidAuctionResponse => {
      implicit val timeout = Timeout(5 seconds)
      val future = context.actorSelection("/user/AuctionSearch").resolveOne()
      val auctionSearchActor = Await.result(future, timeout.duration)
      val futureAuction = auctionSearchActor ? FindAuction(bidAuction.name)
      val auctionRef = Await.result(futureAuction, timeout.duration).asInstanceOf[ActorRef]
      auctionRef ! Bid(Random.nextDouble() * bidAuction.maximumValue)
      context.become(bidWithResponse(bidAuction.maximumValue))
    }

    case bidAuction:BidAuctionResponseByActor => {
      bidAuction.actor ! Bid(Random.nextDouble() * bidAuction.maximumValue)
      context.become(bidWithResponse(bidAuction.maximumValue))
    }
    case DeleteAuction => {
      println("Received DELETE AUCTION")
      context.stop(self)
    }
    case auctionSold: AuctionSold => {
      println("- - - - - - -")
      println("AUCTION " + auctionSold.auctionName + " SOLD FOR: ", auctionSold.value)
      println("- - - - - - -")
    }
    case noAuction: NoAuctionFound => {
      println("NO MESSAGE FOUND WITH NAME " + noAuction.name)
    }
  }
  def bidWithResponse(maxValue: Double): Receive = {
    case lostBid: LostBid => {
      println("Buyer lost bid. Bidding again...")
      if (lostBid.value < maxValue)
        sender() ! Bid(lostBid.value + 0.5)
    }
  }
}
