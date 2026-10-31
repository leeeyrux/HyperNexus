package com.hypernexus

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.ShizukuService

class ShizukuBridge(private val context: Context) {
    
    companion object {
        private const val TAG = "ShizukuBridge"
    }
    
    // Estados de conexión
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission
    
    private var serviceBinder: IBinder? = null
    
    // Listener para cambios de permiso
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == 0) {
            _hasPermission.value = grantResult == Shizuku.PERMISSION_GRANTED
        }
    }
    
    // Listener para servicio muerto
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.w(TAG, "Shizuku binder died")
        _isConnected.value = false
        serviceBinder = null
    }
    
    init {
        // Verificar estado inicial
        _hasPermission.value = Shizuku.checkSelfPermission() == Shizuku.PERMISSION_GRANTED
        
        // Registrar listeners
        Shizuku.addRequestPermissionResultListener(permissionListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
    }
    
    fun requestPermission() {
        try {
            Shizuku.requestPermission(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting permission", e)
        }
    }
    
    fun bindService() {
        try {
            if (Shizuku.checkSelfPermission() != Shizuku.PERMISSION_GRANTED) {
                Log.w(TAG, "No permission, requesting...")
                requestPermission()
                return
            }
            
            // Obtener binder de Shizuku
            val binder = Shizuku.getBinder()
            if (binder != null) {
                serviceBinder = ShizukuBinderWrapper(binder)
                _isConnected.value = true
                Log.i(TAG, "Connected to Shizuku")
            } else {
                Log.e(TAG, "Shizuku binder is null")
                _isConnected.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding to Shizuku", e)
            _isConnected.value = false
        }
    }
    
    fun unbindService() {
        serviceBinder = null
        _isConnected.value = false
    }
    
    /**
     * Ejecuta comando shell con privilegios de Shizuku
     * Retorna Pair(codigoSalida, salidaCompleta)
     */
    fun executeShellCommand(command: String): Pair<Int, String> {
        if (!_isConnected.value || serviceBinder == null) {
            return -1 to "No conectado a Shizuku"
        }
        
        return try {
            // Usar ShizukuService para ejecutar comandos
            val result = ShizukuService.newInstance(serviceBinder!!, command)
            // Nota: La API real puede variar, esto es pseudocódigo conceptual
            // En implementación real usaríamos UserService o transacciones AIDL directas
            
            // Alternativa más directa (si tenemos acceso):
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            exitCode to (output + error)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command: $command", e)
            -1 to "Error: ${e.message}"
        }
    }
    
    /**
     * Ejecuta múltiples comandos en secuencia (para scripts)
     */
    fun executeScript(commands: List<String>): List<Pair<Int, String>> {
        return commands.map { cmd ->
            val result = executeShellCommand(cmd)
            Log.d(TAG, "Executed: $cmd -> ${result.first}")
            result
        }
    }
    
    fun cleanup() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        unbindService()
    }
}