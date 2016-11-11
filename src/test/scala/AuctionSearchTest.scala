import actors.{Auction, AuctionSearch, Buyer, Seller}
import akka.testkit.TestKit
import akka.actor.{ActorRef, ActorSystem}
import org.scalatest.WordSpecLike
import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestActorRef
import messages.{FindAuction, RegisterAuction}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask

import scala.concurrent.Await

class AuctionSearchTest extends TestKit(ActorSystem("AuctionSearchSpec")) with WordSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    system.terminate
  }
  "AuctionSearch" must {

    "save auction in map during registration" in {
      val auctionSearch = TestActorRef(new AuctionSearch())
      val seller = TestActorRef(new Seller("TestSeller"))
      val auction = TestActorRef(new Auction("TestAuction"))

      auctionSearch ! RegisterAuction(auction, "HELLO")
      assert (auctionSearch.underlyingActor.auctionMap.getOrElse("HELLO", null) == auction)
    }

    "return previously registered auction" in {
      val auctionSearch = TestActorRef(new AuctionSearch())
      val seller = TestActorRef(new Seller("TestSeller"))
      val auction = TestActorRef(new Auction("TestAuction"))

      auctionSearch ! RegisterAuction(auction, "HELLO")

      implicit val timeout = Timeout(1 seconds)
      val futureAuction = auctionSearch ? FindAuction("HELLO")
      val auctionRef = Await.result(futureAuction, timeout.duration).asInstanceOf[ActorRef]
      assert (auctionRef == auction)
    }
  }
}

