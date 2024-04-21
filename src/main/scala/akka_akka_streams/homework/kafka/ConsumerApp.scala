package akka_akka_streams.homework.kafka

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWithN}
import akka.stream.{ActorMaterializer, ClosedShape, Materializer}
import ch.qos.logback.classic.{Level, Logger}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

object ConsumerApp {
  implicit val system: ActorSystem = ActorSystem("consumer")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[Logger]
    .setLevel(Level.ERROR)


  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("akka.kafka.consumer")

  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)

  val consumer = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("numbers"))
    .runWith(Sink.asPublisher(false))

  val graph = GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    //1. source
    val input = builder.add(Source.fromPublisher(consumer))
    val converter = builder.add(Flow[ConsumerRecord[String, String]].map(r => r.value().toInt))
    val multiplier1 = builder.add(Flow[Int].map(_ * 10))
    val multiplier2 = builder.add(Flow[Int].map(_ * 2))
    val multiplier3 = builder.add(Flow[Int].map(_ * 3))
    val summarizer = builder.add(Flow[(Int, Int, Int)].map(a => a._1 + a._2 + a._3))
    val output = builder.add(Sink.foreach[(Int)](println))

    val broadcast = builder.add(Broadcast[Int](3))
    val zip = builder.add(ZipWithN[Int, (Int, Int, Int)] { case Seq(a, b, c) => (a, b, c) }(3))


    //shape
    input ~> converter
    converter ~> broadcast

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