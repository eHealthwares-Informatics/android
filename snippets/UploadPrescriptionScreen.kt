package com.ehealthwares.rxsoft.ui.prescription

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.R

private val PrimaryColor = Color(0xFF1EC6B5)
private val BottomBarColor = Color(0xFF152222)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPrescriptionScreen(
    onBack: () -> Unit = {},
    onSearch: () -> Unit = {},
    onUploadPrescription: () -> Unit = {},
    onCreateRequest: () -> Unit = {},
    onPrescriptionMedicine: () -> Unit = {},
    onGeneralMedicine: () -> Unit = {}
) {

    var selectedBottom by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        containerColor = Color(0xFFF6F8F8),
        topBar = {

            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {

                    IconButton(onClick = onSearch) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null
                        )
                    }
                }
            )

        }

    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Upload Prescription",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Simply upload your prescription and our licensed pharmacists will handle the rest.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                UploadCard(
                    onUpload = onUploadPrescription
                )

                Spacer(modifier = Modifier.height(16.dp))

                CreateRequestCard(
                    onClick = onCreateRequest
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    "Browse Medicine",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(18.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    item {

                        BrowseCard(
                            title = "Prescription Medicine",
                            image = R.drawable.prescription_category,
                            onClick = onPrescriptionMedicine
                        )

                    }

                    item {

                        BrowseCard(
                            title = "General Medicine",
                            image = R.drawable.medicine_category,
                            onClick = onGeneralMedicine
                        )

                    }

                }

            }

            BottomFloatingBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                selected = selectedBottom,
                onSelected = {
                    selectedBottom = it
                }
            )

        }

    }

}

@Composable
private fun UploadCard(
    onUpload: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF3F5F6),
                modifier = Modifier.size(56.dp)
            ) {

                Box(contentAlignment = Alignment.Center) {

                    Icon(
                        Icons.Outlined.UploadFile,
                        contentDescription = null,
                        tint = PrimaryColor
                    )

                }

            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    "Prescription",
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    "Maximum size 800 KB",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )

            }

            Button(
                onClick = onUpload,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor
                )
            ) {

                Text("Upload")

            }

        }

    }

}

@Composable
private fun CreateRequestCard(
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF3F5F6),
                modifier = Modifier.size(56.dp)
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = null
                    )

                }

            }

            Spacer(modifier = Modifier.width(16.dp))

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(30.dp)
            ) {

                Text("Create Request")

            }

        }

    }

}

@Composable
private fun BrowseCard(
    title: String,
    image: Int,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column {

            // ASSET REFERENCE
            Image(
                painter = painterResource(image),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                title,
                modifier = Modifier.padding(horizontal = 12.dp),
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

        }

    }

}

@Composable
private fun BottomFloatingBar(
    modifier: Modifier = Modifier,
    selected: Int,
    onSelected: (Int) -> Unit
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = BottomBarColor
        )
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            val icons = listOf(
                Icons.Outlined.Home,
                Icons.Outlined.FavoriteBorder,
                Icons.Outlined.Share,
                Icons.Outlined.ShoppingCart,
                Icons.Outlined.Person
            )

            icons.forEachIndexed { index, icon ->

                val active = selected == index

                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clickable {
                            onSelected(index)
                        },
                    shape = CircleShape,
                    color = if (active) PrimaryColor else Color.Transparent
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White
                        )

                    }

                }

            }

        }

    }

}