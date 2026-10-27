package com.arecaharvest.service;

import com.arecaharvest.dto.request.FieldRequest;
import com.arecaharvest.dto.response.FieldResponse;
import com.arecaharvest.entity.Field;
import com.arecaharvest.entity.Owner;
import com.arecaharvest.entity.enums.FieldStatus;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.FieldRepository;
import com.arecaharvest.repository.HarvestSessionRepository;
import com.arecaharvest.repository.HarvestingContractRepository;
import com.arecaharvest.repository.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("FieldService Unit Tests")
@ExtendWith(MockitoExtension.class)
class FieldServiceTest {

    @Mock private FieldRepository fieldRepository;
    @Mock private OwnerRepository ownerRepository;
    @Mock private HarvestingContractRepository contractRepository;
    @Mock private HarvestSessionRepository sessionRepository;
    @Mock private AuditLogService auditLog;

    @InjectMocks private FieldService fieldService;

    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = Owner.builder().name("Shreedhar Bhat").build();
        owner.setId(1L);
    }

    @Test
    @DisplayName("Create field throws ResourceNotFoundException if owner does not exist")
    void createField_ownerNotFound_throwsException() {
        FieldRequest req = new FieldRequest();
        req.setOwnerId(999L);
        req.setFieldName("Betta Plot");

        when(ownerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fieldService.create(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Owner");
    }

    @Test
    @DisplayName("Create field persists new field with status ACTIVE")
    void createField_success() {
        FieldRequest req = new FieldRequest();
        req.setOwnerId(1L);
        req.setFieldName("Hill Plot");
        req.setAcreage(new BigDecimal("4.5"));
        req.setTreeCount(900);

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));

        Field savedField = Field.builder()
                .owner(owner)
                .fieldName("Hill Plot")
                .acreage(new BigDecimal("4.5"))
                .treeCount(900)
                .status(FieldStatus.ACTIVE)
                .build();
        savedField.setId(10L);

        when(fieldRepository.save(any(Field.class))).thenReturn(savedField);

        FieldResponse res = fieldService.create(req);

        assertThat(res).isNotNull();
        assertThat(res.getFieldName()).isEqualTo("Hill Plot");
        assertThat(res.getStatus()).isEqualTo(FieldStatus.ACTIVE);
        verify(fieldRepository, times(1)).save(any(Field.class));
        verify(auditLog, times(1)).log(eq("CREATE"), eq("FIELD"), eq(10L), anyString());
    }
}
