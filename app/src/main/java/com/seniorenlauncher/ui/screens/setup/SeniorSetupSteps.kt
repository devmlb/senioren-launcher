package com.seniorenlauncher.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.R
import com.seniorenlauncher.data.model.AppTheme
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.ui.screens.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SeniorWelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👋", fontSize = 100.sp)
        Text(stringResource(R.string.wizard_welcome), fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.wizard_welcome_desc),
            fontSize = 26.sp,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        Spacer(Modifier.height(64.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(stringResource(R.string.btn_start), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SeniorReadingStep(onNext: () -> Unit, settingsVm: SettingsViewModel) {
    val settings by settingsVm.settings.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.wizard_font_question), fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        
        ReadingOptionCard(stringResource(R.string.wizard_reading_normal), 18, settings.fontSize == 18) { settingsVm.updateFontSize(18) }
        Spacer(Modifier.height(16.dp))
        ReadingOptionCard(stringResource(R.string.wizard_reading_large), 24, settings.fontSize == 24) { settingsVm.updateFontSize(24) }
        Spacer(Modifier.height(20.dp))
        ReadingOptionCard(stringResource(R.string.wizard_reading_huge), 30, settings.fontSize == 30) { settingsVm.updateFontSize(30) }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(stringResource(R.string.btn_next), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReadingOptionCard(label: String, size: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Text(
            text = label,
            fontSize = size.sp,
            modifier = Modifier.padding(24.dp),
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SeniorColorsStep(onNext: () -> Unit, settingsVm: SettingsViewModel) {
    val settings by settingsVm.settings.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.wizard_colors_question), fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        
        SeniorThemeCard(stringResource(R.string.wizard_theme_classic), AppTheme.CLASSIC, settings.theme == AppTheme.CLASSIC) { settingsVm.updateTheme(AppTheme.CLASSIC) }
        Spacer(Modifier.height(32.dp))
        SeniorThemeCard(stringResource(R.string.wizard_theme_contrast), AppTheme.HIGH_CONTRAST, settings.theme == AppTheme.HIGH_CONTRAST) { settingsVm.updateTheme(AppTheme.HIGH_CONTRAST) }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(stringResource(R.string.btn_next), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SeniorThemeCard(label: String, theme: AppTheme, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (theme == AppTheme.HIGH_CONTRAST) Color.Black else Color(0xFFF0F2F5)
    val textColor = if (theme == AppTheme.HIGH_CONTRAST) Color.Yellow else Color.Black
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            Text(label, color = textColor, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SeniorEmergencyStep(onNext: () -> Unit) {
    val scope = rememberCoroutineScope()
    val contactDao = remember { LauncherApp.instance.database.contactDao() }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🆘", fontSize = 60.sp)
        Text(stringResource(R.string.wizard_emergency_question), fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.emergency_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(R.string.emergency_phone_label)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
        )
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    scope.launch {
                        contactDao.insert(QuickContact(name = name, phoneNumber = phone, isSosContact = true, emoji = "🆘", color = 0xFFDC2626))
                        onNext()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            enabled = name.isNotBlank() && phone.isNotBlank(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(stringResource(R.string.wizard_save_contact), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.wizard_permission_notice), fontSize = 16.sp, textAlign = TextAlign.Center, color = Color.Gray)
    }
}
