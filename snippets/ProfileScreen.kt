package com.ehealthwares.rxsoft.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.R

private val Primary = Color(0xFF1EC6B5)
private val Background = Color(0xFFF7F8FA)
private val BottomBar = Color(0xFF162326)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(

    name: String = "John Doe",
    phone: String = "+234 801 234 5678",

    rewardPoints: Int = 245,

    onPersonalDetails: () -> Unit = {},
    onIdentification: () -> Unit = {},
    onFamilyMembers: () -> Unit = {},
    onAccountCredit: () -> Unit = {}

) {

    var selectedBottom by remember {
        mutableIntStateOf(4)
    }

    Scaffold(

        containerColor = Background,

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold
                    )

                }

            )

        },

        bottomBar = {

            BottomNavigationBar(

                selected = selectedBottom,

                onSelected = {

                    selectedBottom = it

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)

        ) {

            Spacer(Modifier.height(20.dp))

            Card(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(24.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )

            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),

                    horizontalAlignment = Alignment.CenterHorizontally

                ) {

                    // ASSET REFERENCE
                    // res/drawable/profile_avatar.png
                    Image(

                        painter = painterResource(R.drawable.profile_avatar),

                        contentDescription = null,

                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),

                        contentScale = ContentScale.Crop

                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        phone,
                        color = Color.Gray
                    )

                }

            }

            Spacer(Modifier.height(20.dp))

            Card(

                modifier = Modifier.fillMaxWidth(),

                colors = CardDefaults.cardColors(
                    containerColor = Primary
                ),

                shape = RoundedCornerShape(20.dp)

            ) {

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Surface(

                        shape = CircleShape,

                        color = Color.White.copy(alpha = .2f),

                        modifier = Modifier.size(52.dp)

                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(

                                Icons.Outlined.Star,

                                null,

                                tint = Color.White

                            )

                        }

                    }

                    Spacer(Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            "Reward Points",
                            color = Color.White
                        )

                        Text(

                            rewardPoints.toString(),

                            style = MaterialTheme.typography.headlineMedium,

                            color = Color.White,

                            fontWeight = FontWeight.Bold

                        )

                    }

                }

            }

            Spacer(Modifier.height(28.dp))

            ProfileMenuItem(
                icon = Icons.Outlined.Person,
                title = "Personal Details",
                onClick = onPersonalDetails
            )

            ProfileMenuItem(
                icon = Icons.Outlined.Badge,
                title = "Saved Identification",
                onClick = onIdentification
            )

            ProfileMenuItem(
                icon = Icons.Outlined.Group,
                title = "Family Members",
                onClick = onFamilyMembers
            )

            ProfileMenuItem(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = "Account Credit",
                onClick = onAccountCredit
            )

        }

    }

}

@Composable
private fun ProfileMenuItem(

    icon: androidx.compose.ui.graphics.vector.ImageVector,

    title: String,

    onClick: () -> Unit

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            Surface(

                modifier = Modifier.size(44.dp),

                shape = CircleShape,

                color = Primary.copy(alpha = .15f)

            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        icon,
                        null,
                        tint = Primary
                    )

                }

            }

            Spacer(Modifier.width(16.dp))

            Text(

                title,

                modifier = Modifier.weight(1f),

                style = MaterialTheme.typography.titleMedium

            )

            Icon(

                Icons.Outlined.KeyboardArrowRight,

                null,

                tint = Color.Gray

            )

        }

    }

}

@Composable
private fun BottomNavigationBar(

    selected: Int,

    onSelected: (Int) -> Unit

) {

    NavigationBar(

        containerColor = BottomBar

    ) {

        val items = listOf(

            Icons.Outlined.Home,

            Icons.Outlined.FavoriteBorder,

            Icons.Outlined.VolunteerActivism,

            Icons.Outlined.ShoppingCart,

            Icons.Outlined.Person

        )

        items.forEachIndexed { index, icon ->

            NavigationBarItem(

                selected = selected == index,

                onClick = {

                    onSelected(index)

                },

                icon = {

                    Icon(
                        icon,
                        null
                    )

                },

                colors = NavigationBarItemDefaults.colors(

                    selectedIconColor = Primary,

                    unselectedIconColor = Color.White,

                    indicatorColor = Color.Transparent

                )

            )

        }

    }

}