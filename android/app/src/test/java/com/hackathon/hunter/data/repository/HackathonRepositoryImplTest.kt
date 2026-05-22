package com.hackathon.hunter.data.repository

import com.hackathon.hunter.data.local.dao.HackathonDao
import com.hackathon.hunter.data.local.entity.HackathonEntity
import com.hackathon.hunter.data.local.entity.ReportedHackathonEntity
import com.hackathon.hunter.data.remote.HackathonApiService
import com.hackathon.hunter.data.remote.dto.HackathonDto
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HackathonRepositoryImplTest {

    private lateinit var hackathonDao: HackathonDao
    private lateinit var hackathonApiService: HackathonApiService
    private lateinit var repository: HackathonRepositoryImpl

    @Before
    fun setUp() {
        hackathonDao = mockk(relaxed = true)
        hackathonApiService = mockk()
        repository = HackathonRepositoryImpl(hackathonDao, hackathonApiService)
    }

    @Test
    fun `getHackathons should delegate to Dao`() = runTest {
        val testData = listOf(
            HackathonEntity(
                id = 1,
                platform = "devpost",
                platformId = "1",
                title = "Test Hackathon",
                description = "Desc",
                url = "https://example.com",
                rulesUrl = null,
                prizeType = "fiat",
                prizeCurrency = "USD",
                prizeValue = 1000.0,
                isOnline = true,
                startDate = "2026-05-21T00:00:00Z",
                endDate = "2026-05-22T00:00:00Z",
                isVietnamEligible = true,
                reportCount = 0,
                isReportedByUser = false,
                isBookmarked = false
            )
        )
        every { hackathonDao.getHackathons() } returns flowOf(testData)

        val result = repository.getHackathons().first()

        assertEquals(testData, result)
        verify { hackathonDao.getHackathons() }
    }

    @Test
    fun `fetchHackathons should fetch from API and save to DB`() = runTest {
        val dtos = listOf(
            HackathonDto(
                id = 1,
                platform = "devpost",
                platformId = "1",
                title = "Test Hackathon",
                description = "Desc",
                url = "https://example.com",
                rulesUrl = null,
                prizeType = "fiat",
                prizeCurrency = "USD",
                prizeValue = 1000.0,
                isOnline = true,
                startDate = "2026-05-21T00:00:00Z",
                endDate = "2026-05-22T00:00:00Z",
                isVietnamEligible = true,
                reportCount = 0,
                createdAt = "2026-05-21T00:00:00Z",
                updatedAt = "2026-05-21T00:00:00Z"
            )
        )
        coEvery { hackathonApiService.getHackathons(any(), any(), any(), any(), any(), any(), any(), any()) } returns dtos

        val result = repository.fetchHackathons()

        assertTrue(result.isSuccess)
        coVerify { hackathonDao.insertOrUpdate(match { it.first().id == 1 }) }
    }

    @Test
    fun `reportHackathon should immediately mark reported locally and call API`() = runTest {
        val mockDto = HackathonDto(
            id = 1,
            platform = "devpost",
            platformId = "1",
            title = "Test Hackathon",
            description = "Desc",
            url = "https://example.com",
            rulesUrl = null,
            prizeType = "fiat",
            prizeCurrency = "USD",
            prizeValue = 1000.0,
            isOnline = true,
            startDate = "2026-05-21T00:00:00Z",
            endDate = "2026-05-22T00:00:00Z",
            isVietnamEligible = false, // Changed by API
            reportCount = 3, // Changed by API
            createdAt = "2026-05-21T00:00:00Z",
            updatedAt = "2026-05-21T00:00:00Z"
        )
        val localEntity = HackathonEntity(
            id = 1,
            platform = "devpost",
            platformId = "1",
            title = "Test Hackathon",
            description = "Desc",
            url = "https://example.com",
            rulesUrl = null,
            prizeType = "fiat",
            prizeCurrency = "USD",
            prizeValue = 1000.0,
            isOnline = true,
            startDate = "2026-05-21T00:00:00Z",
            endDate = "2026-05-22T00:00:00Z",
            isVietnamEligible = true,
            reportCount = 2,
            isReportedByUser = false,
            isBookmarked = true // Bookmarked state should be preserved
        )

        coEvery { hackathonDao.getHackathonByIdOneShot(1) } returns localEntity
        coEvery { hackathonApiService.reportHackathon(1) } returns mockDto

        val result = repository.reportHackathon(1)

        assertTrue(result.isSuccess)

        // Verify optimistic updates are called instantly
        coVerify(ordering = Ordering.SEQUENCE) {
            hackathonDao.markAsReported(1)
            hackathonDao.insertReportedLog(match { it.hackathonId == 1 })
            hackathonApiService.reportHackathon(1)
            hackathonDao.getHackathonByIdOneShot(1)
            hackathonDao.update(match {
                it.id == 1 && it.reportCount == 3 && !it.isVietnamEligible && it.isBookmarked
            })
        }
    }

    @Test
    fun `reportHackathon API failure should still keep local reported status`() = runTest {
        coEvery { hackathonApiService.reportHackathon(1) } throws RuntimeException("Network Error")

        val result = repository.reportHackathon(1)

        assertTrue(result.isFailure)

        // Verify local updates were still triggered and not rolled back
        coVerify {
            hackathonDao.markAsReported(1)
            hackathonDao.insertReportedLog(match { it.hackathonId == 1 })
        }
        // Verify update database is not called since API failed
        coVerify(exactly = 0) {
            hackathonDao.update(any())
        }
    }
}
