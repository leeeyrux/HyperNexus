package com.hypernexus

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class ShizukuBridge(private val context: Context) {
    
    companion object {
        private const val TAG = "ShizukuBridge"
    }
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission
    
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == 0) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            _hasPermission.value = granted
            if (granted) {
                bindService()
            }
        }
    }
    
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.w(TAG, "El binder de Shizuku ha muerto de forma inesperada.")
        _isConnected.value = false
        _hasPermission.value = false
    }
    
    init {
        try {
            if (Shizuku.pingBinder()) {
                _hasPermission.value = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                Shizuku.addRequestPermissionResultListener(permissionListener)
                Shizuku.addBinderDeadListener(binderDeadListener)
                
                if (_hasPermission.value) {
                    bindService()
                }
            } else {
                Log.w(TAG, "Shizuku no esta corriendo en el dispositivo.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar los listeners de Shizuku", e)
        }
    }
    
    fun requestPermission() {
        try {
            if (Shizuku.pingBinder()) {
                Shizuku.requestPermission(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al solicitar permisos a Shizuku", e)
        }
    }
    
    fun bindService() {
        try {
            if (!Shizuku.pingBinder()) {
                _isConnected.value = false
                return
            }
            
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                _isConnected.value = true
                _hasPermission.value = true
                Log.i(TAG, "Conectado exitosamente al puente privilegiado de Shizuku")
            } else {
                _isConnected.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al enlazar el servicio de Shizuku", e)
            _isConnected.value = false
        }
    }
    
    fun unbindService() {
        _isConnected.value = false
    }
    
    fun executeShellCommand(command: String): Pair<Int, String> {
        if (!_isConnected.value || !Shizuku.pingBinder()) {
            return -1 to "Error: No conectado a Shizuku o el servicio esta apagado"
        }
        
        return try {
            // Ejecución remota nativa con privilegios ADB/System vía Shizuku
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            val output = outputReader.readText()
            val error = errorReader.readText()
            
            val exitCode = process.waitFor()
            
            outputReader.close()
            errorReader.close()
            
            exitCode to (output + error).trim()
        } catch (e: Exception) {
            Log.e(TAG, "Fallo critico al ejecutar el comando con Shizuku: $command", e)
            -1 to "Error de ejecucion: ${e.message}"
        }
    }
    
    fun executeScript(commands: List<String>): List<Pair<Int, String>> {
        return commands.map { cmd ->
            val result = executeShellCommand(cmd)
            Log.d(TAG, "Comando ejecutado: $cmd -> Codigo de salida: ${result.first}")
            result
        }
    }
    
    fun cleanup() {
        try {
            Shizuku.removeRequestPermissionResultListener(permissionListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error durante el cleanup de Shizuku", e)
        }
        unbindService()
    }
}
