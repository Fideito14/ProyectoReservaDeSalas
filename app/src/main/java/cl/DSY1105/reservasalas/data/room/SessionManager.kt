
package cl.DSY1105.reservasalas.data.room

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore("app_prefs")
        val JWT_TOKEN = stringPreferencesKey("jwt_token")
    }

    val tokenFlow: Flow<String> =
        context.dataStore.data.map { it[JWT_TOKEN] ?: "" }

    suspend fun saveToken(token: String) {
        context.dataStore.edit {
            it[JWT_TOKEN] = token
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit {
            it.remove(JWT_TOKEN)
        }
    }
}
