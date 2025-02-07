package com.razorquake.morselens.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private val UNIT_TIME = longPreferencesKey("unit_time")

    val unitTimeFlow: Flow<Long> = context.datastore.data
        .map { preferences ->
            preferences[UNIT_TIME] ?: 200L
        }

    suspend fun setUnitTime(unitTime: Long) {
        context.datastore.edit { preferences ->
            preferences[UNIT_TIME] = unitTime
        }
    }
}