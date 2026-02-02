package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CloudinaryResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    CloudinaryResponse uploadImage(MultipartFile file);

    CloudinaryResponse uploadImage(MultipartFile file, String module);

    void deleteImage(String publicId);
}
