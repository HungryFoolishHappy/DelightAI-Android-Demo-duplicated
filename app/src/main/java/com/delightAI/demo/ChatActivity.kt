package com.delightAI.demo

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delightAI.demo.ui.theme.DelightAIDemoTheme
import com.delightai.api.ChatBuilder
import com.delightai.api.PollingBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChatActivity : ComponentActivity() {
    var message = mutableStateListOf<Message?>(*listOf(Message(isUser = false, text = "Hello")).toTypedArray())

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras

        enableEdgeToEdge()
        setContent {
            ChatActivityUI(onBackClick = {
                this.finish()
            }, _messages = message, extras)
        }
    }
}

@Composable
fun ChatActivityUI(onBackClick: () -> Unit, _messages: SnapshotStateList<Message?>, extras: Bundle?) {
    var sending by rememberSaveable { mutableStateOf("") }
    var editText by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    DelightAIDemoTheme {
        ChatActivityUIPage(
            sending,
            onEditText = { newText ->
                editText = newText
            },
            onTextChange = { newText ->
                sending = newText
            },
            onMessagesAdd = { newMessage: Message ->
                _messages.add(newMessage)
                scope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            },
            onMessageChange = { message_id: String, text: String ->
                val messageToUpdateIndex = _messages.indexOfFirst { it?.id ?: it?.id == message_id }

                val updatedMessage = _messages.get(messageToUpdateIndex)?.copy(text = (_messages.get(messageToUpdateIndex)?.text?.replace("waiting for the response ...", "")
                    ?: "") + text)
                _messages[messageToUpdateIndex] = updatedMessage
                scope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            },
            _messages=_messages,
            onBackClick = onBackClick,
            extras = extras,
            scrollState = scrollState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatActivityUIPage(
    sending: String,
    onEditText: (String) -> Unit,
    onTextChange: (String) -> Unit,
    _messages: SnapshotStateList<Message?>,
    onMessagesAdd: (Message) -> Unit,
    onMessageChange: (String, String) -> Unit,
    onBackClick: () -> Unit,
    extras: Bundle?,
    scrollState: ScrollState,
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Delight Demo") },
                navigationIcon = {
                    IconButton(onClick = {
                        onBackClick()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomTextField(sending, onEditText, onTextChange, onMessagesAdd, onMessageChange, extras)
        },
    ) { innerPadding ->
        ChatScreenComponent(
            name = "Android",
            modifier = Modifier.padding(innerPadding),
            _messages,
            onMessagesAdd,
            scrollState
        )
    }
}

@Composable
fun BottomTextField(
    sending: String,
    onEditText: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onMessagesAdd: (Message) -> Unit,
    onMessageChange: (String, String) -> Unit,
    extras: Bundle?) {
    val context = LocalContext.current
    val view = LocalView.current
    val webhook_id = extras?.getString("webhook_id") ?: "a4b404d4-5f9a-4617-a5a4-8ea338352eb4"
    val user_id = extras?.getString("user_id") ?: "Wi-Android-9937-491d-aefd-02e6b89ff0aa"
    val username = extras?.getString("username") ?:"Wi-Android-9937-491d-aefd-02e6b89ff0aa"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = sending,
            onValueChange = onTextChange,
            label = { Text(
                fontSize = 18.sp,
                text = "Ask me anything ..."
            )},
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(end = 10.dp),
        )
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                if (sending == "") {
                    return@Button
                }
                val message = Message(isUser = true, text = sending)
                onMessagesAdd(message)

                val inputMethodManager = (context as? ComponentActivity)?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                GlobalScope.launch {
                    val build = ChatBuilder().build()
                    val pollingBuild = PollingBuilder().build()
                    val waitingResponseMessage = Message(isUser = false, text = "waiting for the response ...")
                    val response = build.send(text = sending, webhook_id, user_id, username, message_id = waitingResponseMessage.id)

                    onMessagesAdd(waitingResponseMessage)
                    onTextChange("")
                    onEditText("")
                    val tex = response?.text
                    if (tex != null) {
                        withContext(Dispatchers.IO) {
                            var loop = 30
                            while(loop > 0) {
                                val pollingResponse = pollingBuild.polling(webhook_id = response.poll)
                                if (pollingResponse?.completed == true) {
                                    val text = pollingResponse.text
                                    if (text != null) {
                                        onMessageChange(waitingResponseMessage.id, text)
                                        onEditText(text)
                                        break
                                    }
                                } else if (pollingResponse?.new_tokens != "") {
                                    pollingResponse?.new_tokens?.let {
                                        onMessageChange(waitingResponseMessage.id,
                                            it
                                        )
                                        onEditText(it)
                                    }
                                }
                                Thread.sleep(1_000)
                                loop--
                            }
                        }
                    }
                }
            }
        ) {
            Icon(Icons.Filled.Send, contentDescription = "Back")
        }
    }
}

@Composable
fun ChatScreenComponent(name: String, modifier: Modifier = Modifier, messages: SnapshotStateList<Message?>, onMessagesAdd: (Message) -> Unit, scrollState: ScrollState) {
    ChatScreen(modifier, messages, onMessagesAdd, scrollState)
}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//}