package com.example.order_management.service;

import com.example.order_management.dto.CustomerRequest;
import com.example.order_management.entity.Customer;
import com.example.order_management.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer createCustomer(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, CustomerRequest request) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setName(request.getName());
                    customer.setEmail(request.getEmail());
                    customer.setAddress(request.getAddress());
                    return customerRepository.save(customer);
                })
                .orElseThrow(() -> new RuntimeException("Customer tidak ditemukan"));
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer tidak ditemukan");
        }
        customerRepository.deleteById(id);
    }
}
