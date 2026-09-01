package com.abulubad.counter.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abulubad.counter.platform.formatCurrency
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.PlexMono
import com.abulubad.counter.ui.theme.PlexSans

@Composable
fun OrderScreen(onAddItem: (OrderItem) -> Unit, modifier: Modifier = Modifier) {
    val catalog = remember { orderCatalog() }
    var selected by remember { mutableIntStateOf(0) }
    Row(modifier.fillMaxSize().background(CounterColors.Surface)) {
        Column(
            Modifier.width(168.dp).fillMaxHeight().background(CounterColors.SurfaceSunk)
                .verticalScroll(rememberScrollState()),
        ) {
            catalog.forEachIndexed { index, category ->
                CategoryRow(category, selected == index) { selected = index }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 168.dp),
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(catalog[selected].items, key = { it.sku }) { item ->
                ItemTile(item) { onAddItem(item) }
            }
        }
    }
}

@Composable
private fun CategoryRow(category: OrderCategory, active: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .background(if (active) CounterColors.Surface else CounterColors.SurfaceSunk)
            .drawBehind {
                if (active) drawRect(CounterColors.Confirmed, size = Size(3.dp.toPx(), size.height))
            }
            .padding(13.dp),
    ) {
        Text(category.name, color = CounterColors.Ink, fontFamily = PlexSans, fontSize = CounterType.body, fontWeight = if (active) FontWeight.Medium else FontWeight.Normal)
        Text("${category.items.size} items", color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro)
    }
}

@Composable
private fun ItemTile(item: OrderItem, onClick: () -> Unit) {
    Column(
        Modifier.height(112.dp).clickable(onClick = onClick).background(CounterColors.Surface)
            .border(1.dp, CounterColors.Rule).padding(10.dp),
    ) {
        Text(item.name, color = CounterColors.Ink, fontFamily = PlexSans, fontSize = CounterType.body, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(item.sku, color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro)
            Text(formatCurrency(item.priceMinorUnits, "USD"), color = CounterColors.Ink, fontFamily = PlexMono, fontSize = CounterType.emphasis, fontWeight = FontWeight.Medium)
        }
        Text(stockLabel(item.stock), color = stockColor(item.stock), fontFamily = PlexMono, fontSize = CounterType.micro)
    }
}

private fun stockLabel(stock: Int): String = if (stock == 0) "out of stock" else "$stock on hand"

private fun stockColor(stock: Int): Color = when {
    stock == 0 -> CounterColors.Conflict
    stock < 5 -> CounterColors.Held
    else -> CounterColors.InkMuted
}