package de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.repository

import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.BaseRepository
import com.dropbox.android.external.store4.get
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApi
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.SimpleResource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
    private val recipePreviewsStore: RecipePreviewsStore,
    private val recipeStore: RecipeStore
) : RecipeRepository, BaseRepository() {

    override suspend fun getRecipePreviews(): Flow<StoreResponse<List<RecipePreviewDto>>> {
        return withContext(Dispatchers.IO) {
            recipePreviewsStore.stream(StoreRequest.cached(key = Unit, refresh = false))
        }
    }

    override suspend fun getRecipePreviewsByCategory(categoryName: String): Flow<StoreResponse<List<RecipePreviewDto>>> {
        return withContext(Dispatchers.IO) {
            recipePreviewsByCategoryStore.stream(
                StoreRequest.cached(
                    key = categoryName,
                    refresh = false
                )
            )
        }
    }

    override suspend fun getRecipeFlow(id: Int): Flow<StoreResponse<RecipeDto>> {
        return withContext(Dispatchers.IO) {
            recipeStore.stream(StoreRequest.cached(key = id, refresh = false))
        }
    }

    override suspend fun getRecipe(id: Int): RecipeDto {
        return withContext(Dispatchers.IO) {
            recipeStore.get(id)
        }
    }

    override suspend fun createRecipe(recipe: RecipeDto): Resource<Int> {
        return withContext(Dispatchers.IO) {
            val api = apiProvider.getNcCookbookApi()
                ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

            try {
                val id = api.createRecipe(recipe = recipe)
                Resource.Success(data = id)
            } catch (e: Exception) {
                handleResponseError(e.cause)
            }
        }
    }

    override suspend fun updateRecipe(recipe: RecipeDto): SimpleResource {
        return withContext(Dispatchers.IO) {
            val api = apiProvider.getNcCookbookApi()
                ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

            try {
                api.updateRecipe(id = recipe.id, recipe = recipe)
                // TODO: Clear or update other stores too
                recipeStore.clear(recipe.id)
                Resource.Success(Unit)
            } catch (e: Exception) {
                handleResponseError(e.cause)
            }
        }
    }

    override suspend fun deleteRecipe(id: Int, categoryName: String): SimpleResource {
        return withContext(Dispatchers.IO) {
            val api = apiProvider.getNcCookbookApi()
                ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

            when (val response = api.deleteRecipe(id)) {
                is NetworkResponse.Success -> {
                    recipePreviewsByCategoryStore.clear(categoryName)
                    recipePreviewsStore.clear(Unit)
                    recipeStore.clear(id)
                    Resource.Success(Unit)
                }
                is NetworkResponse.Error -> handleResponseError(response.error)
            }
        }
    }
}
