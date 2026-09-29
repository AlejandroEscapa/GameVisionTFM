package es.androidtfm.gamevision.di

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.data.catalog.CachedGameCatalog
import es.androidtfm.gamevision.data.catalog.GameCatalog
import es.androidtfm.gamevision.data.catalog.local.CatalogDatabase
import es.androidtfm.gamevision.data.catalog.local.GameDao
import es.androidtfm.gamevision.data.catalog.rawg.RawgGameCatalog
import es.androidtfm.gamevision.datastore.SessionPreferences
import es.androidtfm.gamevision.datastore.ThemeDataStore
import es.androidtfm.gamevision.retrofit.GameApiService
import es.androidtfm.gamevision.retrofit.NewsApiService
import es.androidtfm.gamevision.retrofit.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named
import javax.inject.Singleton

/**
 * Módulo Hilt con los singleton de la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        // Persistencia offline (F0/T0.14): las escrituras sin red se guardan en disco
        // y se sincronizan al volver la conexión (CA0.2). Se declara explícito a
        // propósito: el default del SDK ya es caché persistente, y así un cambio de
        // default no lo desactiva por debajo de la app.
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideGamesApi(): GameApiService = RetrofitInstance.gamesApi

    /**
     * Base de datos Room de la caché de catálogo (F0/T0.4). Alcance acotado:
     * fichas visitadas + búsquedas recientes (decisión D0.3).
     */
    @Provides
    @Singleton
    fun provideCatalogDatabase(@ApplicationContext context: Context): CatalogDatabase =
        Room.databaseBuilder(context, CatalogDatabase::class.java, "catalog.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideGameDao(database: CatalogDatabase): GameDao = database.gameDao()

    /**
     * Catálogo de juegos. Hoy RAWG envuelto en caché local con modo degradado
     * (F0/T0.4-T0.5); cambiar a IGDB es cambiar la implementación de dentro —
     * ninguna pantalla ni ViewModel toca DTOs del proveedor.
     */
    @Provides
    @Singleton
    fun provideGameCatalog(gamesApi: GameApiService, gameDao: GameDao): GameCatalog =
        CachedGameCatalog(RawgGameCatalog(gamesApi), gameDao)

    @Provides
    @Singleton
    fun provideNewsApi(): NewsApiService = RetrofitInstance.newsApi

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager =
        CredentialManager.create(context)

    @Provides
    @Named("webClientId")
    fun provideWebClientId(@ApplicationContext context: Context): String =
        context.getString(R.string.default_web_client_id)

    @Provides
    @Singleton
    fun provideThemeDataStore(@ApplicationContext context: Context): ThemeDataStore =
        ThemeDataStore(context)

    /**
     * Scope de aplicación para corrutinas de ViewModels que sobreviven a la pantalla
     * (no usar para datos que deban morir con el ViewModel).
     */
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideSessionPreferences(@ApplicationContext context: Context): SessionPreferences =
        SessionPreferences(context)
}
