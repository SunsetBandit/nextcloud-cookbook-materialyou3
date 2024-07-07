package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700

@Composable
fun DefaultButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors =
        ButtonDefaults.buttonColors(
            backgroundColor = Color.White,
            contentColor = NcBlue700,
        ),
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        content = content,
    )
}

@Preview
@Composable
fun DefaultButtonPreview() {
    DefaultButton(onClick = { }) {
        Text("Button")
    }
}
