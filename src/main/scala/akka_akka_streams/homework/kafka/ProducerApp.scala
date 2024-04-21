package akka_akka_streams.homework.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future
import scala.util.{Failure, Success}

object ProducerApp extends App {
  implicit val system: ActorSystem = ActorSystem("producer")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val config = ConfigFactory.load()
  val producerConfig = config.getConfig("akka.kafka.producer")

  val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

  val producer: Future[Done] =
    Source(1 to 100)
      .map(value => new ProducerRecord[String, String]("numbers", value.toString))
      .runWith(Producer.plainSink(producerSettings))

  producer onComplete {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }
}