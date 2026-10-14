package com.ehealthwares.rxsoft.data.remote.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NotificationApiContractTest {

    private lateinit var server: MockWebServer
    private lateinit var api: NotificationApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NotificationApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getNotifications_returnsParsedFeed() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "data": [
                        {
                          "id": "n1",
                          "title": "New order request",
                          "body": "Order #1001 is awaiting approval",
                          "type": "order_request",
                          "sourceEntityType": "request",
                          "sourceEntityId": "ord_1",
                          "sourceEntityRef": "1001",
                          "read": false,
                          "createdAt": "2026-10-10T08:30:00Z"
                        },
                        {
                          "id": "n2",
                          "title": "Stock alert",
                          "body": "Paracetamol 500mg below reorder level",
                          "type": "stock_alert",
                          "sourceEntityType": "item",
                          "sourceEntityId": "item_9",
                          "sourceEntityRef": "PCM-500",
                          "read": true,
                          "readAt": "2026-10-09T10:00:00Z",
                          "createdAt": "2026-10-09T09:00:00Z"
                        }
                      ],
                      "meta": { "page": 1, "limit": 50, "total": 2 }
                    }
                    """.trimIndent(),
                ),
        )

        val response = api.getNotifications(since = "2026-10-01T00:00:00Z", limit = 50)

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(2, body!!.data.size)

        val first = body.data[0]
        assertEquals("n1", first.id)
        assertEquals("New order request", first.title)
        assertEquals("request", first.sourceEntityType)
        assertEquals("ord_1", first.sourceEntityId)
        assertEquals("1001", first.sourceEntityRef)
        assertFalse(first.read)
        assertEquals("2026-10-10T08:30:00Z", first.createdAt)

        val second = body.data[1]
        assertTrue(second.read)
        assertEquals("2026-10-09T10:00:00Z", second.readAt)

        assertEquals(2, body.meta?.total)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/notifications", request.requestUrl?.encodedPath)
        assertEquals("2026-10-01T00:00:00Z", request.requestUrl?.queryParameter("since"))
        assertEquals("50", request.requestUrl?.queryParameter("limit"))
    }

    @Test
    fun getUnreadCount_returnsCount() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"count": 7}"""))

        val response = api.getUnreadCount()

        assertTrue(response.isSuccessful)
        assertEquals(7, response.body()?.count)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/notifications/unread-count", request.requestUrl?.encodedPath)
    }

    @Test
    fun markRead_sendsPutToReadEndpoint() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val response = api.markRead("n1")

        assertTrue(response.isSuccessful)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/notifications/n1/read", request.requestUrl?.encodedPath)
    }

    @Test
    fun markAllRead_sendsPatchToReadAllEndpoint() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val response = api.markAllRead()

        assertTrue(response.isSuccessful)

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/api/notifications/read-all", request.requestUrl?.encodedPath)
    }

    @Test
    fun registerSubscription_sendsPost() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody("{}"))

        val response = api.registerSubscription(emptyMap())

        assertTrue(response.isSuccessful)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/notification-subscriptions", request.requestUrl?.encodedPath)
    }
}
