package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectedAccount
import com.example.data.model.PageTurnAnimation
import com.example.data.model.ReaderTheme
import com.example.data.repository.BookRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsSyncScreen(
    repository: BookRepository,
    modifier: Modifier = Modifier
) {
    val googleAccount by repository.googleAccount.collectAsState()
    val wattpadAccount by repository.wattpadAccount.collectAsState()
    val settings by repository.settings.collectAsState()
    val books by repository.books.collectAsState()

    val offlineBooksCount = books.count { it.isDownloadedOffline }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .padding(bottom = 96.dp)
            .testTag("accounts_sync_screen")
    ) {
        Text(
            text = "Cuentas y Sincronización",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Gestiona tus perfiles conectados para sincronizar tu biblioteca e historial.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Google Account Card
        AccountConnectionCard(
            account = googleAccount,
            brandColor = Color(0xFF4285F4),
            icon = Icons.Default.Cloud,
            onToggle = { repository.toggleGoogleAccount() },
            onSync = { repository.syncAllCloud() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Wattpad Account Card
        AccountConnectionCard(
            account = wattpadAccount,
            brandColor = Color(0xFFFF500A),
            icon = Icons.Default.MenuBook,
            onToggle = { repository.toggleWattpadAccount() },
            onSync = { repository.syncAllCloud() }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Storage & Offline Reading Section
        Text(
            text = "Almacenamiento y Modo Offline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Libros Descargados Offline",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$offlineBooksCount libros listos para leer sin Internet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.OfflinePin,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (offlineBooksCount.toFloat() / books.size.coerceAtLeast(1).toFloat()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aprox. 42 MB de memoria en dispositivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Preferencias Generales de la Aplicación
        Text(
            text = "Preferencias Predeterminadas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Theme preference row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tema de Lector",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = settings.theme.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReaderTheme.values().forEach { t ->
                            Surface(
                                shape = CircleShape,
                                color = t.backgroundColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .clickable { repository.updateSettings(settings.copy(theme = t)) }
                            ) {}
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Page Animation preference row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Animación de Paso de Página",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = settings.pageTurnAnimation.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            val nextAnim = when (settings.pageTurnAnimation) {
                                PageTurnAnimation.REAL_CURL_3D -> PageTurnAnimation.SMOOTH_SLIDE
                                PageTurnAnimation.SMOOTH_SLIDE -> PageTurnAnimation.CONTINUOUS_SCROLL
                                PageTurnAnimation.CONTINUOUS_SCROLL -> PageTurnAnimation.REAL_CURL_3D
                            }
                            repository.updateSettings(settings.copy(pageTurnAnimation = nextAnim))
                        }
                    ) {
                        Icon(Icons.Default.Flip, contentDescription = "Cambiar animación")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Mini Apartado: Contacto del Desarrollador
        Text(
            text = "Información del Desarrollador",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val context = androidx.compose.ui.platform.LocalContext.current
        var copiedToast by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("developer_contact_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Desarrollador",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "JonathanHurtadoDev@proton.me",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Desarrollador contacto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("JonathanHurtadoDev@proton.me"))
                            copiedToast = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_developer_email_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (copiedToast) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (copiedToast) "¡Copiado!" else "Copiar Correo", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:JonathanHurtadoDev@proton.me")
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Contacto Libris Reader - Usuario")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("JonathanHurtadoDev@proton.me"))
                                copiedToast = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("send_developer_email_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enviar Correo", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AccountConnectionCard(
    account: ConnectedAccount,
    brandColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: () -> Unit,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = brandColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = brandColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = account.provider,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (account.isConnected) account.username else "No conectado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onToggle,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (account.isConnected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (account.isConnected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (account.isConnected) "Desconectar" else "Conectar")
                }
            }

            if (account.isConnected) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Última sincronización: ${account.lastSyncTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onSync) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sincronizar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
