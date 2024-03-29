package com.example.project.service.impl;

import com.example.project.dto.dish.DishDTO;
import com.example.project.dto.filter.FilterDishDTO;
import com.example.project.model.Category;
import com.example.project.model.Order;
import com.example.project.model.Dish;
import com.example.project.repository.IOrderRepository;
import com.example.project.repository.IDishRepository;
import com.example.project.service.ICategoryService;
import com.example.project.service.IDishService;
import com.example.project.util.TimeUtil;
import javassist.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DishService implements IDishService {

    @Value("${check.price.enabled:false}")
    private boolean checkPriceEnabled;
    @Value("${check.price.increase.percents:5}")
    private double checkPriceIncreasePercents;
    @Value("${check.price.decrease.percents:5}")
    private double checkPriceDecreasePercents;

    @Autowired
    ICategoryService categoryService;
    @Autowired
    IDishRepository dishRepository;
    @Autowired
    IOrderRepository orderRepository;

    private static final Logger LOG = LogManager.getLogger(DishService.class);

    @Override
    public void create(DishDTO dishDto) throws NotFoundException {
        Category category = categoryService.getCategoryById(dishDto.getCategoryId());

        if (category == null) {
            throw new NotFoundException(String.format("Category with Id %s was not found!", dishDto.getCategoryId()));
        }

        if (dishRepository.existsBySearchId(dishDto.getSearchId())) {
            throw new IllegalArgumentException(String.format("Dish with searchId %s already exists!", dishDto.getSearchId()));
        }

//        if (dishRepository.existsByNameEnOrNameUa(dishDto.getNameEn()) || dishRepository.existsByNameEnOrNameUa(dishDto.getNameUa())) {
//            throw new IllegalArgumentException(String.format("Dish with english name %s or ukrainian name already exists!",
//                            dishDto.getNameEn(), dishDto.getNameUa()));
//        }

        Dish dish = new Dish();
        dish.setSearchId(dishDto.getSearchId());
        dish.setDescriptionEn(dishDto.getDescriptionEn());
        dish.setDescriptionUa(dishDto.getDescriptionUa());
        dish.setImageData(dishDto.getImageData());
        dish.setNameEn(dishDto.getNameEn());
        dish.setNameUa(dishDto.getNameUa());
        dish.setCategory(category);
        dish.setPrice(dishDto.getPrice());
        dish.setCheckDate(TimeUtil.formatLocalDateTime(new LocalDateTime()));
        dish.setMinSales(dishDto.getMinSales());
        dish.setMaxSales(dishDto.getMaxSales());
        dish.setCostPrice(dishDto.getCostPrice());

        dishRepository.save(dish);
    }

    @Override
    public DishDTO getDishDto(Dish dish) {
        DishDTO dishDto = new DishDTO();

        dishDto.setId(dish.getId());
        dishDto.setNameEn(dish.getNameEn());
        dishDto.setNameUa(dish.getNameUa());
        dishDto.setSearchId(dish.getSearchId());
        dishDto.setDescriptionEn(dish.getDescriptionEn());
        dishDto.setDescriptionUa(dish.getDescriptionUa());
        dishDto.setImageData(dish.getImageData());
        dishDto.setCategoryId(dish.getCategory().getId());
        dishDto.setPrice(dish.getPrice());
        dishDto.setMinSales(dish.getMinSales());
        dishDto.setMaxSales(dish.getMaxSales());

        return dishDto;
    }

    @Override
    public Dish getDishBySearchId(String searchId) {
        return dishRepository.findDishBySearchId(searchId);
    }

    @Override
    public List<Dish> getFilteredDishes(FilterDishDTO filterDishDTO) {
        List<Dish> dishes = dishRepository.findAll();
        String filterName = filterDishDTO.getName();
        Double filterMinPrice = filterDishDTO.getMinPrice();
        Double filterMaxPrice = filterDishDTO.getMaxPrice();
        if (filterName != null) {
            dishes.retainAll(dishes.stream().filter(dish -> dish.getNameEn().equals(filterName) ||
                    dish.getNameUa().equals(filterName) ||
                    dish.getSearchId().equals(filterName)).collect(Collectors.toList()));
        }

        if (filterMinPrice != null) {
            dishes.retainAll(dishes.stream().filter(dish -> dish.getPrice() >= filterMinPrice).collect(Collectors.toList()));
        }

        if (filterMaxPrice != null) {
            dishes.retainAll(dishes.stream().filter(dish -> dish.getPrice() <= filterMaxPrice).collect(Collectors.toList()));
        }

        return dishes;
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
    public void update(DishDTO dishDto) throws NotFoundException {
        Dish updatedDish = dishRepository.getById(dishDto.getId());
        Dish existedDish = dishRepository.findDishBySearchId(dishDto.getSearchId());

        if (updatedDish == null) {
            throw new NotFoundException(String.format("Dish with id %s was not found!", dishDto.getId()));
        }

        if (existedDish != null && !updatedDish.getId().equals(existedDish.getId())) {
            throw new IllegalArgumentException(String.format("Dish with searchId %s already exists!", dishDto.getSearchId()));
        }

        updatedDish.setSearchId(dishDto.getSearchId());
        updatedDish.setDescriptionEn(dishDto.getDescriptionEn());
        updatedDish.setDescriptionUa(dishDto.getDescriptionUa());
        updatedDish.setImageData(dishDto.getImageData());
        updatedDish.setNameEn(dishDto.getNameEn());
        updatedDish.setNameUa(dishDto.getNameUa());
        updatedDish.setPrice(dishDto.getPrice());
        updatedDish.setMinSales(dishDto.getMinSales());
        updatedDish.setMaxSales(dishDto.getMaxSales());
        updatedDish.setCostPrice(dishDto.getCostPrice());

        dishRepository.save(updatedDish);
    }

    @Override
    public Dish findDishById(String dishId) {
        return dishRepository.getById(dishId);
    }

    @Override
    public void deleteDishById(String dishId) throws NotFoundException {
        if (!dishRepository.existsById(dishId)) {
            throw new NotFoundException(String.format("Dish with Id %s was not found!", dishId));
        }

        dishRepository.deleteById(dishId);
    }

    @Override
    public void checkPrices() {
        if (!checkPriceEnabled) {
            return;
        }
        List<Dish> dishes = dishRepository.findAll();

        LocalDateTime todayDate = TimeUtil.formatLocalDateTime(new LocalDateTime());
        for (Dish dish : dishes) {
            List<Order> orders = orderRepository.findAllByCreatedDateBetween(dish.getCheckDate(), todayDate);
            double count = 0;
            for (var order : orders) {
                for (var orderUnit : order.getOrderUnits()) {
                    if (orderUnit.getDish().getId().equals(dish.getId())) {
                        count += orderUnit.getQuantity();
                    }
                }
            }
            if (count > dish.getMaxSales()) {
                dish.setPrice(increasePrice(dish.getPrice(), checkPriceIncreasePercents));
            } else if (count < dish.getMinSales()) {
                Double newPrice = Math.max(Math.ceil(decreasePrice(dish.getPrice(), checkPriceDecreasePercents)),
                        Math.ceil(dish.getCostPrice() * 1.05));
                dish.setPrice(newPrice);
            }
            dish.setCheckDate(todayDate);
            dishRepository.save(dish);
        }
    }

    private double increasePrice(double price, double percent) {
        return Math.ceil(price + price / 100 * percent);
    }

    private double decreasePrice(double price, double percent) {
        return Math.ceil(price - price / 100 * percent);
    }
}
