import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.{ForEachTestContainer, GenericContainer}
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{EntityDecoder, Method, Request, Response}
import org.scalatest.funspec.AnyFunSpec
import testcontainers.{AddTodoRequest, AddTodoResponse, MongoClient, Todo, TodoDao, TodoService}
import org.http4s.client.dsl.io._
import org.http4s.dsl.io.{DELETE, GET, POST}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers.wait.strategy.Wait
import org.http4s.circe._

class TodoServiceSpec extends AnyFunSpec with ForEachTestContainer with Matchers {

  override val container: GenericContainer = GenericContainer(
    "mongo:5.0",
    exposedPorts = Seq(27017),
    waitStrategy = Wait.defaultWaitStrategy()
  )

  def getTodoService: TodoService = {
    val host = container.containerIpAddress
    val port = container.mappedPort(27017)

    val mongoClient = new MongoClient(host, port)
    val todoDAO = new TodoDao(mongoClient)
    new TodoService(todoDAO)
  }

  def getBody[T](response: Response[IO])(implicit entityDecoder: EntityDecoder[IO, T]): T = response
    .as[T]
    .unsafeRunSync()

  def makeRequest(todoService: TodoService, request: Request[IO]): Response[IO] = todoService.routes
    .run(request)
    .unsafeRunSync()

  describe("TodoService") {
    describe("GET /todos") {
      it("should return a list of todos") {
        val todoService = getTodoService

        val todo1 = AddTodoRequest("make dinner")
        val todo2 = AddTodoRequest("feed the dog")

        val createdTodos = List(todo1, todo2)
          .map(_.asJson)
          .map(POST(_, uri"/todos"))
          .map(makeRequest(todoService, _))
          .map(getBody[AddTodoResponse](_))

        val expected = List(Todo(todo1.title, createdTodos(0).id), Todo(todo2.title, createdTodos(1).id))

        val response = makeRequest(todoService, Request(method = Method.GET, uri = uri"/todos"))

        getBody[List[Todo]](response) shouldEqual expected
        response.status.code shouldEqual 200
      }
    }

    describe("POST /todos") {
      it("should create a todo and return it's id") {
        val todoService = getTodoService

        val response = makeRequest(todoService, POST(AddTodoRequest("do taxes").asJson, uri"/todos"))

        val responseBody = getBody[AddTodoResponse](response)

        response.status.code shouldEqual 201
        responseBody shouldEqual AddTodoResponse(responseBody.id)
      }
    }

    describe("DELETE /todos") {
      it("should delete a todo") {
        val todoService = getTodoService

        makeRequest(todoService, POST(AddTodoRequest("do laundry").asJson, uri"/todos"))

        val getResponse = makeRequest(todoService, GET(uri"/todos"))
        val todos = getBody[List[Todo]](getResponse)

        todos shouldEqual List(Todo("do laundry", todos.head.id))

        val deleteResponse = makeRequest(todoService, DELETE(uri"todos" / todos.head.id))

        deleteResponse.status.code shouldEqual 204

        val getResponse2 = makeRequest(todoService, GET(uri"/todos"))

        getBody[List[Todo]](getResponse2) shouldEqual List.empty
      }
    }
  }
}
