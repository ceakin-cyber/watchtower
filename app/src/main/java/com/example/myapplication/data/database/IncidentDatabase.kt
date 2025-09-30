package com.example.myapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.ui.incident.Incident
import com.example.myapplication.data.security.DatabaseEncryption

@Database(
    entities = [Incident::class, EvidenceAttachment::class, EmergencyContact::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(EncryptionConverters::class)
abstract class IncidentDatabase : RoomDatabase() {
    
    abstract fun incidentDao(): IncidentDao
    abstract fun evidenceAttachmentDao(): EvidenceAttachmentDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    
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
        
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add emergency contacts table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS emergency_contacts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        phoneNumber TEXT NOT NULL,
                        relationship TEXT NOT NULL,
                        isPrimary INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """)
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
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
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