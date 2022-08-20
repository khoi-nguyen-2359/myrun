package akio.apps.myrun.feature.userfollow.ui

import akio.apps.myrun.feature.core.ktx.rememberViewModelProvider
import akio.apps.myrun.feature.core.ui.AppBarArrowBackButton
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.StatusBarSpacer
import akio.apps.myrun.feature.profile.R
import akio.apps.myrun.feature.userfollow.UserFollowViewModel
import akio.apps.myrun.feature.userfollow.UserFollowViewModel.Companion.INIT_SCREEN_STATE
import akio.apps.myrun.feature.userfollow.di.DaggerUserFollowFeatureComponent
import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.paging.PagingData
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@Composable
fun UserFollowScreen(navEntry: NavBackStackEntry, navController: NavController) = AppTheme {
    val application = LocalContext.current.applicationContext as Application
    val userFollowViewModel = navEntry.rememberViewModelProvider {
        DaggerUserFollowFeatureComponent.factory().create(application).userFollowViewModel()
    }
    UserFollowScreenContent(navController, userFollowViewModel)
}

@ExperimentalPagerApi
@Composable
private fun UserFollowScreenContent(
    navController: NavController,
    userFollowViewModel: UserFollowViewModel,
) {
    val screenState by userFollowViewModel.screenState.collectAsState(initial = INIT_SCREEN_STATE)
    Column(modifier = Modifier.fillMaxSize()) {
        StatusBarSpacer()
        TopAppBar(
            navigationIcon = { AppBarArrowBackButton(navController) },
            title = { Text(text = stringResource(id = R.string.userfollow_title)) },
        )

        UserFollowTabs(screenState)
    }
}

@ExperimentalPagerApi
@Composable
private fun ColumnScope.UserFollowTabs(screenState: UserFollowViewModel.ScreenState) {
    val tabLabelResIds =
        listOf(R.string.userfollow_tab_following, R.string.userfollow_tab_followers)
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.weight(1f)) {
        TabRow(modifier = Modifier.fillMaxWidth(), selectedTabIndex = pagerState.currentPage) {
            tabLabelResIds.forEachIndexed { index, labelResId ->
                UserFollowTypeTab(labelResId, pagerState.currentPage == index) {
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                }
            }
        }
        HorizontalPager(
            count = screenState.tabStates.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            UserFollowList(screenState.tabStates[pageIndex])
        }
    }
}

@Composable
private fun UserFollowList(tabState: UserFollowViewModel.TabState) {
    // val lazyPagingItems = tabState.pagingDataFlow.collectAsLazyPagingItems()
    // LazyColumn(
    //     modifier = Modifier
    //         .fillMaxWidth()
    //         .fillMaxHeight(),
    //     state = rememberLazyListState()
    // ) {
    //     items(
    //         lazyPagingItems,
    //         key = { userFollow -> userFollow.uid }
    //     ) { userFollow ->
    //         if (userFollow == null) {
    //             return@items
    //         }
    //         Row {
    //             UserAvatarImage(imageUrl = userFollow.photoUrl)
    //             Text(text = userFollow.displayName + userFollow.status)
    //         }
    //     }
    // }
    Text(text = "Page $tabState")
}

@ExperimentalPagerApi
@Composable
private fun UserFollowTypeTab(
    @StringRes tabLabelResId: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = { Text(text = stringResource(tabLabelResId)) }
        // selectedContentColor = AppColors.primary,
        // unselectedContentColor = AppColors.black
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
@Preview(backgroundColor = 0xffffff, showBackground = true)
private fun PreviewUserFollowTabs() =
    Column {
        UserFollowTabs(
            UserFollowViewModel.ScreenState(
                tabStates = listOf(
                    UserFollowViewModel.TabState(
                        pagingDataFlow = flowOf(PagingData.empty()),
                        isSelected = true
                    ),
                    UserFollowViewModel.TabState(
                        pagingDataFlow = flowOf(PagingData.empty()),
                        isSelected = false
                    ),
                )
            )
        )
    }
