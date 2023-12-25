package br.com.codapp.imagecreator.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import br.com.codapp.imagecreator.ui.theme.MarginPaddingSizeMedium
import br.com.codapp.imagecreator.ui.theme.TextSizeSmall

@Composable
fun DefaultTopAppBar(
    title: String,
    subTitle: String = "",
    titleMaxLines: Int = Int.MAX_VALUE,
    titleOverflow: TextOverflow = TextOverflow.Ellipsis,
    subTitleMaxLines: Int = Int.MAX_VALUE,
    subTitleOverflow: TextOverflow = TextOverflow.Ellipsis,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    maxLines = titleMaxLines,
                    overflow = titleOverflow,
                )
                if (subTitle.isNotBlank()) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = subTitle,
                            fontSize = TextSizeSmall,
                            maxLines = subTitleMaxLines,
                            overflow = subTitleOverflow,
                        )
                    }
                }
            }
        },
        contentColor = White,
        navigationIcon = navigationIcon,
        actions = { actions?.invoke(this) },
        elevation = elevation
    )
}

@Composable
fun ContextualMenuDialog(
    contextualMenuItems: List<ContextualMenuItem>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
    ) {
        ContextualMenu(items = contextualMenuItems)
    }
}

@Composable
private fun ContextualMenu(items: List<ContextualMenuItem>) {
    LazyColumn(
        modifier = Modifier.background(White)
    ) {
        items(items) { item ->
            ContextualItemMenu(itemMenu = item)
            Divider(color = Color.Gray, thickness = 1.dp)
        }
    }
}

@Composable
private fun ContextualItemMenu(itemMenu: ContextualMenuItem) {
    Column(
        modifier = Modifier
            .padding(MarginPaddingSizeMedium)
            .fillMaxWidth()
            .clickable { itemMenu.onClick() }
    ) {
        Text(text = stringResource(id = itemMenu.text))
    }
}

data class ContextualMenuItem(
    val text: Int,
    val onClick: () -> Unit
)