package com.prueba_cuscatlan.shopping_Car_miguel.service;

import com.prueba_cuscatlan.shopping_Car_miguel.model.dto.ExternalProductDTO;

import java.util.List;

public interface ExternalProductService {

    List<ExternalProductDTO> findAll();

    ExternalProductDTO findById(Long id);

    List<String> findCategories();

    List<ExternalProductDTO> findByCategory(String category);
}
