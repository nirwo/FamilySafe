package nir.wolff.api

import android.location.Location
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PikudHaorefApiTest {
    private lateinit var api: PikudHaorefApi

    @Before
    fun setup() {
        api = PikudHaorefApi()
    }

    @Test
    fun testGetActiveAlerts() = runBlocking {
        val alerts = api.getActiveAlerts()
        // Even if there are no active alerts, the API should return an empty list, not null
        assertNotNull(alerts)
    }

    @Test
    fun testLocationInTelAviv() {
        val location = Location("test").apply {
            latitude = 32.0853
            longitude = 34.7818
        }

        val alert = PikudHaorefApi.Alert(
            id = "test",
            title = "Test Alert",
            description = "Test Description",
            area = "תל אביב - מרכז העיר",
            timestamp = System.currentTimeMillis()
        )

        assertTrue(api.isLocationInAlertArea(location, alert))
    }

    @Test
    fun testLocationOutsideRange() {
        val location = Location("test").apply {
            latitude = 31.0 // Far from any defined district
            longitude = 34.0
        }

        val alert = PikudHaorefApi.Alert(
            id = "test",
            title = "Test Alert",
            description = "Test Description",
            area = "תל אביב - מרכז העיר",
            timestamp = System.currentTimeMillis()
        )

        assertFalse(api.isLocationInAlertArea(location, alert))
    }
}
