package com.example.project.service;

import com.example.project.dto.dish.DishDTO;
import com.example.project.dto.filter.FilterDishDTO;
import com.example.project.model.Dish;
import javassist.NotFoundException;

import java.util.List;

public interface IDishService {

    void create(DishDTO dishDto) throws NotFoundException;

    DishDTO getDishDto(Dish dish);

    Dish getDishBySearchId(String searchId);

    List<Dish> getFilteredDishes(FilterDishDTO filterDishDTO);

    List<DishDTO> getAllDishes();

    void update(DishDTO dishDto) throws NotFoundException;

    Dish findDishById(String dishId);

    void deleteDishById(String dishId) throws NotFoundException;

    void checkPrices();
}
