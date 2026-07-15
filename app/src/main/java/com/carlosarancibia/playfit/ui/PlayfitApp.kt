package com.carlosarancibia.playfit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carlosarancibia.playfit.model.ProductOnboardingDraft
import com.carlosarancibia.playfit.model.ProductPlayNextModel
import com.carlosarancibia.playfit.model.ProductProfile
import com.carlosarancibia.playfit.model.ProductGameState
import com.carlosarancibia.playfit.model.ProductTasteHistoryEntry
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.ui.components.design.GlowBackground
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.screens.AuthScreen
import com.carlosarancibia.playfit.ui.screens.DecisionIntroScreen
import com.carlosarancibia.playfit.ui.screens.GameDossierNotFound
import com.carlosarancibia.playfit.ui.screens.GameDossierError
import com.carlosarancibia.playfit.ui.screens.GameDossierLoading
import com.carlosarancibia.playfit.ui.screens.GameDossierScreen
import com.carlosarancibia.playfit.ui.screens.OnboardingScreen
import com.carlosarancibia.playfit.ui.screens.PicksScreen
import com.carlosarancibia.playfit.ui.screens.PlayNextScreen
import com.carlosarancibia.playfit.ui.screens.ResetPasswordScreen
import com.carlosarancibia.playfit.ui.screens.SearchScreen
import com.carlosarancibia.playfit.ui.screens.SettingsScreen
import com.carlosarancibia.playfit.ui.screens.SplashScreen
import com.carlosarancibia.playfit.ui.screens.GameNode
import com.carlosarancibia.playfit.ui.screens.NodeType
import com.carlosarancibia.playfit.ui.screens.TasteMapVisualizerScreen
import com.carlosarancibia.playfit.ui.screens.buildMapNodes
import com.carlosarancibia.playfit.ui.screens.TasteScreen
import com.carlosarancibia.playfit.ui.screens.calculateGameCoordinates
import com.carlosarancibia.playfit.ui.viewmodel.PlayfitViewModel
import com.carlosarancibia.playfit.ui.viewmodel.DossierUiState
import com.carlosarancibia.playfit.model.ProductPlatformSelection
import com.carlosarancibia.playfit.model.ProductAccessStatus
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.model.ProductState
import com.carlosarancibia.playfit.data.auth.AuthResult

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem("play-next", "Play Next", Icons.Default.PlayArrow),
    BottomNavItem("picks", "Picks", Icons.Default.FavoriteBorder),
    BottomNavItem("taste", "Taste", Icons.AutoMirrored.Filled.List),
    BottomNavItem("search", "Search", Icons.Default.Search),
    BottomNavItem("settings", "Settings", Icons.Default.Settings),
)

private val topLevelRoutes = bottomNavItems.map { it.route }

@Composable
fun PlayfitApp(
    viewModel: PlayfitViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.ui.collectAsState()
    val playNext by viewModel.playNext.collectAsState()
    val picks by viewModel.picks.collectAsState()
    val platforms by viewModel.platforms.collectAsState()
    val platformsUi by viewModel.platformsUi.collectAsState()
    val productState by viewModel.state.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val pendingPasswordRecovery by viewModel.pendingPasswordRecovery.collectAsState()

    var showOnboarding by remember { mutableStateOf(false) }
    var showAuth by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.toast) {
        uiState.toast?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = uiState.toastActionLabel,
                duration = if (uiState.toastActionLabel == "Undo") {
                    androidx.compose.material3.SnackbarDuration.Long
                } else {
                    androidx.compose.material3.SnackbarDuration.Short
                },
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed &&
                uiState.toastActionLabel == "Undo"
            ) {
                viewModel.undoLastDecision()
            }
            viewModel.clearToast()
        }
    }

    LaunchedEffect(showAuth, authState.isAuthenticated, authState.isAnonymous) {
        if (showAuth && authState.isAuthenticated && !authState.isAnonymous) {
            showAuth = false
        }
    }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    if (showAuth) {
        Dialog(
            onDismissRequest = { showAuth = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AuthScreen(
                    onDismiss = { showAuth = false },
                    onGoogleSignIn = {
                        viewModel.signInWithGoogle().also {
                            if (it is AuthResult.Success) showAuth = false
                        }
                    },
                    onEmailSignIn = { email, password ->
                        viewModel.signInWithEmail(email, password).also {
                            if (it is AuthResult.Success) showAuth = false
                        }
                    },
                    onEmailSignUp = { email, password ->
                        viewModel.signUpWithEmail(email, password).also {
                            if (it is AuthResult.Success) showAuth = false
                        }
                    },
                    onGuestSignIn = {
                        viewModel.signInAsGuest().also {
                            if (it is AuthResult.Success) showAuth = false
                        }
                    },
                    onResetPassword = { email ->
                        viewModel.resetPassword(email)
                    },
                    isAnonymous = authState.isAnonymous,
                )
            }
        }
    }

    if (pendingPasswordRecovery != null) {
        Dialog(
            onDismissRequest = { viewModel.cancelPendingPasswordRecovery() },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                ResetPasswordScreen(
                    onSubmit = { newPassword -> viewModel.updatePassword(newPassword) },
                    onCancel = { viewModel.cancelPendingPasswordRecovery() },
                    onSuccess = {},
                )
            }
        }
    }

    if (!onboardingCompleted) {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!showOnboarding) {
                DecisionIntroScreen(
                    onStartCalibration = {
                        viewModel.resetOnboardingState()
                        showOnboarding = true
                    },
                    onSignIn = { showAuth = true },
                    themeMode = themeMode,
                    onThemeChange = { viewModel.setThemeMode(it) },
                    onSearchGames = { query -> viewModel.searchGames(query) },
                )
            } else {
                OnboardingScreen(
                    viewModel = viewModel,
                    onComplete = { platforms, liked, disliked ->
                        val draft = ProductOnboardingDraft(
                            platforms = platforms.map { p ->
                                ProductPlatformSelection(
                                    platformId = p,
                                    status = ProductAccessStatus.Available,
                                )
                            },
                            likedGameIds = liked,
                            dislikedGameIds = disliked,
                        )
                        viewModel.completeOnboarding(draft)
                    },
                    onCancel = { showOnboarding = false },
                    platforms = platforms,
                    onSearchGames = { query -> viewModel.searchGames(query) },
                    themeMode = themeMode,
                    onThemeChange = { viewModel.setThemeMode(it) },
                )
            }
        }
        return
    }

    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (item.route == "picks" && picks.isNotEmpty()) {
                                    BadgedBox(badge = {
                                        Badge { Text("${picks.size}", style = MaterialTheme.typography.labelSmall) }
                                    }) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sync status bar
            if (uiState.refreshing || uiState.saving || uiState.pendingSync || uiState.showingStaleData) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = PlayfitSpacing.md, vertical = 7.dp),
                ) {
                    Text(
                        text = when {
                            uiState.refreshing -> "Syncing changes\u2026"
                            uiState.saving -> "Saving\u2026"
                            uiState.pendingSync -> "Changes saved on this device; waiting to sync"
                            else -> "Showing saved data; pull to refresh"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            NavHost(
                navController = navController,
                startDestination = "play-next",
                modifier = Modifier.weight(1f),
            ) {
            composable(
                "play-next",
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                PlayNextScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onOpenGame = { gameId -> navController.navigate("game/$gameId") },
                    onOpenSettings = { navController.navigate("settings") },
                )
            }
            composable(
                "picks",
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                PicksScreen(
                    picks = picks,
                    uiState = uiState,
                    viewModel = viewModel,
                    onOpenGame = { gameId -> navController.navigate("game/$gameId") },
                    onNavigateToPlayNext = { navController.navigate("play-next") { popUpTo(0) { inclusive = true } } },
                    hasProfile = onboardingCompleted,
                )
            }
            composable(
                "taste",
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                val profile = productState.user.profile
                val tasteModel by viewModel.tasteModel.collectAsState()
                TasteScreen(
                    profile = profile ?: ProductProfile(),
                    tasteModel = tasteModel,
                    hasProfile = onboardingCompleted,
                    isLoading = uiState.loading,
                    error = uiState.error,
                    showingStaleData = uiState.showingStaleData,
                    pendingSync = uiState.pendingSync,
                    onOpenGame = { gameId -> navController.navigate("game/$gameId") },
                    onOpenMap = { navController.navigate("taste-map") },
                    onRemovePick = { viewModel.removePick(it) },
                    onChangeSignal = { gameId, feedback ->
                        viewModel.applyDecisionFeedback(gameId, feedback)
                    },
                    onDeleteSignal = { gameId, source -> viewModel.deleteSignal(gameId, source) },
                    onRefresh = { viewModel.refreshRecommendations() },
                    isRefreshing = uiState.refreshing,
                )
            }
            composable(
                "search",
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                val searchState by viewModel.search.collectAsState()
                LaunchedEffect(Unit) { viewModel.resetSearch() }
                SearchScreen(
                    searchState = searchState,
                    platforms = platforms,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onFamilyChange = { viewModel.updateSearchFamily(it) },
                    onLoadMore = { viewModel.loadMoreSearchResults() },
                    onRetry = { viewModel.retrySearch() },
                    onOpenGame = { gameId -> navController.navigate("game/$gameId") },
                )
            }
            composable(
                "settings",
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    platforms = platforms,
                    platformsLoading = platformsUi.loading,
                    platformsError = platformsUi.error,
                    platformsStale = platformsUi.showingStaleData,
                    hasProfile = onboardingCompleted || productState.user.onboardingCompletedAt != null,
                    onResetTaste = {
                        viewModel.resetTaste()
                    },
                    onNavigateToPlayNext = { navController.navigate("play-next") { popUpTo(0) { inclusive = true } } },
                    onDeleteAccount = {
                        viewModel.deleteAccount()
                    },
                )
            }
            composable(
                route = "game/{gameId}",
                arguments = listOf(navArgument("gameId") { type = NavType.StringType }),
                enterTransition = { slideInVertically { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutVertically { it } },
            ) { entry ->
                val gameId = entry.arguments?.getString("gameId") ?: return@composable
                val dossier by viewModel.dossier.collectAsState()
                LaunchedEffect(gameId) { viewModel.loadGameRecommendation(gameId) }
                when (val current = dossier) {
                    is DossierUiState.Success -> if (current.entry.game.gameId == gameId) {
                        GameDossierScreen(
                            entry = current.entry,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            isPicked = current.entry.game.gameId in picks.map { it.game.gameId },
                        )
                    } else GameDossierLoading()
                    is DossierUiState.NotFound -> if (current.gameId == gameId) {
                        GameDossierNotFound(
                            onBack = {
                                navController.navigate("play-next") {
                                    popUpTo("play-next") { inclusive = true }
                                }
                            },
                        )
                    } else GameDossierLoading()
                    is DossierUiState.Error -> if (current.gameId == gameId) {
                        GameDossierError(
                            message = current.message,
                            onRetry = { viewModel.loadGameRecommendation(gameId, forceRefresh = true) },
                            onBack = {
                                navController.navigate("play-next") {
                                    popUpTo("play-next") { inclusive = true }
                                }
                            },
                        )
                    } else GameDossierLoading()
                    DossierUiState.Idle,
                    is DossierUiState.Loading -> GameDossierLoading()
                }
            }
            composable(
                "taste-map",
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                val tasteModel by viewModel.tasteModel.collectAsState()
                val nodes = remember(productState, picks, playNext, tasteModel) {
                    buildMapNodes(
                        state = productState,
                        picks = picks,
                        playNext = playNext,
                        tasteModel = tasteModel,
                    )
                }
                TasteMapVisualizerScreen(
                    nodes = nodes,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
}
