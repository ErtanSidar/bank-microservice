package com.essoft.accounts.service.impl;

import com.essoft.accounts.client.CardsFeignClient;
import com.essoft.accounts.client.LoansFeignClient;
import com.essoft.accounts.dto.AccountsDto;
import com.essoft.accounts.dto.CardsDto;
import com.essoft.accounts.dto.CustomerDetailsDto;
import com.essoft.accounts.dto.LoansDto;
import com.essoft.accounts.entity.Accounts;
import com.essoft.accounts.entity.Customer;
import com.essoft.accounts.exception.ResourceNotFoundException;
import com.essoft.accounts.mapper.AccountsMapper;
import com.essoft.accounts.mapper.CustomerMapper;
import com.essoft.accounts.repository.AccountsRepository;
import com.essoft.accounts.repository.CustomerRepository;
import com.essoft.accounts.service.ICustomersService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(correlationId, mobileNumber);
        customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());

        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(correlationId, mobileNumber);
        customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());

        return customerDetailsDto;

    }
}
