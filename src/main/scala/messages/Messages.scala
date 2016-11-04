package messages

import akka.actor.{ActorRef, ActorSystem, Cancellable}

case class InitAuctionApp(system: ActorSystem)

case class SayHello(value:String)
case class Bid(value:Double)
case class LostBid(value:Double)
case class BidAuction(auctionRef:ActorRef)
case class BidAuctionLoop(name: String)
case class BidAuctionResponse (name: String, maximumValue: Double)
case class BidAuctionResponseByActor (actor: ActorRef, maximumValue: Double)

case class StartAuction()
case class AuctionStarted()
case class AuctionIgnored()
case class GetCurrentValue()

case class DeleteAuction()
case class RelistAuction()
case class TimerTick(cancellable: Cancellable)


// Creating auctions
case class CreateAuction(name: String)
case class NoAuctionFound(name: String)

case class InitAuctionSearch()
case class RegisterAuction(auctionRef: ActorRef, id: String)
case class FindAuction(id: String)


case class Identify()
case class Identity(actorRef: ActorRef)



case class AuctionSold(value: Double, auctionName: String)