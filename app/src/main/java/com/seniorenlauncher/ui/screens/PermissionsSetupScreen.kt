package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.seniorenlauncher.R

data class PermissionCategory(
    val titleRes: Int,
    val descriptionRes: Int,
    val privacyNoteRes: Int,
    val icon: ImageVector,
    val permissions: List<String>
)

@Composable
fun PermissionsSetupScreen(onNext: () -> Unit, isSenior: Boolean = false) {
    val context = LocalContext.current

    val categories = remember(isSenior) {
        val list = mutableListOf<PermissionCategory>()
        
        val locationPermissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            locationPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            locationPermissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }

        list.add(PermissionCategory(
            if (isSenior) R.string.perm_cat_emergency_senior else R.string.perm_cat_location_tech, 
            if (isSenior) R.string.perm_cat_emergency_senior_desc else R.string.perm_cat_location_tech_desc, 
            R.string.perm_privacy_location,
            Icons.Default.LocationOn, 
            locationPermissions
        ))

        list.add(PermissionCategory(
            if (isSenior) R.string.perm_cat_phone_senior else R.string.perm_cat_phone_tech, 
            if (isSenior) R.string.perm_cat_phone_senior_desc else R.string.perm_cat_phone_tech_desc, 
            R.string.perm_privacy_phone,
            Icons.Default.Phone, 
            listOf(Manifest.permission.CALL_PHONE)
        ))

        list.add(PermissionCategory(
            if (isSenior) R.string.perm_cat_sms_senior else R.string.perm_cat_sms_tech, 
            if (isSenior) R.string.perm_cat_sms_senior_desc else R.string.perm_cat_sms_tech_desc, 
            R.string.perm_privacy_sms,
            Icons.Default.Sms, 
            listOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
        ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val healthPermissions = mutableListOf(Manifest.permission.ACTIVITY_RECOGNITION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                healthPermissions.add(Manifest.permission.FOREGROUND_SERVICE_HEALTH)
            }
            
            list.add(PermissionCategory(
                R.string.perm_cat_health, 
                R.string.perm_cat_health_desc, 
                R.string.perm_privacy_health,
                Icons.AutoMirrored.Filled.DirectionsWalk, 
                healthPermissions
            ))
        }

        list.add(PermissionCategory(
            if (isSenior) R.string.perm_cat_camera_senior else R.string.perm_cat_camera_tech, 
            if (isSenior) R.string.perm_cat_camera_senior_desc else R.string.perm_cat_camera_tech_desc, 
            R.string.perm_privacy_camera,
            Icons.Default.CameraAlt, 
            listOf(Manifest.permission.CAMERA)
        ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(PermissionCategory(
                if (isSenior) R.string.perm_cat_notifications_senior else R.string.perm_cat_notifications_tech, 
                if (isSenior) R.string.perm_cat_notifications_senior_desc else R.string.perm_cat_notifications_tech_desc, 
                R.string.perm_privacy_notifications,
                Icons.Default.Notifications, 
                listOf(Manifest.permission.POST_NOTIFICATIONS)
            ))
        }

        list
    }

    var currentStep by remember { mutableIntStateOf(0) }
    val currentCategory = categories.getOrNull(currentStep) ?: return onNext()

    var isGranted by remember(currentStep) {
        mutableStateOf(checkPermissions(context, currentCategory.permissions))
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        isGranted = results.values.all { it }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Slate 900
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Premium Stepper
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.perm_step_format, currentStep + 1, categories.size).uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color(0xFF3B82F6)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((currentStep + 1) / categories.size.toFloat())
                            .fillMaxHeight()
                            .background(Color(0xFF3B82F6))
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedContent(
                targetState = currentCategory,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 2 }).togetherWith(fadeOut() + slideOutVertically { -it / 2 })
                },
                label = "PermissionContent",
                modifier = Modifier.weight(1f)
            ) { category ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Luminous Icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = RoundedCornerShape(40.dp),
                            color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp),
                                    tint = Color(0xFF3B82F6)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(category.titleRes).uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 38.sp,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(category.descriptionRes),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Bento-style Privacy Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(40.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Shield, 
                                contentDescription = null, 
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = stringResource(category.privacyNoteRes),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // Action Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isGranted) {
                    val nextInteractionSource = remember { MutableInteractionSource() }
                    val nextPressed by nextInteractionSource.collectIsPressedAsState()
                    val nextScale by animateFloatAsState(
                        targetValue = if (nextPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                    )

                    Button(
                        onClick = { 
                            if (currentStep < categories.size - 1) currentStep++ else onNext() 
                        },
                        interactionSource = nextInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .scale(nextScale),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(40.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (currentStep < categories.size - 1) stringResource(R.string.perm_btn_next) else stringResource(R.string.perm_btn_start_app), 
                                fontSize = 24.sp, 
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.width(12.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                } else {
                    val grantInteractionSource = remember { MutableInteractionSource() }
                    val grantPressed by grantInteractionSource.collectIsPressedAsState()
                    val grantScale by animateFloatAsState(
                        targetValue = if (grantPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                    )

                    Button(
                        onClick = { launcher.launch(currentCategory.permissions.toTypedArray()) },
                        interactionSource = grantInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .scale(grantScale),
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text(stringResource(R.string.perm_btn_confirm), fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    
                    TextButton(
                        onClick = { 
                            if (currentStep < categories.size - 1) currentStep++ else onNext() 
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            stringResource(R.string.perm_btn_not_now), 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

private fun checkPermissions(context: Context, permissions: List<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
