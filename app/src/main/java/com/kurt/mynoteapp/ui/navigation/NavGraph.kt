package com.kurt.mynoteapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kurt.mynoteapp.ui.note.detail.NoteDetailRoute
import com.kurt.mynoteapp.ui.note.list.NoteListRoute

object Destinations {
    const val LIST = "list"
    const val DETAIL = "detail/{id}"
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destinations.LIST, modifier = modifier) {
        composable(Destinations.LIST) {
            NoteListRoute(
                onNoteClick = { id -> navController.navigate("detail/$id") },
                onAddNew = { navController.navigate("detail/0") }
            )
        }
        composable(
            route = Destinations.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            NoteDetailRoute(noteId = id, onBack = { navController.popBackStack() })
        }
    }
}


