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

import higherkindness.mu.rpc.idlgen.util.Toolbox.u._
import higherkindness.mu.rpc.idlgen.util.AstOptics.ast
import higherkindness.mu.rpc.protocol.{Avro, AvroWithSchema, Custom, Protobuf, SerializationType}

package object idlgen {

  val DefaultRequestParamName = "arg"
  val EmptyType               = "Empty.type"

  val ScalaFileExtension = ".scala"

  val serializationTypes: Map[String, SerializationType] =
    Map(
      "Protobuf"       -> Protobuf,
      "Avro"           -> Avro,
      "AvroWithSchema" -> AvroWithSchema,
      "Custom"         -> Custom)

  object BaseType {
    def unapply(tpe: Tree): Option[String] = tpe match {
      case ast._Ident(Ident(TypeName(name))) => Some(name)
      case _                                 => None
    }
  }

  object SingleAppliedTypeTree {
    def unapply(tpe: Tree): Option[(String, Tree)] = tpe match {
      case ast._AppliedTypeTree(AppliedTypeTree(ast._Ident(Ident(TypeName(ctor))), List(tree))) =>
        Some((ctor, tree))
      case _ => None
    }
  }

  object SingletonType {
    def unapply(tpe: Tree): Option[String] = tpe match {
      case ast._SingletonTypeTree(SingletonTypeTree(ast._Ident(Ident(TermName(t))))) => Some(t)
      case _                                                                         => None
    }
  }
}
