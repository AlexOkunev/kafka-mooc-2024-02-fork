package akka_akka_streams.AkkaDataStreams
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}


object AkkaStreamGraph {
  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()

  val graoh = GraphDSL.create(){ implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    //1. source
    val input = builder.add(Source(1 to 100))
    val increment = builder.add(Flow[Int].map(x=>x+1))
    val multiplier = builder.add(Flow[Int].map(x=>x*10))
    val output = builder.add(Sink.foreach[(Int, Int)](println))

    val broadcast = builder.add(Broadcast[Int](2))
    val zip = builder.add(Zip[Int, Int])

    //shape
    input ~> broadcast

    broadcast.out(0) ~> increment ~> zip.in0
    broadcast.out(1) ~> multiplier ~> zip.in1

    zip.out ~> output

    //close shape
    ClosedShape
  }

  def main(args: Array[String]): Unit = {
    RunnableGraph.fromGraph(graoh).run()
  }


}