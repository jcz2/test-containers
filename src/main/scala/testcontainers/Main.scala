package testcontainers

import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder

object Main extends IOApp {
  val mongoClient = new MongoClient("localhost", 27017)
  val todoDAO = new TodoDao(mongoClient)
  val todoService = new TodoService(todoDAO)

  override def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(todoService.routes)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
}