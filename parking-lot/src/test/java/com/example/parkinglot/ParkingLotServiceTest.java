package com.example.parkinglot;

import com.example.parkinglot.domain.ParkingLot;
import com.example.parkinglot.domain.ParkingReceipt;
import com.example.parkinglot.domain.PaymentMethod;
import com.example.parkinglot.domain.PaymentStatus;
import com.example.parkinglot.domain.SpotType;
import com.example.parkinglot.domain.Ticket;
import com.example.parkinglot.domain.TicketStatus;
import com.example.parkinglot.domain.Vehicle;
import com.example.parkinglot.domain.VehicleType;
import com.example.parkinglot.exception.InvalidTicketException;
import com.example.parkinglot.exception.ParkingLotFullException;
import com.example.parkinglot.exception.VehicleAlreadyParkedException;
import com.example.parkinglot.service.InMemoryPaymentProcessor;
import com.example.parkinglot.service.ParkingLotService;
import com.example.parkinglot.strategy.HourlyPricingStrategy;
import com.example.parkinglot.strategy.NearestAvailableSpotStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingLotServiceTest {
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-08-05T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Test
    void parksVehicleInNearestCompatiblePreferredSpot() {
        ParkingLotService service = serviceWith(ParkingLot.withFloors("Test Lot", 1, Map.of(
                SpotType.MOTORCYCLE, 1,
                SpotType.COMPACT, 1,
                SpotType.LARGE, 1,
                SpotType.EV, 1
        )));

        Ticket bikeTicket = service.park(new Vehicle("KA-01-M-0001", VehicleType.MOTORCYCLE));
        Ticket carTicket = service.park(new Vehicle("KA-01-C-0001", VehicleType.CAR));
        Ticket electricTicket = service.park(new Vehicle("KA-01-E-0001", VehicleType.ELECTRIC_CAR));
        Ticket truckTicket = service.park(new Vehicle("KA-01-T-0001", VehicleType.TRUCK));

        assertEquals(SpotType.MOTORCYCLE, bikeTicket.parkingSpot().type());
        assertEquals(SpotType.COMPACT, carTicket.parkingSpot().type());
        assertEquals(SpotType.EV, electricTicket.parkingSpot().type());
        assertEquals(SpotType.LARGE, truckTicket.parkingSpot().type());
        assertEquals(4, service.activeTicketCount());
    }

    @Test
    void rejectsDuplicateActiveVehicle() {
        ParkingLotService service = serviceWith(ParkingLot.withFloors("Small Lot", 1, Map.of(SpotType.COMPACT, 2)));

        service.park(new Vehicle("ka-01-hh-1234", VehicleType.CAR));

        assertThrows(
                VehicleAlreadyParkedException.class,
                () -> service.park(new Vehicle("KA-01-HH-1234", VehicleType.CAR))
        );
    }

    @Test
    void throwsWhenNoCompatibleSpotIsAvailable() {
        ParkingLotService service = serviceWith(ParkingLot.withFloors("Small Lot", 1, Map.of(SpotType.COMPACT, 1)));

        service.park(new Vehicle("DL-01-AA-0001", VehicleType.CAR));

        assertThrows(
                ParkingLotFullException.class,
                () -> service.park(new Vehicle("DL-01-AA-0002", VehicleType.CAR))
        );
        assertThrows(
                ParkingLotFullException.class,
                () -> service.park(new Vehicle("DL-01-TR-0001", VehicleType.TRUCK))
        );
    }

    @Test
    void unparkChargesByRoundedUpHourAndReleasesSpot() {
        ParkingLotService service = serviceWith(ParkingLot.withFloors("Billing Lot", 1, Map.of(SpotType.COMPACT, 1)));
        Ticket ticket = service.park(new Vehicle("MH-01-AA-1000", VehicleType.CAR));

        clock.advance(Duration.ofMinutes(60).plusSeconds(1));
        ParkingReceipt receipt = service.unpark(ticket.ticketId(), PaymentMethod.CARD);

        assertEquals(TicketStatus.PAID, ticket.status());
        assertEquals(new BigDecimal("40.00"), receipt.fee());
        assertEquals(PaymentStatus.SUCCESS, receipt.payment().status());
        assertEquals(0, service.activeTicketCount());
        assertEquals(1, service.availableSpotCount(VehicleType.CAR));
        assertEquals(ticket.ticketId(), service.findClosedTicket(ticket.ticketId()).orElseThrow().ticketId());
    }

    @Test
    void rejectsUnknownOrAlreadyClosedTicket() {
        ParkingLotService service = serviceWith(ParkingLot.withFloors("Ticket Lot", 1, Map.of(SpotType.COMPACT, 1)));
        Ticket ticket = service.park(new Vehicle("TN-09-ZZ-1010", VehicleType.CAR));

        clock.advance(Duration.ofMinutes(10));
        service.unpark(ticket.ticketId(), PaymentMethod.CASH);

        assertThrows(InvalidTicketException.class, () -> service.unpark(ticket.ticketId(), PaymentMethod.CASH));
        assertThrows(InvalidTicketException.class, () -> service.unpark("T-999999", PaymentMethod.CASH));
    }

    private ParkingLotService serviceWith(ParkingLot parkingLot) {
        return new ParkingLotService(
                parkingLot,
                new NearestAvailableSpotStrategy(),
                HourlyPricingStrategy.defaultRates(),
                new InMemoryPaymentProcessor(),
                clock
        );
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
