package pl.kwojcik.za

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage

fun isValidEmail(value: String): Result<Unit> {
    if (value.isEmpty()) {
        return Result.failure(IllegalArgumentException("Can not be empty"))
    }
    val separatorIndex = value.indexOf("@")
    if (separatorIndex < 1 || (separatorIndex + 1) >= value.length) {
        return Result.failure(IllegalArgumentException("Must be valid email"))
    }
    return Result.success(Unit)
}


@Composable
fun ProfileScreen(navController: NavController) {
    ProfileView(goToNextScreen = {
        navController.navigate(Screen.toGame(it))
    })
}

@Composable
fun ProfileView(goToNextScreen: (noOfColors: Int) -> Unit = {}) {
    val name = rememberSaveable { mutableStateOf("") }
    val email = rememberSaveable { mutableStateOf("") }
    val colorNO = rememberSaveable { mutableStateOf("5") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MasterAnd",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        ProfileImageWithPicker()

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextFieldWithError(
            value = name,
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
            value = email,
            validationFn = {isValidEmail(it)},
            label = "Enter name",
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
            onClick = { goToNextScreen( colorNO.value.toInt()) }
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
    val internalValue = rememberSaveable{ mutableStateOf(value.component1()) }

    val onValueChange: (String) -> Unit = { newValue: String ->
        internalValue.value = newValue
        validationFn(newValue)
            .onSuccess { value.value = newValue; error.value = null }
            .onFailure { error.value = it }
    }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = internalValue.value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = error.value != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = { error.value?.message }
    )
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

        AsyncImage(
            model = {
                if (profileImageUri.value != null) {
                    print(profileImageUri.value)
                    profileImageUri.value
                } else
                    androidx.core.R.drawable.ic_call_answer
            },
            contentDescription = "Profile image",
            modifier = Modifier
                .width(200.dp)
                .height(150.dp),
            contentScale = ContentScale.Fit,
        )
    }
}
