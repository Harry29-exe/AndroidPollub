package pl.kwojcik.za

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pl.kwojcik.za.app.PlayerResult
import pl.kwojcik.za.app.PlayerResultRepository

class ResultViewModel(
    private val playerResultRepository: PlayerResultRepository
) : ViewModel() {
    val results = mutableStateOf(listOf<PlayerResult>())
    val score = mutableIntStateOf(0)

    suspend fun load() {
        val result = playerResultRepository.findRecent()
        results.value = result
    }
}

@Composable
fun ResultScreen(
    navController: NavController,
    modelView: ResultViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    modelView.score.intValue =
        navController.currentBackStackEntry?.arguments?.getInt("result")
            ?: modelView.score.intValue

    LaunchedEffect(key1 = modelView.results.value.size) {
        modelView.load()
    }

    ResultView(result = modelView.score,
        resultList = modelView.results,
        playAgain = {
            navController.popBackStack()
        },
        logout = {
            navController.popBackStack(Screen.profilePath(), inclusive = false)
        }
    )
}

@Composable
fun ResultView(
    resultList: MutableState<List<PlayerResult>>,
    result: MutableIntState,
    playAgain: () -> Unit = {},
    logout: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(
            text = "Results",
            style = MaterialTheme.typography.displayLarge
        )

        Text(
            text = "Recent score: ${result.intValue}",
            style = MaterialTheme.typography.displayMedium
        )

        LazyColumn {
            items(resultList.value.size) { index ->
                Row(horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .drawBehind {

                            val strokeWidth = 2 * density
                            val y = size.height - strokeWidth / 2

                            drawLine(
                                Color.LightGray,
                                Offset(0f, y),
                                Offset(size.width, y),
                                strokeWidth
                            )
                        }
                ) {
                    Text(
                        text = resultList.value[index].playerName,
                        style = MaterialTheme.typography.displayMedium)

                    Spacer(Modifier.weight(1f, true))

                    Text(text = "${resultList.value[index].score}",
                            style = MaterialTheme.typography.displayMedium)
                }
            }
        }

        Button(onClick = playAgain) {
            Text(text = "Restart game")
        }
        Button(onClick = logout) {
            Text(text = "Logout")
        }


    }

}