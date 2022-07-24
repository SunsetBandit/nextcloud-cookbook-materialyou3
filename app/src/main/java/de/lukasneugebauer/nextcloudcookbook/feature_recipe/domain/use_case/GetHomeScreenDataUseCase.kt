package de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.use_case

import com.dropbox.android.external.store4.get
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.RecipeOfTheDay
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.HomeScreenDataResult
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.util.RecipeConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetHomeScreenDataUseCase @Inject constructor(
    private val categoriesStore: CategoriesStore,
    private val preferencesManager: PreferencesManager,
    private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
    private val recipePreviewsStore: RecipePreviewsStore,
    private val recipeStore: RecipeStore
) {

    suspend operator fun invoke(): List<HomeScreenDataResult> {
        val currentDate = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
        val homeScreenData = mutableListOf<HomeScreenDataResult>()
        var recipeOfTheDay = preferencesManager.preferencesFlow.map { it.recipeOfTheDay }.first()

        if (recipeOfTheDay.id == 0 || recipeOfTheDay.updatedAt.isBefore(currentDate)) {
            try {
                val newRecipeOfTheDayId = recipePreviewsStore.get(Unit).random().toRecipePreview().id
                recipeOfTheDay = RecipeOfTheDay(
                    id = newRecipeOfTheDayId,
                    updatedAt = LocalDateTime.now()
                )
                preferencesManager.updateRecipeOfTheDay(recipeOfTheDay)
            } catch (e: HttpException) {
                Timber.e(e.stackTraceToString())
            }
        }

        // FIXME: 25.12.21 Get different recipe of the day if api returns 404 error.
        //  E.g. after deleting recipe of the day.
        withContext(Dispatchers.IO) {
            try {
                val result = HomeScreenDataResult.Single(
                    R.string.home_recommendation,
                    recipeStore.get(recipeOfTheDay.id).toRecipe()
                )
                homeScreenData.add(result)
            } catch (e: Exception) {
                Timber.e(e.stackTraceToString())
            }
        }

        withContext(Dispatchers.IO) {
            try {
                categoriesStore.get(Unit)
                    .sortedByDescending { it.recipeCount }
                    .take(RecipeConstants.HOME_SCREEN_CATEGORIES)
                    .forEach { categoryDto ->
                        val recipePreviews = recipePreviewsByCategoryStore
                            .get(categoryDto.name)
                            .map { it.toRecipePreview() }
                        if (recipePreviews.isNotEmpty()) {
                            val result = HomeScreenDataResult.Row(
                                categoryDto.name,
                                recipePreviews
                            )
                            homeScreenData.add(result)
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e.stackTraceToString())
            }
        }

        return homeScreenData.toList()
    }
}
