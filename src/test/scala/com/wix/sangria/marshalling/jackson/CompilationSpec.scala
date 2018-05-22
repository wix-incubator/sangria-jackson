/*
 *    Copyright 2018 Wix.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.wix.sangria.marshalling.jackson

import com.wix.sangria.marshalling.jackson.JacksonConfiguration.objectMapper
import org.scalatest.{Matchers, WordSpec}
import sangria.marshalling.ToInput
import sangria.marshalling.testkit.Article

class CompilationSpec extends WordSpec with Matchers {
  "BasicJacksonMarshallerForObjectMapper" should {
    val marshaller = new BasicJacksonMarshallerForObjectMapper(objectMapper)

    import marshaller._

    "not derive from input for case classes" in {
      "implicitly[FromInput[Article]]" shouldNot compile
    }

    "not derive from inputs for regular classes" in {
      "implicitly[FromInput[AClass]]" shouldNot compile
    }

    "not derive from inputs for traits" in {
      "implicitly[FromInput[ATrait]]" shouldNot compile
    }

    "not derive to inputs for case classes" in {
      "implicitly[ToInput[Article, marshaller.JacksonResultMarshaller.Node]]" shouldNot compile
    }

    "not derive to inputs for regular classes" in {
      "implicitly[ToInput[AClass, marshaller.JacksonResultMarshaller.Node]]" shouldNot compile
    }

    "not derive to inputs for traits" in {
      "implicitly[ToInput[ATrait, marshaller.JacksonResultMarshaller.Node]]" shouldNot compile
    }
  }
  
  "ToInputDerivation" should {
    val marshaller = new BasicJacksonMarshallerForObjectMapper(objectMapper)
      with ToInputDerivation

    import marshaller._

    "derive to inputs for case classes" in {
      implicitly[ToInput[Article, marshaller.JacksonResultMarshaller.Node]] should not.be(null)
    }

    "derive to inputs for regular classes" in {
      implicitly[ToInput[AClass, marshaller.JacksonResultMarshaller.Node]] should not.be(null)
    }

    "derive to inputs for traits" in {
      implicitly[ToInput[ATrait, marshaller.JacksonResultMarshaller.Node]] should not.be(null)
    }
  }
}

class AClass

trait ATrait
