package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import com.space.repository.specification.ShipSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ShipServiceImpl implements ShipService {
    @Autowired
    private ShipRepository shipRepository;

    public Page<Ship> getAllShips(ShipSpecification shipSpecification, PageRequest pageable) {
        Page<Ship> ships;
        if (shipSpecification.getSize() == 0) {
            ships = shipRepository.findAll(pageable);
        } else {
            ships = shipRepository.findAll(shipSpecification, pageable);
        }
        return ships;
    }

    public Long recordCount(ShipSpecification shipSpecification, PageRequest pageable) {
        Page<Ship> ships = getAllShips(shipSpecification, pageable);
        return ships.getTotalElements();
    }

    public Ship createShip(Ship ship) {
        ship.calculateRating();
        return shipRepository.save(ship);
    }

    public Ship getShipById(Long id) {
        Optional<Ship> shipById = shipRepository.findById(id);
        return shipById.orElse(null);
    }

    public Ship updateShip(Ship ship) {
        return shipRepository.save(ship);
    }

    public void deleteShip(Ship ship) {
       shipRepository.delete(ship);
    }
}
