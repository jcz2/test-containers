package testcontainers

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io.{->, /, Created, DELETE, GET, NoContent, Ok, POST, Root}
import org.http4s.dsl.io._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

class TodoService(todoDao: TodoDao) {
  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "todos" =>
      todoDao
        .getAllTodos
        .map(_.asJson)
        .map(_.toString())
        .flatMap(Ok(_))

    case req @ POST -> Root / "todos" =>
      req.as[AddTodoRequest]
        .flatMap(todo => todoDao.addTodo(todo))
        .flatMap {
          case Some(id) => Created(id.asJson.toString())
          case None => InternalServerError()
        }

    case DELETE -> Root / "todos" / id =>
      todoDao
        .deleteTodo(id)
        .flatMap(_ => NoContent())
  }
  .orNotFound
}
