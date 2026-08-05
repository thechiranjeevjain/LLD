package com.example.parkinglot;

import com.example.parkinglot.domain.ParkingLot;
import com.example.parkinglot.domain.ParkingReceipt;
import com.example.parkinglot.domain.PaymentMethod;
import com.example.parkinglot.domain.SpotType;
import com.example.parkinglot.domain.Ticket;
import com.example.parkinglot.domain.Vehicle;
import com.example.parkinglot.domain.VehicleType;
import com.example.parkinglot.service.ParkingLotService;

import java.util.Map;

public final class ParkingLotApplication {

    private ParkingLotApplication() {
    }

    public static void main(String[] args) {
        ParkingLot parkingLot = ParkingLot.withFloors(
                "Central Parking",
                2,
                Map.of(
                        SpotType.MOTORCYCLE, 2,
                        SpotType.COMPACT, 3,
                        SpotType.LARGE, 1,
                        SpotType.EV, 1
                )
        );

        ParkingLotService service = new ParkingLotService(parkingLot);

        Ticket carTicket = service.park(new Vehicle("KA-01-HH-1234", VehicleType.CAR));
        Ticket bikeTicket = service.park(new Vehicle("KA-02-BB-0007", VehicleType.MOTORCYCLE));

        System.out.printf("Parked %s at %s%n", carTicket.vehicle().licensePlate(), carTicket.parkingSpot().spotId());
        System.out.printf("Parked %s at %s%n", bikeTicket.vehicle().licensePlate(), bikeTicket.parkingSpot().spotId());
        System.out.printf("Car-compatible spots available: %d%n", service.availableSpotCount(VehicleType.CAR));

        ParkingReceipt receipt = service.unpark(carTicket.ticketId(), PaymentMethod.UPI);
        System.out.printf(
                "Unparked %s from %s, fee=%s, payment=%s%n",
                receipt.vehicle().licensePlate(),
                receipt.spotId(),
                receipt.fee(),
                receipt.payment().status()
        );
    }
}

