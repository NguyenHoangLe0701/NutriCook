package com.example.nutricook.di

import android.content.Context
import androidx.room.Room
import com.example.nutricook.data.local.database.NutriCookDatabase
import com.example.nutricook.data.preload.DataPreloadManager
import com.example.nutricook.data.remote.api.RecipeApi
import com.example.nutricook.data.repository.RecipeRepository
import com.google.firebase.firestore.FirebaseFirestore // Inject từ FirebaseCoreModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRecipeApi(): RecipeApi {
        return Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(api: RecipeApi): RecipeRepository = RecipeRepository(api)

    @Provides
    @Singleton
    fun provideNutriCookDatabase(@ApplicationContext context: Context): NutriCookDatabase {
        return Room.databaseBuilder(
            context,
            NutriCookDatabase::class.java,
            "nutricook_database"
        )
            .fallbackToDestructiveMigration() // Xóa và tạo lại DB khi schema thay đổi
            .build()
    }

    // FirebaseFirestore đã được provide trong FirebaseCoreModule (ProfileFirebaseModule.kt)
    // Không cần provide lại ở đây để tránh duplicate binding

    @Provides
    @Singleton
    fun provideDataPreloadManager(
        database: NutriCookDatabase,
        firestore: FirebaseFirestore // Inject từ FirebaseCoreModule
    ): DataPreloadManager {
        return DataPreloadManager(database, firestore)
    }
}