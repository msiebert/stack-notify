package stack_notify.controllers

import play.api._
import play.api.data._
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc._

trait StackNotifyController extends Controller {

	/**
	 * Return a JSON success message
	 * @param map the values to return
	 */
	def success(fields: (String, JsValueWrapper)*) = {
		val seq = Seq(("success", Json.toJson(true)), ("result", Json.obj(fields:_*)))

		Ok(JsObject(seq.toSeq))
	}

	/**
	 * Return a JSON failure message
	 * @param message the message to return
	 */
	def failure(message: String) = {
		Ok(Json.obj(
			"success"-> false,
			"message"-> message
		))
	}

}