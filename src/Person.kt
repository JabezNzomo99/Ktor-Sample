package com.example

import org.jetbrains.exposed.sql.Table

data class Person(val name:String,val age:Int){
    var id:Int?=null
}