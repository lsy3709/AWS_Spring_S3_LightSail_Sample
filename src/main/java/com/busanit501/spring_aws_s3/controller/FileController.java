package com.busanit501.spring_aws_s3.controller;

import com.busanit501.spring_aws_s3.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/api/files")
public class FileController {

  private final S3Service s3Service;

  @Autowired
  public FileController(S3Service s3Service) {
    this.s3Service = s3Service;
  }

  @GetMapping("/upload")
  public void uploadFile() {

  }

  @PostMapping("/upload")
  public String uploadFile(@RequestParam("file") MultipartFile file) {
    return s3Service.uploadFile(file);
  }
}







