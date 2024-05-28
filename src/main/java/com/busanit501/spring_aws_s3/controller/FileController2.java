package com.busanit501.spring_aws_s3.controller;

import com.busanit501.spring_aws_s3.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api2/files")
public class FileController2 {

  private final S3Service s3Service;

  @Autowired
  public FileController2(S3Service s3Service) {
    this.s3Service = s3Service;
  }

  @PostMapping("/upload")
  public String uploadFile(@RequestParam("file") MultipartFile file) {
    String fileUrl = s3Service.uploadFile(file);
    return "<a href=\"" + fileUrl + "\" target=\"_blank\">View Uploaded File</a>";
  }

  @PostMapping("/multi-upload")
  public ResponseEntity<List<String>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
   try{
     List<String> urlList = List.of(files).stream()
         .map(s3Service::uploadFile)
//        .map(fileUrl -> "<a href=\"" + fileUrl + "\" target=\"_blank\">View Uploaded File</a>")
         .collect(Collectors.toList());
     return ResponseEntity.ok(urlList);
   }
   catch (MaxUploadSizeExceededException e) {
     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of("One or more files exceed size limit!"));
   }
  }

  @GetMapping("/download")
  public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam String key) {
    byte[] data = s3Service.downloadFile(key);
    if (data == null) {
      return ResponseEntity.notFound().build();
    }

    ByteArrayResource resource = new ByteArrayResource(data);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
        .body(resource);
  }
}
