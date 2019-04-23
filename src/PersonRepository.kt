package com.example

import java.lang.IllegalArgumentException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger

object PersonRepository {
    private val idCounter = AtomicInteger()
    private val persons = CopyOnWriteArraySet<Person>()

    fun addPerson(person: Person):Person{
        if(persons.contains(person)){
           return persons.find {
               it == person
           }!!
        }
        val personId = idCounter.incrementAndGet()
        person.id = personId
        persons.add(person)
        return person
    }

    fun getPerson(id:String)= persons.find { it.id.toString() == id } ?: throw IllegalArgumentException("No entity found for $id")
    fun getPerson(id:Int) =  getPerson(id.toString())
    fun getAll() = persons.toList()
    fun removePerson(person: Person){
        if(persons.contains(person)){
            persons.remove(person)
        }else{
            throw IllegalArgumentException("Person not contained in repo")
        }
    }
    fun removePerson(id:String) = persons.remove(getPerson(id))
    fun removePerson(id:Int)=persons.remove(getPerson(id))
    fun clear() = persons.clear()


}