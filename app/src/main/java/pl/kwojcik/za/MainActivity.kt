package pl.kwojcik.za

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.kwojcik.za.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

object Screen {
    private const val PROFILE = "profile-screen"
    private const val GAME = "game-screen"
    private const val RESULTS = "results-screen"

    fun profilePath(): String {
        return PROFILE
    }

    fun toProfile(): String {
        return PROFILE
    }

    fun gamePath(): String {
        return "$GAME/{noOfColors}"
    }

    fun toGame(noOfColors: Int): String {
        return "$GAME/$noOfColors"
    }

    fun resultsPath(): String {
        return "$RESULTS/{result}"
    }

    fun toResults(result: Int): String {
        return "$RESULTS/$result"
    }
}

@Preview(showBackground = true)
@Composable
fun App() {
    val navController = rememberNavController()
    val fadeSpec = tween<Float>(easing = EaseIn, durationMillis = 1000)
    val slideSpec = tween<IntOffset>(easing = EaseIn, durationMillis = 1000)
    val enterTransition = fadeIn(fadeSpec) + slideInHorizontally(animationSpec = slideSpec) { it }
    val exitTransition = fadeOut(fadeSpec) + slideOutHorizontally(animationSpec = slideSpec) { -it }

    MyApplicationTheme {
        NavHost(navController = navController, startDestination = Screen.profilePath()) {
            composable(
                Screen.profilePath(),
                enterTransition = { enterTransition },
                exitTransition = { exitTransition }
            ) {
                ProfileScreen(navController = navController)
            }
            composable(Screen.gamePath(),
                arguments = listOf(navArgument("noOfColors") { type = NavType.IntType }),
                enterTransition = { enterTransition },
                exitTransition = { exitTransition }
            ) {
                GameScreen(navController = navController)
            }
            composable(Screen.resultsPath(),
                arguments = listOf(navArgument("result") {type = NavType.IntType}),
                enterTransition = { enterTransition },
                exitTransition = { exitTransition }
            ) {
                ResultScreen(navController = navController)
            }

        }
    }
}