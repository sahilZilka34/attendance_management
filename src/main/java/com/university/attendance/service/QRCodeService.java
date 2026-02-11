package com.university.attendance.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.university.attendance.entity.AttendanceSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QRCodeService {
    
    private final AttendanceSessionService sessionService;
    
    // Encryption key - In production, use environment variable
    private static final String ENCRYPTION_KEY = "MySecretKey12345"; // Must be 16 chars for AES-128
    private static final String ALGORITHM = "AES";
    
    /**
     * Generate QR code data (encrypted JSON payload)
     * Contains: sessionId, moduleCode, classroom, timestamp, expiry
     */
    public String generateQRCodeData(UUID sessionId) {
    AttendanceSession session = sessionService.getSessionById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found"));
    
    // Calculate expiry time
    LocalDateTime sessionStart = LocalDateTime.of(
        session.getSessionDate(), 
        session.getStartTime()
    );
    LocalDateTime expiryTime = sessionStart.plusMinutes(session.getQrValidityMinutes());
    long expiryTimestamp = expiryTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    
    // Create payload
    Map<String, String> payload = new HashMap<>();
    payload.put("sessionId", sessionId.toString());
    payload.put("moduleCode", session.getModule().getModuleCode());
    payload.put("moduleName", session.getModule().getModuleName());
    payload.put("classroom", session.getClassroom());
    payload.put("teacherId", session.getTeacher().getId().toString());
    payload.put("teacherName", session.getTeacher().getFirstName() + " " + session.getTeacher().getLastName());
    payload.put("timestamp", String.valueOf(System.currentTimeMillis()));
    payload.put("expiresAt", String.valueOf(expiryTimestamp));
    payload.put("nonce", UUID.randomUUID().toString());
    
    // NEW: Add location requirement
    payload.put("locationRequired", String.valueOf(session.getLocationRequired()));
    if (session.getLocationRequired()) {
        payload.put("campusLat", String.valueOf(session.getCampusLatitude()));
        payload.put("campusLng", String.valueOf(session.getCampusLongitude()));
        payload.put("campusRadius", String.valueOf(session.getCampusRadiusMeters()));
    }
    
    // Convert to JSON string
    String jsonPayload = convertMapToJson(payload);
    
    // Encrypt the payload
    String encryptedData = encrypt(jsonPayload);
    
    return encryptedData;
}
    
    /**
     * Generate QR code image as byte array
     * @param data The encrypted QR code data
     * @param width QR code width in pixels
     * @param height QR code height in pixels
     * @return QR code image as byte[]
     */
    public byte[] generateQRCodeImage(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return outputStream.toByteArray();
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
    
    /**
     * Validate and decrypt QR code data
     * Returns sessionId if valid, throws exception if invalid/expired
     */
    public Map<String, String> validateQRCode(String encryptedData) {
        // Decrypt the data
        String decryptedJson = decrypt(encryptedData);
        
        // Parse JSON
        Map<String, String> payload = parseJsonToMap(decryptedJson);
        
        // Validate expiry
        long expiresAt = Long.parseLong(payload.get("expiresAt"));
        long currentTime = System.currentTimeMillis();
        
        if (currentTime > expiresAt) {
            throw new RuntimeException("QR code has expired");
        }
        
        // Validate session still exists and is active
        UUID sessionId = UUID.fromString(payload.get("sessionId"));
        AttendanceSession session = sessionService.getSessionById(sessionId)
            .orElseThrow(() -> new RuntimeException("Invalid session"));
        
        if (session.getStatus() != com.university.attendance.entity.SessionStatus.ACTIVE) {
            throw new RuntimeException("Session is not active");
        }
        
        return payload;
    }
    
    /**
     * Encrypt data using AES
     */
    private String encrypt(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypt data using AES
     */
    private String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed - Invalid QR code", e);
        }
    }
    
    /**
     * Simple JSON converter (Map to JSON string)
     * In production, use Jackson or Gson library
     */
    private String convertMapToJson(Map<String, String> map) {
        StringBuilder json = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (count > 0) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            count++;
        }
        json.append("}");
        return json.toString();
    }
    
    /**
     * Simple JSON parser (JSON string to Map)
     * In production, use Jackson or Gson library
     */
    private Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        String content = json.substring(1, json.length() - 1); // Remove { }
        String[] pairs = content.split(",");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].replaceAll("\"", "").trim();
            String value = keyValue[1].replaceAll("\"", "").trim();
            map.put(key, value);
        }
        
        return map;
    }
}