package com.example.emissionsCalculator.controller.prediction;

import com.example.emissionsCalculator.dto.prediction.WhatIfScenarioRequest;
import com.example.emissionsCalculator.dto.prediction.WhatIfScenarioResponse;
import com.example.emissionsCalculator.service.prediction.WhatIfPredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predict")
@RequiredArgsConstructor
public class WhatIfPredictionController {

    private final WhatIfPredictionService predictionService;

    @PostMapping("/what-if")
    public ResponseEntity<WhatIfScenarioResponse> simulateWhatIfScenario(@RequestBody WhatIfScenarioRequest request) {
        WhatIfScenarioResponse response = predictionService.simulateScenario(request);
        return ResponseEntity.ok(response);
    }
}