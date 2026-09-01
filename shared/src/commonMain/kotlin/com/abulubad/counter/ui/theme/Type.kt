package com.abulubad.counter.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import counter.shared.generated.resources.Res
import counter.shared.generated.resources.ibmplexmono_medium
import counter.shared.generated.resources.ibmplexmono_regular
import counter.shared.generated.resources.ibmplexmono_semibold
import counter.shared.generated.resources.ibmplexsans_medium
import counter.shared.generated.resources.ibmplexsans_regular
import counter.shared.generated.resources.ibmplexsans_semibold
import org.jetbrains.compose.resources.Font

data class CounterFonts(val sans: FontFamily, val mono: FontFamily)

val LocalCounterFonts = staticCompositionLocalOf {
    CounterFonts(FontFamily.Default, FontFamily.Monospace)
}

@Composable
fun counterFonts(): CounterFonts = CounterFonts(
    sans = FontFamily(
        Font(Res.font.ibmplexsans_regular, FontWeight.Normal),
        Font(Res.font.ibmplexsans_medium, FontWeight.Medium),
        Font(Res.font.ibmplexsans_semibold, FontWeight.SemiBold),
    ),
    mono = FontFamily(
        Font(Res.font.ibmplexmono_regular, FontWeight.Normal),
        Font(Res.font.ibmplexmono_medium, FontWeight.Medium),
        Font(Res.font.ibmplexmono_semibold, FontWeight.SemiBold),
    ),
)

val PlexSans: FontFamily @Composable get() = LocalCounterFonts.current.sans
val PlexMono: FontFamily @Composable get() = LocalCounterFonts.current.mono

const val TabularFigures = "tnum"

object CounterType {
    val micro: TextUnit = 11.sp
    val body: TextUnit = 13.sp
    val emphasis: TextUnit = 15.sp
    val display: TextUnit = 20.sp
    val hero: TextUnit = 28.sp
}