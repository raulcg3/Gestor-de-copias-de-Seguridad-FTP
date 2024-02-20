/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

/**
 *
 * @author raulc
 */
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GestorCopiasFTP {

    // Rutas y configuraciones predefinidas
    private static final String CARPETA_LOCAL = "C:/Local";
    private static final String RUTA_ARCHIVO_COMPRIMIDO = "C:\\Users\\raulc\\Downloads\\comprimido.zip";
    private static final String SERVIDOR_FTP = "localhost";
    private static final int PUERTO_FTP = 21;
    private static final String USUARIO_FTP = "admin";
    private static final String CONTRASENA_FTP = "admin";
    private static final String EXTENSION_ZIP = ".zip";

    /**
     * Método principal que inicia la tarea programada para sincronizar la carpeta local con el servidor FTP.
     *
     * @param args Argumentos de línea de comandos (no se utilizan en esta implementación).
     */
    public static void main(String[] args) {
        // Iniciar la tarea programada para mantener la sincronización
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(GestorCopiasFTP::sincronizarCarpeta, 0, 20, TimeUnit.SECONDS);
    }

    /**
     * Método para sincronizar la carpeta local con el servidor FTP.
     */
    private static void sincronizarCarpeta() {
        // Obtener la marca de tiempo para el archivo comprimido
        String marcaTiempo = obtenerMarcaTiempo();
        String nombreArchivoFTP = marcaTiempo + EXTENSION_ZIP;

        // Comprimir la carpeta local
        comprimirCarpeta(CARPETA_LOCAL, RUTA_ARCHIVO_COMPRIMIDO);

        // Subir el archivo comprimido al servidor FTP
        subirArchivoFTP(SERVIDOR_FTP, PUERTO_FTP, USUARIO_FTP, CONTRASENA_FTP, nombreArchivoFTP, RUTA_ARCHIVO_COMPRIMIDO);
    }

    /**
     * Método para comprimir una carpeta en un archivo ZIP.
     *
     * @param carpetaFuente  Ruta de la carpeta a comprimir.
     * @param archivoDestino Ruta del archivo ZIP resultante.
     */
    private static void comprimirCarpeta(String carpetaFuente, String archivoDestino) {
        try {
            FileOutputStream fos = new FileOutputStream(archivoDestino);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File carpeta = new File(carpetaFuente);
            comprimirCarpeta(carpeta, carpeta.getName(), zos);
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método auxiliar para comprimir recursivamente una carpeta y su contenido.
     *
     * @param carpeta      Carpeta a comprimir.
     * @param carpetaPadre Ruta de la carpeta padre.
     * @param zos          Flujo de salida del archivo ZIP.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private static void comprimirCarpeta(File carpeta, String carpetaPadre, ZipOutputStream zos) throws IOException {
        for (File archivo : carpeta.listFiles()) {
            if (archivo.isDirectory()) {
                comprimirCarpeta(archivo, carpetaPadre + "/" + archivo.getName(), zos);
                continue;
            }
            FileInputStream fis = new FileInputStream(archivo);
            ZipEntry entry = new ZipEntry(carpetaPadre + "/" + archivo.getName());
            zos.putNextEntry(entry);
            byte[] buffer = new byte[1024];
            int longitud;
            while ((longitud = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, longitud);
            }
            fis.close();
        }
    }

    /**
     * Método para subir un archivo al servidor FTP.
     *
     * @param servidor         Dirección del servidor FTP.
     * @param puerto           Puerto del servidor FTP.
     * @param usuario          Usuario para iniciar sesión en el servidor FTP.
     * @param contraseña       Contraseña del usuario.
     * @param nombreArchivoFTP Nombre del archivo en el servidor FTP.
     * @param rutaArchivoLocal Ruta del archivo local a subir.
     */
    private static void subirArchivoFTP(String servidor, int puerto, String usuario, String contraseña, String nombreArchivoFTP, String rutaArchivoLocal) {
        FTPClient clienteFTP = new FTPClient();
        try {
            clienteFTP.connect(servidor, puerto);
            clienteFTP.login(usuario, contraseña);
            clienteFTP.enterLocalPassiveMode();
            clienteFTP.setFileType(FTP.BINARY_FILE_TYPE);

            InputStream inputStream = new FileInputStream(new File(rutaArchivoLocal));

            boolean exitoso = clienteFTP.storeFile(nombreArchivoFTP, inputStream);
            inputStream.close();
            if (exitoso) {
                System.out.println("El archivo se ha subido correctamente.");
            } else {
                System.out.println("Error al subir el archivo.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (clienteFTP.isConnected()) {
                    clienteFTP.logout();
                    clienteFTP.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Método para obtener la marca de tiempo actual en un formato específico.
     *
     * @return Marca de tiempo en formato "yyyy-MM-dd-HH-mm-ss".
     */
    private static String obtenerMarcaTiempo() {
        LocalDateTime tiempoActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String marcaTiempo = tiempoActual.format(formatter);
        System.out.println(marcaTiempo);
        return marcaTiempo;
    }
}
