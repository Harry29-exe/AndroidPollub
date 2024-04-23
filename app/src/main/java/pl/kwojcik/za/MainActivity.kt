package pl.kwojcik.za

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.kwojcik.za.ui.theme.MyApplicationTheme

enum class Page {
    PROFILE,
    GAME
}

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


@Preview(showBackground = true)
@Composable
fun App() {
    val page =  rememberSaveable{ mutableStateOf(Page.PROFILE) }

    MyApplicationTheme {
        if (page.value == Page.PROFILE) {
            ProfileScreen({page.value = Page.GAME})
        } else if (page.value == Page.GAME) {
            MasterMindUI(noOfColors = 5)
        }
    }
}