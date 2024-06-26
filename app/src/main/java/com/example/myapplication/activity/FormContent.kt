package com.example.myapplication.activity
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.database.TAG
import com.example.myapplication.database.registrarLogs
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("SuspiciousIndentation")
@Composable
fun RenderFormulario(context: Context, onFinish: () -> Unit) {
    var showForm by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Mostrar formulario si showForm es verdadero
        if (showForm) {
            Box(
                modifier = Modifier
                    .padding(5.dp) // Agrega espacio alrededor del contenedor
                    .clip(RoundedCornerShape(16.dp)) // Define bordes redondeados
                    .background(Color(0xFFC7DEE1)) // Color de fondo celeste (Light Blue)
                    .padding(2.dp) // Espacio interno
            ) {
                Formulario ({ nombre, apellido, mail, dni, categoria, estado ->
                    println("NOMBRE VISITANTE: $nombre, MAIL: $mail, DNI: $dni, TIPO DE CUENTA: $categoria")
                    if(dni.isNotEmpty()) {
                        registrarLogs(context, nombre, apellido, dni.toInt(), estado, "offline")
                        Toast.makeText(context, "Usuario ingresado exitosamente", Toast.LENGTH_LONG).show()
                        Log.println(Log.INFO, TAG,"Ingreso de datos : "+nombre+", "+apellido+", "+dni)
                    } else {
                        Log.e(TAG, "Error de registro: No se encontró el legajo del visitante en la base local.")
                        Toast.makeText(context, "Error de registro.", Toast.LENGTH_LONG).show()
                    }
                    // Opcionalmente, puedes cerrar el formulario después de enviarlo
                    showForm = false
                    onFinish() // Llama a la función de finalizar
                }, { showForm = false; onFinish() })
            }
        }
    }
}

fun obtenerFechaActualISO(): String {
    // Crear un objeto Calendar para obtener la fecha y hora actual
    val calendar = Calendar.getInstance()

    // Crear un formato de fecha ISO 8601
    val formatoISO = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Obtener la fecha actual en formato ISO 8601
    val fechaActualISO = formatoISO.format(calendar.time)

    // Retornar la fecha en formato ISO
    return fechaActualISO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Formulario(onSubmit: (String, String, String, String, String, String) -> Unit, onBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    val categorias = listOf("Visitante", "Profesor", "Alumno", "Seguridad")
    var estado by remember { mutableStateOf("") }
    val estados = listOf("Ingresando", "Saliendo")
    var mail by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isDropdownExpanded2 by remember { mutableStateOf(false) }
    val Inter = FontFamily(
        Font(resId = R.font.inter_medium), // Fuente regular
        Font(resId = R.font.inter_bold, weight = FontWeight.Bold) // Fuente en negrita
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Icon(
            painter = painterResource(id = R.drawable.arrow_left),
            contentDescription = "arrow left",
            modifier = Modifier
                .size(30.dp)
                .padding(start = 0.dp)
                .clickable {
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

        // Campo de texto para el apellido
        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
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
                        "APELLIDO VISITANTE",
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

        // Campo de texto para DNI
        OutlinedTextField(
            value = dni,
            onValueChange = {
                if (it.all { char -> char.isDigit() }) {
                    dni = it
                }
            },
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
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
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
                            "TIPO DE CUENTA",
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

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para categoria
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded2,
            onExpandedChange = { isDropdownExpanded2 = !isDropdownExpanded2 }
        ) {
            OutlinedTextField(
                value = estado,
                onValueChange = { estado = it },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(25.dp))
                        Text(
                            "ESTADO",
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

            // Menú desplegable con las opciones de estados
            ExposedDropdownMenu(
                expanded = isDropdownExpanded2,
                onDismissRequest = { isDropdownExpanded2 = false }
            ) {
                estados.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            estado = option
                            isDropdownExpanded2 = false
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
                    onSubmit(nombre, apellido, mail, dni, categoria, estado)
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
