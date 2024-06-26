package pl.kwojcik.za

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList
import okhttp3.internal.wait
import pl.kwojcik.za.app.Result
import pl.kwojcik.za.app.ResultRepository
import kotlin.collections.ArrayList
import kotlin.random.Random

class GameViewModel(
    private val resultRepository: ResultRepository
) : ViewModel() {
    val playerId = mutableLongStateOf(0L)
    val score = mutableIntStateOf(0)
    val lastScore = mutableIntStateOf(0)

    suspend fun save() {
        lastScore.intValue = score.intValue
        resultRepository.insert(Result(score.intValue, playerId.longValue))
        score.intValue = 0
    }
}

@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val noOfColors =
        navController.currentBackStackEntry?.arguments?.getInt("noOfColors") ?: 5
    viewModel.playerId.longValue =
        navController.currentBackStackEntry?.arguments?.getLong("playerId")
            ?: viewModel.playerId.longValue

    val coroutineScore = rememberCoroutineScope()

    MasterMindUI(
        score = viewModel.score,
        noOfColors = noOfColors,
        logout = { navController.navigate(Screen.toProfile()) },
        goToResult = {
            coroutineScore.launch {
                viewModel.save()
                navController.navigate(Screen.toResults(viewModel.lastScore.intValue))
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun MasterMindUI(
    score: MutableIntState = mutableIntStateOf(1),
    noOfColors: Int = 5,
    logout: () -> Unit = {},
    goToResult: () -> Unit = {}
) {
    val possibleColors = GameColor.entries.subList(0, noOfColors)
    val game = remember { Game(noOfColors) }

    val previousRows =
        remember { mutableStateListOf<GameRound>(GameRound.empty()) }
    val finished = remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
        Text(text = "Your score: ${score.intValue}")

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(previousRows.size, key = { it }) { rowIndex ->
                GameRow(
                    modifier = Modifier.animateItemPlacement(),
                    possibleColors = possibleColors,
                    enabled = (rowIndex + 1) == previousRows.size,
                    colors = previousRows[rowIndex].colors,
                    colorsStatus = previousRows[rowIndex].status,
                    whenAccepted = {
                        val gameRound = game.check(it)
                        previousRows[rowIndex] = gameRound
                        if (gameRound.isFinished()) {
                            finished.value = true
                        } else {
                            previousRows.add(GameRound.emptyWithColors(it))
                            score.intValue++
                        }
                    }
                )
            }
        }

        if (finished.value) {
            Button(onClick = {
                game.reset()
                previousRows.clear()
                finished.value = false

                goToResult()
            }) {
                Text(text = "High score table")
            }
        }

        Box(modifier = Modifier.fillMaxHeight(0.5f))

        Button(onClick = logout) {
            Text(text = "Logout")
        }
    }
}

@Composable
fun CircularBtn(enabled: Boolean, color: GameColor, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(50.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.color,
            contentColor = MaterialTheme.colorScheme.onBackground,
            disabledContainerColor = color.color
        )
    ) {}
}

@Composable
fun GameRow(
    modifier: Modifier,
    possibleColors: List<GameColor>,
    colors: List<GameColor>,
    colorsStatus: List<ColorCheckStatus> = listOf(
        ColorCheckStatus.WRONG_COLOR,
        ColorCheckStatus.WRONG_COLOR,
        ColorCheckStatus.WRONG_COLOR,
        ColorCheckStatus.WRONG_COLOR
    ),
    whenAccepted: (colors: List<GameColor>) -> Unit = {},
    enabled: Boolean = false
) {
    val visible = remember { mutableStateOf(false) }
    val enterAnimation = expandVertically()
    val buttonEnterAnimation = scaleIn()
    val buttonExitAnimation = scaleOut()

    LaunchedEffect(Unit) {
        visible.value = true
    }


    val currentRound = remember {
        mutableStateListOf(
            *colors.toTypedArray()
        )
    }

    fun isEnabled(): Boolean {
        return enabled && currentRound.toSet().size == 4
    }

    fun setNextColor(index: Int) {
        val color = currentRound[index]
        val indexInPossibleColors = possibleColors.indexOf(color)
        currentRound[index] = possibleColors[(indexInPossibleColors + 1) % possibleColors.size]
    }

    AnimatedVisibility(visible = visible.value, enter = enterAnimation) {
        Row {
            CircularBtn(color = currentRound[0],
                enabled = enabled,
                onClick = { setNextColor(0) })
            CircularBtn(color = currentRound[1],
                enabled = enabled,
                onClick = { setNextColor(1) })
            CircularBtn(color = currentRound[2],
                enabled = enabled,
                onClick = { setNextColor(2) })
            CircularBtn(color = currentRound[3],
                enabled = enabled,
                onClick = { setNextColor(3) })

            AnimatedVisibility(
                visible = isEnabled(),
                enter = buttonEnterAnimation,
                exit = buttonExitAnimation
            ) {
                IconButton(
                    onClick = { whenAccepted(currentRound.toImmutableList()) },
                    enabled = isEnabled(),
                    modifier = Modifier.clip(CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Blue,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "OK",
                        tint = Color.White,

                        )
                }
            }

            Column {
                Row {
                    SmallCircle(color = colorsStatus[0].color)
                    SmallCircle(color = colorsStatus[1].color)
                }
                Row {
                    SmallCircle(color = colorsStatus[2].color)
                    SmallCircle(color = colorsStatus[3].color)
                }
            }
        }
    }
}


@Composable
fun SmallCircle(color: Color) {
    val startAnimating = remember { mutableStateOf(false) }
    val animSpec = tween<Color>(durationMillis = 1000)
    val animatedColor = animateColorAsState(
        targetValue =
        if (startAnimating.value) color else Color.White,
        animationSpec = animSpec
    )

    LaunchedEffect(key1 = color) {
        startAnimating.value = true
    }

    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(20.dp)
            .clip(CircleShape)
            .background(animatedColor.value)
            .border(3.dp, MaterialTheme.colorScheme.outline, CircleShape)
    )
}


class Game(noOfColors: Int) {
    private var colors: List<GameColor>;
    private val availableColors = GameColor.entries
        .subList(0, noOfColors)

    init {
        colors = generateColorCombination(availableColors)
    }

    fun check(colors: List<GameColor>): GameRound {
        val list = colors.mapIndexed { index, color ->
            if (color == this.colors[index]) {
                ColorCheckStatus.OK
            } else if (this.colors.contains(color)) {
                ColorCheckStatus.WRONG_PLACE
            } else {
                ColorCheckStatus.WRONG_COLOR
            }
        }
        return GameRound(colors, list)
    }

    fun reset() {
        this.colors = this.generateColorCombination(this.availableColors)
    }

    private fun generateColorCombination(availableColors: List<GameColor>): List<GameColor> {
        val list = ArrayList<Int>()
        for (i in 0..3) {
            while (true) {
                val index = Random.nextInt(0, availableColors.size)
                if (!list.contains(index)) {
                    list.add(index)
                    break;
                }
            }
        }
        return list.map {
            availableColors[it]
        }
    }
}

enum class GameColor(val color: Color) {
    WHITE(Color.White),
    RED(Color.Red),
    GREEN(Color.Green),
    BLUE(Color.Blue),
    YELLOW(Color.Yellow),
    Cyan(Color.Cyan),
    Magenta(Color.Magenta),
    DarkGray(Color.DarkGray),
    LightGray(Color.LightGray),
    gw(Color.hsv(0.5f, 0.8f, 0.8f)),
    ;
}

enum class ColorCheckStatus(val color: Color) {
    WRONG_COLOR(Color.White),
    WRONG_PLACE(Color.Yellow),
    OK(Color.Red)
}

data class GameRound(
    val colors: List<GameColor>,
    val status: List<ColorCheckStatus>
) {
    fun isFinished(): Boolean {
        return status.none { it != ColorCheckStatus.OK }
    }

    companion object {
        fun empty(): GameRound {
            return GameRound(
                listOf(GameColor.WHITE, GameColor.WHITE, GameColor.WHITE, GameColor.WHITE),
                listOf(
                    ColorCheckStatus.WRONG_COLOR,
                    ColorCheckStatus.WRONG_COLOR,
                    ColorCheckStatus.WRONG_COLOR,
                    ColorCheckStatus.WRONG_COLOR
                )
            )
        }

        fun emptyWithColors(colors: List<GameColor>): GameRound {
            return GameRound(
                colors,
                listOf(
                    ColorCheckStatus.WRONG_COLOR,
                    ColorCheckStatus.WRONG_COLOR,
                    ColorCheckStatus.WRONG_COLOR,
                    ColorCheckStatus.WRONG_COLOR
                )
            )
        }
    }
}

