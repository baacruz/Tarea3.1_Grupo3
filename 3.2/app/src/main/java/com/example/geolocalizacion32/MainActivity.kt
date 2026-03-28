package com.example.geolocalizacion32

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.geolocalizacion32.Configuracion.Gabinetes
import com.example.geolocalizacion32.ViewModel.GabineteViewModel
import com.example.geolocalizacion32.ui.theme.Geolocalizacion32Theme
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { }

        enableEdgeToEdge()
        setContent {
            Geolocalizacion32Theme {
                Navigation()
            }
        }
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val gabineteViewModel: GabineteViewModel = viewModel()
    
    NavHost(navController = navController, startDestination = "map") {
        composable("map") { MapScreen(navController, gabineteViewModel) }
        composable("lista") { ListaGabinetesScreen(navController, gabineteViewModel) }
    }
}

@Composable
fun MapScreen(navController: NavController, viewModel: GabineteViewModel) {
    var showDetail by remember { mutableStateOf(false) }
    var selectedGabineteForDetail by remember { mutableStateOf<Gabinetes?>(null) }
    var showEditDialogFromMap by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshGabinetes()
    }

    if (showEditDialogFromMap && selectedGabineteForDetail != null) {
        GabineteFormDialog(
            gabinete = selectedGabineteForDetail,
            onDismiss = { 
                showEditDialogFromMap = false 
                selectedGabineteForDetail = null
            },
            onSave = { nombre, direccion, lat, lon, sub, gate ->
                viewModel.updateGabinete(selectedGabineteForDetail!!.id, nombre, direccion, lat, lon, sub, gate)
                showEditDialogFromMap = false
                selectedGabineteForDetail = null
                showDetail = false
            }
        )
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, "map") }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            LocationPermissionWrapper {
                RealMapScreen(
                    gabinetes = viewModel.gabinetesList,
                    onMarkerClick = { gabinete ->
                        selectedGabineteForDetail = gabinete
                        showDetail = true
                    }
                )
            }

            if (showDetail && selectedGabineteForDetail != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedGabineteForDetail?.nombre ?: "Detalle", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            IconButton(onClick = { showDetail = false }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        DetailRow(label = "Dirección", value = selectedGabineteForDetail?.direccion ?: "")
                        DetailRow(label = "Latitud", value = selectedGabineteForDetail?.latitud ?: "")
                        DetailRow(label = "Longitud", value = selectedGabineteForDetail?.longitud ?: "")

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { showEditDialogFromMap = true }, 
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Editar")
                            }
                            Button(
                                onClick = { showDetail = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D537B))
                            ) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealMapScreen(gabinetes: List<Gabinetes>, onMarkerClick: (Gabinetes) -> Unit) {
    val context = LocalContext.current
    val spsLocation = LatLng(15.5513, -88.0128)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(spsLocation, 13f)
    }

    val uiSettings by remember { mutableStateOf(MapUiSettings(myLocationButtonEnabled = true)) }
    val properties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true)) }

    var customIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    LaunchedEffect(gabinetes) {
        try {
            customIcon = bitmapDescriptorFromVector(context, R.drawable.ic_gabinete)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings
    ) {
        Marker(
            state = rememberMarkerState(position = spsLocation),
            title = "Ubicación Actual",
            snippet = "San Pedro Sula",
        )

        gabinetes.forEach { gabinete ->
            val lat = gabinete.latitud?.toDoubleOrNull() ?: 0.0
            val lon = gabinete.longitud?.toDoubleOrNull() ?: 0.0
            if (lat != 0.0 && lon != 0.0) {
                Marker(
                    state = rememberMarkerState(position = LatLng(lat, lon)),
                    title = gabinete.nombre,
                    icon = customIcon,
                    onClick = {
                        onMarkerClick(gabinete)
                        true
                    }
                )
            }
        }
    }
}

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bm = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bm)
    drawable.draw(canvas)
    return try {
        BitmapDescriptorFactory.fromBitmap(bm)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ListaGabinetesScreen(navController: NavController, viewModel: GabineteViewModel) {
    val gabinetesList = viewModel.gabinetesList
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedGabinete by remember { mutableStateOf<Gabinetes?>(null) }
    
    var isDeleteMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var gabineteToDelete by remember { mutableStateOf<Gabinetes?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshGabinetes()
    }

    val filteredGabinetes = if (searchQuery.isEmpty()) {
        gabinetesList
    } else {
        gabinetesList.filter { it.nombre?.contains(searchQuery, ignoreCase = true) == true }
    }

    if (showDialog) {
        GabineteFormDialog(
            gabinete = selectedGabinete,
            onDismiss = { 
                showDialog = false 
                selectedGabinete = null
            },
            onSave = { nombre, direccion, lat, lon, sub, gate ->
                if (selectedGabinete == null) {
                    viewModel.createGabinete(nombre, direccion, lat, lon, sub, gate)
                } else {
                    viewModel.updateGabinete(selectedGabinete!!.id, nombre, direccion, lat, lon, sub, gate)
                }
                showDialog = false
                selectedGabinete = null
            }
        )
    }
    
    if (showDeleteConfirm && gabineteToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirm = false 
                gabineteToDelete = null
            },
            title = { Text("Eliminar Gabinete") },
            text = { Text("¿Estás seguro de eliminar a ${gabineteToDelete?.nombre}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGabinete(gabineteToDelete!!.id)
                        showDeleteConfirm = false
                        gabineteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteConfirm = false 
                    gabineteToDelete = null
                }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, "lista") },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(
                    onClick = { isDeleteMode = !isDeleteMode },
                    containerColor = if (isDeleteMode) Color.Gray else Color.Red,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(if (isDeleteMode) Icons.Default.Close else Icons.Default.Remove, contentDescription = "Modo Eliminar")
                }

                FloatingActionButton(
                    onClick = { 
                        selectedGabinete = null
                        showDialog = true 
                    },
                    containerColor = Color(0xFF5D537B),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Gabinete")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            Text("Mapa Gabinetes", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar gabinete por nombre") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF1F1F1)),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredGabinetes) { gabinete ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (!isDeleteMode) {
                                            selectedGabinete = gabinete
                                            showDialog = true
                                        }
                                    }
                            ) {
                                Text(gabinete.nombre ?: "Sin nombre", fontWeight = FontWeight.Bold)
                                Text(gabinete.direccion ?: "Sin dirección", fontSize = 12.sp, color = Color.Gray)
                                Text("Lat: ${gabinete.latitud ?: "0.0"}, Lon: ${gabinete.longitud ?: "0.0"}", fontSize = 10.sp, color = Color.LightGray)
                            }
                            
                            if (isDeleteMode) {
                                IconButton(onClick = {
                                    gabineteToDelete = gabinete
                                    showDeleteConfirm = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GabineteFormDialog(
    gabinete: Gabinetes?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf(gabinete?.nombre ?: "") }
    var direccion by remember { mutableStateOf(gabinete?.direccion ?: "") }
    var latitud by remember { mutableStateOf(gabinete?.latitud ?: "") }
    var longitud by remember { mutableStateOf(gabinete?.longitud ?: "") }
    var submask by remember { mutableStateOf(gabinete?.submask ?: "") }
    var gateway by remember { mutableStateOf(gabinete?.gateway ?: "") }
    
    var showMapPicker by remember { mutableStateOf(false) }

    if (showMapPicker) {
        val initialLat = latitud.toDoubleOrNull() ?: 15.5513
        val initialLon = longitud.toDoubleOrNull() ?: -88.0128
        val pickerLocation = LatLng(initialLat, initialLon)
        val cameraState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(pickerLocation, 15f)
        }
        var markerState = rememberMarkerState(position = pickerLocation)

        AlertDialog(
            onDismissRequest = { showMapPicker = false },
            title = { Text("Seleccionar Ubicación") },
            text = {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState,
                        onMapClick = { newPos ->
                            markerState.position = newPos
                        }
                    ) {
                        Marker(state = markerState)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    latitud = markerState.position.latitude.toString()
                    longitud = markerState.position.longitude.toString()
                    showMapPicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showMapPicker = false }) { Text("Cancelar") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (gabinete == null) "Agregar Nuevo Gabinete" else "Editar Gabinete") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = latitud, onValueChange = { latitud = it }, label = { Text("Latitud") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = longitud, onValueChange = { longitud = it }, label = { Text("Longitud") }, modifier = Modifier.fillMaxWidth())
                    }
                    IconButton(
                        onClick = { showMapPicker = true },
                        modifier = Modifier.padding(start = 8.dp).size(50.dp).background(Color(0xFF5D537B), CircleShape)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Pick on map", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = submask, onValueChange = { submask = it }, label = { Text("Submask") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = gateway, onValueChange = { gateway = it }, label = { Text("Gateway") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nombre.trim().isNotEmpty()) {
                    onSave(nombre, direccion, latitud, longitud, submask, gateway)
                } else {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = currentRoute == "map",
            onClick = { navController.navigate("map") },
            icon = { Icon(Icons.Default.Map, "Mapa") },
            label = { Text("Mapa") }
        )
        NavigationBarItem(
            selected = currentRoute == "lista",
            onClick = { navController.navigate("lista") },
            icon = { Icon(Icons.Default.List, "Lista") },
            label = { Text("Lista") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Person, "Perfil") },
            label = { Text("Perfil") }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text(value, modifier = Modifier.weight(1f))
    }
}

@Composable
fun LocationPermissionWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (hasLocationPermission) {
        content()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se requieren permisos de ubicación para ver el mapa.")
        }
    }
}
