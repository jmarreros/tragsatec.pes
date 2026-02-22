package com.chc.pes.service.medicion;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FTPService {

    @Value("${ftp.type}")
    private String ftpType;

    @Value("${ftp.host}")
    private String host;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;

    @Value("${ftp.timeout}")
    private Integer timeout;

    public boolean buscarYCopiarArchivo(String nombreArchivo, String uploadDir) {
        return "sftp".equalsIgnoreCase(ftpType)
                ? descargarPorSFTP(nombreArchivo, uploadDir)
                : descargarPorFTP(nombreArchivo, uploadDir);
    }

    /* ========================= FTP ========================= */

    private boolean descargarPorFTP(String nombreArchivo, String uploadDir) {
        FTPClient ftp = new FTPClient();

        try {
            ftp.setConnectTimeout(timeout);
            ftp.connect(host, port);

            if (!ftp.login(username, password)) {
                log.error("Login FTP fallido");
                return false;
            }

            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            Path destino = prepararDestino(uploadDir, nombreArchivo);

            try (InputStream inputStream = ftp.retrieveFileStream(nombreArchivo)) {
                if (inputStream == null) {
                    log.warn("Archivo '{}' no encontrado en FTP", nombreArchivo);
                    return false;
                }
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            ftp.completePendingCommand();
            log.info("Archivo '{}' descargado por FTP", nombreArchivo);
            return true;

        } catch (Exception e) {
            log.error("Error FTP", e);
            return false;
        } finally {
            cerrarFTP(ftp);
        }
    }

    private void cerrarFTP(FTPClient ftp) {
        try {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }
        } catch (Exception e) {
            log.warn("Error cerrando FTP", e);
        }
    }

    /* ========================= SFTP ========================= */

    private boolean descargarPorSFTP(String nombreArchivo, String uploadDir) {
        Session session = null;
        ChannelSftp channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(timeout);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(timeout);

            Path destino = prepararDestino(uploadDir, nombreArchivo);

            try (InputStream inputStream = channel.get(nombreArchivo)) {
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Archivo '{}' descargado por SFTP", nombreArchivo);
            return true;

        } catch (SftpException e) {
            log.warn("Archivo '{}' no encontrado en SFTP", nombreArchivo);
            return false;
        } catch (Exception e) {
            log.error("Error SFTP", e);
            return false;
        } finally {
            cerrarSFTP(channel, session);
        }
    }

    private void cerrarSFTP(ChannelSftp channel, Session session) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /* ========================= Utils ========================= */

    private Path prepararDestino(String uploadDir, String nombreArchivo) throws Exception {
        Path directorio = Paths.get(uploadDir);
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }
        return directorio.resolve(nombreArchivo);
    }
}