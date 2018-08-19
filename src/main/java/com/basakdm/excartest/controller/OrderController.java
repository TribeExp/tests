package com.basakdm.excartest.controller;

import com.basakdm.excartest.dto.OrderDTO;
import com.basakdm.excartest.entity.CarEntity;
import com.basakdm.excartest.entity.OrderEntity;
import com.basakdm.excartest.request_models.order_models.OrderIdAndPriceAdd;
import com.basakdm.excartest.service.CarService;
import com.basakdm.excartest.service.OrderService;
import com.basakdm.excartest.utils.converters.ConvertOrders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private CarService carServiceImpl;

    /**
     * Get all Orders.
     * @return {@link ResponseEntity<Collection<OrderDTO>>}.
     */
    @GetMapping("/all")
    public ResponseEntity<Collection<OrderDTO>> findAll(){
        log.info("(/order/all), findAll()");
        return ResponseEntity.ok(orderService.findAll().stream()
                .map(ConvertOrders::mapOrder)
                .collect(Collectors.toList()));
    }

    /**
     * Find Orders by id
     * @param id orders unique identifier.
     * @return Optional with order, if order was founded. Empty optional in opposite case.
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<OrderDTO> findUserById(@PathVariable @Positive Long id){
        log.info("(/order/{id}), findUserById()");
        return orderService.findById(id)
                .map(ConvertOrders::mapOrder)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create Order.
     * @param orderEntity params for create a new order.
     * @return Created order with id.
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody OrderEntity orderEntity){
        log.info("(/order/create), create()");
        orderEntity.setIsActivated(false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(orderEntity));
    }

    /**
     * Activate order
     * @param id order id
     * @return {@link ResponseEntity} OK - if is activated, CONFLICT if catch exception
     */
    @PostMapping("/activate/{id}")
    public ResponseEntity activate(@PathVariable @Positive Long id){
        OrderEntity orderEntity;
        try {
            orderEntity = orderService.findById(id)
                    .orElseThrow(() -> new Exception("Order not found"));
        } catch (Exception e) {
            log.info("(/order/activate), activate() - catch exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        orderEntity.setIsActivated(true);
        orderService.update(orderEntity);
        log.info("(/order/activate), activate() - with id: " + orderEntity.getId() + " is activate");
        return ResponseEntity.ok("Order with id: " + orderEntity.getId() + " is activate");
    }

    /**
     * Delete order by id.
     * @param id order params for delete a order.
     * @return {@link ResponseEntity}
     */
    @PostMapping ("/delete/{id}")
    public ResponseEntity delete(@PathVariable @Positive Long id){
        log.info("(/order/delete/{id}), delete()");
        orderService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Update order by id.
     * @param orderEntity order params for update a order.
     * @return {@link ResponseEntity}
     */
    @PostMapping ("/update")
    public ResponseEntity update(@RequestBody OrderEntity orderEntity){
        log.info("(/order/update), update()");
        orderService.update(orderEntity);
        return ResponseEntity.ok().build();
    }

    /**
     * Get the number of days how many user use the machine
     * @param orderId order params for update a order.
     * @return {@link ResponseEntity}  amount of days.
     */
    @GetMapping(value = "/getAmountOfDaysById/{orderId}")
    public ResponseEntity getAmountOfDaysById(@PathVariable @Positive Long orderId){
        log.info("(/order/getAmountOfDaysById/{orderId}), getAmountOfDaysById()");
        return orderService.findById(orderId)
                .map(o -> ResponseEntity.ok(o.getAmountOfDays()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Calculate and get the last day when the user will ride by car
     * @param orderId order params for find a order, in which we will do the calculation.
     * @return {@link ResponseEntity} date of taking car.
     */
    @GetMapping(value = "/calcDateFromMomentOfTakingCar/{orderId}")
    public ResponseEntity calcDateFromMomentOfTakingCar(@PathVariable @Positive Long orderId){
        log.info("(/order/calcDateFromMomentOfTakingCar/{orderId}), calcDateFromMomentOfTakingCar()");
        Integer amountOfDays = (Integer) getAmountOfDaysById(orderId).getBody();
        log.info("amountOfDays = " + amountOfDays);
        Optional<OrderEntity> optionalOrderEntity = orderService.findById(orderId);
        OrderEntity orderEntity = null;
        try {
            orderEntity = optionalOrderEntity.orElseThrow(() -> new Exception("Order not found"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        Date firstDay = orderEntity.getFromWhatDate();
        log.info("firstDay = " + firstDay);

        Date lastDay = firstDay;

        Calendar calendar = Calendar.getInstance();

        log.info("calendar.setTime(lastDay)");
        calendar.setTime(lastDay);
        calendar.add(Calendar.DAY_OF_WEEK, amountOfDays);

        lastDay = (Date) calendar.getTime();
        log.info("lastDay = " + lastDay);
        return ResponseEntity.ok(lastDay);
    }

    /**
     * Calculate and get the last day when the user will ride by car
     * @param orderId order params for find a order, in which we will do the calculation.
     * @return  {@link ResponseEntity}.
     */
    @GetMapping(value = "/getPriceAdd/{orderId}")
    public ResponseEntity getPriceAdd(@PathVariable @Positive Long orderId){
        log.info("order/getPriceAdd/{orderId}, getPriceAdd()");
        return orderService.findById(orderId)
                .map(o -> ResponseEntity.ok(o.getPriceAdd()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Set cell with value-added order.
     * @param idAndPrice an object that contains an identifier and a price, to search the table.
     * @return  {@link ResponseEntity}.
     */
    @PostMapping ("/setPriceAdd")
    public ResponseEntity setPriceAdd(@RequestBody OrderIdAndPriceAdd idAndPrice){
        log.info("order/setPriceAdd, setPriceAdd()");
        Optional<OrderEntity> optionalOrderEntity = orderService.findById(idAndPrice.getOrderId());
        OrderEntity orderEntity = optionalOrderEntity.get();
        orderEntity.setPriceAdd(idAndPrice.getPriceAdd());
        log.info("orderEntity.setPriceAdd");

        orderService.saveOrder(orderEntity);
        log.info("orderRepositoryDAO.saveAndFlush(orderEntity);");
        return ResponseEntity.ok().build();
    }

    /**
     * Getting for a priceAdd from the order, by carId
     * @param carId params for get addPrice.
     * @return  {@link ResponseEntity}.
     */
    @GetMapping(value = "/getPriceAddByIdCar/{carId}")
    public ResponseEntity getPriceAddByIdCar(@PathVariable @Positive Long carId){
        log.info("(order/getPriceAddByIdCar/{carId}), getPriceAddByIdCar()");
        return orderService.findByIdCar(carId)
                .map(o -> ResponseEntity.ok(o.getPriceAdd()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Obtaining an Order object, by identifier tsar, with which you can access any cell.
     * @param carId the identifier of the machine by which we will search for the order.
     * @return {@link ResponseEntity}
     */
    //
    @GetMapping(value = "/getOrderByIdCar/{carId}")
    public ResponseEntity getOrderEntityByIdCar(@PathVariable @Positive Long carId){
        log.info("(order/getOrderByIdCar/{carId}), getOrderEntityByIdCar()");
        return orderService.findByIdCar(carId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get carEntity to access any field in the table car.
     * @param carId params for get finPrice.
     * @return  {@link ResponseEntity}.
     */
    @GetMapping(value = "/getCarEntityByIdCar/{carId}")
    public ResponseEntity<CarEntity> getCarEntityById(@PathVariable @Positive Long carId){
        log.info("(order/getCarEntityByIdCar/{carId}), getCarEntityById()");
        return carServiceImpl.findById(carId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get calculated finPrice(final price)
     * @param carId params for get finPrice.
     * @return  {@link ResponseEntity}.
     */
    @GetMapping(value = "/getFinPriceByIdCar/{carId}")
    public ResponseEntity getFinPriceByIdCar(@PathVariable @Positive Long carId){
        log.info("(order/getFinPriceByIdCar/{carId}), getFinPriceByIdCar()");
        return orderService.findByIdCar(carId)
                .map(o -> ResponseEntity.ok(o.getFinPrice()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Find orders by (isActivated = true).
     * @return  {@link ResponseEntity<Collection<OrderEntity>>}.
     */
    @GetMapping("/isActivated/True")
    public ResponseEntity<Collection<OrderEntity>> findAllByIsActivatedTrue(){
        log.info("(/order/isActivated/true), findAllByIsActivatedTrue()");
        return ResponseEntity.ok(orderService.findAllByIsActivatedTrue());
    }

    /**
     * Set final price(calculate)
     * @param carId params for set finPrice.
     * @return {@link ResponseEntity}
     */
    @PostMapping ("/setFinPriceByIdCar/{carId}")
    public ResponseEntity setFinPriceByIdCar(@PathVariable @Positive Long carId){
        log.info("(order/setFinPriceByIdCar/{carId}), setFinPriceByIdCar()");
        Long finPrice;
        if (getPriceAddByIdCar(carId) == null) {
            finPrice = ((CarEntity)getCarEntityById(carId).getBody()).getPrice() * ((OrderEntity)getOrderEntityByIdCar(carId).getBody()).getAmountOfDays();
            log.info("(getPriceAddByIdCar = null, finPrice = " + finPrice);
        } else {
            finPrice = ((CarEntity)getCarEntityById(carId).getBody()).getPrice() * ((OrderEntity)getOrderEntityByIdCar(carId).getBody()).getAmountOfDays() + ((Long)getPriceAddByIdCar(carId).getBody());
            log.info("(getPriceAddByIdCar = null, finPrice = " + finPrice);
        }

        OrderEntity orderEntity = null;
        try {
            orderEntity = orderService.findByIdCar(carId).orElseThrow(() -> new Exception("Order not found"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        orderEntity.setFinPrice(finPrice);

        orderService.saveOrder(orderEntity);
        log.info("orderRepositoryDAO.saveAndFlush(orderEntity)");
        return ResponseEntity.ok().build();
    }

}
