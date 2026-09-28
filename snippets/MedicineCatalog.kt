package com.ehealthwares.rxsoft.ui.medicine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.R

private val Primary = Color(0xFF1EC6B5)
private val Background = Color(0xFFF6F8F8)
private val BottomBar = Color(0xFF162326)

data class Medicine(
    val id: Int,
    val name: String,
    val manufacturer: String,
    val price: Double,
    val oldPrice: Double?,
    val imageRes: Int,
    var favourite: Boolean = false
)

private fun sampleMedicines() = mutableStateListOf(

    Medicine(
        id = 1,
        name = "Moxal Plus Suspension",
        manufacturer = "Julphar",
        price = 3200.0,
        oldPrice = 3700.0,
        imageRes = R.drawable.med_moxal
    ),

    Medicine(
        id = 2,
        name = "Adol 500mg Tablets",
        manufacturer = "Julphar",
        price = 1800.0,
        oldPrice = null,
        imageRes = R.drawable.med_adol
    ),

    Medicine(
        id = 3,
        name = "Orofar Spray",
        manufacturer = "Sandoz",
        price = 2500.0,
        oldPrice = 2900.0,
        imageRes = R.drawable.med_orofar
    ),

    Medicine(
        id = 4,
        name = "Proflora Capsules",
        manufacturer = "BioCare",
        price = 4200.0,
        oldPrice = 4700.0,
        imageRes = R.drawable.med_proflora
    ),

    Medicine(
        id = 5,
        name = "Vitamin C 1000mg",
        manufacturer = "NatureMade",
        price = 2100.0,
        oldPrice = null,
        imageRes = R.drawable.med_vitaminc
    ),

    Medicine(
        id = 6,
        name = "Amoxicillin 500mg",
        manufacturer = "GSK",
        price = 5600.0,
        oldPrice = 6200.0,
        imageRes = R.drawable.med_amoxicillin
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineCatalogScreen(

    onSearch: (String) -> Unit = {},
    onFilter: () -> Unit = {},
    onMedicineClick: (Medicine) -> Unit = {},
    onCartClick: () -> Unit = {}

) {

    val medicines = remember {
        sampleMedicines()
    }

    var search by remember {
        mutableStateOf("")
    }

    var selectedBottom by remember {
        mutableIntStateOf(0)
    }

    Scaffold(

        containerColor = Background,

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        "Medicine",
                        fontWeight = FontWeight.Bold
                    )
                },

                actions = {

                    IconButton(
                        onClick = onCartClick
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = null
                        )
                    }

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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(

                    value = search,

                    onValueChange = {

                        search = it
                        onSearch(it)

                    },

                    modifier = Modifier.weight(1f),

                    singleLine = true,

                    placeholder = {

                        Text("Search medicines")

                    },

                    leadingIcon = {

                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null
                        )

                    },

                    shape = RoundedCornerShape(16.dp)

                )

                Spacer(modifier = Modifier.width(12.dp))

                FilledIconButton(

                    onClick = onFilter,

                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Primary
                    )

                ) {

                    Icon(
                        Icons.Outlined.FilterList,
                        contentDescription = null,
                        tint = Color.White
                    )

                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            val filtered = remember(search, medicines.size) {

                medicines.filter {

                    it.name.contains(
                        search,
                        ignoreCase = true
                    ) ||
                            it.manufacturer.contains(
                                search,
                                ignoreCase = true
                            )

                }

            }

            LazyVerticalGrid(

                columns = GridCells.Fixed(2),

                horizontalArrangement = Arrangement.spacedBy(16.dp),

                verticalArrangement = Arrangement.spacedBy(16.dp),

                modifier = Modifier.weight(1f),

                contentPadding = PaddingValues(bottom = 90.dp)

            ) {

                items(
                    filtered,
                    key = { it.id }
                ) { medicine ->

                    MedicineCard(

                        medicine = medicine,

                        onFavourite = {

                            medicine.favourite =
                                !medicine.favourite

                        },

                        onClick = {

                            onMedicineClick(medicine)

                        }

                    )

                }

            }

        }

        BottomNavigationBar(

            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomCenter),

            selected = selectedBottom,

            onSelected = {

                selectedBottom = it

            }

        )

    }

}

@Composable
private fun MedicineCard(
    medicine: Medicine,
    onFavourite: () -> Unit,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column {

            Box {

                // *************** ASSET REFERENCE ****************
                // res/drawable/
                // med_moxal.png
                // med_adol.png
                // med_orofar.png
                // med_proflora.png
                // med_vitaminc.png
                // med_amoxicillin.png
                //
                // Replace with AsyncImage() if loading
                // from your backend.
                Image(
                    painter = painterResource(medicine.imageRes),
                    contentDescription = medicine.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Fit
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {

                    IconButton(
                        onClick = onFavourite
                    ) {

                        Icon(
                            imageVector =
                            if (medicine.favourite)
                                Icons.Outlined.Favorite
                            else
                                Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint =
                            if (medicine.favourite)
                                Color.Red
                            else
                                Color.Gray
                        )

                    }

                }

            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {

                Text(
                    medicine.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    medicine.manufacturer,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        "₦${medicine.price.toInt()}",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    medicine.oldPrice?.let {

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            "₦${it.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )

                    }

                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text("Add")

                }

            }

        }

    }

}