package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.liqpay.LiqPayResponse;
import com.example.project.dto.liqpay.PayOptions;
import com.example.project.dto.order.response.CreateOrderItemDTO;
import com.example.project.dto.order.response.OrderDTO;
import com.example.project.dto.order.response.OrderItemDTO;
import com.example.project.dto.checkout.CheckoutItemDTO;
import com.example.project.dto.checkout.StripeResponse;
import com.example.project.model.OrderState;
import com.example.project.service.ILiqPayService;
import com.example.project.service.IOrderService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import javassist.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private ILiqPayService liqPayService;

    private static final Logger LOG = LogManager.getLogger(OrderController.class);

    @Async
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @GetMapping("/getOrders/")
    public CompletableFuture<ResponseEntity<OrderDTO>> getOrders(@RequestParam("userEmail") String userEmail) throws NotFoundException {
        List<OrderItemDTO> orders = orderService.getAllOrders(userEmail);
        OrderDTO orderDto = new OrderDTO();
        orderDto.setOrderItems(orders);
        double totalSum = 0;
        for (var el : orders) {
            totalSum += el.getPrice();
        }
        orderDto.setTotalCost(totalSum);
        return CompletableFuture.completedFuture(ResponseEntity.ok(orderDto));
    }

    @Async
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @GetMapping("/getOrder/")
    public CompletableFuture<ResponseEntity<OrderItemDTO>> getOrder(@RequestParam("id") String id) {
        OrderItemDTO order = orderService.getOrderById(id);
        return CompletableFuture.completedFuture(ResponseEntity.ok(order));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createOrder(@RequestBody CreateOrderItemDTO orderItemDTO) throws NotFoundException {
        orderService.createOrder(orderItemDTO);
        return new ResponseEntity<>(new ApiResponse(true, "Order created!"), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @PostMapping("/ready")
    public ResponseEntity<ApiResponse> setOrderReady(@RequestParam("id") String id) throws NotFoundException {
        orderService.setOrderState(id, OrderState.READY);
        return new ResponseEntity<>(new ApiResponse(true, "Order's state is READY now!"), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @PostMapping("/close")
    public ResponseEntity<ApiResponse> closeOrder(@RequestParam("id") String id) throws NotFoundException {
        orderService.setOrderState(id, OrderState.CLOSED);
        return new ResponseEntity<>(new ApiResponse(true, "Order's state is CLOSED now!"), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @PostMapping("/create-checkout-session")
    public ResponseEntity<StripeResponse> checkoutList(@RequestBody List<CheckoutItemDTO> checkoutItemDTOList)
            throws StripeException {
        Session session = orderService.createSession(checkoutItemDTOList);
        StripeResponse stripeResponse = new StripeResponse(session.getId());
        return new ResponseEntity<>(stripeResponse, HttpStatus.OK);

    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @PostMapping("/liqPay")
    public ResponseEntity<LiqPayResponse> liqPay()
            throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
        LiqPayResponse liqPayResponse = liqPayService.createSession(new PayOptions("3", "100", "pay", "UAH",
                "Donate for Armed Forces of Ukraine", UUID.randomUUID().toString()));
        return new ResponseEntity<>(liqPayResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @PostMapping("/liqPay2")
    public ResponseEntity<LiqPayResponse> liqPay(@RequestBody List<CheckoutItemDTO> checkoutItemDTOList)
            throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
        LiqPayResponse liqPayResponse = liqPayService.createSession(checkoutItemDTOList);
        return new ResponseEntity<>(liqPayResponse, HttpStatus.OK);
    }

}
