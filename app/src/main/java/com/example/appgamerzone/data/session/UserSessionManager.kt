package com.example.appgamerzone.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appgamerzone.data.model.User
import com.example.appgamerzone.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class UserSessionManager(private val context: Context) {

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        private val USER_ADDRESS_KEY = stringPreferencesKey("user_address")
    }

    suspend fun saveUser(user: User) {
        android.util.Log.d("UserSessionManager", "🔐 Guardando usuario en sesión:")
        android.util.Log.d("UserSessionManager", "  - ID: '${user.id}'")
        android.util.Log.d("UserSessionManager", "  - Email: '${user.email}'")
        android.util.Log.d("UserSessionManager", "  - Name: '${user.fullName}'")
        android.util.Log.d("UserSessionManager", "  - ID length: ${user.id.length}")
        android.util.Log.d("UserSessionManager", "  - ID is empty: ${user.id.isEmpty()}")

        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_EMAIL_KEY] = user.email
            preferences[USER_NAME_KEY] = user.fullName
            preferences[USER_ROLE_KEY] = user.role.name
            preferences[USER_PHONE_KEY] = user.phone
            preferences[USER_ADDRESS_KEY] = user.address
        }

        // Verificar que se guardó
        val savedUserId = currentUserId.first()
        android.util.Log.d("UserSessionManager", "✅ Verificación - userId guardado: '$savedUserId'")
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    val currentUserId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val currentUserRole: Flow<UserRole?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]?.let {
            try {
                UserRole.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    val currentUserName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val currentUserEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY] != null
    }

    val isAdmin: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY] == UserRole.ADMIN.name
    }

    suspend fun getUserId(): String? {
        var userId: String? = null
        context.dataStore.data.collect { preferences ->
            userId = preferences[USER_ID_KEY]
        }
        return userId
    }
}

