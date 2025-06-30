package org.klkt.klktaccouting.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.klkt.klktaccouting.repository.CateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreService.class);
    private final CateRepository cateRepository;


    public List<Map<String, Object>> get_list_cate_search(Map<String, Object> data) {
        try {
            return cateRepository.get_list_cate_search(data);
        } catch (Exception e) {
            // Xử lý lỗi không xác định
            LOGGER.error("Unexpected error in get_list_data_by_user: {}", data, e);
            throw new ServiceException("System busy, please try again!!!", e);
        }
    }

    public Map<String, Object> upsert_cate(Map<String, Object> data) {
        try {
            return cateRepository.upsert_cate(data);
        } catch (Exception e) {
            // Xử lý lỗi không xác định
            LOGGER.error("Unexpected error in upsert_tax_doc: {}", data, e);
            throw new ServiceException("System busy, please try again!!!", e);
        }
    }

    public Map<String, Object> update_status(Map<String, Object> data) {
        try {
            return cateRepository.update_status(data);
        } catch (Exception e) {
            // Xử lý lỗi không xác định
            LOGGER.error("Unexpected error in upsert_tax_doc: {}", data, e);
            throw new ServiceException("System busy, please try again!!!", e);
        }
    }
}
