package com.carlosarancibia.playfit.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PlayfitApiService {

    @POST("api/recommendations/today")
    suspend fun getTodayRecommendations(): TodayResponse

    @GET("api/recommendations/picks")
    suspend fun getPicks(): List<RankedSeedGameDto>

    @GET("api/recommendations/game/{gameId}")
    suspend fun getGameRecommendation(@Path("gameId") gameId: String): GameResponse

    @POST("api/recommendations/similar")
    suspend fun getSimilarRecommendations(@Body request: SimilarGamesRequest): SimilarGamesResponse

    @GET("api/platforms")
    suspend fun getPlatforms(): PlatformsResponse

    @POST("api/recommendations/profile")
    suspend fun buildProfile(@Body request: ProfileBuildRequest): ProfileBuildResponse

    @GET("api/profile")
    suspend fun getProfile(@Query("device_id") deviceId: String? = null): ProfileStateResponse

    @POST("api/profile")
    suspend fun saveProfile(@Body request: ProfileSaveRequest)

    @PATCH("api/profile/games/{gameId}")
    suspend fun upsertGameState(
        @Path("gameId") gameId: String,
        @Body state: GameStateRequest,
    )

    @DELETE("api/profile/games/{gameId}")
    suspend fun deleteGameState(@Path("gameId") gameId: String)

    @DELETE("api/profile")
    suspend fun deleteProfile()

    @GET("api/games")
    suspend fun searchGames(
        @Query("q") query: String? = null,
        @Query("platform") platform: String? = null,
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
    ): GamesResponse

    @POST("api/games/batch")
    suspend fun batchGames(@Body request: BatchGamesRequest): BatchGamesResponse
}

// DTO responses
@Serializable
data class TodayResponse(
    val primary: RankedSeedGameDto? = null,
    val alternatives: List<RankedSeedGameDto> = emptyList(),
    @SerialName("savedPickIds")
    val savedPickIds: List<String> = emptyList(),
    @SerialName("stateVersion")
    val stateVersion: String? = null,
)

@Serializable
data class GameResponse(
    val entry: RankedSeedGameDto? = null,
    @SerialName("stateVersion")
    val stateVersion: String? = null,
)

@Serializable
data class ProfileBuildRequest(
    val onboarding: OnboardingDraftDto,
    val gameStates: Map<String, GameStateDto>,
)

@Serializable
data class OnboardingDraftDto(
    val step: String,
    val platforms: List<PlatformSelectionDto>,
    val likedGameIds: List<String>,
    val dislikedGameIds: List<String>,
)

@Serializable
data class ProfileBuildResponse(
    val profile: ProfileDto,
)

@Serializable
data class ProfileStateResponse(
    val state: ProfilePersistedState? = null,
)

@Serializable
data class ProfilePersistedState(
    @SerialName("game_states")
    val gameStates: Map<String, GameStateDto>? = null,
    val profile: ProfileDto? = null,
    val onboarding: PersistedOnboardingDto? = null,
)

@Serializable
data class PlatformSelectionDto(
    val platformId: String,
    val status: String,
)

@Serializable
data class PersistedOnboardingDto(
    val step: String,
    val platforms: List<PlatformSelectionDto>,
    val likedGameIds: List<String>,
    val dislikedGameIds: List<String>,
    val onboardingCompletedAt: String? = null,
)

@Serializable
data class ProfileDto(
    val summary: String = "",
    @SerialName("likedGenres")
    val likedGenres: List<String> = emptyList(),
    @SerialName("avoidedGenres")
    val avoidedGenres: List<String> = emptyList(),
    @SerialName("likedTags")
    val likedTags: Map<String, Int> = emptyMap(),
    @SerialName("dislikedTags")
    val dislikedTags: Map<String, Int> = emptyMap(),
    @SerialName("ratedCount")
    val ratedCount: Int = 0,
    val signals: List<SignalDto> = emptyList(),
)

@Serializable
data class SignalDto(
    val id: String = "",
    val tone: String = "",
    val label: String = "",
    @SerialName("reason")
    val description: String = "",
)

@Serializable
data class RankedSeedGameDto(
    val game: SeedGameDto,
    @SerialName("affinityScore")
    val affinityScore: Double = 0.0,
    @SerialName("riskScore")
    val riskScore: Double = 0.0,
    val confidence: String = "Medium",
    @SerialName("fitReasons")
    val fitReasons: List<String> = emptyList(),
    @SerialName("cautionReasons")
    val cautionReasons: List<String> = emptyList(),
    @SerialName("platformAvailability")
    val platformAvailability: String = "Unknown",
    @SerialName("accessStatus")
    val accessStatus: String = "Unknown",
    @SerialName("inPlayfitPicks")
    val inPlayfitPicks: Boolean = false,
    @SerialName("similarGames")
    val similarGames: List<SimilarGameDto> = emptyList(),
)

@Serializable
data class SeedGameDto(
    @SerialName("gameId")
    val gameId: String = "",
    val title: String = "",
    val aliases: List<String> = emptyList(),
    val series: String? = null,
    val source: String? = null,
    @SerialName("primaryGenre")
    val primaryGenre: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("coverPath")
    val coverPath: String? = null,
    @SerialName("externalCoverUrl")
    val externalCoverUrl: String? = null,
    @SerialName("releaseYear")
    val releaseYear: String? = null,
    @SerialName("availablePlatformIds")
    val availablePlatformIds: List<String> = emptyList(),
    @SerialName("availablePlatformNames")
    val availablePlatformNames: List<String> = emptyList(),
    @SerialName("releaseState")
    val releaseState: String = "Released",
)

@Serializable
data class SimilarGameDto(
    @SerialName("gameId")
    val gameId: String = "",
    val title: String = "",
    val score: Double = 0.0,
)

@Serializable
data class GameStateDto(
    @SerialName("gameId")
    val gameId: String? = null,
    val title: String = "",
    val status: String? = null,
    val rating: Double? = null,
    @SerialName("inPlayfitPicks")
    val inPlayfitPicks: Boolean? = null,
    @SerialName("inBacklog")
    val inBacklog: Boolean? = null,
    @SerialName("inWishlist")
    val inWishlist: Boolean? = null,
    val excluded: Boolean? = null,
    val source: String = "manual",
    val createdAt: String = "",
    val updatedAt: String = "",
)

@Serializable
data class ProfileSaveRequest(
    val deviceId: String? = null,
    val gameStates: Map<String, GameStateDto>,
    val profile: ProfileDto?,
    val onboarding: PersistedOnboardingDto,
)

@Serializable
data class GameStateRequest(
    val status: String? = null,
    val rating: Double? = null,
    @SerialName("inPlayfitPicks")
    val inPlayfitPicks: Boolean? = null,
    @SerialName("inBacklog")
    val inBacklog: Boolean? = null,
    @SerialName("inWishlist")
    val inWishlist: Boolean? = null,
    val excluded: Boolean? = null,
    val source: String? = null,
)

@Serializable
data class GamesResponse(
    val games: List<SeedGameDto>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
)

@Serializable
data class BatchGamesRequest(
    val gameIds: List<String>,
)

@Serializable
data class BatchGamesResponse(
    val games: List<SeedGameDto>,
)

@Serializable
data class DeleteGameStateOperation(
    val gameId: String,
)

@Serializable
data class SimilarGamesRequest(
    val gameId: String,
)

@Serializable
data class SimilarGamesResponse(
    val similar: List<SimilarGameDto> = emptyList(),
)

@Serializable
data class PlatformDto(
    @SerialName("platformId")
    val platformId: String? = null,
    @SerialName("displayName")
    val displayName: String? = null,
    val id: String? = null,
    val name: String? = null,
    val slug: String = "",
    val family: String? = null,
    val kind: String? = null,
    @SerialName("activeStatus")
    val activeStatus: String? = null,
    @SerialName("sortOrder")
    val sortOrder: Int? = null,
)

@Serializable
data class PlatformsResponse(
    val platforms: List<PlatformDto> = emptyList(),
)
