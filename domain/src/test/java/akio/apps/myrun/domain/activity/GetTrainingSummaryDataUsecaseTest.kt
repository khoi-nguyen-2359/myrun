package akio.apps.myrun.domain.activity

import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GetTrainingSummaryDataUsecaseTest {

    private lateinit var usecase: GetTrainingSummaryDataUsecase
    private lateinit var mockedActivityRepository: ActivityRepository
    private lateinit var mockedUserAuthenticationState: UserAuthenticationState
    private val testCoroutineDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    private val defaultUserId: String = "defaultUserId"

    @Before
    fun setup() {
        mockedActivityRepository = mock()
        mockedUserAuthenticationState = mock()
        usecase = GetTrainingSummaryDataUsecase(
            mockedActivityRepository,
            mockedUserAuthenticationState,
            testCoroutineDispatcher
        )
    }

    @Test
    fun test() = testCoroutineDispatcher.runBlockingTest {
        whenever(mockedUserAuthenticationState.requireUserAccountId()).thenReturn(defaultUserId)

        val biWeekRange = 1630886400000..1632096000000 // 2021/09/6 - 2021/09/20
        val biMonthRange = 1627776000000..1633046400000 // 2021/08/01 - 2021/10/01

        // bi month runs
        whenever(
            mockedActivityRepository.getActivitiesInTimeRange(
                defaultUserId,
                ActivityType.Running,
                biMonthRange.first,
                biMonthRange.last
            )
        ).thenReturn(
            listOf(
                createActivity(
                    ActivityType.Running,
                    startTime = biWeekRange.last - 1,
                    duration = 7,
                    distance = 8.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = biWeekRange.last - 2,
                    duration = 5,
                    distance = 6.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = biWeekRange.first + 2,
                    duration = 3L,
                    distance = 4.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = biWeekRange.first + 1,
                    duration = 1L,
                    distance = 2.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = biMonthRange.last - 1,
                    duration = 15,
                    distance = 16.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = biMonthRange.last - 2,
                    duration = 13,
                    distance = 14.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = biMonthRange.first + 2,
                    duration = 11,
                    distance = 12.0
                ),
                createActivity(
                    ActivityType.Running,
                    startTime = biMonthRange.first + 1,
                    duration = 9,
                    distance = 10.0
                )
            )
        )

        // bi month rides
        whenever(
            mockedActivityRepository.getActivitiesInTimeRange(
                defaultUserId,
                ActivityType.Cycling,
                biMonthRange.first,
                biMonthRange.last
            )
        ).thenReturn(
            listOf(
                createActivity(
                    ActivityType.Cycling,
                    startTime = biWeekRange.last - 1,
                    duration = 23,
                    distance = 24.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = biWeekRange.last - 2,
                    duration = 21,
                    distance = 22.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = biWeekRange.first + 2,
                    duration = 19,
                    distance = 20.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = biWeekRange.first + 1,
                    duration = 17,
                    distance = 18.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = biMonthRange.last - 1,
                    duration = 31,
                    distance = 32.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = biMonthRange.last - 2,
                    duration = 29,
                    distance = 30.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = biMonthRange.first + 2,
                    duration = 27,
                    distance = 28.0
                ),
                createActivity(
                    ActivityType.Cycling,
                    startTime = biMonthRange.first + 1,
                    duration = 25,
                    distance = 26.0
                ),
            )
        )
        val summaryTableMap = usecase.getUserTrainingSummaryData()
        verify(mockedActivityRepository).getActivitiesInTimeRange(
            defaultUserId,
            ActivityType.Running,
            biMonthRange.first,
            biMonthRange.last
        )
        verify(mockedActivityRepository).getActivitiesInTimeRange(
            defaultUserId,
            ActivityType.Cycling,
            biMonthRange.first,
            biMonthRange.last
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
    ): ActivityModel = when (activityType) {
        ActivityType.Running -> RunningActivityModel(
            ActivityDataModel(
                "id",
                ActivityType.Running,
                "name",
                "routeImage",
                "placeIdentifier",
                startTime = startTime,
                endTime = 1000L,
                duration = duration,
                distance = distance,
                "encodedPolyline",
                ActivityModel.AthleteInfo("userId", "userName", "userAvatar")
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
                "placeIdentifier",
                startTime = startTime,
                endTime = 1000L,
                duration = duration,
                distance = distance,
                "encodedPolyline",
                ActivityModel.AthleteInfo("userId", "userName", "userAvatar")
            ),
            speed = 0.0
        )
        else -> throw Exception()
    }
}
