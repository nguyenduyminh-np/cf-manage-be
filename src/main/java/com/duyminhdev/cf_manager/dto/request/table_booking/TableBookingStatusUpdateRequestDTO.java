package com.duyminhdev.cf_manager.dto.request.table_booking;

import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TableBookingStatusUpdateRequestDTO {
    @NotNull(message = "Mã đặt bàn không được để trống")
    @Positive(message = "Mã đặt bàn phải lớn hơn 0")
    private Integer bookingId;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Mã trạng thái đặt bàn (bookingStatus) không hợp lệ")
    private String bookingStatus;

  //  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant checkInAt;

 //   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant checkOutAt;
}
