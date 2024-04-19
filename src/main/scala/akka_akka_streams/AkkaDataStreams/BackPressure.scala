package akka_akka_streams.AkkaDataStreams

import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.actor.ActorSystem

object BackPressure extends App {
  val sourceFast = Source(1 to 1000)
  val flow = Flow[Int].map{el =>
    println(s"Flow inside: $el")
    el +10
  }

  val sinkSlow = Sink.foreach[Int]{el =>
    Thread.sleep(1000)
    println(s"Sink inside: $el")
  }

  val flowWithBuffer = flow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)

  implicit  val system = ActorSystem("fusion")
  implicit val materializer = ActorMaterializer()

  sourceFast.async
    .via(flowWithBuffer).async
    .to(sinkSlow)
    .run()
}