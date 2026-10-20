package com.seniorenlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.R
import com.seniorenlauncher.data.model.EmergencyInfo
import com.seniorenlauncher.data.model.Medication
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.launch

@Composable
fun EmergencyInfoScreen(onBack: () -> Unit) {
    val isDemoMode = false

    val scope = rememberCoroutineScope()
    val db = LauncherApp.instance.database
    val emergencyDao = db.emergencyDao()
    val medDao = db.medicationDao()
    
    val realInfo by emergencyDao.get().collectAsState(initial = EmergencyInfo())
    val realMedications by medDao.getActive().collectAsState(initial = emptyList())
    
    val dummyInfo = EmergencyInfo(
        fullName = "Hendrik de Jong",
        birthDate = "12-05-1945",
        address = "Hoofdstraat 42, Amsterdam",
        bloodType = "A+",
        allergies = "Geen",
        conditions = "Hoge bloeddruk",
        doctorName = "Huisarts de Vries",
        doctorPhone = "020 555 0123",
        iceContactName = "Dochter Sofie",
        iceContactPhone = "06 12345678",
        hasPacemaker = false
    )
    val dummyMeds = listOf(
        Medication(name = "Bloeddruk", dose = "1 pil", times = "08:00")
    )

    val currentInfo = if (isDemoMode) dummyInfo else realInfo ?: EmergencyInfo()
    val medications = if (isDemoMode) dummyMeds else realMedications
    
    var showEditDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = stringResource(R.string.emergency_title), onBack = onBack)
        
        Box(modifier = Modifier.weight(1f)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Medische informatie
                EmergencySection(
                    title = stringResource(R.string.emergency_medical_title),
                    titleColor = Color(0xFFEF4444),
                    backgroundColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                ) {
                    EmergencyRow(stringResource(R.string.emergency_blood_type), currentInfo.bloodType)
                    EmergencyRow(stringResource(R.string.emergency_allergies), currentInfo.allergies)
                    EmergencyRow(stringResource(R.string.emergency_conditions), currentInfo.conditions)
                    EmergencyRow(stringResource(R.string.emergency_pacemaker), if (currentInfo.hasPacemaker) stringResource(R.string.yes) else stringResource(R.string.no))
                }

                // Medicijnen
                EmergencySection(
                    title = stringResource(R.string.emergency_current_meds),
                    titleColor = Color(0xFF3B82F6),
                    backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
                ) {
                    if (medications.isEmpty()) {
                        Text(stringResource(R.string.emergency_no_meds), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        medications.forEach { med ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(med.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("${med.dose} — ${med.times}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (med != medications.last()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }
                        }
                    }
                }

                // ICE Contacten
                EmergencySection(
                    title = stringResource(R.string.emergency_ice_contacts),
                    titleColor = Color(0xFF10B981),
                    backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f)
                ) {
                    EmergencyRow(stringResource(R.string.emergency_name_label), currentInfo.iceContactName.ifBlank { stringResource(R.string.not_set) })
                    EmergencyRow(stringResource(R.string.emergency_phone_label), currentInfo.iceContactPhone.ifBlank { stringResource(R.string.not_set) })
                }

                // Persoonlijke info
                EmergencySection(
                    title = stringResource(R.string.emergency_personal_info),
                    titleColor = Color(0xFFF59E0B),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    EmergencyRow(stringResource(R.string.emergency_name_label), currentInfo.fullName)
                    EmergencyRow(stringResource(R.string.emergency_birthdate), currentInfo.birthDate)
                    EmergencyRow(stringResource(R.string.emergency_address), currentInfo.address)
                    EmergencyRow(stringResource(R.string.emergency_doctor), "${currentInfo.doctorName} — ${currentInfo.doctorPhone}")
                }
                
                Spacer(Modifier.height(80.dp))
            }

            if (!isDemoMode) {
                LargeFloatingActionButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 8.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit), modifier = Modifier.size(36.dp))
                }
            }
        }
    }

    if (showEditDialog && !isDemoMode) {
        var name by remember { mutableStateOf(currentInfo.fullName) }
        var birth by remember { mutableStateOf(currentInfo.birthDate) }
        var addr by remember { mutableStateOf(currentInfo.address) }
        var blood by remember { mutableStateOf(currentInfo.bloodType) }
        var allergies by remember { mutableStateOf(currentInfo.allergies) }
        var conditions by remember { mutableStateOf(currentInfo.conditions) }
        var docName by remember { mutableStateOf(currentInfo.doctorName) }
        var docPhone by remember { mutableStateOf(currentInfo.doctorPhone) }
        var pacemaker by remember { mutableStateOf(currentInfo.hasPacemaker) }
        var iceName by remember { mutableStateOf(currentInfo.iceContactName) }
        var icePhone by remember { mutableStateOf(currentInfo.iceContactPhone) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.emergency_edit_dialog_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.emergency_personal_header), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.emergency_fullname_label)) })
                    OutlinedTextField(value = birth, onValueChange = { birth = it }, label = { Text(stringResource(R.string.emergency_birthdate)) })
                    OutlinedTextField(value = addr, onValueChange = { addr = it }, label = { Text(stringResource(R.string.emergency_address)) })
                    
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.emergency_ice_header), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = iceName, onValueChange = { iceName = it }, label = { Text(stringResource(R.string.emergency_contact_name_label)) })
                    OutlinedTextField(value = icePhone, onValueChange = { icePhone = it }, label = { Text(stringResource(R.string.emergency_contact_phone_label)) })

                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.emergency_medical_header), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = blood, onValueChange = { blood = it }, label = { Text(stringResource(R.string.emergency_blood_type)) })
                    OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text(stringResource(R.string.emergency_allergies)) })
                    OutlinedTextField(value = conditions, onValueChange = { conditions = it }, label = { Text(stringResource(R.string.emergency_conditions)) })
                    OutlinedTextField(value = docName, onValueChange = { docName = it }, label = { Text(stringResource(R.string.emergency_doctor_name_label)) })
                    OutlinedTextField(value = docPhone, onValueChange = { docPhone = it }, label = { Text(stringResource(R.string.emergency_doctor_phone_label)) })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pacemaker, onCheckedChange = { pacemaker = it })
                        Text(stringResource(R.string.emergency_has_pacemaker_label))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        emergencyDao.save(EmergencyInfo(
                            fullName = name, birthDate = birth, address = addr,
                            bloodType = blood, allergies = allergies, conditions = conditions,
                            doctorName = docName, doctorPhone = docPhone, hasPacemaker = pacemaker,
                            iceContactName = iceName, iceContactPhone = icePhone
                        ))
                        showEditDialog = false
                    }
                }) { Text(stringResource(R.string.btn_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }
}

@Composable
fun EmergencySection(title: String, titleColor: Color, backgroundColor: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun EmergencyRow(label: String, value: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        HorizontalDivider(Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
