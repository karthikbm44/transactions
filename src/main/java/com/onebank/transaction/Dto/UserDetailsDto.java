package com.onebank.transaction.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailsDto {
    private String accountHolderName;
    private AddressDto address;
    private String mobileNumber;
    private String accountNumber;
    private String emailId;

}
