package com.arecaharvest.service;

import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.Vehicle;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.VehicleRepository;
import com.arecaharvest.repository.WorkerRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final WorkerRepository workerRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<Vehicle> search(String status, String type, Pageable pageable) {
        return PagedResponse.of(vehicleRepository.searchVehicles(status, type, pageable));
    }

    @Transactional(readOnly = true)
    public Vehicle getById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
    }

    @Transactional
    public Vehicle create(VehicleRequest req) {
        if (vehicleRepository.existsByRegistrationNumber(req.getRegistrationNumber())) {
            throw new BusinessException("Vehicle " + req.getRegistrationNumber() + " already registered");
        }
        Vehicle v = Vehicle.builder()
                .registrationNumber(req.getRegistrationNumber())
                .vehicleType(req.getVehicleType()).make(req.getMake()).model(req.getModel())
                .year(req.getYear()).fuelType(req.getFuelType())
                .status(Vehicle.VehicleStatus.ACTIVE)
                .insuranceExpiry(req.getInsuranceExpiry())
                .lastServiceDate(req.getLastServiceDate())
                .nextServiceDate(req.getNextServiceDate())
                .notes(req.getNotes())
                .build();
        if (req.getDriverId() != null) {
            v.setDriver(workerRepository.findById(req.getDriverId()).orElse(null));
        }
        Vehicle saved = vehicleRepository.save(v);
        auditLog.log("CREATE", "VEHICLE", saved.getId(), "Vehicle: " + saved.getRegistrationNumber());
        return saved;
    }

    @Transactional
    public Vehicle update(Long id, VehicleRequest req) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
        v.setMake(req.getMake()); v.setModel(req.getModel()); v.setYear(req.getYear());
        if (req.getFuelType() != null) v.setFuelType(req.getFuelType());
        v.setInsuranceExpiry(req.getInsuranceExpiry());
        v.setLastServiceDate(req.getLastServiceDate());
        v.setNextServiceDate(req.getNextServiceDate());
        v.setNotes(req.getNotes());
        if (req.getStatus() != null) v.setStatus(req.getStatus());
        if (req.getDriverId() != null) {
            v.setDriver(workerRepository.findById(req.getDriverId()).orElse(null));
        }
        auditLog.log("UPDATE", "VEHICLE", id, "Vehicle updated");
        return vehicleRepository.save(v);
    }

    @Getter @Setter
    public static class VehicleRequest {
        private String registrationNumber;
        private Vehicle.VehicleType vehicleType;
        private String make; private String model; private Integer year;
        private Long driverId; private Vehicle.FuelType fuelType;
        private Vehicle.VehicleStatus status;
        private LocalDate insuranceExpiry; private LocalDate lastServiceDate;
        private LocalDate nextServiceDate; private String notes;
    }
}
