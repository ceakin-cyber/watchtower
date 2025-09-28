package com.example.myapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.ui.incident.Incident
import com.example.myapplication.data.security.DatabaseEncryption

@Database(
    entities = [Incident::class, EvidenceAttachment::class],
    version = 6,
    exportSchema = false
)
abstract class IncidentDatabase : RoomDatabase() {
    
    abstract fun incidentDao(): IncidentDao
    abstract fun evidenceAttachmentDao(): EvidenceAttachmentDao
    
    companion object {
        @Volatile
        private var INSTANCE: IncidentDatabase? = null
        
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration to support encryption - no schema changes needed
                // The encryption happens at the application layer
                // This migration just marks the transition to encrypted storage
            }
        }
        
        fun getDatabase(context: Context): IncidentDatabase {
            return INSTANCE ?: synchronized(this) {
                println("DEBUG: Creating new database instance")
                
                // Initialize encryption
                val encryption = DatabaseEncryption.getInstance(context)
                EncryptionConverters.setEncryption(encryption)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IncidentDatabase::class.java,
                    "incident_database"
                )
                .addMigrations(MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        println("DEBUG: Database created with encryption - checking tables")
                        // Check if the incidents table exists
                        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='incidents'")
                        val tableExists = cursor.count > 0
                        println("DEBUG: Incidents table exists: $tableExists")
                        cursor.close()
                    }
                })
                .build()
                INSTANCE = instance
                println("DEBUG: Database instance created with encryption")
                instance
            }
        }
    }
}