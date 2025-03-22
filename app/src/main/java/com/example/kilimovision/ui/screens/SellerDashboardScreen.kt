package com.example.kilimovision.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kilimovision.model.Advertisement
import com.example.kilimovision.model.Product
import com.example.kilimovision.model.Seller
import com.example.kilimovision.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    navController: NavController,
    onNavigateToProfile: () -> Unit = {}, // Default empty implementation for backward compatibility
    onLogout: () -> Unit,
    onRedirectToLanding: () -> Unit = {}
) {

    val context = LocalContext.current


    // Check user type to prevent unauthorized access
    val authViewModel: AuthViewModel = viewModel()
    val userType by authViewModel.userType.collectAsState()
    // Region selection state
    var region by remember { mutableStateOf("Nairobi") }
    val regions = listOf("Nairobi", "Nakuru", "Mombasa", "Kisumu", "Eldoret", "Other")

    // Error handling to prevent Google Play Services crashes
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("SellerDashboard", "Caught exception: ${exception.message}")
    }

    // State variables
    var seller by remember { mutableStateOf<Seller?>(null) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var advertisements by remember { mutableStateOf<List<Advertisement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Tabs
    val tabs = listOf("Overview", "Products", "Advertisements", "Analytics")

    Log.d("SellerDashboard", "Rendering seller dashboard screen")

    // Effect to check user type and redirect if not a seller
    LaunchedEffect(userType) {
        if (userType != "seller" && userType != null) {
            Log.d("FarmerMainScreen", "Unauthorized access attempt. User type: $userType")
            Toast.makeText(
                context,
                "Access denied: Your account is not registered as a seller",
                Toast.LENGTH_LONG
            ).show()

            onRedirectToLanding()


        }
    }

    // Fetch seller data
    LaunchedEffect(key1 = Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                // Get seller profile
                val db = FirebaseFirestore.getInstance()
                val sellerDocs = db.collection("sellers")
                    .whereEqualTo("userId", currentUser.uid)
                    .limit(1)
                    .get()
                    .await()

                if (!sellerDocs.isEmpty) {
                    val sellerDoc = sellerDocs.documents.first()
                    seller = Seller(
                        id = sellerDoc.id,
                        userId = sellerDoc.getString("userId") ?: "",
                        businessName = sellerDoc.getString("businessName") ?: "",
                        email = sellerDoc.getString("email") ?: "",
                        phone = sellerDoc.getString("phone") ?: "",
                        address = sellerDoc.getString("address") ?: ""
                    )

                    // Get products
                    val productDocs = db.collection("products")
                        .whereEqualTo("sellerId", sellerDoc.id)
                        .get()
                        .await()

                    products = productDocs.documents.map { doc ->
                        Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            inStock = doc.getBoolean("inStock") ?: false
                        )
                    }

                    // Get advertisements
                    val adDocs = db.collection("advertisements")
                        .whereEqualTo("sellerId", sellerDoc.id)
                        .get()
                        .await()

                    advertisements = adDocs.documents.map { doc ->
                        Advertisement(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            imageUrl = doc.getString("image") ?: "",
                            status = doc.getString("status") ?: ""
                        )
                    }
                } else {
                    // If seller not found, create sample data for testing
                    seller = Seller(
                        id = "sample_id",
                        businessName = "Sample Business",
                        address = "123 Sample Street",
                        email = "sample@example.com",
                        phone = "+1234567890"
                    )

                    products = listOf(
                        Product(
                            id = "product1",
                            name = "BioFungicide",
                            description = "Organic disease control",
                            price = 29.99,
                            inStock = true
                        ),
                        Product(
                            id = "product2",
                            name = "Plant Protection Spray",
                            description = "Protects against common diseases",
                            price = 19.99,
                            inStock = true
                        )
                    )

                    advertisements = listOf(
                        Advertisement(
                            id = "ad1",
                            title = "Summer Sale",
                            description = "20% off all products",
                            imageUrl = "https://example.com/image.jpg",
                            status = "active"
                        )
                    )
                }
            } catch (e: Exception) {
                // Handle error
                Log.e("SellerDashboard", "Error fetching data: ${e.message}", e)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Seller Dashboard",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        // Profile icon
                        Text("👤")
                    }
                    IconButton(onClick = onLogout) {
                        // Logout icon
                        Text("⚙️")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Seller info
            seller?.let { sellerInfo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = sellerInfo.businessName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = sellerInfo.address,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Region selection
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            TextField(
                                value = region,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Region") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = false,
                                onDismissRequest = { }
                            ) {
                                regions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            region = option
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tab row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content based on selected tab
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTabIndex) {
                    0 -> OverviewTab(products.size, advertisements.size)
                    1 -> ProductsTab(
                        products = products,
                        onAddProduct = { navController.navigate("add_product") }
                    )
                    2 -> AdvertisementsTab(
                        advertisements = advertisements,
                        onAddAdvertisement = {
                            seller?.let {
                                navController.navigate("create_advertisement/${it.id}")
                            }
                        }
                    )
                    3 -> AnalyticsTab()
                }
            }
        }
    }
}

@Composable
fun OverviewTab(
    productCount: Int,
    adCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Business Overview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Products",
                value = productCount.toString(),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Advertisements",
                value = adCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Views",
                value = "0",
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Leads",
                value = "0",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProductsTab(
    products: List<Product>,
    onAddProduct: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Products",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Button(onClick = onAddProduct) {
                Text("Add Product")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No products yet. Add your first product!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    ProductListItem(product = product)
                }
            }
        }
    }
}

@Composable
fun AdvertisementsTab(
    advertisements: List<Advertisement>,
    onAddAdvertisement: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Advertisements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Button(onClick = onAddAdvertisement) {
                Text("Create Ad")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (advertisements.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No advertisements yet. Create your first ad!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(advertisements) { ad ->
                    AdvertisementListItem(advertisement = ad)
                }
            }
        }
    }
}

@Composable
fun AnalyticsTab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Analytics coming soon",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProductListItem(
    product: Product
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Price: KES ${product.price}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = product.inStock,
                onCheckedChange = { /* Update in Firestore */ }
            )
        }
    }
}

@Composable
fun AdvertisementListItem(
    advertisement: Advertisement
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = advertisement.title,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Status: ${advertisement.status}",
                    fontSize = 14.sp,
                    color = when (advertisement.status) {
                        "active" -> MaterialTheme.colorScheme.primary
                        "pending" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }

            Button(
                onClick = { /* View details */ },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Details")
            }
        }
    }
}