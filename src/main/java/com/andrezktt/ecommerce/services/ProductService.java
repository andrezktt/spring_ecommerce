package com.andrezktt.ecommerce.services;

import com.andrezktt.ecommerce.dto.CategoryDTO;
import com.andrezktt.ecommerce.dto.ProductDTO;
import com.andrezktt.ecommerce.dto.ProductMinDTO;
import com.andrezktt.ecommerce.entities.Category;
import com.andrezktt.ecommerce.entities.Product;
import com.andrezktt.ecommerce.repositories.ProductRepository;
import com.andrezktt.ecommerce.services.exceptions.DatabaseException;
import com.andrezktt.ecommerce.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Transactional(readOnly = true)
    public Page<ProductMinDTO> findAll(String name, Pageable pageable) {
        return repository.searchByName(name, pageable).map(e -> new ProductMinDTO(e));
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        return new ProductDTO(repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado.")));
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product entity = new Product();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {
            Product entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);
            return new ProductDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Recurso não encontrado!");
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso não encontrado!");
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial!");
        }
    }

    private void copyDtoToEntity(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setImgUrl(dto.getImgUrl());
        entity.getCategories().clear();
        for (CategoryDTO categoryDTO : dto.getCategories()) {
            Category category = new Category();
            category.setId(categoryDTO.getId());
            entity.getCategories().add(category);
        }
    }
}
