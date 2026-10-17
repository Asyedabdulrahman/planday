package com.planday.app.di

import android.content.Context
import androidx.room.Room
import com.planday.app.data.local.dao.DaySettingsDao
import com.planday.app.data.local.dao.ProjectDao
import com.planday.app.data.local.dao.ReminderDao
import com.planday.app.data.local.dao.ReminderLogDao
import com.planday.app.data.local.dao.TaskDao
import com.planday.app.data.local.db.PlanDayDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PlanDayDatabase {
        return Room.databaseBuilder(
            context,
            PlanDayDatabase::class.java,
            "planday.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTaskDao(db: PlanDayDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideReminderDao(db: PlanDayDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideReminderLogDao(db: PlanDayDatabase): ReminderLogDao = db.reminderLogDao()

    @Provides
    fun provideProjectDao(db: PlanDayDatabase): ProjectDao = db.projectDao()

    @Provides
    fun provideDaySettingsDao(db: PlanDayDatabase): DaySettingsDao = db.daySettingsDao()
}
