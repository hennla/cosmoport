package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.specification.Condition;
import com.space.repository.specification.Coparison;
import com.space.repository.specification.ShipSpecification;
import com.space.repository.specification.Type;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController()
public class ShipController {
    private final Double MIN_SPEED = 0.01;
    private final Double MAX_SPEED = 0.99;
    private final Integer MIN_CREW = 1;
    private final Integer MAX_CREW = 9999;
    private final Long MIN_PROD_DATE = 26192239200784L;
    private final Long MAX_PROD_DATE = 33134738399776L;
    private final Double MIN_RATING = 0.0;
    private final Double MAX_RATING = 80.0;
    private final Integer MAX_LENGTH = 50;

    @Autowired
    private ShipService shipServiceImpl;

    private String getOrder(String order) {
      if (order == null || order.toUpperCase().equals("ID")) {
          return "id";
      } else if (order.toUpperCase().equals("SPEED")) {
          return "speed";
      } else if (order.toUpperCase().equals("DATE")) {
          return "prodDate";
      } else {
          return "rating";
      }
    }

    private Date startYear(Long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date+1);
        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
        return calendar.getTime();
    }

    private Date endYear(Long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date-1);
        calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31, 23, 59, 59);
        return calendar.getTime();
    }

    private Boolean checkInputParam(Ship ship, boolean isCreate) {
        if (ship.getName() != null) {
            if (ship.getName() == "" || ship.getName().length() > MAX_LENGTH) {
                return false;
            }
        } else if (isCreate) {
            return false;
        }
        if (ship.getPlanet() != null) {
            if (ship.getPlanet() == "" || ship.getPlanet().length() > MAX_LENGTH) {
                return false;
            }
        } else if (isCreate) {
            return false;
        }
        if (ship.getSpeed() != null) {
            if (ship.getSpeed() < MIN_SPEED || ship.getSpeed() > MAX_SPEED) {
                return false;
            }
        } else if (isCreate) {
            return false;
        }
        if (ship.getCrewSize() != null) {
            if (ship.getCrewSize() < MIN_CREW || ship.getCrewSize() > MAX_CREW) {
                return false;
            }
        } else if (isCreate) {
            return false;
        }

        Calendar startProdDate = Calendar.getInstance();
        startProdDate.setTimeInMillis(MIN_PROD_DATE);
        Calendar endProdDate = Calendar.getInstance();
        endProdDate.setTimeInMillis(MAX_PROD_DATE);
        if (ship.getProdDate() != null) {
            if (ship.getProdDate().compareTo(startProdDate.getTime()) < 0 || ship.getProdDate().compareTo(endProdDate.getTime()) > 0) {
                return false;
            }
        } else if (isCreate) {
            return false;
        }

        if (ship.getUsed() == null && isCreate) {
            ship.setUsed(false);
        }
        return true;
    }

    private Long getId(String id) {
       Long verifiedId;
        if (id == null) {
            return null;
        }
       try {
           verifiedId = Long.parseLong(id);
       } catch (NumberFormatException e) {
           return null;
       }
       if (verifiedId <= 0) {
           return null;
       }
       return verifiedId;
    }

    private ShipSpecification getShipSpecifications(String name, String planet, String shipType, Long after, Long before, Boolean isUsed,
                                                    Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                                                    Double minRating, Double maxRating) {
        ShipSpecification shipSpecification = new ShipSpecification();
        if (name != null) {
            shipSpecification.addCondition(new Condition(name, null, Coparison.LIKE, "name", Type.OTHER));
        }
        if (planet != null) {
            shipSpecification.addCondition(new Condition(planet, null, Coparison.LIKE, "planet", Type.OTHER));
        }
        if (after != null && before != null) {
            shipSpecification.addCondition(new Condition(startYear(after), endYear(before), Coparison.BETWEEN, "prodDate", Type.DATE));
        }

        if (minSpeed != null || maxSpeed != null) {
            if (minSpeed == null) {
                minSpeed = MIN_SPEED;
            }
            if (maxSpeed == null) {
                maxSpeed = MAX_SPEED;
            }
            shipSpecification.addCondition(new Condition(minSpeed, maxSpeed, Coparison.BETWEEN, "speed", Type.DOUBLE));
        }

        if (minCrewSize != null || maxCrewSize != null) {
            if (minCrewSize == null) {
                minCrewSize = MIN_CREW;
            }
            if (maxCrewSize == null) {
                maxCrewSize = MAX_CREW;
            }
            shipSpecification.addCondition(new Condition(minCrewSize, maxCrewSize, Coparison.BETWEEN, "crewSize", Type.INTEGER));
        }

        if (minRating != null || maxRating != null) {
            if (minRating == null) {
                minRating = MIN_RATING;
            }
            if (maxRating == null) {
                maxRating = MAX_RATING;
            }
            shipSpecification.addCondition(new Condition(minRating, maxRating, Coparison.BETWEEN, "rating",Type.DOUBLE));
        }

        if (isUsed != null) {
            shipSpecification.addCondition(new Condition(isUsed, null, Coparison.EQUAL, "isUsed", Type.OTHER));
        }

        if (shipType != null) {
            shipSpecification.addCondition(new Condition(ShipType.valueOf(shipType), null, Coparison.EQUAL, "shipType", Type.OTHER));
        }
        return shipSpecification;
    }

    @GetMapping("rest/ships")
    public ResponseEntity<List<Ship>> getShips(@RequestParam(value = "name", required = false) String name,
                                               @RequestParam(value = "planet", required = false) String planet,
                                               @RequestParam(value = "shipType", required = false) String shipType,
                                               @RequestParam(value = "after", required = false) Long after,
                                               @RequestParam(value = "before", required = false) Long before,
                                               @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                               @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                               @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                               @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                               @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                               @RequestParam(value = "minRating", required = false) Double minRating,
                                               @RequestParam(value = "maxRating", required = false) Double maxRating,
                                               @RequestParam(value = "order", required = false, defaultValue = "id") String order,
                                               @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {



       PageRequest pageable =  PageRequest.of(pageNumber, pageSize, Sort.by(getOrder(order)));

       Page<Ship> ships = shipServiceImpl.getAllShips(getShipSpecifications(name, planet, shipType, after, before, isUsed,
               minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating), pageable);

       return ResponseEntity.ok().body(ships.getContent());
    }

    @GetMapping("rest/ships/count")
    public ResponseEntity<Long> getRecordCount(@RequestParam(value = "name", required = false) String name,
                                               @RequestParam(value = "planet", required = false) String planet,
                                               @RequestParam(value = "shipType", required = false) String shipType,
                                               @RequestParam(value = "after", required = false) Long after,
                                               @RequestParam(value = "before", required = false) Long before,
                                               @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                               @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                               @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                               @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                               @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                               @RequestParam(value = "minRating", required = false) Double minRating,
                                               @RequestParam(value = "maxRating", required = false) Double maxRating,
                                               @RequestParam(value = "order", required = false, defaultValue = "id") String order,
                                               @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        PageRequest pageable =  PageRequest.of(pageNumber, pageSize, Sort.by(getOrder(order)));

        Long count = shipServiceImpl.recordCount(getShipSpecifications(name, planet, shipType, after, before, isUsed,
                minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating), pageable);
        return ResponseEntity.ok().body(count);
    }

    @PostMapping("rest/ships")
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        if (!checkInputParam(ship, true)) {
            return ResponseEntity.badRequest().body(null);
        }
        if (shipServiceImpl.createShip(ship) == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok().body(ship);
    }

    @GetMapping("rest/ships/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable String id) {
        Long verifiedId = getId(id);
        if (verifiedId == null) {
            return ResponseEntity.badRequest().body(null);
        }
        Ship ship = shipServiceImpl.getShipById(verifiedId);
        if (ship == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ship);
    }

    @PostMapping("rest/ships/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable String id, @RequestBody Ship ship) {
        Long verifiedId = getId(id);
        if (verifiedId == null || !checkInputParam(ship, false)) {
            return ResponseEntity.badRequest().body(null);
        }
        Ship updatedShip = shipServiceImpl.getShipById(verifiedId);
        if (updatedShip == null) {
            return ResponseEntity.notFound().build();
        }
        boolean needRaintinUpdate = false;
        if (ship.getName() != null) { updatedShip.setName(ship.getName());}
        if (ship.getPlanet() != null) { updatedShip.setPlanet(ship.getPlanet());}
        if (ship.getShipType()!= null) { updatedShip.setShipType(ship.getShipType());}
        if (ship.getSpeed() != null) { updatedShip.setSpeed(ship.getSpeed()); needRaintinUpdate = true;}
        if (ship.getProdDate() != null) { updatedShip.setProdDate(ship.getProdDate());needRaintinUpdate = true;}
        if (ship.getCrewSize() != null) { updatedShip.setCrewSize(ship.getCrewSize());}
        if (ship.getUsed() != null) { updatedShip.setUsed(ship.getUsed()); needRaintinUpdate = true;}
        if (needRaintinUpdate) {updatedShip.calculateRating();}
        updatedShip = shipServiceImpl.updateShip(updatedShip);
        if (updatedShip == null) {
            return ResponseEntity.badRequest().body(ship);
        }
        return ResponseEntity.ok(updatedShip);
    }

    @DeleteMapping("rest/ships/{id}")
    public ResponseEntity<String> deleteShip(@PathVariable String id) {
        Long verifiedId = getId(id);
        if (verifiedId == null) {
            return ResponseEntity.badRequest().body(null);
        }
        Ship ship = shipServiceImpl.getShipById(verifiedId);
        if (ship == null) {
            return ResponseEntity.notFound().build();
        }
        shipServiceImpl.deleteShip(ship);
        return ResponseEntity.ok(id);
    }
}
