package com.zoopick.server.service;

import com.zoopick.server.config.FastApiProperties;
import com.zoopick.server.dto.vision.VisionAnalyzeRequest;
import com.zoopick.server.dto.vision.VisionAnalyzeResponse;
import com.zoopick.server.exception.BadRequestException;
import com.zoopick.server.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class VisionService {
    private final RestClient fastApiRestClient;
    private final FastApiProperties fastApiProperties;

    public VisionAnalyzeResponse analyzeImage(VisionAnalyzeRequest request) {
        String imageUrl = request.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BadRequestException("이미지 URL이 올바르지 않습니다.", "imageUrl is null or blank");
        }
        String url = fastApiProperties.getBaseUrl() + fastApiProperties.getVision().getAnalyzePath();

        try {
            VisionAnalyzeResponse response = fastApiRestClient.post()
                    .uri(url)
                    .body(request)
                    .retrieve()
                    .body(VisionAnalyzeResponse.class);
            if (response == null) {
                throw new DataNotFoundException("분석 결과", imageUrl);
            }
            return response;
        } catch (BadRequestException | DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("FastAPI 요청 중 오류가 발생했습니다.", e.getMessage());
        }
    }
}