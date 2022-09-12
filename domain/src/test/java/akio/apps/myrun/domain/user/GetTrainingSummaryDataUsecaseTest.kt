package akio.apps.myrun.domain.user

import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.user.api.model.PlaceIdentifier
import akio.apps.myrun.domain.time.TimeProvider
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GetTrainingSummaryDataUsecaseTest {

    private lateinit var usecase: GetTrainingSummaryDataUsecase
    private lateinit var mockedActivityRepository: ActivityRepository
    private lateinit var mockedTimeProvider: TimeProvider

    private val defaultUserId: String = "defaultUserId"

    @Before
    fun setup() {
        mockedActivityRepository = mock()
        mockedTimeProvider = mock()
        usecase = GetTrainingSummaryDataUsecase(
            mockedActivityRepository,
            mockedTimeProvider
        )
    }

    @Test
    fun test() = runTest {
        val timeZoneRawOffset = TimeZone.getDefault().rawOffset
        whenever(mockedTimeProvider.currentTimeMillis()).thenReturn(
            1639353600000L + timeZoneRawOffset // 00:00:00 Dec 13 2021 (GMT)
        )

        val timeRangeStart = 1635724800000 - timeZoneRawOffset // 00:00:00 1 Nov 2021 (local)
        val timeRangeEnd = 1640995200000 - timeZoneRawOffset // 00:00:00 1 Jan 2022 (local)
        whenever(
            mockedActivityRepository.getActivitiesInTimeRange(
                defaultUserId,
                timeRangeStart,
                timeRangeEnd
            )
        ).thenReturn(
            listOf(
                createActivity(
                    ActivityType.Running,
                    startTime = 1639440000000L - timeZoneRawOffset, // 00:00:00 14 Dec 2021
                    duration = 7,
                    distance = 8.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = 1639526400000L - timeZoneRawOffset, // 00:00:00 15 Dec 2021
                    duration = 5,
                    distance = 6.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = 1639008000000L - timeZoneRawOffset, // 00:00:00 9 Dec 2021
                    duration = 3L,
                    distance = 4.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = 1639094400000L - timeZoneRawOffset, // 00:00:00 10 Dec 2021
                    duration = 1L,
                    distance = 2.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = 1640908800000L - timeZoneRawOffset, // 00:00:00 31 Dec 2021
                    duration = 15,
                    distance = 16.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = 1640822400000L - timeZoneRawOffset, // 00:00:00 30 Dec 2021
                    duration = 13,
                    distance = 14.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = 1638144000000 - timeZoneRawOffset, // 00:00:00 29 Nov 2021
                    duration = 11,
                    distance = 12.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = 1638230400000 - timeZoneRawOffset, // 00:00:00 30 Nov 2021
                    duration = 9,
                    distance = 10.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = 1639440000000L - timeZoneRawOffset, // 00:00:00 14 Dec 2021
                    duration = 23,
                    distance = 24.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = 1639526400000L - timeZoneRawOffset, // 00:00:00 15 Dec 2021
                    duration = 21,
                    distance = 22.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = 1639008000000L - timeZoneRawOffset, // 00:00:00 9 Dec 2021
                    duration = 19,
                    distance = 20.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = 1639094400000L - timeZoneRawOffset, // 00:00:00 10 Dec 2021
                    duration = 17,
                    distance = 18.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = 1640908800000L - timeZoneRawOffset, // 00:00:00 31 Dec 2021
                    duration = 31,
                    distance = 32.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = 1640822400000L - timeZoneRawOffset, // 00:00:00 30 Dec 2021
                    duration = 29,
                    distance = 30.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = 1638144000000 - timeZoneRawOffset, // 00:00:00 29 Nov 2021
                    duration = 27,
                    distance = 28.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = 1638230400000 - timeZoneRawOffset, // 00:00:00 30 Nov 2021
                    duration = 25,
                    distance = 26.0
                )
            )
        )

        val summaryTableMap = usecase.getUserTrainingSummaryData(defaultUserId)
        verify(mockedActivityRepository).getActivitiesInTimeRange(
            defaultUserId,
            timeRangeStart,
            timeRangeEnd
        )

        val runSummary = summaryTableMap[ActivityType.Running]
        val rideSummary = summaryTableMap[ActivityType.Cycling]
        assertNotNull(runSummary)
        assertNotNull(rideSummary)
        assertEquals(
            GetTrainingSummaryDataUsecase.TrainingSummaryData(
                distance = 14.0,
                time = 12,
                activityCount = 2
            ),
            runSummary.thisWeekSummary
        )
        assertEquals(
            GetTrainingSummaryDataUsecase.TrainingSummaryData(
                distance = 6.0,
                time = 4,
                activityCount = 2
            ),
            runSummary.lastWeekSummary
        )
        assertEquals(
            GetTrainingSummaryDataUsecase.TrainingSummaryData(
                distance = 50.0,
                time = 44,
                activityCount = 6
            ),
            runSummary.thisMonthSummary
        )
        assertEquals(
            GetTrainingSummaryDataUsecase.TrainingSummaryData(
                distance = 22.0,
                time = 20,
                activityCount = 2
            ),
            runSummary.lastMonthSummary
        )
        assertEquals(
            GetTrainingSummaryDataUsecase.TrainingSummaryData(
                distance = 46.0,
                time = 44,
                activityCount = 2
            ),
            rideSummary.thisWeekSummary
        )
        assertEquals(
            GetTrainingSummaryDataUsecase.TrainingSummaryData(
                distance = 38.0,
                time = 36,
                activityCount = 2
            ),
            rideSummary.lastWeekSummary
        )
        assertEquals(
            GetTrainingSummaryDataUsecase.TrainingSummaryData(
                distance = 146.0,
                time = 140,
                activityCount = 6
            ),
            rideSummary.thisMonthSummary
        )
        assertEquals(
            GetTrainingSummaryDataUsecase.TrainingSummaryData(
                distance = 54.0,
                time = 52,
                activityCount = 2
            ),
            rideSummary.lastMonthSummary
        )
    }

    private fun createActivity(
        activityType: ActivityType,
        startTime: Long,
        duration: Long,
        distance: Double,
    ): BaseActivityModel = when (activityType) {
        ActivityType.Running -> RunningActivityModel(
            ActivityDataModel(
                "id",
                ActivityType.Running,
                "name",
                "routeImage",
                PlaceIdentifier.fromPlaceIdentifierString("placeIdentifier"),
                startTime = startTime,
                endTime = 1000L,
                duration = duration,
                distance = distance,
                "encodedPolyline",
                AthleteInfo("userId", "userName", "userAvatar")
            ),
            pace = 0.0,
            cadence = 0
        )
        ActivityType.Cycling -> CyclingActivityModel(
            ActivityDataModel(
                "id",
                ActivityType.Cycling,
                "name",
                "routeImage",
                PlaceIdentifier.fromPlaceIdentifierString("placeIdentifier"),
                startTime = startTime,
                endTime = 1000L,
                duration = duration,
                distance = distance,
                "encodedPolyline",
                AthleteInfo("userId", "userName", "userAvatar")
            ),
            speed = 0.0
        )
        else -> throw Exception()
    }
}
