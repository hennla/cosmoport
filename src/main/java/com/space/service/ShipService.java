package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.specification.ShipSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public interface ShipService {
    Page<Ship> getAllShips(ShipSpecification shipSpecification, PageRequest pageable);
    Long recordCount(ShipSpecification shipSpecification, PageRequest pageable);
    Ship createShip(Ship ship);
    Ship getShipById(Long id);
    Ship updateShip(Ship ship);
    void deleteShip(Ship ship);
}
