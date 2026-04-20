package com.duyminhdev.cf_manager.dto.request.table_booking;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class TableBookingDepositRequestDTO {

  @NotNull(message = "bookingId is required")
  @Positive(message = "bookingId must be > 0")
  private Integer bookingId;

    @DecimalMin(value = "0", inclusive = false, message = "Số tiền đặt cọc phải lớn hơn 0")
    private BigDecimal depositAmount;

  //  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant depositPaidAt;

    @Size(max = 255, message = "Mã giao dịch đặt cọc không được vượt quá 255 ký tự")
    private String depositTxnRef;
}
