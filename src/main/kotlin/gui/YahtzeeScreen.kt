package gui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domain.models.ScoreCategory
import java.lang.Exception

// Main UI component that renders different screens based on the current state
@Composable
fun yahtzeeAppScreen(viewModel: YahtzeeViewModel) {
    val state by viewModel.uiState.collectAsState()

    // Render different screens based on the current screen state
    when (state.currentScreen) {
        AppScreen.SETUP -> setupScreen(state, { name -> viewModel.addPlayer(name) }, { viewModel.startGame() })
        AppScreen.GAME ->
            gameScreen(
                state,
                { viewModel.undoMove() },
                { viewModel.endGame() },
                { diceInput, category -> viewModel.submitMove(diceInput, category) },
                { viewModel.showLeaderBoard() },
            )
        AppScreen.LEADERBOARD -> leaderBoardTable(state)
    }
}

// Composable function for the setup screen
@Composable
fun setupScreen(
    state: UIState,
    addPlayer: (String) -> Unit,
    startGame: () -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }

    val onAddPlayer = {
        addPlayer(nameInput)
        nameInput = ""
    }
    // UI layout for player registration and game start
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Player registration", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        Row {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Player Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAddPlayer() }),
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onAddPlayer,
                modifier = Modifier.height(56.dp),
            ) {
                Text("Add")
            }
        }

        if (state.errorText.isNotEmpty()) {
            Text(state.errorText, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text("Players:", fontWeight = FontWeight.Bold)
        state.pendingPlayers.values.forEach { Text("- $it") }

        Spacer(Modifier.height(24.dp))
        Button(onClick = { startGame() }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)) {
            Text("START GAME", fontWeight = FontWeight.Bold)
        }
    }
}

// Composable function for the main game screen, including score board and dice input
@Composable
fun gameScreen(
    state: UIState,
    undoMove: () -> Unit,
    endGame: () -> Unit,
    submitMove: (String, ScoreCategory) -> Unit,
    showLeaderBoard: () -> Unit,
) {
    var diceInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // HEADER
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val headerText = if (state.isGameOver) "GAME OVER" else "Current move: ${state.currentPlayerName}"
            Text(
                text = headerText,
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(30.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (state.errorText.isNotEmpty()) {
                Text(text = state.errorText, color = Color.Red, style = MaterialTheme.typography.subtitle1)
            }
        }
        Spacer(Modifier.height(8.dp))

        // TABLE + ASSIST
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.weight(0.7f).fillMaxHeight()) {
                if (state.scoreBoard.isNotEmpty()) {
                    scoreBoardTable(state, categorySelected = { cat ->
                        submitMove(diceInput, cat)
                        diceInput = ""
                    })
                }
            }

            Box(
                modifier = Modifier.weight(0.3f).fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                assistCommentator(state)
            }
        }

        // Spacer(Modifier.height(16.dp))

        // FOOTER + INPUT
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!state.isGameOver) {
                // Left button
                Button(
                    onClick = { undoMove() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFCDD2)),
                ) {
                    Text("Undo move")
                }

                // Center input
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Click and write numbers:", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))

                    diceInputField(
                        diceInput = diceInput,
                        onValueChange = { diceInput = it },
                    )
                }

                // Right button
                Button(
                    onClick = { endGame() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                ) {
                    Text("End Game")
                }
            } else {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { (showLeaderBoard()) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
                    modifier = Modifier.height(50.dp),
                ) {
                    Text(
                        "Show LeaderBoard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

// Composable functions for rendering dice and score board
@Composable
fun visualDiceRow(input: String) {
    val numbers =
        try {
            input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.map { it.toInt() }
        } catch (e: Exception) {
            emptyList()
        }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 0 until 5) {
            val num = numbers.getOrNull(i)
            diceFace(value = num)
        }
    }
}

// Composable function to render a single dice face based on its value
@Composable
fun diceFace(value: Int?) {
    Box(
        modifier =
            Modifier
                .size(50.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (value != null && value in 1..6) diceDots(value)
    }
}

// Composable function to render the dots on a dice face based on its value
@Composable
fun diceDots(value: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Upper row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            dot(visible = value > 1)
            dot(visible = false)
            dot(visible = value > 3)
        }

        // Middle row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            dot(visible = value == 6)
            dot(visible = value in listOf(1, 3, 5))
            dot(visible = value == 6)
        }

        // Low row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            dot(visible = value > 3)
            dot(visible = false)
            dot(visible = value > 1)
        }
    }
}

// Composable function to render a single dot on the dice face, visibility based on the dice value
@Composable
fun dot(visible: Boolean) {
    Box(
        modifier =
            Modifier
                .size(10.dp)
                .background(if (visible) Color.Black else Color.Transparent, CircleShape),
    )
}

// Composable function to render the score board table with player scores and categories
@Composable
fun scoreBoardTable(
    state: UIState,
    categorySelected: (ScoreCategory) -> Unit,
) {
    val players = state.scoreBoard.keys.toList()

    LazyColumn(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray)) {
        item {
            Row(modifier = Modifier.background(Color.LightGray).padding(8.dp)) {
                Text("Category", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                players.forEach { id ->
                    val playerName = state.pendingPlayers[id] ?: "Unknown"
                    Text(playerName, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
            }
        }

        items(ScoreCategory.entries) { category ->
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                    Text(category.name, modifier = Modifier.weight(1f))
                    if (category != ScoreCategory.BONUS && !state.isGameOver) {
                        Button(
                            onClick = {
                                categorySelected(category)
                            },
                            modifier = Modifier.height(30.dp),
                        ) {
                            Text("->")
                        }
                    }
                }
                players.forEach { id ->
                    Text(state.scoreBoard[id]?.get(category) ?: "-", modifier = Modifier.weight(1f))
                }
            }
            Divider()
        }
        item {
            Row(modifier = Modifier.background(Color(0xFFE0E0E0)).padding(8.dp)) {
                Text("TOTAL:", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                players.forEach { id ->
                    Text(state.totalScores[id]?.toString() ?: "0", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Composable function to render the final leaderboard screen with player rankings and stats
@Composable
fun leaderBoardTable(state: UIState) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("FINAL RATING", style = MaterialTheme.typography.h3, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        state.finalLeaderBoard.forEachIndexed { index, profile ->
            Text("${index + 1}. ${profile.name} | ELO: ${profile.eloRating} | WINS: ${profile.winRate * 100}%", fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun assistCommentator(state: UIState) {
    Row(
        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp).fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        val imagePath =
            when (state.assistMood) {
                AssistMood.NEUTRAL -> "assist/neutral.png"
                AssistMood.HAPPY -> "assist/happy.png"
                AssistMood.ANGRY -> "assist/angry.png"
                AssistMood.EXCITED -> "assist/excited.png"
                AssistMood.SURPRISED -> "assist/surprised.png"
            }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White,
                            RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                        )
                        .border(
                            2.dp,
                            Color.Magenta,
                            RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                        )
                        .padding(16.dp),
            ) {
                Text(
                    text = state.assistMessage,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                )
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // squared box
                        .clip(RoundedCornerShape(16.dp)),
            ) {
                Image(
                    painter = painterResource(imagePath),
                    contentDescription = "Assistant",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Composable
fun diceInputField(
    diceInput: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = diceInput,
        onValueChange = { newValue ->
            val filteredValues = newValue.filter { it in '1'..'6' }.take(5)
            onValueChange(filteredValues)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0 until 5) {
                    val c = diceInput.getOrNull(i)
                    val isNextToType = i == diceInput.length

                    Box(
                        modifier =
                            Modifier
                                .size(50.dp)
                                .border(
                                    width = if (isNextToType) 2.dp else 1.dp,
                                    color =
                                        if (isNextToType) {
                                            Color.Blue
                                        } else if (c != null) {
                                            Color.Black
                                        } else {
                                            Color.Gray
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .padding(4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (c != null) {
                            diceDots(c.digitToInt())
                        } else if (isNextToType) {
                            Text(
                                "_",
                                color = Color.Gray,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        },
    )
}
