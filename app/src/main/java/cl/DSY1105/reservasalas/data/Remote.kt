package cl.DSY1105.reservasalas.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// Contrato esperado del microservicio Spring Boot. 10.0.2.2 = localhost del PC desde el emulador.
interface ApiService {
    @GET("salas") suspend fun salas(): List<Sala>
    @GET("reservas") suspend fun reservas(@Query("fecha") fecha: String): List<Reserva>
    @POST("reservas") suspend fun reservar(@Body r: Reserva): Reserva
    @DELETE("reservas/{id}") suspend fun cancelar(@Path("id") id: Long)
}

object Api {
    val service: ApiService by lazy {
        Retrofit.Builder().baseUrl("http://10.0.2.2:8080/api/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java)
    }
}
