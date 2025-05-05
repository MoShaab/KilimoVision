package com.example.kilimovision.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.platform.LocalContext

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

import androidx.compose.material.icons.automirrored.filled.ExitToApp
import com.example.kilimovision.repository.FirebaseRepository
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.firestoreSettings
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    navController: NavController,
    onNavigateToProfile: () -> Unit = {}, // Default empty implementation for backward compatibility
    onLogout: () -> Unit,
    onRedirectToLanding: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {

    val context = LocalContext.current




    // Check user type to prevent unauthorized access
    val authViewModel: AuthViewModel = viewModel()
    val userType by authViewModel.userType.collectAsState()
    // Region selection state

//    var expanded by remember { mutableStateOf(false) }
//    var region by remember { mutableStateOf("") }
//    val regions = listOf("Nairobi", "Nakuru", "Mombasa", "Kisumu", "Eldoret", "Other")

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
                val sellerDocs = db.collection("sellerProfiles")
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
                        "KilimoVision for Sellers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,  // Reduced from default (which is typically 18.sp)
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Open your drawer/menu */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = Color.Black
                        )
                    }
                },

                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log out"
                        )
                    }
                    IconButton(onClick = onNavigateToRegister) {
                        Icon(
                            imageVector = Icons.Filled.AppRegistration,
                            contentDescription = "Register"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
//                        ExposedDropdownMenuBox(
//                            expanded = expanded,
//                            onExpandedChange = { expanded = !expanded},
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 8.dp)
//                        ) {
//                            TextField(
//                                value = region,
//                                onValueChange = {},
//                                readOnly = true,
//                                label = { Text("Region") },
//                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
//                                modifier = Modifier.menuAnchor()
//                            )
//
//                            ExposedDropdownMenu(
//                                expanded = expanded,
//                                onDismissRequest = { expanded =  false}
//                            ) {
//                                regions.forEach { option ->
//                                    DropdownMenuItem(
//                                        text = { Text(option) },
//                                        onClick = {
//                                            region = option
//                                            expanded =  false
//                                        }
//                                    )
//                                }
//                            }
//                        }
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
                        onAddProduct = {

                            seller?.let {
                                navController.navigate("add_product/${it.id}")
                            }
                        },
                        navController =  navController
                    )
                    2 -> AdvertisementsTab(
                        advertisements = advertisements,
                        onAddAdvertisement = {
                            seller?.let {
                                navController.navigate("create_advertisement/${it.id}")
                            }
                        },
                        navController = navController
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Business Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Today's summary of your business activities",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Products",
                value = productCount.toString(),
                icon = Icons.Default.ShoppingCart,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Advertisements",
                value = adCount.toString(),
                icon = Icons.Default.Notifications,
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
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Leads",
                value = "0",
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProductsTab(
    products: List<Product>,
    onAddProduct: () -> Unit,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository  = FirebaseRepository()
    // State for product list updates
    var productsList by remember { mutableStateOf(products) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Effects
    LaunchedEffect(products) {
        productsList = products
    }

    // Product detail dialog state
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    // Show product detail dialog
    if (showDetailDialog && selectedProduct != null) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = { Text(selectedProduct!!.name) },
            text = {
                Column {
                    Text("Description: ${selectedProduct!!.description}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Price: KES ${selectedProduct!!.price}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Stock Status: ${if (selectedProduct!!.inStock) "In Stock" else "Out of Stock"}")
                }
            },
            confirmButton = {
                Button(onClick = { showDetailDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Products",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = onAddProduct,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Product")
            }
        }

        if (isRefreshing) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        if (productsList.isEmpty()) {
            EmptyStateMessage(
                message = "No products yet. Add your first product!",
                icon = Icons.Default.ShoppingCart
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productsList) { product ->
                    ProductListItem(
                        product = product,
                        onEdit = {
                            // Navigate to edit product screen
                            navController.navigate("edit_product/${product.id}")
                        },
                        onDelete = { productToDelete ->
                            scope.launch {
                                isRefreshing = true
                                    repository.deleteProduct(productToDelete.id) { success ->
                                    isRefreshing = false
                                    if (success) {
                                        // Update local list
                                        productsList = productsList.filter { it.id != productToDelete.id }
                                        Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to delete product", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onViewDetails = { productToView ->
                            selectedProduct = productToView
                            showDetailDialog = true
                        },
                        onStockChange = { productToUpdate, newStockStatus ->
                            scope.launch {
                                isRefreshing = true
                                repository.updateProductStock(productToUpdate.id, newStockStatus) { success ->
                                    isRefreshing = false
                                    if (success) {
                                        // Update local list
                                        productsList = productsList.map {
                                            if (it.id == productToUpdate.id) {
                                                it.copy(inStock = newStockStatus)
                                            } else {
                                                it
                                            }
                                        }
                                        Toast.makeText(
                                            context,
                                            "Product ${if (newStockStatus) "marked as in stock" else "marked as out of stock"}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(context, "Failed to update product stock status", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(
    message: String,
    icon: ImageVector
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}


@Composable
fun ProductListItem(
    product: Product,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    onViewDetails: (Product) -> Unit,
    onStockChange: (Product, Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // State for dropdown menu
    var expanded by remember { mutableStateOf(false) }

    // State for confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Handle delete confirmation
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete ${product.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(product)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = MaterialTheme.shapes.medium)
            .clickable { onViewDetails(product) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "KES ${product.price}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = product.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (product.inStock) "In Stock" else "Out of Stock",
                        fontSize = 12.sp,
                        color = if (product.inStock)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Dropdown menu for actions
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = {
                                    expanded = false
                                    onEdit(product)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Details") },
                                leadingIcon = { Icon(Icons.Default.Info, null) },
                                onClick = {
                                    expanded = false
                                    onViewDetails(product)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stock toggle
                Switch(
                    checked = product.inStock,
                    onCheckedChange = { isChecked ->
                        scope.launch {
                            onStockChange(product, isChecked)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun AdvertisementsTab(
    advertisements: List<Advertisement>,
    onAddAdvertisement: () -> Unit,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = FirebaseRepository()
    // State for advertisement list updates
    var adsList by remember { mutableStateOf(advertisements) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Effects
    LaunchedEffect(advertisements) {
        adsList = advertisements
    }

    // Advertisement detail dialog state
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedAd by remember { mutableStateOf<Advertisement?>(null) }

    // Show advertisement detail dialog
    if (showDetailDialog && selectedAd != null) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = { Text(selectedAd!!.title) },
            text = {
                Column {
                    Text("Description: ${selectedAd!!.description}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status: ${selectedAd!!.status}")
                    Spacer(modifier = Modifier.height(16.dp))

                    // Image placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAd!!.imageUrl.isNotEmpty()) {
                            // If using Coil, you would load the image here

                        } else {
                            Text("No image available")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDetailDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Advertisements",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = onAddAdvertisement,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Ad")
            }
        }

        if (isRefreshing) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        if (adsList.isEmpty()) {
            EmptyStateMessage(
                message = "No advertisements yet. Create your first ad!",
                icon = Icons.Default.Notifications
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(adsList) { ad ->
                    AdvertisementListItem(
                        advertisement = ad,
                        onEdit = {
                            // Navigate to edit advertisement screen
                            navController.navigate("edit_advertisement/${ad.id}")
                        },
                        onDelete = { adToDelete ->
                            scope.launch {
                                isRefreshing = true
                                repository.deleteAdvertisement(adToDelete.id) { success ->
                                    isRefreshing = false
                                    if (success) {
                                        // Update local list
                                        adsList = adsList.filter { it.id != adToDelete.id }
                                        Toast.makeText(context, "Advertisement deleted successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to delete advertisement", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onViewDetails = { adToView ->
                            selectedAd = adToView
                            showDetailDialog = true
                        },
                        onStatusChange = { adToUpdate, newStatus ->
                            scope.launch {
                                isRefreshing = true
                                repository.updateAdvertisementStatus(adToUpdate.id, newStatus) { success ->
                                    isRefreshing = false
                                    if (success) {
                                        // Update local list
                                        adsList = adsList.map {
                                            if (it.id == adToUpdate.id) {
                                                it.copy(status = newStatus)
                                            } else {
                                                it
                                            }
                                        }
                                        Toast.makeText(
                                            context,
                                            "Advertisement status updated to ${newStatus}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(context, "Failed to update advertisement status", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementListItem(
    advertisement: Advertisement,
    onEdit: (Advertisement) -> Unit,
    onDelete: (Advertisement) -> Unit,
    onViewDetails: (Advertisement) -> Unit,
    onStatusChange: (Advertisement, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // State for dropdown menu
    var expanded by remember { mutableStateOf(false) }
    var showStatusOptions by remember { mutableStateOf(false) }

    // State for confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Handle delete confirmation
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Advertisement") },
            text = { Text("Are you sure you want to delete this advertisement? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(advertisement)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

//    // Status change dialog
//    if (showStatusOptions) {
//        BasicAlertDialog(
//            onDismissRequest = { showStatusOptions = false },
//            title = { Text("Change Status") },
//            text = { Text("Select a new status for this advertisement") },
//            confirmButton = {},
//            dismissButton = {
//                OutlinedButton(onClick = { showStatusOptions = false }) {
//                    Text("Cancel")
//                }
//            },
//            content = {
//                Column(
//                    modifier = Modifier.padding(16.dp)
//                ) {
//                    val statuses = listOf("active", "pending", "inactive")
//                    statuses.forEach { status ->
//                        Button(
//                            onClick = {
//                                showStatusOptions = false
//                                onStatusChange(advertisement, status)
//                            },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 4.dp),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = when (status) {
//                                    "active" -> MaterialTheme.colorScheme.primary
//                                    "pending" -> MaterialTheme.colorScheme.tertiary
//                                    else -> MaterialTheme.colorScheme.error
//                                }
//                            )
//                        ) {
//                            Text(status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
//                        }
//                    }
//                }
//            }
//        )
//    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = MaterialTheme.shapes.medium)
            .clickable { onViewDetails(advertisement) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Use AsyncImage if you have Coil integrated
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = advertisement.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = advertisement.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Dropdown menu for actions
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = {
                                expanded = false
                                onEdit(advertisement)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Change Status") },
                            leadingIcon = { Icon(Icons.Default.Refresh, null) },
                            onClick = {
                                expanded = false
                                showStatusOptions = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Details") },
                            leadingIcon = { Icon(Icons.Default.Info, null) },
                            onClick = {
                                expanded = false
                                onViewDetails(advertisement)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                expanded = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showStatusOptions = true }
                ) {
                    val (statusColor, statusIcon) = when (advertisement.status.lowercase()) {
                        "active" -> Pair(MaterialTheme.colorScheme.primary, Icons.Default.CheckCircle)
                        "pending" -> Pair(MaterialTheme.colorScheme.tertiary, Icons.Default.Refresh)
                        else -> Pair(MaterialTheme.colorScheme.error, Icons.Default.Warning)
                    }

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = advertisement.status.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.ROOT
                            ) else it.toString()
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }

                OutlinedButton(
                    onClick = { onViewDetails(advertisement) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Details",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Analytics Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Placeholder charts
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Product Views",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Placeholder chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chart coming soon",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Second placeholder chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Product Engagement",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Placeholder chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chart coming soon",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .shadow(elevation = 2.dp, shape = MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

