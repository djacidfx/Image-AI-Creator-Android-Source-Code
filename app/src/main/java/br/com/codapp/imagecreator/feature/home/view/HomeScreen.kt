package br.com.codapp.imagecreator.feature.home.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.text.isDigitsOnly
import br.com.codapp.imagecreator.R
import br.com.codapp.imagecreator.commons.extensions.scanImages
import br.com.codapp.imagecreator.commons.extensions.shareFile
import br.com.codapp.imagecreator.data.local.entity.ImageEntity
import br.com.codapp.imagecreator.data.remote.ErrorType
import br.com.codapp.imagecreator.data.remote.ImageAiConf
import br.com.codapp.imagecreator.feature.home.state.HomeIntent
import br.com.codapp.imagecreator.feature.home.viewmodel.HomeViewModel
import br.com.codapp.imagecreator.ui.component.AdMobView
import br.com.codapp.imagecreator.ui.component.ContextualMenuDialog
import br.com.codapp.imagecreator.ui.component.ContextualMenuItem
import br.com.codapp.imagecreator.ui.component.DefaultTopAppBar
import br.com.codapp.imagecreator.ui.theme.MarginPaddingSizeMedium
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aallam.openai.api.image.ImageSize
import org.koin.androidx.compose.getViewModel
import java.io.File
import java.util.*

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = getViewModel()
) {

    val context = LocalContext.current

    val scaffoldState = rememberScaffoldState()
    val homeUiState by homeViewModel.homeState.collectAsState()

    homeViewModel.sendIntent(HomeIntent.GetAllCachedImages)

    homeUiState.error?.let { error ->
        if (error != ErrorType.NONE) {
            val errorMsg = stringResource(id = error.errorMsg)

            LaunchedEffect(Unit) {
                scaffoldState.snackbarHostState.showSnackbar(message = errorMsg)
                homeUiState.error = ErrorType.NONE
            }
        }
    }

    homeUiState.imageEntityList.forEach {
        File(it.path).scanImages(context = context)
    }

    Home(
        scaffoldState = scaffoldState,
        imageUrlList = homeUiState.imageEntityList,
        showProgress = homeUiState.loading,
        onAddImageClick = { prompt, qt, imageSize ->
            homeViewModel.sendIntent(
                HomeIntent.GenerateImagesByPrompt(
                    prompt = prompt,
                    qt = qt,
                    size = imageSize
                )
            )
        },
        onRemoveClick = { imageEntity ->
            homeViewModel.sendIntent(
                HomeIntent.RemoveCachedImage(imageEntity)
            )
        }
    )
}

@Composable
fun Home(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    imageUrlList: List<ImageEntity>,
    showProgress: Boolean,
    onAddImageClick: (prompt: String, qt: Int, imageSize: ImageSize) -> Unit,
    onRemoveClick: (imageEntity: ImageEntity) -> Unit,
) {

    val openAddImageDialogState = remember { mutableStateOf(false)  }

    if (openAddImageDialogState.value) {
        AddImageDialog(
            onAddImageClick = { prompt, qt, imageSize ->
                onAddImageClick(prompt, qt, imageSize)
            },
            onDismiss = {
                openAddImageDialogState.value = false
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            DefaultTopAppBar(
                title = stringResource(id = R.string.app_name)
            )
        },
        content = {
            ImageItemList(
                imageUrlList = imageUrlList,
                onRemoveClick = onRemoveClick
            )
            if (showProgress) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CircularProgressIndicator()
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    openAddImageDialogState.value = true
                },
                content = {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null
                    )
                }
            )
        },
        bottomBar = {
            AdMobView(adUnit = stringResource(id = R.string.admob_banner_bottom_adunit))
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageItemList(
    imageUrlList: List<ImageEntity>,
    onRemoveClick: (imageEntity: ImageEntity) -> Unit,
) {
    LazyVerticalStaggeredGrid(
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(120.dp)
    ) {
        items(imageUrlList) { image ->
            ImageItem(
                imageEntity = image,
                onRemoveClick = {
                    onRemoveClick(it)
                }
            )
        }
    }
}

@Composable
fun ImageItem(
    imageEntity: ImageEntity,
    onRemoveClick: (imageEntity: ImageEntity) -> Unit,
) {
    val context = LocalContext.current

    var showImagePreviewDialog by remember { mutableStateOf(false) }
    var showRemoveImageDialog by remember { mutableStateOf(false) }

    val imageLoader = ImageLoader.Builder(context)
        .build()

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {

        val (image, share, remove) = createRefs()

        if (showImagePreviewDialog) {
            ImagePreviewDialog(
                imagePath = imageEntity.path,
                onDismiss = { showImagePreviewDialog = false }
            )
        }

        if (showRemoveImageDialog) {
            ImageRemoveDialog(
                onRemoveClick = {
                    onRemoveClick(imageEntity)
                },
                onDismiss = { showRemoveImageDialog = false }
            )
        }

        AsyncImage(
            modifier = Modifier
                .size(120.dp)
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
                .clickable {
                    showImagePreviewDialog = true
                },
            model = ImageRequest.Builder(context)
                .data(File(imageEntity.path))
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            imageLoader = imageLoader,
        )

        Icon(
            modifier = Modifier
                .constrainAs(remove) {
                    top.linkTo(image.top)
                    start.linkTo(image.start)
                }
                .padding(8.dp)
                .clickable {
                    showRemoveImageDialog = true
                },
            imageVector = Icons.TwoTone.Delete,
            contentDescription = null
        )

        Icon(
            modifier = Modifier
                .constrainAs(share) {
                    top.linkTo(image.top)
                    end.linkTo(image.end)
                }
                .padding(8.dp)
                .clickable {
                    shareFile(path = imageEntity.path, context = context)
                },
            imageVector = Icons.TwoTone.Share,
            contentDescription = null
        )
    }
}

@Composable
private fun ImageRemoveDialog(
    onRemoveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    onRemoveClick()
                    onDismiss()
                },
                content = {
                    Text(text = stringResource(id = R.string.remove_image_dialog_positive_button))
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                content = {
                    Text(text = stringResource(id = R.string.remove_image_dialog_negative_button))
                }
            )
        },
        title = {
            Text(text = stringResource(id = R.string.remove_image_dialog_title))
        }
    )
}

@Composable
private fun ImagePreviewDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        content = {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = File(imagePath),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AddImageDialog(
    onAddImageClick: (prompt: String, qt: Int, imageSize: ImageSize) -> Unit,
    onDismiss: () -> Unit
) {

    var text by remember { mutableStateOf("") }
    var qtText by remember { mutableStateOf("1") }
    var isError by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(ImageAiConf.IMAGE_SIZE_LIST[0]) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    onAddImageClick(
                        text,
                        qtText.toInt(),
                        selectedOptionText
                    )
                    onDismiss()
                }
            ) {
                if (!isError) {
                    Text(text = stringResource(id = R.string.add_image_dialog_positive_button))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(id = R.string.add_image_dialog_negative_button))
            }
        },
        title = { Text(text = stringResource(id = R.string.add_image_dialog_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.add_image_dialog_content)
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MarginPaddingSizeMedium),
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(text = stringResource(id = R.string.add_image_dialog_edt)) }
                )
                ExposedDropdownMenuBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MarginPaddingSizeMedium),
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {
                    TextField(
                        readOnly = true,
                        value = selectedOptionText.size,
                        onValueChange = { },
                        label = { Text(stringResource(id = R.string.add_image_dialog_dropdown)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        ImageAiConf.IMAGE_SIZE_LIST.forEach { selectionOption ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedOptionText = selectionOption
                                    expanded = false
                                }
                            ) {
                                Text(text = selectionOption.size)
                            }
                        }
                    }
                }

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MarginPaddingSizeMedium),
                    value = qtText,
                    onValueChange = {
                        qtText = if (it.isDigitsOnly()) {
                            it
                        } else {
                            qtText
                        }

                        isError = !qtText.isDigitsOnly() ||
                                qtText.isEmpty() ||
                                qtText.toInt() <= 0 ||
                                qtText.toInt() > ImageAiConf.MAX_IMAGES_GENERATE
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    label = {
                        Text(
                            text = stringResource(id = R.string.add_image_qt_edt, ImageAiConf.MAX_IMAGES_GENERATE)
                        )
                    },
                    singleLine = true,
                    isError = isError
                )
                if (isError) {
                    Text(
                        text = stringResource(id = R.string.add_image_qt_edt_error, ImageAiConf.MAX_IMAGES_GENERATE),
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

            }
        }
    )
}

@Composable
fun ImageContextualMenuDialog(
    onRemoveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ContextualMenuDialog(
        contextualMenuItems = listOf(
            ContextualMenuItem(
                text = R.string.add_image_contextual_menu_remove,
                onClick = { onRemoveClick() }
            )
        ),
        onDismiss = { onDismiss() }
    )
}

@Preview
@Composable
fun HomePreview() {
    Home(
        imageUrlList = fakeImageEntityList,
        showProgress = true,
        onAddImageClick = { _,_,_ -> },
        onRemoveClick = {}
    )
}

@Preview
@Composable
fun ImageItemListPreview() {
    ImageItemList(imageUrlList = fakeImageEntityList, onRemoveClick = {})
}

@Preview
@Composable
fun ImageRemoveDialogPreview() {
    ImageRemoveDialog(onRemoveClick = {  }) { }
}

@Preview
@Composable
fun AddImageDialogPreview() {
    AddImageDialog(
        onAddImageClick = {_,_,_ -> },
        onDismiss = { }
    )
}

private val fakeImageEntityList = listOf<ImageEntity>(
    ImageEntity(url = "https://img.olhardigital.com.br/wp-content/uploads/2021/01/android.jpg", createdAt = Date(), path = ""),
    ImageEntity(url = "https://img.olhardigital.com.br/wp-content/uploads/2021/01/android.jpg", createdAt = Date(), path = ""),
    ImageEntity(url = "https://img.olhardigital.com.br/wp-content/uploads/2021/01/android.jpg", createdAt = Date(), path = ""),
    ImageEntity(url = "https://img.olhardigital.com.br/wp-content/uploads/2021/01/android.jpg", createdAt = Date(), path = ""),
    ImageEntity(url = "https://img.olhardigital.com.br/wp-content/uploads/2021/01/android.jpg", createdAt = Date(), path = ""),
    ImageEntity(url = "https://img.olhardigital.com.br/wp-content/uploads/2021/01/android.jpg", createdAt = Date(), path = ""),
    ImageEntity(url = "https://img.olhardigital.com.br/wp-content/uploads/2021/01/android.jpg", createdAt = Date(), path = ""),
    ImageEntity(url = "https://img.olhardigital.com.br/wp-content/uploads/2021/01/android.jpg", createdAt = Date(), path = ""),
    ImageEntity(url = "https://img.olhardigital.com.br/wp-content/uploads/2021/01/android.jpg", createdAt = Date(), path = "")
)