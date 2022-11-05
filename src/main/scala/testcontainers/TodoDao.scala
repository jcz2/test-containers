package testcontainers

import cats.effect.IO
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros, document}

import scala.concurrent.ExecutionContext.Implicits.global

class TodoDao(mongoClient: MongoClient) {
  import TodoDao._

  def getAllTodos: IO[List[Todo]] = {
    val result = mongoClient
      .todosCollection
      .flatMap(_.find(document()).cursor().collect[List](25))
    IO.fromFuture(IO(result))
  }

  def addTodo(todo: AddTodoRequest): IO[Option[AddTodoResponse]] = {
    val id = BSONObjectID.generate()
    val doc = BSONDocument("title" -> todo.title, "_id" -> id)
    val result = mongoClient
      .todosCollection
      .flatMap(_.insert.one(doc))
      .map(writeResult => {
        if (writeResult.n == 1) {
          Some(AddTodoResponse(id.stringify))
        } else {
          None
        }
      })
    IO.fromFuture(IO(result))
  }

  def deleteTodo(id: String): IO[BSONCollection#FindAndModifyResult] = {
    IO.fromTry(BSONObjectID.parse(id))
      .flatMap(_id => {
        val result = mongoClient
          .todosCollection
          .flatMap(_.findAndRemove(BSONDocument("_id" -> _id)))
        IO.fromFuture(IO(result))
      })
  }
}

object TodoDao {
  implicit val todoReader: BSONDocumentReader[Todo] = BSONDocumentReader.from[Todo] { bson =>
    for {
      title <- bson.getAsTry[String]("title")
      id <- bson.getAsTry[BSONObjectID]("_id").map(_.stringify)
    } yield Todo(title, id)
  }

  implicit val todoPostRequestDTOWriter: BSONDocumentWriter[AddTodoRequest] = Macros.writer[AddTodoRequest]
}