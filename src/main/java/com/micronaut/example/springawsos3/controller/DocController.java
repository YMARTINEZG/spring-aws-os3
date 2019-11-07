package com.micronaut.example.springawsos3.controller;


import com.micronaut.example.springawsos3.services.S3ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class DocController {

    @Autowired
    S3ClientService s3Client;

    @PostMapping("/api/doc/upload")
    public String uploadMultipartFile(@RequestParam("keyname") String keyName, @RequestParam("uploadfile") MultipartFile file){
        s3Client.uploadFile(keyName, file);
        return "Upload Successfully. -> KeyName = " + keyName;
    }

    @GetMapping("/api/doc/{keyname}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String keyname) {
        ByteArrayOutputStream downloadInputStream = s3Client.downloadFile(keyname);

        return ResponseEntity.ok()
                .contentType(contentType(keyname))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + keyname + "\"")
                .body(downloadInputStream.toByteArray());
    }

    @GetMapping("/api/doc/url/{keyname}")
    public String getPreassignedUrl(@PathVariable String keyname) {
        Optional<URL> url = s3Client.generatePreassignedUrl(keyname);

        if (url.isPresent()){
            return url.get().toString();
        } else {
            return "Not Found";
        }
    }

    @DeleteMapping("/api/doc")
    public Map<String, String> deleteFile(@RequestParam("file_name") String fileName)
    {
        s3Client.deleteFileFromS3Bucket(fileName);

        Map<String, String> response = new HashMap<>();
        response.put("message", "file [" + fileName + "] removing request submitted successfully.");

        return response;
    }

    private MediaType contentType(String keyname) {
        String[] arr = keyname.split("\\.");
        String type = arr[arr.length-1];
        switch(type) {
            case "txt": return MediaType.TEXT_PLAIN;
            case "png": return MediaType.IMAGE_PNG;
            case "jpg": return MediaType.IMAGE_JPEG;
            default: return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
