package com.cycad.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycad.app.ui.theme.CYCADTheme
import kotlinx.coroutines.delay

// --- Data Models ---

data class InventoryItem(
    val id: Int,
    val name: String,
    val price: Double,
    val stock: Int,
    val description: String,
    val company: String,
    val category: String,
    val color: Color
)

data class CartItem(
    val item: InventoryItem,
    var quantity: Int
)

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Cart : Screen("cart")
    object Account : Screen("account")
    object Checkout : Screen("checkout")
    object OrderSuccess : Screen("success")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CYCADTheme {
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(3000) // 3 second startup
                    isLoading = false
                }

                Crossfade(targetState = isLoading, animationSpec = tween(1000), label = "SplashTransition") { loading ->
                    if (loading) {
                        LoadingScreen()
                    } else {
                        MainAppFlow()
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppFlow() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val cart = remember { mutableStateListOf<CartItem>() }
    
    // Simple navigation state for tabs
    val bottomNavItems = listOf(
        Triple(Screen.Home, Icons.Default.Home, "Home"),
        Triple(Screen.Categories, Icons.Default.Category, "Categories"),
        Triple(Screen.Cart, Icons.Default.ShoppingCart, "Cart"),
        Triple(Screen.Account, Icons.Default.Person, "Account")
    )

    BackHandler(enabled = currentScreen != Screen.Home) {
        currentScreen = Screen.Home
    }

    Scaffold(
        bottomBar = {
            if (currentScreen !in listOf(Screen.Checkout, Screen.OrderSuccess)) {
                Column {
                    HorizontalDivider(color = Color.LightGray)
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        bottomNavItems.forEach { (screen, icon, label) ->
                            NavigationBarItem(
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (screen == Screen.Cart && cart.isNotEmpty()) {
                                                Badge(containerColor = Color.Red) { Text(cart.sumOf { it.quantity }.toString()) }
                                            }
                                        }
                                    ) { Icon(icon, contentDescription = label) }
                                },
                                label = { Text(label) },
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFFD11013),
                                    selectedTextColor = Color(0xFFD11013),
                                    indicatorColor = Color(0xFFFFEAEA)
                                )
                            )
                        }
                    }
                    CopyrightFooter()
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "ScreenTransition",
            modifier = Modifier.padding(innerPadding)
        ) { screen ->
            when (screen) {
                is Screen.Home -> HomeScreen(
                    onAddToCart = { item -> addToCart(cart, item) },
                    onNavigateToCategory = { currentScreen = Screen.Categories }
                )
                is Screen.Categories -> CategoriesScreen(
                    onAddToCart = { item -> addToCart(cart, item) }
                )
                is Screen.Cart -> CartScreen(
                    cart = cart,
                    onNavigateToCheckout = { currentScreen = Screen.Checkout },
                    onUpdateQuantity = { item, delta -> updateCartQuantity(cart, item, delta) }
                )
                is Screen.Account -> AccountScreen()
                is Screen.Checkout -> CheckoutScreen(
                    cart = cart,
                    onBack = { currentScreen = Screen.Cart },
                    onConfirmOrder = { currentScreen = Screen.OrderSuccess }
                )
                is Screen.OrderSuccess -> OrderSuccessScreen(
                    onFinish = {
                        cart.clear()
                        currentScreen = Screen.Home
                    }
                )
            }
        }
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "House appliances" -> Icons.Default.Home
        "Work appliances" -> Icons.Default.Business
        "Office stationary" -> Icons.Default.Edit
        "Electronics" -> Icons.Default.Laptop
        "Motorvehicle parts" -> Icons.Default.DirectionsCar
        "Kitchen appliances" -> Icons.Default.Kitchen
        "Gaming equipment" -> Icons.Default.Gamepad
        "Tools & Hardware" -> Icons.Default.Build
        else -> Icons.Default.ShoppingBag
    }
}

private fun addToCart(cart: MutableList<CartItem>, item: InventoryItem) {
    val existingIndex = cart.indexOfFirst { it.item.id == item.id }
    if (existingIndex != -1) {
        cart[existingIndex] = cart[existingIndex].copy(quantity = cart[existingIndex].quantity + 1)
    } else {
        cart.add(CartItem(item, 1))
    }
}

private fun updateCartQuantity(cart: MutableList<CartItem>, cartItem: CartItem, delta: Int) {
    val index = cart.indexOf(cartItem)
    if (index != -1) {
        val newQty = cart[index].quantity + delta
        if (newQty <= 0) cart.removeAt(index)
        else cart[index] = cart[index].copy(quantity = newQty)
    }
}

// --- Screens ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAddToCart: (InventoryItem) -> Unit, onNavigateToCategory: () -> Unit) {
    val inventory = remember { getFullInventory() }
    val featuredItems = inventory.filter { it.stock > 40 }.take(4)
    val flashSales = inventory.filter { it.price < 20.0 }.take(4)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { CycadLogo() },
            actions = {
                IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White) }
                IconButton(onClick = {}) { Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1B1B))
        )

        MainBackgroundGradient(PaddingValues(0.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Flash Sale Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(150.dp).border(3.dp, Color.Black, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD11013)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Column {
                                Text("FLASH SALES", fontWeight = FontWeight.Black, color = Color.White, fontSize = 24.sp)
                                Text("UP TO 50% OFF EVERYTHING", color = Color.Yellow, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Surface(color = Color.White, shape = RoundedCornerShape(4.dp)) {
                                    Text("00h : 45m : 12s", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Yellow, modifier = Modifier.align(Alignment.BottomEnd).size(80.dp).alpha(0.3f))
                        }
                    }
                }

                // Quick Categories
                item {
                    Text("SHOP BY CATEGORY", fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val icons = listOf(Icons.Default.Kitchen, Icons.Default.Laptop, Icons.Default.DirectionsCar, Icons.Default.Business)
                        val names = listOf("Kitchen", "Electronics", "Auto", "Office")
                        items(4) { i ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToCategory() }) {
                                Box(modifier = Modifier.size(60.dp).background(Color.White, CircleShape).border(2.dp, Color.Black, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(icons[i], contentDescription = null, tint = Color(0xFFD11013))
                                }
                                Text(names[i], color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Recommended Section
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("RECOMMENDED FOR YOU", fontWeight = FontWeight.Black, color = Color.White)
                        Text("SEE ALL", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToCategory() })
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(featuredItems) { item ->
                            CompactProductCard(item, onAddToCart)
                        }
                    }
                }

                // Flash Sales Section
                item {
                    Text("FLASH SALES - LIMITED TIME", fontWeight = FontWeight.Black, color = Color.White)
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(flashSales) { item ->
                            CompactProductCard(item, onAddToCart)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(onAddToCart: (InventoryItem) -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "House appliances", "Work appliances", "Office stationary", "Electronics", "Motor-vehicle parts", "Kitchen appliances", "Gaming equipment", "Tools & Hardware")
    val inventory = remember { getFullInventory() }
    val filtered = inventory.filter { selectedCategory == "All" || it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("CATEGORIES", fontWeight = FontWeight.Black, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1B1B))
        )
        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar
            LazyColumn(
                modifier = Modifier.width(100.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.1f)),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(categories) { cat ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = cat }
                            .background(if (selectedCategory == cat) Color.Yellow else Color.Transparent)
                            .padding(12.dp)
                    ) {
                        Text(
                            cat.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (selectedCategory == cat) Color.Black else Color.White
                        )
                    }
                }
            }
            // Grid
            MainBackgroundGradient(PaddingValues(0.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered) { item ->
                        InventoryCardSmall(item, { onAddToCart(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun AccountScreen() {
    MainBackgroundGradient(PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("MY CYCAD ACCOUNT", fontWeight = FontWeight.Black, color = Color.White, fontSize = 24.sp)
            Spacer(Modifier.height(24.dp))
            AccountOption(Icons.AutoMirrored.Filled.ListAlt, "My Orders")
            AccountOption(Icons.Default.Favorite, "Wishlist")
            AccountOption(Icons.Default.CreditCard, "CycadPay / Wallet")
            AccountOption(Icons.Default.LocationOn, "Address Book")
            AccountOption(Icons.Default.Settings, "Settings")
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("LOGOUT", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun AccountOption(icon: ImageVector, label: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Gray)
            Spacer(Modifier.width(16.dp))
            Text(label, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun CompactProductCard(item: InventoryItem, onAddToCart: (InventoryItem) -> Unit) {
    Card(
        modifier = Modifier.width(140.dp).border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp).background(item.color.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(item.category),
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
            Text("$${item.price}", fontWeight = FontWeight.Black, color = Color.Red, fontSize = 14.sp)
            Button(
                onClick = { onAddToCart(item) },
                modifier = Modifier.fillMaxWidth().height(32.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055BF))
            ) {
                Text("ADD", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun InventoryCardSmall(item: InventoryItem, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).background(item.color.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(item.category),
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(item.company.uppercase(), fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(item.name, fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("$${item.price}", fontWeight = FontWeight.Black, color = Color(0xFFD11013))
            IconButton(onClick = onAddToCart, modifier = Modifier.align(Alignment.End).size(32.dp).background(Color(0xFF0055BF), CircleShape)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cart: List<CartItem>,
    onNavigateToCheckout: () -> Unit,
    onUpdateQuantity: (CartItem, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("CART (${cart.sumOf { it.quantity }})", fontWeight = FontWeight.Black, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1B1B))
        )
        MainBackgroundGradient(PaddingValues(0.dp)) {
            if (cart.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.White.copy(alpha = 0.5f))
                    Text("YOUR CART IS EMPTY!", color = Color.White, fontWeight = FontWeight.Black)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(cart) { item -> CartCard(item, onUpdateQuantity) }
                }
                Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        HorizontalDivider(thickness = 4.dp, color = Color.Black)
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", fontWeight = FontWeight.Bold)
                                Text("$${"%.2f".format(cart.sumOf { it.item.price * it.quantity })}", fontWeight = FontWeight.Black, color = Color.Red, fontSize = 20.sp)
                            }
                            Button(
                                onClick = onNavigateToCheckout,
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(56.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00852B)),
                                border = BorderStroke(3.dp, Color.Black)
                            ) {
                                Text("CHECKOUT NOW", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cart: List<CartItem>,
    onBack: () -> Unit,
    onConfirmOrder: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Address, 2: Delivery, 3: Payment
    var address by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CHECKOUT - STEP $step/3", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1B1B))
            )
        }
    ) { innerPadding ->
        MainBackgroundGradient(innerPadding) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when(step) {
                    1 -> {
                        Text("SHIPPING ADDRESS", fontWeight = FontWeight.Black, color = Color.White)
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)),
                            placeholder = { Text("Enter your full address...") }
                        )
                        Button(onClick = { step = 2 }, enabled = address.isNotBlank(), modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black)) {
                            Text("PROCEED TO DELIVERY", fontWeight = FontWeight.Black)
                        }
                    }
                    2 -> {
                        Text("DELIVERY METHOD", fontWeight = FontWeight.Black, color = Color.White)
                        DeliveryOption("Standard Delivery", "Est. arrival: 2-3 days", "$5.00")
                        DeliveryOption("Cycad Express", "Est. arrival: Today", "$15.00")
                        Button(onClick = { step = 3 }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black)) {
                            Text("PROCEED TO PAYMENT", fontWeight = FontWeight.Black)
                        }
                    }
                    3 -> {
                        Text("PAYMENT METHOD", fontWeight = FontWeight.Black, color = Color.White)
                        PaymentOption("Cash on Delivery", Icons.Default.Money)
                        PaymentOption("Credit/Debit Card", Icons.Default.CreditCard)
                        PaymentOption("Cycad Wallet", Icons.Default.Wallet)
                        Spacer(Modifier.weight(1f))
                        Button(onClick = onConfirmOrder, modifier = Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                            Text("CONFIRM ORDER", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryOption(title: String, desc: String, price: String) {
    Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(2.dp, Color.Black, RoundedCornerShape(8.dp)), color = Color.White, shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(desc, fontSize = 12.sp, color = Color.Gray)
            }
            Text(price, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PaymentOption(title: String, icon: ImageVector) {
    Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(2.dp, Color.Black, RoundedCornerShape(8.dp)), color = Color.White, shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            RadioButton(selected = false, onClick = {})
        }
    }
}

@Composable
fun OrderSuccessScreen(onFinish: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1B1B1B)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF00852B), CircleShape)
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp))
            }
            Spacer(Modifier.height(32.dp))
            Text("MISSION SUCCESSFUL", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Your CYCAD order is being processed and will be delivered shortly.", color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            
            Spacer(Modifier.height(64.dp))
            
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                border = BorderStroke(3.dp, Color.Black)
            ) {
                Text("RETURN TO BASE", fontWeight = FontWeight.Black)
            }
        }
    }
}

// --- Shared UI Components ---

@Composable
fun MainBackgroundGradient(innerPadding: PaddingValues, content: @Composable ColumnScope.() -> Unit) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1B1B1B), Color(0xFF89CFF0))
    )
    Column(
        modifier = Modifier.padding(innerPadding).fillMaxSize().background(backgroundGradient),
        content = content
    )
}

@Composable
fun CartCard(cartItem: CartItem, onUpdateQuantity: (CartItem, Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(3.dp, Color.Black, RoundedCornerShape(8.dp))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(cartItem.item.color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .border(2.dp, Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(cartItem.item.category),
                    contentDescription = null,
                    tint = cartItem.item.color,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cartItem.item.name, fontWeight = FontWeight.Black)
                Text("$${"%.2f".format(cartItem.item.price)} each", fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onUpdateQuantity(cartItem, -1) }) { Icon(Icons.Default.Remove, contentDescription = "Decrease") }
                Text(cartItem.quantity.toString(), fontWeight = FontWeight.Black, fontSize = 18.sp)
                IconButton(onClick = { onUpdateQuantity(cartItem, 1) }) { Icon(Icons.Default.Add, contentDescription = "Increase") }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1B1B1B)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CycadLogoLarge()
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text("WE HAVE WHAT YOU NEED", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelLarge, letterSpacing = 4.sp, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(modifier = Modifier.width(200.dp).clip(RoundedCornerShape(4.dp)), color = Color(0xFFD11013), trackColor = Color.White.copy(alpha = 0.1f))
        }
    }
}

@Composable
fun CycadLogoLarge() {
    Box(
        modifier = Modifier.background(Color(0xFFD11013), RoundedCornerShape(8.dp)).padding(horizontal = 24.dp, vertical = 8.dp)
            .border(4.dp, Color.White, RoundedCornerShape(8.dp)).shadow(12.dp, RoundedCornerShape(8.dp))
    ) {
        Text("CYCAD", color = Color.White, fontWeight = FontWeight.Black, fontSize = 34.sp, letterSpacing = 0.sp)
    }
}

@Composable
fun CycadLogo() {
    Box(
        modifier = Modifier.background(Color(0xFFD11013), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
            .border(2.dp, Color.White, RoundedCornerShape(4.dp))
    ) {
        Text("CYCAD", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 0.sp)
    }
}

@Composable
fun CopyrightFooter() {
    Surface(color = Color(0xFF1B1B1B), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "WE HAVE WHAT YOU NEED", color = Color.Yellow, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(text = "© 2026 CYCAD GENERAL STORE.", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            Text(text = "STANDARD PROTOTYPE", color = Color.Red, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

fun getFullInventory(): List<InventoryItem> = listOf(
    // House appliances
    InventoryItem(1, "Robot Vacuum", 299.99, 15, "Self-docking smart cleaner", "CYCAD Home", "House appliances", Color(0xFFD11013)),
    InventoryItem(2, "Air Purifier", 120.00, 20, "HEAP filter air cleaning", "PureBreeze", "House appliances", Color(0xFF0055BF)),
    InventoryItem(3, "Humidifier", 45.00, 50, "Quiet mist cool air", "Misty", "House appliances", Color(0xFFF6D012)),
    InventoryItem(4, "Smart Door Lock", 180.00, 30, "Keyless entry system", "SecureBlock", "House appliances", Color(0xFF00852B)),
    InventoryItem(5, "Electric Heater", 65.00, 10, "Fast heating portable unit", "WarmStep", "House appliances", Color(0xFF1B1B1B)),
    InventoryItem(6, "Window Cleaner Bot", 210.00, 5, "Suction-based glass cleaner", "ClearView", "House appliances", Color(0xFFA5A5A5)),

    // Work appliances
    InventoryItem(7, "Industrial Shredder", 450.00, 8, "Cross-cut paper shredder", "WorkForce", "Work appliances", Color(0xFFD11013)),
    InventoryItem(8, "Large Format Printer", 899.00, 3, "High-res poster printer", "PrintMaster", "Work appliances", Color(0xFF0055BF)),
    InventoryItem(9, "Label Maker", 35.00, 100, "Handheld thermal printer", "StickIt", "Work appliances", Color(0xFFF6D012)),
    InventoryItem(10, "Heavy Duty Stapler", 25.00, 40, "Staples up to 100 sheets", "Bind", "Work appliances", Color(0xFF00852B)),
    InventoryItem(11, "Laminator", 60.00, 25, "Hot and cold lamination", "Shield", "Work appliances", Color(0xFF1B1B1B)),
    InventoryItem(12, "Paper Cutter", 50.00, 15, "Precision guillotine cutter", "TrimLine", "Work appliances", Color(0xFFA5A5A5)),

    // Office stationery
    InventoryItem(13, "Premium Pen Set", 15.00, 200, "Smooth gel ink pens", "WriteWell", "Office stationary", Color(0xFFD11013)),
    InventoryItem(14, "Spiral Notebook", 4.99, 500, "100 sheets lined paper", "NoteIt", "Office stationary", Color(0xFF0055BF)),
    InventoryItem(15, "Desk Organizer", 12.50, 60, "Mesh multi-slot holder", "TidyUp", "Office stationary", Color(0xFFF6D012)),
    InventoryItem(16, "Sticky Notes (10pk)", 8.00, 300, "Multi-color post-its", "MarkIt", "Office stationary", Color(0xFF00852B)),
    InventoryItem(17, "Highlighter Pack", 6.00, 150, "Neon chisel tip markers", "Glow", "Office stationary", Color(0xFF1B1B1B)),
    InventoryItem(18, "File Folders (50pk)", 10.00, 80, "Letter size manila folders", "Archivist", "Office stationary", Color(0xFFA5A5A5)),

    // Electronics
    InventoryItem(19, "4K Monitor", 350.00, 25, "27-inch IPS display", "PixelPro", "Electronics", Color(0xFFD11013)),
    InventoryItem(20, "Wireless Keyboard", 45.00, 80, "Quiet-touch keys", "Clicky", "Electronics", Color(0xFF0055BF)),
    InventoryItem(21, "Bluetooth Speaker", 55.00, 120, "Waterproof outdoor sound", "BoomBox", "Electronics", Color(0xFFF6D012)),
    InventoryItem(22, "Power Bank 20Ah", 30.00, 200, "Fast charging dual port", "JuiceUp", "Electronics", Color(0xFF00852B)),
    InventoryItem(23, "Smart Watch", 199.00, 45, "Fitness and health tracking", "CYCAD Tech", "Electronics", Color(0xFF1B1B1B)),
    InventoryItem(24, "Noise Buds", 129.00, 60, "Active noise cancellation", "SilentSound", "Electronics", Color(0xFFA5A5A5)),

    // Motorvehicle parts
    InventoryItem(25, "Brake Pads Set", 85.00, 40, "Ceramic heavy duty pads", "StopSafe", "Motorvehicle parts", Color(0xFFD11013)),
    InventoryItem(26, "LED Headlights", 60.00, 30, "Ultra-bright white light", "BrightWay", "Motorvehicle parts", Color(0xFF0055BF)),
    InventoryItem(27, "Oil Filter", 12.00, 150, "High efficiency filtration", "FlowClean", "Motor-vehicle parts", Color(0xFFF6D012)),
    InventoryItem(28, "Car Battery", 140.00, 20, "12V long life battery", "PowerDrive", "Motorvehicle parts", Color(0xFF00852B)),
    InventoryItem(29, "Wiper Blades", 25.00, 100, "All-weather silicone", "Sweep", "Motorvehicle parts", Color(0xFF1B1B1B)),
    InventoryItem(30, "Air Filter", 18.00, 75, "Engine intake protection", "Breathe", "Motorvehicle parts", Color(0xFFA5A5A5)),

    // Kitchen appliances
    InventoryItem(31, "Espresso Machine", 499.00, 10, "15-bar professional pump", "BrewBean", "Kitchen appliances", Color(0xFFD11013)),
    InventoryItem(32, "Air Fryer", 89.00, 35, "5-quart oil-free cooker", "Crispy", "Kitchen appliances", Color(0xFF0055BF)),
    InventoryItem(33, "Electric Kettle", 40.00, 60, "Stainless steel 1.7L", "QuickBoil", "Kitchen appliances", Color(0xFFF6D012)),
    InventoryItem(34, "Toaster 4-Slice", 50.00, 40, "Wide slot with bagel mode", "ToastIt", "Kitchen appliances", Color(0xFF00852B)),
    InventoryItem(35, "Hand Blender", 30.00, 80, "2-speed immersion stick", "MixMaster", "Kitchen appliances", Color(0xFF1B1B1B)),
    InventoryItem(36, "Microwave Oven", 110.00, 15, "1000W countertop unit", "HeatWave", "Kitchen appliances", Color(0xFFA5A5A5)),

    // Gaming equipment
    InventoryItem(37, "Mechanical Board", 120.00, 40, "RGB Blue-switch keys", "GameClick", "Gaming equipment", Color(0xFFD11013)),
    InventoryItem(38, "Gaming Mouse", 70.00, 90, "16000 DPI optical sensor", "SwiftMove", "Gaming equipment", Color(0xFF0055BF)),
    InventoryItem(39, "Gaming Headset", 95.00, 50, "7.1 surround sound mic", "SonicEdge", "Gaming equipment", Color(0xFFF6D012)),
    InventoryItem(40, "XL Mousepad", 25.00, 150, "Stitched edge fabric", "Glide", "Gaming equipment", Color(0xFF00852B)),
    InventoryItem(41, "Controller", 65.00, 70, "Wireless haptic feedback", "PlayStick", "Gaming equipment", Color(0xFF1B1B1B)),
    InventoryItem(42, "VR Headset", 399.00, 12, "Standalone virtual reality", "DeepDive", "Gaming equipment", Color(0xFFA5A5A5)),

    // Tools & Hardware
    InventoryItem(43, "Power Drill", 120.00, 20, "20V brushless motor", "FixIt", "Tools & Hardware", Color(0xFFD11013)),
    InventoryItem(44, "Screwdriver Set", 25.00, 100, "24-piece magnetic tips", "TurnIt", "Tools & Hardware", Color(0xFF0055BF)),
    InventoryItem(45, "Tape Measure", 10.00, 200, "25ft heavy duty tape", "MeasureUp", "Tools & Hardware", Color(0xFFF6D012)),
    InventoryItem(46, "Hammer (Steel)", 15.00, 80, "Forged one-piece steel", "Bash", "Tools & Hardware", Color(0xFF00852B)),
    InventoryItem(47, "Toolbox (Large)", 45.00, 30, "Double-layer organizer", "StoreAll", "Tools & Hardware", Color(0xFF1B1B1B)),
    InventoryItem(48, "Adjustable Wrench", 20.00, 60, "Wide jaw chrome finish", "Tighten", "Tools & Hardware", Color(0xFFA5A5A5))
)

@Preview(showBackground = true)
@Composable
fun HomePreview() { CYCADTheme { MainAppFlow() } }
