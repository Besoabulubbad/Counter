package com.abulubad.counter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.LocalCounterDimens

@Composable
fun CounterScaffold(
    chrome: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    actionBar: (@Composable RowScope.() -> Unit)? = null,
    body: @Composable () -> Unit,
) {
    val dimens = LocalCounterDimens.current
    Column(modifier.fillMaxSize().background(CounterColors.Surface)) {
        Column(Modifier.fillMaxWidth().background(CounterColors.Chrome)) {
            Spacer(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.safeDrawing))
            Row(
                Modifier.fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .height(dimens.chromeHeight)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = chrome,
            )
        }
        Box(
            Modifier.weight(1f).fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        if (actionBar == null) {
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                        } else {
                            WindowInsetsSides.Horizontal
                        },
                    ),
                ),
        ) {
            body()
        }
        if (actionBar != null) {
            Column(Modifier.fillMaxWidth().background(CounterColors.Chrome)) {
                Row(
                    Modifier.fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .height(dimens.actionBarHeight)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    content = actionBar,
                )
                Spacer(Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }
    }
}