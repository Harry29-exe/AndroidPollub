package pl.kwojcik.za

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pl.kwojcik.za.app.Player
import pl.kwojcik.za.app.PlayerRepository

class ProfileViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {
    var playerId = mutableStateOf(0L)
    val name = mutableStateOf("")
    val email = mutableStateOf("")

    suspend fun savePlayer() {
        playerId.value = playerRepository.insert(Player(name.value, email.value))
    }
}

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()

    ProfileView(
        viewModel = viewModel,
        goToNextScreen = { noOfColors ->
            coroutineScope.launch {
                viewModel.savePlayer()
                Log.d("ProfileView", "playerId: ${viewModel.playerId.value}")
                navController.navigate(Screen.toGame(viewModel.playerId.value, noOfColors))
            }
        })
}

@Composable
fun ProfileView(
    viewModel: ProfileViewModel,
    goToNextScreen: (noOfColors: Int) -> Unit = { _ -> }
) {


    val colorNO = rememberSaveable { mutableStateOf("5") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedTitle()
        ProfileImageWithPicker()

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextFieldWithError(
            value = viewModel.name,
            validationFn = {
                if (it.isNotEmpty()) {
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalArgumentException("Can not be empty"))
                }
            },
            label = "Enter name"
        )

        OutlinedTextFieldWithError(
            value = viewModel.email,
            validationFn = { isValidEmail(it) },
            label = "Enter email",
        )

        OutlinedTextFieldWithError(
            value = colorNO,
            label = "Enter number of colors",
            validationFn = {
                Result.runCatching {
                    val value = Integer.parseInt(it)
                    if (value < 5 || value > 10) {
                        throw IllegalArgumentException("value must be between 5 and 10")
                    }
                }
            },
            keyboardType = KeyboardType.Decimal
        )

        Button(
            modifier = Modifier.fillMaxWidth(1f),
            onClick = {
                goToNextScreen(colorNO.value.toInt())
            }
        ) {
            Text(text = "Next")
        }
    }
}


@Composable
fun OutlinedTextFieldWithError(
    value: MutableState<String>,
    validationFn: (text: String) -> Result<Unit> = { Result.success(Unit) },
    keyboardType: KeyboardType = KeyboardType.Text,
    label: String = ""
) {
    val error = rememberSaveable { mutableStateOf<Throwable?>(null) }
    val internalValue = rememberSaveable { mutableStateOf(value.component1()) }

    val onValueChange: (String) -> Unit = { newValue: String ->
        internalValue.value = newValue
        validationFn(newValue)
            .onSuccess { value.value = newValue; error.value = null }
            .onFailure { error.value = it }
    }
    Row {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .onFocusChanged {
                    if (it.isFocused) {
                        onValueChange(internalValue.value)
                    }
                },
            value = internalValue.value,
            onValueChange = onValueChange,
            trailingIcon = { if (error.value != null) {Icon(Icons.Filled.Info, "Pick") }},
            label = { Text(label) },
            singleLine = true,
            isError = error.value != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            supportingText = { error.value?.message }
        )
    }
}


@Preview
@Composable
fun ProfileImageWithPicker() {
    val profileImageUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { selectedUri ->
            if (selectedUri != null) {
                profileImageUri.value = selectedUri
            }
        })
    Column() {

        IconButton(
            modifier = Modifier
                .align(Alignment.End)
                .zIndex(1000f),
            onClick = {
                imagePicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
        ) {
            Icon(Icons.Filled.Info, "Pick")
        }

        if (profileImageUri.value == null) {
            Image(
                modifier = Modifier.width(150.dp).height(150.dp),
                painter = painterResource(id = R.drawable.baseline_question_mark_24),
                contentDescription = "Profile image",
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = profileImageUri.value,
                contentDescription = "Profile image",
                modifier = Modifier
                    .width(200.dp)
                    .height(150.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
fun AnimatedTitle() {
    val font = remember { Animatable(initialValue = 24f) }
    val animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = 1000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    )

    LaunchedEffect(true) {
        font.animateTo(35f, animationSpec = animationSpec)
    }

    Column(
        modifier = Modifier
            .height(90.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            text = "MasterAnd",
            style = TextStyle(fontSize = font.value.sp),
            modifier = Modifier.padding(bottom = 48.dp)
        )
    }
}

private fun isValidEmail(value: String): Result<Unit> {
    if (value.isEmpty()) {
        return Result.failure(IllegalArgumentException("Can not be empty"))
    }
    val separatorIndex = value.indexOf("@")
    if (separatorIndex < 1 || (separatorIndex + 1) >= value.length) {
        return Result.failure(IllegalArgumentException("Must be valid email"))
    }
    return Result.success(Unit)
}
