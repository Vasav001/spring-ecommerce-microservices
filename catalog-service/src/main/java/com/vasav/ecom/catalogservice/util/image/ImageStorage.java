package com.vasav.ecom.catalogservice.util.image;

public interface ImageStorage {

    String scheme();

    UploadInstruction prepareUpload(String key, String contentType, long contentLength);
}