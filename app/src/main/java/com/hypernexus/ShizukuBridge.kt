package com.hypernexus
import android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.Context
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
    
    // Flujos de estado reactivos para que la interfaz de usuario sepa si hay permisos o no
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission
    
    // Listener que se dispara cuando el usuario acepta o rechaza el diálogo de Shizuku
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == 0) {
            _hasPermission.value = grantResult == Shizuku.PERMISSION_GRANTED
            if (grantResult == Shizuku.PERMISSION_GRANTED) {
                bindService()
            }
        }
    }
    
    // Listener que detecta si el servicio de Shizuku se muere en segundo plano
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.w(TAG, "El binder de Shizuku ha muerto de forma inesperada.")
        _isConnected.value = false
        _hasPermission.value = false
    }
    
    init {
        try {
            // Verificar si Shizuku está disponible y si ya cuenta con permisos asignados
            if (Shizuku.pingBinder()) {
                _hasPermission.value = Shizuku.checkSelfPermission() == Shizuku.PERMISSION_GRANTED
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
    
    // Solicita de forma nativa los privilegios mostrando el diálogo flotante del sistema
    fun requestPermission() {
        try {
            if (Shizuku.pingBinder()) {
                Shizuku.requestPermission(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al solicitar permisos a Shizuku", e)
        }
    }
    
    // Establece de forma lógica el puente de conexión
    fun bindService() {
        try {
            if (!Shizuku.pingBinder()) {
                _isConnected.value = false
                return
            }
            
            if (Shizuku.checkSelfPermission() == Shizuku.PERMISSION_GRANTED) {
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
    
    /**
     * EJECUCIÓN PRIVILEGIADA REAL: Inyecta comandos en la Shell del sistema 
     * utilizando el proceso nativo de Shizuku (con permisos ADB/Root).
     * Retorna un Pair con: (CodigoDeSalida, TextoDeRespuesta)
     */
    fun executeShellCommand(command: String): Pair<Int, String> {
        if (!_isConnected.value || !Shizuku.pingBinder()) {
            return -1 to "Error: No conectado a Shizuku o el servicio esta apagado"
        }
        
        return try {
            // OPTIMIZACIÓN: Se levanta un proceso remoto directamente en el espacio de Shizuku
            // Esto garantiza que el comando corra como ADB/Root y no con los límites de tu APK.
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            val output = outputReader.readText()
            val error = errorReader.readText()
            
            // Espera a que termine la ejecución del comando para obtener el código de salida (0 = Éxito)
            val exitCode = process.waitFor()
            
            outputReader.close()
            errorReader.close()
            
            exitCode to (output + error).trim()
        } catch (e: Exception) {
            Log.e(TAG, "Fallo critico al ejecutar el comando: $command", e)
            -1 to "Error de ejecucion: ${e.message}"
        }
    }
    
    /**
     * Ejecuta multiples comandos secuenciales en bloque de forma segura
     */
    fun executeScript(commands: List<String>): List<Pair<Int, String>> {
        return commands.map { cmd ->
            val result = executeShellCommand(cmd)
            Log.d(TAG, "Comando ejecutado: $cmd -> Codigo de salida: ${result.first}")
            result
        }
    }
    
    // Limpieza de memoria al destruir el servicio para evitar fugas de memoria (Memory Leaks)
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
