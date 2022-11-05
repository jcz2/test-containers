package testcontainers

import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.{AsyncDriver, DB, MongoConnection}
import reactivemongo.api.bson.collection.{BSONCollection, BSONCollectionProducer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoClient(host: String, port: Int) {
  val mongoUri = s"mongodb://${host}:${port}"

  val todosCollection: Future[BSONCollection] = {
    val driver = new AsyncDriver()
    val parsedURI: Future[ParsedURI] = MongoConnection.fromString(mongoUri)
    val connection: Future[MongoConnection] = parsedURI.flatMap(uri => driver.connect(uri))
    val todosDB: Future[DB] = connection.flatMap(_.database("todosDB"))
    todosDB.map(_.collection("todos"))
  }
}
