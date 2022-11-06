package com.example.project.service.impl;

import com.example.project.dto.dishDto.DishDTO;
import com.example.project.exceptions.DishNotExistsException;
import com.example.project.model.Category;
import com.example.project.model.Order;
import com.example.project.model.Dish;
import com.example.project.repository.IOrderRepository;
import com.example.project.repository.IDishRepository;
import com.example.project.service.IDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DishService implements IDishService {

    @Autowired
    IDishRepository dishRepository;

    @Autowired
    IOrderRepository orderRepository;

    @Override
    public void addDish(DishDTO dishDto, Category category) throws Exception {
        Dish dish = new Dish();
        assertDishIsNotExistByCode(dishDto.getCode());
        validateDishImage(dishDto);
        dish.setCode(dishDto.getCode());
        dish.setDescription(dishDto.getDescription());
        dish.setImageURL(dishDto.getImageURL());
        dish.setName(dishDto.getName());
        dish.setCategory(category);
        dish.setPrice(dishDto.getPrice());
        dish.setCheckDate(new Date());
        dish.setMinSales(dishDto.getMinSales());
        dish.setMaxSales(dishDto.getMaxSales());
        dishRepository.save(dish);
    }

    @Override
    public DishDTO getDishDto(Dish dish) {
        DishDTO dishDto = new DishDTO();
        dishDto.setCode(dish.getCode());
        dishDto.setDescription(dish.getDescription());
        dishDto.setImageURL(dish.getImageURL());
        dishDto.setName(dish.getName());
        dishDto.setCategoryId(dish.getCategory().getId());
        dishDto.setPrice(dish.getPrice());
        dishDto.setId(dish.getId());
        dishDto.setMinSales(dish.getMinSales());
        dishDto.setMaxSales(dish.getMaxSales());
        return dishDto;
    }

    @Override
    public Dish getDishByCode(String code) {
        return dishRepository.findDishByCode(code)
                .orElseThrow(() -> new DishNotExistsException("Dish code is invalid: " + code));
    }

    @Override
    public List<DishDTO> getAllDishes() {
        List<Dish> allDishes = dishRepository.findAll();
        List<DishDTO> dishDTOS = new ArrayList<>();

        for (Dish dish : allDishes) {
            dishDTOS.add(getDishDto(dish));
        }
        return dishDTOS;
    }


    @Override
    public void updateDish(DishDTO dishDto) throws Exception {
        Dish dish = dishRepository.findDishByCode(dishDto.getCode())
                .orElse(null);

        assertDishIsNotNull(dish);
        validateDishImage(dishDto);

        dish.setCode(dishDto.getCode());
        dish.setDescription(dishDto.getDescription());
        dish.setImageURL(dishDto.getImageURL());
        dish.setName(dishDto.getName());
        dish.setPrice(dishDto.getPrice());
        dish.setMinSales(dishDto.getMinSales());
        dish.setMaxSales(dishDto.getMaxSales());
        dish.setCostPrice(dishDto.getCostPrice());
        dishRepository.save(dish);
    }

    private void validateDishImage(DishDTO dishDto) throws Exception {
        if (dishDto.getImageURL().length() > 240) {
            throw new Exception("Image URL is too long!");
        }
    }

    @Override
    public Dish findDishById(Long dishId) throws DishNotExistsException {
        return dishRepository.findById(dishId)
                .orElseThrow(() -> new DishNotExistsException("Dish id is invalid: " + dishId));
    }

    @Override
    public void deleteDishById(Long dishId) {
        dishRepository.deleteById(dishId);
    }

    private LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private Date convertToDateViaSqlDate(LocalDate dateToConvert) {
        return java.sql.Date.valueOf(dateToConvert);
    }

    @Override
    public Date convertDate() {
        return convertToDateViaSqlDate(convertToLocalDateViaInstant(new Date()).minusMonths(1).minusDays(1));
    }

    @Override
    public void checkPrices() {
        List<Dish> dishes = dishRepository.findAll();

        Date todayDate = new Date();
        for (Dish dish : dishes) {
            List<Order> orders = orderRepository.findAllByCreatedDateBetween(dish.getCheckDate(), todayDate);
            double count = 0;
            for (var order : orders) {
                for (var orderUnit : order.getOrderUnits()) {
                    if (orderUnit.getDish().getId() == dish.getId()) {
                        count += orderUnit.getQuantity();
                    }
                }
            }
            if (count > dish.getMaxSales()) {
                dish.setPrice(Math.ceil(dish.getPrice() * 1.1));
            } else if (count < dish.getMinSales()) {
                Double newPrice = Math.max(Math.ceil(dish.getPrice() / 1.1), dish.getCostPrice() * 1.05);
                dish.setPrice(newPrice);
            }
            dish.setCheckDate(todayDate);
            dishRepository.save(dish);
        }

    }

    private void assertDishIsNotExistByCode(String dishCode) throws IllegalArgumentException {
        if (dishRepository.findDishByCode(dishCode)
                .orElse(null) != null) {
            throw new IllegalArgumentException("Dish with the same code has already existed!");
        }
    }

    private void assertDishIsNotNull(Dish dish) throws IllegalArgumentException {
        if (dish != null) {
            throw new IllegalArgumentException("Dish is not null!");
        }
    }

}