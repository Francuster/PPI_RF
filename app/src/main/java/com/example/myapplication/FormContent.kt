package com.example.myapplication
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun MainContent() {
    var showForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botón para mostrar el formulario
        Button(
            onClick = {
                showForm = !showForm
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Mostrar formulario")
        }

        // Mostrar formulario si showForm es verdadero
        if (showForm) {
            Box(
                modifier = Modifier
                    .padding(5.dp) // Agrega espacio alrededor del contenedor
                    .clip(RoundedCornerShape(16.dp)) // Define bordes redondeados
                    .background(Color(0xFFC7DEE1)) // Color de fondo celeste (Light Blue)
                    .padding(2.dp) // Espacio interno
            ) {
                Formulario ({ nombre, mail, dni, categoria ->
                    // Aquí puedes manejar los datos del formulario (name y email)
                    println("NOMBRE VISITANTE: $nombre, MAIL: $mail, DNI: $dni, TIPO DE CUENTA: $categoria")

                    // Opcionalmente, puedes cerrar el formulario después de enviarlo
                    showForm = false
                }, {showForm = false})
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Formulario(onSubmit: (String, String, String, String) -> Unit, onBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    val categorias = listOf("Visitante", "Profesor", "Alumno")
    var mail by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val Inter = FontFamily(
        Font(resId = R.font.inter_medium), // Fuente regular
        Font(resId = R.font.inter_bold, weight = FontWeight.Bold) // Fuente en negrita
    )

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.arrow_left),
            contentDescription = "arrow left",
            modifier = Modifier
                .size(30.dp)
                .padding(start = 0.dp)
                .clickable{
                    // Aquí puedes agregar el código que deseas ejecutar cuando el icono se haga clic
                    onBack()
                },
            tint = Color(0xFF7D7A73)
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "REGISTRO\nINGRESO/EGRESO",
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF7D7A73)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para el nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.usuario),
                        contentDescription = "usuario",
                        modifier = Modifier
                            .size(25.dp),
                        tint = Color(0xFF7D7A73)
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                    Text(
                        "NOMBRE VISITANTE",
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF7D7A73)
                        ),
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF77BBC6), shape = RoundedCornerShape(12.dp)), // Color celeste y bordes redondeados
            shape = RoundedCornerShape(12.dp), // Bordes redondeados
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black, // Color negro para el texto cuando está enfocado
                unfocusedTextColor = Color.Black, // Color de fondo celeste
                focusedBorderColor = Color(0xFF77BBC6), // Color azul (por ejemplo)
                unfocusedBorderColor = Color(0xFF77BBC6)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para mail
        OutlinedTextField(
            value = mail,
            onValueChange = { mail = it },
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.mail),
                        contentDescription = "mail",
                        modifier = Modifier.size(25.dp),
                        tint = Color(0xFF7D7A73)
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                    Text(
                        "MAIL",
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF7D7A73)
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth() // Color celeste y bordes redondeados
                .background(Color(0xFF77BBC6), shape = RoundedCornerShape(12.dp)), // Color celeste y bordes redondeados
            shape = RoundedCornerShape(12.dp), // Bordes redondeados
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black, // Color negro para el texto cuando está enfocado
                unfocusedTextColor = Color.Black, // Color de fondo celeste
                focusedBorderColor = Color(0xFF77BBC6), // Color azul (por ejemplo)
                unfocusedBorderColor = Color(0xFF77BBC6)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para mail
        OutlinedTextField(
            value = dni,
            onValueChange = { dni = it },
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.categoria),
                        contentDescription = "categoria",
                        modifier = Modifier.size(25.dp),
                        tint = Color(0xFF7D7A73)
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                    Text(
                        "DNI",
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF7D7A73)
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth() // Color celeste y bordes redondeados
                .background(Color(0xFF77BBC6), shape = RoundedCornerShape(12.dp)), // Color celeste y bordes redondeados
            shape = RoundedCornerShape(12.dp), // Bordes redondeados
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black, // Color negro para el texto cuando está enfocado
                unfocusedTextColor = Color.Black, // Color de fondo celeste
                focusedBorderColor = Color(0xFF77BBC6), // Color azul (por ejemplo)
                unfocusedBorderColor = Color(0xFF77BBC6)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para categoria
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
        ) {
            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.categoria),
                            contentDescription = "categoria",
                            modifier = Modifier
                                .size(25.dp),
                            tint = Color(0xFF7D7A73)
                        )
                        Spacer(modifier = Modifier.width(25.dp))
                        Text(
                            "CATEGORÍA",
                            style = TextStyle(
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFF7D7A73)
                            )
                        )
                    }
                },
                readOnly = true, // El campo es solo de lectura, el valor se selecciona desde el menú
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .onGloballyPositioned { _ ->
                        // Ajustar la posición del menú
                        // Puedes personalizar este código según tu diseño
                    }
                    .fillMaxWidth() // Color celeste y bordes redondeados
                    .background(Color(0xFF77BBC6), shape = RoundedCornerShape(12.dp)), // Color celeste y bordes redondeados
                shape = RoundedCornerShape(12.dp), // Bordes redondeados
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black, // Color negro para el texto cuando está enfocado
                    unfocusedTextColor = Color.Black, // Color de fondo celeste
                    focusedBorderColor = Color(0xFF77BBC6), // Color azul (por ejemplo)
                    unfocusedBorderColor = Color(0xFF77BBC6)
                )
            )

            // Menú desplegable con las opciones de categoría
            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                categorias.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            categoria = option
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para enviar el formulario
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                colors =  ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8BD7D7)
                ),
                onClick = {
                    // Llamar a la función de envío con los datos ingresados
                    onSubmit(nombre, mail, dni, categoria)
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(0.21f)
            ) {
                Text(
                    "LISTO",
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF7D7A73)
                    )
                )
            }
        }
    }
}
