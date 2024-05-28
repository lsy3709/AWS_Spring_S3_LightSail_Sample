package com.busanit501.spring_aws_s3.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;

@Service
public class S3Service {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${aws.bucketName}")
  private String bucketName;

  @Value("${aws.region}")
  private String region;

  public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
  }
  public byte[] downloadFile(String key) {
    try {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = s3Object.read(buffer)) != -1) {
        byteArrayOutputStream.write(buffer, 0, bytesRead);
      }
      return byteArrayOutputStream.toByteArray();
    } catch (S3Exception | IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public String uploadFile(MultipartFile file) {
    String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
    String folderPath = "test1234/test567/";
    String fullPath = folderPath + fileName;

    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(fullPath)
          .acl(ObjectCannedACL.PUBLIC_READ)
          .contentType(file.getContentType())  // Content-Type 설정
          .build();

      s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));
System.out.println("fullPath : " + fullPath);
// 유효시간 1시간.
//      return getPresignedUrl(fullPath);
      // 유효시간 퍼블릭으로
      return getPublicUrl(fullPath);
    } catch (S3Exception | IOException e) {
      e.printStackTrace();
      return "File upload failed: " + e.getMessage();
    }
  }
  // 유효시간 퍼블릭으로
  private String getPublicUrl(String objectKey) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);
  }

  private String getPresignedUrl(String objectKey) {
    GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
        .getObjectRequest(b -> b.bucket(bucketName).key(objectKey))
        .signatureDuration(Duration.ofHours(1))
        .build();

    PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
    URL url = presignedGetObjectRequest.url();
    return url.toString();
  }
}
