package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue

@Composable
fun BottomBar(
    allScreens: List<NextcloudCookbookScreen>,
    navController: NavHostController,
    currentScreen: NextcloudCookbookScreen
) {
    BottomNavigation(
        backgroundColor = NcBlue,
        elevation = 4.dp
    ) {
        allScreens
            .filter { it.bottomBar }
            .map {
                BottomNavigationItem(
                    selected = currentScreen == it,
                    onClick = { navController.navigate(it.name) },
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.name
                        )
                    },
                    label = { Text(text = it.name) },
                    selectedContentColor = Color.White
                )
            }
    }
}