package com.example

import io.ktor.application.*
import io.ktor.client.request.request
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.*
import io.ktor.html.respondHtml
import io.ktor.http.*
import io.ktor.request.receive
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.PipelineContext
import kotlinx.html.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.text.DateFormat

const val REST_ENDPOINT ="/person"
const val USER_REST_ENDPOINT ="/user"

fun Application.main(){
    Database.connect("jdbc:mysql://localhost:3306/test", driver = "com.mysql.jdbc.Driver",
        user = "root", password = "")
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(User)
    }
    install(DefaultHeaders)
    install(CORS){
        method(HttpMethod.Post)
        anyHost()
        maxAge = Duration.ofDays(1)
    }
    install(ContentNegotiation){
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }
    routing {
        get("$REST_ENDPOINT/{id}"){
            errorAware {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Parameter id not found")
                call.respond(PersonRepository.getPerson(id))
            }
        }
        get(REST_ENDPOINT){
            errorAware {
                call.respond(PersonRepository.getAll())
            }
        }
        delete("$REST_ENDPOINT/{id}"){
            errorAware {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Parameter id not found")
                call.respondSuccessJson(PersonRepository.removePerson(id))
            }
        }
        delete(REST_ENDPOINT){
            errorAware {
                PersonRepository.clear()
                call.respondSuccessJson()
            }
        }
        post(REST_ENDPOINT) {
            errorAware {
                val receive = call.receive<Person>()
                println(receive)
                call.respond(PersonRepository.addPerson(receive))
            }
        }

        get(USER_REST_ENDPOINT){
                errorAware {
                    val usersFromDb = transaction {
                        User.selectAll().toList()
                    }
                    call.respond(HttpStatusCode.OK,usersFromDb)
                }
            }
        post(USER_REST_ENDPOINT){
                errorAware{
                    val userReceive = call.receive<UserModel>()
                    transaction {
                        User.insert {
                            it[name] = userReceive.name
                            it[age] = userReceive.age
                        }
                    }
                }
            }
        delete("$USER_REST_ENDPOINT/{id}"){
                errorAware {
                    val userId = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("User Id Not Found")
                    val deletedUser = transaction {
                        User.deleteWhere {
                            User.id eq userId
                        }
                    }
                    call.respond(HttpStatusCode.OK,deletedUser)
                }
            }



        get("/"){
            call.respondHtml {
                head {
                    title("Kotlin API")
                }
                body {
                    div {
                        h1 {
                            +"Welcome : Ktor Maestro"
                        }
                    }
                    p{
                        +"Proceed to /person to use API"
                    }
                }
            }
        }
    }

}
private suspend fun <R> PipelineContext<*, ApplicationCall>.errorAware(block: suspend () -> R): R? {
    return try {
        block()
    } catch (e: Exception) {
        call.respondText("""{"error":"$e"}"""
            , ContentType.parse("application/json")
            , HttpStatusCode.InternalServerError)
        null
    }
}

private suspend fun ApplicationCall.respondSuccessJson(value: Boolean = true) = respond("""{"success": "$value"}""")