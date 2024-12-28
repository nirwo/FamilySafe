package nir.wolff.service

import android.location.Location
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertServiceTest {
    private lateinit var alertService: AlertService

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(context)
        alertService = AlertService()
    }

    @Test
    fun testCheckForAlerts() = runBlocking {
        val location = Location("test").apply {
            latitude = 32.0853 // Tel Aviv center
            longitude = 34.7818
        }

        // This will actually try to fetch alerts from Pikud Haoref
        val result = alertService.checkForAlerts("test-group-id", location)
        // We can't assert true/false as it depends on whether there are actual alerts
        // But the call should complete without throwing exceptions
        assertNotNull(result)
    }

    @Test
    fun testMarkUserStatus() = runBlocking {
        try {
            alertService.markUserStatus(
                userId = "test-user-id",
                groupId = "test-group-id",
                isSafe = true
            )
            // If we reach here, the function completed without throwing
            assertTrue(true)
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }
}
