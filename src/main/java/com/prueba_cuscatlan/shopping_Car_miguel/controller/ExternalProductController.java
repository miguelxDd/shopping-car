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

@Tag(name = "Products", description = "Product catalog — sourced from FakeStore API via Proxy + Circuit Breaker")
@RestController
@RequestMapping(Constants.PRODUCTS_PATH)
@RequiredArgsConstructor
public class ExternalProductController {

    private final ExternalProductService externalProductService;

    @Operation(summary = "List all products")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product list returned"),
        @ApiResponse(responseCode = "502", description = "FakeStore API unreachable (circuit open after retries)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<List<ExternalProductDTO>> findAll() {
        return ResponseEntity.ok(externalProductService.findAll());
    }

    @Operation(summary = "Get a product by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found",
            content = @Content(schema = @Schema(implementation = ExternalProductDTO.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "502", description = "FakeStore API unreachable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    ResponseEntity<ExternalProductDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(externalProductService.findById(id));
    }

    @Operation(summary = "Get all product categories")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category list returned"),
        @ApiResponse(responseCode = "502", description = "FakeStore API unreachable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/categories")
    ResponseEntity<List<String>> findCategories() {
        return ResponseEntity.ok(externalProductService.findCategories());
    }

    @Operation(summary = "Get products by category")
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
