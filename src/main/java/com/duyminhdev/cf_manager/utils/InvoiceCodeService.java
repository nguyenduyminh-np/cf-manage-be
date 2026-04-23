package com.duyminhdev.cf_manager.utils;

import com.duyminhdev.cf_manager.entity.InvoiceSequence;
import com.duyminhdev.cf_manager.repository.InvoiceSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceCodeService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    private final InvoiceSequenceRepository sequenceRepository;

    public InvoiceCodeService(InvoiceSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Transactional
    public String generateInvoiceCode() {
        // lấy thời gian hiện tại dạng Instant (chuẩn hệ thống)
        Instant now = Instant.now();

        // convert sang ngày theo timezone business
        LocalDate today = now.atZone(ZONE_ID).toLocalDate();

        // lock row theo ngày
        InvoiceSequence sequence = sequenceRepository.findByDateForUpdate(today)
                .orElseGet(() -> {
                    InvoiceSequence newSeq = new InvoiceSequence();
                    newSeq.setSeqDate(today);
                    newSeq.setCurrentValue(0L);
                    return newSeq;
                });

        long nextValue = sequence.getCurrentValue() + 1;
        sequence.setCurrentValue(nextValue);

        sequenceRepository.save(sequence);

        String datePart = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("HD-%s-%04d", datePart, nextValue);
    }
}