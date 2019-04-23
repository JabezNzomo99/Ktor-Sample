package com.example

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {
    fun insertUser(user:User){
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(User)
        }
    }
}