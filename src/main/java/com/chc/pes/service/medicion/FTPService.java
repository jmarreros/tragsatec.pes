package com.chc.pes.service.medicion;

import org.apache.commons.vfs2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FTPService {

    private static final Logger logger = LoggerFactory.getLogger(FTPService.class);

    @Value("${ftp.type}")
    private String ftpType;

    @Value("${ftp.host}")
    private String ftpHost;

    @Value("${ftp.port}")
    private int ftpPort;

    @Value("${ftp.username}")
    private String ftpUsername;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${file.upload-dir}")
    private String uploadDir;


    /**
     * Obtiene el protocolo según la configuración (ftp o sftp)
     */
    private String obtenerProtocolo() {
        return "sftp".equalsIgnoreCase(ftpType) ? "sftp" : "ftp";
    }

    /**
     * Construye la URI de conexión FTP/SFTP para un archivo
     */
    private String construirUri(String directorioRemoto, String nombreArchivo) {
        String path = directorioRemoto.endsWith("/") ? directorioRemoto : directorioRemoto + "/";
        String protocolo = obtenerProtocolo();
        return String.format("%s://%s:%s@%s:%d%s%s",
                protocolo, ftpUsername, ftpPassword, ftpHost, ftpPort, path, nombreArchivo);
    }

    /**
     * Busca un archivo por nombre en el servidor FTP/SFTP y lo copia a la carpeta fileUploads
     *
     * @param nombreArchivo nombre del archivo a buscar
     * @return true si el archivo fue encontrado y copiado exitosamente, false en caso contrario
     */
    public boolean buscarYCopiarArchivo(String nombreArchivo) {
        FileObject archivoRemoto = null;
        FileObject archivoLocal = null;
        String directorioRemoto = "/";

        try {
            FileSystemManager manager = VFS.getManager();

            String uriArchivo = construirUri(directorioRemoto, nombreArchivo);
            archivoRemoto = manager.resolveFile(uriArchivo);

            // Verificar si el archivo existe
            if (!archivoRemoto.exists()) {
                logger.warn("Archivo '{}' no encontrado en el directorio '{}'", nombreArchivo, directorioRemoto);
                return false;
            }

            // Crear directorio de destino si no existe
            Path directorioDestino = Paths.get(uploadDir);
            if (!Files.exists(directorioDestino)) {
                Files.createDirectories(directorioDestino);
            }

            // Ruta completa del archivo de destino
            Path rutaArchivoDestino = directorioDestino.resolve(nombreArchivo);
            archivoLocal = manager.resolveFile(rutaArchivoDestino.toUri());

            // Copiar el archivo remoto al local
            archivoLocal.copyFrom(archivoRemoto, Selectors.SELECT_SELF);

            logger.info("Archivo '{}' copiado exitosamente a '{}'", nombreArchivo, rutaArchivoDestino);
            return true;

        } catch (FileSystemException e) {
            logger.error("Error FTP al buscar/copiar archivo: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage(), e);
            return false;
        } finally {
            cerrarFileObject(archivoRemoto);
            cerrarFileObject(archivoLocal);
        }
    }


    /**
     * Cierra un FileObject de forma segura
     */
    private void cerrarFileObject(FileObject fileObject) {
        if (fileObject != null) {
            try {
                fileObject.close();
            } catch (FileSystemException e) {
                logger.error("Error al cerrar FileObject", e);
            }
        }
    }
}
