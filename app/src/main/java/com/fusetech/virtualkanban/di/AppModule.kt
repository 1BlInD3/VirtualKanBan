package com.fusetech.virtualkanban.di

import android.content.Context
import com.fusetech.virtualkanban.BaseApplication
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.retrofit.RetrofitFunctions
import com.fusetech.virtualkanban.utils.SaveFile
import com.fusetech.virtualkanban.utils.SqlLogic
import com.fusetech.virtualkanban.utils.XML
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): BaseApplication {
        return app as BaseApplication
    }
    @Singleton
    @Provides
    fun getSql():SqlLogic{
        return SqlLogic()
    }
    @Singleton
    @Provides
    fun getSave(): SaveFile{
        return SaveFile()
    }
    @Singleton
    @Provides
    fun getXml(): XML{
        return XML()
    }
    @Singleton
    @Provides
    fun getRetro(): RetrofitFunctions{
        return RetrofitFunctions()
    }

    @Singleton
    @Provides
    fun getMain(): MainActivity{
        return MainActivity()
    }

}