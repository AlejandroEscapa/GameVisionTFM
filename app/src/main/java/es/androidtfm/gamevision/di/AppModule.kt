package es.androidtfm.gamevision.di

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.data.catalog.GameCatalog
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
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideGamesApi(): GameApiService = RetrofitInstance.gamesApi

    /**
     * Catálogo de juegos. Hoy RAWG; cambiar a IGDB (o añadir uno secundario) es
     * cambiar esta línea — ninguna pantalla ni ViewModel toca DTOs del proveedor.
     */
    @Provides
    @Singleton
    fun provideGameCatalog(gamesApi: GameApiService): GameCatalog = RawgGameCatalog(gamesApi)

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
