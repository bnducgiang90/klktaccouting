package org.klkt.klktaccouting.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.klkt.klktaccouting.repository.CoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CoreService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreService.class);
    private final CoreRepository coreRepository;

    public List<Map<String, Object>> get_list_data_by_user(Map<String, Object> data) {
        try {
            return coreRepository.get_list_data_by_user(data);
        } catch (Exception e) {
            // Xử lý lỗi không xác định
            LOGGER.error("Unexpected error in get_list_data_by_user: {}", data, e);
            throw new ServiceException("System busy, please try again!!!", e);
        }
    }

    public List<Map<String, Object>> upsert_tax_doc(Map<String, Object> data) {
        try {
            return coreRepository.upsert_tax_doc(data);
        } catch (Exception e) {
            // Xử lý lỗi không xác định
            LOGGER.error("Unexpected error in get_list_data_by_user: {}", data, e);
            throw new ServiceException("System busy, please try again!!!", e);
        }
    }
}
