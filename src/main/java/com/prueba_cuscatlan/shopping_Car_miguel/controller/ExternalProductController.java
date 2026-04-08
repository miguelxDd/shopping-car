package com.prueba_cuscatlan.shopping_Car_miguel.controller;

import com.prueba_cuscatlan.shopping_Car_miguel.exception.ErrorResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ExternalProductDTO;
import com.prueba_cuscatlan.shopping_Car_miguel.service.ExternalProductService;
import com.prueba_cuscatlan.shopping_Car_miguel.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "External Products", description = "Proxy to FakeStore API — read-only product catalog")
@RestController
@RequestMapping(Constants.EXTERNAL_PRODUCTS_PATH)
@RequiredArgsConstructor
public class ExternalProductController {

    private final ExternalProductService externalProductService;

    @Operation(summary = "List all products from FakeStore API")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product list returned"),
        @ApiResponse(responseCode = "502", description = "FakeStore API unreachable (circuit open after retries)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<List<ExternalProductDTO>> findAll() {
        return ResponseEntity.ok(externalProductService.findAll());
    }

    @Operation(summary = "Get a product by ID from FakeStore API")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found",
            content = @Content(schema = @Schema(implementation = ExternalProductDTO.class))),
        @ApiResponse(responseCode = "404", description = "Product not found in FakeStore",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "502", description = "FakeStore API unreachable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    ResponseEntity<ExternalProductDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(externalProductService.findById(id));
    }

    @Operation(summary = "Get all product categories from FakeStore API")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category list returned"),
        @ApiResponse(responseCode = "502", description = "FakeStore API unreachable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/categories")
    ResponseEntity<List<String>> findCategories() {
        return ResponseEntity.ok(externalProductService.findCategories());
    }

    @Operation(summary = "Get products by category from FakeStore API")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Products in category returned"),
        @ApiResponse(responseCode = "502", description = "FakeStore API unreachable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/category/{category}")
    ResponseEntity<List<ExternalProductDTO>> findByCategory(@PathVariable String category) {
        return ResponseEntity.ok(externalProductService.findByCategory(category));
    }
}
