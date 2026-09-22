package com.visionguia.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionguia.data.local.EmergencyContactEntity
import com.visionguia.data.local.VisionGuiaDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisionGuiaDatabaseInstrumentedTest {
    @Test fun primaryEmergencyContactPersists() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val database = Room.inMemoryDatabaseBuilder(context, VisionGuiaDatabase::class.java).build()
            try {
            database.dao().savePrimaryContact(EmergencyContactEntity(name = "Contacto de prueba", phone = "999999999"))
            val saved = database.dao().primaryContact()
            assertEquals("Contacto de prueba", saved?.name)
            assertEquals("999999999", saved?.phone)
            } finally {
                database.close()
            }
        }
    }
}
