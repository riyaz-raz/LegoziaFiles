package com.legozia.files.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.legozia.files.model.SortDirection
import com.legozia.files.model.SortType
import com.legozia.files.model.ViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.filePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "file_preferences"
)

class FilePreferences(private val context: Context) {
    
    companion object {
        private val VIEW_MODE_KEY = stringPreferencesKey("view_mode")
        private val SORT_TYPE_KEY = stringPreferencesKey("sort_type")
        private val SORT_DIRECTION_KEY = stringPreferencesKey("sort_direction")
        private val SHOW_HIDDEN_FILES_KEY = booleanPreferencesKey("show_hidden_files")
        private val FAVORITES_KEY = stringSetPreferencesKey("favorites")
        private val RECENT_FILES_KEY = stringSetPreferencesKey("recent_files")
    }
    
    val viewMode: Flow<ViewMode> = context.filePreferencesDataStore.data.map { preferences ->
        val mode = preferences[VIEW_MODE_KEY] ?: ViewMode.LIST.name
        ViewMode.valueOf(mode)
    }
    
    val sortType: Flow<SortType> = context.filePreferencesDataStore.data.map { preferences ->
        val type = preferences[SORT_TYPE_KEY] ?: SortType.NAME.name
        SortType.valueOf(type)
    }
    
    val sortDirection: Flow<SortDirection> = context.filePreferencesDataStore.data.map { preferences ->
        val direction = preferences[SORT_DIRECTION_KEY] ?: SortDirection.ASCENDING.name
        SortDirection.valueOf(direction)
    }
    
    val showHiddenFiles: Flow<Boolean> = context.filePreferencesDataStore.data.map { preferences ->
        preferences[SHOW_HIDDEN_FILES_KEY] ?: false
    }
    
    val favorites: Flow<Set<String>> = context.filePreferencesDataStore.data.map { preferences ->
        preferences[FAVORITES_KEY] ?: emptySet()
    }
    
    val recentFiles: Flow<Set<String>> = context.filePreferencesDataStore.data.map { preferences ->
        preferences[RECENT_FILES_KEY] ?: emptySet()
    }
    
    suspend fun setViewMode(mode: ViewMode) {
        context.filePreferencesDataStore.edit { preferences ->
            preferences[VIEW_MODE_KEY] = mode.name
        }
    }
    
    suspend fun setSortType(type: SortType) {
        context.filePreferencesDataStore.edit { preferences ->
            preferences[SORT_TYPE_KEY] = type.name
        }
    }
    
    suspend fun setSortDirection(direction: SortDirection) {
        context.filePreferencesDataStore.edit { preferences ->
            preferences[SORT_DIRECTION_KEY] = direction.name
        }
    }
    
    suspend fun setShowHiddenFiles(show: Boolean) {
        context.filePreferencesDataStore.edit { preferences ->
            preferences[SHOW_HIDDEN_FILES_KEY] = show
        }
    }
    
    suspend fun addFavorite(path: String) {
        context.filePreferencesDataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            preferences[FAVORITES_KEY] = currentFavorites + path
        }
    }
    
    suspend fun removeFavorite(path: String) {
        context.filePreferencesDataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            preferences[FAVORITES_KEY] = currentFavorites - path
        }
    }
    
    suspend fun addRecentFile(path: String) {
        context.filePreferencesDataStore.edit { preferences ->
            val currentRecent = preferences[RECENT_FILES_KEY] ?: emptySet()
            // Keep only last 50 recent files
            val updatedRecent = (currentRecent + path).toList().takeLast(50).toSet()
            preferences[RECENT_FILES_KEY] = updatedRecent
        }
    }

    
    suspend fun clearRecentFiles() {
        context.filePreferencesDataStore.edit { preferences ->
            preferences[RECENT_FILES_KEY] = emptySet()
        }
    }
}
