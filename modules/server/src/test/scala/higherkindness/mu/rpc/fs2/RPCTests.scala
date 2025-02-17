/*
 * Copyright 2017-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package higherkindness.mu.rpc
package fs2

import higherkindness.mu.rpc.common._
import higherkindness.mu.rpc.server._
import _root_.fs2.Stream
import org.scalatest._

class RPCTests extends RpcBaseTestSuite with BeforeAndAfterAll {

  import higherkindness.mu.rpc.fs2.Utils._
  import higherkindness.mu.rpc.fs2.Utils.database._
  import higherkindness.mu.rpc.fs2.Utils.implicits._

  override protected def beforeAll(): Unit =
    serverStart[ConcurrentMonad].unsafeRunSync()

  override protected def afterAll(): Unit =
    serverStop[ConcurrentMonad].unsafeRunSync()

  "mu-rpc server" should {

    "allow to startup a server and check if it's alive" in {

      def check[F[_]](implicit S: GrpcServer[F]): F[Boolean] =
        S.isShutdown

      check[ConcurrentMonad].unsafeRunSync() shouldBe false

    }

    "allow to get the port where it's running" in {

      def check[F[_]](implicit S: GrpcServer[F]): F[Int] =
        S.getPort

      check[ConcurrentMonad].unsafeRunSync() shouldBe SC.port

    }

  }

  "mu-rpc client with fs2.Stream as streaming implementation" should {

    "be able to run unary services" in {

      muAvroRPCServiceClient.use(_.unary(a1)).unsafeRunSync() shouldBe c1

    }

    "be able to run unary services with avro schemas" in {

      muAvroWithSchemaRPCServiceClient.use(_.unaryWithSchema(a1)).unsafeRunSync() shouldBe c1

    }

    "be able to run server streaming services" in {

      muProtoRPCServiceClient
        .use(_.serverStreaming(b1).compile.toList)
        .unsafeRunSync() shouldBe cList

    }

    "handle errors in server streaming services" in {

      def clientProgram(errorCode: String): ConcurrentMonad[List[C]] =
        muProtoRPCServiceClient
          .use(
            _.serverStreamingWithError(E(a1, errorCode))
              .handleErrorWith(ex => Stream(C(ex.getMessage, a1)))
              .compile
              .toList)

      clientProgram("SE")
        .unsafeRunSync() shouldBe List(C("INVALID_ARGUMENT: SE", a1))
      clientProgram("SRE")
        .unsafeRunSync() shouldBe List(C("INVALID_ARGUMENT: SRE", a1))
      clientProgram("RTE")
        .unsafeRunSync() shouldBe List(C("INTERNAL: RTE", a1))
      clientProgram("Thrown")
        .unsafeRunSync() shouldBe List(C("INTERNAL: Thrown", a1))
    }

    "be able to run client streaming services" in {

      muProtoRPCServiceClient
        .use(_.clientStreaming(Stream.fromIterator[ConcurrentMonad](aList.iterator)))
        .unsafeRunSync() shouldBe dResult33
    }

    "be able to run client bidirectional streaming services" in {

      muAvroRPCServiceClient
        .use(_.biStreaming(Stream.fromIterator[ConcurrentMonad](eList.iterator)).compile.toList)
        .unsafeRunSync()
        .distinct shouldBe eList

    }

    "be able to run client bidirectional streaming services with avro schema" in {

      muAvroWithSchemaRPCServiceClient
        .use(_.biStreamingWithSchema(Stream.fromIterator[ConcurrentMonad](eList.iterator)).compile.toList)
        .unsafeRunSync()
        .distinct shouldBe eList

    }

    "be able to run multiple rpc services" in {

      val tuple =
        (
          muAvroRPCServiceClient.use(_.unary(a1)),
          muAvroWithSchemaRPCServiceClient.use(_.unaryWithSchema(a1)),
          muProtoRPCServiceClient.use(_.serverStreaming(b1).compile.toList),
          muProtoRPCServiceClient.use(
            _.clientStreaming(Stream.fromIterator[ConcurrentMonad](aList.iterator))),
          muAvroRPCServiceClient.use(
            _.biStreaming(Stream.fromIterator[ConcurrentMonad](eList.iterator)).compile.toList),
          muAvroWithSchemaRPCServiceClient.use(_.biStreamingWithSchema(
            Stream.fromIterator[ConcurrentMonad](eList.iterator)).compile.toList))

      tuple._1.unsafeRunSync() shouldBe c1
      tuple._2.unsafeRunSync() shouldBe c1
      tuple._3.unsafeRunSync() shouldBe cList
      tuple._4.unsafeRunSync() shouldBe dResult33
      tuple._5.unsafeRunSync().distinct shouldBe eList
      tuple._6.unsafeRunSync().distinct shouldBe eList

    }

  }

  "mu-rpc client with fs2.Stream as streaming implementation and compression enabled" should {

    "be able to run unary services" in {

      muCompressedAvroRPCServiceClient.use(_.unaryCompressed(a1)).unsafeRunSync() shouldBe c1

    }

    "be able to run unary services with avro schema" in {

      muCompressedAvroWithSchemaRPCServiceClient
        .use(_.unaryCompressedWithSchema(a1))
        .unsafeRunSync() shouldBe c1

    }

    "be able to run server streaming services" in {

      muCompressedProtoRPCServiceClient
        .use(_.serverStreamingCompressed(b1).compile.toList)
        .unsafeRunSync shouldBe cList

    }

    "be able to run client streaming services" in {

      muCompressedProtoRPCServiceClient
        .use(_.clientStreamingCompressed(Stream.fromIterator[ConcurrentMonad](aList.iterator)))
        .unsafeRunSync() shouldBe dResult33
    }

    "be able to run client bidirectional streaming services" in {

      muCompressedAvroRPCServiceClient
        .use(_.biStreamingCompressed(Stream.fromIterator[ConcurrentMonad](eList.iterator)).compile.toList)
        .unsafeRunSync()
        .distinct shouldBe eList

    }

    "be able to run client bidirectional streaming services with avro schema" in {

      muCompressedAvroWithSchemaRPCServiceClient
        .use(
          _.biStreamingCompressedWithSchema(Stream.fromIterator[ConcurrentMonad](eList.iterator)).compile.toList)
        .unsafeRunSync()
        .distinct shouldBe eList

    }

    "be able to run multiple rpc services" in {

      val tuple =
        (
          muCompressedAvroRPCServiceClient.use(_.unaryCompressed(a1)),
          muCompressedAvroWithSchemaRPCServiceClient.use(_.unaryCompressedWithSchema(a1)),
          muCompressedProtoRPCServiceClient.use(_.serverStreamingCompressed(b1).compile.toList),
          muCompressedProtoRPCServiceClient.use(
            _.clientStreamingCompressed(Stream.fromIterator[ConcurrentMonad](aList.iterator))),
          muCompressedAvroRPCServiceClient.use(_.biStreamingCompressed(
            Stream.fromIterator[ConcurrentMonad](eList.iterator)).compile.toList),
          muCompressedAvroWithSchemaRPCServiceClient
            .use(
              _.biStreamingCompressedWithSchema(
                Stream.fromIterator[ConcurrentMonad](eList.iterator)).compile.toList))

      tuple._1.unsafeRunSync() shouldBe c1
      tuple._2.unsafeRunSync() shouldBe c1
      tuple._3.unsafeRunSync() shouldBe cList
      tuple._4.unsafeRunSync() shouldBe dResult33
      tuple._5.unsafeRunSync().distinct shouldBe eList
      tuple._6.unsafeRunSync().distinct shouldBe eList

    }

  }

}
