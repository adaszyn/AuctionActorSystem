import actors.Auction.{AuctionIgnored, AuctionStarted, StartAuction}
import actors.{Auction, AuctionSearch, Seller}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import messages._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

class SellerTest extends TestKit(ActorSystem("SellerSpec")) with WordSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    system.terminate
  }
  "Seller" must {

    "get response from auction when started" in {
      val parent = TestProbe()
      val child = parent.childActorOf(Props(new Auction("HELLO")))
      parent.send(child, StartAuction)
      parent.expectMsg(AuctionStarted)
    }

    "get response from auction when ignored" in {
      val parent = TestProbe()
      val child = parent.childActorOf(Props(new Auction("HELLO")))
      parent.send(child, StartAuction)

      parent.expectMsg(1 seconds, AuctionStarted)
      parent.expectMsg(10 seconds, AuctionIgnored)
    }
  }
}

