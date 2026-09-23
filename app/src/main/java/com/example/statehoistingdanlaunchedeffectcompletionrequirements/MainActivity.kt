package com.example.statehoistingdanlaunchedeffectcompletionrequirements
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Enum untuk mengelola Status Pemesanan
enum class OrderStatus {
    IDLE, ERROR, PROCESSING, SUCCESS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TicketScreen()
                }
            }
        }
    }
}

@Composable
fun TicketScreen() {
    // --- STATE HOISTING (Dikelola oleh Parent) ---
    // 1. Harga Tiket (State)
    var hargaTiket by remember { mutableIntStateOf(50000) } // Misal harga per tiket 50.000

    // 2. Jumlah Tiket (State)
    var jumlahTiket by remember { mutableIntStateOf(1) }

    // 3. Nama Pembeli Tiket (State)
    var namaPembeli by remember { mutableStateOf("") }

    // State tambahan untuk mengatur status antarmuka
    var orderStatus by remember { mutableStateOf(OrderStatus.IDLE) }

    // --- LAUNCHED EFFECT ---
    // Menjalankan proses asinkron saat orderStatus berubah menjadi PROCESSING
    LaunchedEffect(orderStatus) {
        if (orderStatus == OrderStatus.PROCESSING) {
            // Simulasi proses selama 5 detik
            delay(5000L)
            // Setelah 5 detik, muncul lagi dengan status pemesanan berhasil
            orderStatus = OrderStatus.SUCCESS
        }
    }

    // Memanggil Child Composable dan melempar state ke bawah
    TicketContent(
        namaPembeli = namaPembeli,
        onNamaChange = {
            namaPembeli = it
            // Hilangkan error merah jika user mulai mengetik lagi
            if (orderStatus == OrderStatus.ERROR) orderStatus = OrderStatus.IDLE
        },
        jumlahTiket = jumlahTiket,
        onJumlahChange = { jumlahTiket = it },
        orderStatus = orderStatus,
        onPesanClick = {
            if (namaPembeli.isBlank()) {
                orderStatus = OrderStatus.ERROR // Validasi: Nama Kosong
            } else {
                orderStatus = OrderStatus.PROCESSING // Masuk ke proses pemesanan
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketContent(
    namaPembeli: String,
    onNamaChange: (String) -> Unit,
    jumlahTiket: Int,
    onJumlahChange: (Int) -> Unit,
    orderStatus: OrderStatus,
    onPesanClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pemesanan Tiket", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF195BB4) // Warna biru sesuai gambar
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form: Nama
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Nama", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = namaPembeli,
                    onValueChange = onNamaChange,
                    placeholder = { Text("Masukkan nama Anda") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = orderStatus != OrderStatus.PROCESSING // Disable saat proses
                )
            }

            // Form: Jumlah Tiket
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Jumlah Tiket", fontWeight = FontWeight.Bold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tombol Minus
                    FilledTonalIconButton(
                        onClick = { if (jumlahTiket > 1) onJumlahChange(jumlahTiket - 1) },
                        enabled = orderStatus != OrderStatus.PROCESSING && jumlahTiket > 1,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    // Teks Jumlah Tiket
                    Text(
                        text = jumlahTiket.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Tombol Plus
                    FilledTonalIconButton(
                        onClick = { onJumlahChange(jumlahTiket + 1) },
                        enabled = orderStatus != OrderStatus.PROCESSING,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Pesan
            Button(
                onClick = onPesanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = orderStatus != OrderStatus.PROCESSING,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF195BB4),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("Pesan Tiket", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Komponen Status Card di paling bawah
            StatusCard(orderStatus = orderStatus)
        }
    }
}

@Composable
fun StatusCard(orderStatus: OrderStatus) {
    // Menentukan warna dan teks sesuai dengan state Enum saat ini
    val (backgroundColor, contentColor, statusText) = when (orderStatus) {
        OrderStatus.IDLE -> Triple(
            Color(0xFFF3F4F6), Color(0xFF4B5563), "Status: Silakan pesan tiket"
        )
        OrderStatus.PROCESSING -> Triple(
            Color(0xFFEBF5FF), Color(0xFF1E429F), "Status: Memproses pesanan........."
        )
        OrderStatus.SUCCESS -> Triple(
            Color(0xFFDEF7EC), Color(0xFF03543F), "Status: Tiket telah dipesan"
        )
        OrderStatus.ERROR -> Triple(
            Color(0xFFFDE8E8), Color(0xFF9B1C1C), "Status: Nama Masih Kosong"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon Sesuai Status
        when (orderStatus) {
            OrderStatus.IDLE -> { /* Tanpa Icon */ }
            OrderStatus.PROCESSING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            }
            OrderStatus.SUCCESS -> {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Sukses", tint = Color(0xFF0E9F6E))
            }
            OrderStatus.ERROR -> {
                Icon(Icons.Filled.Warning, contentDescription = "Error", tint = Color(0xFFF05252))
            }
        }

        Text(
            text = statusText,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}