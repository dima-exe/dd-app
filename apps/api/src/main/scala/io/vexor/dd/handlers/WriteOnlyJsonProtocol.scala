package io.vexor.dd.handlers

import spray.json._

trait WriteOnlyJsonProtocol[T] {
  def read(json: JsValue): T = {
    deserializationError("Write only record")
  }
}

