package com.delightAI.demo

import android.app.Activity
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.platform.LocalView
import com.delightai.api.ChatBuilder
import com.delightai.api.PollingBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChatUIA(modifier: Modifier = Modifier, messages: ArrayList<Message>, onMessagesAdd: (Message) -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current

    // Chat UI Area
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start) {
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (message in messages) {
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                if (message.isUser) {
                    Spacer(modifier = Modifier.padding(start = 25.dp))
                } else {
                    Text(
                        "AI",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Start,
                        color = Color.White,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                color = Color.Black,
                                shape = CircleShape
                            )
                            .border(
                                width = 5.dp,
                                color = Color.Black,
                                shape = CircleShape
                            )
                            .padding(10.dp)
                    )
                }
                Text(
                    message.text,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Start,
                    color = if (message.isUser) Color.White else Color.Black,
                    modifier = Modifier
                        .padding(10.dp)
                        .background(
                            color = if (message.isUser) Color.Blue else Color.LightGray,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = if (message.isUser) Color.Blue else Color.LightGray,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                )
            }
        }

    }
}