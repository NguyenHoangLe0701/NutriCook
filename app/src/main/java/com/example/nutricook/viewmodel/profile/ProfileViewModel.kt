package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.data.storage.NutritionProfileStore
import com.example.nutricook.data.nutrition.IMealRepository
import com.example.nutricook.model.nutrition.BodyMetrics
import com.example.nutricook.model.nutrition.DailyTotals
import com.example.nutricook.model.nutrition.Goal
import com.example.nutricook.model.nutrition.MacroPrefs
import com.example.nutricook.model.nutrition.NutritionProfile
import com.example.nutricook.model.nutrition.NutritionTargets
import com.example.nutricook.model.nutrition.recalculate
import com.example.nutricook.model.nutrition.withInputs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val profileStore: NutritionProfileStore,
    private val mealRepo: IMealRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _ui.asStateFlow()

    data class ProfileTargets(
        val calories: Int = 0,
        val proteinG: Double = 0.0,
        val fatG: Double = 0.0,
        val carbG: Double = 0.0
    )

    private val _targets = MutableStateFlow(ProfileTargets())
    val targets: StateFlow<ProfileTargets> = _targets.asStateFlow()

    val todayTotals: StateFlow<DailyTotals> =
        mealRepo.observeTodayTotals()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                DailyTotals(date = "", caloriesIn = 0, proteinG = 0.0, fatG = 0.0, carbG = 0.0)
            )

    val last7Days: StateFlow<List<DailyTotals>> =
        mealRepo.observeLastNDaysTotals(7)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    init {
        repo.myProfileFlow()
            .distinctUntilChanged()
            .onEach { p -> _ui.update { it.copy(loading = false, profile = p) } }
            .catch { e -> _ui.update { it.copy(message = e.message ?: "Lỗi luồng hồ sơ") } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            if (_ui.value.profile == null) refreshOnce()
        }

        profileStore.flow()
            .onEach { profile ->
                val recalced = safeRecalc(profile)
                _targets.value = recalced.targets.toVmTargets()
            }
            .catch { e ->
                _ui.update { it.copy(message = e.message ?: "Không đọc được dữ liệu My Fat") }
            }
            .launchIn(viewModelScope)
    }

    fun refreshOnce() = viewModelScope.launch {
        _ui.update { it.copy(loading = true) }
        runCatching { repo.getMyProfile() }
            .onSuccess { p -> _ui.update { it.copy(loading = false, profile = p) } }
            .onFailure { e ->
                _ui.update {
                    it.copy(loading = false, message = e.message ?: "Không tải được hồ sơ")
                }
            }
    }

    fun updateProfile(fullName: String?, bio: String?) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }
        runCatching {
            repo.updateProfile(
                fullName = fullName,
                email = null,
                dayOfBirth = null,
                gender = null,
                bio = bio
            )
        }
            .onSuccess {
                refreshOnce()
                _ui.update { it.copy(updating = false, message = "Đã lưu hồ sơ") }
            }
            .onFailure { e ->
                _ui.update {
                    it.copy(updating = false, message = e.message ?: "Không thể lưu hồ sơ")
                }
            }
    }

    fun updateAvatar(localUri: String) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }
        runCatching { repo.updateAvatar(localUri) }
            .onSuccess { url ->
                _ui.update { st ->
                    val newProfile = st.profile?.copy(
                        user = st.profile.user.copy(avatarUrl = url)
                    )
                    st.copy(
                        updating = false,
                        profile = newProfile ?: st.profile,
                        message = "Đã cập nhật ảnh đại diện"
                    )
                }
                refreshOnce()
            }
            .onFailure { e ->
                _ui.update {
                    it.copy(updating = false, message = e.message ?: "Không thể cập nhật ảnh")
                }
            }
    }

    fun setFollow(targetUid: String, follow: Boolean) = viewModelScope.launch {
        runCatching { repo.setFollow(targetUid, follow) }
            .onFailure { e ->
                _ui.update { it.copy(message = e.message ?: "Lỗi thao tác theo dõi") }
            }
    }

    suspend fun isFollowing(targetUid: String): Boolean =
        runCatching { repo.isFollowing(targetUid) }.getOrElse { false }

    fun changePassword(oldPassword: String, newPassword: String) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }
        runCatching { repo.changePassword(oldPassword, newPassword) }
            .onSuccess { _ui.update { it.copy(updating = false, message = "Đã đổi mật khẩu") } }
            .onFailure { e ->
                _ui.update {
                    it.copy(updating = false, message = e.message ?: "Đổi mật khẩu thất bại")
                }
            }
    }

    fun consumeMessage() {
        _ui.update { it.copy(message = null) }
    }

    // ---------- My Fat ----------
    fun updateMetrics(newMetrics: BodyMetrics) = viewModelScope.launch {
        profileStore.update { old -> old.withInputs(metrics = newMetrics).recalculate() }
    }

    fun updateGoal(goal: Goal) = viewModelScope.launch {
        profileStore.update { old -> old.withInputs(goal = goal).recalculate() }
    }

    fun updateCalorieDelta(deltaKcalPerDay: Int) = viewModelScope.launch {
        profileStore.update { old -> old.withInputs(deltaKcalPerDay = deltaKcalPerDay).recalculate() }
    }

    fun updateMacroPrefs(prefs: MacroPrefs) = viewModelScope.launch {
        profileStore.update { old -> old.withInputs(prefs = prefs).recalculate() }
    }

    /** Đặt Goal mới và GHI ĐÈ chỉ số target thủ công (kcal/pro/fat/carb). */
    fun setGoalWithManualTargets(
        goal: Goal,
        calories: Int,
        proteinG: Double,
        fatG: Double,
        carbG: Double
    ) = viewModelScope.launch {
        profileStore.update { old ->
            val rec = old.withInputs(goal = goal).recalculate()
            rec.copy(
                targets = NutritionTargets(
                    caloriesTarget = calories,
                    proteinG = proteinG,
                    fatG = fatG,
                    carbG = carbG
                )
            )
        }
    }

    fun resetMyFat() = viewModelScope.launch {
        profileStore.set(NutritionProfile().recalculate())
    }

    // ---------- Helpers ----------
    private fun safeRecalc(p: NutritionProfile): NutritionProfile = try {
        p.recalculate()
    } catch (_: Throwable) {
        NutritionProfile().recalculate()
    }

    private fun NutritionTargets.toVmTargets(): ProfileTargets =
        ProfileTargets(
            calories = caloriesTarget,
            proteinG = proteinG,
            fatG = fatG,
            carbG = carbG
        )
}
