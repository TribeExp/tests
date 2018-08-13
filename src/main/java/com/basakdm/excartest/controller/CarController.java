package com.basakdm.excartest.controller;

import com.basakdm.excartest.dto.CarDTO;
import com.basakdm.excartest.entity.CarEntity;
import com.basakdm.excartest.enum_ent.car_enum.Transmission;
import com.basakdm.excartest.service.CarService;
import com.basakdm.excartest.utils.ConverterCars;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/car")
public class CarController {

    @Autowired
    private CarService carServiceImpl;

    @GetMapping("/all")
    public Collection<CarDTO> findAll(){
        return carServiceImpl.findAll().stream()
                .map(ConverterCars::mapCar)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{carId}")
    public ResponseEntity<CarDTO> findCarById(@PathVariable @Positive Long carId){
        return carServiceImpl.findById(carId)
                .map(ConverterCars::mapCar)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/createCar")
    public ResponseEntity<?> createCar(@RequestBody CarEntity carEntity){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ConverterCars.mapCar(carServiceImpl.createCar(carEntity)));
    }

    @DeleteMapping("/delete/{carId}")
    public void delete(@PathVariable @Positive Long carId){
        carServiceImpl.delete(carId);
    }

    @PostMapping ("/update")
    public void update(@RequestBody CarEntity car){
        carServiceImpl.update(car);
    }


    @GetMapping("/isActivated/False")
    public Collection<CarEntity> findAllByIsActivatedFalse(){
        return carServiceImpl.findAllByIsActivatedFalse();
    }
    @GetMapping("/isActivated/True")
    public Collection<CarEntity> findAllByIsActivatedTrue(){
        return carServiceImpl.findAllByIsActivatedTrue();
    }


    @GetMapping("/isFree/False")
    public Collection<CarEntity> findAllByIsFreeFalse(){
        return carServiceImpl.findAllByIsFreeFalse();
    }
    @GetMapping("/isFree/True")
    public Collection<CarEntity> findAllByIsFreeTrue(){
        return carServiceImpl.findAllByIsFreeTrue();
    }


    @GetMapping(value = "/getPhoto/{carId}")
    public String getPhotoById(@PathVariable @Positive Long carId){
        String photoReference = carServiceImpl.findById(carId).get().getPhoto();
        return photoReference;
    }

    @GetMapping(value = "/getLocation/{carId}")
    public String getLocationById(@PathVariable @Positive Long carId){
        String coordinates = carServiceImpl.findById(carId).get().getLocation();
        return coordinates;
    }

    @GetMapping(value = "/transmissionType/{transmission}")
    public Collection<CarEntity> getAllByTransmissionType(@PathVariable @Positive Transmission transmission){
        Collection<CarEntity> cars = carServiceImpl.findAllByTransmissionType(transmission);
        return cars;
    }






}
