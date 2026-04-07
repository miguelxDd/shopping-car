package com.prueba_cuscatlan.shopping_Car_miguel.controller;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ProductRequest;
import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ProductResponse;
import com.prueba_cuscatlan.shopping_Car_miguel.service.ProductService;
import com.prueba_cuscatlan.shopping_Car_miguel.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Local Products", description = "CRUD for locally managed products (local catalog)")
@RestController
@RequestMapping(Constants.PRODUCTS_PATH)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "List all local products (paginated)")
    @GetMapping
    ResponseEntity<Page<ProductResponse>> findAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    @Operation(summary = "Get a local product by ID")
    @GetMapping("/{id}")
    ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Operation(summary = "Create a new local product")
    @PostMapping
    ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @Operation(summary = "Update an existing local product")
    @PutMapping("/{id}")
    ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                           @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @Operation(summary = "Delete a local product")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
