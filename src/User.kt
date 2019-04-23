package com.example

import org.jetbrains.exposed.dao.IntIdTable

object User : IntIdTable() {
    val name = varchar("name", 50)
    val age = integer("age")
}