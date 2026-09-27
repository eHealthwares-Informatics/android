package com.rxsoft.mobile.ui.prescription

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rxsoft.mobile.R
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens

@Composable
fun UploadPrescriptionScreen(
    onBack: () -> Unit = {},
    onSearch: () -> Unit = {},
    onUploadPrescription: () -> Unit = {},
    onCreateRequest: () -> Unit = {},
    onPrescriptionMedicine: () -> Unit = {},
    onGeneralMedicine: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AppTopAppBar(
                title = "",
                onBack = onBack,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SpacingTokens.xl)
            ) {
                Spacer(Modifier.height(SpacingTokens.sm))
                Text(
                    "Upload Prescription",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(SpacingTokens.sm))
                Text(
                    "Simply upload your prescription and our licensed pharmacists will handle the rest.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.height(SpacingTokens.xxl))
                UploadCard(onUpload = onUploadPrescription)
                Spacer(Modifier.height(SpacingTokens.lg))
                CreateRequestCard(onClick = onCreateRequest)
                Spacer(Modifier.height(SpacingTokens.xxxl))
                Text(
                    "Browse Medicine",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(SpacingTokens.xl))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg)) {
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
        }
    }
}

@Composable
private fun UploadCard(onUpload: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeTokens.xl,
    ) {
        Row(
            modifier = Modifier.padding(SpacingTokens.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = ShapeTokens.textField,
                color = colors.surfaceVariant,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.UploadFile, contentDescription = "Upload prescription", tint = colors.primary)
                }
            }
            Spacer(Modifier.width(SpacingTokens.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text("Prescription", fontWeight = FontWeight.SemiBold)
                Text(
                    "Maximum size 800 KB",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Button(
                onClick = onUpload,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            ) {
                Text("Upload", color = colors.onPrimary)
            }
        }
    }
}

@Composable
private fun CreateRequestCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeTokens.xl,
    ) {
        Row(
            modifier = Modifier.padding(SpacingTokens.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val colors = MaterialTheme.colorScheme
            Surface(
                shape = ShapeTokens.textField,
                color = colors.surfaceVariant,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Share, contentDescription = "Create request")
                }
            }
            Spacer(Modifier.width(SpacingTokens.lg))
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onClick, shape = CircleShape) {
                Text("Create Request")
            }
        }
    }
}

@Composable
private fun BrowseCard(title: String, image: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = title },
        shape = ShapeTokens.xxl,
    ) {
        Column {
            Image(
                painter = painterResource(image),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(SpacingTokens.md))
            Text(
                title,
                modifier = Modifier.padding(horizontal = SpacingTokens.md),
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(SpacingTokens.sm))
        }
    }
}
