package akka_akka_streams.homework

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWithN}
import akka.stream.{ActorMaterializer, ClosedShape}


object homeworktemplate {
  implicit val system = ActorSystem("fusion")
  implicit val materializer = ActorMaterializer()

  val graph = GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    //1. source
    val input = builder.add(Source(1 to 100))
    val multiplier1 = builder.add(Flow[Int].map(_ * 10))
    val multiplier2 = builder.add(Flow[Int].map(_ * 2))
    val multiplier3 = builder.add(Flow[Int].map(_ * 3))
    val summarizer = builder.add(Flow[(Int, Int, Int)].map(a => a._1 + a._2 + a._3))
    val output = builder.add(Sink.foreach[(Int)](println))

    val broadcast = builder.add(Broadcast[Int](3))
    val zip = builder.add(ZipWithN[Int, (Int, Int, Int)] { case Seq(a, b, c) => (a, b, c) }(3))

    //shape
    input ~> broadcast

    broadcast.out(0) ~> multiplier1 ~> zip.in(0)
    broadcast.out(1) ~> multiplier2 ~> zip.in(1)
    broadcast.out(2) ~> multiplier3 ~> zip.in(2)

    zip.out ~> summarizer

    summarizer.out ~> output

    //close shape
    ClosedShape
  }

  def main(args: Array[String]): Unit = {
    RunnableGraph.fromGraph(graph).run()

  }
}