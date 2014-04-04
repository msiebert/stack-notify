package stack_notify.models

import anorm._
import anorm.SqlParser._
import AnormImplicits._
import java.util.UUID
import play.api.db.DB
import play.api.Play.current

case class Tag (
	id: UUID,
	tag: String
) {
	/**
	 * Create a new Tag record with a randomly generated id
	 * @param tag the content of the tag
	 */
	def this(tag: String) = this(UUID.randomUUID(), tag)
}

object TagModel {

	private case class UserTag (
		userId: UUID,
		tagId: UUID
	)

	private val userTagParser = {
		get[UUID]("user_id") ~
		get[UUID]("tag_id") map {
			case user_id ~ tag_id => new UserTag(user_id, tag_id)
		}
	}

	protected val tagParser = {
		get[UUID]("id") ~
		get[String]("tag") map {
			case id ~ tag => new Tag(id, tag)
		}
	}

	/**
	 * Create a new Tag in the database
	 * @param tag the new Tag
	 */
	def create(tag: Tag): Unit = {
		DB.withConnection("default") { connection =>
			SQL("""
				INSERT INTO `tags` (`id`, `tag`)
				VALUES ({id}, {tag})
			""").on(
				"id" -> tag.id,
				"tag" -> tag.tag
			).executeUpdate()(connection)
		}
	}

	/**
	 * Get all Tags for a User
	 * @param userId the id of the user to look up
	 * @return a List of Tags
	 */
	def tagsForUser(userId: UUID): List[Tag] = {
		DB.withConnection("default") { connection =>
			val tags = SQL("""
				SELECT *
				FROM `users_tags`
				WHERE `user_id`={user_id}
			""").on(
				"user_id" -> userId
			).as(userTagParser *)(connection).map { tag => 
				tag.tagId 
			}.toList

			if (tags.size == 0) {
				List[Tag]()
			}
			else {
				RichSQL("""
					SELECT *
					FROM `tags`
					WHERE `id` IN ({tag_ids})
				""").onList(
					"tag_ids" -> tags
				).toSQL.as(tagParser *)(connection)
			}
		}
	}

	/**
	 * Get the ids of all Tags that have content contained in the list passed in
	 * @param tags the list of tag names
	 * @return the list of tag ids
	 */
	def tagIds(tags: List[String]): List[UUID] = {
		DB.withConnection("default") { connection =>
			val dbTags = RichSQL("""
				SELECT *
				FROM `tags`
				WHERE `tag` IN ({tags})
			""").onList(
				"tags" -> tags
			).toSQL.as(tagParser *)(connection).map { tag => 
				(tag.tag -> tag.id)
			}.toMap

			//add any tags that aren't already there
			val tagIds = tags.foldLeft(Map[String, UUID]()) { case (result, current) =>
				if (!dbTags.contains(current)) {
					val tag = new Tag(current)
					create(tag)
					result + (tag.tag -> tag.id)
				}
				else {
					result
				}
			} ++ dbTags

			tagIds.map { case (tag, id) =>
				id
			}.toList
		}
	}

	/**
	 * Add tags for a user
	 * @param userId the id of the user to add tags to
	 * @param tags the tag ids of the tags to add to the user
	 */
	def createTagsForUser(userId: UUID, tags: List[UUID]): Unit = {
		DB.withConnection("default") { connection =>
			RichSQL("""
				INSERT INTO `users_tags` (`user_id`, `tag_id`)
				VALUES ({fields})
			""").multiInsert(tags.size, Seq("user_id", "tag_id"))(
				"user_id" -> (0 until tags.size).map { _ => toParameterValue(userId) },
				"tag_id" -> tags.map(toParameterValue(_))
			).toSQL.executeUpdate()(connection)
		}
	}

	/**
	 * Delete all tags for a user
	 * @param userId the id of the user for whom to delete all tags
	 */
	def deleteTagsForUser(userId: UUID): Unit = {
		DB.withConnection("default") { connection =>
			SQL("""
				DELETE FROM `users_tags`
				WHERE `user_id`={user_id}
			""").on(
				"user_id" -> userId
			).executeUpdate()(connection)
		}
	}

}