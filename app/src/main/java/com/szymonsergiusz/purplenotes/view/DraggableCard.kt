package com.szymonsergiusz.purplenotes.view

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.szymonsergiusz.purplenotes.notes.Note
import kotlin.math.roundToInt


const val ANIMATION_DURATION = 500
const val MIN_DRAG_AMOUNT = 6
@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
fun DraggableCard(
    card: Note,
    isRevealed: Boolean,
    cardHeight: Dp,
    cardOffset: Float,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
) {
    val offsetX = remember { mutableStateOf(0f) }
    val transitionState = remember {
        MutableTransitionState(isRevealed).apply {
            targetState = !isRevealed
        }
    }
    val transition = updateTransition(transitionState, "cardTransition")

    val offsetTransition by transition.animateFloat(
        label = "cardOffsetTransition",
        transitionSpec = { tween(durationMillis = ANIMATION_DURATION) },
        targetValueByState = { if (isRevealed) cardOffset - offsetX.value else -offsetX.value },

        )
    val cardElevation by transition.animateDp(
        label = "cardElevation",
        transitionSpec = { tween(durationMillis = ANIMATION_DURATION) },
        targetValueByState = { if (isRevealed) 40.dp else 2.dp }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .offset { IntOffset((offsetX.value + offsetTransition).roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    val original = Offset(offsetX.value, 0f)
                    val summed = original + Offset(x = dragAmount, y = 0f)
                    val newValue = Offset(x = summed.x.coerceIn(0f, cardOffset), y = 0f)
                    if (newValue.x >= 10) {
                        onExpand()
                        return@detectHorizontalDragGestures
                    } else if (newValue.x <= 0) {
                        onCollapse()
                        return@detectHorizontalDragGestures
                    }
                    change.consumePositionChange()
                    offsetX.value = newValue.x
                }
            },
        shape = RoundedCornerShape(0.dp),
        elevation = cardElevation,
        content = {
            NoteCard(note = card)
        })
}