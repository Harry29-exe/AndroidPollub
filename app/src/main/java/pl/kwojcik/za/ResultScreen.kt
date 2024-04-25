package pl.kwojcik.za

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ResultScreen(navController: NavController) {
    val result = navController.currentBackStackEntry?.savedStateHandle?.get<Int>("result") ?: 0

    ResultView(result = result,
        playAgain = {
            navController.popBackStack()
        },
        logout = {
            navController.popBackStack(Screen.profilePath(), inclusive = false)
        }
        )
}

@Composable
fun ResultView(result: Int,
               playAgain: () -> Unit = {},
               logout: () -> Unit = {}
               ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight()) {
        Text(text = "Results",
        style = MaterialTheme.typography.displayLarge)

        Text(text = "Recent score: $result",
            style = MaterialTheme.typography.displayMedium)

        Button(onClick = playAgain) {
            Text(text = "Restart game")
        }
        Button(onClick = logout) {
            Text(text = "Logout")
        }
    }

}