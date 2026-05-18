package com.cycad.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycad.app.ui.theme.CYCADTheme
import kotlinx.coroutines.delay

// Data model for the store inventory
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
                        StoreApp()
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1B1B)), // Soft Black background
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Silhouette-inspired placeholder or logo area
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CycadLegoLogoLarge()
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "WE HAVE WHAT YOU NEED!!!",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Light
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LinearProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFD11013), // Lego Red
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
        
        // Bottom Branding
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)) {
            Text(
                text = "CYCAD STORE PROTOCOL v1.0",
                color = Color.Red.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CycadLegoLogoLarge() {
    Box(
        modifier = Modifier
            .background(Color(0xFFD11013), RoundedCornerShape(8.dp))
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .border(4.dp, Color.White, RoundedCornerShape(8.dp))
            .shadow(12.dp, RoundedCornerShape(8.dp))
    ) {
        Text(
            text = "CYCAD STORE",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            letterSpacing = (-2).sp
        )
    }
}

@Composable
fun CycadLegoLogo() {
    Box(
        modifier = Modifier
            .background(Color(0xFFD11013), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .border(2.dp, Color.White, RoundedCornerShape(4.dp))
    ) {
        Text(
            text = "CYCAD STORE",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            letterSpacing = (-1).sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreApp() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var cartCount by remember { mutableIntStateOf(0) }
    
    val categories = listOf(
        "All", "House appliances", "Work appliances", "Office stationary", 
        "Electronics", "Motorvehicle parts", "Kitchen appliances", "Gaming equipment", "Tools & Hardware"
    )

    val inventory = remember {
        listOf(
            // House appliances
            InventoryItem(1, "Robot Vacuum", 299.99, 15, "Self-docking smart cleaner", "CYCAD Home", "House appliances", Color(0xFFD11013)),
            InventoryItem(2, "Air Purifier", 120.00, 20, "HEPA filter air cleaning", "PureBreeze", "House appliances", Color(0xFF0055BF)),
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

            // Office stationary
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
            InventoryItem(27, "Oil Filter", 12.00, 150, "High efficiency filtration", "FlowClean", "Motorvehicle parts", Color(0xFFF6D012)),
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
    }

    val filteredItems = remember(searchQuery, selectedCategory) {
        inventory.filter { 
            (selectedCategory == "All" || it.category == selectedCategory) &&
            (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) || it.company.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF1B1B1B))) { // Soft Black Top
                TopAppBar(
                    title = { CycadLegoLogo() },
                    actions = {
                        IconButton(onClick = { }) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(containerColor = Color.Yellow, contentColor = Color.Black) { 
                                            Text(cartCount.toString(), fontWeight = FontWeight.Bold) 
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
                
                // Search Bar in a "Block"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(3.dp, Color.Black, RoundedCornerShape(8.dp)),
                        placeholder = { Text("Search CYCAD Bricks...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }

                // Sub-categorization Scrollable List
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            name = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }
        },
        bottomBar = {
            CopyrightFooter()
        }
    ) { innerPadding ->
        // Gradient Background: Soft Black to Baby Blue
        val backgroundGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1B1B1B), // Soft Black
                Color(0xFF89CFF0)  // Baby Blue
            )
        )

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            InventoryList(
                items = filteredItems,
                onAddToCart = { cartCount++ }
            )
        }
    }
}

@Composable
fun CategoryChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                width = 2.dp,
                color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            ),
        color = if (isSelected) Color(0xFFD11013) else Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = name.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun InventoryList(items: List<InventoryItem>, onAddToCart: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (items.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Out of Bricks!", fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        }
        items(items) { item ->
            InventoryCard(item, onAddToCart)
        }
    }
}

@Composable
fun InventoryCard(item: InventoryItem, onAddToCart: () -> Unit) {
    // A Card that looks like a Lego Block
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(3.dp, Color.Black, RoundedCornerShape(8.dp))
    ) {
        Column {
            // "Studs" on top of the block feel (visual decoration)
            Row(modifier = Modifier.padding(start = 12.dp, top = 8.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(item.color.copy(alpha = 0.2f))
                            .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
            
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator block
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(item.color, RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .shadow(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.category.take(1).uppercase(),
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.company.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "$${"%.2f".format(item.price)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD11013)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (item.stock > 0) "QTY: ${item.stock}" else "SOLD OUT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (item.stock > 0) Color.Black else Color.Red
                    )
                    
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.padding(top = 8.dp),
                        enabled = item.stock > 0,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0055BF),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, Color.Black),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("GRAB", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun CopyrightFooter() {
    Surface(
        color = Color(0xFF1B1B1B), // Soft Black
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WE HAVE WHAT YOU NEED",
                color = Color.Yellow,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "© 2026 CYCAD STORE. Not affiliated with LEGO or PEARSON Group.",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "PROTOTYPE v1.0 - CONFIDENTIAL",
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingPreview() {
    CYCADTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun StorePreview() {
    CYCADTheme {
        StoreApp()
    }
}
