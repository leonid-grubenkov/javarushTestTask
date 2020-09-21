package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.CosmoportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class ShipServiceImpl implements ShipService{

    @Autowired
    private CosmoportRepository repository;

    private final Calendar prodCal = Calendar.getInstance();


    @Override
    public List<Ship> getShips(String name, String planet, ShipType shipType, Long before, Long after, Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating) {
        List<Ship> list = new ArrayList<>();
        repository.findAll().forEach((ship) -> {

            if (name != null && !ship.getName().contains(name)) return;
            if (planet != null && !ship.getPlanet().contains(planet)) return;
            if (shipType != null && ship.getShipType() != shipType) return;
            if (after != null && ship.getProdDate().getTime() > after) return;
            if (before != null && ship.getProdDate().getTime() < before) return;
            if (isUsed != null && ship.getUsed().booleanValue() != isUsed.booleanValue()) return;
            if (minSpeed != null && ship.getSpeed().compareTo(minSpeed) < 0) return;
            if (maxSpeed != null && ship.getSpeed().compareTo(maxSpeed) > 0) return;
            if (minCrewSize != null && ship.getCrewSize().compareTo(minCrewSize) < 0) return;
            if (maxCrewSize != null && ship.getCrewSize().compareTo(maxCrewSize) > 0) return;
            if (minRating != null && ship.getRating().compareTo(minRating) < 0) return;
            if (maxRating != null && ship.getRating().compareTo(maxRating) > 0) return;

            list.add(ship);
        });
        return list;
    }

    @Override
    public Ship createShip(Ship ship) {
        return repository.save(ship);
    }

    @Override
    public Ship getShipById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {

        if (newShip.getName() != null) {
            if (isStringValid(newShip.getName())){
                oldShip.setName(newShip.getName());
            } else throw new IllegalArgumentException();
        }

        if (newShip.getPlanet() != null) {
            if (isStringValid(newShip.getPlanet())) {
                oldShip.setPlanet(newShip.getPlanet());
            } else throw new IllegalArgumentException();
        }

        if (newShip.getShipType() != null) oldShip.setShipType(newShip.getShipType());

        if (newShip.getProdDate() != null) {
            if (isDateValid(newShip.getProdDate())) {
                oldShip.setProdDate(newShip.getProdDate());
            }
        }

        if (newShip.getUsed() != null){
            oldShip.setUsed(newShip.getUsed());
        }

        if (newShip.getSpeed() != null){
            if (isSpeedValid(newShip.getSpeed())){
                oldShip.setSpeed(newShip.getSpeed());
            } else throw new IllegalArgumentException();
        }

        if (newShip.getCrewSize() != null) {
            if (isCrewSizeValid(newShip.getCrewSize())){
                oldShip.setCrewSize(newShip.getCrewSize());
            } else throw new IllegalArgumentException();
        }

        if (newShip.getProdDate() != null) {
            if (isDateValid(newShip.getProdDate())){
                oldShip.setProdDate(newShip.getProdDate());
            } else throw new IllegalArgumentException();
        }

        oldShip.setRating(computeRating(oldShip.getSpeed(), oldShip.getUsed(), oldShip.getProdDate()));
        repository.save(oldShip);
        return oldShip;
    }

    @Override
    public void deleteShip(Ship ship) {
        repository.delete(ship);
    }

    @Override
    public List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > ships.size()) to = ships.size();
        return ships.subList(from, to);
    }


    @Override
    public List<Ship> sortShips(List<Ship> ships, ShipOrder order) {
        if (order != null) {
            ships.sort((ship1, ship2) -> {
                switch (order) {
                    case ID: return ship1.getId().compareTo(ship2.getId());
                    case SPEED: return ship1.getSpeed().compareTo(ship2.getSpeed());
                    case DATE: return ship1.getProdDate().compareTo(ship2.getProdDate());
                    case RATING: return ship1.getRating().compareTo(ship2.getRating());
                    default: return 0;
                }
            });
        }
        return ships;
    }

    @Override
    public boolean isShipValid(Ship ship) {
        return ship != null
                && isStringValid(ship.getName())
                && isStringValid(ship.getPlanet())
                && isDateValid(ship.getProdDate())
                && isCrewSizeValid(ship.getCrewSize())
                && isSpeedValid(ship.getSpeed());
    }

    @Override
    public double computeRating(double speed, boolean isUsed, Date prod) {
        final int year = 3019;
        final int prodYear = getYearFromDate(prod);
        final double k = isUsed ? 0.5 : 1;
        final double rating = 80 * speed * k / (year - prodYear + 1);
        return Math.round(rating * 100) / 100D;
    }

    private int getYearFromDate(Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    private boolean isStringValid(String line){
        return (line != null && line.length() <= 50 && !line.isEmpty());
    }

    private boolean isDateValid(Date date){
        if (date == null) return false;
        final int year = getYearFromDate(date);
        return year >= 2800 && year <= 3019;
    }

    private boolean isCrewSizeValid(Integer size){
        return size != null && size <= 9999 && size >= 1;
    }

    private boolean isSpeedValid(Double speed){
        return speed != null && speed >= 0.01 && speed <= 0.99;
    }


}
