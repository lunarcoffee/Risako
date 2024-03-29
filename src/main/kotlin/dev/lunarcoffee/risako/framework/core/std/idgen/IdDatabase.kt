package dev.lunarcoffee.risako.framework.core.std.idgen

import dev.lunarcoffee.risako.framework.core.DB
import org.litote.kmongo.eq

object IdDatabase {
    private val activeCol = DB.getCollection<GeneratedId>("ActiveIdGen0")

    suspend fun register(id: Long) = activeCol.insertOne(GeneratedId(id))
    suspend fun delete(id: Long) = activeCol.deleteOne(GeneratedId::id eq id)
    suspend fun contains(id: Long) = activeCol.findOne(GeneratedId::id eq id) != null
}
