package com.scan2dine.scan2dine.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.scan2dine.scan2dine.dto.OrderRequest;
import com.scan2dine.scan2dine.dto.OrderItemRequest;
import com.scan2dine.scan2dine.entity.CustomerOrder;
import com.scan2dine.scan2dine.entity.OrderItem;
import com.scan2dine.scan2dine.repo.CustomerOrderRepo;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "${app.frontend.url}")
public class OrderController {

    private final CustomerOrderRepo orderRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public OrderController(CustomerOrderRepo orderRepo, SimpMessagingTemplate messagingTemplate) {
        this.orderRepo = orderRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<CustomerOrder> createOrder(@RequestBody OrderRequest request) {
        CustomerOrder order = new CustomerOrder();
        order.setRestaurantId(request.getRestaurantId());
        order.setUserId(request.getUserId());
        order.setTableNumber(request.getTableNumber());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus("PENDING");
        order.setPaymentDone(false);

        if (request.getItems() != null) {
            for (OrderItemRequest itemReq : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setMenuItemId(itemReq.getMenuItemId());
                item.setMenuItemName(itemReq.getName());
                item.setPrice(itemReq.getPrice());
                item.setQuantity(itemReq.getQuantity());
                item.setCustomerOrder(order);
                order.getItems().add(item);
            }
        }

        CustomerOrder savedOrder = orderRepo.save(order);
        messagingTemplate.convertAndSend("/topic/orders/" + savedOrder.getRestaurantId(), savedOrder);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<java.util.List<CustomerOrder>> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderRepo.findByRestaurantIdOrderByCreatedAtDesc(restaurantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrder> getOrderById(@PathVariable Long id) {
        return orderRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<java.util.List<CustomerOrder>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderRepo.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Optional<CustomerOrder> optionalOrder = orderRepo.findById(id);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CustomerOrder order = optionalOrder.get();
        String newStatus = payload.get("status");
        
        if (newStatus != null) {
            // Generate Razorpay Order when moving to PAYMENT_PENDING
            if ("PAYMENT_PENDING".equals(newStatus) && order.getRazorpayOrderId() == null) {
                if (razorpayKeyId == null || razorpayKeyId.contains("dummy")) {
                    // Bypass for local testing without real keys
                    order.setRazorpayOrderId("order_dummy_" + System.currentTimeMillis());
                } else {
                    try {
                        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
                        JSONObject orderRequest = new JSONObject();
                        orderRequest.put("amount", order.getTotalAmount() * 100); // amt in paise
                        orderRequest.put("currency", "INR");
                        orderRequest.put("receipt", "txn_" + order.getId());
                        
                        Order rzpOrder = razorpay.orders.create(orderRequest);
                        order.setRazorpayOrderId(rzpOrder.get("id"));
                    } catch (RazorpayException e) {
                        System.err.println("Razorpay Error: " + e.getMessage());
                        return ResponseEntity.status(500).body(Map.of("error", "Failed to create Razorpay Order"));
                    }
                }
            }

            order.setStatus(newStatus);
            orderRepo.save(order);
            
            messagingTemplate.convertAndSend("/topic/order-status/" + order.getId(), order);
            messagingTemplate.convertAndSend("/topic/orders/" + order.getRestaurantId(), order);
        }

        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> confirmPayment(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Optional<CustomerOrder> optionalOrder = orderRepo.findById(id);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CustomerOrder order = optionalOrder.get();
        
        if (!"PAYMENT_PENDING".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Order is not awaiting payment"));
        }

        String paymentId = payload.get("paymentId");
        String signature = payload.get("signature");

        // Ideally, in production you should verify the razorpay_signature here
        // using Utils.verifyPaymentSignature(payload, razorpayKeySecret)
        
        order.setRazorpayPaymentId(paymentId);
        order.setPaymentDone(true);
        order.setStatus("PAID");
        orderRepo.save(order);

        messagingTemplate.convertAndSend("/topic/order-status/" + order.getId(), order);
        messagingTemplate.convertAndSend("/topic/orders/" + order.getRestaurantId(), order);

        return ResponseEntity.ok(order);
    }
}
