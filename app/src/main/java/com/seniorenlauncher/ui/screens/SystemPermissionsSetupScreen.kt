package com.seniorenlauncher.ui.screens

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.seniorenlauncher.R

@Composable
fun SystemPermissionsSetupScreen(onNext: () -> Unit, isSenior: Boolean = false) {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var canUseFullScreenIntent by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                notificationManager.canUseFullScreenIntent()
            } else true
        )
    }
    var canScheduleExactAlarms by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else true
        )
    }
    var canRequestPackageInstalls by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.packageManager.canRequestPackageInstalls()
            } else true
        )
    }
    var hasNotificationAccess by remember { mutableStateOf(false) }

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(
            (context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager)
                .isIgnoringBatteryOptimizations(context.packageName)
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canDrawOverlays = Settings.canDrawOverlays(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    canUseFullScreenIntent = notificationManager.canUseFullScreenIntent()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    canRequestPackageInstalls = context.packageManager.canRequestPackageInstalls()
                }
                isIgnoringBatteryOptimizations = (context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager)
                    .isIgnoringBatteryOptimizations(context.packageName)
                
                hasNotificationAccess = isNotificationServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val allGranted = canDrawOverlays && canUseFullScreenIntent && canScheduleExactAlarms && 
                     hasNotificationAccess && isIgnoringBatteryOptimizations && canRequestPackageInstalls

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(if (isSenior) R.string.sys_perm_title_senior else R.string.sys_perm_title_tech),
            fontSize = if (isSenior) 36.sp else 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = if (isSenior) 42.sp else 38.sp
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = stringResource(if (isSenior) R.string.sys_perm_desc_senior else R.string.sys_perm_desc_tech),
            fontSize = if (isSenior) 22.sp else 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = if (isSenior) 30.sp else 24.sp
        )
        
        Spacer(Modifier.height(32.dp))

        PermissionRow(
            title = stringResource(if (isSenior) R.string.sys_perm_notif_title_senior else R.string.sys_perm_notif_title_tech),
            description = stringResource(if (isSenior) R.string.sys_perm_notif_desc_senior else R.string.sys_perm_notif_desc_tech),
            isGranted = hasNotificationAccess,
            icon = Icons.Default.Notifications,
            isSenior = isSenior,
            onClick = {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                context.startActivity(intent)
            }
        )

        if (!hasNotificationAccess && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.sys_perm_restricted_warning),
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        PermissionRow(
            title = stringResource(if (isSenior) R.string.sys_perm_overlay_title_senior else R.string.sys_perm_overlay_title_tech),
            description = stringResource(if (isSenior) R.string.sys_perm_overlay_desc_senior else R.string.sys_perm_overlay_desc_tech),
            isGranted = canDrawOverlays,
            icon = Icons.Default.Layers,
            isSenior = isSenior,
            onClick = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        )

        Spacer(Modifier.height(16.dp))

        PermissionRow(
            title = stringResource(if (isSenior) R.string.sys_perm_fullscreen_title_senior else R.string.sys_perm_fullscreen_title_tech),
            description = stringResource(if (isSenior) R.string.sys_perm_fullscreen_desc_senior else R.string.sys_perm_fullscreen_desc_tech),
            isGranted = canUseFullScreenIntent,
            icon = Icons.Default.Fullscreen,
            isSenior = isSenior,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        PermissionRow(
            title = stringResource(if (isSenior) R.string.sys_perm_alarm_title_senior else R.string.sys_perm_alarm_title_tech),
            description = stringResource(if (isSenior) R.string.sys_perm_alarm_desc_senior else R.string.sys_perm_alarm_desc_tech),
            isGranted = canScheduleExactAlarms,
            icon = Icons.Default.Alarm,
            isSenior = isSenior,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        PermissionRow(
            title = stringResource(if (isSenior) R.string.sys_perm_install_title_senior else R.string.sys_perm_install_title_tech),
            description = stringResource(if (isSenior) R.string.sys_perm_install_desc_senior else R.string.sys_perm_install_desc_tech),
            isGranted = canRequestPackageInstalls,
            icon = Icons.Default.SystemUpdate,
            isSenior = isSenior,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
        )

        PermissionRow(
            title = stringResource(if (isSenior) R.string.sys_perm_battery_title_senior else R.string.sys_perm_battery_title_tech),
            description = stringResource(if (isSenior) R.string.sys_perm_battery_desc_senior else R.string.sys_perm_battery_desc_tech),
            isGranted = isIgnoringBatteryOptimizations,
            icon = Icons.Default.BatteryAlert,
            isSenior = isSenior,
            onClick = {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSenior) 100.dp else 80.dp),
            shape = RoundedCornerShape(24.dp),
            enabled = allGranted,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allGranted) Color(0xFF10B981) else Color.Gray
            )
        ) {
            Text(
                text = if (allGranted) stringResource(if (isSenior) R.string.sys_perm_btn_done_senior else R.string.sys_perm_btn_done_tech) 
                       else stringResource(if (isSenior) R.string.sys_perm_btn_pending_senior else R.string.sys_perm_btn_pending_tech),
                fontSize = if (isSenior) 24.sp else 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    isSenior: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isGranted) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFFE8F5E9) else Color(0xFFFEF2F2)
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSenior) 4.dp else 2.dp, 
            if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444)
        )
    ) {
        Row(
            modifier = Modifier.padding(if (isSenior) 24.dp else 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isSenior) 64.dp else 56.dp)
                    .background(
                        if (isGranted) Color(0xFF10B981).copy(alpha = 0.1f) 
                        else Color(0xFFEF4444).copy(alpha = 0.1f), 
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    contentDescription = if (isSenior) title else null, 
                    modifier = Modifier.size(if (isSenior) 40.dp else 32.dp), 
                    tint = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
            
            Spacer(Modifier.width(if (isSenior) 24.dp else 20.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    title, 
                    fontSize = if (isSenior) 22.sp else 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) Color(0xFF1B5E20) else Color(0xFF991B1B)
                )
                Text(
                    description, 
                    fontSize = if (isSenior) 18.sp else 14.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = if (isSenior) 24.sp else 18.sp
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            if (isGranted) {
                Text("\u2705", fontSize = if (isSenior) 32.sp else 24.sp)
            } else {
                Text("\u274C", fontSize = if (isSenior) 32.sp else 24.sp)
            }
        }
    }
}
